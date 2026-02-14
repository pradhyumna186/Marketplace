import axios from 'axios';

// Use relative /api in dev so Vite proxy forwards to backend (same-origin = Authorization header sent).
// Set VITE_API_URL to full URL (e.g. http://localhost:8080/api) if not using proxy.
const API_BASE_URL = import.meta.env.VITE_API_URL || '/api';

// Create axios instance
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add token to requests
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Auth endpoints
export const authAPI = {
  register: (data) => api.post('/auth/register', data),
  login: (data) => api.post('/auth/login', data),
  verifyEmail: (token) => api.get(`/auth/verify-email?token=${token}`),
  resendVerification: (email) => api.post(`/auth/resend-verification?email=${email}`),
  getTrustedDevices: () => api.get('/auth/trusted-devices'),
  revokeDevice: (deviceId) => api.delete(`/auth/trusted-devices/${deviceId}`),
  logout: () => api.post('/auth/logout'),
  logoutAll: () => api.post('/auth/logout-all'),
  deleteAccount: () => api.delete('/auth/account'),
  refreshToken: (refreshToken) => api.post('/auth/refresh-token', null, {
    headers: { 'Refresh-Token': refreshToken }
  }),
  forgotPassword: (email) => api.post(`/auth/forgot-password?email=${email}`),
  resetPassword: (token, newPassword) => api.post('/auth/reset-password', { token, newPassword }),
  updateProfile: (data) => api.put('/auth/profile', data),
};

// Category endpoints
export const categoryAPI = {
  getAll: () => api.get('/categories'),
  getById: (id) => api.get(`/categories/${id}`),
  requestCategory: (data) => api.post('/categories/request', data),
  getMyRequests: () => api.get('/categories/my-requests'),
  search: (keyword) => api.get(`/categories/search?keyword=${keyword}`),
};

// Product endpoints
export const productAPI = {
  create: (data) => api.post('/products', data),
  getAll: (params) => api.get('/products', { params }),
  getById: (id) => api.get(`/products/${id}`),
  update: (id, data) => api.put(`/products/${id}`, data),
  delete: (id) => api.delete(`/products/${id}`),
  getByCategory: (categoryId, params) => api.get(`/products/category/${categoryId}`, { params }),
  search: (keyword, params) => api.get(`/products/search?keyword=${keyword}`, { params }),
  filter: (params) => api.get('/products/filter', { params }),
  getByBuilding: (building, params) => api.get(`/products/building/${building}`, { params }),
  getMyProducts: (params) => api.get('/products/my-products', { params }),
  markSold: (id, buyerId, soldPrice) => api.post(`/products/${id}/mark-sold?buyerId=${buyerId}&soldPrice=${soldPrice}`),
  getTrending: (limit) => api.get(`/products/trending?limit=${limit || 10}`),
  getRecent: (limit) => api.get(`/products/recent?limit=${limit || 10}`),
};

// Chat endpoints
export const chatAPI = {
  startChat: (productId) => api.post(`/chats/start/${productId}`),
  getAll: (params) => api.get('/chats', { params }),
  getById: (id) => api.get(`/chats/${id}`),
  getMessages: (id, params) => api.get(`/chats/${id}/messages`, { params }),
  sendMessage: (id, data) => api.post(`/chats/${id}/messages`, data),
  markRead: (id) => api.post(`/chats/${id}/mark-read`),
  closeChat: (id) => api.post(`/chats/${id}/close`),
  getUnread: () => api.get('/chats/unread'),
};

// Negotiation endpoints
export const negotiationAPI = {
  makeOffer: (chatId, data) => api.post(`/negotiations/chats/${chatId}/offer`, data),
  acceptOffer: (id) => api.post(`/negotiations/${id}/accept`),
  rejectOffer: (id, reason) => api.post(`/negotiations/${id}/reject?reason=${reason || ''}`),
  getChatNegotiations: (chatId) => api.get(`/negotiations/chats/${chatId}`),
  getPendingOffers: () => api.get('/negotiations/pending-offers'),
};

// File upload endpoints (do not set Content-Type so browser adds boundary for multipart)
// Explicitly pass Authorization so it's never dropped with FormData
function uploadHeaders() {
  const token = localStorage.getItem('accessToken');
  const headers = {};
  if (token) headers.Authorization = `Bearer ${token}`;
  return headers;
}

export const fileAPI = {
  uploadProductImages: (files) => {
    const formData = new FormData();
    Array.from(files).forEach(file => {
      formData.append('files', file);
    });
    return api.post('/files/upload/product-images', formData, { headers: uploadHeaders() });
  },
  uploadCategoryIcon: (file) => {
    const formData = new FormData();
    formData.append('file', file);
    return api.post('/files/upload/category-icon', formData, { headers: uploadHeaders() });
  },
};

// Admin endpoints
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

export default api;

