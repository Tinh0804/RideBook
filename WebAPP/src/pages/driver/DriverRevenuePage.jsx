import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from 'recharts'
import { RiMoneyDollarCircleLine, RiCarLine, RiStarLine, RiCalendarLine, RiArrowRightLine, RiBankCardLine, RiTrophyLine } from 'react-icons/ri'
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
  { value: 'all',   label: 'Năm nay' },
]

const DriverRevenuePage = () => {
  const { user, userProfile } = useAuthStore()
  const navigate = useNavigate()

  const [revenue,  setRevenue]  = useState(null)
  const [history,  setHistory]  = useState([])
  const [dailyStats, setDailyStats] = useState(null)
  const [period,   setPeriod]   = useState('week')
  const [loading,  setLoading]  = useState(true)
  const [selectedDate, setSelectedDate] = useState(new Date().toISOString().split('T')[0])

  useEffect(() => {
    const driverId = userProfile?.id || user?.id || user?.driverId;
    if (!driverId) {
      setLoading(false)
      return;
    }
    setLoading(true)
    Promise.all([
      driverApi.getRevenue(period),
      bookingApi.getDriverHistory(driverId),
      driverApi.getDailyRevenue(selectedDate)
    ])
      .then(([rev, hist, daily]) => {
        setRevenue(rev)
        setHistory(hist || [])
        setDailyStats(daily)
      })
      .catch(() => {})
      .finally(() => setLoading(false))
  }, [user, userProfile, selectedDate, period])

  if (loading) return <div className="flex justify-center py-16"><Spinner size="xl" /></div>

  const chartData = revenue?.details?.map(d => ({ label: d.timeLabel, value: d.revenue })) || []

  // === CALCULATIONS (Now from Backend) ===
  const grossRevenue = dailyStats?.grossRevenue || 0
  const netIncome = dailyStats?.netIncome || 0
  const platformFee = dailyStats?.platformFee || 0
  const cashCollected = dailyStats?.cashIncome || 0
  const onlineIncome = dailyStats?.onlineIncome || 0
  const selectedDayTripsCount = dailyStats?.totalTrips || 0
  
  const QUEST_GOAL = dailyStats?.questGoal || 10
  const QUEST_REWARD = dailyStats?.questReward || 50000
  const isQuestCompleted = dailyStats?.isQuestCompleted || false
  const questEarned = dailyStats?.questEarned || 0
  const finalIncome = dailyStats?.finalIncome || 0
  const platformFeeRate = (grossRevenue > 0) ? (platformFee / grossRevenue) : 0.20

  const questProgress = Math.min((selectedDayTripsCount / QUEST_GOAL) * 100, 100)

  return (
    <div className="space-y-6 animate-fade-in">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="section-title">Doanh thu & Thu nhập</h1>
          <p className="text-content-muted text-sm mt-1">Theo dõi thu nhập thực tế, chiết khấu và tiền thưởng</p>
        </div>
        
        {/* Global Date Picker */}
        <div className="flex items-center gap-3 bg-surface-card border border-surface-border px-4 py-2 rounded-xl shadow-sm">
          <RiCalendarLine className="text-brand-400" size={20} />
          <div className="flex flex-col">
            <span className="text-[10px] text-content-muted font-semibold uppercase tracking-wider">Chọn ngày</span>
            <input 
              type="date" 
              value={selectedDate} 
              onChange={(e) => setSelectedDate(e.target.value)}
              className="text-sm font-medium bg-transparent text-content-main focus:outline-none focus:text-brand-400 transition-colors cursor-pointer"
            />
          </div>
        </div>
      </div>

      {/* Main Income & Quest Section */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        
        {/* Net Income Hero Card */}
        <div className="lg:col-span-2 relative overflow-hidden rounded-3xl p-6 md:p-8 shadow-2xl bg-gradient-to-br from-emerald-600 to-teal-800 text-white border border-white/10">
          <div className="absolute top-0 right-0 w-64 h-64 bg-white/10 rounded-full blur-3xl opacity-50"></div>
          
          <div className="relative z-10 flex flex-col h-full justify-between gap-6">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2 bg-black/20 px-3 py-1.5 rounded-full backdrop-blur-md border border-emerald-500/30">
                <RiMoneyDollarCircleLine size={16} className="text-emerald-300" />
                <span className="text-xs font-medium text-white">Thu nhập thực tế trong ngày</span>
              </div>
              <button onClick={() => navigate('/driver/wallet')} className="text-xs font-semibold bg-white/20 hover:bg-white/30 transition-colors px-4 py-2 rounded-xl backdrop-blur-sm shadow-lg border border-white/10 flex items-center gap-1">
                Xem Ví <RiArrowRightLine size={14} />
              </button>
            </div>
            
            <div>
              <p className="text-emerald-100 text-sm mb-1">Sau khi trừ {(platformFeeRate * 100).toFixed(0)}% phí nền tảng</p>
              <h2 className="font-display text-4xl md:text-5xl font-bold tracking-tight text-white drop-shadow-md">
                {formatCurrency(finalIncome)}
              </h2>
            </div>
            
            <div className="grid grid-cols-2 gap-4 mt-2">
              <div className="bg-black/20 rounded-2xl p-4 backdrop-blur-sm border border-white/5">
                <p className="text-xs text-emerald-200 mb-1 flex items-center gap-1">
                  Tổng cước xe (Gross)
                </p>
                <p className="font-bold text-lg">{formatCurrency(grossRevenue)}</p>
              </div>
              <div className="bg-red-500/10 rounded-2xl p-4 backdrop-blur-sm border border-red-500/20">
                <p className="text-xs text-red-200 mb-1 flex items-center gap-1">
                  Chiết khấu nền tảng
                </p>
                <p className="font-bold text-lg text-red-100">-{formatCurrency(platformFee)}</p>
              </div>
            </div>
          </div>
        </div>

        {/* Quest & Today Summary */}
        <div className="lg:col-span-1 flex flex-col gap-4">
          {/* Selected Date's Net Insight */}
          <div className="card p-5 border-l-4 border-l-brand-500 bg-gradient-to-r from-brand-500/5 to-transparent">
            <p className="text-xs font-semibold text-content-muted uppercase tracking-wider mb-1 flex items-center gap-2">
              <RiCarLine className="text-brand-400" /> Số chuyến hoàn thành
            </p>
            <p className="font-display text-2xl font-bold text-content-main mb-2">{selectedDayTripsCount} <span className="text-sm font-medium text-content-muted">chuyến</span></p>
            <p className="text-xs font-medium text-brand-400 flex items-center gap-1 bg-brand-500/10 w-fit px-2 py-0.5 rounded-full">
              Ngày {formatDate(selectedDate).split(' ')[0]}
            </p>
          </div>
          
          {/* Daily Quest Progress */}
          <div className={cn(
            "card p-5 border-l-4 flex-1 flex flex-col justify-center",
            isQuestCompleted 
              ? "border-l-yellow-400 bg-gradient-to-r from-yellow-500/10 to-transparent" 
              : "border-l-purple-500 bg-gradient-to-r from-purple-500/5 to-transparent"
          )}>
            <div className="flex justify-between items-start mb-2">
              <p className="text-xs font-semibold text-content-muted uppercase tracking-wider flex items-center gap-2">
                <RiTrophyLine className={isQuestCompleted ? "text-yellow-400" : "text-purple-400"} /> Thưởng ngày
              </p>
              {isQuestCompleted && (
                <span className="text-[10px] font-bold bg-yellow-500 text-black px-2 py-0.5 rounded-full shadow-glow-yellow animate-pulse">
                  ĐÃ ĐẠT
                </span>
              )}
            </div>
            
            <p className="font-display text-2xl font-bold text-content-main mb-1">{formatCurrency(QUEST_REWARD)}</p>
            <p className="text-xs text-content-muted mb-3">Chạy đủ {QUEST_GOAL} chuyến nhận ngay thưởng</p>
            
            <div className="space-y-1.5 mt-auto">
              <div className="flex justify-between text-xs font-medium">
                <span className={isQuestCompleted ? "text-yellow-400" : "text-purple-400"}>
                  {selectedDayTripsCount}/{QUEST_GOAL} chuyến
                </span>
                <span className="text-content-muted">{questProgress.toFixed(0)}%</span>
              </div>
              <div className="h-2 bg-surface-border rounded-full overflow-hidden">
                <div 
                  className={cn("h-full rounded-full transition-all duration-1000", isQuestCompleted ? "bg-yellow-400" : "bg-purple-500")}
                  style={{ width: `${questProgress}%` }}
                />
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Split: Cash vs Online Wallet */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div className="card p-6 border border-green-500/20 bg-gradient-to-br from-green-500/5 to-transparent">
          <div className="flex items-center gap-3 mb-4">
            <div className="w-10 h-10 rounded-full bg-green-500/20 flex items-center justify-center text-green-400">
              <RiMoneyDollarCircleLine size={20} />
            </div>
            <div>
              <p className="font-semibold text-content-main">Tiền mặt đã thu</p>
              <p className="text-xs text-content-muted">Tiền khách trả trực tiếp cho bạn</p>
            </div>
          </div>
          <h3 className="font-display text-2xl font-bold text-green-400">{formatCurrency(cashCollected)}</h3>
          <p className="text-xs text-content-muted mt-2 border-t border-surface-border pt-2">
            *Lưu ý: Hệ thống sẽ tự động trừ {(platformFeeRate * 100).toFixed(0)}% phí nền tảng ({formatCurrency(cashCollected * platformFeeRate)}) của các cuốc tiền mặt này từ Ví điện tử của bạn.
          </p>
        </div>
        
        <div className="card p-6 border border-blue-500/20 bg-gradient-to-br from-blue-500/5 to-transparent">
          <div className="flex items-center gap-3 mb-4">
            <div className="w-10 h-10 rounded-full bg-blue-500/20 flex items-center justify-center text-blue-400">
              <RiBankCardLine size={20} />
            </div>
            <div>
              <p className="font-semibold text-content-main">Doanh thu qua Ví</p>
              <p className="text-xs text-content-muted">Khách thanh toán trực tuyến (VNPAY, Thẻ)</p>
            </div>
          </div>
          <h3 className="font-display text-2xl font-bold text-blue-400">{formatCurrency(onlineIncome)}</h3>
          <p className="text-xs text-content-muted mt-2 border-t border-surface-border pt-2">
            Số tiền này đã bao gồm trong tổng doanh thu. Tiền sẽ được cộng vào Ví điện tử của bạn.
          </p>
        </div>
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

      {/* Recent Trips (Shortened) */}
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <h2 className="font-display text-lg font-bold text-content-main">Chuyến đi gần đây</h2>
          <button 
            onClick={() => navigate('/driver/history')}
            className="text-sm font-medium text-brand-400 hover:text-brand-300 transition-colors flex items-center gap-1"
          >
            Xem tất cả <RiArrowRightLine size={16} />
          </button>
        </div>
        
        {history.length === 0 ? (
          <div className="card p-8 text-center text-content-muted">Chưa có chuyến đi nào</div>
        ) : (
          <div className="relative border-l-2 border-surface-border ml-4 space-y-4 pb-2">
            {history.slice(0, 3).map((trip) => (
              <div key={trip.bookingId || trip.id} className="relative pl-6">
                {/* Timeline dot */}
                <div className={cn(
                  'absolute -left-[9px] top-4 w-4 h-4 rounded-full border-2 border-surface-card',
                  (trip.status === BOOKING_STATUS.COMPLETED || trip.bookingStatus === BOOKING_STATUS.COMPLETED) ? 'bg-brand-500' : 'bg-red-500',
                )} />
                
                <div className="card p-4 hover:border-brand-500/30 transition-colors flex items-center justify-between">
                  <div className="flex-1 pr-4">
                    <p className="text-sm font-medium text-content-main line-clamp-2 leading-relaxed mb-1.5">
                      {trip.dropoffLocation}
                    </p>
                    <div className="flex items-center gap-3 text-xs text-content-muted">
                      <span className="flex items-center gap-1 bg-surface-dark px-2 py-0.5 rounded-md">
                        🕒 {formatDate(trip.bookingTime)}
                      </span>
                      <span className={cn(
                        "font-medium",
                        (trip.status === BOOKING_STATUS.COMPLETED || trip.bookingStatus === BOOKING_STATUS.COMPLETED) ? "text-brand-400" : "text-red-400"
                      )}>
                        {(trip.status === BOOKING_STATUS.COMPLETED || trip.bookingStatus === BOOKING_STATUS.COMPLETED) ? 'Hoàn thành' : 'Đã hủy'}
                      </span>
                    </div>
                  </div>
                  <div className="text-right shrink-0 border-l border-surface-border pl-4">
                    <p className="font-bold text-lg text-content-main">{formatCurrency(trip.totalPrice)}</p>
                    <div className="flex items-center justify-end gap-1 mt-1">
                      {trip.paymentMethod === 'CASH' ? (
                        <span className="text-[10px] uppercase font-bold tracking-wider text-green-400 bg-green-500/10 px-2 py-0.5 rounded-sm">Tiền mặt</span>
                      ) : (
                        <span className="text-[10px] uppercase font-bold tracking-wider text-blue-400 bg-blue-500/10 px-2 py-0.5 rounded-sm">Online</span>
                      )}
                    </div>
                  </div>
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

