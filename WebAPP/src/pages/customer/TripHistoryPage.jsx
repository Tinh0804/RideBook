import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/store/rootStore';
import { bookingApi } from '@/features/booking/api/bookingApi';
import { customerApi } from '@/features/customer/api/customerApi';
import { formatCurrency } from '@/utils/currency';
import { formatDate } from '@/utils/formatDate';
import { BOOKING_STATUS, BOOKING_STATUS_LABEL } from '@/config';
import Button from '@/components/Elements/Button';
import Spinner from '@/components/Elements/Spinner';
import { cn } from '@/utils/cn';
import { motion } from 'motion/react';
import { RiMapPinLine, RiMapPin2Line, RiTimeLine, RiCarLine, RiStarLine, RiCalendarEventLine } from 'react-icons/ri';

const statusBadge = {
  [BOOKING_STATUS.QUEUED]:    'bg-purple-500/10 text-purple-700 dark:text-purple-400 border-purple-500/20',
  [BOOKING_STATUS.COMPLETED]: 'bg-emerald-500/10 text-emerald-700 dark:text-emerald-400 border-emerald-500/20',
  [BOOKING_STATUS.CANCELLED]: 'bg-red-500/10 text-red-700 dark:text-red-400 border-red-500/20',
  [BOOKING_STATUS.PENDING]:   'bg-amber-500/10 text-amber-700 dark:text-amber-400 border-amber-500/20',
  default:                    'bg-slate-500/10 text-slate-700 dark:text-slate-400 border-slate-500/20',
};

const FILTERS = [
  { value: 'ALL', label: 'Tất cả' },
  { value: BOOKING_STATUS.QUEUED, label: 'Đã lên lịch' },
  { value: BOOKING_STATUS.COMPLETED, label: 'Hoàn thành' },
  { value: BOOKING_STATUS.CANCELLED, label: 'Đã hủy' },
];

const TripHistoryPage = () => {
  const navigate = useNavigate();
  const { userProfile, setUserProfile } = useAuthStore();
  const [trips, setTrips] = useState([]);
  const [filter, setFilter] = useState('ALL');
  const [fromDate, setFromDate] = useState('');
  const [toDate, setToDate] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const customerId = userProfile?.id || userProfile?.customerId;
    if (!customerId) {
      customerApi.getMyInfo()
        .then((profile) => {
          setUserProfile(profile);
          const id = profile?.id || profile?.customerId;
          if (!id) { 
            setLoading(false); return; 
          }
          return bookingApi.getCustomerHistory(id);
        })
        .then((res) => {
          setTrips(res || []);
        })
        .catch((e) => console.error('[TripHistory] Error:', e))
        .finally(() => setLoading(false));
      return;
    }
    bookingApi
      .getCustomerHistory(customerId)
      .then((res) => {
        setTrips(res || []);
      })
      .catch((e) => console.error('[TripHistory] Error:', e))
      .finally(() => setLoading(false));
  }, [userProfile?.id, userProfile?.customerId, setUserProfile]);

  const filteredTrips = trips.filter((t) => {
    if (filter !== 'ALL' && t.bookingStatus !== filter) return false;
    
    if (fromDate || toDate) {
      const tripDate = new Date(t.scheduledAt || t.bookingTime);
      tripDate.setHours(0, 0, 0, 0);
      
      if (fromDate) {
        const from = new Date(fromDate);
        from.setHours(0, 0, 0, 0);
        if (tripDate < from) return false;
      }
      
      if (toDate) {
        const to = new Date(toDate);
        to.setHours(0, 0, 0, 0);
        if (tripDate > to) return false;
      }
    }
    
    return true;
  });

  if (loading) {
    return (
      <div className="flex h-full items-center justify-center bg-[#e8ece3] dark:bg-surface-dark">
        <Spinner size="lg" />
      </div>
    );
  }

  return (
    <div className="h-full overflow-y-auto bg-[#e8ece3] dark:bg-surface-dark pointer-events-auto">
      <motion.div
        initial={{ opacity: 0, x: -18 }}
        animate={{ opacity: 1, x: 0 }}
        transition={{ duration: 0.45, ease: [0.16, 1, 0.3, 1] }}
        className="mx-auto w-full max-w-3xl space-y-6 p-fluid pb-10 lg:p-8"
      >
        {/* Header Section (Matching user's sleek editorial style) */}
        <section className="relative min-h-40 overflow-hidden rounded-2xl bg-slate-950 p-fluid text-white shadow-sm">
          <div className="relative z-10 max-w-[80%]">
            <p className="mb-3 text-sm font-semibold text-lime-accent uppercase tracking-wider">Hành trình của bạn</p>
            <h1 className="font-display text-fluid-4xl font-bold leading-[1.05] tracking-[-0.04em]">
              Lịch sử<br />Chuyến đi
            </h1>
            <p className="mt-4 text-sm leading-relaxed text-white/55">
              Bạn đã thực hiện {trips.length} chuyến đi cùng BookCar.
            </p>
          </div>
          <span className="absolute -bottom-6 right-2 font-display text-[9rem] font-bold tracking-[-0.08em] text-white/[.04] select-none">
            {trips.length.toString().padStart(2, '0')}
          </span>
        </section>

        {/* Filter Tabs */}
        <div className="flex flex-col sm:flex-row gap-4 items-start sm:items-center justify-between">
          <div className="flex gap-2 overflow-x-auto no-scrollbar pb-2">
            {FILTERS.map((f) => (
              <button
                key={f.value}
                onClick={() => setFilter(f.value)}
                className={cn(
                  'whitespace-nowrap rounded-xl px-5 py-2.5 text-sm font-bold transition-colors border',
                  filter === f.value
                    ? 'bg-slate-950 text-white border-slate-950 dark:bg-white dark:text-slate-950 dark:border-white'
                    : 'bg-surface-card text-content-muted border-surface-border hover:border-slate-400'
                )}
              >
                {f.label}
              </button>
            ))}
          </div>
          
          {/* Date Range Filter */}
          <div className="flex items-center gap-2">
            <div className="relative">
              <input
                type="date"
                value={fromDate}
                onChange={(e) => setFromDate(e.target.value)}
                className="w-full rounded-xl border border-surface-border bg-surface-card px-3 py-2 text-sm text-content-main focus:border-slate-400 focus:outline-none dark:focus:border-slate-600"
                placeholder="Từ ngày"
              />
            </div>
            <span className="text-content-muted">-</span>
            <div className="relative">
              <input
                type="date"
                value={toDate}
                onChange={(e) => setToDate(e.target.value)}
                className="w-full rounded-xl border border-surface-border bg-surface-card px-3 py-2 text-sm text-content-main focus:border-slate-400 focus:outline-none dark:focus:border-slate-600"
                placeholder="Đến ngày"
              />
            </div>
          </div>
        </div>

        {/* Trip List */}
        {filteredTrips.length === 0 ? (
          <div className="rounded-2xl border border-surface-border bg-surface-card py-20 text-center shadow-sm">
            <div className="mx-auto mb-4 grid h-16 w-16 place-items-center rounded-full bg-surface-muted text-content-muted">
              <RiCarLine size={32} />
            </div>
            <p className="font-display text-fluid-xl font-bold text-content-main">Chưa có chuyến đi nào</p>
            <p className="mt-2 text-sm text-content-muted">Khám phá thành phố cùng BookCar ngay hôm nay.</p>
            <Button
              onClick={() => navigate('/customer/booking')}
              className="mt-6 rounded-xl bg-lime-accent font-bold text-slate-950 hover:bg-[#b8ff59]"
            >
              Đặt chuyến ngay
            </Button>
          </div>
        ) : (
          <div className="space-y-4">
            {filteredTrips.map((trip, idx) => (
              <motion.div
                initial={{ opacity: 0, y: 15 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.4, delay: idx * 0.05, ease: [0.16, 1, 0.3, 1] }}
                key={trip.bookingId}
                className="group relative overflow-hidden rounded-2xl border border-surface-border bg-surface-card p-fluid shadow-sm transition hover:border-slate-300 dark:hover:border-slate-600"
              >
                {/* Status & Date */}
                <div className="mb-5 flex items-center justify-between border-b border-surface-border pb-4">
                  <div className="flex items-center gap-3">
                    <span className={cn(
                      'rounded-md border px-2.5 py-1 text-[11px] font-bold uppercase tracking-wider',
                      statusBadge[trip.bookingStatus] || statusBadge.default
                    )}>
                      {BOOKING_STATUS_LABEL[trip.bookingStatus]}
                    </span>
                    {trip.scheduledAt ? (
                      <span className="flex items-center gap-1.5 text-xs font-semibold text-purple-600 dark:text-purple-400 bg-purple-500/10 px-2 py-0.5 rounded">
                        <RiCalendarEventLine size={13} />
                        Đón lúc: {new Date(trip.scheduledAt).toLocaleString('vi-VN', { hour: '2-digit', minute: '2-digit', day: '2-digit', month: '2-digit' })}
                      </span>
                    ) : (
                      <span className="flex items-center gap-1.5 text-xs font-semibold text-content-muted">
                        <RiTimeLine size={14} />
                        {formatDate(trip.bookingTime)}
                      </span>
                    )}
                  </div>
                  <span className="font-mono text-xs font-medium text-content-muted">
                    #{trip.bookingId.slice(-6).toUpperCase()}
                  </span>
                </div>

                {/* Locations */}
                <div className="relative space-y-4">
                  <span className="absolute bottom-4 left-[11px] top-5 w-px bg-surface-border" />
                  
                  <div className="relative flex gap-4">
                    <span className="relative z-10 grid h-6 w-6 shrink-0 place-items-center rounded-full bg-slate-100 dark:bg-slate-800 text-slate-500">
                      <RiMapPinLine size={14} />
                    </span>
                    <div className="min-w-0 flex-1 pt-0.5">
                      <p className="truncate text-sm font-bold text-content-main">{trip.pickupLocation}</p>
                    </div>
                  </div>

                  <div className="relative flex gap-4">
                    <span className="relative z-10 grid h-6 w-6 shrink-0 place-items-center rounded-full bg-slate-950 text-white dark:bg-white dark:text-slate-950">
                      <RiMapPin2Line size={14} />
                    </span>
                    <div className="min-w-0 flex-1 pt-0.5">
                      <p className="truncate text-sm font-bold text-content-main">{trip.dropoffLocation}</p>
                    </div>
                  </div>
                </div>

                {/* Footer Details */}
                <div className="mt-6 flex flex-wrap items-end justify-between gap-4 rounded-xl bg-surface-muted/50 p-4">
                  <div className="flex gap-6">
                    <div>
                      <p className="text-[11px] font-semibold uppercase tracking-wider text-content-muted">Tài xế</p>
                      <p className="mt-1 text-sm font-bold text-content-main">{trip.driverName || 'Chưa có'}</p>
                    </div>
                    <div>
                      <p className="text-[11px] font-semibold uppercase tracking-wider text-content-muted">Xe</p>
                      <p className="mt-1 text-sm font-bold text-content-main">{trip.vehicleTypeName || '---'}</p>
                    </div>
                  </div>
                  
                  <div className="text-right">
                    <p className="text-[11px] font-semibold uppercase tracking-wider text-content-muted mb-0.5">Tổng cộng</p>
                    <p className="font-display text-fluid-xl font-bold text-slate-950 dark:text-white">
                      {formatCurrency(trip.totalPrice)}
                    </p>
                  </div>
                </div>

                {/* Hover Actions */}
                <div className="mt-4 flex gap-2">
                  <Button
                    size="sm"
                    className="h-10 flex-1 rounded-xl bg-slate-950 font-bold text-white hover:bg-slate-800 dark:bg-white dark:text-slate-950 dark:hover:bg-slate-200 shadow-none"
                    onClick={() =>
                      navigate('/customer/booking', {
                        state: {
                          pickup: trip.pickupLocation,
                          dropoff: trip.dropoffLocation,
                        },
                      })
                    }
                  >
                    Đặt lại hành trình
                  </Button>
                  {trip.bookingStatus === BOOKING_STATUS.COMPLETED && trip.rating ? (
                    <div className="flex h-10 items-center justify-between rounded-xl border border-surface-border bg-surface-base px-3 flex-1 ml-2">
                       <div className="flex items-center gap-1 text-yellow-500 font-medium text-sm">
                         <RiStarLine size={16} />
                         <span>{trip.rating}</span>
                       </div>
                       {trip.review && (
                         <span className="text-xs text-content-muted truncate max-w-[120px]" title={trip.review}>
                           "{trip.review}"
                         </span>
                       )}
                    </div>
                  ) : trip.bookingStatus === BOOKING_STATUS.COMPLETED && (
                    <Button 
                      size="sm" 
                      variant="outline" 
                      className="h-10 w-10 shrink-0 rounded-xl p-0 border-surface-border text-content-muted hover:text-content-main ml-2"
                      title="Đánh giá chuyến đi"
                      onClick={() => navigate('/customer/rating', { state: { booking: trip } })}
                    >
                      <RiStarLine size={18} />
                    </Button>
                  )}
                </div>
              </motion.div>
            ))}
          </div>
        )}
      </motion.div>
    </div>
  );
};

export default TripHistoryPage;