import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuthStore } from '@/store/rootStore'

/**
 * Protects routes based on auth state and role
 * @param {string[]} allowedRoles - If empty, any authenticated user passes
 */
const ProtectedRoute = ({ allowedRoles = [] }) => {
  const { isAuth, user } = useAuthStore()
  const location = useLocation()

  if (!isAuth) {
    return <Navigate to="/login" state={{ from: location }} replace />
  }

  const userRole = user?.role?.toUpperCase()

  if (allowedRoles.length > 0 && userRole && !allowedRoles.includes(userRole)) {
    // Redirect to their own home
    const home =
      userRole === 'DRIVER'  ? '/driver/dashboard' :
      userRole === 'ADMIN'   ? '/admin/dashboard'  :
                               '/customer/home'
    return <Navigate to={home} replace />
  }

  return <Outlet />
}

export default ProtectedRoute
