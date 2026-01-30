package com.increff.pos.dto;

import com.increff.pos.db.InventoryPojo;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.flow.InventoryFlow;
import com.increff.pos.flow.ProductFlow;
import com.increff.pos.model.data.InventoryData;
import com.increff.pos.model.data.TsvUploadResult;
import com.increff.pos.model.form.InventoryForm;
import com.increff.pos.util.TsvUtil;
import com.increff.pos.util.ValidationUtil;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class InventoryDto {

    private final InventoryFlow inventoryFlow;
    private final ProductFlow productFlow;

    public InventoryDto(InventoryFlow inventoryFlow, ProductFlow productFlow) {
        this.inventoryFlow = inventoryFlow;
        this.productFlow = productFlow;
    }

    public InventoryData updateInventory(String productId, InventoryForm form) throws ApiException {
        ValidationUtil.validateInventoryForm(form);
        return inventoryFlow.updateInventory(productId, form.getQuantity());
    }

    public String uploadInventoryWithResults(String base64Content) throws ApiException {
        String content = TsvUtil.decode(base64Content);
        String[] lines = TsvUtil.splitLines(content);
        List<TsvUploadResult> results = new ArrayList<>();
        int startIndex = isHeader(lines[0]) ? 1 : 0;

        for (int i = startIndex; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty())
                continue;
            results.add(processInventoryRow(line, i + 1));
        }

        return TsvUtil.encode(buildResultTsv(results));
    }

    private InventoryPojo parseInventory(String line, int rowNum) throws ApiException {
        String[] columns = line.split("\t");
        if (columns.length < 2) {
            throw new ApiException("Row " + rowNum + ": Invalid format. Expected: barcode, quantity");
        }
        ProductPojo product = productFlow.getByBarcodeAsPojo(columns[0].trim().toLowerCase());
        InventoryPojo pojo = new InventoryPojo();
        pojo.setProductId(product.getId());
        try {
            Integer quantity = Integer.parseInt(columns[1].trim());
            if (quantity < 0) {
                throw new ApiException("Row " + rowNum + ": Quantity cannot be negative");
            }
            pojo.setQuantity(quantity);
        } catch (NumberFormatException e) {
            throw new ApiException("Row " + rowNum + ": Invalid quantity");
        }
        return pojo;
    }

    private TsvUploadResult processInventoryRow(String line, int rowNum) {
        try {
            InventoryPojo pojo = parseInventory(line, rowNum);
            inventoryFlow.updateByProductId(pojo.getProductId(), pojo.getQuantity());
            return new TsvUploadResult(rowNum, "SUCCESS", "Inventory updated", line);
        } catch (ApiException e) {
            return new TsvUploadResult(rowNum, "FAILED", e.getMessage(), line);
        }
    }

    private boolean isHeader(String firstLine) {
        String lower = firstLine.toLowerCase();
        return lower.contains("barcode") || lower.contains("quantity");
    }

    private String buildResultTsv(List<TsvUploadResult> results) {
        StringBuilder sb = new StringBuilder("Row Number\tStatus\tError Message\tOriginal Data\n");
        for (TsvUploadResult res : results) {
            sb.append(res.getRowNumber()).append("\t")
                    .append(res.getStatus()).append("\t")
                    .append(res.getErrorMessage() != null ? res.getErrorMessage().replace("\t", " ") : "").append("\t")
                    .append(res.getData()).append("\n");
        }
        return sb.toString();
    }
}
