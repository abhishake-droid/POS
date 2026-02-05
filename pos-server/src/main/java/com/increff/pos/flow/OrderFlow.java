package com.increff.pos.flow;

import com.increff.pos.api.*;
import com.increff.pos.db.*;
import com.increff.pos.exception.ApiException;
import com.increff.pos.helper.OrderHelper;
import com.increff.pos.model.data.InventoryCheckResult;
import com.increff.pos.model.data.OrderCreationResult;
import com.increff.pos.model.data.UnfulfillableItemData;
import com.increff.pos.util.OrderCalculator;
import com.increff.pos.util.OrderStatus;
import com.increff.pos.util.SequenceGenerator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderFlow {

    @Autowired
    private OrderApi orderApi;
    @Autowired
    private OrderItemApi orderItemApi;
    @Autowired
    private InventoryApi inventoryApi;
    @Autowired
    private ProductApi productApi;
    @Autowired
    private SequenceGenerator sequenceGenerator;

    @Transactional(rollbackFor = ApiException.class)
    public OrderCreationResult createOrder(List<OrderItemPojo> orderItems) throws ApiException {
        String orderId = generateOrderId();
        InventoryCheckResult checkResult = checkAllInventoryAvailable(orderItems);
        OrderCalculator totals = new OrderCalculator();

        String orderStatus = processOrderCreation(orderId, orderItems, checkResult, totals);
        OrderPojo savedOrder = saveNewOrder(orderId, orderStatus, totals);

        return OrderHelper.createOrderCreationResult(
                savedOrder.getOrderId(),
                checkResult.isAllAvailable(),
                checkResult.getUnfulfillableItems());
    }

    public OrderPojo getOrderWithItems(String orderId) throws ApiException {
        orderId = OrderHelper.validateOrderId(orderId);
        return orderApi.getCheckByOrderId(orderId);
    }

    public List<OrderItemPojo> getOrderItems(String orderId) {
        return orderItemApi.getByOrderId(orderId);
    }

    public List<OrderPojo> getOrderWithFilters(String orderId, String status, ZonedDateTime fromDate,
            ZonedDateTime toDate) {
        return orderApi.getWithFilters(orderId, status, fromDate, toDate);
    }

    public Page<OrderPojo> getOrderWithFilters(String orderId, String status, ZonedDateTime fromDate,
            ZonedDateTime toDate, Pageable pageable) {
        return orderApi.getWithFilters(orderId, status, fromDate, toDate, pageable);
    }

    @Transactional(rollbackFor = ApiException.class)
    public OrderPojo cancelOrder(String orderId) throws ApiException {
        orderId = OrderHelper.validateOrderId(orderId);
        OrderPojo order = validateCancellableOrder(orderId);
        restoreInventoryForCancelledOrder(order.getOrderId());
        return updateOrderStatus(order.getId(), OrderStatus.CANCELLED);
    }

    @Transactional(rollbackFor = ApiException.class)
    public OrderCreationResult retryOrder(String orderId, List<OrderItemPojo> updatedItems)
            throws ApiException {
        orderId = OrderHelper.validateOrderId(orderId);
        OrderPojo order = validateRetryableOrder(orderId);
        List<OrderItemPojo> itemsToCheck = prepareItemsForRetry(orderId, updatedItems);
        InventoryCheckResult checkResult = checkAllInventoryAvailable(itemsToCheck);

        if (checkResult.isAllAvailable()) {
            return processFulfillableRetry(order, itemsToCheck);
        } else {
            return processUnfulfillableRetry(order, updatedItems, checkResult);
        }
    }

    @Transactional(rollbackFor = ApiException.class)
    public OrderPojo updateOrder(String orderId, List<OrderItemPojo> newOrderItems) throws ApiException {
        orderId = OrderHelper.validateOrderId(orderId);
        OrderPojo order = validateUpdatableOrder(orderId);

        restoreAndClearExistingItems(orderId);

        OrderCalculator totals = new OrderCalculator();
        InventoryCheckResult checkResult = checkAllInventoryAvailable(newOrderItems);

        return processOrderUpdate(order, orderId, newOrderItems, checkResult, totals);
    }

    private String generateOrderId() {
        long orderNumber = sequenceGenerator.getNextSequence("order");
        return "ORD-" + String.format("%06d", orderNumber);
    }

    private String processOrderCreation(String orderId, List<OrderItemPojo> orderItems,
            InventoryCheckResult checkResult, OrderCalculator totals) throws ApiException {
        List<OrderItemPojo> savedItems = new ArrayList<>();
        List<String> productIds = OrderHelper.extractProductIds(orderItems);

        if (checkResult.isAllAvailable()) {
            BulkData bulkData = fetchBulkData(productIds);
            Map<String, Integer> inventoryUpdates = OrderHelper.prepareInventoryDeduct(orderItems,
                    bulkData.inventoryMap);

            processOrderItems(orderItems, orderId, bulkData.productMap, savedItems, totals);
            inventoryApi.bulkUpdateQuantities(inventoryUpdates);

            return OrderStatus.PLACED.getValue();
        } else {
            Map<String, ProductPojo> productMap = OrderHelper.fetchProductsMap(productApi, productIds);

            processOrderItems(orderItems, orderId, productMap, savedItems, totals);

            return OrderStatus.UNFULFILLABLE.getValue();
        }
    }

    private OrderPojo saveNewOrder(String orderId, String orderStatus, OrderCalculator totals) throws ApiException {
        OrderPojo order = new OrderPojo();
        order.setOrderId(orderId);
        order.setStatus(orderStatus);
        order.setTotalItems(totals.getTotalItems());
        order.setTotalAmount(totals.getTotalAmount());
        order.setOrderDate(ZonedDateTime.now());

        return orderApi.add(order);
    }

    private InventoryCheckResult checkAllInventoryAvailable(List<OrderItemPojo> orderItems)
            throws ApiException {
        InventoryCheckResult result = new InventoryCheckResult();
        List<UnfulfillableItemData> unfulfillableItems = new ArrayList<>();

        List<String> productIds = OrderHelper.extractProductIds(orderItems);
        BulkData bulkData = fetchBulkData(productIds);

        for (OrderItemPojo item : orderItems) {
            ProductPojo product = bulkData.productMap.get(item.getProductId());
            if (product == null) {
                throw new ApiException("Product with ID " + item.getProductId() + " does not exist");
            }

            InventoryPojo inventory = bulkData.inventoryMap.get(item.getProductId());
            int availableQty = (inventory != null && inventory.getQuantity() != null) ? inventory.getQuantity() : 0;

            if (availableQty < item.getQuantity()) {
                String reason = availableQty == 0 ? "OUT_OF_STOCK" : "INSUFFICIENT_QUANTITY";
                UnfulfillableItemData unfulfillable = OrderHelper.createUnfulfillableItem(
                        product.getBarcode(),
                        product.getName(),
                        item.getQuantity(),
                        availableQty,
                        reason);
                unfulfillableItems.add(unfulfillable);
            }
        }

        result.setAllAvailable(unfulfillableItems.isEmpty());
        result.setUnfulfillableItems(unfulfillableItems);
        return result;
    }

    private OrderPojo validateCancellableOrder(String orderId) throws ApiException {
        OrderPojo order = orderApi.getCheckByOrderId(orderId);

        if (OrderStatus.INVOICED.getValue().equals(order.getStatus())) {
            throw new ApiException("Order " + orderId + " is already invoiced and cannot be cancelled");
        }
        if (OrderStatus.CANCELLED.getValue().equals(order.getStatus())) {
            throw new ApiException("Order " + orderId + " is already cancelled");
        }
        return order;
    }

    private void restoreInventoryForCancelledOrder(String orderId) {
        List<OrderItemPojo> items = orderItemApi.getByOrderId(orderId);
        List<String> productIds = OrderHelper.extractProductIds(items);
        Map<String, InventoryPojo> inventoryMap = OrderHelper.fetchInventoriesMap(inventoryApi, productIds);
        Map<String, Integer> inventoryUpdates = OrderHelper.prepareInventoryRestore(items, inventoryMap);
        inventoryApi.bulkUpdateQuantities(inventoryUpdates);
    }

    private OrderPojo updateOrderStatus(String orderId, OrderStatus status) throws ApiException {
        return orderApi.update(orderId, OrderHelper.createOrderPatch(status.getValue()));
    }

    private OrderPojo validateUpdatableOrder(String orderId) throws ApiException {
        OrderPojo order = orderApi.getCheckByOrderId(orderId);
        if (!OrderStatus.PLACED.getValue().equals(order.getStatus())) {
            throw new ApiException("Only PLACED orders can be edited. Current status: " + order.getStatus());
        }
        return order;
    }

    private void restoreAndClearExistingItems(String orderId) throws ApiException {
        List<OrderItemPojo> existingItems = orderItemApi.getByOrderId(orderId);
        List<String> existingProductIds = OrderHelper.extractProductIds(existingItems);
        Map<String, InventoryPojo> inventoryMap = OrderHelper.fetchInventoriesMap(inventoryApi, existingProductIds);
        Map<String, Integer> restoreUpdates = OrderHelper.prepareInventoryRestore(existingItems, inventoryMap);

        inventoryApi.bulkUpdateQuantities(restoreUpdates);

        for (OrderItemPojo item : existingItems) {
            orderItemApi.delete(item.getId());
        }
    }

    private OrderPojo processOrderUpdate(OrderPojo order, String orderId, List<OrderItemPojo> newOrderItems,
            InventoryCheckResult checkResult, OrderCalculator totals) throws ApiException {
        List<OrderItemPojo> savedItems = new ArrayList<>();

        List<String> newProductIds = OrderHelper.extractProductIds(newOrderItems);
        if (checkResult.isAllAvailable()) {
            BulkData bulkData = fetchBulkData(newProductIds);
            Map<String, Integer> deductUpdates = OrderHelper.prepareInventoryDeduct(newOrderItems,
                    bulkData.inventoryMap);

            processOrderItems(newOrderItems, orderId, bulkData.productMap, savedItems, totals);
            inventoryApi.bulkUpdateQuantities(deductUpdates);

            return updateOrderStatus(order.getId(), OrderStatus.PLACED, totals);
        } else {
            Map<String, ProductPojo> productMap = OrderHelper.fetchProductsMap(productApi, newProductIds);

            processOrderItems(newOrderItems, orderId, productMap, savedItems, totals);

            return updateOrderStatus(order.getId(), OrderStatus.UNFULFILLABLE, totals);
        }
    }

    private OrderPojo updateOrderStatus(String orderId, OrderStatus status, OrderCalculator totals)
            throws ApiException {
        return orderApi.update(orderId, OrderHelper.createOrderPatch(
                status.getValue(),
                totals.getTotalItems(),
                totals.getTotalAmount()));
    }

    private OrderPojo validateRetryableOrder(String orderId) throws ApiException {
        OrderPojo order = orderApi.getCheckByOrderId(orderId);
        if (!OrderStatus.UNFULFILLABLE.getValue().equals(order.getStatus())) {
            throw new ApiException("Only UNFULFILLABLE orders can be retried. Current status: " + order.getStatus());
        }
        return order;
    }

    private List<OrderItemPojo> prepareItemsForRetry(String orderId, List<OrderItemPojo> updatedItems) {
        if (updatedItems != null && !updatedItems.isEmpty()) {
            orderItemApi.deleteByOrderId(orderId);
            for (OrderItemPojo item : updatedItems) {
                item.setOrderId(orderId);
            }
            return updatedItems;
        } else {
            return orderItemApi.getByOrderId(orderId);
        }
    }

    private OrderCreationResult processFulfillableRetry(OrderPojo order, List<OrderItemPojo> itemsToCheck)
            throws ApiException {
        List<String> productIds = OrderHelper.extractProductIds(itemsToCheck);
        BulkData bulkData = fetchBulkData(productIds);
        Map<String, Integer> inventoryUpdates = OrderHelper.prepareInventoryDeduct(itemsToCheck, bulkData.inventoryMap);

        OrderCalculator totals = new OrderCalculator();
        List<OrderItemPojo> itemsToSave = new ArrayList<>();

        for (OrderItemPojo item : itemsToCheck) {
            ProductPojo product = bulkData.productMap.get(item.getProductId());

            if (item.getId() == null) {
                item.setOrderId(order.getOrderId());
                item.setBarcode(product.getBarcode());
                item.setProductName(product.getName());
                item.setLineTotal(item.getQuantity() * item.getMrp());
                itemsToSave.add(item);
            }

            totals.addItem(item.getQuantity(), item.getLineTotal());
        }

        if (!itemsToSave.isEmpty()) {
            orderItemApi.addBulk(itemsToSave);
        }

        inventoryApi.bulkUpdateQuantities(inventoryUpdates);

        OrderPojo updatedOrder = orderApi.update(order.getId(), OrderHelper.createOrderPatch(
                OrderStatus.PLACED.getValue(),
                totals.getTotalItems(),
                totals.getTotalAmount()));

        return OrderHelper.createOrderCreationResult(
                updatedOrder.getOrderId(),
                true,
                new ArrayList<>());
    }

    private OrderCreationResult processUnfulfillableRetry(OrderPojo order, List<OrderItemPojo> updatedItems,
            InventoryCheckResult checkResult) throws ApiException {
        OrderCalculator totals = new OrderCalculator();

        if (updatedItems != null && !updatedItems.isEmpty()) {
            List<String> productIds = OrderHelper.extractProductIds(updatedItems);
            Map<String, ProductPojo> productMap = OrderHelper.fetchProductsMap(productApi, productIds);

            processOrderItems(updatedItems, order.getOrderId(), productMap, new ArrayList<>(), totals);

            orderApi.update(order.getId(), OrderHelper.createOrderPatch(
                    OrderStatus.UNFULFILLABLE.getValue(),
                    totals.getTotalItems(),
                    totals.getTotalAmount()));
        }

        return OrderHelper.createOrderCreationResult(
                order.getOrderId(),
                false,
                checkResult.getUnfulfillableItems());
    }

    private BulkData fetchBulkData(List<String> productIds) throws ApiException {
        List<ProductPojo> products = productApi.getByIds(productIds);
        List<InventoryPojo> inventories = inventoryApi.getByProductIds(productIds);

        Map<String, ProductPojo> productMap = products.stream()
                .collect(Collectors.toMap(ProductPojo::getId, p -> p));
        Map<String, InventoryPojo> inventoryMap = inventories.stream()
                .collect(Collectors.toMap(InventoryPojo::getProductId, i -> i));

        return new BulkData(productMap, inventoryMap);
    }

    private void processOrderItems(
            List<OrderItemPojo> items,
            String orderId,
            Map<String, ProductPojo> productMap,
            List<OrderItemPojo> savedItems,
            OrderCalculator totals) throws ApiException {

        for (OrderItemPojo item : items) {
            ProductPojo product = productMap.get(item.getProductId());

            item.setOrderId(orderId);
            item.setBarcode(product.getBarcode());
            item.setProductName(product.getName());
            item.setLineTotal(item.getQuantity() * item.getMrp());

            OrderItemPojo savedItem = orderItemApi.add(item);
            savedItems.add(savedItem);

            totals.addItem(item.getQuantity(), item.getLineTotal());
        }
    }

    private static class BulkData {
        final Map<String, ProductPojo> productMap;
        final Map<String, InventoryPojo> inventoryMap;

        BulkData(Map<String, ProductPojo> productMap, Map<String, InventoryPojo> inventoryMap) {
            this.productMap = productMap;
            this.inventoryMap = inventoryMap;
        }
    }
}
