import apiClient from './api.service';
import { ClientData, ClientForm } from '../types/client.types';

export const clientService = {
  getAll: async (page: number, size: number): Promise<any> => {
    const response = await apiClient.post('/client/get-all-paginated', { page, size });
    return response.data;
  },

  getById: async (id: string): Promise<ClientData> => {
    const response = await apiClient.get(`/client/get-by-id/${id}`);
    return response.data;
  },

  create: async (form: ClientForm): Promise<ClientData> => {
    const response = await apiClient.post('/client/add', form);
    return response.data;
  },

  update: async (id: string, form: ClientForm): Promise<ClientData> => {
    const response = await apiClient.put(`/client/update/${id}`, form);
    return response.data;
  },
};
