package com.increff.pos.controller;

import com.increff.pos.dto.InventoryDto;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.InventoryData;
import com.increff.pos.model.form.InventoryForm;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryDto inventoryDto;

    public InventoryController(InventoryDto inventoryDto) {
        this.inventoryDto = inventoryDto;
    }

    @PutMapping("/{productId}")
    public InventoryData updateInventory(@PathVariable String productId, @RequestBody InventoryForm form)
            throws ApiException {
        return inventoryDto.updateInventory(productId, form);
    }

    @PostMapping("/upload-with-results")
    public String uploadInventoryWithResults(@RequestBody String base64Content) throws ApiException {
        return inventoryDto.uploadInventoryWithResults(base64Content);
    }
}
