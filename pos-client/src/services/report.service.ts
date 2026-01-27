import apiClient from './api.service';
import { SalesReportRow } from '../types/order.types';

export const reportService = {
  getSalesReport: async (
    fromDate: string,
    toDate: string,
    brand?: string
  ): Promise<SalesReportRow[]> => {
    const response = await apiClient.get('/report/sales', {
      params: { fromDate, toDate, brand },
    });
    return response.data;
  },
};

