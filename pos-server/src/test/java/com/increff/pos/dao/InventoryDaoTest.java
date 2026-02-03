package com.increff.pos.dao;

import com.increff.pos.db.InventoryPojo;
import com.increff.pos.test.AbstractUnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InventoryDaoTest extends AbstractUnitTest {

    @Autowired
    private InventoryDao inventoryDao;

    @Test
    void testSaveAndFindByProductId() {
        // Given
        InventoryPojo inventory = new InventoryPojo();
        inventory.setProductId("prod123");
        inventory.setQuantity(100);

        // When
        inventoryDao.save(inventory);
        InventoryPojo found = inventoryDao.findByProductId("prod123");

        // Then
        assertNotNull(found);
        assertEquals("prod123", found.getProductId());
        assertEquals(100, found.getQuantity());
    }

    @Test
    void testUpsertQuantityByProductId() {
        // Given
        String productId = "prod_upsert";

        // When - First upsert (insert)
        InventoryPojo result1 = inventoryDao.upsertQuantityByProductId(productId, 50);

        // Then
        assertNotNull(result1);
        assertEquals(50, result1.getQuantity());

        // When - Second upsert (update)
        InventoryPojo result2 = inventoryDao.upsertQuantityByProductId(productId, 75);

        // Then
        assertEquals(75, result2.getQuantity());
    }

    @Test
    void testIncrementQuantityByProductId() {
        // Given
        String productId = "prod_increment";
        inventoryDao.upsertQuantityByProductId(productId, 100);

        // When
        InventoryPojo result = inventoryDao.incrementQuantityByProductId(productId, 25);

        // Then
        assertEquals(125, result.getQuantity());
    }

    @Test
    void testFindByProductIds() {
        // Given
        InventoryPojo inv1 = new InventoryPojo();
        inv1.setProductId("prod1");
        inv1.setQuantity(10);
        inventoryDao.save(inv1);

        InventoryPojo inv2 = new InventoryPojo();
        inv2.setProductId("prod2");
        inv2.setQuantity(20);
        inventoryDao.save(inv2);

        // When
        List<InventoryPojo> results = inventoryDao.findByProductIds(Arrays.asList("prod1", "prod2"));

        // Then
        assertEquals(2, results.size());
    }
}
