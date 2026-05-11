import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  RiCarLine, RiMoneyDollarCircleLine, RiStarLine,
  RiMapPinLine, RiToggleLine, RiToggleFill, RiArrowRightLine,
} from 'react-icons/ri'
import { AreaChart, Area, XAxis, YAxis, Tooltip, ResponsiveContainer } from 'recharts'
import { useDriverStore, useAuthStore } from '@/store/rootStore'
import { driverApi } from '@/features/driver/api/driverApi'
import { formatCurrency } from '@/utils/currency'
import { formatDate } from '@/utils/formatDate'
import Spinner from '@/components/Elements/Spinner'
import { cn } from '@/utils/cn'

const DriverDashboardPage = () => {
  const navigate = useNavigate()
  const { user } = useAuthStore()
  const { isOnline, setOnline } = useDriverStore()

  const [dashboard, setDashboard] = useState(null)
  const [loading,   setLoading]   = useState(true)
  const [toggling,  setToggling]  = useState(false)

  useEffect(() => {
    driverApi.getDashboard()
      .then((dashboard) => setDashboard(dashboard))
      .catch(() => {})
      .finally(() => setLoading(false))
  }, [])

  const handleToggleStatus = async () => {
    setToggling(true)
    try {
      const data = await driverApi.toggleStatus()
      const newStatus = data?.result?.isOnline ?? !isOnline
      setOnline(newStatus)
    } catch {}
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

  const chartData = dashboard?.weeklyRevenue || [
    { day: 'T2', revenue: 0 }, { day: 'T3', revenue: 0 }, { day: 'T4', revenue: 0 },
    { day: 'T5', revenue: 0 }, { day: 'T6', revenue: 0 }, { day: 'T7', revenue: 0 },
    { day: 'CN', revenue: 0 },
  ]

  return (
    <div className="space-y-8">
      {/* Greeting + online toggle */}
      <div className="flex items-start justify-between gap-4 flex-wrap">
        <div>
          <p className="text-content-muted text-sm">Xin chào 👋</p>
          <h1 className="font-display text-3xl font-bold text-content-main mt-1">
            {user?.name || user?.userName}
          </h1>
          <p className="text-content-muted text-sm mt-1">
            {isOnline ? 'Bạn đang hoạt động, sẵn sàng nhận chuyến' : 'Bạn đang ngoại tuyến'}
          </p>
        </div>
        <button
          onClick={handleToggleStatus}
          disabled={toggling}
          className={cn(
            'flex items-center gap-3 px-5 py-3 rounded-2xl border font-semibold text-sm transition-all duration-300',
            isOnline
              ? 'bg-brand-500/15 border-brand-500/40 text-brand-400 hover:bg-brand-500/25'
              : 'bg-surface-border border-surface-muted text-content-muted hover:text-content-muted hover:border-gray-500',
          )}
        >
          {toggling
            ? <Spinner size="sm" />
            : isOnline
              ? <RiToggleFill size={24} className="text-brand-400" />
              : <RiToggleLine size={24} />
          }
          {isOnline ? 'Đang hoạt động' : 'Ngoại tuyến'}
        </button>
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

      {/* Revenue chart */}
      <div className="card p-6 space-y-4">
        <div className="flex items-center justify-between">
          <h2 className="font-display text-lg font-bold text-content-main">Doanh thu 7 ngày qua</h2>
          <button
            onClick={() => navigate('/driver/revenue')}
            className="text-xs text-brand-400 hover:text-brand-300 flex items-center gap-1 transition-colors"
          >
            Chi tiết <RiArrowRightLine size={12} />
          </button>
        </div>
        <ResponsiveContainer width="100%" height={180}>
          <AreaChart data={chartData} margin={{ top: 5, right: 0, left: -20, bottom: 0 }}>
            <defs>
              <linearGradient id="colorRevenue" x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%"  stopColor="#22c55e" stopOpacity={0.3} />
                <stop offset="95%" stopColor="#22c55e" stopOpacity={0} />
              </linearGradient>
            </defs>
            <XAxis dataKey="day"     tick={{ fill: '#6b7280', fontSize: 11 }} axisLine={false} tickLine={false} />
            <YAxis                   tick={{ fill: '#6b7280', fontSize: 11 }} axisLine={false} tickLine={false}
              tickFormatter={(v) => v >= 1000 ? `${v/1000}k` : v}
            />
            <Tooltip
              contentStyle={{ background: '#111827', border: '1px solid #1F2937', borderRadius: '12px' }}
              labelStyle={{ color: '#9ca3af' }}
              formatter={(v) => [formatCurrency(v), 'Doanh thu']}
            />
            <Area type="monotone" dataKey="revenue" stroke="#22c55e" strokeWidth={2} fill="url(#colorRevenue)" />
          </AreaChart>
        </ResponsiveContainer>
      </div>

      {/* Recent trips */}
      {dashboard?.recentTrips?.length > 0 && (
        <div className="space-y-3">
          <div className="flex items-center justify-between">
            <h2 className="font-display text-lg font-bold text-content-main">Chuyến gần đây</h2>
            <button
              onClick={() => navigate('/driver/revenue')}
              className="text-xs text-brand-400 hover:text-brand-300 flex items-center gap-1 transition-colors"
            >
              Xem tất cả <RiArrowRightLine size={12} />
            </button>
          </div>
          <div className="space-y-2">
            {dashboard.recentTrips.slice(0, 4).map((trip) => (
              <div key={trip.id} className="card p-4 flex items-center gap-4">
                <div className="w-10 h-10 rounded-xl bg-surface-border flex items-center justify-center shrink-0">
                  <RiMapPinLine size={18} className="text-content-muted" />
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium text-content-main truncate">{trip.dropoffLocation}</p>
                  <p className="text-xs text-content-muted">{formatDate(trip.bookingTime)}</p>
                </div>
                <p className="font-semibold text-brand-400 shrink-0">{formatCurrency(trip.totalPrice)}</p>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* CTA when online */}
      {isOnline && (
        <div className="card p-5 bg-gradient-to-r from-brand-900/50 to-surface-card border-brand-500/30 space-y-3">
          <p className="font-semibold text-content-main">Có {dashboard?.availableBookings || 0} chuyến chờ trong khu vực của bạn</p>
          <button
            onClick={() => navigate('/driver/trips')}
            className="flex items-center gap-2 text-brand-400 font-semibold text-sm hover:text-brand-300 transition-colors"
          >
            Xem và nhận chuyến <RiArrowRightLine size={16} />
          </button>
        </div>
      )}
    </div>
  )
}

export default DriverDashboardPage
