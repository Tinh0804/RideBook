import apiClient from '@/services/apiClient'
import { AuthenticationResponseSchema, parseApiResponse } from '@/schemas/dto'

export const authApi = {
  login: (payload) =>
    apiClient.post('/auth/login', payload).then((r) => parseApiResponse(AuthenticationResponseSchema, r.data)),

  logout: (refreshToken) =>
    apiClient.post(`/auth/logout?refreshToken=${refreshToken}`).then((r) => r.data),

  refreshToken: (refreshToken) =>
    apiClient.post(`/auth/refresh-token?refreshToken=${refreshToken}`).then((r) => parseApiResponse(AuthenticationResponseSchema, r.data)),

  resetPassword: (payload) =>
    apiClient.put('/auth/reset-password', payload).then((r) => r.data),

  checkPhone: (phone) =>
    apiClient.get(`/auth/check-phone?phone=${phone}`).then((r) => r.data),

  introspect: (token) =>
    apiClient.post('/auth/introspect', { token }).then((r) => r.data),

  oauthLogin: (payload) =>
    apiClient.post('/auth/oauth2/external-login', payload).then((r) => r.data),
}
