import apiClient from '@/services/apiClient'
import {
  parseApiResponse,
  parseApiArrayResponse,
  DriverProfileSchema,
  DriverDashboardSchema,
  DriverRevenueSchema,
  DailyRevenueSchema,
} from '@/schemas/dto'

export const driverApi = {
  register: (formData) =>
    apiClient
      .post('/drivers/register', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      })
      .then((r) => parseApiResponse(DriverProfileSchema, r.data)),

  getDashboard: () =>
    apiClient.get('/drivers/my-dashboard').then((r) => parseApiResponse(DriverDashboardSchema, r.data)),

  getRevenue: (period = 'week') =>
    apiClient.get(`/drivers/my-revenue?period=${period}`).then((r) => parseApiResponse(DriverRevenueSchema, r.data)),

  getDailyRevenue: (date) =>
    apiClient.get(`/drivers/my-revenue/daily${date ? `?date=${date}` : ''}`).then((r) => parseApiResponse(DailyRevenueSchema, r.data)),

  toggleStatus: () =>
    apiClient.put('/drivers/status-activity').then((r) => r.data),

  getMyInfo: () =>
    apiClient.get('/drivers/my-info').then((r) => parseApiResponse(DriverProfileSchema, r.data)),

  updateDriver: (driverId, payload) =>
    apiClient.put(`/drivers/${driverId}`, payload).then((r) => parseApiResponse(DriverProfileSchema, r.data)),

  getAll: () =>
    apiClient.get('/drivers').then((r) => parseApiArrayResponse(DriverProfileSchema, r.data)),

  getById: (driverId) =>
    apiClient.get(`/drivers/${driverId}`).then((r) => parseApiResponse(DriverProfileSchema, r.data)),

  deleteDriver: (driverId) =>
    apiClient.delete(`/drivers/${driverId}`).then((r) => r.data),

  getByArea: (area) =>
    apiClient.get(`/drivers/area/${area}`).then((r) => parseApiArrayResponse(DriverProfileSchema, r.data)),

  toggleAccountStatus: (driverId) =>
    apiClient.put(`/drivers/${driverId}/account-status`).then((r) => r.data),

  getByVehicleType: (vehicleTypeId) =>
    apiClient.get(`/drivers/vehicle-type/${vehicleTypeId}`).then((r) => parseApiArrayResponse(DriverProfileSchema, r.data)),

  updateLocation: (driverId, lat, lng) =>
    apiClient.put(`/drivers/${driverId}`, { currentLat: lat, currentLng: lng }).then((r) => r.data),

  // --- Admin Wallet Management ---
  getDriverWallet: (driverId) =>
    apiClient.get(`/wallets/admin/driver/${driverId}`).then((r) => r.data),

  getDriverTransactions: (driverId, page = 0, size = 10) =>
    apiClient.get(`/wallets/admin/driver/${driverId}/transactions?page=${page}&size=${size}`).then((r) => r.data),

  adjustDriverBalance: (driverId, amount, reason) =>
    apiClient.post(`/wallets/admin/driver/${driverId}/adjust?amount=${amount}&reason=${encodeURIComponent(reason)}`).then((r) => r.data),
}
