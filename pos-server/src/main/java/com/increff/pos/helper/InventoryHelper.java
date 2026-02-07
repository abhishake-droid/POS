package com.increff.pos.helper;

import com.increff.pos.db.InventoryPojo;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.flow.ProductFlow;
import com.increff.pos.model.data.InventoryData;
import com.increff.pos.model.data.TsvUploadResult;

import java.util.List;
import java.util.Map;

public class InventoryHelper {

    public static InventoryData convertToData(InventoryPojo pojo, String barcode) {
        InventoryData data = new InventoryData();
        data.setId(pojo.getId());
        data.setProductId(pojo.getProductId());
        data.setBarcode(barcode);
        data.setQuantity(pojo.getQuantity());
        return data;
    }

    public static InventoryPojo parseInventory(String line, int rowNum, ProductFlow productFlow) throws ApiException {
        String[] columns = line.split("\t");

        try {
            String barcodeStr = columns[0].trim().toLowerCase();
            String quantityStr = columns[1].trim();

            ProductPojo product = productFlow.getByBarcode(barcodeStr);
            InventoryPojo pojo = new InventoryPojo();
            pojo.setProductId(product.getId());

            try {
                Integer quantity = Integer.parseInt(quantityStr);
                if (quantity < 0) {
                    throw new ApiException("Row " + rowNum + ": Quantity cannot be negative");
                }
                pojo.setQuantity(quantity);
            } catch (NumberFormatException e) {
                throw new ApiException("Row " + rowNum + ": Invalid quantity");
            }

            return pojo;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ApiException("Row " + rowNum + ": Missing required columns. Expected: barcode, quantity");
        }
    }

    public static InventoryPojo parseInventory(String line, int rowNum, Map<String, ProductPojo> productMap)
            throws ApiException {
        String[] columns = line.split("\t");

        try {
            String barcodeStr = columns[0].trim().toLowerCase();
            String quantityStr = columns[1].trim();

            ProductPojo product = productMap.get(barcodeStr);
            if (product == null) {
                throw new ApiException("Row " + rowNum + ": Product with barcode " + barcodeStr + " does not exist");
            }

            InventoryPojo pojo = new InventoryPojo();
            pojo.setProductId(product.getId());

            try {
                Integer quantity = Integer.parseInt(quantityStr);
                if (quantity < 0) {
                    throw new ApiException("Row " + rowNum + ": Quantity cannot be negative");
                }
                pojo.setQuantity(quantity);
            } catch (NumberFormatException e) {
                throw new ApiException("Row " + rowNum + ": Invalid quantity");
            }

            return pojo;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ApiException("Row " + rowNum + ": Missing required columns. Expected: barcode, quantity");
        }
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

    public static InventoryPojo createInventoryUpdate(String productId, Integer quantity) {
        InventoryPojo pojo = new InventoryPojo();
        pojo.setProductId(productId);
        pojo.setQuantity(quantity);
        return pojo;
    }
}
