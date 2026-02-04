package com.increff.pos.dto;

import com.increff.pos.db.InventoryPojo;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.flow.InventoryFlow;
import com.increff.pos.flow.ProductFlow;
import com.increff.pos.model.data.InventoryData;
import com.increff.pos.model.data.TsvUploadResult;
import com.increff.pos.model.form.InventoryForm;
import com.increff.pos.helper.InventoryHelper;
import com.increff.pos.util.TsvUtil;
import java.util.Map;
import java.util.HashMap;
import com.increff.pos.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class InventoryDto {

    @Autowired
    private InventoryFlow inventoryFlow;

    @Autowired
    private ProductFlow productFlow;

    public InventoryData updateInventory(String productId, InventoryForm form) throws ApiException {
        productId = com.increff.pos.util.NormalizeUtil.normalizeId(productId);
        ValidationUtil.validate(form);
        InventoryPojo pojo = inventoryFlow.updateInventory(productId, form.getQuantity());
        return toDataWithRelations(pojo);
    }

    private InventoryData toDataWithRelations(InventoryPojo pojo) throws ApiException {
        ProductPojo product = inventoryFlow.getProductById(pojo.getProductId());
        return InventoryHelper.convertToData(pojo, product.getBarcode());
    }

    public String uploadInventoryTsv(String base64Content) throws ApiException {
        String content = TsvUtil.decode(base64Content);
        String[] lines = TsvUtil.splitLines(content);
        List<TsvUploadResult> results = new ArrayList<>();

        validateInventoryHeader(lines);

        Map<String, Integer> quantityByProductId = parseAndAggregateInventory(lines, results);
        List<InventoryPojo> validInventories = createInventoryUpdates(quantityByProductId);
        performBulkInventoryUpdate(validInventories);

        return TsvUtil.encode(InventoryHelper.buildResultTsv(results));
    }

    private void validateInventoryHeader(String[] lines) throws ApiException {
        if (!InventoryHelper.isHeader(lines[0])) {
            throw new ApiException(
                    "Invalid TSV format: Missing required header row. " +
                            "First line must contain: barcode, quantity (in any order, tab-separated)");
        }
    }

    private Map<String, Integer> parseAndAggregateInventory(String[] lines, List<TsvUploadResult> results) {
        Map<String, Integer> quantityByProductId = new HashMap<>();

        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty())
                continue;

            try {
                InventoryPojo pojo = InventoryHelper.parseInventory(line, i + 1, productFlow);
                String productId = pojo.getProductId();
                quantityByProductId.merge(productId, pojo.getQuantity(), Integer::sum);
                results.add(new TsvUploadResult(i + 1, "SUCCESS", "Inventory updated", line));
            } catch (ApiException e) {
                results.add(new TsvUploadResult(i + 1, "FAILED", e.getMessage(), line));
            }
        }

        return quantityByProductId;
    }

    private List<InventoryPojo> createInventoryUpdates(Map<String, Integer> quantityByProductId) {
        List<InventoryPojo> validInventories = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : quantityByProductId.entrySet()) {
            InventoryPojo pojo = InventoryHelper.createInventoryUpdate(entry.getKey(), entry.getValue());
            validInventories.add(pojo);
        }

        return validInventories;
    }

    private void performBulkInventoryUpdate(List<InventoryPojo> inventories) throws ApiException {
        if (!inventories.isEmpty()) {
            inventoryFlow.updateBulk(inventories);
        }
    }
}
