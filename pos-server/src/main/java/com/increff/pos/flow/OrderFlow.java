package com.increff.pos.flow;

import com.increff.pos.api.*;
import com.increff.pos.db.*;
import com.increff.pos.exception.ApiException;
import com.increff.pos.util.SequenceGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

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
    public com.increff.pos.model.data.OrderCreationResult createOrder(List<OrderItemPojo> orderItems)
            throws ApiException {

        long orderNumber = sequenceGenerator.getNextSequence("order");
        String orderId = "ORD-" + String.format("%06d", orderNumber);

        com.increff.pos.model.data.InventoryCheckResult checkResult = checkAllInventoryAvailable(orderItems);

        int totalItems = 0;
        double totalAmount = 0.0;

        String orderStatus;
        List<OrderItemPojo> savedItems = new ArrayList<>();

        if (checkResult.isAllAvailable()) {
            orderStatus = "PLACED";

            for (OrderItemPojo item : orderItems) {
                ProductPojo product = productApi.getCheck(item.getProductId());
                InventoryPojo inventory = inventoryApi.getCheckByProductId(item.getProductId());

                int newQuantity = inventory.getQuantity() - item.getQuantity();
                inventoryApi.updateByProductId(item.getProductId(), newQuantity);

                item.setOrderId(orderId);
                item.setBarcode(product.getBarcode());
                item.setProductName(product.getName());
                item.setLineTotal(item.getQuantity() * item.getMrp());

                OrderItemPojo savedItem = orderItemApi.add(item);
                savedItems.add(savedItem);

                totalItems += item.getQuantity();
                totalAmount += item.getLineTotal();
            }
        } else {
            orderStatus = "UNFULFILLABLE";

            for (OrderItemPojo item : orderItems) {
                ProductPojo product = productApi.getCheck(item.getProductId());

                item.setOrderId(orderId);
                item.setBarcode(product.getBarcode());
                item.setProductName(product.getName());
                item.setLineTotal(item.getQuantity() * item.getMrp());

                OrderItemPojo savedItem = orderItemApi.add(item);
                savedItems.add(savedItem);

                totalItems += item.getQuantity();
                totalAmount += item.getLineTotal();
            }
        }

        OrderPojo order = new OrderPojo();
        order.setOrderId(orderId);
        order.setStatus(orderStatus);
        order.setTotalItems(totalItems);
        order.setTotalAmount(totalAmount);
        order.setOrderDate(ZonedDateTime.now());

        OrderPojo savedOrder = orderApi.add(order);

        com.increff.pos.model.data.OrderCreationResult result = new com.increff.pos.model.data.OrderCreationResult();
        result.setOrderId(savedOrder.getOrderId());
        result.setFulfillable(checkResult.isAllAvailable());
        result.setUnfulfillableItems(checkResult.getUnfulfillableItems());

        return result;
    }

    private com.increff.pos.model.data.InventoryCheckResult checkAllInventoryAvailable(List<OrderItemPojo> orderItems)
            throws ApiException {
        com.increff.pos.model.data.InventoryCheckResult result = new com.increff.pos.model.data.InventoryCheckResult();
        List<com.increff.pos.model.data.UnfulfillableItemData> unfulfillableItems = new ArrayList<>();

        for (OrderItemPojo item : orderItems) {
            ProductPojo product = productApi.getCheck(item.getProductId());
            InventoryPojo inventory = inventoryApi.getByProductId(item.getProductId());
            int availableQty = (inventory != null && inventory.getQuantity() != null) ? inventory.getQuantity() : 0;

            if (availableQty < item.getQuantity()) {
                com.increff.pos.model.data.UnfulfillableItemData unfulfillable = new com.increff.pos.model.data.UnfulfillableItemData();
                unfulfillable.setBarcode(product.getBarcode());
                unfulfillable.setProductName(product.getName());
                unfulfillable.setRequestedQuantity(item.getQuantity());
                unfulfillable.setAvailableQuantity(availableQty);
                unfulfillable.setReason(availableQty == 0 ? "OUT_OF_STOCK" : "INSUFFICIENT_QUANTITY");
                unfulfillableItems.add(unfulfillable);
            }
        }

        result.setAllAvailable(unfulfillableItems.isEmpty());
        result.setUnfulfillableItems(unfulfillableItems);
        return result;
    }

    public OrderPojo getOrderWithItems(String orderId) throws ApiException {
        return orderApi.getCheckByOrderId(orderId);
    }

    public List<OrderItemPojo> getOrderItems(String orderId) {
        return orderItemApi.getByOrderId(orderId);
    }

    public List<OrderPojo> getOrderWithFilters(String orderId, String status, ZonedDateTime fromDate,
            ZonedDateTime toDate) {
        return orderApi.getWithFilters(orderId, status, fromDate, toDate);
    }

    @Transactional(rollbackFor = ApiException.class)
    public OrderPojo cancelOrder(String orderId) throws ApiException {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new ApiException("Order ID cannot be empty");
        }
        orderId = orderId.trim();

        OrderPojo order = orderApi.getCheckByOrderId(orderId);

        if ("INVOICED".equals(order.getStatus())) {
            throw new ApiException("Order " + orderId + " is already invoiced and cannot be cancelled");
        }
        if ("CANCELLED".equals(order.getStatus())) {
            return order;
        }

        List<OrderItemPojo> items = orderItemApi.getByOrderId(orderId);
        for (OrderItemPojo item : items) {
            Integer currentQty = 0;
            try {
                InventoryPojo inv = inventoryApi.getCheckByProductId(item.getProductId());
                currentQty = inv != null && inv.getQuantity() != null ? inv.getQuantity() : 0;
            } catch (ApiException e) {
                currentQty = 0;
            }
            int newQty = currentQty + (item.getQuantity() != null ? item.getQuantity() : 0);
            inventoryApi.updateByProductId(item.getProductId(), newQty);
        }

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

        OrderPojo order = orderApi.getCheckByOrderId(orderId);
        if (order == null) {
            throw new ApiException("Order " + orderId + " not found");
        }

        if (!"PLACED".equals(order.getStatus())) {
            throw new ApiException("Only PLACED orders can be edited. Current status: " + order.getStatus());
        }

        List<OrderItemPojo> existingItems = orderItemApi.getByOrderId(orderId);

        for (OrderItemPojo item : existingItems) {
            Integer currentQty = 0;
            try {
                InventoryPojo inv = inventoryApi.getCheckByProductId(item.getProductId());
                currentQty = inv != null && inv.getQuantity() != null ? inv.getQuantity() : 0;
            } catch (ApiException e) {
                currentQty = 0;
            }
            int restoredQty = currentQty + (item.getQuantity() != null ? item.getQuantity() : 0);
            inventoryApi.updateByProductId(item.getProductId(), restoredQty);
        }

        for (OrderItemPojo item : existingItems) {
            orderItemApi.delete(item.getId());
        }

        int totalItems = 0;
        double totalAmount = 0.0;

        List<OrderItemPojo> savedItems = new ArrayList<>();
        for (OrderItemPojo item : newOrderItems) {
            ProductPojo product = productApi.getCheck(item.getProductId());

            InventoryPojo inventory = inventoryApi.getCheckByProductId(item.getProductId());
            if (inventory == null || inventory.getQuantity() < item.getQuantity()) {
                throw new ApiException("Insufficient inventory for product " + product.getName() +
                        ". Available: " + (inventory != null ? inventory.getQuantity() : 0) +
                        ", Required: " + item.getQuantity());
            }

            int newQuantity = inventory.getQuantity() - item.getQuantity();
            inventoryApi.updateByProductId(item.getProductId(), newQuantity);

            item.setOrderId(orderId);
            item.setBarcode(product.getBarcode());
            item.setProductName(product.getName());
            item.setLineTotal(item.getQuantity() * item.getMrp());

            OrderItemPojo savedItem = orderItemApi.add(item);
            savedItems.add(savedItem);

            totalItems += item.getQuantity();
            totalAmount += item.getLineTotal();
        }

        OrderPojo patch = new OrderPojo();
        patch.setTotalItems(totalItems);
        patch.setTotalAmount(totalAmount);
        return orderApi.update(order.getId(), patch);
    }

    @Transactional(rollbackFor = ApiException.class)
    public com.increff.pos.model.data.OrderCreationResult retryOrder(String orderId, List<OrderItemPojo> updatedItems)
            throws ApiException {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new ApiException("Order ID cannot be empty");
        }
        orderId = orderId.trim();

        OrderPojo order = orderApi.getCheckByOrderId(orderId);
        if (order == null) {
            throw new ApiException("Order " + orderId + " not found");
        }

        if (!"UNFULFILLABLE".equals(order.getStatus())) {
            throw new ApiException("Only UNFULFILLABLE orders can be retried. Current status: " + order.getStatus());
        }

        List<OrderItemPojo> itemsToCheck;
        if (updatedItems != null && !updatedItems.isEmpty()) {
            List<OrderItemPojo> existingItems = orderItemApi.getByOrderId(orderId);
            for (OrderItemPojo existingItem : existingItems) {
                orderItemApi.delete(existingItem.getId());
            }

            for (OrderItemPojo item : updatedItems) {
                item.setOrderId(orderId);
            }
            itemsToCheck = updatedItems;
        } else {
            itemsToCheck = orderItemApi.getByOrderId(orderId);
        }

        com.increff.pos.model.data.InventoryCheckResult checkResult = checkAllInventoryAvailable(itemsToCheck);

        int totalItems = 0;
        double totalAmount = 0.0;

        if (checkResult.isAllAvailable()) {
            List<OrderItemPojo> savedItems = new ArrayList<>();

            for (OrderItemPojo item : itemsToCheck) {
                ProductPojo product = productApi.getCheck(item.getProductId());
                InventoryPojo inventory = inventoryApi.getCheckByProductId(item.getProductId());

                int newQuantity = inventory.getQuantity() - item.getQuantity();
                inventoryApi.updateByProductId(item.getProductId(), newQuantity);

                if (item.getId() == null) {
                    item.setOrderId(orderId);
                    item.setBarcode(product.getBarcode());
                    item.setProductName(product.getName());
                    item.setLineTotal(item.getQuantity() * item.getMrp());
                    OrderItemPojo savedItem = orderItemApi.add(item);
                    savedItems.add(savedItem);
                }

                totalItems += item.getQuantity();
                totalAmount += item.getLineTotal();
            }

            OrderPojo patch = new OrderPojo();
            patch.setStatus("PLACED");
            patch.setTotalItems(totalItems);
            patch.setTotalAmount(totalAmount);
            OrderPojo updatedOrder = orderApi.update(order.getId(), patch);

            com.increff.pos.model.data.OrderCreationResult result = new com.increff.pos.model.data.OrderCreationResult();
            result.setOrderId(updatedOrder.getOrderId());
            result.setFulfillable(true);
            result.setUnfulfillableItems(new ArrayList<>());
            return result;

        } else {
            if (updatedItems != null && !updatedItems.isEmpty()) {
                for (OrderItemPojo item : updatedItems) {
                    ProductPojo product = productApi.getCheck(item.getProductId());
                    item.setBarcode(product.getBarcode());
                    item.setProductName(product.getName());
                    item.setLineTotal(item.getQuantity() * item.getMrp());
                    orderItemApi.add(item);

                    totalItems += item.getQuantity();
                    totalAmount += item.getLineTotal();
                }

                OrderPojo patch = new OrderPojo();
                patch.setTotalItems(totalItems);
                patch.setTotalAmount(totalAmount);
                orderApi.update(order.getId(), patch);
            }

            com.increff.pos.model.data.OrderCreationResult result = new com.increff.pos.model.data.OrderCreationResult();
            result.setOrderId(order.getOrderId());
            result.setFulfillable(false);
            result.setUnfulfillableItems(checkResult.getUnfulfillableItems());
            return result;
        }
    }
}
