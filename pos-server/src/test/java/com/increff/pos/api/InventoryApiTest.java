package com.increff.pos.api;

import com.increff.pos.db.ClientPojo;
import com.increff.pos.db.InventoryPojo;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.test.AbstractUnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class InventoryApiTest extends AbstractUnitTest {

    @Autowired
    private InventoryApi inventoryApi;

    @Autowired
    private ProductApi productApi;

    @Autowired
    private ClientApi clientApi;

    @Test
    void testAdd_Success() throws ApiException {
        // Given
        ProductPojo product = createTestProduct("BC_INV1");

        InventoryPojo inventory = new InventoryPojo();
        inventory.setProductId(product.getId());
        inventory.setQuantity(100);

        // When
        InventoryPojo result = inventoryApi.add(inventory);

        // Then
        assertNotNull(result);
        assertEquals(product.getId(), result.getProductId());
        assertEquals(100, result.getQuantity());
    }

    @Test
    void testGetCheckByProductId_Success() throws ApiException {
        // Given
        ProductPojo product = createTestProduct("BC_INV2");

        InventoryPojo inventory = new InventoryPojo();
        inventory.setProductId(product.getId());
        inventory.setQuantity(50);
        inventoryApi.add(inventory);

        // When
        InventoryPojo result = inventoryApi.getCheckByProductId(product.getId());

        // Then
        assertNotNull(result);
        assertEquals(product.getId(), result.getProductId());
        assertEquals(50, result.getQuantity());
    }

    @Test
    void testUpdateByProductId_Success() throws ApiException {
        // Given
        ProductPojo product = createTestProduct("BC_INV3");

        InventoryPojo inventory = new InventoryPojo();
        inventory.setProductId(product.getId());
        inventory.setQuantity(25);
        inventoryApi.add(inventory);

        // When - updateByProductId INCREMENTS quantity, not sets it
        InventoryPojo updated = inventoryApi.updateByProductId(product.getId(), 75);

        // Then - 25 + 75 = 100
        assertEquals(100, updated.getQuantity());
    }

    // Helper method
    private ProductPojo createTestProduct(String barcode) throws ApiException {
        ClientPojo client = new ClientPojo();
        client.setClientId(barcode + "_client");
        client.setName("Test Client");
        client.setEmail(barcode + "@example.com");
        client.setPhone("1234567890");
        client = clientApi.add(client);

        ProductPojo product = new ProductPojo();
        product.setBarcode(barcode);
        product.setClientId(client.getClientId());
        product.setName("Test Product");
        product.setMrp(100.0);
        return productApi.add(product);
    }

    @Test
    void testGetCheck_Success() throws ApiException {
        // Given
        ProductPojo product = createTestProduct("BC_INV4");
        InventoryPojo inventory = new InventoryPojo();
        inventory.setProductId(product.getId());
        inventory.setQuantity(30);
        InventoryPojo saved = inventoryApi.add(inventory);

        // When
        InventoryPojo result = inventoryApi.getCheck(saved.getId());

        // Then
        assertNotNull(result);
        assertEquals(saved.getId(), result.getId());
        assertEquals(30, result.getQuantity());
    }

    @Test
    void testGetCheck_NotFound() {
        // When/Then
        assertThrows(ApiException.class, () -> inventoryApi.getCheck("nonexistent"));
    }

    @Test
    void testGetCheckByProductId_NotFound() {
        // When/Then
        assertThrows(ApiException.class, () -> inventoryApi.getCheckByProductId("nonexistent"));
    }

    @Test
    void testUpdate_Success() throws ApiException {
        // Given
        ProductPojo product = createTestProduct("BC_INV5");
        InventoryPojo inventory = new InventoryPojo();
        inventory.setProductId(product.getId());
        inventory.setQuantity(40);
        InventoryPojo saved = inventoryApi.add(inventory);

        // When
        saved.setQuantity(60);
        InventoryPojo updated = inventoryApi.update(saved.getId(), saved);

        // Then
        assertEquals(60, updated.getQuantity());
    }

    @Test
    void testUpdateByProductId_NegativeIncrement() throws ApiException {
        // Given
        ProductPojo product = createTestProduct("BC_INV6");
        InventoryPojo inventory = new InventoryPojo();
        inventory.setProductId(product.getId());
        inventory.setQuantity(100);
        inventoryApi.add(inventory);

        // When - Decrement by negative value
        InventoryPojo updated = inventoryApi.updateByProductId(product.getId(), -30);

        // Then - 100 + (-30) = 70
        assertEquals(70, updated.getQuantity());
    }

    @Test
    void testAdd_ZeroQuantity() throws ApiException {
        // Given
        ProductPojo product = createTestProduct("BC_INV7");
        InventoryPojo inventory = new InventoryPojo();
        inventory.setProductId(product.getId());
        inventory.setQuantity(0);

        // When
        InventoryPojo result = inventoryApi.add(inventory);

        // Then - Zero quantity should be allowed
        assertNotNull(result);
        assertEquals(0, result.getQuantity());
    }

    @Test
    void testUpdate_NotFound() {
        // Given
        InventoryPojo inventory = new InventoryPojo();
        inventory.setQuantity(50);

        // When/Then
        assertThrows(ApiException.class, () -> inventoryApi.update("nonexistent", inventory));
    }
}
