import apiClient from './api.service';
import { SalesReportRow } from '../types/order.types';
import { DailySalesData, ClientSalesReport } from '../types/report.types';

export const reportService = {
  /**
   * Get detailed sales report with client and product aggregation
   */
  getSalesReport: async (
    fromDate: string,
    toDate: string,
    clientId?: string
  ): Promise<ClientSalesReport[]> => {
    const response = await apiClient.get('/report/sales-report', {
      params: { fromDate, toDate, clientId },
    });
    return response.data;
  },

  /**
   * Get daily aggregated sales report
   */
  getDailySales: async (
    date?: string,
    clientId?: string
  ): Promise<DailySalesData[]> => {
    const response = await apiClient.get('/report/daily-sales', {
      params: { date, clientId },
    });
    return response.data;
  },
};
