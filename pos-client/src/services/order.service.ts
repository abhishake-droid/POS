import apiClient from './api.service';
import {
  CreateOrderForm,
  OrderData,
  OrderSearchFilters,
} from '../types/order.types';

export const orderService = {
  create: async (form: CreateOrderForm): Promise<OrderData> => {
    const response = await apiClient.post('/order/create', form);
    return response.data;
  },

  cancel: async (orderId: string): Promise<OrderData> => {
    const response = await apiClient.post(`/order/cancel/${orderId}`);
    return response.data;
  },

  getAll: async (
    page: number,
    size: number,
    filters: OrderSearchFilters
  ): Promise<{ content: OrderData[]; totalPages: number; totalElements: number }> => {
    try {
      const response = await apiClient.post('/order/get-all-paginated', {
        page,
        size,
        ...filters,
      });
      return response.data;
    } catch (error: any) {
      // Re-throw with more context
      if (!error.response) {
        error.message = error.message || 'Network error - backend may not be running';
      }
      throw error;
    }
  },

  getById: async (orderId: string): Promise<OrderData> => {
    const response = await apiClient.get(`/order/get-by-id/${orderId}`);
    return response.data;
  },

  generateInvoice: async (orderId: string): Promise<OrderData> => {
    const response = await apiClient.post(`/invoice/generate/${orderId}`);
    return response.data;
  },

  downloadInvoice: async (orderId: string): Promise<void> => {
    const response = await apiClient.get(`/invoice/download/${orderId}`, {
      responseType: 'blob',
    });
    const blob = new Blob([response.data], { type: 'application/pdf' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `invoice-${orderId}.pdf`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
  },

  update: async (orderId: string, form: CreateOrderForm): Promise<OrderData> => {
    const response = await apiClient.put(`/order/update/${orderId}`, form);
    return response.data;
  },

  retry: async (orderId: string, form?: CreateOrderForm): Promise<OrderData> => {
    const response = await apiClient.post(`/order/retry/${orderId}`, form || null);
    return response.data;
  },
};

