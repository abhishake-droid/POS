package com.increff.pos.api;

import com.increff.pos.db.InventoryPojo;
import com.increff.pos.exception.ApiException;

import java.util.List;

public interface InventoryApi {
    InventoryPojo add(InventoryPojo inventoryPojo) throws ApiException;

    List<InventoryPojo> addBulk(List<InventoryPojo> inventories) throws ApiException;

    InventoryPojo getCheck(String id) throws ApiException;

    InventoryPojo getCheckByProductId(String productId) throws ApiException;

    InventoryPojo getByProductId(String productId);

    InventoryPojo update(String id, InventoryPojo inventoryPojo) throws ApiException;

    InventoryPojo updateByProductId(String productId, Integer quantity) throws ApiException;

    List<InventoryPojo> updateBulk(List<InventoryPojo> inventoryPojos) throws ApiException;

    List<InventoryPojo> getByProductIds(List<String> productIds);

    void bulkUpdateQuantities(java.util.Map<String, Integer> productIdToQuantity);
}
