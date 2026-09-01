import apiClient from '@/services/apiClient'
import { parseApiResponse, CustomerProfileSchema } from '@/schemas/dto'
export const customerApi = {
  getAllForAdmin: (pageOrOptions = 0, size = 20, search = '') => {
    let queryParams
    if (typeof pageOrOptions === 'object' && pageOrOptions !== null) {
      queryParams = new URLSearchParams()
      Object.entries(pageOrOptions).forEach(([k, v]) => {
        if (v !== undefined && v !== null && v !== '' && v !== 'ALL') {
          queryParams.append(k, v)
        }
      })
    } else {
      queryParams = new URLSearchParams({ page: pageOrOptions, size })
      if (search) queryParams.append('search', search)
    }
    return apiClient.get(`/admin/customers?${queryParams}`).then(r => r.data?.result ?? r.data)
  },

  exportCustomers: (options = {}) => {
    const params = new URLSearchParams()
    Object.entries(options).forEach(([k, v]) => {
      if (v !== undefined && v !== null && v !== '' && v !== 'ALL') {
        params.append(k, v)
      }
    })
    return apiClient.get(`/admin/customers/export?${params}`, { responseType: 'blob' })
  },

  getById: (customerId) =>
    apiClient.get(`/admin/customers/${customerId}`).then(r => r.data),

  toggleAccountStatus: (customerId) =>
    apiClient.put(`/admin/customers/${customerId}/account-status`).then(r => r.data),

  updateCustomerInfo: (customerId, payload) =>
    apiClient.put(`/admin/customers/${customerId}`, payload, {
      headers: { 'Content-Type': 'multipart/form-data' },
    }).then(r => r.data),

  changeCustomerPassword: (customerId, data) =>
    apiClient.put(`/admin/customers/${customerId}/password`, data).then(r => r.data),

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
