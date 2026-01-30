package com.increff.pos.dto;

import com.increff.pos.flow.ProductFlow;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.helper.ProductHelper;
import com.increff.pos.model.data.ProductData;
import com.increff.pos.model.data.TsvUploadResult;
import com.increff.pos.model.form.PageForm;
import com.increff.pos.model.form.ProductForm;
import com.increff.pos.util.TsvUtil;
import com.increff.pos.util.ValidationUtil;
import com.increff.pos.util.NormalizeUtil;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductDto {

    private final ProductFlow productFlow;

    public ProductDto(ProductFlow productFlow) {
        this.productFlow = productFlow;
    }

    public ProductData create(ProductForm form) throws ApiException {
        ValidationUtil.validateProductForm(form);
        NormalizeUtil.normalizeProductForm(form);
        ProductPojo pojo = ProductHelper.convertToEntity(form);
        return productFlow.create(pojo);
    }

    public ProductData getById(String id) throws ApiException {
        return productFlow.getById(id);
    }

    public ProductData getByBarcode(String barcode) throws ApiException {
        return productFlow.getByBarcode(barcode);
    }

    public Page<ProductData> getAll(PageForm form) throws ApiException {
        ValidationUtil.validatePageForm(form);
        return productFlow.getAll(form);
    }

    public ProductData update(String id, ProductForm form) throws ApiException {
        ValidationUtil.validateProductForm(form);
        NormalizeUtil.normalizeProductForm(form);
        ProductPojo pojo = ProductHelper.convertToEntity(form);
        return productFlow.update(id, pojo);
    }

    public String uploadProductsWithResults(String base64Content) throws ApiException {
        String content = TsvUtil.decode(base64Content);
        String[] lines = TsvUtil.splitLines(content);
        List<TsvUploadResult> results = new ArrayList<>();
        int startIndex = isHeader(lines[0]) ? 1 : 0;

        for (int i = startIndex; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty())
                continue;
            results.add(processProductRow(line, i + 1));
        }

        return TsvUtil.encode(buildResultTsv(results));
    }

    private ProductPojo parseProduct(String line, int rowNum) throws ApiException {
        String[] columns = line.split("\t");
        if (columns.length < 4) {
            throw new ApiException(
                    "Row " + rowNum + ": Invalid format. Expected: barcode, clientId, name, mrp, [imageUrl]");
        }
        ProductPojo pojo = new ProductPojo();
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
        return pojo;
    }

    private TsvUploadResult processProductRow(String line, int rowNum) {
        try {
            ProductPojo pojo = parseProduct(line, rowNum);
            try {
                productFlow.getByBarcodeAsPojo(pojo.getBarcode());
                return new TsvUploadResult(rowNum, "SKIPPED", "Product already exists", line);
            } catch (ApiException e) {
                productFlow.add(pojo);
                return new TsvUploadResult(rowNum, "SUCCESS", "Product created", line);
            }
        } catch (ApiException e) {
            return new TsvUploadResult(rowNum, "FAILED", e.getMessage(), line);
        }
    }

    private boolean isHeader(String firstLine) {
        String lower = firstLine.toLowerCase();
        return lower.contains("barcode") || lower.contains("clientid") || lower.contains("name")
                || lower.contains("mrp");
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
