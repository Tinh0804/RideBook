import { useState, useEffect } from 'react'
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from 'recharts'
import { RiMoneyDollarCircleLine, RiCarLine, RiStarLine, RiCalendarLine } from 'react-icons/ri'
import { driverApi } from '@/features/driver/api/driverApi'
import { bookingApi } from '@/features/booking/api/bookingApi'
import { useAuthStore } from '@/store/rootStore'
import { formatCurrency } from '@/utils/currency'
import { formatDate } from '@/utils/formatDate'
import { BOOKING_STATUS } from '@/config'
import Spinner from '@/components/Elements/Spinner'
import { cn } from '@/utils/cn'

const PERIOD_TABS = [
  { value: 'week',  label: '7 ngày' },
  { value: 'month', label: 'Tháng này' },
  { value: 'all',   label: 'Tất cả' },
]

const DriverRevenuePage = () => {
  const { user } = useAuthStore()

  const [revenue,  setRevenue]  = useState(null)
  const [history,  setHistory]  = useState([])
  const [period,   setPeriod]   = useState('week')
  const [loading,  setLoading]  = useState(true)

  useEffect(() => {
    Promise.all([
      driverApi.getRevenue(),
      user?.id ? bookingApi.getDriverHistory(user.id) : Promise.resolve({ result: [] }),
    ])
      .then(([rev, hist]) => {
        setRevenue(rev)
        setHistory(hist || [])
      })
      .catch(() => {})
      .finally(() => setLoading(false))
  }, [user])

  if (loading) return <div className="flex justify-center py-16"><Spinner size="xl" /></div>

  const chartData = revenue?.details?.map(d => ({ label: d.timeLabel, value: d.revenue })) || [
    { label: 'T2', value: 0 }, { label: 'T3', value: 0 }, { label: 'T4', value: 0 },
    { label: 'T5', value: 0 }, { label: 'T6', value: 0 }, { label: 'T7', value: 0 },
    { label: 'CN', value: 0 },
  ]

  const completedTrips = history.filter((t) => t.status === BOOKING_STATUS.COMPLETED)

  const stats = [
    { label: 'Tổng thu nhập',  value: formatCurrency(revenue?.summary?.totalRevenue || 0),  icon: RiMoneyDollarCircleLine, color: 'text-brand-400',  bg: 'bg-brand-400/10'  },
    { label: 'Chuyến thành công', value: revenue?.summary?.totalTrips || completedTrips.length,                    icon: RiCarLine,               color: 'text-blue-400',   bg: 'bg-blue-400/10'   },
    { label: 'Hôm nay',        value: formatCurrency(0),  icon: RiCalendarLine,          color: 'text-purple-400', bg: 'bg-purple-400/10' },
    { label: 'Đánh giá TB',    value: `${revenue?.avgRating || '—'} ★`,           icon: RiStarLine,              color: 'text-yellow-400', bg: 'bg-yellow-400/10' },
  ]

  return (
    <div className="space-y-6">
      <div>
        <h1 className="section-title">Doanh thu & Thu nhập</h1>
        <p className="text-content-muted text-sm mt-1">Theo dõi thu nhập của bạn</p>
      </div>

      {/* Stats */}
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

      {/* Chart */}
      <div className="card p-6 space-y-4">
        <div className="flex items-center justify-between flex-wrap gap-3">
          <h2 className="font-display text-lg font-bold text-content-main">Biểu đồ thu nhập</h2>
          <div className="flex gap-1 p-1 bg-surface-dark border border-surface-border rounded-xl">
            {PERIOD_TABS.map((t) => (
              <button
                key={t.value}
                onClick={() => setPeriod(t.value)}
                className={cn(
                  'px-3 py-1.5 rounded-lg text-xs font-medium transition-all',
                  period === t.value ? 'bg-brand-500 text-content-main' : 'text-content-muted hover:text-content-muted',
                )}
              >
                {t.label}
              </button>
            ))}
          </div>
        </div>
        <ResponsiveContainer width="100%" height={200}>
          <BarChart data={chartData} margin={{ top: 0, right: 0, left: -20, bottom: 0 }}>
            <CartesianGrid strokeDasharray="3 3" stroke="#1F2937" vertical={false} />
            <XAxis dataKey="label" tick={{ fill: '#6b7280', fontSize: 11 }} axisLine={false} tickLine={false} />
            <YAxis tick={{ fill: '#6b7280', fontSize: 11 }} axisLine={false} tickLine={false}
              tickFormatter={(v) => v >= 1000 ? `${v/1000}k` : v}
            />
            <Tooltip
              contentStyle={{ background: '#111827', border: '1px solid #1F2937', borderRadius: '12px' }}
              labelStyle={{ color: '#9ca3af' }}
              formatter={(v) => [formatCurrency(v), 'Thu nhập']}
              cursor={{ fill: 'rgba(34,197,94,0.05)' }}
            />
            <Bar dataKey="value" fill="#22c55e" radius={[6, 6, 0, 0]} maxBarSize={40} />
          </BarChart>
        </ResponsiveContainer>
      </div>

      {/* Trip history table */}
      <div className="space-y-3">
        <h2 className="font-display text-lg font-bold text-content-main">Lịch sử chuyến đi</h2>
        {history.length === 0 ? (
          <div className="card p-8 text-center text-content-muted">Chưa có chuyến đi nào</div>
        ) : (
          <div className="space-y-2">
            {history.map((trip) => (
              <div key={trip.id} className="card p-4 flex items-center gap-4">
                <div className={cn(
                  'w-10 h-10 rounded-xl flex items-center justify-center shrink-0',
                  trip.status === BOOKING_STATUS.COMPLETED ? 'bg-brand-500/15' : 'bg-red-500/15',
                )}>
                  <RiCarLine size={18} className={trip.status === BOOKING_STATUS.COMPLETED ? 'text-brand-400' : 'text-red-400'} />
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium text-content-main truncate">{trip.dropoffLocation}</p>
                  <p className="text-xs text-content-muted">{formatDate(trip.bookingTime)}</p>
                </div>
                <div className="text-right shrink-0">
                  <p className="font-semibold text-brand-400">{formatCurrency(trip.totalPrice)}</p>
                  <p className="text-xs text-gray-600">{trip.paymentMethod === 'CASH' ? 'Tiền mặt' : 'Online'}</p>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}

export default DriverRevenuePage
