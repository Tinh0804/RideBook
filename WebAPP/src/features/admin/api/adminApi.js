import apiClient from '@/services/apiClient'

export const adminApi = {
    getOverviewStats: async (period = 'YEAR', year = new Date().getFullYear()) => {
        const response = await apiClient.get(`/admin/stats/overview?period=${period}&year=${year}`);
        return response.data?.result ?? response.data;
    },

    // Vehicle Types
    getAllVehicleTypes: async () => {
        const response = await apiClient.get('/vehicle-types');
        return response.data;
    },
    createVehicleType: async (data) => {
        const response = await apiClient.post('/admin/vehicle-types', data);
        return response.data;
    },
    updateVehicleType: async (id, data) => {
        const response = await apiClient.put(`/admin/vehicle-types/${id}`, data);
        return response.data;
    },
    deleteVehicleType: async (id) => {
        const response = await apiClient.delete(`/admin/vehicle-types/${id}`);
        return response.data;
    },

    // Time Slots
    getAllTimeSlots: async () => {
        const response = await apiClient.get('/admin/time-slots');
        return response.data;
    },
    updateTimeSlot: async (id, data) => {
        const response = await apiClient.put(`/admin/time-slots/${id}`, data);
        return response.data;
    },

    // Pricing
    getAllPricing: async () => {
        const response = await apiClient.get('/admin/pricing');
        return response.data;
    },
    updatePricing: async (id, data) => {
        const response = await apiClient.put(`/admin/pricing/${id}`, data);
        return response.data;
    }
};
