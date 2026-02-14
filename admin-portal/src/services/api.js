import axios from 'axios';

const API_BASE = import.meta.env.VITE_API_URL || '/api';

const api = axios.create({
  baseURL: API_BASE,
  headers: { 'Content-Type': 'application/json' },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('admin_accessToken');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

api.interceptors.response.use(
  (r) => r,
  (err) => {
    if (err.response?.status === 401) {
      localStorage.removeItem('admin_accessToken');
      localStorage.removeItem('admin_user');
      window.location.href = '/';
    }
    if (err.response?.status === 403) {
      localStorage.removeItem('admin_accessToken');
      localStorage.removeItem('admin_refreshToken');
      localStorage.removeItem('admin_user');
      sessionStorage.setItem('admin_403_message', 'Access denied. Your account may not have admin privileges.');
      window.location.href = '/';
    }
    return Promise.reject(err);
  }
);

export const authAPI = {
  login: (data) => api.post('/auth/admin-login', data),
};

export const adminAPI = {
  getDashboard: () => api.get('/admin/dashboard'),
  getUsers: (search, params) => api.get('/admin/users', { params: { ...params, search: search || undefined } }),
  suspendUser: (id) => api.put(`/admin/users/${id}/suspend`),
  unsuspendUser: (id) => api.put(`/admin/users/${id}/unsuspend`),
  lockUser: (id) => api.put(`/admin/users/${id}/lock`),
  unlockUser: (id) => api.put(`/admin/users/${id}/unlock`),
  getProducts: (params) => api.get('/admin/products', { params }),
  deactivateProduct: (id) => api.put(`/admin/products/${id}/deactivate`),
  deleteProduct: (id) => api.delete(`/admin/products/${id}`),
  getPendingCategoryRequests: () => api.get('/admin/category-requests/pending'),
  approveCategoryRequest: (id) => api.post(`/admin/category-requests/${id}/approve`),
  rejectCategoryRequest: (id, reviewNotes) => api.post(`/admin/category-requests/${id}/reject`, null, { params: { reviewNotes: reviewNotes || '' } }),
  createCategory: (data) => api.post('/admin/categories', data),
  updateCategory: (id, data) => api.put(`/admin/categories/${id}`, data),
  deactivateCategory: (id) => api.put(`/admin/categories/${id}/deactivate`),
};

export const categoryAPI = {
  getAll: () => api.get('/categories'),
};

export default api;
