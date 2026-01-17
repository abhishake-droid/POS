package com.increff.pos.helper;

import com.increff.pos.db.InventoryPojo;
import com.increff.pos.model.data.InventoryData;
import com.increff.pos.model.form.InventoryForm;

/**
 * Helper class for Inventory entity conversions
 */
public class InventoryHelper {

    public static InventoryPojo convertToEntity(InventoryForm form) {
        InventoryPojo pojo = new InventoryPojo();
        pojo.setProductId(form.getProductId() != null ? form.getProductId().trim() : null);
        pojo.setQuantity(form.getQuantity());
        return pojo;
    }

    public static InventoryData convertToDto(InventoryPojo pojo, String barcode) {
        InventoryData data = new InventoryData();
        data.setId(pojo.getId());
        data.setProductId(pojo.getProductId());
        data.setBarcode(barcode);
        data.setQuantity(pojo.getQuantity());
        return data;
    }

    public static InventoryData convertToDto(InventoryPojo pojo) {
        InventoryData data = new InventoryData();
        data.setId(pojo.getId());
        data.setProductId(pojo.getProductId());
        data.setQuantity(pojo.getQuantity());
        return data;
    }
}
