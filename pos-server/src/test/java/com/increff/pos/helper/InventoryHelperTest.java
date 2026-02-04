package com.increff.pos.helper;

import com.increff.pos.db.InventoryPojo;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.flow.ProductFlow;
import com.increff.pos.model.data.InventoryData;
import com.increff.pos.model.data.TsvUploadResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryHelperTest {

    @Mock
    private ProductFlow productFlow;

    @Test
    void testConvertToDto_WithBarcode() {
        // Given
        InventoryPojo pojo = new InventoryPojo();
        pojo.setId("inv456");
        pojo.setProductId("prod456");
        pojo.setQuantity(50);

        String barcode = "BC456";

        // When
        InventoryData data = InventoryHelper.convertToData(pojo, barcode);

        // Then
        assertNotNull(data);
        assertEquals("inv456", data.getId());
        assertEquals("prod456", data.getProductId());
        assertEquals(50, data.getQuantity());
        assertEquals("BC456", data.getBarcode());
    }

    // TSV Parsing Tests

    @Test
    void testParseInventory_ValidLine() throws ApiException {
        // Given
        String line = "BC001\t100";
        int rowNum = 1;

        ProductPojo product = new ProductPojo();
        product.setId("prod001");
        product.setBarcode("bc001");

        when(productFlow.getByBarcode("bc001")).thenReturn(product);

        // When
        InventoryPojo pojo = InventoryHelper.parseInventory(line, rowNum, productFlow);

        // Then
        assertNotNull(pojo);
        assertEquals("prod001", pojo.getProductId());
        assertEquals(100, pojo.getQuantity());
        verify(productFlow).getByBarcode("bc001");
    }

    @Test
    void testParseInventory_WithExtraSpaces() throws ApiException {
        // Given
        String line = "  BC002  \t  50  ";
        int rowNum = 2;

        ProductPojo product = new ProductPojo();
        product.setId("prod002");
        product.setBarcode("bc002");

        when(productFlow.getByBarcode("bc002")).thenReturn(product);

        // When
        InventoryPojo pojo = InventoryHelper.parseInventory(line, rowNum, productFlow);

        // Then
        assertNotNull(pojo);
        assertEquals("prod002", pojo.getProductId());
        assertEquals(50, pojo.getQuantity());
    }

    @Test
    void testParseInventory_InvalidFormat() {
        // Given
        String line = "BC003"; // Missing quantity
        int rowNum = 3;

        // When/Then
        ApiException exception = assertThrows(ApiException.class, () -> {
            InventoryHelper.parseInventory(line, rowNum, productFlow);
        });
        assertTrue(exception.getMessage().contains("Missing required columns"));
    }

    @Test
    void testParseInventory_InvalidQuantity() throws ApiException {
        // Given
        String line = "BC004\tinvalid";
        int rowNum = 4;

        ProductPojo product = new ProductPojo();
        product.setId("prod004");
        when(productFlow.getByBarcode("bc004")).thenReturn(product);

        // When/Then
        ApiException exception = assertThrows(ApiException.class, () -> {
            InventoryHelper.parseInventory(line, rowNum, productFlow);
        });
        assertTrue(exception.getMessage().contains("Invalid quantity"));
    }

    @Test
    void testParseInventory_NegativeQuantity() throws ApiException {
        // Given
        String line = "BC005\t-10";
        int rowNum = 5;

        ProductPojo product = new ProductPojo();
        product.setId("prod005");
        when(productFlow.getByBarcode("bc005")).thenReturn(product);

        // When/Then
        ApiException exception = assertThrows(ApiException.class, () -> {
            InventoryHelper.parseInventory(line, rowNum, productFlow);
        });
        assertTrue(exception.getMessage().contains("cannot be negative"));
    }

    @Test
    void testParseInventory_ZeroQuantity() throws ApiException {
        // Given
        String line = "BC006\t0";
        int rowNum = 6;

        ProductPojo product = new ProductPojo();
        product.setId("prod006");
        when(productFlow.getByBarcode("bc006")).thenReturn(product);

        // When
        InventoryPojo pojo = InventoryHelper.parseInventory(line, rowNum, productFlow);

        // Then
        assertNotNull(pojo);
        assertEquals(0, pojo.getQuantity());
    }

    @Test
    void testIsHeader_WithBarcode() {
        // Given
        String line = "barcode\tquantity";

        // When
        boolean result = InventoryHelper.isHeader(line);

        // Then
        assertTrue(result);
    }

    @Test
    void testIsHeader_WithQuantity() {
        // Given
        String line = "quantity\tbarcode";

        // When
        boolean result = InventoryHelper.isHeader(line);

        // Then
        assertTrue(result);
    }

    @Test
    void testIsHeader_CaseInsensitive() {
        // Given
        String line = "BARCODE\tQUANTITY";

        // When
        boolean result = InventoryHelper.isHeader(line);

        // Then
        assertTrue(result);
    }

    @Test
    void testIsHeader_DataRow() {
        // Given
        String line = "BC001\t100";

        // When
        boolean result = InventoryHelper.isHeader(line);

        // Then
        assertFalse(result);
    }

    @Test
    void testBuildResultTsv_EmptyList() {
        // Given
        List<TsvUploadResult> results = new ArrayList<>();

        // When
        String tsv = InventoryHelper.buildResultTsv(results);

        // Then
        assertNotNull(tsv);
        assertTrue(tsv.startsWith("Row Number\tStatus\tError Message\tOriginal Data"));
    }

    @Test
    void testBuildResultTsv_WithResults() {
        // Given
        List<TsvUploadResult> results = new ArrayList<>();

        TsvUploadResult result1 = new TsvUploadResult(1, "SUCCESS", "Inventory updated", "BC001\t100");
        results.add(result1);

        TsvUploadResult result2 = new TsvUploadResult(2, "FAILED", "Product not found", "BC999\t50");
        results.add(result2);

        // When
        String tsv = InventoryHelper.buildResultTsv(results);

        // Then
        assertNotNull(tsv);
        assertTrue(tsv.contains("1\tSUCCESS\tInventory updated"));
        assertTrue(tsv.contains("2\tFAILED\tProduct not found"));
    }

    @Test
    void testBuildResultTsv_WithTabsInErrorMessage() {
        // Given
        List<TsvUploadResult> results = new ArrayList<>();

        TsvUploadResult result = new TsvUploadResult(1, "ERROR", "Error:\tInvalid\tdata", "BC001\t100");
        results.add(result);

        // When
        String tsv = InventoryHelper.buildResultTsv(results);

        // Then
        assertNotNull(tsv);
        // Tabs in error message should be replaced with spaces
        assertFalse(tsv.contains("Error:\tInvalid\tdata"));
        assertTrue(tsv.contains("Error: Invalid data"));
    }
}
