package com.increff.pos.controller;

import com.increff.pos.dto.ProductDto;
import com.increff.pos.model.data.InventoryData;
import com.increff.pos.model.data.ProductData;
import com.increff.pos.model.form.InventoryForm;
import com.increff.pos.model.form.PageForm;
import com.increff.pos.model.form.ProductForm;
import com.increff.pos.model.form.TsvUploadForm;
import com.increff.pos.exception.ApiException;
import com.increff.pos.util.AuthUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@Tag(name = "Product Management", description = "APIs for managing products and inventory")
@RestController
@RequestMapping("/api/product")
public class ProductController {

    private final ProductDto productDto;

    @Autowired
    private AuthUtil authUtil;

    public ProductController(ProductDto productDto) {
        this.productDto = productDto;
    }

    @Operation(summary = "Create a new product")
    @PostMapping("/add")
    public ProductData create(@RequestBody ProductForm form) throws ApiException {
        return productDto.create(form);
    }

    @Operation(summary = "Get all products with pagination")
    @PostMapping("/get-all-paginated")
    public Page<ProductData> getAll(@RequestBody PageForm form) throws ApiException {
        return productDto.getAll(form);
    }

    @Operation(summary = "Get product by ID")
    @GetMapping("/get-by-id/{id}")
    public ProductData getById(@PathVariable String id) throws ApiException {
        return productDto.getById(id);
    }

    @Operation(summary = "Get product by barcode")
    @GetMapping("/get-by-barcode/{barcode}")
    public ProductData getByBarcode(@PathVariable String barcode) throws ApiException {
        return productDto.getByBarcode(barcode);
    }

    @Operation(summary = "Update product")
    @PutMapping("/update/{id}")
    public ProductData update(@PathVariable String id, @RequestBody ProductForm form, HttpServletRequest request) throws ApiException {
        if (!authUtil.isSupervisor(request)) {
            throw new ApiException("Only supervisors can update products");
        }
        return productDto.update(id, form);
    }

    @Operation(summary = "Update inventory for a product")
    @PutMapping("/update-inventory/{productId}")
    public InventoryData updateInventory(@PathVariable String productId, @RequestBody InventoryForm form, HttpServletRequest request) throws ApiException {
        if (!authUtil.isSupervisor(request)) {
            throw new ApiException("Only supervisors can update inventory");
        }
        return productDto.updateInventory(productId, form);
    }

    @Operation(summary = "Upload products via TSV file (base64 encoded)")
    @PostMapping("/upload-products-tsv")
    public List<ProductData> uploadProductsTsv(@RequestBody TsvUploadForm form, HttpServletRequest request) throws ApiException {
        if (!authUtil.isSupervisor(request)) {
            throw new ApiException("Only supervisors can upload products");
        }
        return productDto.uploadProductsTsv(form.getFileContent());
    }

    @Operation(summary = "Upload inventory via TSV file (base64 encoded)")
    @PostMapping("/upload-inventory-tsv")
    public List<InventoryData> uploadInventoryTsv(@RequestBody TsvUploadForm form, HttpServletRequest request) throws ApiException {
        if (!authUtil.isSupervisor(request)) {
            throw new ApiException("Only supervisors can upload inventory");
        }
        return productDto.uploadInventoryTsv(form.getFileContent());
    }

    @Operation(summary = "Upload products via TSV file and get results (base64 encoded)")
    @PostMapping("/upload-products-tsv-with-results")
    public java.util.Map<String, String> uploadProductsTsvWithResults(@RequestBody TsvUploadForm form, HttpServletRequest request) throws ApiException {
        if (!authUtil.isSupervisor(request)) {
            throw new ApiException("Only supervisors can upload products");
        }
        String resultTsv = productDto.uploadProductsTsvWithResults(form.getFileContent());
        java.util.Map<String, String> response = new java.util.HashMap<>();
        response.put("resultTsv", resultTsv);
        return response;
    }

    @Operation(summary = "Upload inventory via TSV file and get results (base64 encoded)")
    @PostMapping("/upload-inventory-tsv-with-results")
    public java.util.Map<String, String> uploadInventoryTsvWithResults(@RequestBody TsvUploadForm form, HttpServletRequest request) throws ApiException {
        if (!authUtil.isSupervisor(request)) {
            throw new ApiException("Only supervisors can upload inventory");
        }
        String resultTsv = productDto.uploadInventoryTsvWithResults(form.getFileContent());
        java.util.Map<String, String> response = new java.util.HashMap<>();
        response.put("resultTsv", resultTsv);
        return response;
    }
}
