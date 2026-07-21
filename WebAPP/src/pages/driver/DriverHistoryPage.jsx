import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/store/rootStore';
import { bookingApi } from '@/features/booking/api/bookingApi';
import { formatCurrency } from '@/utils/currency';
import { formatDate } from '@/utils/formatDate';
import { BOOKING_STATUS, BOOKING_STATUS_LABEL } from '@/config';
import Spinner from '@/components/Elements/Spinner';
import Button from '@/components/Elements/Button';
import { cn } from '@/utils/cn';
import { motion } from 'motion/react';
import { RiMapPinLine, RiMapPin2Line, RiTimeLine, RiCarLine, RiStarLine } from 'react-icons/ri';

const statusBadge = {
  [BOOKING_STATUS.COMPLETED]: 'bg-emerald-500/10 text-emerald-700 dark:text-emerald-400 border-emerald-500/20',
  [BOOKING_STATUS.CANCELLED]: 'bg-red-500/10 text-red-700 dark:text-red-400 border-red-500/20',
  [BOOKING_STATUS.PENDING]:   'bg-amber-500/10 text-amber-700 dark:text-amber-400 border-amber-500/20',
  default:                    'bg-slate-500/10 text-slate-700 dark:text-slate-400 border-slate-500/20',
};

const FILTERS = [
  { value: 'ALL', label: 'Tất cả' },
  { value: BOOKING_STATUS.COMPLETED, label: 'Hoàn thành' },
  { value: BOOKING_STATUS.CANCELLED, label: 'Đã hủy' },
];

const DriverHistoryPage = () => {
  const navigate = useNavigate();
  const { userProfile, user } = useAuthStore();
  const [trips, setTrips] = useState([]);
  const [filter, setFilter] = useState('ALL');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const driverId = userProfile?.id || user?.id || user?.driverId;
    if (!driverId) return;
    
    setLoading(true);
    bookingApi
      .getDriverHistoryPage(driverId, filter, page, 10)
      .then((res) => {
        setTrips(res?.content || []);
        setTotalPages(res?.page?.totalPages ?? res?.totalPages ?? 0);
        setTotalElements(res?.page?.totalElements ?? res?.totalElements ?? 0);
      })
      .catch(() => {})
      .finally(() => setLoading(false));
  }, [userProfile, user, filter, page]);

  const handleFilterChange = (newFilter) => {
    setFilter(newFilter);
    setPage(0);
  };

  if (loading && trips.length === 0) {
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
        className="mx-auto w-full max-w-4xl space-y-6 p-5 pb-10 lg:p-8"
      >
        {/* Header Section (Matching user's sleek editorial style) */}
        <section className="relative min-h-40 overflow-hidden rounded-2xl bg-brand-600 p-6 sm:p-8 text-white shadow-sm">
          <div className="absolute top-0 right-0 w-64 h-64 bg-brand-500 rounded-full blur-3xl opacity-50 -translate-y-1/2 translate-x-1/3" />
          <div className="absolute bottom-0 left-0 w-64 h-64 bg-brand-700 rounded-full blur-3xl opacity-50 translate-y-1/3 -translate-x-1/3" />
          
          <div className="relative z-10 max-w-[80%]">
            <p className="mb-3 text-sm font-semibold text-brand-100 uppercase tracking-wider">Hoạt động của bạn</p>
            <h1 className="font-display text-4xl font-bold leading-[1.05] tracking-[-0.04em]">
              Lịch sử<br />Chuyến đi
            </h1>
            <p className="mt-4 text-sm leading-relaxed text-brand-50">
              Bạn đã thực hiện {totalElements} chuyến đi cùng hệ thống.
            </p>
          </div>
          <span className="absolute -bottom-6 right-2 font-display text-[9rem] font-bold tracking-[-0.08em] text-white/[.08] select-none">
            {totalElements.toString().padStart(2, '0')}
          </span>
        </section>

        {/* Filter Tabs */}
        <div className="flex gap-2 overflow-x-auto no-scrollbar pb-2">
          {FILTERS.map((f) => (
            <button
              key={f.value}
              onClick={() => handleFilterChange(f.value)}
              className={cn(
                'whitespace-nowrap rounded-xl px-5 py-2.5 text-sm font-bold transition-colors border',
                filter === f.value
                  ? 'bg-brand-500 text-white border-brand-500 shadow-lg shadow-brand-500/20'
                  : 'bg-white dark:bg-surface-card text-gray-500 dark:text-content-muted border-gray-200 dark:border-surface-border hover:border-brand-300'
              )}
            >
              {f.label}
            </button>
          ))}
        </div>

        {/* Trip List */}
        {trips.length === 0 ? (
          <div className="rounded-2xl border border-gray-100 dark:border-surface-border bg-white dark:bg-surface-card py-20 text-center shadow-sm">
            <div className="mx-auto mb-4 grid h-16 w-16 place-items-center rounded-full bg-gray-50 dark:bg-surface-muted text-gray-400 dark:text-content-muted">
              <RiCarLine size={32} />
            </div>
            <p className="font-display text-xl font-bold text-gray-900 dark:text-content-main">Chưa có chuyến đi nào</p>
            <p className="mt-2 text-sm text-gray-500 dark:text-content-muted">Bật trạng thái hoạt động để bắt đầu nhận cuốc xe.</p>
            <Button
              onClick={() => navigate('/driver/dashboard')}
              className="mt-6 rounded-xl bg-brand-500 font-bold text-white hover:bg-brand-600 shadow-lg shadow-brand-500/20"
            >
              Về trang chủ
            </Button>
          </div>
        ) : (
          <div className="space-y-4">
            {trips.map((trip, idx) => (
              <motion.div
                initial={{ opacity: 0, y: 15 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.4, delay: idx * 0.05, ease: [0.16, 1, 0.3, 1] }}
                key={trip.bookingId}
                className="group relative overflow-hidden rounded-2xl border border-gray-100 dark:border-surface-border bg-white dark:bg-surface-card p-5 sm:p-6 shadow-sm transition hover:border-brand-200 dark:hover:border-brand-500/30"
              >
                {/* Status & Date */}
                <div className="mb-5 flex items-center justify-between border-b border-gray-100 dark:border-surface-border pb-4">
                  <div className="flex items-center gap-3">
                    <span className={cn(
                      'rounded-md border px-2.5 py-1 text-[11px] font-bold uppercase tracking-wider',
                      statusBadge[trip.bookingStatus] || statusBadge.default
                    )}>
                      {BOOKING_STATUS_LABEL[trip.bookingStatus]}
                    </span>
                    <span className="flex items-center gap-1.5 text-xs font-semibold text-gray-500 dark:text-content-muted">
                      <RiTimeLine size={14} />
                      {formatDate(trip.bookingTime)}
                    </span>
                    {trip.distance && (
                      <>
                        <span className="text-gray-300 dark:text-surface-border">•</span>
                        <span className="text-xs font-semibold text-gray-500 dark:text-content-muted">{trip.distance.toFixed(1)} km</span>
                      </>
                    )}
                  </div>
                  <span className="font-mono text-xs font-medium text-gray-400 dark:text-content-muted">
                    #{trip.bookingId.slice(-6).toUpperCase()}
                  </span>
                </div>

                {/* Locations */}
                <div className="relative space-y-4">
                  <span className="absolute bottom-4 left-[11px] top-5 w-px bg-gray-100 dark:bg-surface-border" />
                  
                  <div className="relative flex gap-4">
                    <span className="relative z-10 grid h-6 w-6 shrink-0 place-items-center rounded-full bg-brand-50 dark:bg-brand-500/10 text-brand-500">
                      <RiMapPinLine size={14} />
                    </span>
                    <div className="min-w-0 flex-1 pt-0.5">
                      <p className="truncate text-sm font-bold text-gray-900 dark:text-content-main">{trip.pickupLocation}</p>
                    </div>
                  </div>

                  <div className="relative flex gap-4">
                    <span className="relative z-10 grid h-6 w-6 shrink-0 place-items-center rounded-full bg-red-50 dark:bg-red-500/10 text-red-500">
                      <RiMapPin2Line size={14} />
                    </span>
                    <div className="min-w-0 flex-1 pt-0.5">
                      <p className="truncate text-sm font-bold text-gray-900 dark:text-content-main">{trip.dropoffLocation}</p>
                    </div>
                  </div>
                </div>

                {/* Footer Details */}
                <div className="mt-6 flex flex-wrap items-end justify-between gap-4 rounded-xl bg-gray-50 dark:bg-surface-muted/50 p-4">
                  <div className="flex gap-6">
                    <div>
                      <p className="text-[11px] font-semibold uppercase tracking-wider text-gray-500 dark:text-content-muted">Khách hàng</p>
                      <p className="mt-1 text-sm font-bold text-gray-900 dark:text-content-main">{trip.customerName || 'Khách vãng lai'}</p>
                      {trip.customerPhone && <p className="text-xs text-gray-500 dark:text-content-muted mt-0.5">{trip.customerPhone}</p>}
                    </div>
                    <div>
                      <p className="text-[11px] font-semibold uppercase tracking-wider text-gray-500 dark:text-content-muted">Thanh toán</p>
                      <p className="mt-1 text-sm font-bold text-gray-900 dark:text-content-main">{trip.paymentMethod === 'CASH' ? 'Tiền mặt' : 'Online'}</p>
                    </div>
                  </div>
                  
                  <div className="text-right">
                    <p className="text-[11px] font-semibold uppercase tracking-wider text-gray-500 dark:text-content-muted mb-0.5">Thu nhập chuyến</p>
                    <p className="font-display text-xl font-bold text-brand-500">
                      {formatCurrency(trip.totalPrice)}
                    </p>
                  </div>
                </div>
              </motion.div>
            ))}

            {/* Pagination Controls */}
            {totalPages > 1 && (
              <div className="flex flex-wrap items-center justify-center gap-2 mt-8 pt-4">
                <button
                  onClick={() => setPage(p => Math.max(0, p - 1))}
                  disabled={page === 0}
                  className="px-4 py-2 rounded-xl border border-gray-200 dark:border-surface-border bg-white dark:bg-surface-card disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50 dark:hover:bg-surface-muted transition-colors text-sm font-semibold text-gray-700 dark:text-white"
                >
                  Trang trước
                </button>
                <div className="flex flex-wrap items-center gap-1 mx-2">
                  {Array.from({ length: totalPages }).map((_, i) => (
                    <button
                      key={i}
                      onClick={() => setPage(i)}
                      className={cn(
                        "w-9 h-9 rounded-xl flex items-center justify-center text-sm font-bold transition-colors",
                        page === i 
                          ? "bg-brand-500 text-white shadow-md shadow-brand-500/20" 
                          : "bg-white dark:bg-surface-card hover:bg-gray-50 dark:hover:bg-surface-muted text-gray-700 dark:text-white border border-transparent hover:border-gray-200 dark:hover:border-surface-border"
                      )}
                    >
                      {i + 1}
                    </button>
                  ))}
                </div>
                <button
                  onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
                  disabled={page === totalPages - 1}
                  className="px-4 py-2 rounded-xl border border-gray-200 dark:border-surface-border bg-white dark:bg-surface-card disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50 dark:hover:bg-surface-muted transition-colors text-sm font-semibold text-gray-700 dark:text-white"
                >
                  Trang sau
                </button>
              </div>
            )}
          </div>
        )}
      </motion.div>
    </div>
  );
};

export default DriverHistoryPage;
