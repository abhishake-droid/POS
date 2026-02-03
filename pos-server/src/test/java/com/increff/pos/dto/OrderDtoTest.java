package com.increff.pos.dto;

import com.increff.pos.exception.ApiException;
import com.increff.pos.flow.OrderFlow;
import com.increff.pos.model.data.OrderCreationResult;
import com.increff.pos.model.data.OrderData;
import com.increff.pos.model.form.OrderForm;
import com.increff.pos.model.form.OrderLineForm;
import com.increff.pos.db.OrderPojo;
import com.increff.pos.db.OrderItemPojo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderDtoTest {

    @Mock
    private OrderFlow orderFlow;

    @InjectMocks
    private OrderDto orderDto;

    private OrderForm validForm;
    private OrderPojo orderPojo;
    private OrderCreationResult creationResult;

    @BeforeEach
    void setUp() {
        OrderLineForm line = new OrderLineForm();
        line.setProductId("prod1");
        line.setQuantity(10);
        line.setMrp(100.0);

        validForm = new OrderForm();
        validForm.setLines(Arrays.asList(line));

        orderPojo = new OrderPojo();
        orderPojo.setId("order1");
        orderPojo.setOrderId("ORD001");
        orderPojo.setOrderDate(ZonedDateTime.now());
        orderPojo.setStatus("PENDING");

        creationResult = new OrderCreationResult();
        creationResult.setOrderId("ORD001");
        creationResult.setFulfillable(true);
    }

    @Test
    void testCreate_Success() throws ApiException {
        // Given
        when(orderFlow.createOrder(anyList())).thenReturn(creationResult);
        when(orderFlow.getOrderWithItems(anyString())).thenReturn(orderPojo);
        when(orderFlow.getOrderItems(anyString())).thenReturn(new ArrayList<>());

        // When
        OrderData result = orderDto.create(validForm);

        // Then
        assertNotNull(result);
        verify(orderFlow, times(1)).createOrder(anyList());
    }

    @Test
    void testCreate_EmptyLines() {
        // Given
        validForm.setLines(new ArrayList<>());

        // When/Then
        assertThrows(ApiException.class, () -> orderDto.create(validForm));
    }

    @Test
    void testGetById_Success() throws ApiException {
        // Given
        when(orderFlow.getOrderWithItems("ORD001")).thenReturn(orderPojo);
        when(orderFlow.getOrderItems(anyString())).thenReturn(new ArrayList<>());

        // When
        OrderData result = orderDto.getById("ORD001");

        // Then
        assertNotNull(result);
        verify(orderFlow, times(1)).getOrderWithItems("ORD001");
    }

    @Test
    void testCancel_Success() throws ApiException {
        // Given
        when(orderFlow.cancelOrder("ORD001")).thenReturn(orderPojo);
        when(orderFlow.getOrderItems(anyString())).thenReturn(new ArrayList<>());

        // When
        OrderData result = orderDto.cancel("ORD001");

        // Then
        assertNotNull(result);
        verify(orderFlow, times(1)).cancelOrder("ORD001");
    }

    @Test
    void testUpdate_Success() throws ApiException {
        // Given
        when(orderFlow.updateOrder(anyString(), anyList())).thenReturn(orderPojo);
        when(orderFlow.getOrderItems(anyString())).thenReturn(new ArrayList<>());

        // When
        OrderData result = orderDto.update("ORD001", validForm);

        // Then
        assertNotNull(result);
        verify(orderFlow, times(1)).updateOrder(anyString(), anyList());
    }

    @Test
    void testUpdate_EmptyLines() {
        // Given
        validForm.setLines(new ArrayList<>());

        // When/Then
        assertThrows(ApiException.class, () -> orderDto.update("ORD001", validForm));
    }

    @Test
    void testRetry_Success() throws ApiException {
        // Given
        when(orderFlow.retryOrder(anyString(), anyList())).thenReturn(creationResult);
        when(orderFlow.getOrderWithItems(anyString())).thenReturn(orderPojo);
        when(orderFlow.getOrderItems(anyString())).thenReturn(new ArrayList<>());

        // When
        OrderData result = orderDto.retry("ORD001", validForm);

        // Then
        assertNotNull(result);
        verify(orderFlow, times(1)).retryOrder(anyString(), anyList());
    }

    @Test
    void testRetry_WithoutForm() throws ApiException {
        // Given
        when(orderFlow.retryOrder(anyString(), isNull())).thenReturn(creationResult);
        when(orderFlow.getOrderWithItems(anyString())).thenReturn(orderPojo);
        when(orderFlow.getOrderItems(anyString())).thenReturn(new ArrayList<>());

        // When
        OrderData result = orderDto.retry("ORD001", null);

        // Then
        assertNotNull(result);
        verify(orderFlow, times(1)).retryOrder(anyString(), isNull());
    }

    @Test
    void testCreate_NullLines() {
        // Given
        validForm.setLines(null);

        // When/Then
        assertThrows(ApiException.class, () -> orderDto.create(validForm));
    }

    @Test
    void testCreate_MultipleLines() throws ApiException {
        // Given
        OrderLineForm line1 = new OrderLineForm();
        line1.setProductId("prod1");
        line1.setQuantity(10);
        line1.setMrp(100.0);

        OrderLineForm line2 = new OrderLineForm();
        line2.setProductId("prod2");
        line2.setQuantity(5);
        line2.setMrp(200.0);

        validForm.setLines(Arrays.asList(line1, line2));

        when(orderFlow.createOrder(anyList())).thenReturn(creationResult);
        when(orderFlow.getOrderWithItems(anyString())).thenReturn(orderPojo);
        when(orderFlow.getOrderItems(anyString())).thenReturn(new ArrayList<>());

        // When
        OrderData result = orderDto.create(validForm);

        // Then
        assertNotNull(result);
        verify(orderFlow, times(1)).createOrder(anyList());
    }

    @Test
    void testGetById_NotFound() throws ApiException {
        // Given
        when(orderFlow.getOrderWithItems("INVALID")).thenThrow(new ApiException("Order not found"));

        // When/Then
        assertThrows(ApiException.class, () -> orderDto.getById("INVALID"));
    }

    @Test
    void testCancel_AlreadyCancelled() throws ApiException {
        // Given
        when(orderFlow.cancelOrder("ORD001")).thenThrow(new ApiException("Order already cancelled"));

        // When/Then
        assertThrows(ApiException.class, () -> orderDto.cancel("ORD001"));
    }

}
