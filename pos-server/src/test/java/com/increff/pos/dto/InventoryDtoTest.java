package com.increff.pos.dto;

import com.increff.pos.db.InventoryPojo;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.flow.InventoryFlow;
import com.increff.pos.flow.ProductFlow;
import com.increff.pos.model.data.InventoryData;
import com.increff.pos.model.form.InventoryForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryDtoTest {

    @Mock
    private InventoryFlow inventoryFlow;

    @Mock
    private ProductFlow productFlow;

    @InjectMocks
    private InventoryDto inventoryDto;

    private InventoryForm validForm;
    private InventoryPojo inventoryPojo;
    private ProductPojo productPojo;

    @BeforeEach
    void setUp() {
        validForm = new InventoryForm();
        validForm.setQuantity(100);

        inventoryPojo = new InventoryPojo();
        inventoryPojo.setId("inv1");
        inventoryPojo.setProductId("prod1");
        inventoryPojo.setQuantity(100);

        productPojo = new ProductPojo();
        productPojo.setId("prod1");
        productPojo.setBarcode("bc123");
    }

    @Test
    void testUpdateInventory_Success() throws ApiException {
        // Given
        when(inventoryFlow.updateInventory("prod1", 100)).thenReturn(inventoryPojo);
        when(inventoryFlow.getProductById("prod1")).thenReturn(productPojo);

        // When
        InventoryData result = inventoryDto.updateInventory("prod1", validForm);

        // Then
        assertNotNull(result);
        assertEquals(100, result.getQuantity());
        assertEquals("bc123", result.getBarcode());
        verify(inventoryFlow, times(1)).updateInventory("prod1", 100);
    }

    @Test
    void testUpdateInventory_NegativeQuantity() {
        // Given
        validForm.setQuantity(-10);

        // When/Then
        assertThrows(ApiException.class, () -> inventoryDto.updateInventory("prod1", validForm));
    }

    @Test
    void testUpdateInventory_NullQuantity() {
        // Given
        validForm.setQuantity(null);

        // When/Then
        assertThrows(ApiException.class, () -> inventoryDto.updateInventory("prod1", validForm));
    }

    @Test
    void testUpdateInventory_ProductNotFound() throws ApiException {
        // Given
        when(inventoryFlow.updateInventory("invalid", 100))
                .thenThrow(new ApiException("Product not found"));

        // When/Then
        assertThrows(ApiException.class, () -> inventoryDto.updateInventory("invalid", validForm));
    }

    @Test
    void testUpdateInventory_ZeroQuantity() throws ApiException {
        // Given
        validForm.setQuantity(0);
        inventoryPojo.setQuantity(0);
        when(inventoryFlow.updateInventory("prod1", 0)).thenReturn(inventoryPojo);
        when(inventoryFlow.getProductById("prod1")).thenReturn(productPojo);

        // When
        InventoryData result = inventoryDto.updateInventory("prod1", validForm);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getQuantity());
    }

    @Test
    void testUpdateInventory_LargeQuantity() throws ApiException {
        // Given
        validForm.setQuantity(999999);
        inventoryPojo.setQuantity(999999);
        when(inventoryFlow.updateInventory("prod1", 999999)).thenReturn(inventoryPojo);
        when(inventoryFlow.getProductById("prod1")).thenReturn(productPojo);

        // When
        InventoryData result = inventoryDto.updateInventory("prod1", validForm);

        // Then
        assertNotNull(result);
        assertEquals(999999, result.getQuantity());
    }

    @Test
    void testUploadInventoryWithResults_Success() throws ApiException {
        // Given
        String tsvContent = "barcode\tquantity\nbc123\t100\nbc456\t200";
        String base64Content = java.util.Base64.getEncoder().encodeToString(tsvContent.getBytes());

        ProductPojo product1 = new ProductPojo();
        product1.setId("prod1");
        product1.setBarcode("bc123");

        ProductPojo product2 = new ProductPojo();
        product2.setId("prod2");
        product2.setBarcode("bc456");

        when(productFlow.getByBarcode("bc123")).thenReturn(product1);
        when(productFlow.getByBarcode("bc456")).thenReturn(product2);

        // When
        String result = inventoryDto.uploadInventoryTsv(base64Content);

        // Then
        assertNotNull(result);
        String decoded = new String(java.util.Base64.getDecoder().decode(result));
        assertTrue(decoded.contains("SUCCESS"));
        verify(inventoryFlow, times(1)).updateBulk(anyList());
    }

    @Test
    void testUploadInventoryWithResults_WithHeader() throws ApiException {
        // Given
        String tsvContent = "barcode\tquantity\nbc123\t50";
        String base64Content = java.util.Base64.getEncoder().encodeToString(tsvContent.getBytes());

        ProductPojo product = new ProductPojo();
        product.setId("prod1");
        product.setBarcode("bc123");

        when(productFlow.getByBarcode("bc123")).thenReturn(product);

        // When
        String result = inventoryDto.uploadInventoryTsv(base64Content);

        // Then
        assertNotNull(result);
        verify(inventoryFlow, times(1)).updateBulk(anyList());
    }

    @Test
    void testUploadInventoryWithResults_WithoutHeader() {
        // Given - TSV without header (should fail now)
        String tsvContent = "bc123\t75";
        String base64Content = java.util.Base64.getEncoder().encodeToString(tsvContent.getBytes());

        // When/Then - Should throw exception because header is mandatory
        ApiException exception = assertThrows(ApiException.class, () -> {
            inventoryDto.uploadInventoryTsv(base64Content);
        });
        assertTrue(exception.getMessage().contains("Missing required header row"));
    }

    @Test
    void testUploadInventoryWithResults_EmptyLines() throws ApiException {
        // Given
        String tsvContent = "barcode\tquantity\n\nbc123\t100\n\n";
        String base64Content = java.util.Base64.getEncoder().encodeToString(tsvContent.getBytes());

        ProductPojo product = new ProductPojo();
        product.setId("prod1");
        product.setBarcode("bc123");

        when(productFlow.getByBarcode("bc123")).thenReturn(product);

        // When
        String result = inventoryDto.uploadInventoryTsv(base64Content);

        // Then
        assertNotNull(result);
        verify(inventoryFlow, times(1)).updateBulk(anyList());
    }

    @Test
    void testUploadInventoryWithResults_DuplicateBarcodes() throws ApiException {
        // Given
        String tsvContent = "barcode\tquantity\nbc123\t100\nbc123\t50\nbc123\t25";
        String base64Content = java.util.Base64.getEncoder().encodeToString(tsvContent.getBytes());

        ProductPojo product = new ProductPojo();
        product.setId("prod1");
        product.setBarcode("bc123");

        when(productFlow.getByBarcode("bc123")).thenReturn(product);

        // When
        String result = inventoryDto.uploadInventoryTsv(base64Content);

        // Then
        assertNotNull(result);
        // Should aggregate: 100 + 50 + 25 = 175
        verify(inventoryFlow, times(1))
                .updateBulk(argThat(list -> list.size() == 1 && list.get(0).getQuantity() == 175));
    }

    @Test
    void testUploadInventoryWithResults_PartialFailure() throws ApiException {
        // Given
        String tsvContent = "barcode\tquantity\nbc123\t100\ninvalid\t200\nbc456\t300";
        String base64Content = java.util.Base64.getEncoder().encodeToString(tsvContent.getBytes());

        ProductPojo product1 = new ProductPojo();
        product1.setId("prod1");
        product1.setBarcode("bc123");

        ProductPojo product2 = new ProductPojo();
        product2.setId("prod2");
        product2.setBarcode("bc456");

        when(productFlow.getByBarcode("bc123")).thenReturn(product1);
        when(productFlow.getByBarcode("invalid")).thenThrow(new ApiException("Product not found"));
        when(productFlow.getByBarcode("bc456")).thenReturn(product2);

        // When
        String result = inventoryDto.uploadInventoryTsv(base64Content);

        // Then
        assertNotNull(result);
        String decoded = new String(java.util.Base64.getDecoder().decode(result));
        assertTrue(decoded.contains("SUCCESS"));
        assertTrue(decoded.contains("FAILED"));
        verify(inventoryFlow, times(1)).updateBulk(anyList());
    }

    @Test
    void testUploadInventoryWithResults_AllFailures() throws ApiException {
        // Given
        String tsvContent = "barcode\tquantity\ninvalid1\t100\ninvalid2\t200";
        String base64Content = java.util.Base64.getEncoder().encodeToString(tsvContent.getBytes());

        when(productFlow.getByBarcode(anyString())).thenThrow(new ApiException("Product not found"));

        // When
        String result = inventoryDto.uploadInventoryTsv(base64Content);

        // Then
        assertNotNull(result);
        String decoded = new String(java.util.Base64.getDecoder().decode(result));
        assertTrue(decoded.contains("FAILED"));
        verify(inventoryFlow, never()).updateBulk(anyList());
    }

    @Test
    void testUploadInventoryWithResults_ZeroQuantity() throws ApiException {
        // Given
        String tsvContent = "barcode\tquantity\nbc123\t0";
        String base64Content = java.util.Base64.getEncoder().encodeToString(tsvContent.getBytes());

        ProductPojo product = new ProductPojo();
        product.setId("prod1");
        product.setBarcode("bc123");

        when(productFlow.getByBarcode("bc123")).thenReturn(product);

        // When
        String result = inventoryDto.uploadInventoryTsv(base64Content);

        // Then
        assertNotNull(result);
        verify(inventoryFlow, times(1)).updateBulk(anyList());
    }

}
