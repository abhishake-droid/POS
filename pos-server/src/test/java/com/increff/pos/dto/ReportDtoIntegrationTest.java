package com.increff.pos.dto;

import com.increff.pos.api.*;
import com.increff.pos.db.*;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.ClientSalesReportData;
import com.increff.pos.model.data.DailySalesData;
import com.increff.pos.test.AbstractUnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReportDtoIntegrationTest extends AbstractUnitTest {

    @Autowired
    private ReportDto reportDto;

    @Autowired
    private DailySalesApi dailySalesApi;

    @Autowired
    private OrderApi orderApi;

    @Autowired
    private OrderItemApi orderItemApi;

    @Autowired
    private ProductApi productApi;

    @Autowired
    private ClientApi clientApi;

    @Test
    void testGetDailySalesReport_WithRealData() throws ApiException {
        // Given - Create real data
        LocalDate today = LocalDate.now();

        DailySalesPojo sales = new DailySalesPojo();
        sales.setDate(today);
        sales.setClientId("C001");
        sales.setClientName("Test Client");
        sales.setInvoicedOrdersCount(5);
        sales.setInvoicedItemsCount(50);
        sales.setTotalRevenue(5000.0);
        dailySalesApi.add(sales);

        // When
        List<DailySalesData> result = reportDto.getDailySalesReport(today.toString(), null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Client", result.get(0).getClientName());
        assertEquals(5000.0, result.get(0).getTotalRevenue());
    }

    @Test
    void testGetDailySalesReport_WithClientFilter() throws ApiException {
        // Given
        LocalDate today = LocalDate.now();

        DailySalesPojo sales1 = new DailySalesPojo();
        sales1.setDate(today);
        sales1.setClientId("C001");
        sales1.setClientName("Client 1");
        sales1.setInvoicedOrdersCount(3);
        sales1.setInvoicedItemsCount(30);
        sales1.setTotalRevenue(3000.0);
        dailySalesApi.add(sales1);

        DailySalesPojo sales2 = new DailySalesPojo();
        sales2.setDate(today);
        sales2.setClientId("C002");
        sales2.setClientName("Client 2");
        sales2.setInvoicedOrdersCount(2);
        sales2.setInvoicedItemsCount(20);
        sales2.setTotalRevenue(2000.0);
        dailySalesApi.add(sales2);

        // When
        List<DailySalesData> result = reportDto.getDailySalesReport(today.toString(), "C001");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Client 1", result.get(0).getClientName());
    }

    @Test
    void testGetSalesReport_WithRealOrders() throws ApiException {
        // Given - Create client
        ClientPojo client = new ClientPojo();
        client.setClientId("C003");
        client.setName("Test Client");
        client.setEmail("test@example.com");
        client.setPhone("1234567890");
        client = clientApi.add(client);

        // Create product
        ProductPojo product = new ProductPojo();
        product.setBarcode("BC001");
        product.setClientId(client.getClientId());
        product.setName("Test Product");
        product.setMrp(100.0);
        product = productApi.add(product);

        // Create order
        OrderPojo order = new OrderPojo();
        order.setOrderId("ORD001");
        order.setStatus("INVOICED");
        order.setTotalItems(10);
        order.setTotalAmount(1000.0);
        order.setOrderDate(ZonedDateTime.now());
        order = orderApi.add(order);

        // Create order item
        OrderItemPojo orderItem = new OrderItemPojo();
        orderItem.setOrderId(order.getOrderId());
        orderItem.setProductId(product.getId());
        orderItem.setBarcode(product.getBarcode());
        orderItem.setProductName(product.getName());
        orderItem.setQuantity(10);
        orderItem.setMrp(100.0);
        orderItem.setLineTotal(1000.0);
        orderItemApi.add(orderItem);

        // When
        LocalDate today = LocalDate.now();
        List<ClientSalesReportData> result = reportDto.getSalesReport(
                today.minusDays(1).toString(),
                today.plusDays(1).toString(),
                null);

        // Then
        assertNotNull(result);
        assertTrue(result.size() > 0);
        assertEquals("Test Client", result.get(0).getClientName());
        assertEquals(1000.0, result.get(0).getTotalRevenue());
    }

    @Test
    void testGetSalesReport_EmptyResults() throws ApiException {
        // When - Query for future dates
        LocalDate futureDate = LocalDate.now().plusDays(30);
        List<ClientSalesReportData> result = reportDto.getSalesReport(
                futureDate.toString(),
                futureDate.plusDays(1).toString(),
                null);

        // Then
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testGetDailySalesReport_NoData() throws ApiException {
        // When - Query for future date
        LocalDate futureDate = LocalDate.now().plusDays(30);
        List<DailySalesData> result = reportDto.getDailySalesReport(futureDate.toString(), null);

        // Then
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testGetSalesReport_WithClientFilter() throws ApiException {
        // Given - Create two clients
        ClientPojo client1 = new ClientPojo();
        client1.setClientId("C004");
        client1.setName("Client 1");
        client1.setEmail("client1@example.com");
        client1.setPhone("1111111111");
        client1 = clientApi.add(client1);

        ClientPojo client2 = new ClientPojo();
        client2.setClientId("C005");
        client2.setName("Client 2");
        client2.setEmail("client2@example.com");
        client2.setPhone("2222222222");
        client2 = clientApi.add(client2);

        // Create products for each client
        ProductPojo product1 = new ProductPojo();
        product1.setBarcode("BC002");
        product1.setClientId(client1.getClientId());
        product1.setName("Product 1");
        product1.setMrp(50.0);
        product1 = productApi.add(product1);

        ProductPojo product2 = new ProductPojo();
        product2.setBarcode("BC003");
        product2.setClientId(client2.getClientId());
        product2.setName("Product 2");
        product2.setMrp(75.0);
        product2 = productApi.add(product2);

        // Create orders for each client
        OrderPojo order1 = new OrderPojo();
        order1.setOrderId("ORD002");
        order1.setStatus("INVOICED");
        order1.setTotalItems(5);
        order1.setTotalAmount(250.0);
        order1.setOrderDate(ZonedDateTime.now());
        order1 = orderApi.add(order1);

        OrderItemPojo orderItem1 = new OrderItemPojo();
        orderItem1.setOrderId(order1.getOrderId());
        orderItem1.setProductId(product1.getId());
        orderItem1.setBarcode(product1.getBarcode());
        orderItem1.setProductName(product1.getName());
        orderItem1.setQuantity(5);
        orderItem1.setMrp(50.0);
        orderItem1.setLineTotal(250.0);
        orderItemApi.add(orderItem1);

        // When - Filter by client1
        LocalDate today = LocalDate.now();
        List<ClientSalesReportData> result = reportDto.getSalesReport(
                today.minusDays(1).toString(),
                today.plusDays(1).toString(),
                client1.getClientId());

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Client 1", result.get(0).getClientName());
        assertEquals(250.0, result.get(0).getTotalRevenue());
    }

    @Test
    void testGetDailySalesReport_NullDate() throws ApiException {
        // Given - Create data for today
        LocalDate today = LocalDate.now();

        DailySalesPojo sales = new DailySalesPojo();
        sales.setDate(today);
        sales.setClientId("C006");
        sales.setClientName("Today Client");
        sales.setInvoicedOrdersCount(1);
        sales.setInvoicedItemsCount(10);
        sales.setTotalRevenue(1000.0);
        dailySalesApi.add(sales);

        // When - Pass null date (should default to today)
        List<DailySalesData> result = reportDto.getDailySalesReport(null, null);

        // Then
        assertNotNull(result);
        assertTrue(result.size() > 0);
    }

    @Test
    void testGetDailySalesReport_EmptyDate() throws ApiException {
        // Given - Create data for today
        LocalDate today = LocalDate.now();

        DailySalesPojo sales = new DailySalesPojo();
        sales.setDate(today);
        sales.setClientId("C007");
        sales.setClientName("Empty Date Client");
        sales.setInvoicedOrdersCount(1);
        sales.setInvoicedItemsCount(10);
        sales.setTotalRevenue(1000.0);
        dailySalesApi.add(sales);

        // When - Pass empty string (should default to today)
        List<DailySalesData> result = reportDto.getDailySalesReport("", null);

        // Then
        assertNotNull(result);
        assertTrue(result.size() > 0);
    }

    @Test
    void testGetSalesReport_ProductNotFound() throws ApiException {
        // Given - Create order with item but no product
        OrderPojo order = new OrderPojo();
        order.setOrderId("ORD003");
        order.setStatus("INVOICED");
        order.setTotalItems(1);
        order.setTotalAmount(100.0);
        order.setOrderDate(ZonedDateTime.now());
        order = orderApi.add(order);

        OrderItemPojo orderItem = new OrderItemPojo();
        orderItem.setOrderId(order.getOrderId());
        orderItem.setProductId("NONEXISTENT_PRODUCT");
        orderItem.setBarcode("BC999");
        orderItem.setProductName("Ghost Product");
        orderItem.setQuantity(1);
        orderItem.setMrp(100.0);
        orderItem.setLineTotal(100.0);
        orderItemApi.add(orderItem);

        // When
        LocalDate today = LocalDate.now();
        List<ClientSalesReportData> result = reportDto.getSalesReport(
                today.minusDays(1).toString(),
                today.plusDays(1).toString(),
                null);

        // Then - Should handle missing product gracefully
        assertNotNull(result);
        // Product not found should be skipped
    }

    @Test
    void testGetSalesReport_ClientNotFound() throws ApiException {
        // Given - Create client and product
        ClientPojo client = new ClientPojo();
        client.setClientId("C008");
        client.setName("Temp Client");
        client.setEmail("temp@example.com");
        client.setPhone("8888888888");
        client = clientApi.add(client);

        ProductPojo product = new ProductPojo();
        product.setBarcode("BC004");
        product.setClientId("NONEXISTENT_CLIENT");
        product.setName("Orphan Product");
        product.setMrp(50.0);
        product = productApi.add(product);

        OrderPojo order = new OrderPojo();
        order.setOrderId("ORD004");
        order.setStatus("INVOICED");
        order.setTotalItems(1);
        order.setTotalAmount(50.0);
        order.setOrderDate(ZonedDateTime.now());
        order = orderApi.add(order);

        OrderItemPojo orderItem = new OrderItemPojo();
        orderItem.setOrderId(order.getOrderId());
        orderItem.setProductId(product.getId());
        orderItem.setBarcode(product.getBarcode());
        orderItem.setProductName(product.getName());
        orderItem.setQuantity(1);
        orderItem.setMrp(50.0);
        orderItem.setLineTotal(50.0);
        orderItemApi.add(orderItem);

        // When
        LocalDate today = LocalDate.now();
        List<ClientSalesReportData> result = reportDto.getSalesReport(
                today.minusDays(1).toString(),
                today.plusDays(1).toString(),
                null);

        // Then - Should use "Unknown" for missing client
        assertNotNull(result);
        assertTrue(result.size() > 0);
        boolean hasUnknown = result.stream()
                .anyMatch(r -> "Unknown".equals(r.getClientName()));
        assertTrue(hasUnknown);
    }

    @Test
    void testGetSalesReport_MultipleProductsSameClient() throws ApiException {
        // Given - Create client with multiple products
        ClientPojo client = new ClientPojo();
        client.setClientId("C009");
        client.setName("Multi Product Client");
        client.setEmail("multi@example.com");
        client.setPhone("9999999999");
        client = clientApi.add(client);

        ProductPojo product1 = new ProductPojo();
        product1.setBarcode("BC005");
        product1.setClientId(client.getClientId());
        product1.setName("Product A");
        product1.setMrp(100.0);
        product1 = productApi.add(product1);

        ProductPojo product2 = new ProductPojo();
        product2.setBarcode("BC006");
        product2.setClientId(client.getClientId());
        product2.setName("Product B");
        product2.setMrp(200.0);
        product2 = productApi.add(product2);

        OrderPojo order = new OrderPojo();
        order.setOrderId("ORD005");
        order.setStatus("INVOICED");
        order.setTotalItems(2);
        order.setTotalAmount(300.0);
        order.setOrderDate(ZonedDateTime.now());
        order = orderApi.add(order);

        OrderItemPojo item1 = new OrderItemPojo();
        item1.setOrderId(order.getOrderId());
        item1.setProductId(product1.getId());
        item1.setBarcode(product1.getBarcode());
        item1.setProductName(product1.getName());
        item1.setQuantity(1);
        item1.setMrp(100.0);
        item1.setLineTotal(100.0);
        orderItemApi.add(item1);

        OrderItemPojo item2 = new OrderItemPojo();
        item2.setOrderId(order.getOrderId());
        item2.setProductId(product2.getId());
        item2.setBarcode(product2.getBarcode());
        item2.setProductName(product2.getName());
        item2.setQuantity(1);
        item2.setMrp(200.0);
        item2.setLineTotal(200.0);
        orderItemApi.add(item2);

        // When
        LocalDate today = LocalDate.now();
        List<ClientSalesReportData> result = reportDto.getSalesReport(
                today.minusDays(1).toString(),
                today.plusDays(1).toString(),
                null);

        // Then - Should aggregate both products
        assertNotNull(result);
        ClientSalesReportData clientReport = result.stream()
                .filter(r -> "Multi Product Client".equals(r.getClientName()))
                .findFirst()
                .orElse(null);
        assertNotNull(clientReport);
        assertEquals(2, clientReport.getProducts().size());
        assertEquals(300.0, clientReport.getTotalRevenue());
        assertEquals(100.0, clientReport.getMinPrice());
        assertEquals(200.0, clientReport.getMaxPrice());
        assertEquals(150.0, clientReport.getAvgPrice());
    }

    @Test
    void testGetSalesReport_EmptyClientId() throws ApiException {
        // When - Pass empty client ID (should return all clients)
        LocalDate today = LocalDate.now();
        List<ClientSalesReportData> result = reportDto.getSalesReport(
                today.minusDays(1).toString(),
                today.plusDays(1).toString(),
                "");

        // Then
        assertNotNull(result);
    }

    @Test
    void testGetDailySalesReport_EmptyClientId() throws ApiException {
        // When - Pass empty client ID (should return all clients)
        LocalDate today = LocalDate.now();
        List<DailySalesData> result = reportDto.getDailySalesReport(today.toString(), "");

        // Then
        assertNotNull(result);
    }
}
