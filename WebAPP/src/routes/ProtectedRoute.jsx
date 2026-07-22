import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuthStore } from '@/store/rootStore'

/**
 * Protects routes based on auth state and role
 * @param {string[]} allowedRoles - If empty, any authenticated user passes
 */
const ProtectedRoute = ({ allowedRoles = [] }) => {
  const { isAuth, user } = useAuthStore()
  const location = useLocation()

  const rawRole = user?.role
  const userRoleStr = typeof rawRole === 'object' ? rawRole?.roleName : rawRole

  if (!isAuth) {
    const roleParam = userRoleStr?.toLowerCase() || 'customer'
    return <Navigate to={`/login/${roleParam}`} state={{ from: location }} replace />
  }

  const userRole = userRoleStr?.toUpperCase()

  if (allowedRoles.length > 0 && userRole && !allowedRoles.includes(userRole)) {
    // Redirect to their own home
    const home =
      userRole === 'DRIVER' ? '/driver/dashboard' :
        userRole === 'ADMIN' ? '/admin/dashboard' :
          '/customer/home'
    return <Navigate to={home} replace />
  }

  return <Outlet />
}

export default ProtectedRoute
