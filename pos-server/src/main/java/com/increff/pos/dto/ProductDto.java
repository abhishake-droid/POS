package com.increff.pos.dto;

import com.increff.pos.db.ClientPojo;
import com.increff.pos.db.InventoryPojo;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.flow.ProductFlow;
import com.increff.pos.exception.ApiException;
import com.increff.pos.helper.ProductHelper;
import com.increff.pos.model.data.ProductData;
import com.increff.pos.model.data.TsvUploadResult;
import com.increff.pos.model.form.PageForm;
import com.increff.pos.model.form.ProductForm;
import com.increff.pos.util.TsvUtil;
import com.increff.pos.util.ValidationUtil;
import com.increff.pos.util.NormalizeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;

import java.util.List;
import java.util.Map;
import java.util.Set;

//Todo proper how parsing works how the error is thrown

@Service
public class ProductDto {

    @Autowired
    private ProductFlow productFlow;

    public ProductData create(ProductForm form) throws ApiException {
        ValidationUtil.validate(form);
        NormalizeUtil.normalizeProductForm(form);
        ProductPojo pojo = ProductHelper.convertToEntity(form);
        ProductPojo saved = productFlow.create(pojo);
        return toDataWithRelations(saved);
    }

    public ProductData getById(String id) throws ApiException {
        ProductPojo product = productFlow.getById(id);
        return toDataWithRelations(product);
    }

    public ProductData getByBarcode(String barcode) throws ApiException {
        if (barcode == null || barcode.trim().isEmpty()) {
            throw new ApiException("Barcode cannot be empty");
        }
        String normalizedBarcode = NormalizeUtil.normalizeBarcode(barcode);
        ProductPojo product = productFlow.getByBarcode(normalizedBarcode);
        return toDataWithRelations(product);
    }

    public Page<ProductData> getAll(PageForm form) throws ApiException {
        ValidationUtil.validate(form);
        Page<ProductPojo> pojoPage = productFlow.getAll(form);
        return pojoPage.map(product -> {
            try {
                return toDataWithRelations(product);
            } catch (ApiException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public ProductData update(String id, ProductForm form) throws ApiException {
        ValidationUtil.validate(form);
        NormalizeUtil.normalizeProductForm(form);
        ProductPojo pojo = ProductHelper.convertToEntity(form);
        ProductPojo updated = productFlow.update(id, pojo);
        return toDataWithRelations(updated);
    }

    private ProductData toDataWithRelations(ProductPojo product) throws ApiException {
        InventoryPojo inventory = productFlow.getInventoryByProductId(product.getId());
        ClientPojo client = productFlow.getClientById(product.getClientId());
        return ProductHelper.convertToData(product, client.getName(), inventory.getQuantity());
    }

    public String uploadProductsWithResults(String base64Content) throws ApiException {
        String content = TsvUtil.decode(base64Content);
        String[] lines = TsvUtil.splitLines(content);
        List<TsvUploadResult> results = new ArrayList<>();
        List<ProductPojo> validProducts = new ArrayList<>();
        List<String> validBarcodes = new ArrayList<>();
        List<Integer> validProductRows = new ArrayList<>();

        if (!ProductHelper.isHeader(lines[0])) {
            throw new ApiException(
                    "Invalid TSV format: Missing required header row. " +
                            "First line must contain: barcode, clientid, name, mrp (in any order, tab-separated)");
        }

        Map<String, Integer> columnMap = ProductHelper.parseHeader(lines[0]);

        // First pass
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty())
                continue;

            int rowNum = i + 1;

            try {
                ProductPojo pojo = ProductHelper.parseProduct(line, rowNum, columnMap);
                validProducts.add(pojo);
                validBarcodes.add(pojo.getBarcode());
                validProductRows.add(rowNum);
            } catch (ApiException e) {
                results.add(new TsvUploadResult(rowNum, "FAILED", e.getMessage(), line));
            }
        }

        // Second pass
        List<String> existingBarcodes = productFlow.getExistingBarcodes(validBarcodes);
        Set<String> existingBarcodesSet = new HashSet<>(existingBarcodes);

        // Third pass
        List<ProductPojo> productsToInsert = new ArrayList<>();
        List<Integer> rowsToInsert = new ArrayList<>();

        for (int i = 0; i < validProducts.size(); i++) {
            ProductPojo pojo = validProducts.get(i);
            int rowNum = validProductRows.get(i);
            String line = lines[rowNum - 1];

            if (existingBarcodesSet.contains(pojo.getBarcode())) {
                results.add(new TsvUploadResult(rowNum, "SKIPPED", "Product already exists", line));
            } else {
                productsToInsert.add(pojo);
                rowsToInsert.add(rowNum);
            }
        }

        // Fourth pass
        if (!productsToInsert.isEmpty()) {
            try {
                List<ProductPojo> savedProducts = productFlow.addBulk(productsToInsert);
                for (int i = 0; i < savedProducts.size(); i++) {
                    int rowNum = rowsToInsert.get(i);
                    String line = lines[rowNum - 1];
                    results.add(new TsvUploadResult(rowNum, "SUCCESS", "Product created", line));
                }
            } catch (ApiException e) {
                for (int i = 0; i < productsToInsert.size(); i++) {
                    int rowNum = rowsToInsert.get(i);
                    String line = lines[rowNum - 1];
                    results.add(new TsvUploadResult(rowNum, "FAILED", "Bulk insert failed: " + e.getMessage(), line));
                }
            }
        }

        results.sort((a, b) -> Integer.compare(a.getRowNumber(), b.getRowNumber()));

        return TsvUtil.encode(ProductHelper.buildResultTsv(results));
    }
}
