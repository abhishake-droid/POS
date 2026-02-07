package com.increff.pos.api;

import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.form.PageForm;
import org.springframework.data.domain.Page;
import java.util.List;
import java.util.Map;

public interface ProductApi {
    ProductPojo add(ProductPojo productPojo) throws ApiException;

    ProductPojo getCheck(String id) throws ApiException;

    ProductPojo getCheckByBarcode(String barcode) throws ApiException;

    Page<ProductPojo> getAll(PageForm form);

    ProductPojo update(String id, ProductPojo productPojo) throws ApiException;

    List<ProductPojo> addBulk(List<ProductPojo> productPojos) throws ApiException;

    List<ProductPojo> getByIds(List<String> ids);

    List<String> getExistingBarcodes(List<String> barcodes);

    Map<String, ProductPojo> getByBarcodes(List<String> barcodes) throws ApiException;
}
