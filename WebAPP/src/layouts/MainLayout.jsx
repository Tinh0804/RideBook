import { Outlet, NavLink, useNavigate } from 'react-router-dom'
import { useState, useEffect } from 'react'
import {
  RiCarFill, RiHomeLine, RiHistoryLine, RiUserLine,
  RiWalletLine, RiNotification3Line, RiMenuLine,
  RiLogoutBoxLine, RiDashboardLine, RiCarLine,
  RiBarChartLine, RiMapPinLine, RiSettings3Line,
  RiCloseLine, RiMoneyDollarCircleLine,
  RiSunLine, RiMoonLine
} from 'react-icons/ri'
import { useAuthStore, useUIStore, useBookingStore, useDriverStore } from '@/store/rootStore'
import { useAuth } from '@/hooks/useAuth'
import { notificationApi } from '@/features/booking/api/masterDataApi'
import { bookingApi } from '@/features/booking/api/bookingApi'
import { ROLES, BOOKING_STATUS } from '@/config'
import { cn } from '@/utils/cn'

const BASE_CUSTOMER_NAV = [
  { to: '/customer/home',     icon: RiHomeLine,     label: 'Trang chủ' },
  { to: '/customer/history',  icon: RiHistoryLine,  label: 'Lịch sử chuyến' },
  { to: '/customer/promotions',  icon: RiMoneyDollarCircleLine,     label: 'Khuyến mãi' },
  { to: '/customer/profile',  icon: RiUserLine,     label: 'Hồ sơ cá nhân' },
]

const DRIVER_NAV = [
  { to: '/driver/dashboard',  icon: RiDashboardLine,           label: 'Tổng quan' },
  { to: '/driver/trips',      icon: RiCarLine,                 label: 'Nhận chuyến' },
  { to: '/driver/history',    icon: RiHistoryLine,             label: 'Lịch sử chuyến' },
  { to: '/driver/revenue',    icon: RiBarChartLine,            label: 'Doanh thu' },
  { to: '/driver/wallet',     icon: RiWalletLine,              label: 'Ví tiền' },
  { to: '/driver/profile',    icon: RiUserLine,                label: 'Hồ sơ' },
]

const ADMIN_NAV = [
  { to: '/admin/dashboard',   icon: RiDashboardLine,           label: 'Dashboard' },
  { to: '/admin/customers',   icon: RiUserLine,                label: 'Khách hàng' },
  { to: '/admin/drivers',     icon: RiCarLine,                 label: 'Tài xế' },
  { to: '/admin/bookings',    icon: RiHistoryLine,             label: 'Chuyến đi' },
  { to: '/admin/promotions',  icon: RiMoneyDollarCircleLine,   label: 'Khuyến mãi' },
  { to: '/admin/settings',    icon: RiSettings3Line,           label: 'Cài đặt' },
]

const MainLayout = () => {
  const { user, handleLogout } = useAuth()
  const { userProfile, handleLogoutProfile} = useAuth ()
  const { sidebarOpen, toggleSidebar, notifCount, setNotifCount, theme, toggleTheme } = useUIStore()
  const { currentBooking, setCurrentBooking, clearCurrentBooking } = useBookingStore()
  const { setCurrentTrip, clearCurrentTrip } = useDriverStore()
  const [notifOpen, setNotifOpen] = useState(false)
  const [notifications, setNotifications] = useState([])
  const [tripLoading, setTripLoading] = useState(true)

  const role = user?.role?.toUpperCase()
  
  const customerNav = [
    BASE_CUSTOMER_NAV[0], // Trang chủ
    currentBooking 
      ? { to: '/customer/tracking', icon: RiMapPinLine, label: 'Chuyến xe' }
      : { to: '/customer/booking',  icon: RiMapPinLine, label: 'Đặt xe' },
    BASE_CUSTOMER_NAV[1], // Lịch sử
    BASE_CUSTOMER_NAV[2], // Khuyến mãi
    BASE_CUSTOMER_NAV[3], // Hồ sơ
  ]

  const navItems =
    role === ROLES.DRIVER  ? DRIVER_NAV  :
    role === ROLES.ADMIN   ? ADMIN_NAV   :
                             customerNav

  useEffect(() => {
    notificationApi.getAll()
      .then((items) => {
        setNotifications(items)
        setNotifCount(items.filter((n) => !n.read).length)
      })
      .catch(() => {})
  }, [setNotifCount])

  // Lấy dữ liệu chuyến đi hiện tại từ Backend khi load ứng dụng, thay vì lưu ở Frontend
  useEffect(() => {
    if (!user?.customerId) {
      setTripLoading(false)
      return
    }

    setTripLoading(true)
    if (role === ROLES.CUSTOMER) {
      bookingApi.getCustomerHistory(user.customerId)
        .then(history => {
          const active = history.find(b => [BOOKING_STATUS.PENDING, BOOKING_STATUS.ACCEPTED, BOOKING_STATUS.IN_PROGRESS, BOOKING_STATUS.ARRIVED].includes(b.bookingStatus))
          if (active) setCurrentBooking(active)
          else clearCurrentBooking()
        })
        .catch(clearCurrentBooking)
        .finally(() => setTripLoading(false))
    } else if (role === ROLES.DRIVER) {
      bookingApi.getDriverHistory(user?.driverId)
        .then(history => {
          const active = history.find(b => [BOOKING_STATUS.ACCEPTED, BOOKING_STATUS.IN_PROGRESS, BOOKING_STATUS.ARRIVED].includes(b.bookingStatus))
          if (active) setCurrentTrip(active)
          else clearCurrentTrip()
        })
        .catch(clearCurrentTrip)
        .finally(() => setTripLoading(false))
    } else {
      setTripLoading(false)
    }
  }, [user?.customerId, role, setCurrentBooking, clearCurrentBooking, setCurrentTrip, clearCurrentTrip])

  return (
    <div className="flex h-screen overflow-hidden bg-surface-dark">
      {/* ── Sidebar ─────────────────────────────────── */}
      <aside
        className={cn(
          'flex flex-col bg-surface-card border-r border-surface-border',
          'transition-all duration-300 ease-in-out z-30',
          sidebarOpen ? 'w-64' : 'w-16',
          // Mobile: slide in from left
          'fixed lg:static h-full',
          !sidebarOpen && 'lg:w-16',
        )}
      >
        {/* Logo */}
        <div className={cn(
          'flex items-center gap-3 px-4 h-16 border-b border-surface-border shrink-0',
          !sidebarOpen && 'justify-center px-0',
        )}>
          <div className="w-8 h-8 rounded-lg bg-brand-500 flex items-center justify-center shrink-0 shadow-glow-green">
            <RiCarFill className="text-content-main" size={16} />
          </div>
          {sidebarOpen && (
            <span className="font-display text-lg font-bold text-content-main">BookCar</span>
          )}
        </div>

        {/* Nav */}
        <nav className="flex-1 overflow-y-auto no-scrollbar py-4 px-2 space-y-1">
          {navItems.map(({ to, icon: Icon, label }) => (
            <NavLink
              key={to}
              to={to}
              className={({ isActive }) =>
                cn('nav-link', isActive && 'active', !sidebarOpen && 'justify-center px-0 py-3')
              }
              title={!sidebarOpen ? label : undefined}
            >
              <Icon size={20} className="shrink-0" />
              {sidebarOpen && <span>{label}</span>}
            </NavLink>
          ))}
        </nav>

        {/* User area */}
        <div className={cn(
          'border-t border-surface-border p-3 shrink-0',
          !sidebarOpen && 'flex justify-center',
        )}>
          {sidebarOpen ? (
            <div className="flex items-center gap-3 p-2 rounded-xl hover:bg-surface-border/40 cursor-pointer transition-colors">
              <div className="w-8 h-8 rounded-full bg-brand-500/20 border border-brand-500/30 flex items-center justify-center text-brand-400 text-sm font-bold shrink-0">
                {userProfile?.name?.[0] || user?.userName?.[0] || 'U'}
              </div>
              <div className="flex-1 min-w-0">
                <p className="text-sm font-medium text-content-main truncate">{userProfile?.name || user?.userName}</p>
                <p className="text-xs text-content-muted capitalize">{role?.toLowerCase()}</p>
              </div>
            </div>
          ) : (
            <div className="w-8 h-8 rounded-full bg-brand-500/20 border border-brand-500/30 flex items-center justify-center text-brand-400 text-sm font-bold">
              {userProfile?.name?.[0] || user?.userName?.[0] || 'U'}
            </div>
          )}
        </div>
      </aside>

      {/* Mobile overlay */}
      {sidebarOpen && (
        <div
          className="fixed inset-0 bg-black/50 z-20 lg:hidden"
          onClick={toggleSidebar}
        />
      )}

      {/* ── Main content ─────────────────────────────── */}
      <div className="flex-1 flex flex-col overflow-hidden min-w-0">
        {/* Topbar */}
        <header className="h-16 flex items-center gap-4 px-6 border-b border-surface-border bg-surface-card/80 backdrop-blur-md shrink-0 z-10">
          <button
            onClick={toggleSidebar}
            className="p-2 rounded-lg text-content-muted hover:text-content-main hover:bg-surface-border transition-colors"
          >
            {sidebarOpen ? <RiCloseLine size={20} /> : <RiMenuLine size={20} />}
          </button>

          <div className="flex-1" />

          {/* Theme Toggle */}
          <button
            onClick={toggleTheme}
            className="p-2 rounded-lg text-content-muted hover:text-content-main hover:bg-surface-border transition-colors"
            title="Đổi giao diện"
          >
            {theme === 'dark' ? <RiSunLine size={20} /> : <RiMoonLine size={20} />}
          </button>

          {/* Notifications */}
          <div className="relative">
            <button
              onClick={() => setNotifOpen((o) => !o)}
              className="relative p-2 rounded-lg text-content-muted hover:text-content-main hover:bg-surface-border transition-colors"
            >
              <RiNotification3Line size={20} />
              {notifCount > 0 && (
                <span className="absolute top-1 right-1 w-4 h-4 bg-brand-500 rounded-full text-[9px] font-bold text-content-main flex items-center justify-center">
                  {notifCount > 9 ? '9+' : notifCount}
                </span>
              )}
            </button>

            {/* Notif dropdown */}
            {notifOpen && (
              <div className="absolute right-0 top-12 w-80 card shadow-2xl z-50 animate-slide-up">
                <div className="p-4 border-b border-surface-border flex items-center justify-between">
                  <h3 className="font-semibold text-content-main">Thông báo</h3>
                  <span className="text-xs text-brand-400">{notifCount} chưa đọc</span>
                </div>
                <div className="max-h-72 overflow-y-auto divide-y divide-surface-border">
                  {notifications.length === 0 ? (
                    <p className="p-4 text-sm text-content-muted text-center">Không có thông báo</p>
                  ) : (
                    notifications.slice(0, 8).map((n) => (
                      <div
                        key={n.id}
                        className={cn(
                          'p-4 hover:bg-surface-border/30 cursor-pointer transition-colors',
                          !n.read && 'bg-brand-500/5 border-l-2 border-brand-500'
                        )}
                      >
                        <p className="text-sm text-content-muted">{n.message || n.content}</p>
                        <p className="text-xs text-gray-600 mt-1">{n.createdAt}</p>
                      </div>
                    ))
                  )}
                </div>
              </div>
            )}
          </div>

          {/* Logout */}
          <button
            onClick={handleLogout}
            className="p-2 rounded-lg text-content-muted hover:text-red-400 hover:bg-red-500/10 transition-colors"
            title="Đăng xuất"
          >
            <RiLogoutBoxLine size={20} />
          </button>
        </header>

        {/* Page content */}
        <main className="flex-1 overflow-y-auto p-6">
          {tripLoading ? (
            <div className="flex items-center justify-center h-full">
              <div className="flex flex-col items-center gap-4">
                <div className="w-10 h-10 border-4 border-brand-500/30 border-t-brand-500 rounded-full animate-spin" />
                <p className="text-sm text-content-muted animate-pulse">Đang tải dữ liệu...</p>
              </div>
            </div>
          ) : (
            <Outlet />
          )}
        </main>
      </div>
    </div>
  )
}

export default MainLayout
