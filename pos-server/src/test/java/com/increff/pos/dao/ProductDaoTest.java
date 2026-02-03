package com.increff.pos.dao;

import com.increff.pos.db.ProductPojo;
import com.increff.pos.test.AbstractUnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ProductDaoTest extends AbstractUnitTest {

    @Autowired
    private ProductDao productDao;

    @Test
    void testSaveAndFindById() {
        // Given
        ProductPojo product = new ProductPojo();
        product.setBarcode("BC123");
        product.setName("Test Product");
        product.setMrp(100.0);
        product.setClientId("client123");

        // When
        ProductPojo saved = productDao.save(product);
        Optional<ProductPojo> found = productDao.findById(saved.getId());

        // Then
        assertTrue(found.isPresent());
        assertEquals("BC123", found.get().getBarcode());
        assertEquals("Test Product", found.get().getName());
    }

    @Test
    void testFindByBarcode() {
        // Given
        ProductPojo product = new ProductPojo();
        product.setBarcode("BC456");
        product.setName("Another Product");
        product.setMrp(200.0);
        product.setClientId("client456");
        productDao.save(product);

        // When
        Optional<ProductPojo> result = productDao.findByBarcode("BC456");

        // Then
        assertTrue(result.isPresent());
        assertEquals("BC456", result.get().getBarcode());
    }

    @Test
    void testFindByBarcode_NotFound() {
        // When
        Optional<ProductPojo> result = productDao.findByBarcode("NONEXISTENT");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void testFindByClientId() {
        // Given
        ProductPojo product1 = new ProductPojo();
        product1.setBarcode("BC1");
        product1.setName("Product 1");
        product1.setMrp(100.0);
        product1.setClientId("client789");
        productDao.save(product1);

        ProductPojo product2 = new ProductPojo();
        product2.setBarcode("BC2");
        product2.setName("Product 2");
        product2.setMrp(200.0);
        product2.setClientId("client789");
        productDao.save(product2);

        // When
        List<ProductPojo> results = productDao.findByClientId("client789");

        // Then
        assertEquals(2, results.size());
    }

    @Test
    void testDelete() {
        // Given
        ProductPojo product = new ProductPojo();
        product.setBarcode("BC_DELETE");
        product.setName("To Delete");
        product.setMrp(50.0);
        product.setClientId("client_delete");
        ProductPojo saved = productDao.save(product);

        // When
        productDao.deleteById(saved.getId());
        Optional<ProductPojo> found = productDao.findById(saved.getId());

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    void testFindByClientId_EmptyResult() {
        // When
        List<ProductPojo> results = productDao.findByClientId("nonexistent_client");

        // Then
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void testUpdate() {
        // Given
        ProductPojo product = new ProductPojo();
        product.setBarcode("BC_UPDATE");
        product.setName("Original");
        product.setMrp(100.0);
        product.setClientId("client_update");
        ProductPojo saved = productDao.save(product);

        // When
        saved.setName("Updated");
        saved.setMrp(150.0);
        productDao.save(saved);
        Optional<ProductPojo> updated = productDao.findById(saved.getId());

        // Then
        assertTrue(updated.isPresent());
        assertEquals("Updated", updated.get().getName());
        assertEquals(150.0, updated.get().getMrp());
    }

    @Test
    void testFindAll() {
        // Given
        ProductPojo product = new ProductPojo();
        product.setBarcode("BC_ALL");
        product.setName("All Test");
        product.setMrp(100.0);
        product.setClientId("client_all");
        productDao.save(product);

        // When
        List<ProductPojo> all = productDao.findAll();

        // Then
        assertNotNull(all);
        assertTrue(all.size() > 0);
    }

    @Test
    void testFindById_NotFound() {
        // When
        Optional<ProductPojo> result = productDao.findById("nonexistent_id");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void testSaveMultipleProducts() {
        // When
        for (int i = 0; i < 5; i++) {
            ProductPojo product = new ProductPojo();
            product.setBarcode("BC_BULK_" + i);
            product.setName("Bulk Product " + i);
            product.setMrp(100.0 + i);
            product.setClientId("bulk_client");
            productDao.save(product);
        }

        // Then
        List<ProductPojo> all = productDao.findAll();
        assertTrue(all.size() >= 5);
    }

    @Test
    void testUpdateProduct() {
        // Given
        ProductPojo product = new ProductPojo();
        product.setBarcode("BC_UPDATE");
        product.setName("Original Name");
        product.setMrp(100.0);
        product.setClientId("client_update");
        ProductPojo saved = productDao.save(product);

        // When
        saved.setName("Updated Name");
        saved.setMrp(200.0);
        ProductPojo updated = productDao.save(saved);

        // Then
        assertEquals("Updated Name", updated.getName());
        assertEquals(200.0, updated.getMrp());
    }

    @Test
    void testUpdateFieldsById() {
        // Given
        ProductPojo product = new ProductPojo();
        product.setBarcode("BC_FIELDS");
        product.setName("Original Name");
        product.setMrp(100.0);
        product.setClientId("client_original");
        ProductPojo saved = productDao.save(product);

        // When
        ProductPojo updated = productDao.updateFieldsById(saved.getId(), "new_client", "New Name", 250.0,
                "http://image.url");

        // Then
        assertNotNull(updated);
        assertEquals("New Name", updated.getName());
        assertEquals(250.0, updated.getMrp());
        assertEquals("new_client", updated.getClientId());
        assertEquals("http://image.url", updated.getImageUrl());
    }

    @Test
    void testFindByIds() {
        // Given
        ProductPojo product1 = new ProductPojo();
        product1.setBarcode("BC_IDS1");
        product1.setName("Product 1");
        product1.setMrp(100.0);
        product1.setClientId("client1");
        ProductPojo saved1 = productDao.save(product1);

        ProductPojo product2 = new ProductPojo();
        product2.setBarcode("BC_IDS2");
        product2.setName("Product 2");
        product2.setMrp(200.0);
        product2.setClientId("client1");
        ProductPojo saved2 = productDao.save(product2);

        // When
        List<ProductPojo> results = productDao.findByIds(Arrays.asList(saved1.getId(), saved2.getId()));

        // Then
        assertEquals(2, results.size());
    }

    @Test
    void testFindBarcodesByBarcodes() {
        // Given
        ProductPojo product1 = new ProductPojo();
        product1.setBarcode("BC_SEARCH1");
        product1.setName("Product 1");
        product1.setMrp(100.0);
        product1.setClientId("client1");
        productDao.save(product1);

        ProductPojo product2 = new ProductPojo();
        product2.setBarcode("BC_SEARCH2");
        product2.setName("Product 2");
        product2.setMrp(200.0);
        product2.setClientId("client1");
        productDao.save(product2);

        // When
        List<String> barcodes = productDao
                .findBarcodesByBarcodes(Arrays.asList("BC_SEARCH1", "BC_SEARCH2", "NONEXISTENT"));

        // Then
        assertEquals(2, barcodes.size());
        assertTrue(barcodes.contains("BC_SEARCH1"));
        assertTrue(barcodes.contains("BC_SEARCH2"));
    }
}
