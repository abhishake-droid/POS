package com.increff.pos.flow;

import com.increff.pos.api.InventoryApi;
import com.increff.pos.api.ProductApi;
import com.increff.pos.db.InventoryPojo;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.helper.InventoryHelper;
import com.increff.pos.model.data.InventoryData;
import org.springframework.stereotype.Service;

@Service
public class InventoryFlow {

    private final InventoryApi inventoryApi;
    private final ProductApi productApi;

    public InventoryFlow(InventoryApi inventoryApi, ProductApi productApi) {
        this.inventoryApi = inventoryApi;
        this.productApi = productApi;
    }

    public InventoryData updateInventory(String productId, Integer quantity) throws ApiException {
        InventoryPojo updated = inventoryApi.updateByProductId(productId, quantity);
        ProductPojo product = productApi.get(productId);
        return InventoryHelper.convertToDto(updated, product.getBarcode());
    }
}

