import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuthStore } from '@/store/rootStore'

/**
 * Protects routes based on auth state and role
 * @param {string[]} allowedRoles - If empty, any authenticated user passes
 */
const ProtectedRoute = ({ allowedRoles = [] }) => {
  const { isAuth, account } = useAuthStore()
  const location = useLocation()

  if (!isAuth) {
    return <Navigate to="/login" state={{ from: location }} replace />
  }

  if (allowedRoles.length > 0 && account?.role?.roleName && !allowedRoles.includes(account?.role?.roleName?.toUpperCase())) {
    // Redirect to their own home
    const home =
      account?.role?.roleName?.toUpperCase() === 'DRIVER'  ? '/driver/dashboard' :
      account?.role?.roleName?.toUpperCase() === 'ADMIN'   ? '/admin/dashboard'  :
                                 '/customer/home'
    return <Navigate to={home} replace />
  }

  return <Outlet />
}

export default ProtectedRoute
