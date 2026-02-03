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
        ValidationUtil.validate(form);
        InventoryPojo pojo = inventoryFlow.updateInventory(productId, form.getQuantity());
        return toDataWithRelations(pojo);
    }

    private InventoryData toDataWithRelations(InventoryPojo pojo) throws ApiException {
        ProductPojo product = inventoryFlow.getProductById(pojo.getProductId());
        return InventoryHelper.convertToData(pojo, product.getBarcode());
    }

    public String uploadInventoryWithResults(String base64Content) throws ApiException {
        String content = TsvUtil.decode(base64Content);
        String[] lines = TsvUtil.splitLines(content);
        List<TsvUploadResult> results = new ArrayList<>();
        List<InventoryPojo> validInventories = new ArrayList<>();
        int startIndex = InventoryHelper.isHeader(lines[0]) ? 1 : 0;

        // Map to aggregate quantities by productId
        java.util.Map<String, Integer> quantityByProductId = new java.util.HashMap<>();

        for (int i = startIndex; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty())
                continue;

            try {
                InventoryPojo pojo = InventoryHelper.parseInventory(line, i + 1, productFlow);
                String productId = pojo.getProductId();

                // Aggregate quantities for duplicate barcodes
                quantityByProductId.merge(productId, pojo.getQuantity(), Integer::sum);

                results.add(new TsvUploadResult(i + 1, "SUCCESS", "Inventory updated", line));
            } catch (ApiException e) {
                results.add(new TsvUploadResult(i + 1, "FAILED", e.getMessage(), line));
            }
        }

        // Convert aggregated map to list of InventoryPojo
        for (java.util.Map.Entry<String, Integer> entry : quantityByProductId.entrySet()) {
            InventoryPojo pojo = new InventoryPojo();
            pojo.setProductId(entry.getKey());
            pojo.setQuantity(entry.getValue());
            validInventories.add(pojo);
        }

        if (!validInventories.isEmpty()) {
            inventoryFlow.updateBulk(validInventories);
        }

        return TsvUtil.encode(InventoryHelper.buildResultTsv(results));
    }
}
