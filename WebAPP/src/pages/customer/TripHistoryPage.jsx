// TripHistoryPage.tsx
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

const TripHistoryPage = () => {
  const navigate = useNavigate();
  const { userProfile, setUserProfile } = useAuthStore();
  const [trips, setTrips] = useState([]);
  const [filter, setFilter] = useState('ALL');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const customerId = userProfile?.id || userProfile?.customerId;
    console.log('[TripHistory] customerId from store:', customerId, 'userProfile:', userProfile);
    if (!customerId) {
      console.log('[TripHistory] userProfile missing, calling getMyInfo...');
      customerApi.getMyInfo()
        .then((profile) => {
          console.log('[TripHistory] getMyInfo returned:', profile);
          setUserProfile(profile);
          const id = profile?.id || profile?.customerId;
          if (!id) { 
            console.warn('[TripHistory] Still no id after getMyInfo');
            setLoading(false); return; 
          }
          return bookingApi.getCustomerHistory(id);
        })
        .then((res) => {
          console.log('[TripHistory] getCustomerHistory fallback result:', res);
          setTrips(res || []);
        })
        .catch((e) => console.error('[TripHistory] Error:', e))
        .finally(() => setLoading(false));
      return;
    }
    bookingApi
      .getCustomerHistory(customerId)
      .then((res) => {
        console.log('[TripHistory] getCustomerHistory result:', res);
        setTrips(res || []);
      })
      .catch((e) => console.error('[TripHistory] Error:', e))
      .finally(() => setLoading(false));
  }, [userProfile?.id, userProfile?.customerId, setUserProfile]);

  const filteredTrips =
    filter === 'ALL' ? trips : trips.filter((t) => t.bookingStatus === filter);

  if (loading) {
    return (
      <div className="flex justify-center py-20">
        <Spinner size="lg" />
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto px-4 py-8 space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold text-content-main">Lịch sử chuyến đi</h1>
          <p className="text-sm text-content-muted mt-0.5">{trips.length} chuyến đã thực hiện</p>
        </div>
        <Button
          variant="outline"
          onClick={() => navigate('/customer/home')}
          className="w-fit"
        >
          + Đặt chuyến mới
        </Button>
      </div>

      {/* Filter tabs */}
      <div className="flex gap-1 border-b border-surface-border">
        {FILTERS.map((f) => (
          <button
            key={f.value}
            onClick={() => setFilter(f.value)}
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
      {filteredTrips.length === 0 ? (
        <div className="text-center py-16 text-content-muted">
          <p className="text-lg font-medium">Không có chuyến đi nào</p>
          <p className="text-sm mt-1">Hãy đặt chuyến đầu tiên để trải nghiệm</p>
        </div>
      ) : (
        <div className="space-y-4">
          {filteredTrips.map((trip) => (
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
                  {BOOKING_STATUS_LABEL[trip.bookingStatus]}
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

              {/* Driver, vehicle & price */}
              <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 pt-3 border-t border-surface-border">
                <div className="flex flex-wrap gap-4 text-sm">
                  <div>
                    <div className="text-content-muted text-xs">Tài xế</div>
                    <div className="font-medium text-content-main">{trip.driverName}</div>
                    <div className="text-xs text-content-muted">{trip.driverPhone}</div>
                  </div>
                  <div>
                    <div className="text-content-muted text-xs">Phương tiện</div>
                    <div className="font-medium text-content-main">{trip.vehicleTypeName}</div>
                  </div>
                </div>
                <div className="flex items-baseline gap-1">
                  <span className="text-content-muted text-sm">Tổng:</span>
                  <span className="text-xl font-bold text-brand-400">
                    {formatCurrency(trip.totalPrice)}
                  </span>
                </div>
              </div>

              {/* Actions */}
              <div className="flex gap-2 mt-4 pt-1">
                {trip.bookingStatus === BOOKING_STATUS.COMPLETED && (
                  <Button size="sm" variant="outline" className="text-xs">
                    Đánh giá
                  </Button>
                )}
                <Button
                  size="sm"
                  variant="ghost"
                  className="text-xs"
                  onClick={() =>
                    navigate('/customer/booking', {
                      state: {
                        pickup: trip.pickupLocation,
                        dropoff: trip.dropoffLocation,
                      },
                    })
                  }
                >
                  Đặt lại
                </Button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default TripHistoryPage;