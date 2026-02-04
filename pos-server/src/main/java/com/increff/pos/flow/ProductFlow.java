package com.increff.pos.flow;

import com.increff.pos.api.ClientApi;
import com.increff.pos.api.InventoryApi;
import com.increff.pos.api.ProductApi;
import com.increff.pos.db.ClientPojo;
import com.increff.pos.db.InventoryPojo;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.form.PageForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import com.increff.pos.helper.ProductHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductFlow {

    @Autowired
    private ProductApi productApi;
    @Autowired
    private InventoryApi inventoryApi;
    @Autowired
    private ClientApi clientApi;

    @Transactional(rollbackFor = ApiException.class)
    public ProductPojo create(ProductPojo productPojo) throws ApiException {
        ProductPojo saved = productApi.add(productPojo);
        InventoryPojo inventory = ProductHelper.createInitialInventory(saved.getId());
        inventoryApi.add(inventory);
        return saved;
    }

    @Transactional(rollbackFor = ApiException.class)
    public List<ProductPojo> addBulk(List<ProductPojo> productPojos) throws ApiException {
        List<ProductPojo> saved = productApi.addBulk(productPojos);

        List<InventoryPojo> inventories = saved.stream()
                .map(pojo -> com.increff.pos.helper.ProductHelper.createInitialInventory(pojo.getId()))
                .collect(Collectors.toList());

        inventoryApi.addBulk(inventories);

        return saved;
    }

    @Transactional(readOnly = true)
    public ProductPojo getById(String id) throws ApiException {
        return productApi.getCheck(id);
    }

    @Transactional(readOnly = true)
    public ProductPojo getByBarcode(String barcode) throws ApiException {
        return productApi.getCheckByBarcode(barcode);
    }

    @Transactional(readOnly = true)
    public Page<ProductPojo> getAll(PageForm form) throws ApiException {
        return productApi.getAll(form);
    }

    @Transactional(rollbackFor = ApiException.class)
    public ProductPojo update(String id, ProductPojo updatePojo) throws ApiException {
        return productApi.update(id, updatePojo);
    }

    @Transactional(readOnly = true)
    public InventoryPojo getInventoryByProductId(String productId) throws ApiException {
        return inventoryApi.getCheckByProductId(productId);
    }

    @Transactional(readOnly = true)
    public ClientPojo getClientById(String clientId) throws ApiException {
        return clientApi.getCheckByClientId(clientId);
    }

    @Transactional(readOnly = true)
    public List<String> getExistingBarcodes(List<String> barcodes) {
        return productApi.getExistingBarcodes(barcodes);
    }
}
