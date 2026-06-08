import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/store/rootStore';
import { bookingApi } from '@/features/booking/api/bookingApi';
import { formatCurrency } from '@/utils/currency';
import { formatDate } from '@/utils/formatDate';
import { BOOKING_STATUS, BOOKING_STATUS_LABEL } from '@/config';
import Spinner from '@/components/Elements/Spinner';
import { cn } from '@/utils/cn';

// Status badge style
const statusBadge = {
  [BOOKING_STATUS.COMPLETED]: 'badge-green',
  [BOOKING_STATUS.CANCELLED]: 'badge-red',
  [BOOKING_STATUS.PENDING]:   'badge-yellow',
  default:                    'badge-gray',
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

  return (
    <div className="max-w-4xl mx-auto px-4 py-8 space-y-6 animate-fade-in">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold text-content-main">Lịch sử chuyến đi</h1>
          <p className="text-sm text-content-muted mt-0.5">{totalElements} chuyến đã nhận</p>
        </div>
      </div>

      {/* Filter tabs */}
      <div className="flex gap-1 border-b border-surface-border">
        {FILTERS.map((f) => (
          <button
            key={f.value}
            onClick={() => handleFilterChange(f.value)}
            className={cn(
              'px-4 py-2.5 text-sm font-medium transition-all',
              filter === f.value
                ? 'text-brand-400 border-b-2 border-brand-400 -mb-px'
                : 'text-content-muted hover:text-content-main'
            )}
          >
            {f.label}
          </button>
        ))}
      </div>

      {/* Trip list */}
      {loading && trips.length === 0 ? (
        <div className="flex justify-center py-20">
          <Spinner size="lg" />
        </div>
      ) : trips.length === 0 ? (
        <div className="text-center py-16 text-content-muted">
          <p className="text-lg font-medium">Không có chuyến đi nào</p>
        </div>
      ) : (
        <div className="space-y-4">
          {trips.map((trip) => (
            <div
              key={trip.bookingId}
              className="card p-5 hover:border-brand-500/40 transition-all duration-200"
            >
              {/* Header: Mã chuyến + Trạng thái */}
              <div className="flex flex-wrap justify-between items-center gap-2 mb-4">
                <div className="flex items-center gap-2">
                  <span className="text-xs font-mono text-content-muted bg-surface-muted px-2.5 py-1 rounded-md">
                    #{trip.bookingId.slice(-8)}
                  </span>
                  <span className="text-xs text-content-muted">•</span>
                  <span className="text-xs text-content-muted flex items-center gap-1">
                    <span>📅</span> {formatDate(trip.bookingTime)}
                  </span>
                  {trip.distance && (
                    <>
                      <span className="text-xs text-content-muted">•</span>
                      <span className="text-xs text-content-muted flex items-center gap-1">
                        <span>📏</span> {trip.distance.toFixed(1)} km
                      </span>
                    </>
                  )}
                </div>
                <span
                  className={cn(
                    'text-xs font-semibold px-2.5 py-1 rounded-full',
                    statusBadge[trip.bookingStatus] || statusBadge.default
                  )}
                >
                  {BOOKING_STATUS_LABEL[trip.bookingStatus] || trip.bookingStatus}
                </span>
              </div>

              {/* Route: Điểm đón - trả */}
              <div className="space-y-2 mb-4">
                <div className="flex items-start gap-2 text-sm">
                  <span className="text-brand-400 mt-0.5">📍</span>
                  <span className="text-content-main">{trip.pickupLocation}</span>
                </div>
                <div className="flex items-start gap-2 text-sm">
                  <span className="text-red-500 mt-0.5">🏁</span>
                  <span className="text-content-main">{trip.dropoffLocation}</span>
                </div>
              </div>

              {/* Customer, vehicle & price */}
              <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 pt-3 border-t border-surface-border">
                <div className="flex flex-wrap gap-4 text-sm">
                  <div>
                    <div className="text-content-muted text-xs">Khách hàng</div>
                    <div className="font-medium text-content-main">{trip.customerName || 'Khách vãng lai'}</div>
                    {trip.customerPhone && <div className="text-xs text-content-muted">{trip.customerPhone}</div>}
                  </div>
                  <div>
                    <div className="text-content-muted text-xs">Thanh toán</div>
                    <div className="font-medium text-content-main">
                      {trip.paymentMethod === 'CASH' ? 'Tiền mặt' : 'Trực tuyến'}
                    </div>
                  </div>
                </div>
                <div className="flex items-baseline gap-1">
                  <span className="text-content-muted text-sm">Tổng:</span>
                  <span className="text-xl font-bold text-brand-400">
                    {formatCurrency(trip.totalPrice)}
                  </span>
                </div>
              </div>
            </div>
          ))}

          {/* Pagination Controls */}
          {totalPages > 1 && (
            <div className="flex flex-wrap items-center justify-center gap-2 mt-8 pt-4">
              <button
                onClick={() => setPage(p => Math.max(0, p - 1))}
                disabled={page === 0}
                className="px-3 py-1.5 rounded-lg border border-surface-border disabled:opacity-50 disabled:cursor-not-allowed hover:bg-surface-muted transition-colors text-sm"
              >
                Trang trước
              </button>
              <div className="flex flex-wrap items-center gap-1">
                {Array.from({ length: totalPages }).map((_, i) => (
                  <button
                    key={i}
                    onClick={() => setPage(i)}
                    className={cn(
                      "w-8 h-8 rounded-lg flex items-center justify-center text-sm transition-colors",
                      page === i 
                        ? "bg-brand-500 text-white" 
                        : "hover:bg-surface-muted text-content-main"
                    )}
                  >
                    {i + 1}
                  </button>
                ))}
              </div>
              <button
                onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
                disabled={page === totalPages - 1}
                className="px-3 py-1.5 rounded-lg border border-surface-border disabled:opacity-50 disabled:cursor-not-allowed hover:bg-surface-muted transition-colors text-sm"
              >
                Trang sau
              </button>
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default DriverHistoryPage;
