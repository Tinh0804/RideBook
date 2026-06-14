import apiClient from '../../core/api/apiClient';

export const adminApi = {
    getOverviewStats: async (year = new Date().getFullYear()) => {
        const response = await apiClient.get(`/admin/stats/overview?year=${year}`);
        return response.data;
    },

    // Vehicle Types
    getAllVehicleTypes: async () => {
        const response = await apiClient.get('/vehicle-types');
        return response.data;
    },
    createVehicleType: async (data) => {
        const response = await apiClient.post('/vehicle-types', data);
        return response.data;
    },
    updateVehicleType: async (id, data) => {
        const response = await apiClient.put(`/vehicle-types/${id}`, data);
        return response.data;
    },
    deleteVehicleType: async (id) => {
        const response = await apiClient.delete(`/vehicle-types/${id}`);
        return response.data;
    },

    // Time Slots
    getAllTimeSlots: async () => {
        const response = await apiClient.get('/time-slots');
        return response.data;
    },
    updateTimeSlot: async (id, data) => {
        const response = await apiClient.put(`/time-slots/${id}`, data);
        return response.data;
    },

    // Pricing
    getAllPricing: async () => {
        const response = await apiClient.get('/pricing');
        return response.data;
    },
    updatePricing: async (id, data) => {
        const response = await apiClient.put(`/pricing/${id}`, data);
        return response.data;
    }
};
