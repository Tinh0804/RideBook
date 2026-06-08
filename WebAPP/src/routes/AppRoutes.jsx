import { lazy, Suspense } from 'react'
import { Routes, Route, Navigate } from 'react-router-dom'
import { useAuthStore } from '@/store/rootStore'
import { ROLES } from '@/config'
import AuthLayout    from '@/layouts/AuthLayout'
import MainLayout    from '@/layouts/MainLayout'
import ProtectedRoute from './ProtectedRoute'
import Spinner       from '@/components/Elements/Spinner'

// ─── Lazy page imports ───────────────────────────────────────────────────────
// Auth
const LoginPage              = lazy(() => import('@/pages/auth/LoginPage'))
const RegisterCustomerPage   = lazy(() => import('@/pages/auth/RegisterCustomerPage'))
const RegisterDriverPage     = lazy(() => import('@/pages/auth/RegisterDriverPage'))
const ForgotPasswordPage     = lazy(() => import('@/pages/auth/ForgotPasswordPage'))

// Customer
const CustomerHomePage       = lazy(() => import('@/pages/customer/CustomerHomePage'))
const BookingPage            = lazy(() => import('@/pages/customer/BookingPage'))
const TripTrackingPage       = lazy(() => import('@/pages/customer/TripTrackingPage'))
const TripHistoryPage        = lazy(() => import('@/pages/customer/TripHistoryPage'))
const RatingPage             = lazy(() => import('@/pages/customer/RatingPage'))
const CustomerProfilePage    = lazy(() => import('@/pages/customer/CustomerProfilePage'))
const CustomerPromotionsPage = lazy(() => import('@/pages/customer/PromotionsPage'))

// Driver
const DriverDashboardPage    = lazy(() => import('@/pages/driver/DriverDashboardPage'))
const AvailableTripsPage     = lazy(() => import('@/pages/driver/AvailableTripsPage'))

const DriverRevenuePage      = lazy(() => import('@/pages/driver/DriverRevenuePage'))
const DriverWalletPage       = lazy(() => import('@/pages/driver/DriverWalletPage'))
const DriverProfilePage      = lazy(() => import('@/pages/driver/DriverProfilePage'))
const DriverHistoryPage      = lazy(() => import('@/pages/driver/DriverHistoryPage'))

// Admin
const AdminDashboardPage     = lazy(() => import('@/pages/admin/AdminDashboardPage'))

// Payment
const PaymentCallbackPage    = lazy(() => import('@/pages/customer/PaymentCallbackPage'))

// Common
const NotFoundPage           = lazy(() => import('@/pages/NotFound'))

// ─── Loading Fallback ────────────────────────────────────────────────────────
const PageLoader = () => (
  <div className="flex items-center justify-center min-h-screen">
    <Spinner size="xl" />
  </div>
)

// ─── Root redirect ───────────────────────────────────────────────────────────
const RootRedirect = () => {
  const { isAuth, account } = useAuthStore()
  if (!isAuth) return <Navigate to="/login" replace />
  if (account?.role?.roleName?.toUpperCase() === ROLES.DRIVER) return <Navigate to="/driver/dashboard" replace />
  if (account?.role?.roleName?.toUpperCase() === ROLES.ADMIN)  return <Navigate to="/admin/dashboard"  replace />
  return <Navigate to="/customer/home" replace />
}

// ─── App Routes ──────────────────────────────────────────────────────────────
const AppRoutes = () => (
  <Suspense fallback={<PageLoader />}>
    <Routes>
      {/* Root redirect */}
      <Route index element={<RootRedirect />} />

      {/* ── Auth routes ── */}
      <Route element={<AuthLayout />}>
        <Route path="login"              element={<LoginPage />} />
        <Route path="register/customer"  element={<RegisterCustomerPage />} />
        <Route path="register/driver"    element={<RegisterDriverPage />} />
        <Route path="forgot-password"    element={<ForgotPasswordPage />} />
      </Route>

      {/* ── Customer routes ── */}
      <Route element={<ProtectedRoute allowedRoles={[ROLES.CUSTOMER]} />}>
        <Route element={<MainLayout />}>
          <Route path="customer/home"     element={<CustomerHomePage />} />
          <Route path="customer/booking"  element={<BookingPage />} />
          <Route path="customer/tracking" element={<TripTrackingPage />} />
          <Route path="customer/history"  element={<TripHistoryPage />} />
          <Route path="customer/rating"   element={<RatingPage />} />
          <Route path="customer/profile"  element={<CustomerProfilePage />} />
          <Route path="customer/promotions"  element={<CustomerPromotionsPage />} />
        </Route>
      </Route>

      {/* ── Driver routes ── */}
      <Route element={<ProtectedRoute allowedRoles={[ROLES.DRIVER]} />}>
        <Route element={<MainLayout />}>
          <Route path="driver/dashboard"    element={<DriverDashboardPage />} />
          <Route path="driver/trips"        element={<AvailableTripsPage />} />
          <Route path="driver/current-trip" element={<Navigate to="/driver/trips" replace />} />
          <Route path="driver/revenue"      element={<DriverRevenuePage />} />
          <Route path="driver/wallet"       element={<DriverWalletPage />} />
          <Route path="driver/profile"      element={<DriverProfilePage />} />
          <Route path="driver/history"      element={<DriverHistoryPage />} />
        </Route>
      </Route>

      {/* ── Payment callback (public - no role guard needed) ── */}
      <Route element={<ProtectedRoute allowedRoles={[ROLES.CUSTOMER]} />}>
        <Route path="payment/callback" element={<PaymentCallbackPage />} />
      </Route>

      {/* ── Admin routes ── */}
      <Route element={<ProtectedRoute allowedRoles={[ROLES.ADMIN]} />}>
        <Route element={<MainLayout />}>
          <Route path="admin/dashboard"  element={<AdminDashboardPage />} />
          <Route path="admin/customers"  element={<AdminDashboardPage />} />
          <Route path="admin/drivers"    element={<AdminDashboardPage />} />
          <Route path="admin/bookings"   element={<AdminDashboardPage />} />
          <Route path="admin/*"          element={<AdminDashboardPage />} />
        </Route>
      </Route>

      {/* 404 */}
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  </Suspense>
)

export default AppRoutes
