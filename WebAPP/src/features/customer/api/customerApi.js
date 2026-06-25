import apiClient from '@/services/apiClient'
import { parseApiResponse, CustomerProfileSchema } from '@/schemas/dto'
export const customerApi = {
  getAllForAdmin: (page = 0, size = 20, search = '') =>
    apiClient.get(`/admin/customers?page=${page}&size=${size}${search ? `&search=${encodeURIComponent(search)}` : ''}`).then(r => r.data?.result ?? r.data),

  getById: (customerId) =>
    apiClient.get(`/admin/customers/${customerId}`).then(r => r.data),

  toggleAccountStatus: (customerId) =>
    apiClient.put(`/admin/customers/${customerId}/account-status`).then(r => r.data),

  getMyInfo: () => apiClient.get('/customers/my-info').then(r => parseApiResponse(CustomerProfileSchema, r.data)),


  updateMyInfo: (formData) =>
    apiClient
      .put('/customers/my-info', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      })
      .then((r) => parseApiResponse(CustomerProfileSchema, r.data)),
  
  deleteAvatar: () =>
    apiClient.delete('/customers/my-avatar').then((r) => r.data),

  register: (payload) =>
    apiClient.post('/customers/register', payload).then((r) => parseApiResponse(CustomerProfileSchema, r.data)),
}
