package com.increff.pos.api;

import com.increff.pos.dao.InventoryDao;
import com.increff.pos.dao.ProductDao;
import com.increff.pos.db.InventoryPojo;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
public class InventoryApiImpl implements InventoryApi {

    @Autowired
    private InventoryDao inventoryDao;
    @Autowired
    private ProductDao productDao;

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

        validateInventoryLimit(inventoryPojo.getQuantity());

        return inventoryDao.save(inventoryPojo);
    }

    @Override
    @Transactional(rollbackFor = ApiException.class)
    public List<InventoryPojo> addBulk(List<InventoryPojo> inventories) throws ApiException {
        if (inventories == null || inventories.isEmpty()) {
            return new ArrayList<>();
        }

        for (InventoryPojo inventory : inventories) {
            validateInventoryLimit(inventory.getQuantity());
        }
        return inventoryDao.saveAll(inventories);
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
        validateInventoryLimit(inventoryPojo.getQuantity());
        existing.setQuantity(inventoryPojo.getQuantity());
        return inventoryDao.save(existing);
    }

    @Override
    @Transactional(rollbackFor = ApiException.class)
    public InventoryPojo updateByProductId(String productId, Integer quantity) throws ApiException {
        InventoryPojo updated = inventoryDao.incrementQuantityByProductId(productId, quantity);
        if (updated == null) {
            throw new ApiException("Failed to update inventory for product " + productId);
        }
        return updated;
    }

    @Override
    @Transactional(rollbackFor = ApiException.class)
    public List<InventoryPojo> updateBulk(List<InventoryPojo> inventoryPojos) throws ApiException {
        if (inventoryPojos == null || inventoryPojos.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, Integer> quantityByProductId = new LinkedHashMap<>();
        for (InventoryPojo inventoryPojo : inventoryPojos) {
            quantityByProductId.put(inventoryPojo.getProductId(), inventoryPojo.getQuantity());
        }

        List<String> productIds = new ArrayList<>(quantityByProductId.keySet());

        List<InventoryPojo> existingInventories = inventoryDao.findByProductIds(productIds);
        Map<String, InventoryPojo> existingByProductId = existingInventories.stream()
                .collect(Collectors.toMap(InventoryPojo::getProductId, inv -> inv));

        List<InventoryPojo> toSave = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : quantityByProductId.entrySet()) {
            String productId = entry.getKey();
            Integer quantityToAdd = entry.getValue();

            InventoryPojo existing = existingByProductId.get(productId);
            if (existing != null) {
                existing.setQuantity(existing.getQuantity() + quantityToAdd);
                toSave.add(existing);
            } else {
                InventoryPojo newInv = new InventoryPojo();
                newInv.setProductId(productId);
                newInv.setQuantity(quantityToAdd);
                toSave.add(newInv);
            }
        }

        return inventoryDao.saveAll(toSave);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryPojo> getByProductIds(List<String> productIds) {
        return inventoryDao.findByProductIds(productIds);
    }

    @Override
    @Transactional(rollbackFor = ApiException.class)
    public void bulkUpdateQuantities(Map<String, Integer> productIdToQuantity) {
        inventoryDao.bulkUpdateQuantities(productIdToQuantity);
    }

    private void validateInventoryLimit(Integer quantity) throws ApiException {
        if (quantity != null && quantity > 5000) {
            throw new ApiException("Inventory quantity cannot exceed 5000");
        }
    }
}
