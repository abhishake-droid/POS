package com.increff.pos.api;

import com.increff.pos.dao.ClientDao;
import com.increff.pos.dao.ProductDao;
import com.increff.pos.db.ClientPojo;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.HashSet;

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

        try {
            ProductPojo saved = productDao.save(productPojo);
            logger.info("Product created successfully with barcode: {}", saved.getBarcode());
            return saved;
        } catch (DuplicateKeyException e) {
            throw new ApiException("Product with barcode " + productPojo.getBarcode() + " already exists");
        }
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

        // Barcode is immutable after creation (ignore on update)

        // Validate client if provided
        String clientId = null;
        if (productPojo.getClientId() != null) {
            clientId = productPojo.getClientId().trim();
            ClientPojo client = clientDao.findByClientId(clientId);
            if (client == null) {
                throw new ApiException("Client with ID " + productPojo.getClientId() + " does not exist");
            }
        }

        // Update name
        String name = null;
        if (productPojo.getName() != null) {
            validateNameFormat(productPojo.getName());
            name = productPojo.getName().trim().toLowerCase();
        }

        // Update MRP
        Double mrp = null;
        if (productPojo.getMrp() != null) {
            validateMrp(productPojo.getMrp());
            mrp = productPojo.getMrp();
        }

        // Update image URL
        String imageUrl = null;
        if (productPojo.getImageUrl() != null) {
            imageUrl = productPojo.getImageUrl().trim();
        }

        ProductPojo updated = productDao.updateFieldsById(id, clientId, name, mrp, imageUrl);
        if (updated == null) {
            throw new ApiException("Product not found with id: " + id);
        }
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

        Set<String> seenBarcodes = new HashSet<>();
        for (ProductPojo productPojo : productPojos) {
            // Validate barcode
            validateBarcodeFormat(productPojo.getBarcode());

            String normalizedBarcode = productPojo.getBarcode().trim().toLowerCase();
            if (!seenBarcodes.add(normalizedBarcode)) {
                throw new ApiException("Duplicate barcode in upload: " + normalizedBarcode);
            }
            
            // Check if barcode already exists
            if (productDao.findByBarcode(normalizedBarcode) != null) {
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
            productPojo.setBarcode(normalizedBarcode);
            productPojo.setName(productPojo.getName().trim().toLowerCase());
            productPojo.setClientId(productPojo.getClientId().trim());
            if (productPojo.getImageUrl() != null) {
                productPojo.setImageUrl(productPojo.getImageUrl().trim());
            }
        }

        try {
            List<ProductPojo> saved = productDao.saveAll(productPojos);
            logger.info("Bulk added {} products successfully", saved.size());
            return saved;
        } catch (DuplicateKeyException e) {
            throw new ApiException("Bulk upload failed: a record with this key already exists");
        }
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
