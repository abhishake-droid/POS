import apiClient from './api.service';
import { AuditLogData } from '../types/operator.types';

export const auditLogService = {
  getAll: async (page: number, size: number): Promise<any> => {
    const response = await apiClient.post('/audit-log/get-all-paginated', { page, size });
    return response.data;
  },

  getAllList: async (): Promise<AuditLogData[]> => {
    const response = await apiClient.get('/audit-log/get-all');
    return response.data;
  },

  getByOperator: async (operatorEmail: string): Promise<AuditLogData[]> => {
    const response = await apiClient.get(`/audit-log/get-by-operator/${operatorEmail}`);
    return response.data;
  },
};
