import axios from 'axios'
import { API_BASE_URL, TOKEN_KEY, REFRESH_TOKEN_KEY, USER_KEY } from '@/config'
import { useAuthStore } from '@/store/rootStore'

const PUBLIC_AUTH_ENDPOINTS = [
  '/auth/login',
  '/auth/refresh-token',
  '/auth/register',
  '/auth/oauth2',
  '/auth/reset-password',
  '/auth/check-phone',
]

const NO_AUTO_REFRESH_ENDPOINTS = [...PUBLIC_AUTH_ENDPOINTS, '/auth/logout']

const matchesEndpoint = (url, endpoints) =>
  endpoints.some((endpoint) => url?.includes(endpoint))

const isSerializedJwt = (token) =>
  typeof token === 'string' &&
  token.split('.').length === 3 &&
  token.split('.').every(Boolean)

const clearAuthSession = () => {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(REFRESH_TOKEN_KEY)
  useAuthStore.setState({
    user: null,
    userProfile: null,
    accessToken: null,
    refreshToken: null,
    isAuth: false,
    account: null,
  })
  sessionStorage.removeItem(USER_KEY)
}

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
    const isPublic = matchesEndpoint(config.url, PUBLIC_AUTH_ENDPOINTS)

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
    const mustNotRefresh = matchesEndpoint(originalRequest?.url, NO_AUTO_REFRESH_ENDPOINTS)

    if (error.response?.status === 401 && originalRequest && !originalRequest._retry && !mustNotRefresh) {
      const refreshToken = localStorage.getItem(REFRESH_TOKEN_KEY)

      if (!isSerializedJwt(refreshToken)) {
        clearAuthSession()
        window.location.href = '/welcome?sessionExpired=true'
        return Promise.reject(error)
      }

      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject })
        })
          .then((token) => {
            originalRequest.headers = originalRequest.headers || {}
            originalRequest.headers.Authorization = `Bearer ${token}`
            return apiClient(originalRequest)
          })
          .catch((err) => Promise.reject(err))
      }

      originalRequest._retry = true
      isRefreshing = true

      try {
        const { data } = await axios.post(
          `${API_BASE_URL}/auth/refresh-token`,
          null,
          { params: { refreshToken } }
        )
        const refreshedAuth = data?.result || data
        const newToken = refreshedAuth?.token || refreshedAuth?.accessToken
        const newRefreshToken = refreshedAuth?.refreshToken

        if (!isSerializedJwt(newToken) || !isSerializedJwt(newRefreshToken)) {
          throw new Error('Invalid token refresh response')
        }

        localStorage.setItem(TOKEN_KEY, newToken)
        localStorage.setItem(REFRESH_TOKEN_KEY, newRefreshToken)
        useAuthStore.setState({
          accessToken: newToken,
          refreshToken: newRefreshToken,
          isAuth: true,
        })

        processQueue(null, newToken)
        originalRequest.headers = originalRequest.headers || {}
        originalRequest.headers.Authorization = `Bearer ${newToken}`
        return apiClient(originalRequest)
      } catch (err) {
        processQueue(err, null)
        clearAuthSession()
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
