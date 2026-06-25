import apiClient from '@/services/apiClient'
import {
  parseApiResponse,
  parseApiArrayResponse,
  RatingSchema,
  NotificationSchema,
  VehicleTypeSchema,
  PromotionSchema,
} from '@/schemas/dto'

export const ratingApi = {
  create: (payload) =>
    apiClient.post('/ratings', payload).then((r) => parseApiResponse(RatingSchema, r.data)),

  getByDriver: (driverId) =>
    apiClient.get(`/ratings/driver/${driverId}`).then((r) => parseApiArrayResponse(RatingSchema, r.data)),

  getByCustomer: (customerId) =>
    apiClient.get(`/ratings/customer/${customerId}`).then((r) => parseApiArrayResponse(RatingSchema, r.data)),

  
}

export const notificationApi = {
  getAll: () =>
    apiClient.get('/notifications').then((r) => parseApiArrayResponse(NotificationSchema, r.data)),

  markRead: (id) =>
    apiClient.put(`/notifications/${id}/read`).then((r) => parseApiResponse(NotificationSchema, r.data)),
}

export const masterDataApi = {
  getVehicleTypes: () =>
    apiClient.get('/vehicle-types').then((r) => parseApiArrayResponse(VehicleTypeSchema, r.data)),

  getActivePromotions: () =>
    apiClient.get('/promotions/active').then((r) => parseApiArrayResponse(PromotionSchema, r.data)),

  getPromotionByCode: (code) =>
    apiClient.get(`/promotions/${code}`).then((r) => parseApiResponse(PromotionSchema, r.data)),

  savePromotion: (customerId, code) =>
    apiClient.post(`/promotions/customer/${customerId}/save/${code}`),

  getMyPromotions: (customerId) =>
    apiClient.get(`/promotions/customer/${customerId}`).then((r) => parseApiArrayResponse(PromotionSchema, r.data)),

  createPromotion: (payload) =>
    apiClient.post('/promotions', payload).then((r) => parseApiResponse(PromotionSchema, r.data)),

  // Admin-only methods
  getAllPromotionsForAdmin: () =>
    apiClient.get('/promotions').then((r) => parseApiArrayResponse(PromotionSchema, r.data)),

  updatePromotion: (id, payload) =>
    apiClient.put(`/promotions/${id}`, payload).then((r) => parseApiResponse(PromotionSchema, r.data)),

  deletePromotion: (id) =>
    apiClient.delete(`/promotions/${id}`).then((r) => r.data),

  togglePromotion: (id) =>
    apiClient.patch(`/promotions/${id}/toggle`).then((r) => parseApiResponse(PromotionSchema, r.data)),

  toggleVisibility: (id) =>
    apiClient.patch(`/promotions/${id}/toggle-visibility`).then((r) => parseApiResponse(PromotionSchema, r.data)),
}
