package com.increff.pos.helper;

import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.ProductData;
import com.increff.pos.model.data.TsvUploadResult;
import com.increff.pos.model.form.ProductForm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProductHelper {

    public static ProductPojo convertToEntity(ProductForm form) {
        ProductPojo pojo = new ProductPojo();
        pojo.setBarcode(form.getBarcode());
        pojo.setClientId(form.getClientId());
        pojo.setName(form.getName());
        pojo.setMrp(form.getMrp());
        pojo.setImageUrl(form.getImageUrl());
        return pojo;
    }

    public static ProductData convertToData(ProductPojo pojo, String clientName, Integer quantity) {
        ProductData data = new ProductData();
        data.setId(pojo.getId());
        data.setBarcode(pojo.getBarcode());
        data.setClientId(pojo.getClientId());
        data.setClientName(clientName);
        data.setName(pojo.getName());
        data.setMrp(pojo.getMrp());
        data.setImageUrl(pojo.getImageUrl());
        data.setQuantity(quantity);
        return data;
    }

    // TSV Parsing Helper Methods

    public static Map<String, Integer> parseHeader(String headerLine) throws ApiException {
        String[] headers = headerLine.toLowerCase().split("\t");
        Map<String, Integer> columnMap = new HashMap<>();

        for (int i = 0; i < headers.length; i++) {
            String header = headers[i].trim();
            columnMap.put(header, i);
        }

        // Validate required columns are present
        String[] requiredColumns = { "barcode", "clientid", "name", "mrp" };
        for (String required : requiredColumns) {
            if (!columnMap.containsKey(required)) {
                throw new ApiException("Missing required column: " + required);
            }
        }

        return columnMap;
    }

    public static ProductPojo parseProduct(String line, int rowNum, Map<String, Integer> columnMap)
            throws ApiException {
        String[] columns = line.split("\t");

        ProductPojo pojo = new ProductPojo();

        // If no header mapping, use default column order
        if (columnMap == null) {
            if (columns.length < 4) {
                throw new ApiException(
                        "Row " + rowNum + ": Invalid format. Expected: barcode, clientId, name, mrp, [imageUrl]");
            }
            pojo.setBarcode(columns[0].trim().toLowerCase());
            pojo.setClientId(columns[1].trim());
            pojo.setName(columns[2].trim().toLowerCase());
            try {
                pojo.setMrp(Double.parseDouble(columns[3].trim()));
            } catch (NumberFormatException e) {
                throw new ApiException("Row " + rowNum + ": Invalid MRP");
            }
            if (columns.length > 4)
                pojo.setImageUrl(columns[4].trim());
        } else {
            // Use header mapping for flexible column order
            try {
                int barcodeIdx = columnMap.get("barcode");
                int clientIdIdx = columnMap.get("clientid");
                int nameIdx = columnMap.get("name");
                int mrpIdx = columnMap.get("mrp");

                if (barcodeIdx >= columns.length || clientIdIdx >= columns.length ||
                        nameIdx >= columns.length || mrpIdx >= columns.length) {
                    throw new ApiException("Row " + rowNum + ": Missing required columns");
                }

                pojo.setBarcode(columns[barcodeIdx].trim().toLowerCase());
                pojo.setClientId(columns[clientIdIdx].trim());
                pojo.setName(columns[nameIdx].trim().toLowerCase());

                try {
                    pojo.setMrp(Double.parseDouble(columns[mrpIdx].trim()));
                } catch (NumberFormatException e) {
                    throw new ApiException("Row " + rowNum + ": Invalid MRP");
                }

                // Optional imageUrl column
                if (columnMap.containsKey("imageurl")) {
                    int imageUrlIdx = columnMap.get("imageurl");
                    if (imageUrlIdx < columns.length) {
                        pojo.setImageUrl(columns[imageUrlIdx].trim());
                    }
                }
            } catch (NullPointerException e) {
                throw new ApiException("Row " + rowNum + ": Missing required column data");
            }
        }

        return pojo;
    }

    public static boolean isHeader(String firstLine) {
        String[] columns = firstLine.toLowerCase().split("\t");
        Set<String> columnSet = new HashSet<>();

        // Trim and add each column to set
        for (String col : columns) {
            columnSet.add(col.trim());
        }

        // Must have all 4 required columns as separate tab-delimited values
        return columnSet.contains("barcode") && columnSet.contains("clientid") &&
                columnSet.contains("name") && columnSet.contains("mrp");
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
