package com.increff.pos.flow;

import com.increff.pos.api.InventoryApi;
import com.increff.pos.api.ProductApi;
import com.increff.pos.db.InventoryPojo;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.InventoryData;
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
class InventoryFlowTest {

    @Mock
    private InventoryApi inventoryApi;

    @Mock
    private ProductApi productApi;

    @InjectMocks
    private InventoryFlow inventoryFlow;

    private InventoryPojo inventoryPojo;
    private ProductPojo productPojo;

    @BeforeEach
    void setUp() {
        inventoryPojo = new InventoryPojo();
        inventoryPojo.setId("inv1");
        inventoryPojo.setProductId("prod1");
        inventoryPojo.setQuantity(100);

        productPojo = new ProductPojo();
        productPojo.setId("prod1");
        productPojo.setBarcode("bc123");
        productPojo.setName("Test Product");
        productPojo.setMrp(100.0);
    }

    @Test
    void testGetCheck_Success() throws ApiException {
        // Given
        when(inventoryApi.getCheck("inv1")).thenReturn(inventoryPojo);

        // When
        InventoryPojo result = inventoryFlow.getCheck("inv1");

        // Then
        assertNotNull(result);
        assertEquals(100, result.getQuantity());
        verify(inventoryApi, times(1)).getCheck("inv1");
    }

    @Test
    void testUpdateInventory_Success() throws ApiException {
        // Given
        when(inventoryApi.getCheckByProductId("prod1")).thenReturn(inventoryPojo);
        when(inventoryApi.update(anyString(), any(InventoryPojo.class))).thenReturn(inventoryPojo);

        // When
        InventoryPojo result = inventoryFlow.updateInventory("prod1", 150);

        // Then
        assertNotNull(result);
        verify(inventoryApi, times(1)).getCheckByProductId("prod1");
        verify(inventoryApi, times(1)).update(anyString(), any(InventoryPojo.class));
    }

    @Test
    void testUpdateBulk_Success() throws ApiException {
        // Given
        List<InventoryPojo> pojos = Arrays.asList(inventoryPojo);
        when(inventoryApi.updateBulk(anyList())).thenReturn(pojos);

        // When
        List<InventoryPojo> result = inventoryFlow.updateBulk(pojos);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(inventoryApi, times(1)).updateBulk(anyList());
    }

    @Test
    void testUpdateInventory_InventoryNotFound() throws ApiException {
        // Given
        when(inventoryApi.getCheckByProductId("prod1"))
                .thenThrow(new ApiException("Inventory not found"));

        // When/Then
        assertThrows(ApiException.class, () -> inventoryFlow.updateInventory("prod1", 150));
    }

    @Test
    void testUpdateInventory_NegativeQuantity() throws ApiException {
        // Given
        when(inventoryApi.getCheckByProductId("prod1")).thenReturn(inventoryPojo);
        when(inventoryApi.update(anyString(), any(InventoryPojo.class))).thenReturn(inventoryPojo);

        // When
        InventoryPojo result = inventoryFlow.updateInventory("prod1", -10);

        // Then
        assertNotNull(result);
        verify(inventoryApi, times(1)).update(anyString(), argThat(inv -> inv.getQuantity() == -10));
    }

    @Test
    void testUpdateBulk_EmptyList() throws ApiException {
        // Given
        when(inventoryApi.updateBulk(anyList())).thenReturn(java.util.Collections.emptyList());

        // When
        List<InventoryPojo> result = inventoryFlow.updateBulk(java.util.Collections.emptyList());

        // Then
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testGetCheck_InventoryNotFound() throws ApiException {
        // Given
        when(inventoryApi.getCheck("inv1")).thenThrow(new ApiException("Inventory not found"));

        // When/Then
        assertThrows(ApiException.class, () -> inventoryFlow.getCheck("inv1"));
    }

    @Test
    void testUpdateInventory_ZeroQuantity() throws ApiException {
        // Given
        when(inventoryApi.getCheckByProductId("prod1")).thenReturn(inventoryPojo);
        when(inventoryApi.update(anyString(), any(InventoryPojo.class))).thenReturn(inventoryPojo);

        // When
        InventoryPojo result = inventoryFlow.updateInventory("prod1", 0);

        // Then
        assertNotNull(result);
        verify(inventoryApi, times(1)).update(anyString(), argThat(inv -> inv.getQuantity() == 0));
    }

    @Test
    void testUpdateBulk_MultipleItems() throws ApiException {
        // Given
        InventoryPojo inv2 = new InventoryPojo();
        inv2.setId("inv2");
        inv2.setProductId("prod2");
        inv2.setQuantity(50);

        List<InventoryPojo> pojos = Arrays.asList(inventoryPojo, inv2);
        when(inventoryApi.updateBulk(anyList())).thenReturn(pojos);

        // When
        List<InventoryPojo> result = inventoryFlow.updateBulk(pojos);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(inventoryApi, times(1)).updateBulk(anyList());
    }
}
