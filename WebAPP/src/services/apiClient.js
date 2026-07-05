import axios from 'axios'
import { API_BASE_URL, TOKEN_KEY, REFRESH_TOKEN_KEY, USER_KEY } from '@/config'

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// ─── Request Interceptor ─────────────────────────────────────────────────────
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem(TOKEN_KEY)
    
    // Danh sách các public endpoint không cần đính kèm token
    const publicEndpoints = ['/auth/login', '/auth/refresh-token', '/auth/register', '/auth/oauth2']
    const isPublic = publicEndpoints.some(endpoint => config.url?.includes(endpoint))

    if (token && !isPublic) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// ─── Response Interceptor ────────────────────────────────────────────────────
let isRefreshing = false
let failedQueue  = []

const processQueue = (error, token = null) => {
  failedQueue.forEach((prom) => {
    if (error) prom.reject(error)
    else prom.resolve(token)
  })
  failedQueue = []
}

apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config

    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject })
        })
          .then((token) => {
            originalRequest.headers.Authorization = `Bearer ${token}`
            return apiClient(originalRequest)
          })
          .catch((err) => Promise.reject(err))
      }

      originalRequest._retry = true
      isRefreshing = true

      const refreshToken = localStorage.getItem(REFRESH_TOKEN_KEY)

      try {
        const { data } = await axios.post(
          `${API_BASE_URL}/auth/refresh-token?refreshToken=${refreshToken}`
        )
        const newToken = data?.result?.token || data?.token || data?.result?.accessToken || data?.accessToken
        if (newToken) {
          localStorage.setItem(TOKEN_KEY, newToken)
          apiClient.defaults.headers.common.Authorization = `Bearer ${newToken}`
          processQueue(null, newToken)
          return apiClient(originalRequest)
        }
      } catch (err) {
        processQueue(err, null)
        // Clear auth and redirect to login
        localStorage.removeItem(TOKEN_KEY)
        localStorage.removeItem(REFRESH_TOKEN_KEY)
        localStorage.removeItem(USER_KEY)
        window.location.href = '/welcome?sessionExpired=true'
        return Promise.reject(err)
      } finally {
        isRefreshing = false
      }
    }

    return Promise.reject(error)
  }
)

export default apiClient
