import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  RiCarLine, RiMoneyDollarCircleLine, RiStarLine,
  RiMapPinLine, RiToggleLine, RiToggleFill, RiArrowRightLine,
  RiDashboardFill, RiTimeLine, RiFundsLine
} from 'react-icons/ri'
import { useDriverStore, useAuthStore } from '@/store/rootStore'
import { driverApi } from '@/features/driver/api/driverApi'
import { formatCurrency } from '@/utils/currency'
import { formatDate } from '@/utils/formatDate'
import Spinner from '@/components/Elements/Spinner'
import { cn } from '@/utils/cn'

const DriverDashboardPage = () => {
  const navigate = useNavigate()
  const { user, userProfile, updateUserProfile } = useAuthStore()
  const { isOnline, setOnline } = useDriverStore()

  const [dashboard, setDashboard] = useState(null)
  const [loading,   setLoading]   = useState(true)
  const [toggling,  setToggling]  = useState(false)

  useEffect(() => {
    if (userProfile?.activityStatus !== undefined && userProfile?.activityStatus !== null) {
      setOnline(userProfile.activityStatus)
    }
    driverApi.getDashboard()
      .then((dashboard) => setDashboard(dashboard))
      .catch(() => {})
      .finally(() => setLoading(false))
  }, [userProfile, setOnline])

  const handleToggleStatus = async () => {
    setToggling(true)
    try {
      const data = await driverApi.toggleStatus()
      const newStatus = typeof data?.result === 'boolean' ? data.result : !isOnline
      setOnline(newStatus)
      if (updateUserProfile) {
        updateUserProfile({ activityStatus: newStatus })
      }
    } catch (error) {
      console.error('Failed to toggle status:', error)
    }
    setToggling(false)
  }

  if (loading) return (
    <div className="flex justify-center py-16"><Spinner size="xl" /></div>
  )

  const stats = [
    { label: 'Tổng chuyến', value: dashboard?.totalRides || 0, icon: <RiCarLine size={24} />, color: 'text-blue-500', bg: 'bg-blue-50 dark:bg-blue-500/10' },
    { label: 'Doanh thu', value: formatCurrency(dashboard?.todayIncome || 0, true), icon: <RiMoneyDollarCircleLine size={24} />, color: 'text-brand-500', bg: 'bg-brand-50 dark:bg-brand-500/10' },
    { label: 'Đánh giá', value: `${dashboard?.averageRating || '5.0'}`, icon: <RiStarLine size={24} />, color: 'text-yellow-500', bg: 'bg-yellow-50 dark:bg-yellow-500/10' },
    { label: 'Tổng thu', value: formatCurrency(dashboard?.totalIncome || 0, true), icon: <RiFundsLine size={24} />, color: 'text-purple-500', bg: 'bg-purple-50 dark:bg-purple-500/10' },
  ]

  return (
    <div className="bg-[#e8ece3] dark:bg-surface-dark min-h-screen pb-20 w-full relative">
      {/* 1. Map Header Background */}
      <div className="absolute top-0 left-0 right-0 h-[45vh] lg:h-[55vh] z-0 overflow-hidden pointer-events-none">
        <img 
          src="/assets/images/map_bg.jpg" 
          alt="Map Background" 
          className="w-full h-full object-cover opacity-100 dark:opacity-30"
        />
        <div className="absolute inset-0 bg-gradient-to-b from-transparent via-[#e8ece3]/50 to-[#e8ece3] dark:via-surface-dark/50 dark:to-surface-dark" />
      </div>

      <div className="relative z-10 max-w-5xl mx-auto w-full px-4 pt-6 lg:pt-10">
        
        {/* User Greeting & Status */}
        <div className="flex items-center justify-between mb-8">
          <div className="bg-white/90 dark:bg-surface-card/90 backdrop-blur-md px-4 py-2 rounded-full shadow-sm border border-gray-100 dark:border-surface-border">
            <h1 className="text-lg font-bold text-gray-900 dark:text-white">
              Chào tài xế, {userProfile?.name || user?.userName?.split(' ')[0] || ''}! 👋
            </h1>
          </div>
          <div className="bg-white/90 dark:bg-surface-card/90 backdrop-blur-md px-4 py-2 rounded-full shadow-sm border border-gray-100 dark:border-surface-border flex items-center gap-2">
            <RiStarLine className="text-yellow-500" size={16} />
            <span className="font-semibold text-gray-900 dark:text-white text-sm">Hạng Bạch Kim</span>
          </div>
        </div>

        {/* 2. Status Card (Floating) */}
        <div className="bg-white dark:bg-surface-card rounded-3xl shadow-xl border border-gray-100 dark:border-surface-border p-6 md:p-8 mb-10 overflow-hidden relative group">
          <div className={cn("absolute inset-0 opacity-10 transition-colors duration-500", isOnline ? "bg-brand-500" : "bg-gray-500")} />
          <div className="relative z-10 flex flex-col md:flex-row items-center justify-between gap-6">
            <div className="flex items-center gap-5">
              <div className={cn(
                "w-16 h-16 rounded-2xl flex items-center justify-center shrink-0 shadow-lg transition-colors duration-500",
                isOnline ? "bg-brand-500 text-white shadow-brand-500/40" : "bg-gray-200 dark:bg-surface-dark text-gray-400"
              )}>
                {isOnline ? <RiDashboardFill size={32} /> : <RiToggleLine size={32} />}
              </div>
              <div>
                <h2 className="text-2xl font-bold text-gray-900 dark:text-white mb-1">
                  {isOnline ? 'Đang trực tuyến' : 'Đang ngoại tuyến'}
                </h2>
                <p className="text-gray-500 dark:text-gray-400 font-medium">
                  {isOnline ? 'Sẵn sàng nhận các chuyến đi mới quanh bạn.' : 'Bật trạng thái để bắt đầu làm việc và kiếm thêm thu nhập.'}
                </p>
              </div>
            </div>
            
            <button
              onClick={handleToggleStatus}
              disabled={toggling}
              className={cn(
                'flex items-center justify-center gap-2 px-8 py-4 rounded-2xl font-bold text-base transition-all duration-300 shadow-lg shrink-0 w-full md:w-auto',
                isOnline
                  ? 'bg-gray-100 text-gray-700 hover:bg-gray-200 dark:bg-surface-dark dark:text-white dark:hover:bg-surface-border'
                  : 'bg-brand-500 text-white hover:bg-brand-400 hover:shadow-brand-500/40'
              )}
            >
              {toggling
                ? <Spinner size="sm" className={isOnline ? "text-gray-500" : "text-white"} />
                : isOnline
                  ? <RiToggleFill size={24} className="text-gray-700 dark:text-white" />
                  : <RiToggleLine size={24} />
              }
              {isOnline ? 'Tắt nhận chuyến' : 'Bật nhận chuyến'}
            </button>
          </div>
        </div>

        {/* 3. Main Stats Grid */}
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 md:gap-6 mb-10">
          {stats.map((s) => (
            <div key={s.label} className="bg-white dark:bg-surface-card rounded-2xl shadow-sm border border-gray-100 dark:border-surface-border p-5 flex flex-col gap-3 hover:shadow-md transition-shadow">
              <div className={cn('w-12 h-12 rounded-xl flex items-center justify-center', s.bg, s.color)}>
                {s.icon}
              </div>
              <div>
                <div className="text-2xl font-bold text-gray-900 dark:text-white">{s.value}</div>
                <div className="text-sm font-semibold text-gray-500 dark:text-gray-400 mt-1">{s.label}</div>
              </div>
            </div>
          ))}
        </div>

        {/* 4. Practical Layout below: Left Col (Performance & Recent), Right Col (Demand Banner) */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          
          {/* Left Col */}
          <div className="lg:col-span-2 space-y-6">
            
            {/* Performance Metrics */}
            <div className="bg-white dark:bg-surface-card rounded-2xl shadow-sm border border-gray-100 dark:border-surface-border p-6">
              <h3 className="text-lg font-bold text-gray-900 dark:text-white mb-6">Hiệu suất hoạt động</h3>
              <div className="grid grid-cols-1 sm:grid-cols-3 gap-6 md:gap-8">
                {/* Acceptance Rate */}
                <div className="space-y-3">
                  <div className="flex justify-between items-end">
                    <span className="text-gray-500 dark:text-gray-400 font-semibold text-sm">Tỷ lệ nhận</span>
                    <span className="font-bold text-xl text-brand-500">{dashboard?.acceptanceRate || 95}%</span>
                  </div>
                  <div className="h-2 bg-gray-100 dark:bg-surface-dark rounded-full overflow-hidden">
                    <div className="h-full bg-brand-500 rounded-full" style={{ width: `${dashboard?.acceptanceRate || 95}%` }} />
                  </div>
                </div>
                
                {/* Completion Rate */}
                <div className="space-y-3">
                  <div className="flex justify-between items-end">
                    <span className="text-gray-500 dark:text-gray-400 font-semibold text-sm">Hoàn thành</span>
                    <span className="font-bold text-xl text-blue-500">{dashboard?.completionRate || 98}%</span>
                  </div>
                  <div className="h-2 bg-gray-100 dark:bg-surface-dark rounded-full overflow-hidden">
                    <div className="h-full bg-blue-500 rounded-full" style={{ width: `${dashboard?.completionRate || 98}%` }} />
                  </div>
                </div>

                {/* Cancellation Rate */}
                <div className="space-y-3">
                  <div className="flex justify-between items-end">
                    <span className="text-gray-500 dark:text-gray-400 font-semibold text-sm">Tỷ lệ hủy</span>
                    <span className="font-bold text-xl text-red-500">{dashboard?.cancellationRate || 2}%</span>
                  </div>
                  <div className="h-2 bg-gray-100 dark:bg-surface-dark rounded-full overflow-hidden">
                    <div className="h-full bg-red-500 rounded-full" style={{ width: `${dashboard?.cancellationRate || 2}%` }} />
                  </div>
                </div>
              </div>
            </div>

            {/* Recent trips */}
            <div className="bg-white dark:bg-surface-card rounded-2xl shadow-sm border border-gray-100 dark:border-surface-border p-6 h-full">
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-lg font-bold text-gray-900 dark:text-white">Chuyến đi gần đây</h3>
                <button onClick={() => navigate('/driver/revenue')} className="text-brand-500 font-semibold text-sm hover:underline">Xem tất cả</button>
              </div>

              {dashboard?.recentTrips?.length > 0 ? (
                <div className="space-y-5">
                  {dashboard.recentTrips.slice(0, 3).map((trip) => (
                    <div key={trip.id} className="group">
                      <div className="flex items-start justify-between mb-2">
                        <div className="flex items-center gap-1.5 text-xs text-gray-500 dark:text-gray-400">
                          <RiTimeLine size={14} />
                          {formatDate(trip.bookingTime)}
                        </div>
                        <span className="text-sm font-bold text-gray-900 dark:text-white">
                          {formatCurrency(trip.totalPrice)}
                        </span>
                      </div>
                      
                      <div className="flex items-start gap-3">
                        <div className="mt-1 flex flex-col items-center">
                          <div className="w-2 h-2 rounded-full bg-brand-500" />
                          <div className="w-px h-6 bg-gray-300 dark:bg-surface-border my-1" />
                          <div className="w-2 h-2 rounded-full bg-blue-500" />
                        </div>
                        <div className="flex-1 min-w-0">
                          <p className="text-sm text-gray-500 dark:text-gray-400 truncate mb-3">{trip.pickupLocation}</p>
                          <p className="text-sm font-medium text-gray-900 dark:text-white truncate">{trip.dropoffLocation}</p>
                        </div>
                      </div>
                      <div className="border-b border-gray-100 dark:border-surface-border mt-5 last:hidden" />
                    </div>
                  ))}
                </div>
              ) : (
                <div className="text-center py-10">
                  <div className="w-12 h-12 rounded-full bg-gray-100 dark:bg-surface-dark flex items-center justify-center mx-auto mb-3">
                    <RiCarLine size={24} className="text-gray-400 dark:text-gray-500" />
                  </div>
                  <p className="text-gray-500 dark:text-gray-400 text-sm">Chưa có chuyến đi nào hôm nay.</p>
                </div>
              )}
            </div>
          </div>

          {/* Right Col: Demand Area Banner */}
          <div className="space-y-6">
            {isOnline ? (
              <div 
                onClick={() => navigate('/driver/trips')}
                className="w-full rounded-2xl overflow-hidden shadow-sm cursor-pointer relative group aspect-square lg:aspect-[3/4]"
              >
                <img 
                  src="/assets/images/map_bg.jpg" 
                  alt="Hotspot" 
                  className="w-full h-full object-cover transition-transform duration-700 group-hover:scale-105"
                />
                <div className="absolute inset-0 bg-gradient-to-t from-gray-900 via-gray-900/60 to-transparent flex flex-col justify-end p-6">
                  <div className="flex items-center gap-2 mb-3">
                    <span className="relative flex h-3 w-3">
                      <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-brand-400 opacity-75"></span>
                      <span className="relative inline-flex rounded-full h-3 w-3 bg-brand-500"></span>
                    </span>
                    <span className="text-brand-400 font-bold text-sm tracking-wide uppercase">Nhu cầu cao</span>
                  </div>
                  <h4 className="text-white text-2xl font-bold mb-2">Khu vực trung tâm</h4>
                  <p className="text-white/80 mb-6 text-sm">Đang có {dashboard?.availableBookings || 5} chuyến xe chờ tài xế quanh khu vực của bạn.</p>
                  
                  <button className="w-full bg-white text-gray-900 py-3 rounded-xl font-bold text-sm hover:bg-gray-100 transition-colors flex items-center justify-center gap-2">
                    Xem cuốc xe <RiArrowRightLine size={18} />
                  </button>
                </div>
              </div>
            ) : (
              <div className="bg-white dark:bg-surface-card rounded-2xl shadow-sm border border-gray-100 dark:border-surface-border p-8 text-center h-full flex flex-col items-center justify-center">
                <div className="w-20 h-20 rounded-full bg-gray-100 dark:bg-surface-dark flex items-center justify-center mb-6">
                  <RiToggleLine size={40} className="text-gray-400" />
                </div>
                <h3 className="text-xl font-bold text-gray-900 dark:text-white mb-3">Chưa sẵn sàng</h3>
                <p className="text-gray-500 dark:text-gray-400 text-sm mb-8">Bật trực tuyến để nhận cuốc xe và tăng thu nhập ngay hôm nay!</p>
                <button
                  onClick={handleToggleStatus}
                  className="w-full bg-brand-500 hover:bg-brand-400 text-white font-bold py-3.5 px-4 rounded-xl transition-all duration-200 shadow-lg shadow-brand-500/25"
                >
                  Bật nhận chuyến ngay
                </button>
              </div>
            )}
            
            {/* System Alerts */}
            <div className="bg-blue-50 dark:bg-blue-500/10 border border-blue-100 dark:border-blue-500/20 rounded-2xl p-5 flex items-start gap-3">
              <RiStarLine size={20} className="text-blue-500 shrink-0 mt-0.5" />
              <p className="text-sm font-medium text-blue-800 dark:text-blue-300">
                Thưởng thêm <span className="font-bold">50.000đ</span> khi hoàn thành 5 chuyến trước 17:00 hôm nay.
              </p>
            </div>
          </div>

        </div>
      </div>
    </div>
  )
}

export default DriverDashboardPage

