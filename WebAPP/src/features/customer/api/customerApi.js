import apiClient from '@/services/apiClient'

export const customerApi = {
  getAllForAdmin: (page = 0, size = 20, search = '') =>
    apiClient.get(`/admin/customers?page=${page}&size=${size}${search ? `&search=${encodeURIComponent(search)}` : ''}`).then(r => r.data),

  getById: (customerId) =>
    apiClient.get(`/admin/customers/${customerId}`).then(r => r.data),

  toggleAccountStatus: (customerId) =>
    apiClient.put(`/admin/customers/${customerId}/account-status`).then(r => r.data),
}
