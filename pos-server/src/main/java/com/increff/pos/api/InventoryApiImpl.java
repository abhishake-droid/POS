package com.increff.pos.api;

import com.increff.pos.dao.InventoryDao;
import com.increff.pos.dao.ProductDao;
import com.increff.pos.db.InventoryPojo;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
import org.springframework.dao.DuplicateKeyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class InventoryApiImpl implements InventoryApi {
    private static final Logger logger = LoggerFactory.getLogger(InventoryApiImpl.class);

    private final InventoryDao inventoryDao;
    private final ProductDao productDao;

    public InventoryApiImpl(InventoryDao inventoryDao, ProductDao productDao) {
        this.inventoryDao = inventoryDao;
        this.productDao = productDao;
    }

    @Override
    @Transactional(rollbackFor = ApiException.class)
    public InventoryPojo add(InventoryPojo inventoryPojo) throws ApiException {
        logger.info("Creating inventory for productId: {}", inventoryPojo.getProductId());

        // Validate product exists
        ProductPojo product = productDao.findById(inventoryPojo.getProductId()).orElse(null);
        if (product == null) {
            throw new ApiException("Product with ID " + inventoryPojo.getProductId() + " does not exist");
        }

        // Check if inventory already exists for this product
        InventoryPojo existing = inventoryDao.findByProductId(inventoryPojo.getProductId());
        if (existing != null) {
            throw new ApiException("Inventory already exists for product " + inventoryPojo.getProductId());
        }

        // Validate quantity
        validateQuantity(inventoryPojo.getQuantity());

        InventoryPojo saved = inventoryDao.save(inventoryPojo);
        logger.info("Inventory created successfully for productId: {}", saved.getProductId());
        return saved;
    }

    @Override
    public InventoryPojo get(String id) throws ApiException {
        InventoryPojo inventoryPojo = inventoryDao.findById(id).orElse(null);
        if (Objects.isNull(inventoryPojo)) {
            throw new ApiException("Inventory not found with id: " + id);
        }
        return inventoryPojo;
    }

    @Override
    public InventoryPojo getByProductId(String productId) throws ApiException {
        InventoryPojo inventoryPojo = inventoryDao.findByProductId(productId);
        if (Objects.isNull(inventoryPojo)) {
            throw new ApiException("Inventory not found for productId: " + productId);
        }
        return inventoryPojo;
    }

    @Override
    @Transactional(rollbackFor = ApiException.class)
    public InventoryPojo update(String id, InventoryPojo inventoryPojo) throws ApiException {
        logger.info("Updating inventory with id: {}", id);

        InventoryPojo existing = get(id);

        // Validate quantity
        if (inventoryPojo.getQuantity() != null) {
            validateQuantity(inventoryPojo.getQuantity());
            existing.setQuantity(inventoryPojo.getQuantity());
        }

        InventoryPojo updated = inventoryDao.save(existing);
        logger.info("Updated inventory with id: {}", updated.getId());
        return updated;
    }

    @Override
    @Transactional(rollbackFor = ApiException.class)
    public InventoryPojo updateByProductId(String productId, Integer quantity) throws ApiException {
        productId = productId != null ? productId.trim() : null;
        logger.info("Updating inventory for productId: {} with quantity: {}", productId, quantity);

        if (productId == null || productId.isEmpty()) {
            throw new ApiException("Product ID cannot be empty");
        }

        // Validate product exists
        ProductPojo product = productDao.findById(productId).orElse(null);
        if (product == null) {
            throw new ApiException("Product with ID " + productId + " does not exist");
        }

        // Validate quantity
        validateQuantity(quantity);

        try {
            InventoryPojo updated = inventoryDao.upsertQuantityByProductId(productId, quantity);
            if (updated == null) {
                updated = inventoryDao.findByProductId(productId);
            }
            logger.info("Updated inventory for productId: {}", productId);
            return updated;
        } catch (DuplicateKeyException e) {
            // Extremely rare, but handle concurrency issues gracefully
            InventoryPojo retryExisting = inventoryDao.findByProductId(productId);
            if (retryExisting == null) {
                throw new ApiException("Inventory update failed: inventory already exists for product " + productId);
            }
            retryExisting.setQuantity(quantity);
            InventoryPojo updated = inventoryDao.save(retryExisting);
            logger.info("Updated inventory for productId: {} after duplicate key retry", productId);
            return updated;
        } catch (Exception e) {
            throw new ApiException("Failed to update inventory for product " + productId);
        }
    }

    @Override
    @Transactional(rollbackFor = ApiException.class)
    public List<InventoryPojo> updateBulk(List<InventoryPojo> inventoryPojos) throws ApiException {
        logger.info("Bulk updating {} inventory records", inventoryPojos.size());

        // Validate row limit (5000 max)
        if (inventoryPojos.size() > 5000) {
            throw new ApiException("Cannot upload more than 5000 rows at once");
        }

        // Deduplicate within the same request (last quantity wins) to avoid unique-index collisions on productId.
        Map<String, Integer> quantityByProductId = new LinkedHashMap<>();
        for (InventoryPojo inventoryPojo : inventoryPojos) {
            String productId = inventoryPojo.getProductId() != null ? inventoryPojo.getProductId().trim() : null;
            if (productId == null || productId.isEmpty()) {
                throw new ApiException("Product ID cannot be empty");
            }

            // Validate quantity
            validateQuantity(inventoryPojo.getQuantity());

            quantityByProductId.put(productId, inventoryPojo.getQuantity());
        }

        // Use atomic upserts per productId (avoids duplicate-key collisions)
        List<InventoryPojo> saved = new java.util.ArrayList<>();
        for (Map.Entry<String, Integer> entry : quantityByProductId.entrySet()) {
            saved.add(updateByProductId(entry.getKey(), entry.getValue()));
        }
        logger.info("Bulk updated {} inventory records successfully", saved.size());
        return saved;
    }

    private void validateQuantity(Integer quantity) throws ApiException {
        if (quantity == null) {
            throw new ApiException("Quantity cannot be empty");
        }
        if (quantity < 0) {
            throw new ApiException("Quantity cannot be negative");
        }
    }
}
