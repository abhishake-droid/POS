package com.increff.pos.helper;

import com.increff.pos.api.InventoryApi;
import com.increff.pos.api.ProductApi;
import com.increff.pos.db.InventoryPojo;
import com.increff.pos.db.OrderItemPojo;
import com.increff.pos.db.OrderPojo;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.OrderData;
import com.increff.pos.model.data.OrderItemData;
import com.increff.pos.model.data.OrderCreationResult;
import com.increff.pos.model.data.UnfulfillableItemData;
import org.springframework.util.StringUtils;
import com.increff.pos.util.NormalizeUtil;

import java.time.LocalDate;
import java.time.ZonedDateTime;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OrderHelper {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    public static OrderData convertToData(OrderPojo pojo, boolean hasInvoice) {
        OrderData data = new OrderData();
        data.setId(pojo.getId());
        data.setOrderId(pojo.getOrderId());
        data.setStatus(pojo.getStatus());
        data.setTotalItems(pojo.getTotalItems());
        data.setTotalAmount(pojo.getTotalAmount());
        if (pojo.getOrderDate() != null) {
            data.setCreatedAt(pojo.getOrderDate().format(DATE_FORMATTER));
        }
        data.setHasInvoice(hasInvoice);
        return data;
    }

    public static OrderItemData convertItemToDto(OrderItemPojo pojo) {
        OrderItemData data = new OrderItemData();
        data.setId(pojo.getId());
        data.setOrderId(pojo.getOrderId());
        data.setProductId(pojo.getProductId());
        data.setBarcode(pojo.getBarcode());
        data.setProductName(pojo.getProductName());
        data.setQuantity(pojo.getQuantity());
        data.setMrp(pojo.getMrp());
        data.setLineTotal(pojo.getLineTotal());
        return data;
    }

    public static List<OrderItemData> convertItemsToDtoList(List<OrderItemPojo> pojoList) {
        return pojoList.stream().map(OrderHelper::convertItemToDto).collect(Collectors.toList());
    }

    public static String validateOrderId(String orderId) throws ApiException {
        orderId = NormalizeUtil.normalizeOrderId(orderId);

        if (!StringUtils.hasText(orderId)) {
            throw new ApiException("Order ID cannot be empty");
        }
        return orderId;
    }

    public static List<String> extractProductIds(List<OrderItemPojo> items) {
        return items.stream()
                .map(OrderItemPojo::getProductId)
                .distinct()
                .collect(Collectors.toList());
    }

    public static List<String> extractBarcodes(List<OrderItemPojo> items) {
        if (items == null) {
            return Collections.emptyList();
        }
        return items.stream()
                .map(OrderItemPojo::getBarcode)
                .distinct()
                .collect(Collectors.toList());
    }

    public static Map<String, ProductPojo> fetchProductsMap(ProductApi productApi, List<String> productIds) {
        List<ProductPojo> products = productApi.getByIds(productIds);
        return products.stream()
                .collect(Collectors.toMap(ProductPojo::getId, p -> p));
    }

    public static Map<String, InventoryPojo> fetchInventoriesMap(InventoryApi inventoryApi, List<String> productIds) {
        List<InventoryPojo> inventories = inventoryApi.getByProductIds(productIds);
        return inventories.stream()
                .collect(Collectors.toMap(InventoryPojo::getProductId, i -> i));
    }

    public static Map<String, Integer> prepareInventoryRestore(
            List<OrderItemPojo> items,
            Map<String, InventoryPojo> inventoryMap) {

        Map<String, Integer> updates = new HashMap<>();
        for (OrderItemPojo item : items) {
            InventoryPojo inv = inventoryMap.get(item.getProductId());
            Integer currentQty = (inv != null && inv.getQuantity() != null) ? inv.getQuantity() : 0;
            int newQty = currentQty + (item.getQuantity() != null ? item.getQuantity() : 0);
            updates.put(item.getProductId(), newQty);
        }
        return updates;
    }

    public static Map<String, Integer> prepareInventoryDeduct(
            List<OrderItemPojo> items,
            Map<String, InventoryPojo> inventoryMap) {

        Map<String, Integer> updates = new HashMap<>();
        for (OrderItemPojo item : items) {
            InventoryPojo inv = inventoryMap.get(item.getProductId());
            int newQty = inv.getQuantity() - item.getQuantity();
            updates.put(item.getProductId(), newQty);
        }
        return updates;
    }

    public static OrderPojo createOrderPatch(String status) {
        OrderPojo patch = new OrderPojo();
        patch.setStatus(status);
        return patch;
    }

    public static OrderPojo createOrderPatch(String status, Integer totalItems, Double totalAmount) {
        OrderPojo patch = new OrderPojo();
        patch.setStatus(status);
        patch.setTotalItems(totalItems);
        patch.setTotalAmount(totalAmount);
        return patch;
    }

    public static OrderCreationResult createOrderCreationResult(
            String orderId, boolean isFulfillable,
            List<UnfulfillableItemData> unfulfillableItems) {
        OrderCreationResult result = new OrderCreationResult();
        result.setOrderId(orderId);
        result.setFulfillable(isFulfillable);
        result.setUnfulfillableItems(unfulfillableItems);
        return result;
    }

    public static UnfulfillableItemData createUnfulfillableItem(String barcode, String productName,
            Integer requestedQuantity, Integer availableQuantity, String reason) {
        UnfulfillableItemData item = new UnfulfillableItemData();
        item.setBarcode(barcode);
        item.setProductName(productName);
        item.setRequestedQuantity(requestedQuantity);
        item.setAvailableQuantity(availableQuantity);
        item.setReason(reason);
        return item;
    }

    public static ZonedDateTime parseStartDate(String dateStr) throws ApiException {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            return ZonedDateTime.parse(dateStr);
        } catch (java.time.format.DateTimeParseException e) {
            try {
                return LocalDate.parse(dateStr)
                        .atStartOfDay(java.time.ZoneOffset.UTC);
            } catch (DateTimeParseException ex) {
                throw new ApiException("Invalid date format. Use yyyy-MM-dd format (e.g., 2024-01-01)");
            }
        }
    }

    public static ZonedDateTime parseEndDate(String dateStr) throws ApiException {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            return ZonedDateTime.parse(dateStr);
        } catch (DateTimeParseException e) {
            try {
                return java.time.LocalDate.parse(dateStr)
                        .atTime(23, 59, 59)
                        .atZone(java.time.ZoneOffset.UTC);
            } catch (DateTimeParseException ex) {
                throw new ApiException("Invalid date format. Use yyyy-MM-dd format (e.g., 2024-01-01)");
            }
        }
    }

    public static OrderItemPojo createOrderItem(String barcode, Integer quantity, Double mrp) {
        OrderItemPojo item = new OrderItemPojo();
        item.setBarcode(barcode);
        item.setQuantity(quantity);
        item.setMrp(mrp);
        return item;
    }
}
