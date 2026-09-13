import apiClient from '@/services/apiClient';

export const loyaltyApi = {
    getMyAccount: () => apiClient.get('/loyalty/my-account').then(r => r.data?.result ?? r.data),
    getPointHistory: (page = 0, size = 10) => 
        apiClient.get(`/loyalty/my-history?page=${page}&size=${size}`).then(r => r.data?.result ?? r.data),
};
