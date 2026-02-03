package com.increff.pos.api;

import com.increff.pos.db.ClientPojo;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.test.AbstractUnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductApiTest extends AbstractUnitTest {

    @Autowired
    private ProductApi productApi;

    @Autowired
    private ClientApi clientApi;

    @Test
    void testAdd_Success() throws ApiException {
        // Given
        ClientPojo client = createTestClient("testclient", "test@example.com");

        ProductPojo product = new ProductPojo();
        product.setBarcode("BC123");
        product.setClientId(client.getClientId());
        product.setName("Test Product");
        product.setMrp(100.0);

        // When
        ProductPojo result = productApi.add(product);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("BC123", result.getBarcode());
        assertEquals("Test Product", result.getName());
        assertEquals(100.0, result.getMrp());
    }

    @Test
    void testGetCheck_Success() throws ApiException {
        // Given
        ClientPojo client = createTestClient("testclient2", "test2@example.com");

        ProductPojo product = new ProductPojo();
        product.setBarcode("BC_GET");
        product.setClientId(client.getClientId());
        product.setName("Get Product");
        product.setMrp(200.0);
        ProductPojo saved = productApi.add(product);

        // When
        ProductPojo result = productApi.getCheck(saved.getId());

        // Then
        assertNotNull(result);
        assertEquals(saved.getId(), result.getId());
        assertEquals("BC_GET", result.getBarcode());
    }

    @Test
    void testGetCheck_NotFound() {
        // When/Then
        assertThrows(ApiException.class, () -> productApi.getCheck("nonexistent_id"));
    }

    @Test
    void testGetCheckByBarcode_Success() throws ApiException {
        // Given
        ClientPojo client = createTestClient("testclient3", "test3@example.com");

        ProductPojo product = new ProductPojo();
        product.setBarcode("BC_GETBY");
        product.setClientId(client.getClientId());
        product.setName("GetBy Product");
        product.setMrp(150.0);
        productApi.add(product);

        // When
        ProductPojo result = productApi.getCheckByBarcode("BC_GETBY");

        // Then
        assertNotNull(result);
        assertEquals("BC_GETBY", result.getBarcode());
    }

    @Test
    void testUpdate_Success() throws ApiException {
        // Given
        ClientPojo client = createTestClient("testclient4", "test4@example.com");

        ProductPojo product = new ProductPojo();
        product.setBarcode("BC_UPDATE");
        product.setClientId(client.getClientId());
        product.setName("Original Name");
        product.setMrp(100.0);
        ProductPojo saved = productApi.add(product);

        // When
        saved.setName("Updated Name");
        saved.setMrp(150.0);
        ProductPojo updated = productApi.update(saved.getId(), saved);

        // Then
        assertEquals("Updated Name", updated.getName());
        assertEquals(150.0, updated.getMrp());
    }

    // Helper method
    private ClientPojo createTestClient(String clientId, String email) throws ApiException {
        ClientPojo client = new ClientPojo();
        client.setClientId(clientId);
        client.setName("Test Client");
        client.setEmail(email);
        client.setPhone("1234567890");
        return clientApi.add(client);
    }

    @Test
    void testGetCheckByBarcode_NotFound() {
        // When/Then
        assertThrows(ApiException.class, () -> productApi.getCheckByBarcode("NONEXISTENT"));
    }

    @Test
    void testGetByIds_Success() throws ApiException {
        // Given
        ClientPojo client = createTestClient("testclient6", "test6@example.com");
        ProductPojo product1 = new ProductPojo();
        product1.setBarcode("BC_IDS1");
        product1.setClientId(client.getClientId());
        product1.setName("Product 1");
        product1.setMrp(100.0);
        ProductPojo saved1 = productApi.add(product1);

        ProductPojo product2 = new ProductPojo();
        product2.setBarcode("BC_IDS2");
        product2.setClientId(client.getClientId());
        product2.setName("Product 2");
        product2.setMrp(200.0);
        ProductPojo saved2 = productApi.add(product2);

        // When
        var results = productApi.getByIds(java.util.Arrays.asList(saved1.getId(), saved2.getId()));

        // Then
        assertNotNull(results);
        assertEquals(2, results.size());
    }

    @Test
    void testUpdate_NotFound() {
        // Given
        ProductPojo product = new ProductPojo();
        product.setName("Updated");

        // When/Then
        assertThrows(ApiException.class, () -> productApi.update("nonexistent", product));
    }

    @Test
    void testGetByIds_MultipleProducts() throws ApiException {
        // Given
        ClientPojo client = createTestClient("testclient9", "test9@example.com");

        ProductPojo product1 = new ProductPojo();
        product1.setBarcode("BC_MULTI1");
        product1.setClientId(client.getClientId());
        product1.setName("Multi Product 1");
        product1.setMrp(100.0);
        ProductPojo saved1 = productApi.add(product1);

        ProductPojo product2 = new ProductPojo();
        product2.setBarcode("BC_MULTI2");
        product2.setClientId(client.getClientId());
        product2.setName("Multi Product 2");
        product2.setMrp(200.0);
        ProductPojo saved2 = productApi.add(product2);

        // When
        var results = productApi.getByIds(java.util.Arrays.asList(saved1.getId(), saved2.getId()));

        // Then
        assertNotNull(results);
        assertEquals(2, results.size());
    }

    @Test
    void testGetByIds_EmptyList() throws ApiException {
        // When
        List<ProductPojo> results = productApi.getByIds(new ArrayList<>());

        // Then
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void testGetByIds_SingleId() throws ApiException {
        // Given
        ClientPojo client = createTestClient("testclient11", "test11@example.com");
        ProductPojo product = new ProductPojo();
        product.setBarcode("BC_SINGLE");
        product.setClientId(client.getClientId());
        product.setName("Single Product");
        product.setMrp(100.0);
        ProductPojo saved = productApi.add(product);

        // When
        List<ProductPojo> results = productApi.getByIds(Arrays.asList(saved.getId()));

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(saved.getId(), results.get(0).getId());
    }

}
