package com.increff.pos.helper;

import com.increff.pos.db.ProductPojo;
import com.increff.pos.model.data.ProductData;
import com.increff.pos.model.form.ProductForm;

import java.util.List;
import java.util.stream.Collectors;

public class ProductHelper {

    public static ProductPojo convertToEntity(ProductForm form) {
        ProductPojo pojo = new ProductPojo();
        pojo.setBarcode(form.getBarcode() != null ? form.getBarcode().trim().toLowerCase() : null);
        pojo.setClientId(form.getClientId() != null ? form.getClientId().trim() : null);
        pojo.setName(form.getName() != null ? form.getName().trim().toLowerCase() : null);
        pojo.setMrp(form.getMrp());
        pojo.setImageUrl(form.getImageUrl() != null ? form.getImageUrl().trim() : null);
        return pojo;
    }

    public static ProductData convertToDto(ProductPojo pojo, String clientName, Integer quantity) {
        ProductData data = new ProductData();
        data.setId(pojo.getId());
        data.setBarcode(pojo.getBarcode());
        data.setClientId(pojo.getClientId());
        data.setClientName(clientName);
        data.setName(pojo.getName());
        data.setMrp(pojo.getMrp());
        data.setImageUrl(pojo.getImageUrl());
        data.setQuantity(quantity);
        return data;
    }

    public static ProductData convertToDto(ProductPojo pojo) {
        ProductData data = new ProductData();
        data.setId(pojo.getId());
        data.setBarcode(pojo.getBarcode());
        data.setClientId(pojo.getClientId());
        data.setName(pojo.getName());
        data.setMrp(pojo.getMrp());
        data.setImageUrl(pojo.getImageUrl());
        return data;
    }

    public static List<ProductData> convertToDataList(List<ProductPojo> pojoList) {
        return pojoList.stream().map(ProductHelper::convertToDto).collect(Collectors.toList());
    }
}
