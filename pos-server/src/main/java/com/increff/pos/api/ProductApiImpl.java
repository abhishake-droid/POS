package com.increff.pos.api;

import com.increff.pos.dao.ClientDao;
import com.increff.pos.dao.ProductDao;
import com.increff.pos.db.ClientPojo;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * API implementation for Product operations
 */
@Service
public class ProductApiImpl implements ProductApi {
    private static final Logger logger = LoggerFactory.getLogger(ProductApiImpl.class);

    private final ProductDao productDao;
    private final ClientDao clientDao;

    public ProductApiImpl(ProductDao productDao, ClientDao clientDao) {
        this.productDao = productDao;
        this.clientDao = clientDao;
    }

    @Override
    @Transactional(rollbackFor = ApiException.class)
    public ProductPojo add(ProductPojo productPojo) throws ApiException {
        logger.info("Creating product with barcode: {}", productPojo.getBarcode());

        // Validate barcode
        validateBarcodeFormat(productPojo.getBarcode());
        
        // Check if barcode already exists
        if (productDao.findByBarcode(productPojo.getBarcode()) != null) {
            throw new ApiException("Product with barcode " + productPojo.getBarcode() + " already exists");
        }

        // Validate client exists
        ClientPojo client = clientDao.findByClientId(productPojo.getClientId());
        if (client == null) {
            throw new ApiException("Client with ID " + productPojo.getClientId() + " does not exist");
        }

        // Validate name
        validateNameFormat(productPojo.getName());

        // Validate MRP
        validateMrp(productPojo.getMrp());

        // Normalize data (lowercase, trim)
        productPojo.setBarcode(productPojo.getBarcode().trim().toLowerCase());
        productPojo.setName(productPojo.getName().trim().toLowerCase());
        productPojo.setClientId(productPojo.getClientId().trim());
        if (productPojo.getImageUrl() != null) {
            productPojo.setImageUrl(productPojo.getImageUrl().trim());
        }

        ProductPojo saved = productDao.save(productPojo);
        logger.info("Product created successfully with barcode: {}", saved.getBarcode());
        return saved;
    }

    @Override
    public ProductPojo get(String id) throws ApiException {
        ProductPojo productPojo = productDao.findById(id).orElse(null);
        if (Objects.isNull(productPojo)) {
            throw new ApiException("Product not found with id: " + id);
        }
        return productPojo;
    }

    @Override
    public ProductPojo getByBarcode(String barcode) throws ApiException {
        ProductPojo productPojo = productDao.findByBarcode(
                barcode != null ? barcode.trim().toLowerCase() : null
        );
        if (Objects.isNull(productPojo)) {
            throw new ApiException("Product not found with barcode: " + barcode);
        }
        return productPojo;
    }

    @Override
    public Page<ProductPojo> getAll(int page, int size) {
        logger.info("Fetching products page {} with size {}", page, size);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return productDao.findAll(pageRequest);
    }

    @Override
    @Transactional(rollbackFor = ApiException.class)
    public ProductPojo update(String id, ProductPojo productPojo) throws ApiException {
        logger.info("Updating product with id: {}", id);

        ProductPojo existing = get(id);

        // Validate barcode if provided
        if (productPojo.getBarcode() != null) {
            validateBarcodeFormat(productPojo.getBarcode());
            ProductPojo barcodeDuplicate = productDao.findByBarcode(productPojo.getBarcode().trim().toLowerCase());
            if (barcodeDuplicate != null && !barcodeDuplicate.getId().equals(id)) {
                throw new ApiException("Barcode already exists");
            }
            existing.setBarcode(productPojo.getBarcode().trim().toLowerCase());
        }

        // Validate client if provided
        if (productPojo.getClientId() != null) {
            ClientPojo client = clientDao.findByClientId(productPojo.getClientId().trim());
            if (client == null) {
                throw new ApiException("Client with ID " + productPojo.getClientId() + " does not exist");
            }
            existing.setClientId(productPojo.getClientId().trim());
        }

        // Update name
        if (productPojo.getName() != null) {
            validateNameFormat(productPojo.getName());
            existing.setName(productPojo.getName().trim().toLowerCase());
        }

        // Update MRP
        if (productPojo.getMrp() != null) {
            validateMrp(productPojo.getMrp());
            existing.setMrp(productPojo.getMrp());
        }

        // Update image URL
        if (productPojo.getImageUrl() != null) {
            existing.setImageUrl(productPojo.getImageUrl().trim());
        }

        ProductPojo updated = productDao.save(existing);
        logger.info("Updated product with id: {}", updated.getId());
        return updated;
    }

    @Override
    @Transactional(rollbackFor = ApiException.class)
    public List<ProductPojo> addBulk(List<ProductPojo> productPojos) throws ApiException {
        logger.info("Bulk adding {} products", productPojos.size());
        
        // Validate row limit (5000 max)
        if (productPojos.size() > 5000) {
            throw new ApiException("Cannot upload more than 5000 rows at once");
        }

        for (ProductPojo productPojo : productPojos) {
            // Validate barcode
            validateBarcodeFormat(productPojo.getBarcode());
            
            // Check if barcode already exists
            if (productDao.findByBarcode(productPojo.getBarcode().trim().toLowerCase()) != null) {
                throw new ApiException("Product with barcode " + productPojo.getBarcode() + " already exists");
            }

            // Validate client exists
            ClientPojo client = clientDao.findByClientId(productPojo.getClientId().trim());
            if (client == null) {
                throw new ApiException("Client with ID " + productPojo.getClientId() + " does not exist");
            }

            // Validate name
            validateNameFormat(productPojo.getName());

            // Validate MRP
            validateMrp(productPojo.getMrp());

            // Normalize data
            productPojo.setBarcode(productPojo.getBarcode().trim().toLowerCase());
            productPojo.setName(productPojo.getName().trim().toLowerCase());
            productPojo.setClientId(productPojo.getClientId().trim());
            if (productPojo.getImageUrl() != null) {
                productPojo.setImageUrl(productPojo.getImageUrl().trim());
            }
        }

        List<ProductPojo> saved = productDao.saveAll(productPojos);
        logger.info("Bulk added {} products successfully", saved.size());
        return saved;
    }

    private void validateBarcodeFormat(String barcode) throws ApiException {
        if (barcode == null || barcode.trim().isEmpty()) {
            throw new ApiException("Barcode cannot be empty");
        }
        if (barcode.trim().length() < 3 || barcode.trim().length() > 50) {
            throw new ApiException("Barcode must be between 3 and 50 characters");
        }
    }

    private void validateNameFormat(String name) throws ApiException {
        if (name == null || name.trim().isEmpty()) {
            throw new ApiException("Product name cannot be empty");
        }
        if (name.trim().length() < 3 || name.trim().length() > 100) {
            throw new ApiException("Product name must be between 3 and 100 characters");
        }
    }

    private void validateMrp(Double mrp) throws ApiException {
        if (mrp == null) {
            throw new ApiException("MRP cannot be empty");
        }
        if (mrp < 0) {
            throw new ApiException("MRP cannot be negative");
        }
    }
}
