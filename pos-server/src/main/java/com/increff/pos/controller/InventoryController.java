package com.increff.pos.controller;

import com.increff.pos.dto.InventoryDto;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.InventoryData;
import com.increff.pos.model.form.InventoryForm;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @Autowired
    private InventoryDto inventoryDto;

    @PutMapping("/{productId}")
    public InventoryData updateInventory(@PathVariable String productId, @RequestBody InventoryForm form)
            throws ApiException {
        return inventoryDto.updateInventory(productId, form);
    }

    @PostMapping("/upload-inventory-tsv")
    public String uploadInventoryTsv(@RequestBody String base64Content) throws ApiException {
        return inventoryDto.uploadInventoryTsv(base64Content);
    }
}
