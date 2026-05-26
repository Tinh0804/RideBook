// TripHistoryPage.tsx
import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/store/rootStore';
import { bookingApi } from '@/features/booking/api/bookingApi';
import { formatCurrency } from '@/utils/currency';
import { formatDate } from '@/utils/formatDate';
import { BOOKING_STATUS, BOOKING_STATUS_LABEL } from '@/config';
import Button from '@/components/Elements/Button';
import Spinner from '@/components/Elements/Spinner';
import { cn } from '@/utils/cn';

// Status badge style
const statusBadge = {
  [BOOKING_STATUS.COMPLETED]: 'bg-green-500 text-white-100',
  [BOOKING_STATUS.CANCELLED]: 'bg-red-500 text-white-100',
  [BOOKING_STATUS.PENDING]: 'bg-yellow-500 text-yellow-100',
  default: 'bg-gray-100 text-gray-700',
};

const FILTERS = [
  { value: 'ALL', label: 'Tất cả' },
  { value: BOOKING_STATUS.COMPLETED, label: 'Hoàn thành' },
  { value: BOOKING_STATUS.CANCELLED, label: 'Đã hủy' },
];

const TripHistoryPage = () => {
  const navigate = useNavigate();
  const { userProfile } = useAuthStore();
  const [trips, setTrips] = useState([]);
  const [filter, setFilter] = useState('ALL');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!userProfile?.customerId) return;
    bookingApi
      .getCustomerHistory(userProfile.customerId)
      .then((res) => setTrips(res || []))
      .catch(() => { })
      .finally(() => setLoading(false));
  }, [userProfile]);

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
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-semibold text-gray-900">Lịch sử chuyến đi</h1>
          <p className="text-sm text-gray-500 mt-1">{trips.length} chuyến</p>
        </div>
        <Button variant="outline" onClick={() => navigate('/customer/home')}>
          Đặt chuyến mới
        </Button>
      </div>

      {/* Filter tabs */}
      <div className="flex gap-2 border-b border-gray-200">
        {FILTERS.map((f) => (
          <button
            key={f.value}
            onClick={() => setFilter(f.value)}
            className={cn(
              'px-4 py-2 text-sm font-medium transition-all',
              filter === f.value
                ? 'text-blue-600 border-b-2 border-blue-600 -mb-px'
                : 'text-gray-500 hover:text-gray-700'
            )}
          >
            {f.label}
          </button>
        ))}
      </div>

      {/* Trip list */}
      {filteredTrips.length === 0 ? (
        <div className="text-center py-16 text-gray-500">
          <p className="text-lg">Không có chuyến đi nào</p>
          <p className="text-sm mt-1">Hãy đặt chuyến đầu tiên</p>
        </div>
      ) : (
        <div className="space-y-4">
          {filteredTrips.map((trip) => (
            <div
              key={trip.bookingId}
              className="bg-surface border solid rounded-xl  shadow-sm hover:shadow-md transition-shadow p-5"
            >
              {/* Row 1: Mã chuyến + Trạng thái */}
              <div className="flex justify-between items-start mb-3">
                <div>
                  <span className="text-xs font-mono text-gray-500 bg-yellow-100 px-2 py-1 rounded">
                    #{trip.bookingId.slice(-8)}
                  </span>
                </div>
                <span
                  className={cn(
                    'text-xs font-medium px-2 py-1 rounded-full',
                    statusBadge[trip.bookingStatus] || statusBadge.default
                  )}
                >
                  {BOOKING_STATUS_LABEL[trip.bookingStatus]}
                </span>
              </div>

              {/* Row 2: Thời gian đặt + Khoảng cách */}
              <div className="flex flex-wrap justify-between text-sm text-gray-500 mb-4">
                <span>📅 {formatDate(trip.bookingTime)}</span>
                <span>📏 {trip.distance?.toFixed(1)} km</span>
              </div>

              {/* Row 3: Điểm đón - trả */}
              <div className="space-y-2 mb-4">
                <div className="flex items-start gap-2 text-sm">
                  <span className="text-blue-500 mt-0.5">📍</span>
                  <span className="text-gray-700">{trip.pickupLocation}</span>
                </div>
                <div className="flex items-start gap-2 text-sm">
                  <span className="text-red-500 mt-0.5">🏁</span>
                  <span className="text-gray-700">{trip.dropoffLocation}</span>
                </div>
              </div>

              {/* Row 4: Tài xế + xe + giá */}
              <div className="grid grid-cols-2 gap-3 pt-3 border-t border-gray-100 text-sm">
                <div>
                  <div className="text-gray-500">Tài xế</div>
                  <div className="font-medium text-white-900">{trip.driverName}</div>
                  <div className="text-xs text-gray-400">{trip.driverPhone}</div>
                </div>
                <div>
                  <div className="text-gray-500">Phương tiện</div>
                  <div className="font-medium text-gray-900">{trip.vehicleTypeName}</div>
                </div>
                <div className="col-span-2 flex justify-between items-center mt-2">
                  <span className="text-gray-500">Tổng tiền</span>
                  <span className="text-lg font-bold text-blue-600">
                    {formatCurrency(trip.totalPrice)}
                  </span>
                </div>
              </div>

              {/* Actions */}
              <div className="flex gap-2 mt-4 pt-2">
                {trip.bookingStatus === BOOKING_STATUS.COMPLETED && (
                  <Button size="sm" variant="outline">
                    Đánh giá
                  </Button>
                )}
                <Button
                  size="sm"
                  variant="ghost"
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