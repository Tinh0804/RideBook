import { useState, useEffect } from 'react'
import {
  RiUserLine, RiCarLine, RiMapPinLine, RiMoneyDollarCircleLine,
  RiArrowUpLine, RiArrowDownLine, RiSearchLine, RiFilterLine,
} from 'react-icons/ri'
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, LineChart, Line, CartesianGrid } from 'recharts'
import { customerApi } from '@/features/customer/api/customerApi'
import { driverApi }   from '@/features/driver/api/driverApi'
import { bookingApi }  from '@/features/booking/api/bookingApi'
import { adminApi }    from '@/features/admin/api/adminApi'
import { formatCurrency } from '@/utils/currency'
import { formatDate }     from '@/utils/formatDate'
import { BOOKING_STATUS, BOOKING_STATUS_LABEL } from '@/config'
import Spinner from '@/components/Elements/Spinner'
import { cn } from '@/utils/cn'

const MOCK_CHART = [
  { month: 'T1', revenue: 12000000, trips: 140 },
  { month: 'T2', revenue: 18500000, trips: 210 },
  { month: 'T3', revenue: 15200000, trips: 180 },
  { month: 'T4', revenue: 22000000, trips: 260 },
  { month: 'T5', revenue: 19800000, trips: 235 },
  { month: 'T6', revenue: 27500000, trips: 310 },
]

const AdminDashboardPage = () => {
  const [period, setPeriod] = useState('YEAR')
  const [year, setYear] = useState(new Date().getFullYear())
  const [statsData, setStatsData] = useState(null)
  const [customers, setCustomers] = useState([])
  const [drivers,   setDrivers]   = useState([])
  const [bookings,  setBookings]  = useState([])
  const [loading,   setLoading]   = useState(true)
  const [statsLoading, setStatsLoading] = useState(false)
  const [activeTab, setActiveTab] = useState('overview')

  useEffect(() => {
    Promise.allSettled([
      customerApi.getAllForAdmin(0, 20),
      driverApi.getAll(),
      bookingApi.getAll(),
    ]).then(([c, d, b]) => {
      setCustomers(c.value?.content || c.value || [])
      setDrivers(d.value || [])
      setBookings(b.value || [])
    }).finally(() => setLoading(false))
  }, [])

  useEffect(() => {
    setStatsLoading(true)
    adminApi.getOverviewStats(period, year)
      .then((data) => {
        setStatsData(data)
      })
      .catch((err) => {
        console.error('Error fetching admin stats', err)
      })
      .finally(() => setStatsLoading(false))
  }, [period, year])

  const stats = [
    { label: 'Tổng khách hàng', value: statsData?.totalCustomers || 0, icon: RiUserLine,                  color: 'text-blue-400',   bg: 'bg-blue-400/10',   trend: '+12%' },
    { label: 'Tổng tài xế',     value: statsData?.totalDrivers || 0,   icon: RiCarLine,                   color: 'text-brand-400',  bg: 'bg-brand-400/10',  trend: '+8%'  },
    { label: 'Tổng chuyến đi',  value: statsData?.totalBookings || 0,  icon: RiMapPinLine,                color: 'text-purple-400', bg: 'bg-purple-400/10', trend: '+24%' },
    { label: 'Doanh thu',       value: formatCurrency(statsData?.totalRevenue || 0, true), icon: RiMoneyDollarCircleLine, color: 'text-yellow-400', bg: 'bg-yellow-400/10', trend: '+18%' },
  ]

  const TABS = [
    { value: 'overview',   label: 'Tổng quan'   },
    { value: 'customers',  label: 'Khách hàng'  },
    { value: 'drivers',    label: 'Tài xế'      },
    { value: 'bookings',   label: 'Chuyến đi'   },
  ]

  const STATUS_BADGE_MAP = {
    [BOOKING_STATUS.PENDING]: 'badge-gray',
    [BOOKING_STATUS.ACCEPTED]: 'badge-blue',
    [BOOKING_STATUS.ARRIVED]: 'badge-blue',
    [BOOKING_STATUS.IN_PROGRESS]: 'badge-blue',
    [BOOKING_STATUS.COMPLETED]: 'badge-green',
    [BOOKING_STATUS.CANCELLED]: 'badge-red',
    default: 'badge-gray',
  }

  if (loading) return <div className="flex justify-center py-16"><Spinner size="xl" /></div>

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="section-title">Admin Dashboard</h1>
          <p className="text-content-muted text-sm mt-1">Tổng quan hoạt động hệ thống BookCar</p>
        </div>
        
        {/* Period Filter */}
        <div className="flex items-center gap-2">
          <span className="text-xs text-content-muted">Lọc theo:</span>
          <div className="flex gap-1 p-1 bg-surface-dark border border-surface-border rounded-lg">
            {[
              { value: 'DAY', label: 'Ngày' },
              { value: 'WEEK', label: 'Tuần' },
              { value: 'MONTH', label: 'Tháng' },
              { value: 'YEAR', label: 'Năm' },
            ].map((p) => (
              <button
                key={p.value}
                onClick={() => setPeriod(p.value)}
                className={cn(
                  'px-3 py-1.5 rounded-md text-xs font-medium transition-all',
                  period === p.value ? 'bg-brand-500 text-content-main' : 'text-content-muted hover:text-content-main'
                )}
              >
                {p.label}
              </button>
            ))}
          </div>
          {period === 'YEAR' && (
            <select
              value={year}
              onChange={(e) => setYear(Number(e.target.value))}
              className="bg-surface-dark border border-surface-border text-content-main text-xs rounded-lg px-2.5 py-1.5 focus:outline-none focus:border-brand-500 cursor-pointer"
            >
              {[2024, 2025, 2026, 2027].map((y) => (
                <option key={y} value={y}>{y}</option>
              ))}
            </select>
          )}
        </div>
      </div>

      {/* Stats grid */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        {stats.map((s) => (
          <div key={s.label} className="stat-card">
            <div className="flex items-center justify-between">
              <div className={cn('w-10 h-10 rounded-xl flex items-center justify-center', s.bg)}>
                <s.icon size={20} className={s.color} />
              </div>
              <span className="flex items-center gap-1 text-xs font-semibold text-brand-400">
                <RiArrowUpLine size={12} />{s.trend}
              </span>
            </div>
            <div className="stat-value mt-3">{s.value}</div>
            <div className="stat-label">{s.label}</div>
          </div>
        ))}
      </div>

      {/* Tabs */}
      <div className="flex gap-1 p-1 bg-surface-dark border border-surface-border rounded-xl w-fit flex-wrap">
        {TABS.map((t) => (
          <button key={t.value} onClick={() => setActiveTab(t.value)}
            className={cn(
              'px-4 py-2 rounded-lg text-sm font-medium transition-all',
              activeTab === t.value ? 'bg-brand-500 text-content-main' : 'text-content-muted hover:text-content-muted',
            )}
          >
            {t.label}
          </button>
        ))}
      </div>

      {/* Overview charts */}
      {activeTab === 'overview' && (
        <div className="grid lg:grid-cols-2 gap-6">
          <div className="card p-6 space-y-4 relative">
            {statsLoading && (
              <div className="absolute inset-0 bg-surface-dark/40 backdrop-blur-[1px] flex items-center justify-center rounded-2xl z-10">
                <Spinner size="md" />
              </div>
            )}
            <h2 className="font-display text-base font-bold text-content-main">
              {period === 'DAY' && 'Doanh thu hôm nay (theo giờ)'}
              {period === 'WEEK' && 'Doanh thu tuần này (theo thứ)'}
              {period === 'MONTH' && 'Doanh thu tháng này (theo ngày)'}
              {period === 'YEAR' && `Doanh thu theo tháng (Năm ${year})`}
            </h2>
            <ResponsiveContainer width="100%" height={200}>
              <BarChart data={statsData?.revenueByMonth || []} margin={{ left: -20, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="#1F2937" vertical={false} />
                <XAxis dataKey="month" tick={{ fill: '#6b7280', fontSize: 11 }} axisLine={false} tickLine={false} />
                <YAxis tick={{ fill: '#6b7280', fontSize: 11 }} axisLine={false} tickLine={false}
                  tickFormatter={(v) => v >= 1000000 ? `${(v/1000000).toFixed(0)}M` : v >= 1000 ? `${(v/1000).toFixed(0)}K` : v}
                />
                <Tooltip contentStyle={{ background: '#111827', border: '1px solid #1F2937', borderRadius: '12px' }}
                  formatter={(v) => [formatCurrency(v), 'Doanh thu']} />
                <Bar dataKey="value" fill="#22c55e" radius={[6,6,0,0]} maxBarSize={36} />
              </BarChart>
            </ResponsiveContainer>
          </div>

          <div className="card p-6 space-y-4 relative">
            {statsLoading && (
              <div className="absolute inset-0 bg-surface-dark/40 backdrop-blur-[1px] flex items-center justify-center rounded-2xl z-10">
                <Spinner size="md" />
              </div>
            )}
            <h2 className="font-display text-base font-bold text-content-main">
              {period === 'DAY' && 'Số chuyến đi hôm nay (theo giờ)'}
              {period === 'WEEK' && 'Số chuyến đi tuần này (theo thứ)'}
              {period === 'MONTH' && 'Số chuyến đi tháng này (theo ngày)'}
              {period === 'YEAR' && `Số chuyến đi theo tháng (Năm ${year})`}
            </h2>
            <ResponsiveContainer width="100%" height={200}>
              <LineChart data={statsData?.tripsByMonth || []} margin={{ left: -20, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="#1F2937" />
                <XAxis dataKey="month" tick={{ fill: '#6b7280', fontSize: 11 }} axisLine={false} tickLine={false} />
                <YAxis tick={{ fill: '#6b7280', fontSize: 11 }} axisLine={false} tickLine={false} />
                <Tooltip contentStyle={{ background: '#111827', border: '1px solid #1F2937', borderRadius: '12px' }}
                  formatter={(v) => [v, 'Chuyến đi']} />
                <Line type="monotone" dataKey="value" stroke="#a78bfa" strokeWidth={2} dot={{ fill: '#a78bfa', r: 4 }} />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>
      )}

      {/* Customers table */}
      {activeTab === 'customers' && (
        <div className="card overflow-hidden">
          <div className="p-5 border-b border-surface-border flex items-center justify-between gap-4 flex-wrap">
            <h2 className="font-display text-lg font-bold text-content-main">Danh sách khách hàng ({customers.length})</h2>
            <div className="flex items-center gap-2">
              <div className="relative">
                <RiSearchLine className="absolute left-3 top-1/2 -translate-y-1/2 text-content-muted" size={16} />
                <input className="input-field pl-9 py-2 text-xs w-52" placeholder="Tìm kiếm..." />
              </div>
            </div>
          </div>
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-surface-border">
                  {['#', 'Tên', 'Số điện thoại', 'Địa chỉ', 'Ngày tạo'].map((h) => (
                    <th key={h} className="text-left px-5 py-3 text-xs font-semibold text-content-muted uppercase tracking-wider">{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-surface-border">
                {customers.slice(0, 20).map((c, i) => (
                  <tr key={c.id} className="hover:bg-surface-border/20 transition-colors">
                    <td className="px-5 py-3 text-gray-600 font-mono text-xs">{i + 1}</td>
                    <td className="px-5 py-3">
                      <div className="flex items-center gap-3">
                        <div className="w-8 h-8 rounded-full bg-blue-500/20 border border-blue-500/30 flex items-center justify-center text-xs font-bold text-blue-400 overflow-hidden shrink-0">
                          {c.avatar ? <img src={c.avatar} alt={c.name} className="w-full h-full object-cover" /> : c.name?.[0] || '?'}
                        </div>
                        <div>
                          <p className="font-medium text-content-main">{c.name}</p>
                          <p className="text-xs text-content-muted">@{c.userName}</p>
                        </div>
                      </div>
                    </td>
                    <td className="px-5 py-3 text-content-muted font-mono text-xs">{c.phoneNumber || '—'}</td>
                    <td className="px-5 py-3 text-content-muted text-xs max-w-xs truncate">{c.address || '—'}</td>
                    <td className="px-5 py-3 text-content-muted text-xs">{formatDate(c.createdAt, 'dd/MM/yyyy')}</td>
                  </tr>
                ))}
              </tbody>
            </table>
            {customers.length === 0 && (
              <p className="text-center text-content-muted py-8">Chưa có dữ liệu</p>
            )}
          </div>
        </div>
      )}

      {/* Drivers table */}
      {activeTab === 'drivers' && (
        <div className="card overflow-hidden">
          <div className="p-5 border-b border-surface-border">
            <h2 className="font-display text-lg font-bold text-content-main">Danh sách tài xế ({drivers.length})</h2>
          </div>
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-surface-border">
                  {['#', 'Tên tài xế', 'Số điện thoại', 'Loại xe', 'Biển số', 'Khu vực', 'Trạng thái'].map((h) => (
                    <th key={h} className="text-left px-5 py-3 text-xs font-semibold text-content-muted uppercase tracking-wider">{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-surface-border">
                {drivers.slice(0, 20).map((d, i) => (
                  <tr key={d.id} className="hover:bg-surface-border/20 transition-colors">
                    <td className="px-5 py-3 text-gray-600 font-mono text-xs">{i + 1}</td>
                    <td className="px-5 py-3">
                      <div className="flex items-center gap-3">
                        <div className="w-8 h-8 rounded-full bg-brand-500/20 border border-brand-500/30 flex items-center justify-center text-xs font-bold text-brand-400 overflow-hidden shrink-0">
                          {d.avatar ? <img src={d.avatar} alt={d.driverName} className="w-full h-full object-cover" /> : d.driverName?.[0] || '?'}
                        </div>
                        <p className="font-medium text-content-main">{d.driverName || d.name}</p>
                      </div>
                    </td>
                    <td className="px-5 py-3 text-content-muted font-mono text-xs">{d.phone || '—'}</td>
                    <td className="px-5 py-3 text-content-muted text-xs">{d.vehicleType?.name || d.vehicleName || '—'}</td>
                    <td className="px-5 py-3 text-content-muted font-mono text-xs">{d.licensePlate || '—'}</td>
                    <td className="px-5 py-3 text-content-muted text-xs">{d.area || '—'}</td>
                    <td className="px-5 py-3">
                      <span className={cn('badge', d.isOnline ? 'badge-green' : 'badge-gray')}>
                        {d.isOnline ? 'Online' : 'Offline'}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            {drivers.length === 0 && (
              <p className="text-center text-content-muted py-8">Chưa có dữ liệu</p>
            )}
          </div>
        </div>
      )}

      {/* Bookings table */}
      {activeTab === 'bookings' && (
        <div className="card overflow-hidden">
          <div className="p-5 border-b border-surface-border flex items-center justify-between flex-wrap gap-3">
            <h2 className="font-display text-lg font-bold text-content-main">Lịch sử tất cả chuyến đi ({bookings.length})</h2>
            <button className="flex items-center gap-1.5 text-xs text-content-muted hover:text-content-main border border-surface-border hover:border-brand-500/40 px-3 py-1.5 rounded-lg transition-colors">
              <RiFilterLine size={14} /> Lọc
            </button>
          </div>
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-surface-border">
                  {['Mã đặt xe', 'Điểm đón → Đến', 'Khách hàng', 'Tài xế', 'Phí', 'Trạng thái', 'Ngày'].map((h) => (
                    <th key={h} className="text-left px-5 py-3 text-xs font-semibold text-content-muted uppercase tracking-wider whitespace-nowrap">{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-surface-border">
                {bookings.slice(0, 30).map((b) => (
                  <tr key={b.id} className="hover:bg-surface-border/20 transition-colors">
                    <td className="px-5 py-3 font-mono text-xs text-content-muted">#{b.id?.slice(-8)}</td>
                    <td className="px-5 py-3 max-w-xs">
                      <p className="text-xs text-content-main truncate">{b.pickupLocation}</p>
                      <p className="text-xs text-content-muted truncate mt-0.5">→ {b.dropoffLocation}</p>
                    </td>
                    <td className="px-5 py-3 text-xs text-content-muted">{b.customer?.name || b.customerId || '—'}</td>
                    <td className="px-5 py-3 text-xs text-content-muted">{b.driver?.driverName || b.driver?.name || '—'}</td>
                    <td className="px-5 py-3 font-semibold text-brand-400 text-xs whitespace-nowrap">{formatCurrency(b.totalFare)}</td>
                    <td className="px-5 py-3">
                      <span className={cn('badge', STATUS_BADGE_MAP[b.status] || STATUS_BADGE_MAP.default)}>
                        {BOOKING_STATUS_LABEL[b.status] || b.status}
                      </span>
                    </td>
                    <td className="px-5 py-3 text-xs text-content-muted whitespace-nowrap">{formatDate(b.createdAt, 'dd/MM/yy HH:mm')}</td>
                  </tr>
                ))}
              </tbody>
            </table>
            {bookings.length === 0 && (
              <p className="text-center text-content-muted py-8">Chưa có dữ liệu</p>
            )}
          </div>
        </div>
      )}
    </div>
  )
}

export default AdminDashboardPage
