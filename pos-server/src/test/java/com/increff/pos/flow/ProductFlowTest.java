package com.increff.pos.flow;

import com.increff.pos.api.ClientApi;
import com.increff.pos.api.InventoryApi;
import com.increff.pos.api.ProductApi;
import com.increff.pos.db.ClientPojo;
import com.increff.pos.db.InventoryPojo;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.ProductData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductFlowTest {

    @Mock
    private ProductApi productApi;

    @Mock
    private InventoryApi inventoryApi;

    @Mock
    private ClientApi clientApi;

    @InjectMocks
    private ProductFlow productFlow;

    private ProductPojo productPojo;
    private ClientPojo clientPojo;
    private InventoryPojo inventoryPojo;

    @BeforeEach
    void setUp() {
        productPojo = new ProductPojo();
        productPojo.setId("prod1");
        productPojo.setBarcode("bc123");
        productPojo.setClientId("C001");
        productPojo.setName("test product");
        productPojo.setMrp(100.0);

        clientPojo = new ClientPojo();
        clientPojo.setId("client1");
        clientPojo.setClientId("C001");
        clientPojo.setName("Test Client");

        inventoryPojo = new InventoryPojo();
        inventoryPojo.setId("inv1");
        inventoryPojo.setProductId("prod1");
        inventoryPojo.setQuantity(10);
    }

    @Test
    void testAdd_CreatesInventoryWithZeroQuantity() throws ApiException {
        // Given
        when(productApi.add(any(ProductPojo.class))).thenReturn(productPojo);
        when(inventoryApi.add(any(InventoryPojo.class))).thenReturn(inventoryPojo);

        // When
        ProductPojo result = productFlow.add(productPojo);

        // Then
        assertNotNull(result);
        assertEquals("prod1", result.getId());
        verify(productApi, times(1)).add(any(ProductPojo.class));
        verify(inventoryApi, times(1)).add(argThat(inv -> inv.getProductId().equals("prod1") &&
                inv.getQuantity() == 0));
    }

    @Test
    void testGetByBarcode_Success() throws ApiException {
        // Given
        when(productApi.getCheckByBarcode("bc123")).thenReturn(productPojo);

        // When
        ProductPojo result = productFlow.getByBarcode("bc123");

        // Then
        assertNotNull(result);
        assertEquals("bc123", result.getBarcode());
        verify(productApi, times(1)).getCheckByBarcode("bc123");
    }

    @Test
    void testGetByBarcode_NotFound() throws ApiException {
        // Given
        when(productApi.getCheckByBarcode("invalid")).thenThrow(new ApiException("Product not found"));

        // When/Then
        assertThrows(ApiException.class, () -> productFlow.getByBarcode("invalid"));
    }

    @Test
    void testUpdate_Success() throws ApiException {
        // Given
        when(productApi.update(anyString(), any(ProductPojo.class))).thenReturn(productPojo);

        // When
        ProductPojo result = productFlow.update("prod1", productPojo);

        // Then
        assertNotNull(result);
        verify(productApi, times(1)).update("prod1", productPojo);
    }

    @Test
    void testAdd_InventoryCreationFails() throws ApiException {
        // Given
        when(productApi.add(any(ProductPojo.class))).thenReturn(productPojo);
        when(inventoryApi.add(any(InventoryPojo.class)))
                .thenThrow(new ApiException("Inventory creation failed"));

        // When/Then
        assertThrows(ApiException.class, () -> productFlow.add(productPojo));
    }

    @Test
    void testGetById_Success() throws ApiException {
        // Given
        when(productApi.getCheck("prod1")).thenReturn(productPojo);

        // When
        ProductPojo result = productFlow.getById("prod1");

        // Then
        assertNotNull(result);
        assertEquals("bc123", result.getBarcode());
    }

    @Test
    void testCreate_Success() throws ApiException {
        // Given
        when(productApi.add(any(ProductPojo.class))).thenReturn(productPojo);

        // When
        ProductPojo result = productFlow.create(productPojo);

        // Then
        assertNotNull(result);
        verify(productApi, times(1)).add(any(ProductPojo.class));
    }
}
