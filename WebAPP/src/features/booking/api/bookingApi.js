import apiClient from '@/services/apiClient'
import {
  parseApiResponse,
  parseApiArrayResponse,
  EstimatePriceSchema,
  BookingDetailSchema,
  AvailableRideSchema,
} from '@/schemas/dto'

export const bookingApi = {
  estimatePrice: (payload) =>
    apiClient.post('/bookings/estimate-price', payload).then((r) => parseApiArrayResponse(EstimatePriceSchema, r.data)),

  createBooking: (payload) =>
    apiClient.post('/bookings', payload).then((r) => parseApiResponse(BookingDetailSchema, r.data)),

  getAvailable: () =>
    apiClient.get('/bookings/available').then((r) => parseApiArrayResponse(AvailableRideSchema, r.data)),

  assignDriver: (bookingId, driverId) =>
    apiClient.put(`/bookings/${bookingId}/assign-driver?driverId=${driverId}`).then((r) => parseApiResponse(BookingDetailSchema, r.data)),

  rejectBooking: (bookingId, driverId) =>
    apiClient.post(`/bookings/${bookingId}/reject?driverId=${driverId}`).then((r) => r.data),

  updateStatus: (bookingId, status) =>
    apiClient.put(`/bookings/${bookingId}/status?status=${status}`).then((r) => parseApiResponse(BookingDetailSchema, r.data)),

  completeBooking: (bookingId) =>
    apiClient.put(`/bookings/${bookingId}/complete`).then((r) => parseApiResponse(BookingDetailSchema, r.data)),

  getCustomerHistory: (customerId) =>
    apiClient.get(`/bookings/customer/${customerId}`).then((r) => parseApiArrayResponse(BookingDetailSchema, r.data)),

  getDriverHistory: (driverId) =>
    apiClient.get(`/bookings/driver/${driverId}`).then((r) => parseApiArrayResponse(BookingDetailSchema, r.data)),

  getDriverHistoryPage: (driverId, status = 'ALL', page = 0, size = 10) =>
    apiClient.get(`/bookings/driver/${driverId}/page?status=${status}&page=${page}&size=${size}`).then((r) => r.data?.result),

  getAll: () =>
    apiClient.get('/bookings').then((r) => parseApiArrayResponse(BookingDetailSchema, r.data)),

  getById: (bookingId) =>
    apiClient.get(`/bookings/${bookingId}`).then((r) => parseApiResponse(BookingDetailSchema, r.data)),

  cancelBooking: (bookingId) =>
    apiClient.delete(`/bookings/${bookingId}`).then((r) => r.data),

  cancelBookingByDriver: (bookingId, driverId) =>
    apiClient.delete(`/bookings/${bookingId}/driver/${driverId}`).then((r) => r.data),

  getDriverLocation: (bookingId) =>
    apiClient.get(`/drivers/location/${bookingId}`).then((r) => r.data.result),
}
