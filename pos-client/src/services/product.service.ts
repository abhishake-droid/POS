import apiClient from './api.service';
import { ProductData, ProductForm, InventoryForm, InventoryData } from '../types/product.types';

export const productService = {
  getAll: async (page: number, size: number): Promise<any> => {
    const response = await apiClient.post('/product/get-all-paginated', { page, size });
    return response.data;
  },

  getByBarcode: async (barcode: string): Promise<ProductData> => {
    const response = await apiClient.get(`/product/get-by-barcode/${barcode}`);
    return response.data;
  },


  create: async (form: ProductForm): Promise<ProductData> => {
    const response = await apiClient.post('/product/add', form);
    return response.data;
  },

  update: async (id: string, form: ProductForm): Promise<ProductData> => {
    const response = await apiClient.put(`/product/update/${id}`, form);
    return response.data;
  },

  updateInventory: async (productId: string, form: InventoryForm): Promise<InventoryData> => {
    const response = await apiClient.put(`/inventory/${productId}`, form);
    return response.data;
  },



  uploadProductsTsvWithResults: async (base64Content: string): Promise<string> => {
    const response = await apiClient.post('/product/upload-products-with-results', base64Content, {
      headers: { 'Content-Type': 'text/plain' },
    });
    return response.data;
  },

  uploadInventoryTsvWithResults: async (base64Content: string): Promise<string> => {
    const response = await apiClient.post('/inventory/upload-with-results', base64Content, {
      headers: { 'Content-Type': 'text/plain' },
    });
    return response.data;
  },
};
