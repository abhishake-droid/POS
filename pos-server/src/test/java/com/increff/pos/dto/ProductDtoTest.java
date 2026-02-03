package com.increff.pos.dto;

import com.increff.pos.db.ClientPojo;
import com.increff.pos.db.InventoryPojo;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.flow.ProductFlow;
import com.increff.pos.model.data.ProductData;
import com.increff.pos.model.form.PageForm;
import com.increff.pos.model.form.ProductForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductDtoTest {

    @Mock
    private ProductFlow productFlow;

    @InjectMocks
    private ProductDto productDto;

    private ProductForm validForm;
    private ProductPojo productPojo;
    private InventoryPojo inventoryPojo;
    private ClientPojo clientPojo;

    @BeforeEach
    void setUp() {
        validForm = new ProductForm();
        validForm.setBarcode("BC123");
        validForm.setClientId("C001");
        validForm.setName("Test Product");
        validForm.setMrp(100.0);

        productPojo = new ProductPojo();
        productPojo.setId("prod1");
        productPojo.setBarcode("bc123");
        productPojo.setClientId("C001");
        productPojo.setName("test product");
        productPojo.setMrp(100.0);

        inventoryPojo = new InventoryPojo();
        inventoryPojo.setProductId("prod1");
        inventoryPojo.setQuantity(0);

        clientPojo = new ClientPojo();
        clientPojo.setId("C001");
        clientPojo.setName("Test Client");
    }

    @Test
    void testCreate_Success() throws ApiException {
        // Given
        when(productFlow.create(any(ProductPojo.class))).thenReturn(productPojo);
        when(productFlow.getInventoryByProductId("prod1")).thenReturn(inventoryPojo);
        when(productFlow.getClientById("C001")).thenReturn(clientPojo);

        // When
        ProductData result = productDto.create(validForm);

        // Then
        assertNotNull(result);
        assertEquals("bc123", result.getBarcode());
        assertEquals("Test Client", result.getClientName());
        assertEquals(0, result.getQuantity());
        verify(productFlow, times(1)).create(any(ProductPojo.class));
    }

    @Test
    void testCreate_InvalidBarcode() {
        // Given
        validForm.setBarcode("");

        // When/Then
        assertThrows(ApiException.class, () -> productDto.create(validForm));
    }

    @Test
    void testCreate_InvalidMrp() {
        // Given
        validForm.setMrp(-10.0);

        // When/Then
        assertThrows(ApiException.class, () -> productDto.create(validForm));
    }

    @Test
    void testCreate_NullName() {
        // Given
        validForm.setName(null);

        // When/Then
        assertThrows(ApiException.class, () -> productDto.create(validForm));
    }

    @Test
    void testGetById_Success() throws ApiException {
        // Given
        when(productFlow.getById("prod1")).thenReturn(productPojo);
        when(productFlow.getInventoryByProductId("prod1")).thenReturn(inventoryPojo);
        when(productFlow.getClientById("C001")).thenReturn(clientPojo);

        // When
        ProductData result = productDto.getById("prod1");

        // Then
        assertNotNull(result);
        assertEquals("prod1", result.getId());
        assertEquals("Test Client", result.getClientName());
        verify(productFlow, times(1)).getById("prod1");
    }

    @Test
    void testGetById_NotFound() throws ApiException {
        // Given
        when(productFlow.getById("invalid")).thenThrow(new ApiException("Product not found"));

        // When/Then
        assertThrows(ApiException.class, () -> productDto.getById("invalid"));
    }

    @Test
    void testGetByBarcode_Success() throws ApiException {
        // Given
        when(productFlow.getByBarcode("bc123")).thenReturn(productPojo);
        when(productFlow.getInventoryByProductId("prod1")).thenReturn(inventoryPojo);
        when(productFlow.getClientById("C001")).thenReturn(clientPojo);

        // When
        ProductData result = productDto.getByBarcode("BC123"); // Test normalization

        // Then
        assertNotNull(result);
        assertEquals("bc123", result.getBarcode());
        verify(productFlow, times(1)).getByBarcode("bc123");
    }

    @Test
    void testGetByBarcode_EmptyBarcode() {
        // When/Then
        assertThrows(ApiException.class, () -> productDto.getByBarcode(""));
    }

    @Test
    void testGetByBarcode_NullBarcode() {
        // When/Then
        assertThrows(ApiException.class, () -> productDto.getByBarcode(null));
    }

    @Test
    void testUpdate_Success() throws ApiException {
        // Given
        when(productFlow.update(anyString(), any(ProductPojo.class))).thenReturn(productPojo);
        when(productFlow.getInventoryByProductId("prod1")).thenReturn(inventoryPojo);
        when(productFlow.getClientById("C001")).thenReturn(clientPojo);

        // When
        ProductData result = productDto.update("prod1", validForm);

        // Then
        assertNotNull(result);
        verify(productFlow, times(1)).update(anyString(), any(ProductPojo.class));
    }

    @Test
    void testUpdate_InvalidForm() {
        // Given - negative MRP is invalid
        validForm.setMrp(-1.0);

        // When/Then
        assertThrows(ApiException.class, () -> productDto.update("prod1", validForm));
    }

    @Test
    void testGetAll_Success() throws ApiException {
        // Given
        PageForm pageForm = new PageForm();
        pageForm.setPage(0);
        pageForm.setSize(10);

        Page<ProductPojo> pojoPage = new PageImpl<>(Arrays.asList(productPojo));
        when(productFlow.getAll(any(PageForm.class))).thenReturn(pojoPage);
        when(productFlow.getInventoryByProductId("prod1")).thenReturn(inventoryPojo);
        when(productFlow.getClientById("C001")).thenReturn(clientPojo);

        // When
        Page<ProductData> result = productDto.getAll(pageForm);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(productFlow, times(1)).getAll(any(PageForm.class));
    }

    @Test
    void testGetAll_InvalidPage() {
        // Given
        PageForm pageForm = new PageForm();
        pageForm.setPage(-1);
        pageForm.setSize(10);

        // When/Then
        assertThrows(ApiException.class, () -> productDto.getAll(pageForm));
    }

    @Test
    void testNormalization() throws ApiException {
        // Given - form with mixed case
        validForm.setBarcode("BC123");
        validForm.setName("Test Product");

        when(productFlow.create(any(ProductPojo.class))).thenReturn(productPojo);
        when(productFlow.getInventoryByProductId("prod1")).thenReturn(inventoryPojo);
        when(productFlow.getClientById("C001")).thenReturn(clientPojo);

        // When
        productDto.create(validForm);

        // Then - verify normalization happened (lowercase)
        verify(productFlow, times(1)).create(argThat(pojo -> pojo.getBarcode().equals("bc123") &&
                pojo.getName().equals("test product")));
    }

    @Test
    void testGetAll_WithPagination() throws ApiException {
        // Given
        PageForm pageForm = new PageForm();
        pageForm.setPage(0);
        pageForm.setSize(10);

        List<ProductPojo> products = Arrays.asList(productPojo);
        Page<ProductPojo> page = new PageImpl<>(products);
        when(productFlow.getAll(any(PageForm.class))).thenReturn(page);
        when(productFlow.getInventoryByProductId(anyString())).thenReturn(inventoryPojo);
        when(productFlow.getClientById(anyString())).thenReturn(clientPojo);

        // When
        Page<ProductData> result = productDto.getAll(pageForm);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void testGetAll_InvalidPageSize() {
        // Given
        PageForm pageForm = new PageForm();
        pageForm.setPage(0);
        pageForm.setSize(101); // Max is 100

        // When/Then
        assertThrows(ApiException.class, () -> productDto.getAll(pageForm));
    }

    @Test
    void testGetAll_InvalidPageNumber() {
        // Given
        PageForm pageForm = new PageForm();
        pageForm.setPage(-1);
        pageForm.setSize(10);

        // When/Then
        assertThrows(ApiException.class, () -> productDto.getAll(pageForm));
    }

    @Test
    void testGetById_WithValidId() throws ApiException {
        // Given
        when(productFlow.getById("prod1")).thenReturn(productPojo);
        when(productFlow.getInventoryByProductId("prod1")).thenReturn(inventoryPojo);
        when(productFlow.getClientById("C001")).thenReturn(clientPojo);

        // When
        ProductData result = productDto.getById("prod1");

        // Then
        assertNotNull(result);
        assertEquals("prod1", result.getId());
    }

    @Test
    void testGetById_WithInvalidId() throws ApiException {
        // Given
        when(productFlow.getById("invalid")).thenThrow(new ApiException("Product not found"));

        // When/Then
        assertThrows(ApiException.class, () -> productDto.getById("invalid"));
    }

}
