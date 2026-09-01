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
import { motion } from 'motion/react'

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
      bookingApi.getDriverHistoryPage(driverId, 'ALL', 0, 5), // Lấy 5 chuyến gần nhất
      driverApi.getDailyRevenue(selectedDate)
    ])
      .then(([rev, hist, daily]) => {
        setRevenue(rev)
        setHistory(hist?.content || [])
        setDailyStats(daily)
      })
      .catch(() => {})
      .finally(() => setLoading(false))
  }, [user, userProfile, selectedDate, period])

  if (loading) return (
    <div className="flex h-full items-center justify-center bg-[#e8ece3] dark:bg-surface-dark">
      <Spinner size="xl" />
    </div>
  )

  const chartData = revenue?.details?.map(d => ({ label: d.timeLabel, value: d.revenue })) || []

  // === CALCULATIONS ===
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
    <div className="h-full overflow-y-auto bg-[#e8ece3] p-5 pb-10 dark:bg-surface-dark lg:p-8 pointer-events-auto">
      <motion.div 
        initial={{ opacity: 0, x: -18 }}
        animate={{ opacity: 1, x: 0 }}
        transition={{ duration: 0.45, ease: [0.16, 1, 0.3, 1] }}
        className="mx-auto max-w-5xl space-y-6"
      >
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-2">
          <div>
            <h1 className="font-display text-3xl font-bold text-gray-900 dark:text-white tracking-tight">Doanh thu & Thu nhập</h1>
            <p className="text-gray-500 dark:text-gray-400 text-sm mt-1">Theo dõi thu nhập thực tế, chiết khấu và tiền thưởng</p>
          </div>
          
          {/* Global Date Picker */}
          <div className="flex items-center gap-3 bg-white dark:bg-surface-card border border-gray-100 dark:border-surface-border px-4 py-2.5 rounded-xl shadow-sm">
            <div className="w-8 h-8 rounded-full bg-brand-50 dark:bg-brand-500/10 flex items-center justify-center">
              <RiCalendarLine className="text-brand-500" size={18} />
            </div>
            <div className="flex flex-col">
              <span className="text-[10px] text-gray-500 font-semibold uppercase tracking-wider">Chọn ngày</span>
              <input 
                type="date" 
                value={selectedDate} 
                onChange={(e) => setSelectedDate(e.target.value)}
                className="text-sm font-bold bg-transparent text-gray-900 dark:text-white focus:outline-none focus:text-brand-500 transition-colors cursor-pointer"
              />
            </div>
          </div>
        </div>

        {/* Main Income & Quest Section */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          
          {/* Net Income Hero Card */}
          <div className="lg:col-span-2 relative overflow-hidden rounded-3xl p-6 md:p-8 shadow-xl bg-gradient-to-br from-brand-600 to-brand-800 text-white border border-white/10">
            <div className="absolute top-0 right-0 w-64 h-64 bg-white/10 rounded-full blur-3xl opacity-50 translate-x-1/4 -translate-y-1/4"></div>
            <div className="absolute bottom-0 left-0 w-64 h-64 bg-black/20 rounded-full blur-3xl opacity-50 -translate-x-1/4 translate-y-1/4"></div>
            
            <div className="relative z-10 flex flex-col h-full justify-between gap-6">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2 bg-black/20 px-4 py-2 rounded-full backdrop-blur-md border border-white/10">
                  <RiMoneyDollarCircleLine size={18} className="text-brand-300" />
                  <span className="text-xs font-bold uppercase tracking-wider text-white">Thu nhập thực tế trong ngày</span>
                </div>
                <button onClick={() => navigate('/driver/wallet')} className="text-xs font-bold bg-white text-brand-900 hover:bg-brand-50 transition-colors px-4 py-2 rounded-xl shadow-lg flex items-center gap-1.5">
                  Ví của tôi <RiArrowRightLine size={14} />
                </button>
              </div>
              
              <div>
                <p className="text-brand-100 text-sm font-medium mb-1">Sau khi trừ {(platformFeeRate * 100).toFixed(0)}% phí nền tảng</p>
                <h2 className="font-display text-4xl md:text-5xl font-bold tracking-tight text-white drop-shadow-sm">
                  {formatCurrency(finalIncome)}
                </h2>
              </div>
              
              <div className="grid grid-cols-2 gap-4 mt-2">
                <div className="bg-black/20 rounded-2xl p-4 backdrop-blur-md border border-white/10">
                  <p className="text-[11px] uppercase tracking-wider font-bold text-brand-200 mb-1 flex items-center gap-1">
                    Tổng cước xe (Gross)
                  </p>
                  <p className="font-bold text-xl">{formatCurrency(grossRevenue)}</p>
                </div>
                <div className="bg-red-500/20 rounded-2xl p-4 backdrop-blur-md border border-red-500/30">
                  <p className="text-[11px] uppercase tracking-wider font-bold text-red-200 mb-1 flex items-center gap-1">
                    Chiết khấu nền tảng
                  </p>
                  <p className="font-bold text-xl text-red-100">-{formatCurrency(platformFee)}</p>
                </div>
              </div>
            </div>
          </div>

          {/* Quest & Today Summary */}
          <div className="lg:col-span-1 flex flex-col gap-4">
            {/* Selected Date's Net Insight */}
            <div className="bg-white dark:bg-surface-card rounded-2xl p-5 border border-gray-100 dark:border-surface-border shadow-sm border-l-4 border-l-brand-500">
              <p className="text-xs font-bold text-gray-500 dark:text-content-muted uppercase tracking-wider mb-2 flex items-center gap-2">
                <RiCarLine className="text-brand-500" size={16} /> Số chuyến hoàn thành
              </p>
              <div className="flex items-baseline gap-2 mb-2">
                <span className="font-display text-3xl font-bold text-gray-900 dark:text-white">{selectedDayTripsCount}</span>
                <span className="text-sm font-medium text-gray-500">chuyến</span>
              </div>
              <p className="text-[11px] font-bold uppercase tracking-wider text-brand-600 dark:text-brand-400 bg-brand-50 dark:bg-brand-500/10 w-fit px-2.5 py-1 rounded-md">
                Ngày {formatDate(selectedDate).split(' ')[0]}
              </p>
            </div>
            
            {/* Daily Quest Progress */}
            <div className={cn(
              "bg-white dark:bg-surface-card rounded-2xl p-5 border shadow-sm border-l-4 flex-1 flex flex-col justify-center",
              isQuestCompleted 
                ? "border-l-yellow-400 border-gray-100 dark:border-surface-border" 
                : "border-l-purple-500 border-gray-100 dark:border-surface-border"
            )}>
              <div className="flex justify-between items-start mb-3">
                <p className="text-xs font-bold text-gray-500 dark:text-content-muted uppercase tracking-wider flex items-center gap-2">
                  <RiTrophyLine size={16} className={isQuestCompleted ? "text-yellow-500" : "text-purple-500"} /> Thưởng ngày
                </p>
                {isQuestCompleted && (
                  <span className="text-[10px] font-bold bg-yellow-400 text-yellow-900 px-2 py-0.5 rounded-md shadow-sm">
                    ĐÃ ĐẠT
                  </span>
                )}
              </div>
              
              <p className="font-display text-2xl font-bold text-gray-900 dark:text-white mb-1">{formatCurrency(QUEST_REWARD)}</p>
              <p className="text-xs text-gray-500 font-medium mb-4">Chạy đủ {QUEST_GOAL} chuyến nhận ngay thưởng</p>
              
              <div className="space-y-2 mt-auto">
                <div className="flex justify-between text-xs font-bold">
                  <span className={isQuestCompleted ? "text-yellow-600 dark:text-yellow-400" : "text-purple-600 dark:text-purple-400"}>
                    {selectedDayTripsCount}/{QUEST_GOAL} chuyến
                  </span>
                  <span className="text-gray-500">{questProgress.toFixed(0)}%</span>
                </div>
                <div className="h-2.5 bg-gray-100 dark:bg-surface-border rounded-full overflow-hidden">
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
          <div className="bg-white dark:bg-surface-card rounded-2xl p-6 border border-emerald-100 dark:border-emerald-500/20 shadow-sm relative overflow-hidden">
            <div className="absolute top-0 right-0 w-32 h-32 bg-emerald-500/5 rounded-full blur-2xl"></div>
            <div className="flex items-center gap-4 mb-5 relative z-10">
              <div className="w-12 h-12 rounded-2xl bg-emerald-50 dark:bg-emerald-500/10 flex items-center justify-center text-emerald-500">
                <RiMoneyDollarCircleLine size={24} />
              </div>
              <div>
                <p className="font-bold text-gray-900 dark:text-white">Tiền mặt đã thu</p>
                <p className="text-xs text-gray-500 mt-0.5">Tiền khách trả trực tiếp cho bạn</p>
              </div>
            </div>
            <h3 className="font-display text-3xl font-bold text-emerald-600 dark:text-emerald-400 relative z-10">{formatCurrency(cashCollected)}</h3>
            <p className="text-xs text-gray-500 font-medium mt-4 border-t border-gray-100 dark:border-surface-border pt-3 relative z-10 leading-relaxed">
              *Lưu ý: Hệ thống sẽ tự động trừ <span className="font-bold text-gray-700 dark:text-gray-300">{(platformFeeRate * 100).toFixed(0)}%</span> phí nền tảng (<span className="font-bold text-red-500">{formatCurrency(cashCollected * platformFeeRate)}</span>) của các cuốc tiền mặt này từ Ví của bạn.
            </p>
          </div>
          
          <div className="bg-white dark:bg-surface-card rounded-2xl p-6 border border-blue-100 dark:border-blue-500/20 shadow-sm relative overflow-hidden">
            <div className="absolute top-0 right-0 w-32 h-32 bg-blue-500/5 rounded-full blur-2xl"></div>
            <div className="flex items-center gap-4 mb-5 relative z-10">
              <div className="w-12 h-12 rounded-2xl bg-blue-50 dark:bg-blue-500/10 flex items-center justify-center text-blue-500">
                <RiBankCardLine size={24} />
              </div>
              <div>
                <p className="font-bold text-gray-900 dark:text-white">Doanh thu qua Ví</p>
                <p className="text-xs text-gray-500 mt-0.5">Khách thanh toán trực tuyến (VNPAY, Thẻ)</p>
              </div>
            </div>
            <h3 className="font-display text-3xl font-bold text-blue-600 dark:text-blue-400 relative z-10">{formatCurrency(onlineIncome)}</h3>
            <p className="text-xs text-gray-500 font-medium mt-4 border-t border-gray-100 dark:border-surface-border pt-3 relative z-10 leading-relaxed">
              Số tiền này đã bao gồm trong tổng doanh thu. Tiền đã được <span className="font-bold text-brand-500">tự động cộng</span> vào Ví điện tử của bạn.
            </p>
          </div>
        </div>

        {/* Chart */}
        <div className="bg-white dark:bg-surface-card rounded-2xl p-6 border border-gray-100 dark:border-surface-border shadow-sm space-y-6">
          <div className="flex items-center justify-between flex-wrap gap-4">
            <h2 className="font-display text-xl font-bold text-gray-900 dark:text-white">Biểu đồ thu nhập</h2>
            <div className="flex gap-1 p-1 bg-gray-50 dark:bg-surface-dark border border-gray-200 dark:border-surface-border rounded-xl">
              {PERIOD_TABS.map((t) => (
                <button
                  key={t.value}
                  onClick={() => setPeriod(t.value)}
                  className={cn(
                    'px-4 py-2 rounded-lg text-xs font-bold transition-all',
                    period === t.value 
                      ? 'bg-white dark:bg-surface-card text-gray-900 dark:text-white shadow-sm' 
                      : 'text-gray-500 hover:text-gray-900 dark:hover:text-white',
                  )}
                >
                  {t.label}
                </button>
              ))}
            </div>
          </div>
          <div className="h-[240px] w-full">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={chartData} margin={{ top: 10, right: 0, left: -15, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" vertical={false} />
                <XAxis dataKey="label" tick={{ fill: '#6b7280', fontSize: 12, fontWeight: 500 }} axisLine={false} tickLine={false} dy={10} />
                <YAxis tick={{ fill: '#6b7280', fontSize: 12, fontWeight: 500 }} axisLine={false} tickLine={false} dx={-10}
                  tickFormatter={(v) => v >= 1000 ? `${v/1000}k` : v}
                />
                <Tooltip
                  cursor={{ fill: 'rgba(34,197,94,0.05)' }}
                  content={({ active, payload, label }) => {
                    if (active && payload && payload.length) {
                      return (
                        <div className="bg-gray-900 text-white p-3 rounded-xl shadow-xl border border-gray-800">
                          <p className="text-xs text-gray-400 mb-1">{label}</p>
                          <p className="font-bold text-brand-400">{formatCurrency(payload[0].value)}</p>
                        </div>
                      );
                    }
                    return null;
                  }}
                />
                <Bar dataKey="value" fill="#22c55e" radius={[6, 6, 0, 0]} maxBarSize={48} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Recent Trips */}
        <div className="bg-white dark:bg-surface-card rounded-2xl p-6 border border-gray-100 dark:border-surface-border shadow-sm space-y-5">
          <div className="flex items-center justify-between">
            <h2 className="font-display text-xl font-bold text-gray-900 dark:text-white">Chuyến đi gần đây</h2>
            <button 
              onClick={() => navigate('/driver/history')}
              className="text-sm font-bold text-brand-500 hover:text-brand-600 transition-colors flex items-center gap-1 bg-brand-50 dark:bg-brand-500/10 px-3 py-1.5 rounded-lg"
            >
              Xem tất cả <RiArrowRightLine size={16} />
            </button>
          </div>
          
          {history.length === 0 ? (
            <div className="py-12 text-center">
              <div className="w-16 h-16 rounded-full bg-gray-50 dark:bg-surface-dark flex items-center justify-center mx-auto mb-3">
                <RiCarLine size={24} className="text-gray-400" />
              </div>
              <p className="text-gray-500 font-medium">Chưa có chuyến đi nào</p>
            </div>
          ) : (
            <div className="relative border-l-2 border-gray-100 dark:border-surface-border ml-4 space-y-4 pb-2">
              {history.map((trip) => (
                <div key={trip.bookingId || trip.id} className="relative pl-6">
                  {/* Timeline dot */}
                  <div className={cn(
                    'absolute -left-[9px] top-5 w-4 h-4 rounded-full border-[3px] border-white dark:border-surface-card',
                    (trip.status === BOOKING_STATUS.COMPLETED || trip.bookingStatus === BOOKING_STATUS.COMPLETED) ? 'bg-brand-500' : 'bg-red-500',
                  )} />
                  
                  <div className="bg-gray-50 dark:bg-surface-dark p-4 rounded-xl hover:bg-gray-100 dark:hover:bg-surface-border/50 transition-colors flex flex-col sm:flex-row sm:items-center justify-between gap-4 border border-transparent hover:border-gray-200 dark:hover:border-surface-border">
                    <div className="flex-1">
                      <p className="text-sm font-bold text-gray-900 dark:text-white leading-relaxed mb-2">
                        {trip.dropoffLocation}
                      </p>
                      <div className="flex items-center flex-wrap gap-2 text-xs">
                        <span className="flex items-center gap-1.5 bg-white dark:bg-surface-card px-2.5 py-1 rounded-md text-gray-500 font-medium shadow-sm border border-gray-100 dark:border-surface-border">
                          <RiCalendarLine size={12} /> {formatDate(trip.bookingTime)}
                        </span>
                        <span className={cn(
                          "px-2.5 py-1 rounded-md font-bold text-[10px] uppercase tracking-wider",
                          (trip.status === BOOKING_STATUS.COMPLETED || trip.bookingStatus === BOOKING_STATUS.COMPLETED) ? "bg-emerald-100 text-emerald-700 dark:bg-emerald-500/10 dark:text-emerald-400" : "bg-red-100 text-red-700 dark:bg-red-500/10 dark:text-red-400"
                        )}>
                          {(trip.status === BOOKING_STATUS.COMPLETED || trip.bookingStatus === BOOKING_STATUS.COMPLETED) ? 'Hoàn thành' : 'Đã hủy'}
                        </span>
                      </div>
                    </div>
                    <div className="sm:text-right shrink-0 border-t sm:border-t-0 sm:border-l border-gray-200 dark:border-surface-border pt-3 sm:pt-0 sm:pl-5">
                      <p className="font-display font-bold text-xl text-gray-900 dark:text-white">{formatCurrency(trip.totalPrice)}</p>
                      <div className="flex items-center sm:justify-end gap-1 mt-1">
                        {trip.paymentMethod === 'CASH' ? (
                          <span className="text-[10px] uppercase font-bold tracking-wider text-emerald-700 bg-emerald-100 dark:text-emerald-400 dark:bg-emerald-500/10 px-2.5 py-1 rounded-md">Tiền mặt</span>
                        ) : (
                          <span className="text-[10px] uppercase font-bold tracking-wider text-blue-700 bg-blue-100 dark:text-blue-400 dark:bg-blue-500/10 px-2.5 py-1 rounded-md">Online</span>
                        )}
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </motion.div>
    </div>
  )
}

export default DriverRevenuePage
