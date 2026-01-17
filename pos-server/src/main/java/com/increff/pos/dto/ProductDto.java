package com.increff.pos.dto;

import com.increff.pos.api.InventoryApi;
import com.increff.pos.api.ProductApi;
import com.increff.pos.dao.ClientDao;
import com.increff.pos.dao.ProductDao;
import com.increff.pos.db.ClientPojo;
import com.increff.pos.db.InventoryPojo;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.helper.InventoryHelper;
import com.increff.pos.helper.ProductHelper;
import com.increff.pos.model.data.InventoryData;
import com.increff.pos.model.data.ProductData;
import com.increff.pos.model.data.TsvUploadResult;
import com.increff.pos.model.form.InventoryForm;
import com.increff.pos.model.form.PageForm;
import com.increff.pos.model.form.ProductForm;
import com.increff.pos.util.ValidationUtil;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductDto {

    private final ProductApi productApi;
    private final InventoryApi inventoryApi;
    private final ClientDao clientDao;
    private final ProductDao productDao;

    public ProductDto(ProductApi productApi, InventoryApi inventoryApi, ClientDao clientDao, ProductDao productDao) {
        this.productApi = productApi;
        this.inventoryApi = inventoryApi;
        this.clientDao = clientDao;
        this.productDao = productDao;
    }

    public ProductData create(ProductForm form) throws ApiException {
        validateProductForm(form);
        ProductPojo pojo = ProductHelper.convertToEntity(form);
        ProductPojo saved = productApi.add(pojo);
        return ProductHelper.convertToDto(saved);
    }

    public ProductData getById(String id) throws ApiException {
        ProductPojo pojo = productApi.get(id);
        InventoryPojo inventory = inventoryApi.getByProductId(id);
        ClientPojo client = clientDao.findByClientId(pojo.getClientId());
        return ProductHelper.convertToDto(pojo, client != null ? client.getName() : null, 
                inventory != null ? inventory.getQuantity() : 0);
    }

    public ProductData getByBarcode(String barcode) throws ApiException {
        ProductPojo pojo = productApi.getByBarcode(barcode);
        InventoryPojo inventory = inventoryApi.getByProductId(pojo.getId());
        ClientPojo client = clientDao.findByClientId(pojo.getClientId());
        return ProductHelper.convertToDto(pojo, client != null ? client.getName() : null, 
                inventory != null ? inventory.getQuantity() : 0);
    }

    public Page<ProductData> getAll(PageForm form) throws ApiException {
        ValidationUtil.validatePageForm(form);
        Page<ProductPojo> pojoPage = productApi.getAll(form.getPage(), form.getSize());
        
        return pojoPage.map(pojo -> {
            try {
                InventoryPojo inventory = inventoryApi.getByProductId(pojo.getId());
                ClientPojo client = clientDao.findByClientId(pojo.getClientId());
                return ProductHelper.convertToDto(pojo, 
                        client != null ? client.getName() : null, 
                        inventory != null ? inventory.getQuantity() : 0);
            } catch (ApiException e) {
                ClientPojo client = clientDao.findByClientId(pojo.getClientId());
                return ProductHelper.convertToDto(pojo, 
                        client != null ? client.getName() : null, 0);
            }
        });
    }

    public ProductData update(String id, ProductForm form) throws ApiException {
        validateProductForm(form);
        ProductPojo pojo = ProductHelper.convertToEntity(form);
        ProductPojo updated = productApi.update(id, pojo);
        InventoryPojo inventory = null;
        try {
            inventory = inventoryApi.getByProductId(id);
        } catch (ApiException e) {
            // Inventory doesn't exist, use 0
        }
        ClientPojo client = clientDao.findByClientId(updated.getClientId());
        return ProductHelper.convertToDto(updated, client != null ? client.getName() : null, 
                inventory != null ? inventory.getQuantity() : 0);
    }

    public InventoryData updateInventory(String productId, InventoryForm form) throws ApiException {
        validateInventoryForm(form);
        InventoryPojo updated = inventoryApi.updateByProductId(productId, form.getQuantity());
        ProductPojo product = productApi.get(productId);
        return InventoryHelper.convertToDto(updated, product.getBarcode());
    }

    public List<ProductData> uploadProductsTsv(String base64Content) throws ApiException {
        // Decode base64 content
        String tsvContent;
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(base64Content);
            tsvContent = new String(decodedBytes);
        } catch (IllegalArgumentException e) {
            throw new ApiException("Invalid base64 encoded content");
        }

        // Parse TSV - handle different line endings
        String[] lines = tsvContent.split("\\r?\\n");
        if (lines.length > 5000) {
            throw new ApiException("Cannot upload more than 5000 rows");
        }

        List<ProductPojo> productPojos = new ArrayList<>();
        int startIndex = 0;
        
        // Skip header row if present (check if first line looks like a header)
        if (lines.length > 0 && lines[0].trim().toLowerCase().matches(".*\\b(barcode|clientid|name|mrp|imageurl)\\b.*")) {
            startIndex = 1;
        }
        
        for (int i = startIndex; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) {
                continue;
            }

            String[] columns = line.split("\t");
            if (columns.length < 4) {
                throw new ApiException("Row " + (i + 1) + ": Invalid format. Expected: barcode, clientId, name, mrp, [imageUrl]");
            }

            ProductPojo pojo = new ProductPojo();
            pojo.setBarcode(columns[0].trim());
            pojo.setClientId(columns[1].trim());
            pojo.setName(columns[2].trim());
            try {
                pojo.setMrp(Double.parseDouble(columns[3].trim()));
            } catch (NumberFormatException e) {
                throw new ApiException("Row " + (i + 1) + ": Invalid MRP format");
            }
            if (columns.length > 4) {
                pojo.setImageUrl(columns[4].trim());
            }
            productPojos.add(pojo);
        }

        List<ProductPojo> saved = productApi.addBulk(productPojos);
        return ProductHelper.convertToDataList(saved);
    }

    /**
     * Upload products TSV and return results with status for each row
     */
    public String uploadProductsTsvWithResults(String base64Content) throws ApiException {
        // Decode base64 content
        String tsvContent;
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(base64Content);
            tsvContent = new String(decodedBytes);
        } catch (IllegalArgumentException e) {
            throw new ApiException("Invalid base64 encoded content");
        }

        // Parse TSV - handle different line endings
        String[] lines = tsvContent.split("\\r?\\n");
        if (lines.length > 5000) {
            throw new ApiException("Cannot upload more than 5000 rows");
        }

        List<TsvUploadResult> results = new ArrayList<>();
        int startIndex = 0;
        
        // Skip header row if present
        if (lines.length > 0 && lines[0].trim().toLowerCase().matches(".*\\b(barcode|clientid|name|mrp|imageurl)\\b.*")) {
            startIndex = 1;
        }
        
        int rowNumber = 1;
        for (int i = startIndex; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) {
                continue;
            }

            String originalLine = line;
            String[] columns = line.split("\t");
            
            if (columns.length < 4) {
                results.add(new TsvUploadResult(rowNumber, "FAILED", 
                    "Invalid format. Expected: barcode, clientId, name, mrp, [imageUrl]", originalLine));
                rowNumber++;
                continue;
            }

            try {
                ProductPojo pojo = new ProductPojo();
                pojo.setBarcode(columns[0].trim());
                pojo.setClientId(columns[1].trim());
                pojo.setName(columns[2].trim());
                
                try {
                    pojo.setMrp(Double.parseDouble(columns[3].trim()));
                } catch (NumberFormatException e) {
                    results.add(new TsvUploadResult(rowNumber, "FAILED", 
                        "Invalid MRP format: " + columns[3], originalLine));
                    rowNumber++;
                    continue;
                }
                
                if (columns.length > 4) {
                    pojo.setImageUrl(columns[4].trim());
                }

                // Try to add the product
                try {
                    // Check if product already exists
                    ProductPojo existing = productDao.findByBarcode(pojo.getBarcode().trim().toLowerCase());
                    if (existing != null) {
                        results.add(new TsvUploadResult(rowNumber, "SKIPPED", 
                            "Product with barcode " + pojo.getBarcode() + " already exists", originalLine));
                    } else {
                        ProductPojo saved = productApi.add(pojo);
                        results.add(new TsvUploadResult(rowNumber, "SUCCESS", 
                            "Product created successfully with barcode: " + saved.getBarcode(), originalLine));
                    }
                } catch (ApiException e) {
                    results.add(new TsvUploadResult(rowNumber, "FAILED", e.getMessage(), originalLine));
                }
            } catch (Exception e) {
                results.add(new TsvUploadResult(rowNumber, "FAILED", 
                    "Unexpected error: " + e.getMessage(), originalLine));
            }
            
            rowNumber++;
        }

        // Convert results to TSV format
        StringBuilder tsvResult = new StringBuilder();
        tsvResult.append("Row Number\tStatus\tError Message\tOriginal Data\n");
        
        for (TsvUploadResult result : results) {
            tsvResult.append(result.getRowNumber())
                    .append("\t")
                    .append(result.getStatus())
                    .append("\t")
                    .append(result.getErrorMessage() != null ? result.getErrorMessage().replace("\t", " ") : "")
                    .append("\t")
                    .append(result.getData())
                    .append("\n");
        }

        // Encode to base64
        return Base64.getEncoder().encodeToString(tsvResult.toString().getBytes());
    }

    public List<InventoryData> uploadInventoryTsv(String base64Content) throws ApiException {
        // Decode base64 content
        String tsvContent;
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(base64Content);
            tsvContent = new String(decodedBytes);
        } catch (IllegalArgumentException e) {
            throw new ApiException("Invalid base64 encoded content");
        }

        // Parse TSV - handle different line endings
        String[] lines = tsvContent.split("\\r?\\n");
        if (lines.length > 5000) {
            throw new ApiException("Cannot upload more than 5000 rows");
        }

        List<InventoryPojo> inventoryPojos = new ArrayList<>();
        int startIndex = 0;
        
        // Skip header row if present (check if first line looks like a header)
        if (lines.length > 0 && lines[0].trim().toLowerCase().matches(".*\\b(barcode|quantity)\\b.*")) {
            startIndex = 1;
        }
        
        for (int i = startIndex; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) {
                continue;
            }

            String[] columns = line.split("\t");
            if (columns.length < 2) {
                throw new ApiException("Row " + (i + 1) + ": Invalid format. Expected: barcode, quantity");
            }

            // Find product by barcode
            ProductPojo product = productApi.getByBarcode(columns[0].trim().toLowerCase());
            if (product == null) {
                throw new ApiException("Row " + (i + 1) + ": Product with barcode " + columns[0] + " not found");
            }

            InventoryPojo pojo = new InventoryPojo();
            pojo.setProductId(product.getId());
            try {
                pojo.setQuantity(Integer.parseInt(columns[1].trim()));
            } catch (NumberFormatException e) {
                throw new ApiException("Row " + (i + 1) + ": Invalid quantity format");
            }
            inventoryPojos.add(pojo);
        }

        List<InventoryPojo> saved = inventoryApi.updateBulk(inventoryPojos);
        return saved.stream()
                .map(inv -> {
                    try {
                        ProductPojo product = productApi.get(inv.getProductId());
                        return InventoryHelper.convertToDto(inv, product.getBarcode());
                    } catch (ApiException e) {
                        return InventoryHelper.convertToDto(inv);
                    }
                })
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Upload inventory TSV and return results with status for each row
     */
    public String uploadInventoryTsvWithResults(String base64Content) throws ApiException {
        // Decode base64 content
        String tsvContent;
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(base64Content);
            tsvContent = new String(decodedBytes);
        } catch (IllegalArgumentException e) {
            throw new ApiException("Invalid base64 encoded content");
        }

        // Parse TSV - handle different line endings
        String[] lines = tsvContent.split("\\r?\\n");
        if (lines.length > 5000) {
            throw new ApiException("Cannot upload more than 5000 rows");
        }

        List<TsvUploadResult> results = new ArrayList<>();
        int startIndex = 0;
        
        // Skip header row if present
        if (lines.length > 0 && lines[0].trim().toLowerCase().matches(".*\\b(barcode|quantity)\\b.*")) {
            startIndex = 1;
        }
        
        int rowNumber = 1;
        for (int i = startIndex; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) {
                continue;
            }

            String originalLine = line;
            String[] columns = line.split("\t");
            
            if (columns.length < 2) {
                results.add(new TsvUploadResult(rowNumber, "FAILED", 
                    "Invalid format. Expected: barcode, quantity", originalLine));
                rowNumber++;
                continue;
            }

            try {
                // Find product by barcode
                ProductPojo product = null;
                try {
                    product = productApi.getByBarcode(columns[0].trim().toLowerCase());
                } catch (ApiException e) {
                    results.add(new TsvUploadResult(rowNumber, "FAILED", 
                        "Product with barcode " + columns[0] + " not found", originalLine));
                    rowNumber++;
                    continue;
                }

                Integer quantity;
                try {
                    quantity = Integer.parseInt(columns[1].trim());
                } catch (NumberFormatException e) {
                    results.add(new TsvUploadResult(rowNumber, "FAILED", 
                        "Invalid quantity format: " + columns[1], originalLine));
                    rowNumber++;
                    continue;
                }

                // Try to update inventory
                try {
                    InventoryPojo updated = inventoryApi.updateByProductId(product.getId(), quantity);
                    results.add(new TsvUploadResult(rowNumber, "SUCCESS", 
                        "Inventory updated successfully. Quantity: " + updated.getQuantity(), originalLine));
                } catch (ApiException e) {
                    results.add(new TsvUploadResult(rowNumber, "FAILED", e.getMessage(), originalLine));
                }
            } catch (Exception e) {
                results.add(new TsvUploadResult(rowNumber, "FAILED", 
                    "Unexpected error: " + e.getMessage(), originalLine));
            }
            
            rowNumber++;
        }

        // Convert results to TSV format
        StringBuilder tsvResult = new StringBuilder();
        tsvResult.append("Row Number\tStatus\tError Message\tOriginal Data\n");
        
        for (TsvUploadResult result : results) {
            tsvResult.append(result.getRowNumber())
                    .append("\t")
                    .append(result.getStatus())
                    .append("\t")
                    .append(result.getErrorMessage() != null ? result.getErrorMessage().replace("\t", " ") : "")
                    .append("\t")
                    .append(result.getData())
                    .append("\n");
        }

        // Encode to base64
        return Base64.getEncoder().encodeToString(tsvResult.toString().getBytes());
    }

    private void validateProductForm(ProductForm form) throws ApiException {
        if (form.getBarcode() == null || form.getBarcode().trim().isEmpty()) {
            throw new ApiException("Barcode cannot be empty");
        }
        if (form.getClientId() == null || form.getClientId().trim().isEmpty()) {
            throw new ApiException("Client ID cannot be empty");
        }
        if (form.getName() == null || form.getName().trim().isEmpty()) {
            throw new ApiException("Product name cannot be empty");
        }
        if (form.getMrp() == null || form.getMrp() < 0) {
            throw new ApiException("MRP must be a positive number");
        }
    }

    private void validateInventoryForm(InventoryForm form) throws ApiException {
        if (form.getProductId() == null || form.getProductId().trim().isEmpty()) {
            throw new ApiException("Product ID cannot be empty");
        }
        if (form.getQuantity() == null || form.getQuantity() < 0) {
            throw new ApiException("Quantity must be a non-negative number");
        }
    }
}
