package com.increff.pos.api;

import com.increff.pos.db.ClientPojo;
import com.increff.pos.db.InventoryPojo;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.test.AbstractUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InventoryApiImplTest extends AbstractUnitTest {

    @Autowired
    private InventoryApi inventoryApi;

    @Autowired
    private ProductApi productApi;

    @Autowired
    private ClientApi clientApi;

    private ProductPojo testProduct;

    @BeforeEach
    void setUp() throws ApiException {
        // Create a client
        ClientPojo client = new ClientPojo();
        client.setName("Test Client");
        client.setPhone("1234567890");
        client.setEmail("test@example.com");
        clientApi.add(client);

        // Create a product
        testProduct = new ProductPojo();
        testProduct.setBarcode("test_barcode_inv");
        testProduct.setName("Test Product");
        testProduct.setMrp(100.0);
        testProduct.setClientId(client.getId());
        testProduct = productApi.add(testProduct);
    }

    @Test
    void testAdd_Success() throws ApiException {
        // Given
        InventoryPojo inventory = new InventoryPojo();
        inventory.setProductId(testProduct.getId());
        inventory.setQuantity(50);

        // When
        InventoryPojo result = inventoryApi.add(inventory);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(50, result.getQuantity());
    }

    @Test
    void testAdd_ProductNotFound() {
        // Given
        InventoryPojo inventory = new InventoryPojo();
        inventory.setProductId("nonexistent_product");
        inventory.setQuantity(50);

        // When/Then
        assertThrows(ApiException.class, () -> inventoryApi.add(inventory));
    }

    @Test
    void testAdd_QuantityExceedsLimit() {
        // Given
        InventoryPojo inventory = new InventoryPojo();
        inventory.setProductId(testProduct.getId());
        inventory.setQuantity(6000); // Exceeds 5000 limit

        // When/Then
        assertThrows(ApiException.class, () -> inventoryApi.add(inventory));
    }

    @Test
    void testUpdate_Success() throws ApiException {
        // Given
        InventoryPojo inventory = new InventoryPojo();
        inventory.setProductId(testProduct.getId());
        inventory.setQuantity(50);
        inventoryApi.add(inventory);

        // When - updateByProductId adds to existing quantity
        inventoryApi.updateByProductId(testProduct.getId(), 100);

        // Then
        InventoryPojo updated = inventoryApi.getByProductId(testProduct.getId());
        assertEquals(150, updated.getQuantity()); // 50 + 100
    }

    @Test
    void testGetByProductId_Success() throws ApiException {
        // Given
        InventoryPojo inventory = new InventoryPojo();
        inventory.setProductId(testProduct.getId());
        inventory.setQuantity(75);
        inventoryApi.add(inventory);

        // When
        InventoryPojo result = inventoryApi.getByProductId(testProduct.getId());

        // Then
        assertNotNull(result);
        assertEquals(75, result.getQuantity());
    }

    @Test
    void testGetByProductId_NotFound() {
        // When
        InventoryPojo result = inventoryApi.getByProductId("nonexistent");

        // Then - returns null instead of throwing
        assertNull(result);
    }

    @Test
    void testAdd_DuplicateInventory() throws ApiException {
        // Given
        InventoryPojo inventory1 = new InventoryPojo();
        inventory1.setProductId(testProduct.getId());
        inventory1.setQuantity(50);
        inventoryApi.add(inventory1);

        // When/Then - adding inventory for same product again should fail
        InventoryPojo inventory2 = new InventoryPojo();
        inventory2.setProductId(testProduct.getId());
        inventory2.setQuantity(100);
        assertThrows(ApiException.class, () -> inventoryApi.add(inventory2));
    }

    @Test
    void testAdd_NullQuantity() throws ApiException {
        // Given
        InventoryPojo inventory = new InventoryPojo();
        inventory.setProductId(testProduct.getId());
        inventory.setQuantity(null); // null quantity

        // When
        InventoryPojo result = inventoryApi.add(inventory);

        // Then - should succeed with null quantity
        assertNotNull(result);
        assertNull(result.getQuantity());
    }

    @Test
    void testAdd_QuantityAtLimit() throws ApiException {
        // Given
        InventoryPojo inventory = new InventoryPojo();
        inventory.setProductId(testProduct.getId());
        inventory.setQuantity(5000); // Exactly at limit

        // When
        InventoryPojo result = inventoryApi.add(inventory);

        // Then - should succeed at exactly 5000
        assertNotNull(result);
        assertEquals(5000, result.getQuantity());
    }
}
