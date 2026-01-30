package com.increff.pos.flow;

import com.increff.pos.api.InventoryApi;
import com.increff.pos.api.ProductApi;
import com.increff.pos.db.InventoryPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.helper.InventoryHelper;
import com.increff.pos.model.data.InventoryData;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InventoryFlow {

    private final InventoryApi inventoryApi;
    private final ProductApi productApi;

    public InventoryFlow(InventoryApi inventoryApi, ProductApi productApi) {
        this.inventoryApi = inventoryApi;
        this.productApi = productApi;
    }

    @Transactional(readOnly = true)
    public InventoryData getCheck(String id) throws ApiException {
        InventoryPojo pojo = inventoryApi.getCheck(id);
        String barcode = productApi.getCheck(pojo.getProductId()).getBarcode();
        return InventoryHelper.convertToDto(pojo, barcode);
    }

    @Transactional(rollbackFor = ApiException.class)
    public InventoryData updateInventory(String productId, Integer quantity) throws ApiException {
        InventoryPojo inventory = inventoryApi.getCheckByProductId(productId);
        inventory.setQuantity(quantity);
        InventoryPojo pojo = inventoryApi.update(inventory.getId(), inventory);
        String barcode = productApi.getCheck(pojo.getProductId()).getBarcode();
        return InventoryHelper.convertToDto(pojo, barcode);
    }

    @Transactional(rollbackFor = ApiException.class)
    public InventoryPojo updateByProductId(String productId, Integer quantity) throws ApiException {
        return inventoryApi.updateByProductId(productId, quantity);
    }

    @Transactional(rollbackFor = ApiException.class)
    public List<InventoryPojo> updateBulk(List<InventoryPojo> pojos) throws ApiException {
        return inventoryApi.updateBulk(pojos);
    }
}
