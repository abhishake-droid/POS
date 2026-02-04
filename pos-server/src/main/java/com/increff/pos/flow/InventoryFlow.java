package com.increff.pos.flow;

import com.increff.pos.api.InventoryApi;
import com.increff.pos.api.ProductApi;
import com.increff.pos.db.InventoryPojo;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class InventoryFlow {

    @Autowired
    private InventoryApi inventoryApi;
    @Autowired
    private ProductApi productApi;

    @Transactional(readOnly = true)
    public InventoryPojo getCheck(String id) throws ApiException {
        return inventoryApi.getCheck(id);
    }

    @Transactional(rollbackFor = ApiException.class)
    public InventoryPojo updateInventory(String productId, Integer quantity) throws ApiException {
        InventoryPojo inventory = inventoryApi.getCheckByProductId(productId);
        inventory.setQuantity(quantity);
        return inventoryApi.update(inventory.getId(), inventory);
    }

    @Transactional(readOnly = true)
    public ProductPojo getProductById(String productId) throws ApiException {
        return productApi.getCheck(productId);
    }

    @Transactional(rollbackFor = ApiException.class)
    public List<InventoryPojo> updateBulk(List<InventoryPojo> pojos) throws ApiException {
        return inventoryApi.updateBulk(pojos);
    }
}
