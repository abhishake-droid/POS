package com.increff.pos.flow;

import com.increff.pos.api.*;
import com.increff.pos.db.*;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.OrderCreationResult;
import com.increff.pos.test.AbstractUnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderFlowTest extends AbstractUnitTest {

    @Autowired
    private OrderFlow orderFlow;

    @Autowired
    private ProductApi productApi;

    @Autowired
    private ClientApi clientApi;

    @Autowired
    private InventoryApi inventoryApi;

    @Autowired
    private OrderApi orderApi;

    @Autowired
    private OrderItemApi orderItemApi;

    // Helper method to create a test client
    private ClientPojo createTestClient(String name, String email) throws ApiException {
        ClientPojo client = new ClientPojo();
        client.setClientId("CL_" + System.currentTimeMillis());
        client.setName(name);
        client.setEmail(email);
        client.setPhone("1234567890");
        return clientApi.add(client);
    }

    // Helper method to create a test product
    private ProductPojo createTestProduct(String barcode, String clientId) throws ApiException {
        ProductPojo product = new ProductPojo();
        product.setBarcode(barcode);
        product.setClientId(clientId);
        product.setName("Test Product " + barcode);
        product.setMrp(100.0);
        return productApi.add(product);
    }

    // Helper method to add inventory
    private void addInventory(String productId, int quantity) throws ApiException {
        InventoryPojo inventory = new InventoryPojo();
        inventory.setProductId(productId);
        inventory.setQuantity(quantity);
        inventoryApi.add(inventory);
    }

    // Helper method to create order item with barcode
    private OrderItemPojo createOrderItem(String barcode, int quantity, double mrp) {
        OrderItemPojo item = new OrderItemPojo();
        item.setBarcode(barcode);
        item.setQuantity(quantity);
        item.setMrp(mrp);
        return item;
    }

    @Test
    void testCreateOrder_FullyFulfillable() throws ApiException {
        // Given - Create client, product, and inventory
        ClientPojo client = createTestClient("Test Client", "test@example.com");
        ProductPojo product = createTestProduct("BC_ORDER1", client.getClientId());
        addInventory(product.getId(), 100);

        // Create order items
        OrderItemPojo item = createOrderItem(product.getBarcode(), 10, 100.0);

        // When
        OrderCreationResult result = orderFlow.createOrder(Arrays.asList(item));

        // Then
        assertNotNull(result);
        assertNotNull(result.getOrderId());
        assertTrue(result.isFulfillable());
        assertTrue(result.getUnfulfillableItems().isEmpty());

        // Verify order was created
        OrderPojo order = orderFlow.getOrderWithItems(result.getOrderId());
        assertNotNull(order);
        assertEquals("PLACED", order.getStatus());
        assertEquals(10, order.getTotalItems());
        assertEquals(1000.0, order.getTotalAmount(), 0.01);

        // Verify inventory was deducted
        InventoryPojo updatedInventory = inventoryApi.getCheckByProductId(product.getId());
        assertEquals(90, updatedInventory.getQuantity());
    }

    @Test
    void testCreateOrder_PartiallyFulfillable() throws ApiException {
        // Given - Create client, product with limited inventory
        ClientPojo client = createTestClient("Test Client 2", "test2@example.com");
        ProductPojo product = createTestProduct("BC_ORDER2", client.getClientId());
        addInventory(product.getId(), 5); // Only 5 available

        // Try to order 10
        OrderItemPojo item = createOrderItem(product.getBarcode(), 10, 100.0);

        // When
        OrderCreationResult result = orderFlow.createOrder(Arrays.asList(item));

        // Then
        assertNotNull(result);
        assertFalse(result.isFulfillable());
        assertEquals(1, result.getUnfulfillableItems().size());

        // Verify order was still created with UNFULFILLABLE status
        OrderPojo order = orderFlow.getOrderWithItems(result.getOrderId());
        assertNotNull(order);
        assertEquals("UNFULFILLABLE", order.getStatus());

        // Verify inventory was NOT deducted (unfulfillable orders don't deduct
        // inventory)
        InventoryPojo updatedInventory = inventoryApi.getCheckByProductId(product.getId());
        assertEquals(5, updatedInventory.getQuantity());
    }

    @Test
    void testCreateOrder_MultipleItems() throws ApiException {
        // Given - Create client and multiple products
        ClientPojo client = createTestClient("Test Client 3", "test3@example.com");
        ProductPojo product1 = createTestProduct("BC_ORDER3A", client.getClientId());
        ProductPojo product2 = createTestProduct("BC_ORDER3B", client.getClientId());
        addInventory(product1.getId(), 50);
        addInventory(product2.getId(), 30);

        // Create order with multiple items
        OrderItemPojo item1 = createOrderItem(product1.getBarcode(), 5, 100.0);
        OrderItemPojo item2 = createOrderItem(product2.getBarcode(), 3, 200.0);

        // When
        OrderCreationResult result = orderFlow.createOrder(Arrays.asList(item1, item2));

        // Then
        assertNotNull(result);
        assertTrue(result.isFulfillable());

        // Verify order totals
        OrderPojo order = orderFlow.getOrderWithItems(result.getOrderId());
        assertEquals(8, order.getTotalItems()); // 5 + 3
        assertEquals(1100.0, order.getTotalAmount(), 0.01); // (5*100) + (3*200)

        // Verify both inventories were deducted
        assertEquals(45, inventoryApi.getCheckByProductId(product1.getId()).getQuantity());
        assertEquals(27, inventoryApi.getCheckByProductId(product2.getId()).getQuantity());
    }

    @Test
    void testCancelOrder_Success() throws ApiException {
        // Given - Create an order first
        ClientPojo client = createTestClient("Test Client 4", "test4@example.com");
        ProductPojo product = createTestProduct("BC_ORDER4", client.getClientId());
        addInventory(product.getId(), 100);

        OrderItemPojo item = createOrderItem(product.getBarcode(), 10, 100.0);

        OrderCreationResult createResult = orderFlow.createOrder(Arrays.asList(item));
        String orderId = createResult.getOrderId();

        // Verify inventory was deducted
        assertEquals(90, inventoryApi.getCheckByProductId(product.getId()).getQuantity());

        // When - Cancel the order
        OrderPojo cancelledOrder = orderFlow.cancelOrder(orderId);

        // Then
        assertNotNull(cancelledOrder);
        assertEquals("CANCELLED", cancelledOrder.getStatus());

        // Verify inventory was restored
        InventoryPojo restoredInventory = inventoryApi.getCheckByProductId(product.getId());
        assertEquals(100, restoredInventory.getQuantity());
    }

    @Test
    void testCancelOrder_RestoresInventory() throws ApiException {
        // Given - Create an order with multiple items
        ClientPojo client = createTestClient("Test Client 5", "test5@example.com");
        ProductPojo product = createTestProduct("BC_ORDER5", client.getClientId());
        addInventory(product.getId(), 100);

        OrderItemPojo item = createOrderItem(product.getBarcode(), 20, 100.0);

        OrderCreationResult createResult = orderFlow.createOrder(Arrays.asList(item));
        String orderId = createResult.getOrderId();

        // Verify inventory was deducted
        assertEquals(80, inventoryApi.getCheckByProductId(product.getId()).getQuantity());

        // When - Cancel the order
        orderFlow.cancelOrder(orderId);

        // Then - Verify inventory was fully restored
        assertEquals(100, inventoryApi.getCheckByProductId(product.getId()).getQuantity());
    }

    @Test
    void testUpdateOrder_Success() throws ApiException {
        // Given - Create an order
        ClientPojo client = createTestClient("Test Client 6", "test6@example.com");
        ProductPojo product = createTestProduct("BC_ORDER6", client.getClientId());
        addInventory(product.getId(), 100);

        OrderItemPojo item = createOrderItem(product.getBarcode(), 10, 100.0);

        OrderCreationResult createResult = orderFlow.createOrder(Arrays.asList(item));
        String orderId = createResult.getOrderId();

        // When - Update the order with different quantity
        OrderItemPojo updatedItem = createOrderItem(product.getBarcode(), 15, 100.0); // Increase quantity

        OrderPojo updatedOrder = orderFlow.updateOrder(orderId, Arrays.asList(updatedItem));

        // Then
        assertNotNull(updatedOrder);
        assertEquals(15, updatedOrder.getTotalItems());
        assertEquals(1500.0, updatedOrder.getTotalAmount(), 0.01);

        // Verify inventory was adjusted (100 - 15 = 85)
        assertEquals(85, inventoryApi.getCheckByProductId(product.getId()).getQuantity());
    }

    @Test
    void testUpdateOrder_CannotUpdateInvoiced() throws ApiException {
        // Given - Create an order and mark it as invoiced
        ClientPojo client = createTestClient("Test Client 7", "test7@example.com");
        ProductPojo product = createTestProduct("BC_ORDER7", client.getClientId());
        addInventory(product.getId(), 100);

        OrderItemPojo item = createOrderItem(product.getBarcode(), 10, 100.0);

        OrderCreationResult createResult = orderFlow.createOrder(Arrays.asList(item));
        String orderId = createResult.getOrderId();

        // Manually set order status to INVOICED
        OrderPojo order = orderFlow.getOrderWithItems(createResult.getOrderId());
        order.setStatus("INVOICED");
        orderApi.update(order.getId(), order);

        // When/Then - Try to update invoiced order
        OrderItemPojo updatedItem = createOrderItem(product.getBarcode(), 15, 100.0);

        assertThrows(ApiException.class, () -> orderFlow.updateOrder(orderId, Arrays.asList(updatedItem)));
    }

    @Test
    void testRetryOrder_Success() throws ApiException {
        // Given - Create a partially fulfilled order
        ClientPojo client = createTestClient("Test Client 8", "test8@example.com");
        ProductPojo product = createTestProduct("BC_ORDER8", client.getClientId());
        addInventory(product.getId(), 5); // Limited inventory

        OrderItemPojo item = createOrderItem(product.getBarcode(), 10, 100.0);

        OrderCreationResult createResult = orderFlow.createOrder(Arrays.asList(item));
        String orderId = createResult.getOrderId();
        assertFalse(createResult.isFulfillable());

        // Add more inventory
        InventoryPojo inventory = inventoryApi.getCheckByProductId(product.getId());
        inventory.setQuantity(20);
        inventoryApi.update(inventory.getId(), inventory);

        // When - Retry the order
        OrderCreationResult retryResult = orderFlow.retryOrder(orderId, null);

        // Then
        assertNotNull(retryResult);
        assertTrue(retryResult.isFulfillable());

        // Verify new order was created (actually same order, status updated)
        OrderPojo newOrder = orderFlow.getOrderWithItems(retryResult.getOrderId());
        assertNotNull(newOrder);
        assertEquals("PLACED", newOrder.getStatus());

        // Verify it's the same order (order ID matches)
        assertEquals(orderId, retryResult.getOrderId());
    }

    @Test
    void testRetryOrder_WithUpdatedItems() throws ApiException {
        // Given - Create an unfulfillable order
        ClientPojo client = createTestClient("Test Client 9", "test9@example.com");
        ProductPojo product = createTestProduct("BC_ORDER9", client.getClientId());
        addInventory(product.getId(), 5); // Limited inventory

        OrderItemPojo item = createOrderItem(product.getBarcode(), 20, 100.0); // More than available - will be
                                                                               // unfulfillable

        OrderCreationResult createResult = orderFlow.createOrder(Arrays.asList(item));
        String orderId = createResult.getOrderId();

        // Add more inventory
        InventoryPojo inventory = inventoryApi.getCheckByProductId(product.getId());
        inventory.setQuantity(50);
        inventoryApi.update(inventory.getId(), inventory);

        // When - Retry with different quantity
        OrderItemPojo newItem = createOrderItem(product.getBarcode(), 20, 100.0); // Different quantity

        OrderCreationResult retryResult = orderFlow.retryOrder(orderId, Arrays.asList(newItem));

        // Then
        assertNotNull(retryResult);
        OrderPojo newOrder = orderFlow.getOrderWithItems(retryResult.getOrderId());
        assertEquals(20, newOrder.getTotalItems());
        assertEquals(2000.0, newOrder.getTotalAmount(), 0.01);
    }

    @Test
    void testGetOrderWithFilters_ByOrderId() throws ApiException {
        // Given - Create an order
        ClientPojo client = createTestClient("Test Client 10", "test10@example.com");
        ProductPojo product = createTestProduct("BC_ORDER10", client.getClientId());
        addInventory(product.getId(), 100);

        OrderItemPojo item = createOrderItem(product.getBarcode(), 10, 100.0);

        OrderCreationResult createResult = orderFlow.createOrder(Arrays.asList(item));
        String orderId = createResult.getOrderId();

        // When - Filter by order ID
        List<OrderPojo> results = orderFlow.getOrderWithFilters(orderId, null, null, null);

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(orderId, results.get(0).getOrderId());
    }

    @Test
    void testGetOrderWithFilters_ByStatus() throws ApiException {
        // Given - Create orders with different statuses
        ClientPojo client = createTestClient("Test Client 11", "test11@example.com");
        ProductPojo product = createTestProduct("BC_ORDER11", client.getClientId());
        addInventory(product.getId(), 100);

        OrderItemPojo item = createOrderItem(product.getBarcode(), 10, 100.0);

        OrderCreationResult createResult = orderFlow.createOrder(Arrays.asList(item));
        String orderId = createResult.getOrderId();

        // When - Filter by status
        List<OrderPojo> results = orderFlow.getOrderWithFilters(null, "PLACED", null, null);

        // Then
        assertNotNull(results);
        assertTrue(results.size() > 0);
        assertTrue(results.stream().allMatch(o -> "PLACED".equals(o.getStatus())));
    }

    @Test
    void testGetOrderWithFilters_ByDateRange() throws ApiException {
        // Given - Create an order
        ClientPojo client = createTestClient("Test Client 12", "test12@example.com");
        ProductPojo product = createTestProduct("BC_ORDER12", client.getClientId());
        addInventory(product.getId(), 100);

        OrderItemPojo item = createOrderItem(product.getBarcode(), 10, 100.0);

        OrderCreationResult createResult = orderFlow.createOrder(Arrays.asList(item));

        // When - Filter by date range
        ZonedDateTime yesterday = ZonedDateTime.now().minusDays(1);
        ZonedDateTime tomorrow = ZonedDateTime.now().plusDays(1);
        List<OrderPojo> results = orderFlow.getOrderWithFilters(null, null, yesterday, tomorrow);

        // Then
        assertNotNull(results);
        assertTrue(results.size() > 0);
    }

    @Test
    void testGetOrderItems_Success() throws ApiException {
        // Given - Create an order
        ClientPojo client = createTestClient("Test Client 13", "test13@example.com");
        ProductPojo product = createTestProduct("BC_ORDER13", client.getClientId());
        addInventory(product.getId(), 100);

        OrderItemPojo item = createOrderItem(product.getBarcode(), 10, 100.0);

        OrderCreationResult createResult = orderFlow.createOrder(Arrays.asList(item));
        String orderId = createResult.getOrderId();

        // When
        List<OrderItemPojo> items = orderFlow.getOrderItems(orderId);

        // Then
        assertNotNull(items);
        assertEquals(1, items.size());
        assertEquals(10, items.get(0).getQuantity());
    }
}
