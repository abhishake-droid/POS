package com.increff.pos.flow;

import com.increff.pos.api.ClientApi;
import com.increff.pos.api.InventoryApi;
import com.increff.pos.api.ProductApi;
import com.increff.pos.db.ClientPojo;
import com.increff.pos.db.InventoryPojo;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.helper.ProductHelper;
import com.increff.pos.model.data.ProductData;
import com.increff.pos.model.form.PageForm;
import com.increff.pos.util.ValidationUtil;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductFlow {

    private final ProductApi productApi;
    private final InventoryApi inventoryApi;
    private final ClientApi clientApi;

    public ProductFlow(ProductApi productApi, InventoryApi inventoryApi, ClientApi clientApi) {
        this.productApi = productApi;
        this.inventoryApi = inventoryApi;
        this.clientApi = clientApi;
    }

    @Transactional(rollbackFor = ApiException.class)
    public ProductData create(ProductPojo productPojo) throws ApiException {
        ProductPojo saved = add(productPojo);
        return getById(saved.getId());
    }

    @Transactional(rollbackFor = ApiException.class)
    public ProductPojo add(ProductPojo productPojo) throws ApiException {
        ProductPojo saved = productApi.add(productPojo);
        InventoryPojo inventory = new InventoryPojo();
        inventory.setProductId(saved.getId());
        inventory.setQuantity(0);
        inventoryApi.add(inventory);
        return saved;
    }

    @Transactional(rollbackFor = ApiException.class)
    public List<ProductData> addBulk(List<ProductPojo> productPojos) throws ApiException {
        List<ProductPojo> saved = productApi.addBulk(productPojos);
        for (ProductPojo pojo : saved) {
            InventoryPojo inventory = new InventoryPojo();
            inventory.setProductId(pojo.getId());
            inventory.setQuantity(0);
            inventoryApi.add(inventory);
        }
        return saved.stream().map(this::convertToDataSafe).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductData getById(String id) throws ApiException {
        ProductPojo pojo = productApi.getCheck(id);
        InventoryPojo inventory = inventoryApi.getCheckByProductId(id);
        return convertToData(pojo, inventory.getQuantity());
    }

    @Transactional(readOnly = true)
    public ProductData getByBarcode(String barcode) throws ApiException {
        ProductPojo pojo = productApi.getCheckByBarcode(barcode);
        InventoryPojo inventory = inventoryApi.getCheckByProductId(pojo.getId());
        return convertToData(pojo, inventory.getQuantity());
    }

    @Transactional(readOnly = true)
    public ProductPojo get(String id) throws ApiException {
        return productApi.getCheck(id);
    }

    @Transactional(readOnly = true)
    public ProductPojo getByBarcodeAsPojo(String barcode) throws ApiException {
        return productApi.getCheckByBarcode(barcode);
    }

    @Transactional(readOnly = true)
    public Page<ProductData> getAll(PageForm form) throws ApiException {
        ValidationUtil.validatePageForm(form);
        Page<ProductPojo> pojoPage = productApi.getAll(form);
        return pojoPage.map(this::convertToDataSafe);
    }

    @Transactional(rollbackFor = ApiException.class)
    public ProductData update(String id, ProductPojo updatePojo) throws ApiException {
        ProductPojo updated = productApi.update(id, updatePojo);
        InventoryPojo inventory = inventoryApi.getCheckByProductId(id);
        return convertToData(updated, inventory.getQuantity());
    }

    private ProductData convertToData(ProductPojo pojo, Integer quantity) {
        ClientPojo client = getClientSafe(pojo.getClientId());
        return ProductHelper.convertToDto(pojo, client != null ? client.getName() : null, quantity);
    }

    private ProductData convertToDataSafe(ProductPojo pojo) {
        try {
            InventoryPojo inventory = inventoryApi.getCheckByProductId(pojo.getId());
            return convertToData(pojo, inventory.getQuantity());
        } catch (ApiException e) {
            return convertToData(pojo, 0);
        }
    }

    private ClientPojo getClientSafe(String clientId) {
        try {
            return clientApi.getCheckByClientId(clientId);
        } catch (ApiException e) {
            return null;
        }
    }
}
