package com.increff.pos.helper;

import com.increff.pos.db.InventoryPojo;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.flow.ProductFlow;
import com.increff.pos.model.data.InventoryData;
import com.increff.pos.model.data.TsvUploadResult;

import java.util.List;

public class InventoryHelper {

    public static InventoryData convertToData(InventoryPojo pojo, String barcode) {
        InventoryData data = new InventoryData();
        data.setId(pojo.getId());
        data.setProductId(pojo.getProductId());
        data.setBarcode(barcode);
        data.setQuantity(pojo.getQuantity());
        return data;
    }

    // TSV Parsing Helper Methods

    public static InventoryPojo parseInventory(String line, int rowNum, ProductFlow productFlow) throws ApiException {
        String[] columns = line.split("\t");
        if (columns.length < 2) {
            throw new ApiException("Row " + rowNum + ": Invalid format. Expected: barcode, quantity");
        }
        ProductPojo product = productFlow.getByBarcode(columns[0].trim().toLowerCase());
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

    public static boolean isHeader(String firstLine) {
        String lower = firstLine.toLowerCase();
        return lower.contains("barcode") || lower.contains("quantity");
    }

    public static String buildResultTsv(List<TsvUploadResult> results) {
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
