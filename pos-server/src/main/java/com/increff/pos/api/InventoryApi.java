package com.increff.pos.api;

import com.increff.pos.db.InventoryPojo;
import com.increff.pos.exception.ApiException;

import java.util.List;

public interface InventoryApi {
    InventoryPojo add(InventoryPojo inventoryPojo) throws ApiException;
    InventoryPojo get(String id) throws ApiException;
    InventoryPojo getByProductId(String productId) throws ApiException;
    InventoryPojo update(String id, InventoryPojo inventoryPojo) throws ApiException;
    InventoryPojo updateByProductId(String productId, Integer quantity) throws ApiException;
    List<InventoryPojo> updateBulk(List<InventoryPojo> inventoryPojos) throws ApiException;
}
