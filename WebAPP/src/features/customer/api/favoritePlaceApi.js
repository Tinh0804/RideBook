import apiClient from '@/services/apiClient'

export const favoritePlaceApi = {
  getMyFavoritePlaces: async () => {
    const response = await apiClient.get('/favorite-places')
    return response.data
  },

  addFavoritePlace: async (data) => {
    const response = await apiClient.post('/favorite-places', data)
    return response.data
  },

  updateFavoritePlace: async (id, data) => {
    const response = await apiClient.put(`/favorite-places/${id}`, data)
    return response.data
  },

  deleteFavoritePlace: async (id) => {
    const response = await apiClient.delete(`/favorite-places/${id}`)
    return response.data
  }
}
