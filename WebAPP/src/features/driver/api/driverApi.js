import apiClient from '@/services/apiClient'
import {
  parseApiResponse,
  parseApiArrayResponse,
  DriverProfileSchema,
  DriverDashboardSchema,
  DriverRevenueSchema,
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

  getRevenue: () =>
    apiClient.get('/drivers/my-revenue').then((r) => parseApiResponse(DriverRevenueSchema, r.data)),

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

  getByVehicleType: (vehicleTypeId) =>
    apiClient.get(`/drivers/vehicle-type/${vehicleTypeId}`).then((r) => parseApiArrayResponse(DriverProfileSchema, r.data)),
}
