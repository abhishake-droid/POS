package com.increff.pos.api;

import com.increff.pos.dao.InventoryDao;
import com.increff.pos.dao.ProductDao;
import com.increff.pos.db.InventoryPojo;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ArrayList;

@Service
public class InventoryApiImpl implements InventoryApi {

    private final InventoryDao inventoryDao;
    private final ProductDao productDao;

    public InventoryApiImpl(InventoryDao inventoryDao, ProductDao productDao) {
        this.inventoryDao = inventoryDao;
        this.productDao = productDao;
    }

    @Override
    @Transactional(rollbackFor = ApiException.class)
    public InventoryPojo add(InventoryPojo inventoryPojo) throws ApiException {
        ProductPojo product = productDao.findById(inventoryPojo.getProductId()).orElse(null);
        if (product == null) {
            throw new ApiException("Product with ID " + inventoryPojo.getProductId() + " does not exist");
        }

        InventoryPojo existing = inventoryDao.findByProductId(inventoryPojo.getProductId());
        if (existing != null) {
            throw new ApiException("Inventory already exists for product " + inventoryPojo.getProductId());
        }

        // Validate inventory limit
        if (inventoryPojo.getQuantity() != null && inventoryPojo.getQuantity() > 5000) {
            throw new ApiException("Inventory quantity cannot exceed 5000");
        }

        return inventoryDao.save(inventoryPojo);
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryPojo getCheck(String id) throws ApiException {
        InventoryPojo inventoryPojo = inventoryDao.findById(id).orElse(null);
        if (Objects.isNull(inventoryPojo)) {
            throw new ApiException("Inventory not found with id: " + id);
        }
        return inventoryPojo;
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryPojo getCheckByProductId(String productId) throws ApiException {
        InventoryPojo inventoryPojo = inventoryDao.findByProductId(productId);
        if (Objects.isNull(inventoryPojo)) {
            throw new ApiException("Inventory not found for productId: " + productId);
        }
        return inventoryPojo;
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryPojo getByProductId(String productId) {
        return inventoryDao.findByProductId(productId);
    }

    @Override
    @Transactional(rollbackFor = ApiException.class)
    public InventoryPojo update(String id, InventoryPojo inventoryPojo) throws ApiException {
        InventoryPojo existing = getCheck(id);
        if (inventoryPojo.getQuantity() != null) {
            // Validate inventory limit
            if (inventoryPojo.getQuantity() > 5000) {
                throw new ApiException("Inventory quantity cannot exceed 5000");
            }
            existing.setQuantity(inventoryPojo.getQuantity());
        }
        return inventoryDao.save(existing);
    }

    @Override
    @Transactional(rollbackFor = ApiException.class)
    public InventoryPojo updateByProductId(String productId, Integer quantity) throws ApiException {
        try {
            InventoryPojo updated = inventoryDao.incrementQuantityByProductId(productId, quantity);
            if (updated == null) {
                throw new ApiException("Failed to update inventory for product " + productId);
            }
            return updated;
        } catch (Exception e) {
            throw new ApiException("Failed to update inventory for product " + productId + ": " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = ApiException.class)
    public List<InventoryPojo> updateBulk(List<InventoryPojo> inventoryPojos) throws ApiException {
        Map<String, Integer> quantityByProductId = new LinkedHashMap<>();
        for (InventoryPojo inventoryPojo : inventoryPojos) {
            quantityByProductId.put(inventoryPojo.getProductId(), inventoryPojo.getQuantity());
        }

        List<InventoryPojo> saved = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : quantityByProductId.entrySet()) {
            String productId = entry.getKey();
            Integer quantityToAdd = entry.getValue();

            InventoryPojo existing = inventoryDao.findByProductId(productId);
            if (existing != null) {
                Integer newQuantity = existing.getQuantity() + quantityToAdd;
                existing.setQuantity(newQuantity);
                saved.add(inventoryDao.save(existing));
            } else {
                saved.add(updateByProductId(productId, quantityToAdd));
            }
        }
        return saved;
    }
}
