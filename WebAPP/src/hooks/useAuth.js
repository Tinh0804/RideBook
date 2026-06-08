import { useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import toast from 'react-hot-toast'
import { useAuthStore } from '@/store/rootStore'
import { authApi } from '@/features/auth/api/authApi'
import { customerApi } from '@/features/customer/api/customerApi'
import { driverApi } from '@/features/driver/api/driverApi'
import { ROLES, TOKEN_KEY, REFRESH_TOKEN_KEY } from '@/config'

export const useAuth = () => {
  const { user, userProfile, isAuth, accessToken, refreshToken, login, logout, updateUser, setUserProfile } =
    useAuthStore()
  const navigate = useNavigate()

  const processLoginSuccess = useCallback(async (data, roleNameFallback) => {
    const account = data?.account || {}

    // AccountSchema transforms role object to string (roleName)
    const role = account.role || roleNameFallback

    // user: chỉ lưu thông tin account cơ bản
    const userInfo = {
      id:       account.accountId,
      userName: account.userName || '',
      role,
    }

    // Check both token and accessToken depending on backend structure
    const token   = data?.token || data?.accessToken || ''
    const rfToken = data?.refreshToken || ''

    // Lưu token tạm để apiClient dùng cho request tiếp theo
    if (token) {
      localStorage.setItem(TOKEN_KEY,         token)
      localStorage.setItem(REFRESH_TOKEN_KEY, rfToken)
    }

    // Lưu thông tin account vào store trước
    login({
      user:         userInfo,
      accessToken:  token,
      refreshToken: rfToken,
    })

    // Gọi API my-info để lấy profile đầy đủ và lưu vào userProfile riêng biệt
    try {
      if (role === ROLES.CUSTOMER) {
        const profile = await customerApi.getMyInfo()
        setUserProfile(profile)
      } else if (role === ROLES.DRIVER) {
        const profile = await driverApi.getMyInfo()
        setUserProfile(profile)
      }
    } catch (err) {
      console.error('Failed to fetch user profile:', err)
    }

    // Navigate by role
    const loginRole = roleNameFallback || role
    if      (loginRole === ROLES.DRIVER) navigate('/driver/dashboard', { replace: true })
    else if (loginRole === ROLES.ADMIN)  navigate('/admin/dashboard',  { replace: true })
    else                                 navigate('/customer/home',    { replace: true })
  }, [login, navigate, setUserProfile])

  const handleLogin = useCallback(
    async (credentials) => {
      const data = await authApi.login(credentials)
      await processLoginSuccess(data, credentials.roleName)
    },
    [processLoginSuccess]
  )

  const handleOAuthLogin = useCallback(
    async (payload) => {
      // payload = { code, provider, redirectUri }
      const data = await authApi.oauthLogin(payload)
      // OAuth user defaults to CUSTOMER
      await processLoginSuccess(data, ROLES.CUSTOMER)
    },
    [processLoginSuccess]
  )

  const handleLogout = useCallback(async () => {
    try {
      if (refreshToken) await authApi.logout(refreshToken)
    } catch (_) { /* ignore */ }
    logout()
    toast.success('Đã đăng xuất thành công')
    navigate('/login', { replace: true })
  }, [logout, navigate, refreshToken])

  const isRole = useCallback(
    (role) => user?.role === role,
    [user]
  )

  return {
    user,
    userProfile,
    isAuth,
    accessToken,
    handleLogin,
    handleOAuthLogin,
    handleLogout,
    updateUser,
    setUserProfile,
    isCustomer: isRole(ROLES.CUSTOMER),
    isDriver:   isRole(ROLES.DRIVER),
    isAdmin:    isRole(ROLES.ADMIN),
  }
}
