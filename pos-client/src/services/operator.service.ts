import apiClient from './api.service';
import { OperatorData, OperatorForm } from '../types/operator.types';

export const operatorService = {
  getAll: async (page: number, size: number): Promise<any> => {
    const response = await apiClient.post('/user/get-all-paginated', { page, size });
    return response.data;
  },

  getById: async (id: string): Promise<OperatorData> => {
    const response = await apiClient.get(`/user/get-by-id/${id}`);
    return response.data;
  },

  create: async (form: OperatorForm): Promise<OperatorData> => {
    const response = await apiClient.post('/user/add', form);
    return response.data;
  },
};
