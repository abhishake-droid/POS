import apiClient from './api.service';
import { LoginForm, AuthData } from '../types/auth.types';

export const authService = {
  async login(form: LoginForm): Promise<AuthData> {
    const response = await apiClient.post<AuthData>('/auth/login', form);
    return response.data;
  },

  async validateToken(token: string): Promise<AuthData> {
    const response = await apiClient.post<AuthData>('/auth/validate', { token });
    return response.data;
  },

  async logout(email: string): Promise<void> {
    await apiClient.post('/auth/logout', { email });
  },
};