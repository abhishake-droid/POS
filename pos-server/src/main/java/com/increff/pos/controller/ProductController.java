package com.increff.pos.controller;

import com.increff.pos.dto.ProductDto;
import com.increff.pos.model.data.ProductData;
import com.increff.pos.model.form.PageForm;
import com.increff.pos.model.form.ProductForm;
import com.increff.pos.exception.ApiException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Product Management", description = "APIs for managing products and inventory")
@RestController
@RequestMapping("/api/product")
public class ProductController {

    private final ProductDto productDto;

    public ProductController(ProductDto productDto) {
        this.productDto = productDto;
    }

    @Operation(summary = "Adds a product")
    @PostMapping("/add")
    public ProductData add(@RequestBody ProductForm form) throws ApiException {
        return productDto.create(form);
    }

    @Operation(summary = "Gets a product by ID")
    @GetMapping("/get-by-id/{id}")
    public ProductData getById(@PathVariable String id) throws ApiException {
        return productDto.getById(id);
    }

    @Operation(summary = "Gets a product by barcode")
    @GetMapping("/get-by-barcode/{barcode}")
    public ProductData getByBarcode(@PathVariable String barcode) throws ApiException {
        return productDto.getByBarcode(barcode);
    }

    @Operation(summary = "Gets all products paginated")
    @PostMapping("/get-all-paginated")
    public Page<ProductData> getAll(@RequestBody PageForm form) throws ApiException {
        return productDto.getAll(form);
    }

    @Operation(summary = "Updates a product")
    @PutMapping("/update/{id}")
    public ProductData update(@PathVariable String id, @RequestBody ProductForm form) throws ApiException {
        return productDto.update(id, form);
    }

    @Operation(summary = "Uploads products via TSV and returns status for each row")
    @PostMapping("/upload-products-with-results")
    public String uploadProductsWithResults(@RequestBody String base64Content) throws ApiException {
        return productDto.uploadProductsWithResults(base64Content);
    }
}
