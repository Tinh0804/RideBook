import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  RiCarLine, RiMoneyDollarCircleLine, RiStarLine,
  RiMapPinLine, RiToggleLine, RiToggleFill, RiArrowRightLine,
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
    // Sync online status from backend user profile on mount
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
    { label: 'Tổng chuyến',    value: dashboard?.totalRides   || 0,     icon: RiCarLine,                  color: 'text-blue-400',   bg: 'bg-blue-400/10'  },
    { label: 'Doanh thu',       value: formatCurrency(dashboard?.todayIncome || 0, true), icon: RiMoneyDollarCircleLine, color: 'text-brand-400', bg: 'bg-brand-400/10' },
    { label: 'Đánh giá',        value: `${dashboard?.averageRating || '—'} ★`, icon: RiStarLine,    color: 'text-yellow-400', bg: 'bg-yellow-400/10' },
    { label: 'Tổng thu',  value: formatCurrency(dashboard?.totalIncome || 0, true),     icon: RiMoneyDollarCircleLine,  color: 'text-purple-400', bg: 'bg-purple-400/10' },
  ]


  return (
    <div className="space-y-6">
      {/* Hero Section & Daily Goal */}
      <div className={cn(
        "relative overflow-hidden rounded-3xl p-6 md:p-8 shadow-2xl transition-all duration-500 border border-white/5",
        isOnline ? "bg-gradient-to-br from-brand-600 via-brand-500 to-brand-700" : "bg-gradient-to-br from-gray-800 to-gray-900"
      )}>
        {/* Animated background elements */}
        {isOnline && (
          <>
            <div className="absolute top-0 right-0 w-64 h-64 bg-white/10 rounded-full blur-3xl animate-pulse"></div>
            <div className="absolute bottom-0 left-0 w-48 h-48 bg-white/10 rounded-full blur-2xl animate-pulse delay-1000"></div>
          </>
        )}
        
        <div className="relative z-10 flex flex-col md:flex-row items-start md:items-center justify-between gap-6">
          <div className="space-y-2">
            <p className="text-white/80 text-sm flex items-center gap-2">
              Xin chào <span className="text-xl inline-block animate-wave">👋</span>
            </p>
            <h1 className="font-display text-3xl md:text-4xl font-bold text-white">
              {userProfile?.name || user?.userName}
            </h1>
            <div className="flex items-center gap-2 mt-2 bg-black/20 w-fit px-3 py-1.5 rounded-full backdrop-blur-md">
              <span className="relative flex h-2.5 w-2.5">
                {isOnline && <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-green-400 opacity-75"></span>}
                <span className={cn("relative inline-flex rounded-full h-2.5 w-2.5", isOnline ? "bg-green-500" : "bg-gray-400")}></span>
              </span>
              <p className="text-white font-medium text-xs">
                {isOnline ? 'Sẵn sàng nhận chuyến' : 'Đang ngoại tuyến'}
              </p>
            </div>
          </div>

          <div className="flex flex-col items-start md:items-end gap-3 w-full md:w-auto">
            <button
              onClick={handleToggleStatus}
              disabled={toggling}
              className={cn(
                'flex items-center justify-center gap-2 px-6 py-3 rounded-xl font-bold text-sm transition-all duration-300 shadow-lg w-full md:w-auto',
                isOnline
                  ? 'bg-white text-brand-600 hover:bg-gray-50'
                  : 'bg-brand-500 text-white hover:bg-brand-400 hover:shadow-brand-500/30',
              )}
            >
              {toggling
                ? <Spinner size="sm" className={isOnline ? "text-brand-500" : "text-white"} />
                : isOnline
                  ? <RiToggleFill size={20} className="text-brand-500" />
                  : <RiToggleLine size={20} />
              }
              {isOnline ? 'Tắt nhận chuyến' : 'Bật nhận chuyến'}
            </button>
            
            {/* Quick daily goal progress */}
            {isOnline && (
              <div className="bg-white/10 backdrop-blur-md rounded-xl p-3 border border-white/10 w-full md:w-[220px]">
                <div className="flex justify-between text-xs text-white mb-1.5">
                  <span className="opacity-80">Mục tiêu ngày</span>
                  <span className="font-bold text-green-300">75%</span>
                </div>
                <div className="h-1.5 bg-black/20 rounded-full overflow-hidden">
                  <div className="h-full bg-gradient-to-r from-green-400 to-emerald-300 rounded-full" style={{ width: '75%' }}></div>
                </div>
                <p className="text-[10px] text-white/70 mt-1.5 text-right">Còn 150k để đạt thưởng 50k</p>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* System Alerts */}
      <div className="flex items-center gap-3 bg-gradient-to-r from-blue-500/10 to-indigo-500/10 border border-blue-500/20 text-blue-400 p-4 rounded-2xl text-sm font-medium">
        <div className="w-8 h-8 rounded-full bg-blue-500/20 flex items-center justify-center shrink-0">
          <RiStarLine size={16} className="animate-pulse" />
        </div>
        <p className="flex-1">Nhu cầu đặt xe đang tăng cao tại khu vực <span className="font-bold text-blue-300">Sân bay Tân Sơn Nhất</span>. Cước phí nhân 1.5x!</p>
      </div>


      {/* Stats grid */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        {stats.map((s) => (
          <div key={s.label} className="stat-card">
            <div className={cn('w-10 h-10 rounded-xl flex items-center justify-center', s.bg)}>
              <s.icon size={20} className={s.color} />
            </div>
            <div className="stat-value mt-2">{s.value}</div>
            <div className="stat-label">{s.label}</div>
          </div>
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 space-y-6">
          {/* Performance Metrics */}
          <div className="card p-6 space-y-6">
            <h2 className="font-display text-lg font-bold text-content-main flex items-center gap-2">
              Hiệu suất hoạt động <span className="text-xs font-normal bg-brand-500/10 text-brand-400 px-2 py-0.5 rounded-full">Tốt</span>
            </h2>
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-6">
              {/* Acceptance Rate */}
              <div className="space-y-2">
                <div className="flex justify-between items-baseline">
                  <span className="text-content-muted text-xs uppercase tracking-wider font-semibold">Tỷ lệ nhận</span>
                  <span className="font-bold text-xl text-brand-400">{dashboard?.acceptanceRate || 95}%</span>
                </div>
                <div className="h-1.5 bg-surface-border rounded-full overflow-hidden">
                  <div className="h-full bg-gradient-to-r from-brand-400 to-brand-500 rounded-full" style={{ width: `${dashboard?.acceptanceRate || 95}%` }} />
                </div>
              </div>
              
              {/* Completion Rate */}
              <div className="space-y-2">
                <div className="flex justify-between items-baseline">
                  <span className="text-content-muted text-xs uppercase tracking-wider font-semibold">Hoàn thành</span>
                  <span className="font-bold text-xl text-green-400">{dashboard?.completionRate || 98}%</span>
                </div>
                <div className="h-1.5 bg-surface-border rounded-full overflow-hidden">
                  <div className="h-full bg-gradient-to-r from-green-400 to-emerald-500 rounded-full" style={{ width: `${dashboard?.completionRate || 98}%` }} />
                </div>
              </div>

              {/* Cancellation Rate */}
              <div className="space-y-2">
                <div className="flex justify-between items-baseline">
                  <span className="text-content-muted text-xs uppercase tracking-wider font-semibold">Tỷ lệ hủy</span>
                  <span className="font-bold text-xl text-red-400">{dashboard?.cancellationRate || 2}%</span>
                </div>
                <div className="h-1.5 bg-surface-border rounded-full overflow-hidden">
                  <div className="h-full bg-gradient-to-r from-red-400 to-rose-500 rounded-full" style={{ width: `${dashboard?.cancellationRate || 2}%` }} />
                </div>
              </div>
            </div>
          </div>

          {/* Recent trips */}
          {dashboard?.recentTrips?.length > 0 && (
            <div className="space-y-4">
              <div className="flex items-center justify-between">
                <h2 className="font-display text-lg font-bold text-content-main">Chuyến gần đây</h2>
                <button
                  onClick={() => navigate('/driver/revenue')}
                  className="text-xs text-brand-400 hover:text-brand-300 flex items-center gap-1 transition-colors bg-brand-500/10 px-3 py-1.5 rounded-full"
                >
                  Xem tất cả <RiArrowRightLine size={12} />
                </button>
              </div>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {dashboard.recentTrips.slice(0, 4).map((trip) => (
                  <div key={trip.id} className="card p-4 hover:border-brand-500/30 transition-colors group cursor-pointer">
                    <div className="flex items-start justify-between mb-3">
                      <div className="w-10 h-10 rounded-xl bg-brand-500/10 flex items-center justify-center shrink-0 group-hover:scale-110 transition-transform">
                        <RiMapPinLine size={18} className="text-brand-400" />
                      </div>
                      <div className="text-right">
                        <p className="font-bold text-content-main">{formatCurrency(trip.totalPrice)}</p>
                        <p className="text-[10px] text-content-muted bg-surface-dark px-2 py-0.5 rounded-md mt-1">
                          Tiền mặt
                        </p>
                      </div>
                    </div>
                    <div>
                      <p className="text-sm font-medium text-content-main truncate mb-1">{trip.dropoffLocation}</p>
                      <p className="text-xs text-content-muted flex items-center gap-1">
                        <span>🕒</span> {formatDate(trip.bookingTime)}
                      </p>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>

        {/* Right Column (Heatmap / CTA) */}
        <div className="lg:col-span-1 space-y-6">
          {isOnline ? (
            <div className="card overflow-hidden border-brand-500/20">
              <div className="bg-gradient-to-br from-brand-900/40 to-surface-card p-5 border-b border-white/5">
                <h3 className="font-bold text-content-main flex items-center gap-2">
                  <span className="w-2 h-2 rounded-full bg-brand-400 animate-ping"></span>
                  Radar Tìm Kiếm
                </h3>
                <p className="text-sm text-content-muted mt-1">Khu vực quanh bạn đang có nhu cầu</p>
              </div>
              <div className="relative h-48 bg-[#1a1c23] overflow-hidden">
                {/* Simulated Radar/Heatmap */}
                <div className="absolute inset-0 opacity-30">
                  <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-32 h-32 border border-brand-500/30 rounded-full"></div>
                  <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-48 h-48 border border-brand-500/20 rounded-full"></div>
                  <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-64 h-64 border border-brand-500/10 rounded-full"></div>
                  
                  {/* Radar sweep */}
                  <div className="absolute top-1/2 left-1/2 w-32 h-32 origin-top-left animate-[spin_4s_linear_infinite] bg-gradient-to-br from-brand-500/40 to-transparent clip-triangle"></div>
                </div>
                
                {/* Demand spots */}
                <div className="absolute top-1/4 left-1/4 w-8 h-8 bg-red-500/20 rounded-full animate-pulse blur-md"></div>
                <div className="absolute top-1/4 left-1/4 w-2 h-2 bg-red-500 rounded-full shadow-[0_0_10px_rgba(239,68,68,1)]"></div>
                
                <div className="absolute bottom-1/3 right-1/4 w-12 h-12 bg-orange-500/20 rounded-full animate-pulse blur-md delay-700"></div>
                <div className="absolute bottom-1/3 right-1/4 w-3 h-3 bg-orange-500 rounded-full shadow-[0_0_10px_rgba(249,115,22,1)]"></div>
              </div>
              
              <div className="p-5">
                <div className="bg-brand-500/10 rounded-xl p-4 mb-4">
                  <p className="font-bold text-brand-400 text-2xl mb-1">{dashboard?.availableBookings || 0}</p>
                  <p className="text-sm text-content-main">Chuyến xe đang chờ tài xế gần bạn</p>
                </div>
                <button
                  onClick={() => navigate('/driver/trips')}
                  className="w-full bg-brand-500 hover:bg-brand-400 text-white font-bold py-3 px-4 rounded-xl transition-all duration-200 shadow-lg shadow-brand-500/25 flex items-center justify-center gap-2"
                >
                  Đến trang Nhận chuyến <RiArrowRightLine size={18} />
                </button>
              </div>
            </div>
          ) : (
            <div className="card p-6 bg-gradient-to-b from-surface-card to-surface-dark border-surface-border text-center">
              <div className="w-16 h-16 rounded-full bg-surface-muted mx-auto flex items-center justify-center mb-4">
                <RiToggleLine size={32} className="text-content-muted" />
              </div>
              <h3 className="font-bold text-content-main mb-2">Bạn đang ngoại tuyến</h3>
              <p className="text-sm text-content-muted mb-6">Bật trạng thái hoạt động để bắt đầu nhận cuốc và kiếm thêm thu nhập ngay hôm nay!</p>
              <button
                onClick={handleToggleStatus}
                disabled={toggling}
                className="w-full bg-surface-muted hover:bg-gray-700 text-content-main font-bold py-3 px-4 rounded-xl transition-all duration-200"
              >
                Bật nhận chuyến
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

export default DriverDashboardPage
