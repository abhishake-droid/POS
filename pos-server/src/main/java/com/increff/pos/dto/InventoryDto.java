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

        if (!InventoryHelper.isHeader(lines[0])) {
            throw new ApiException(
                    "Invalid TSV format: Missing required header row. " +
                            "First line must contain: barcode, quantity (in any order, tab-separated)");
        }

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

        for (Map.Entry<String, Integer> entry : quantityByProductId.entrySet()) {
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
