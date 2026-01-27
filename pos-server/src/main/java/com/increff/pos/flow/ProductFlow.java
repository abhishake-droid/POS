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

    public ProductData getById(String id) throws ApiException {
        ProductPojo pojo = productApi.get(id);
        InventoryPojo inventory = inventoryApi.getByProductId(id);
        ClientPojo client = getClientOrNull(pojo.getClientId());
        return ProductHelper.convertToDto(
                pojo,
                client != null ? client.getName() : null,
                inventory != null ? inventory.getQuantity() : 0
        );
    }

    public ProductData getByBarcode(String barcode) throws ApiException {
        ProductPojo pojo = productApi.getByBarcode(barcode);
        InventoryPojo inventory = inventoryApi.getByProductId(pojo.getId());
        ClientPojo client = getClientOrNull(pojo.getClientId());
        return ProductHelper.convertToDto(
                pojo,
                client != null ? client.getName() : null,
                inventory != null ? inventory.getQuantity() : 0
        );
    }

    public Page<ProductData> getAll(PageForm form) throws ApiException {
        ValidationUtil.validatePageForm(form);
        Page<ProductPojo> pojoPage = productApi.getAll(form.getPage(), form.getSize());

        return pojoPage.map(pojo -> {
            try {
                InventoryPojo inventory = inventoryApi.getByProductId(pojo.getId());
                ClientPojo client = getClientOrNull(pojo.getClientId());
                return ProductHelper.convertToDto(
                        pojo,
                        client != null ? client.getName() : null,
                        inventory != null ? inventory.getQuantity() : 0
                );
            } catch (ApiException e) {
                ClientPojo client = getClientOrNull(pojo.getClientId());
                return ProductHelper.convertToDto(
                        pojo,
                        client != null ? client.getName() : null,
                        0
                );
            }
        });
    }

    public ProductData update(String id, ProductPojo updatePojo) throws ApiException {
        ProductPojo updated = productApi.update(id, updatePojo);

        InventoryPojo inventory = null;
        try {
            inventory = inventoryApi.getByProductId(id);
        } catch (ApiException e) {
            // inventory doesn't exist, use 0
        }

        ClientPojo client = getClientOrNull(updated.getClientId());
        return ProductHelper.convertToDto(
                updated,
                client != null ? client.getName() : null,
                inventory != null ? inventory.getQuantity() : 0
        );
    }

    private ClientPojo getClientOrNull(String clientId) {
        try {
            return clientApi.getById(clientId);
        } catch (ApiException e) {
            return null;
        }
    }
}

