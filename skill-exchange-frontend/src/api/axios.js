import axios from 'axios';
import toast from 'react-hot-toast';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '',
  headers: { 'Content-Type': 'application/json' }
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('skillSwapToken');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('skillSwapToken');
      localStorage.removeItem('skillSwapUser');
      if (!window.location.pathname.includes('/login')) window.location.href = '/login';
    } else if (!error.response) {
      toast.error('Connection lost. Please check your internet');
    }
    return Promise.reject(error);
  }
);

export const unwrap = (response) => response.data?.data;
export const pageContent = (payload) => payload?.content ?? payload ?? [];
export default api;