import { Outlet, NavLink, Link, useNavigate, useLocation } from 'react-router-dom'
import { useState, useEffect } from 'react'
import {
  RiHomeLine, RiHistoryLine, RiUserLine,
  RiWalletLine, RiNotification3Line,
  RiLogoutBoxLine, RiDashboardLine, RiCarLine,
  RiBarChartLine, RiMapPinLine,
  RiMoneyDollarCircleLine, RiCarFill,
  RiSunLine, RiMoonLine, RiTimeLine, RiPriceTag3Line, RiRouteLine
} from 'react-icons/ri'
import { useAuthStore, useUIStore, useBookingStore, useDriverStore } from '@/store/rootStore'
import { useAuth } from '@/hooks/useAuth'
import { notificationApi } from '@/features/booking/api/masterDataApi'
import { bookingApi } from '@/features/booking/api/bookingApi'
import { ROLES, BOOKING_STATUS, WS_URL } from '@/config'
import { cn } from '@/utils/cn'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { toast } from 'react-hot-toast'
import Modal from '@/components/Elements/Modal'
import { formatDistanceToNow } from 'date-fns'
import { vi } from 'date-fns/locale'
import { motion, AnimatePresence } from 'motion/react'

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
  { to: '/admin/dashboard',      icon: RiDashboardLine,           label: 'Dashboard' },
  { to: '/admin/customers',      icon: RiUserLine,                label: 'Khách hàng' },
  { to: '/admin/drivers',        icon: RiCarLine,                 label: 'Tài xế' },
  { to: '/admin/bookings',       icon: RiHistoryLine,             label: 'Chuyến đi' },
  { to: '/admin/promotions',     icon: RiMoneyDollarCircleLine,   label: 'Khuyến mãi' },
  { to: '/admin/notifications',  icon: RiNotification3Line,       label: 'Gửi thông báo' },
  { to: '/admin/vehicle-types',  icon: RiCarFill,                 label: 'Loại xe' },
  { to: '/admin/time-slots',     icon: RiTimeLine,                label: 'Khung giờ' },
  { to: '/admin/pricing',        icon: RiPriceTag3Line,           label: 'Bảng giá' },
]

const MainLayout = () => {
  const { user, handleLogout } = useAuth()
  const { userProfile, handleLogoutProfile } = useAuth()
  const { sidebarOpen, toggleSidebar, notifCount, setNotifCount, theme, toggleTheme } = useUIStore()
  const { currentBooking, setCurrentBooking, clearCurrentBooking } = useBookingStore()
  const { setCurrentTrip, clearCurrentTrip } = useDriverStore()
  const [notifOpen, setNotifOpen] = useState(false)
  const [notifications, setNotifications] = useState([])
  const [tripLoading, setTripLoading] = useState(true)
  const [selectedNotif, setSelectedNotif] = useState(null)
  
  // Animation state cho chiếc xe kéo sidebar
  const [isCarDriving, setIsCarDriving] = useState(false)
  
  const navigate = useNavigate()
  const location = useLocation()

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

  // Setup WebSockets STOMP connection for real-time notifications
  useEffect(() => {
    if (!user?.userName) return

    const client = new Client({
      webSocketFactory: () => new SockJS(WS_URL),
      debug: (str) => {},
      reconnectDelay: 5000,
      onConnect: () => {
        client.subscribe(`/topic/notifications/${user.userName}`, (msg) => {
          if (msg.body) {
            try {
              const newNotif = JSON.parse(msg.body)
              setNotifications(prev => [newNotif, ...prev])
              setNotifCount(prev => prev + 1)
              toast.success(newNotif.title + '\n' + newNotif.message, {
                duration: 5000,
                icon: '🔔',
              })
            } catch (err) {
              console.error('Error parsing notification:', err)
            }
          }
        })
      }
    })

    client.activate()
    return () => client.deactivate()
  }, [user?.userName, setNotifCount])

  // Fetch active trips
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

  const handleNotificationClick = async (n) => {
    if (!n.read) {
      try {
        await notificationApi.markRead(n.notificationId)
        setNotifications((prev) =>
          prev.map((item) => (item.notificationId === n.notificationId ? { ...item, read: true } : item))
        )
        setNotifCount((prev) => Math.max(0, prev - 1))
      } catch (err) {}
    }
    setNotifOpen(false)

    if (n.bookingId) {
      if (role === ROLES.CUSTOMER) navigate('/customer/history')
      else if (role === ROLES.DRIVER) navigate('/driver/history')
      else navigate('/admin/bookings')
    } else {
      setSelectedNotif(n)
    }
  }

  // Animation Toggle Sidebar vui nhộn
  const handleToggleSidebar = () => {
    setIsCarDriving(true)
    setTimeout(() => {
      toggleSidebar()
      setTimeout(() => setIsCarDriving(false), 200)
    }, 150)
  }

  return (
    <div className="flex h-screen overflow-hidden bg-[#e8ece3] dark:bg-surface-dark font-sans text-gray-900 dark:text-white transition-colors duration-300">
      
      {/* ── Sidebar (Practical + Fun Animation) ─────────────────────────────────── */}
      <motion.aside
        initial={false}
        animate={{ width: sidebarOpen ? 280 : 80 }}
        className="relative flex flex-col bg-[#e8ece3] dark:bg-surface-dark border-r border-[#cdd4c8] dark:border-surface-border z-30 h-full shrink-0"
      >
        {/* Nút chiếc xe thần thánh */}
        <div className="absolute -right-5 top-10 z-40 hidden lg:block">
          <button
            onClick={handleToggleSidebar}
            className="relative flex items-center justify-center w-10 h-10 bg-brand-500 rounded-full text-white shadow-lg shadow-brand-500/30 hover:bg-brand-600 transition-colors group focus:outline-none"
          >
            <motion.div
              animate={{ 
                x: isCarDriving ? (sidebarOpen ? -12 : 12) : 0,
                rotate: sidebarOpen ? 180 : 0
              }}
              transition={{ type: 'spring', stiffness: 200, damping: 12 }}
            >
              <RiCarFill size={20} />
            </motion.div>
          </button>
        </div>

        {/* Logo */}
        <Link to="/" className="flex items-center gap-4 px-6 h-20 shrink-0 overflow-hidden whitespace-nowrap border-b border-gray-100 dark:border-surface-border group" aria-label="BookCar - Trang chủ">
          <span className="grid h-10 w-10 place-items-center rounded-[14px] bg-gray-900 dark:bg-white text-white dark:text-gray-900 shadow-sm transition-transform duration-300 group-hover:-rotate-6 shrink-0">
            <RiRouteLine size={21} aria-hidden="true" />
          </span>
          <AnimatePresence>
            {sidebarOpen && (
              <motion.span 
                initial={{ opacity: 0, x: -10 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: -10 }}
                transition={{ duration: 0.2 }}
                className="font-display text-2xl font-bold tracking-[-0.04em] text-gray-900 dark:text-white"
              >
                BookCar<span className="text-brand-500">/</span>
              </motion.span>
            )}
          </AnimatePresence>
        </Link>

        {/* Nav */}
        <nav className="flex-1 overflow-y-auto no-scrollbar py-6 px-4 space-y-2">
          {navItems.map(({ to, icon: Icon, label }) => {
            const isActive = location.pathname.startsWith(to)
            return (
              <NavLink
                key={to}
                to={to}
                className={cn(
                  'flex items-center gap-4 px-4 py-3.5 rounded-xl transition-all duration-200 group overflow-hidden whitespace-nowrap',
                  isActive 
                    ? 'bg-brand-500 text-white shadow-md shadow-brand-500/20' 
                    : 'text-gray-500 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-surface-border hover:text-gray-900 dark:hover:text-white',
                  !sidebarOpen && 'justify-center px-0'
                )}
                title={!sidebarOpen ? label : undefined}
              >
                <Icon size={22} className={cn("shrink-0 transition-transform", isActive ? "scale-110" : "group-hover:scale-110")} />
                <AnimatePresence>
                  {sidebarOpen && (
                    <motion.span 
                      initial={{ opacity: 0, width: 0 }}
                      animate={{ opacity: 1, width: 'auto' }}
                      exit={{ opacity: 0, width: 0 }}
                      className="font-semibold text-[15px]"
                    >
                      {label}
                    </motion.span>
                  )}
                </AnimatePresence>
              </NavLink>
            )
          })}
        </nav>

        {/* User profile mini area */}
        <div className="p-4 border-t border-gray-100 dark:border-surface-border shrink-0 overflow-hidden whitespace-nowrap">
          <div className={cn(
            'flex items-center gap-3 p-2 rounded-xl bg-white/50 dark:bg-surface-dark border border-[#cdd4c8] dark:border-surface-border',
            !sidebarOpen && 'justify-center p-2'
          )}>
            <div className="w-10 h-10 rounded-full bg-brand-500 flex items-center justify-center text-white text-lg font-bold shrink-0 shadow-sm">
              {userProfile?.name?.[0] || user?.userName?.[0] || 'U'}
            </div>
            <AnimatePresence>
              {sidebarOpen && (
                <motion.div 
                  initial={{ opacity: 0 }}
                  animate={{ opacity: 1 }}
                  exit={{ opacity: 0 }}
                  className="flex-1 min-w-0"
                >
                  <p className="text-sm font-bold text-gray-900 dark:text-white truncate">{userProfile?.name || user?.userName}</p>
                  <p className="text-xs text-gray-500 dark:text-gray-400 capitalize truncate">{role?.toLowerCase()}</p>
                </motion.div>
              )}
            </AnimatePresence>
          </div>
        </div>
      </motion.aside>

      {/* Mobile overlay */}
      {sidebarOpen && (
        <div
          className="fixed inset-0 bg-black/40 backdrop-blur-sm z-20 lg:hidden"
          onClick={toggleSidebar}
        />
      )}

      {/* ── Main content ─────────────────────────────── */}
      <div className="flex-1 flex flex-col overflow-hidden min-w-0 relative">
        
        {/* Topbar */}
        <header className="h-20 flex items-center justify-between px-6 lg:px-10 shrink-0 z-10">
          
          {/* Mobile toggle */}
          <button
            onClick={toggleSidebar}
            className="lg:hidden p-2 rounded-xl bg-white dark:bg-surface-dark border border-[#cdd4c8] dark:border-surface-border shadow-sm text-gray-600 dark:text-gray-300"
          >
            <RiCarFill size={22} className="text-brand-500" />
          </button>
          
          <div className="flex-1" />

          {/* Right actions */}
          <div className="flex items-center gap-3">
            {/* Theme Toggle */}
            <button
              onClick={toggleTheme}
              className="w-10 h-10 rounded-full flex items-center justify-center bg-white dark:bg-surface-card border border-gray-200 dark:border-surface-border text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-surface-border transition-colors shadow-sm"
              title="Đổi giao diện"
            >
              {theme === 'dark' ? <RiSunLine size={18} /> : <RiMoonLine size={18} />}
            </button>

            {/* Notifications */}
            <div className="relative">
              <button
                onClick={() => setNotifOpen((o) => !o)}
                className="w-10 h-10 rounded-full flex items-center justify-center bg-white dark:bg-surface-card border border-gray-200 dark:border-surface-border text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-surface-border transition-colors shadow-sm relative"
              >
                <RiNotification3Line size={18} />
                {notifCount > 0 && (
                  <span className="absolute -top-1 -right-1 w-5 h-5 bg-red-500 border-2 border-white dark:border-surface-card rounded-full text-[10px] font-bold text-white flex items-center justify-center">
                    {notifCount > 9 ? '9+' : notifCount}
                  </span>
                )}
              </button>

              {/* Notif dropdown */}
              <AnimatePresence>
                {notifOpen && (
                  <motion.div 
                    initial={{ opacity: 0, y: 10, scale: 0.95 }}
                    animate={{ opacity: 1, y: 0, scale: 1 }}
                    exit={{ opacity: 0, y: 10, scale: 0.95 }}
                    className="absolute right-0 top-14 w-80 bg-white dark:bg-surface-card rounded-2xl shadow-[0_10px_40px_rgba(0,0,0,0.1)] dark:shadow-[0_10px_40px_rgba(0,0,0,0.3)] border border-gray-100 dark:border-surface-border z-50 overflow-hidden"
                  >
                    <div className="p-4 border-b border-gray-100 dark:border-surface-border flex items-center justify-between bg-gray-50/50 dark:bg-surface-dark/50">
                      <h3 className="font-bold text-gray-900 dark:text-white">Thông báo</h3>
                      <span className="text-xs font-semibold text-brand-500 bg-brand-500/10 px-2 py-1 rounded-full">{notifCount} mới</span>
                    </div>
                    <div className="max-h-[320px] overflow-y-auto divide-y divide-gray-100 dark:divide-surface-border">
                      {notifications.length === 0 ? (
                        <p className="p-8 text-sm text-gray-500 dark:text-gray-400 text-center">Không có thông báo mới</p>
                      ) : (
                        notifications.slice(0, 8).map((n) => (
                          <div
                            key={n.notificationId}
                            onClick={() => handleNotificationClick(n)}
                            className={cn(
                              'p-4 hover:bg-gray-50 dark:hover:bg-surface-border/50 cursor-pointer transition-colors',
                              !n.read && 'bg-brand-50/50 dark:bg-brand-500/5'
                            )}
                          >
                            <div className="flex items-start gap-3">
                              {!n.read && <div className="w-2 h-2 rounded-full bg-brand-500 mt-1.5 shrink-0" />}
                              <div>
                                <p className={cn("text-sm mb-1", !n.read ? "font-bold text-gray-900 dark:text-white" : "font-semibold text-gray-700 dark:text-gray-300")}>{n.title}</p>
                                <p className="text-sm text-gray-500 dark:text-gray-400 line-clamp-2 leading-relaxed">{n.message}</p>
                                <p className="text-xs text-gray-400 dark:text-gray-500 mt-2 font-medium">
                                  {formatDistanceToNow(new Date(n.sentAt), { addSuffix: true, locale: vi })}
                                </p>
                              </div>
                            </div>
                          </div>
                        ))
                      )}
                    </div>
                  </motion.div>
                )}
              </AnimatePresence>
            </div>

            {/* Logout */}
            <button
              onClick={handleLogout}
              className="w-10 h-10 rounded-full flex items-center justify-center bg-white dark:bg-surface-card border border-gray-200 dark:border-surface-border text-gray-600 dark:text-gray-300 hover:bg-red-50 dark:hover:bg-red-500/10 hover:text-red-500 hover:border-red-200 transition-colors shadow-sm"
              title="Đăng xuất"
            >
              <RiLogoutBoxLine size={18} />
            </button>
          </div>
        </header>

        {/* Page content */}
        <main className="flex-1 overflow-y-auto no-scrollbar relative z-0">
          {tripLoading ? (
            <div className="flex items-center justify-center h-full">
              <div className="flex flex-col items-center gap-4">
                <div className="w-10 h-10 border-4 border-brand-500/20 border-t-brand-500 rounded-full animate-spin" />
                <p className="text-sm text-gray-500 font-medium animate-pulse">Đang tải dữ liệu...</p>
              </div>
            </div>
          ) : (
            <Outlet />
          )}
        </main>
      </div>

      {/* Modal chi tiết thông báo */}
      <Modal
        isOpen={!!selectedNotif}
        onClose={() => setSelectedNotif(null)}
        title={selectedNotif?.title || 'Thông báo hệ thống'}
      >
        <div className="space-y-4">
          <p className="text-sm text-gray-700 dark:text-gray-300 whitespace-pre-wrap leading-relaxed">
            {selectedNotif?.message}
          </p>
          <div className="flex justify-end border-t border-gray-100 dark:border-surface-border pt-4 mt-6">
            <button
              onClick={() => setSelectedNotif(null)}
              className="bg-gray-900 dark:bg-white text-white dark:text-gray-900 font-bold py-2.5 px-6 rounded-xl hover:scale-105 active:scale-95 transition-all shadow-md"
            >
              Đóng
            </button>
          </div>
        </div>
      </Modal>
    </div>
  )
}

export default MainLayout
