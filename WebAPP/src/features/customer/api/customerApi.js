import apiClient from '@/services/apiClient'
import { parseApiResponse, parseApiArrayResponse, CustomerProfileSchema } from '@/schemas/dto'

export const customerApi = {
  register: (payload) =>
    apiClient.post('/customers/register', payload).then((r) => parseApiResponse(CustomerProfileSchema, r.data)),

  getMyInfo: () =>
    apiClient.get('/customers/my-info').then((r) => parseApiResponse(CustomerProfileSchema, r.data)),

  updateMyInfo: (formData) =>
    apiClient
      .put('/customers/my-info', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      })
      .then((r) => parseApiResponse(CustomerProfileSchema, r.data)),

  deleteAvatar: () =>
    apiClient.delete('/customers/my-avatar').then((r) => r.data),

  getAll: () =>
    apiClient.get('/customers').then((r) => parseApiArrayResponse(CustomerProfileSchema, r.data)),

  
}
