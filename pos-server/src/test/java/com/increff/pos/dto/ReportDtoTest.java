package com.increff.pos.dto;

import com.increff.pos.api.*;
import com.increff.pos.db.*;
import com.increff.pos.exception.ApiException;
import com.increff.pos.flow.ReportFlow;
import com.increff.pos.model.data.ClientSalesReportData;
import com.increff.pos.model.data.DailySalesData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportDtoTest {

    @Mock
    private DailySalesApi dailySalesApi;

    @Mock
    private ReportFlow reportFlow;

    @InjectMocks
    private ReportDto reportDto;

    private DailySalesPojo dailySalesPojo;
    private ClientSalesReportData clientSalesReportData;

    @BeforeEach
    void setUp() {
        dailySalesPojo = new DailySalesPojo();
        dailySalesPojo.setId("sales1");
        dailySalesPojo.setDate(LocalDate.now());
        dailySalesPojo.setClientId("client1");
        dailySalesPojo.setClientName("Test Client");
        dailySalesPojo.setInvoicedOrdersCount(5);
        dailySalesPojo.setInvoicedItemsCount(50);
        dailySalesPojo.setTotalRevenue(5000.0);

        clientSalesReportData = new ClientSalesReportData();
        clientSalesReportData.setClientId("client1");
        clientSalesReportData.setClientName("Test Client");
        clientSalesReportData.setTotalRevenue(1000.0);
        clientSalesReportData.setTotalQuantity(10);
        clientSalesReportData.setInvoicedOrdersCount(1);
    }

    @Test
    void testGetDailySalesReport_Success() throws ApiException {
        // Given
        when(dailySalesApi.getByDate(any(LocalDate.class))).thenReturn(Arrays.asList(dailySalesPojo));

        // When
        List<DailySalesData> result = reportDto.getDailySalesReport(LocalDate.now().toString(), null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Client", result.get(0).getClientName());
    }

    @Test
    void testGetDailySalesReport_WithClientId() throws ApiException {
        // Given
        when(dailySalesApi.getByDateAndClient(any(LocalDate.class), eq("client1"))).thenReturn(dailySalesPojo);

        // When
        List<DailySalesData> result = reportDto.getDailySalesReport(LocalDate.now().toString(), "client1");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetDailySalesReport_InvalidDate() {
        // When/Then
        assertThrows(ApiException.class, () -> reportDto.getDailySalesReport("invalid-date", null));
    }

    @Test
    void testGetSalesReport_Success() throws ApiException {
        // Given
        when(reportFlow.generateSalesReport(any(ZonedDateTime.class), any(ZonedDateTime.class), isNull()))
                .thenReturn(Arrays.asList(clientSalesReportData));

        // When
        List<ClientSalesReportData> result = reportDto.getSalesReport(
                LocalDate.now().minusDays(7).toString(),
                LocalDate.now().toString(),
                null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Client", result.get(0).getClientName());
        verify(reportFlow).generateSalesReport(any(ZonedDateTime.class), any(ZonedDateTime.class), isNull());
    }

    @Test
    void testGetSalesReport_MissingDates() {
        // When/Then
        assertThrows(ApiException.class, () -> reportDto.getSalesReport(null, null, null));
    }

    @Test
    void testGetSalesReport_InvalidDateRange() {
        // When/Then
        assertThrows(ApiException.class, () -> reportDto.getSalesReport(
                LocalDate.now().toString(),
                LocalDate.now().minusDays(7).toString(),
                null));
    }
}
