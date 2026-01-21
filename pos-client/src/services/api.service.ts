import axios from 'axios';

// Use relative path for Next.js rewrites to work
// Next.js will proxy /api/* to http://localhost:8080/api/*
const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || '/api';

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

import { authStorage } from '../utils/authStorage';

// Request interceptor to add auth token
apiClient.interceptors.request.use(
  (config) => {
    const token = authStorage.getToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor for error handling
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.code === 'ECONNREFUSED' || error.message?.includes('Network Error')) {
      error.message = 'Cannot connect to backend. Please ensure the server is running on http://localhost:8080';
    }
    // Handle 401 unauthorized
    if (error.response?.status === 401) {
      authStorage.clearAll();
      if (typeof window !== 'undefined' && window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

export default apiClient;
