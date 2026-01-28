package com.increff.pos.flow;

import com.increff.pos.api.*;
import com.increff.pos.db.*;
import com.increff.pos.exception.ApiException;
import com.increff.pos.util.SequenceGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Flow layer for Order operations - orchestrates OrderApi, OrderItemApi,
 * InventoryApi, ProductApi, ClientApi
 */
@Service
public class OrderFlow {

    private final OrderApi orderApi;
    private final OrderItemApi orderItemApi;
    private final InventoryApi inventoryApi;
    private final ProductApi productApi;

    private final SequenceGenerator sequenceGenerator;

    public OrderFlow(
            OrderApi orderApi,
            OrderItemApi orderItemApi,
            InventoryApi inventoryApi,
            ProductApi productApi,
            SequenceGenerator sequenceGenerator) {
        this.orderApi = orderApi;
        this.orderItemApi = orderItemApi;
        this.inventoryApi = inventoryApi;
        this.productApi = productApi;
        this.sequenceGenerator = sequenceGenerator;
    }

    @Transactional(rollbackFor = ApiException.class)
    public OrderPojo createOrder(List<OrderItemPojo> orderItems) throws ApiException {

        // Generate orderId
        long orderNumber = sequenceGenerator.getNextSequence("order");
        String orderId = "ORD-" + String.format("%06d", orderNumber);

        // Calculate totals
        int totalItems = 0;
        double totalAmount = 0.0;

        // Validate products and reduce inventory
        List<OrderItemPojo> savedItems = new ArrayList<>();
        for (OrderItemPojo item : orderItems) {
            // Validate product exists
            ProductPojo product = productApi.get(item.getProductId());
            if (product == null) {
                throw new ApiException("Product with ID " + item.getProductId() + " does not exist");
            }

            // Check inventory
            InventoryPojo inventory = inventoryApi.getByProductId(item.getProductId());
            if (inventory == null || inventory.getQuantity() < item.getQuantity()) {
                throw new ApiException("Insufficient inventory for product " + product.getName() +
                        ". Available: " + (inventory != null ? inventory.getQuantity() : 0) +
                        ", Required: " + item.getQuantity());
            }

            // Reduce inventory
            int newQuantity = inventory.getQuantity() - item.getQuantity();
            inventoryApi.updateByProductId(item.getProductId(), newQuantity);

            // Set order item details
            item.setOrderId(orderId);
            item.setBarcode(product.getBarcode());
            item.setProductName(product.getName());
            item.setLineTotal(item.getQuantity() * item.getMrp());

            // Save order item
            OrderItemPojo savedItem = orderItemApi.add(item);
            savedItems.add(savedItem);

            // Update totals
            totalItems += item.getQuantity();
            totalAmount += item.getLineTotal();
        }

        // Create order
        OrderPojo order = new OrderPojo();
        order.setOrderId(orderId);
        order.setStatus("PLACED");
        order.setTotalItems(totalItems);
        order.setTotalAmount(totalAmount);
        order.setOrderDate(Instant.now());

        return orderApi.add(order);
    }

    public OrderPojo getOrderWithItems(String orderId) throws ApiException {
        return orderApi.getByOrderId(orderId);
    }

    public List<OrderItemPojo> getOrderItems(String orderId) {
        return orderItemApi.getByOrderId(orderId);
    }

    public List<OrderPojo> getOrderWithFilters(String orderId, String status, Instant fromDate, Instant toDate) {
        return orderApi.getWithFilters(orderId, status, fromDate, toDate);
    }

    @Transactional(rollbackFor = ApiException.class)
    public OrderPojo cancelOrder(String orderId) throws ApiException {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new ApiException("Order ID cannot be empty");
        }
        orderId = orderId.trim();

        OrderPojo order = orderApi.getByOrderId(orderId);

        if ("INVOICED".equals(order.getStatus())) {
            throw new ApiException("Order " + orderId + " is already invoiced and cannot be cancelled");
        }
        if ("CANCELLED".equals(order.getStatus())) {
            return order; // idempotent
        }

        // Invoice check is done by status - if INVOICED, cannot cancel

        // Restore inventory for all order items
        List<OrderItemPojo> items = orderItemApi.getByOrderId(orderId);
        for (OrderItemPojo item : items) {
            Integer currentQty = 0;
            try {
                InventoryPojo inv = inventoryApi.getByProductId(item.getProductId());
                currentQty = inv != null && inv.getQuantity() != null ? inv.getQuantity() : 0;
            } catch (ApiException e) {
                // inventory row missing => treat as 0, upsert will create
                currentQty = 0;
            }
            int newQty = currentQty + (item.getQuantity() != null ? item.getQuantity() : 0);
            inventoryApi.updateByProductId(item.getProductId(), newQty);
        }

        // Mark order cancelled
        OrderPojo patch = new OrderPojo();
        patch.setStatus("CANCELLED");
        return orderApi.update(order.getId(), patch);
    }

    @Transactional(rollbackFor = ApiException.class)
    public OrderPojo updateOrder(String orderId, List<OrderItemPojo> newOrderItems) throws ApiException {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new ApiException("Order ID cannot be empty");
        }
        orderId = orderId.trim();

        // Get existing order
        OrderPojo order = orderApi.getByOrderId(orderId);
        if (order == null) {
            throw new ApiException("Order " + orderId + " not found");
        }

        // Only PLACED orders can be edited
        if (!"PLACED".equals(order.getStatus())) {
            throw new ApiException("Only PLACED orders can be edited. Current status: " + order.getStatus());
        }

        // Get existing order items
        List<OrderItemPojo> existingItems = orderItemApi.getByOrderId(orderId);

        // Restore inventory for all existing items
        for (OrderItemPojo item : existingItems) {
            Integer currentQty = 0;
            try {
                InventoryPojo inv = inventoryApi.getByProductId(item.getProductId());
                currentQty = inv != null && inv.getQuantity() != null ? inv.getQuantity() : 0;
            } catch (ApiException e) {
                currentQty = 0;
            }
            int restoredQty = currentQty + (item.getQuantity() != null ? item.getQuantity() : 0);
            inventoryApi.updateByProductId(item.getProductId(), restoredQty);
        }

        // Delete all existing order items
        for (OrderItemPojo item : existingItems) {
            orderItemApi.delete(item.getId());
        }

        // Calculate new totals
        int totalItems = 0;
        double totalAmount = 0.0;

        // Validate new items and reduce inventory
        List<OrderItemPojo> savedItems = new ArrayList<>();
        for (OrderItemPojo item : newOrderItems) {
            // Validate product exists
            ProductPojo product = productApi.get(item.getProductId());
            if (product == null) {
                throw new ApiException("Product with ID " + item.getProductId() + " does not exist");
            }

            // Check inventory
            InventoryPojo inventory = inventoryApi.getByProductId(item.getProductId());
            if (inventory == null || inventory.getQuantity() < item.getQuantity()) {
                throw new ApiException("Insufficient inventory for product " + product.getName() +
                        ". Available: " + (inventory != null ? inventory.getQuantity() : 0) +
                        ", Required: " + item.getQuantity());
            }

            // Reduce inventory
            int newQuantity = inventory.getQuantity() - item.getQuantity();
            inventoryApi.updateByProductId(item.getProductId(), newQuantity);

            // Set order item details
            item.setOrderId(orderId);
            item.setBarcode(product.getBarcode());
            item.setProductName(product.getName());
            item.setLineTotal(item.getQuantity() * item.getMrp());

            // Save order item
            OrderItemPojo savedItem = orderItemApi.add(item);
            savedItems.add(savedItem);

            // Update totals
            totalItems += item.getQuantity();
            totalAmount += item.getLineTotal();
        }

        // Update order totals
        OrderPojo patch = new OrderPojo();
        patch.setTotalItems(totalItems);
        patch.setTotalAmount(totalAmount);
        return orderApi.update(order.getId(), patch);
    }
}
