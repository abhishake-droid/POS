package com.increff.pos.api;

import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductApi {
    ProductPojo add(ProductPojo productPojo) throws ApiException;
    ProductPojo get(String id) throws ApiException;
    ProductPojo getByBarcode(String barcode) throws ApiException;
    Page<ProductPojo> getAll(int page, int size);
    ProductPojo update(String id, ProductPojo productPojo) throws ApiException;
    List<ProductPojo> addBulk(List<ProductPojo> productPojos) throws ApiException;
}
