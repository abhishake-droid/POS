package com.increff.pos.helper;

import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.ProductData;
import com.increff.pos.model.data.TsvUploadResult;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ProductHelperTest {

    @Test
    void testConvertToEntity() {
        // Given
        com.increff.pos.model.form.ProductForm form = new com.increff.pos.model.form.ProductForm();
        form.setBarcode("BC123");
        form.setName("Product Name");
        form.setMrp(99.99);
        form.setClientId("client123");
        form.setImageUrl("http://example.com/image.jpg");

        // When
        ProductPojo pojo = ProductHelper.convertToEntity(form);

        // Then
        assertNotNull(pojo);
        assertEquals("BC123", pojo.getBarcode());
        assertEquals("Product Name", pojo.getName());
        assertEquals(99.99, pojo.getMrp());
        assertEquals("client123", pojo.getClientId());
        assertEquals("http://example.com/image.jpg", pojo.getImageUrl());
    }

    @Test
    void testConvertToDto_WithClientNameAndQuantity() {
        // Given
        ProductPojo pojo = new ProductPojo();
        pojo.setId("prod789");
        pojo.setBarcode("BC789");
        pojo.setName("Third Product");
        pojo.setMrp(149.99);
        pojo.setClientId("client789");
        pojo.setImageUrl(null);

        String clientName = "Test Client";
        Integer quantity = 50;

        // When
        ProductData data = ProductHelper.convertToData(pojo, clientName, quantity);

        // Then
        assertNotNull(data);
        assertEquals("prod789", data.getId());
        assertEquals("BC789", data.getBarcode());
        assertEquals("Third Product", data.getName());
        assertEquals(149.99, data.getMrp());
        assertEquals("client789", data.getClientId());
        assertNull(data.getImageUrl());
        assertEquals("Test Client", data.getClientName());
        assertEquals(50, data.getQuantity());
    }

    @Test
    void testConvertToEntity_NullImageUrl() {
        // Given
        com.increff.pos.model.form.ProductForm form = new com.increff.pos.model.form.ProductForm();
        form.setBarcode("BC999");
        form.setName("No Image Product");
        form.setMrp(29.99);
        form.setClientId("client999");
        form.setImageUrl(null);

        // When
        ProductPojo pojo = ProductHelper.convertToEntity(form);

        // Then
        assertNotNull(pojo);
        assertEquals("BC999", pojo.getBarcode());
        assertNull(pojo.getImageUrl());
    }

    // TSV Parsing Tests

    @Test
    void testParseHeader_ValidHeader() throws ApiException {
        // Given
        String headerLine = "barcode\tclientid\tname\tmrp\timageurl";

        // When
        Map<String, Integer> columnMap = ProductHelper.parseHeader(headerLine);

        // Then
        assertNotNull(columnMap);
        assertEquals(0, columnMap.get("barcode"));
        assertEquals(1, columnMap.get("clientid"));
        assertEquals(2, columnMap.get("name"));
        assertEquals(3, columnMap.get("mrp"));
        assertEquals(4, columnMap.get("imageurl"));
    }

    @Test
    void testParseHeader_JumbledOrder() throws ApiException {
        // Given
        String headerLine = "mrp\tname\tbarcode\tclientid";

        // When
        Map<String, Integer> columnMap = ProductHelper.parseHeader(headerLine);

        // Then
        assertNotNull(columnMap);
        assertEquals(2, columnMap.get("barcode"));
        assertEquals(3, columnMap.get("clientid"));
        assertEquals(1, columnMap.get("name"));
        assertEquals(0, columnMap.get("mrp"));
    }

    @Test
    void testParseHeader_MissingRequiredColumn() {
        // Given
        String headerLine = "barcode\tname\tmrp"; // Missing clientid

        // When/Then
        ApiException exception = assertThrows(ApiException.class, () -> {
            ProductHelper.parseHeader(headerLine);
        });
        assertTrue(exception.getMessage().contains("Missing required column"));
    }

    @Test
    void testParseHeader_WithExtraSpaces() throws ApiException {
        // Given
        String headerLine = "  barcode  \t  clientid  \t  name  \t  mrp  ";

        // When
        Map<String, Integer> columnMap = ProductHelper.parseHeader(headerLine);

        // Then
        assertNotNull(columnMap);
        assertTrue(columnMap.containsKey("barcode"));
        assertTrue(columnMap.containsKey("clientid"));
    }

    // Tests for parseProduct without header map removed - headers are now mandatory

    @Test
    void testParseProduct_WithHeaderMap() throws ApiException {
        // Given
        Map<String, Integer> columnMap = new HashMap<>();
        columnMap.put("barcode", 2);
        columnMap.put("clientid", 0);
        columnMap.put("name", 1);
        columnMap.put("mrp", 3);
        columnMap.put("imageurl", 4);

        String line = "C003\tProduct C\tBC003\t149.99\thttp://image.com/c.jpg";
        int rowNum = 3;

        // When
        ProductPojo pojo = ProductHelper.parseProduct(line, rowNum, columnMap);

        // Then
        assertNotNull(pojo);
        assertEquals("bc003", pojo.getBarcode());
        assertEquals("C003", pojo.getClientId());
        assertEquals("product c", pojo.getName());
        assertEquals(149.99, pojo.getMrp());
        assertEquals("http://image.com/c.jpg", pojo.getImageUrl());
    }

    // Tests for parseProduct without header map removed - headers are now mandatory

    @Test
    void testParseProduct_MissingColumnsWithHeaderMap() {
        // Given
        Map<String, Integer> columnMap = new HashMap<>();
        columnMap.put("barcode", 0);
        columnMap.put("clientid", 1);
        columnMap.put("name", 2);
        columnMap.put("mrp", 5); // Index 5 doesn't exist

        String line = "BC006\tC006\tProduct F\t99.99";
        int rowNum = 6;

        // When/Then
        ApiException exception = assertThrows(ApiException.class, () -> {
            ProductHelper.parseProduct(line, rowNum, columnMap);
        });
        assertTrue(exception.getMessage().contains("Missing required columns"));
    }

    @Test
    void testIsHeader_ValidHeader() {
        // Given
        String line = "barcode\tclientid\tname\tmrp";

        // When
        boolean result = ProductHelper.isHeader(line);

        // Then
        assertTrue(result);
    }

    @Test
    void testIsHeader_WithImageUrl() {
        // Given
        String line = "barcode\tclientid\tname\tmrp\timageurl";

        // When
        boolean result = ProductHelper.isHeader(line);

        // Then
        assertTrue(result);
    }

    @Test
    void testIsHeader_JumbledOrder() {
        // Given
        String line = "name\tmrp\tbarcode\tclientid";

        // When
        boolean result = ProductHelper.isHeader(line);

        // Then
        assertTrue(result);
    }

    @Test
    void testIsHeader_MissingColumn() {
        // Given
        String line = "barcode\tname\tmrp"; // Missing clientid

        // When
        boolean result = ProductHelper.isHeader(line);

        // Then
        assertFalse(result);
    }

    @Test
    void testIsHeader_DataRow() {
        // Given
        String line = "BC001\tC001\tProduct A\t99.99";

        // When
        boolean result = ProductHelper.isHeader(line);

        // Then
        assertFalse(result);
    }

    @Test
    void testBuildResultTsv_EmptyList() {
        // Given
        List<TsvUploadResult> results = new ArrayList<>();

        // When
        String tsv = ProductHelper.buildResultTsv(results);

        // Then
        assertNotNull(tsv);
        assertTrue(tsv.startsWith("Row Number\tStatus\tError Message\tOriginal Data"));
    }

    @Test
    void testBuildResultTsv_WithResults() {
        // Given
        List<TsvUploadResult> results = new ArrayList<>();

        TsvUploadResult result1 = new TsvUploadResult();
        result1.setRowNumber(1);
        result1.setStatus("SUCCESS");
        result1.setErrorMessage(null);
        result1.setData("BC001\tC001\tProduct A\t99.99");
        results.add(result1);

        TsvUploadResult result2 = new TsvUploadResult();
        result2.setRowNumber(2);
        result2.setStatus("ERROR");
        result2.setErrorMessage("Duplicate barcode");
        result2.setData("BC001\tC001\tProduct B\t49.99");
        results.add(result2);

        // When
        String tsv = ProductHelper.buildResultTsv(results);

        // Then
        assertNotNull(tsv);
        assertTrue(tsv.contains("Row Number\tStatus\tError Message\tOriginal Data"));
        assertTrue(tsv.contains("1\tSUCCESS"));
        assertTrue(tsv.contains("2\tERROR\tDuplicate barcode"));
    }

    @Test
    void testBuildResultTsv_WithTabsInErrorMessage() {
        // Given
        List<TsvUploadResult> results = new ArrayList<>();

        TsvUploadResult result = new TsvUploadResult();
        result.setRowNumber(1);
        result.setStatus("ERROR");
        result.setErrorMessage("Error:\tInvalid\tdata"); // Contains tabs
        result.setData("BC001\tC001\tProduct A\t99.99");
        results.add(result);

        // When
        String tsv = ProductHelper.buildResultTsv(results);

        // Then
        assertNotNull(tsv);
        // Tabs in error message should be replaced with spaces
        assertFalse(tsv.contains("Error:\tInvalid\tdata"));
        assertTrue(tsv.contains("Error: Invalid data"));
    }
}
