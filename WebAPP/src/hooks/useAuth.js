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

  const handleLogin = useCallback(
    async (credentials) => {
      const data = await authApi.login(credentials)
      const account = data?.account || {}

      // AccountSchema transforms role object to string (roleName)
      const role = account.role || credentials.roleName

      // user: chỉ lưu thông tin account cơ bản
      const userInfo = {
        id:       account.accountId,
        userName: account.userName || credentials.userName,
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
      const loginRole = credentials.roleName || role
      if      (loginRole === ROLES.DRIVER) navigate('/driver/dashboard', { replace: true })
      else if (loginRole === ROLES.ADMIN)  navigate('/admin/dashboard',  { replace: true })
      else                                 navigate('/customer/home',    { replace: true })
    },
    [login, navigate, setUserProfile]
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
    handleLogout,
    updateUser,
    setUserProfile,
    isCustomer: isRole(ROLES.CUSTOMER),
    isDriver:   isRole(ROLES.DRIVER),
    isAdmin:    isRole(ROLES.ADMIN),
  }
}
