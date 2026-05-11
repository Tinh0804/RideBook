import { useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import toast from 'react-hot-toast'
import { useAuthStore } from '@/store/rootStore'
import { authApi } from '@/features/auth/api/authApi'
import { customerApi } from '@/features/customer/api/customerApi'
import { driverApi } from '@/features/driver/api/driverApi'
import { ROLES, TOKEN_KEY, REFRESH_TOKEN_KEY } from '@/config'

export const useAuth = () => {
  const { user, isAuth, accessToken, refreshToken, login, logout, updateUser } =
    useAuthStore()
  const navigate = useNavigate()

  const handleLogin = useCallback(
    async (credentials) => {
      const data = await authApi.login(credentials)
      const account = data?.account || {}
      
      // Since AccountSchema now transforms role to string (roleName)
      const role = account.role || credentials.roleName
      
      let userProfile = { 
        id: account.accountId,
        userName: account.userName || credentials.userName,
        role: role
      }

      // Check both token and accessToken depending on backend structure
      const token = data?.token || data?.accessToken || ''
      const rfToken = data?.refreshToken || ''

      // Temporarily store token so apiClient can use it for the next requests
      if (token) {
        localStorage.setItem(TOKEN_KEY, token)
        localStorage.setItem(REFRESH_TOKEN_KEY, rfToken)
      }

      try {
        if (role === ROLES.CUSTOMER) {
          const profile = await customerApi.getMyInfo()
          userProfile = { 
            ...userProfile, 
            ...profile, 
            name: profile?.customerName || userProfile.userName 
          }
        } else if (role === ROLES.DRIVER) {
          const profile = await driverApi.getMyInfo()
          userProfile = { 
            ...userProfile, 
            ...profile, 
            name: profile?.driverName || userProfile.userName 
          }
        }
      } catch (err) {
        console.error('Failed to fetch user profile:', err)
      }

      login({
        user: userProfile,
        accessToken:  token,
        refreshToken: rfToken,
      })

      // Navigate by role
      const loginRole = credentials.roleName || role
      if (loginRole === ROLES.DRIVER)   navigate('/driver/dashboard', { replace: true })
      else if (loginRole === ROLES.ADMIN) navigate('/admin/dashboard', { replace: true })
      else                           navigate('/customer/home',   { replace: true })
    },
    [login, navigate]
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
    isAuth,
    accessToken,
    handleLogin,
    handleLogout,
    updateUser,
    isCustomer: isRole(ROLES.CUSTOMER),
    isDriver:   isRole(ROLES.DRIVER),
    isAdmin:    isRole(ROLES.ADMIN),
  }
}
