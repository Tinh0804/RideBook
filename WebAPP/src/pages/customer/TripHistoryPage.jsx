import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { RiMapPinLine, RiMapPin2Line, RiStarLine, RiArrowRightLine } from 'react-icons/ri'
import { useAuthStore } from '@/store/rootStore'
import { bookingApi } from '@/features/booking/api/bookingApi'
import { formatCurrency } from '@/utils/currency'
import { formatDate } from '@/utils/formatDate'
import { BOOKING_STATUS, BOOKING_STATUS_LABEL } from '@/config'
import Spinner from '@/components/Elements/Spinner'
import Button from '@/components/Elements/Button'
import { cn } from '@/utils/cn'

const STATUS_BADGE = {
  [BOOKING_STATUS.COMPLETED]: 'badge-green',
  [BOOKING_STATUS.CANCELLED]: 'badge-red',
  [BOOKING_STATUS.PENDING]:   'badge-yellow',
  default:                    'badge-blue',
}

const FILTERS = [
  { value: 'ALL',                       label: 'Tất cả' },
  { value: BOOKING_STATUS.COMPLETED,    label: 'Hoàn thành' },
  { value: BOOKING_STATUS.CANCELLED,    label: 'Đã hủy' },
]

const TripHistoryPage = () => {
  const navigate = useNavigate()
  const { user } = useAuthStore()

  const [trips,   setTrips]   = useState([])
  const [filter,  setFilter]  = useState('ALL')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    if (!user?.id) return
    bookingApi.getCustomerHistory(user.id)
      .then((trips) => setTrips(trips))
      .catch(() => {})
      .finally(() => setLoading(false))
  }, [user])

  const filtered = filter === 'ALL' ? trips : trips.filter((t) => t.status === filter)

  return (
    <div className="space-y-6">
      <div>
        <h1 className="section-title">Lịch sử chuyến đi</h1>
        <p className="text-content-muted text-sm mt-1">{trips.length} chuyến đã thực hiện</p>
      </div>

      {/* Filter tabs */}
      <div className="flex gap-2 p-1 bg-surface-dark border border-surface-border rounded-xl w-fit">
        {FILTERS.map((f) => (
          <button
            key={f.value}
            onClick={() => setFilter(f.value)}
            className={cn(
              'px-4 py-2 rounded-lg text-sm font-medium transition-all duration-200',
              filter === f.value
                ? 'bg-brand-500 text-content-main shadow-glow-green'
                : 'text-content-muted hover:text-content-muted',
            )}
          >
            {f.label}
          </button>
        ))}
      </div>

      {/* Trip list */}
      {loading ? (
        <div className="flex justify-center py-16"><Spinner size="xl" /></div>
      ) : filtered.length === 0 ? (
        <div className="flex flex-col items-center justify-center py-16 space-y-3">
          <span className="text-5xl">🚗</span>
          <p className="text-content-muted">Chưa có chuyến đi nào</p>
          <Button variant="outline" onClick={() => navigate('/customer/home')}>
            Đặt chuyến đầu tiên
          </Button>
        </div>
      ) : (
        <div className="space-y-3">
          {filtered.map((trip) => (
            <div key={trip.id} className="card p-5 space-y-4 hover:border-brand-500/20 transition-colors">
              {/* Header */}
              <div className="flex items-start justify-between gap-3">
                <div className="space-y-1">
                  <p className="text-xs text-content-muted font-mono">#{trip.id?.slice(-8)}</p>
                  <p className="text-xs text-gray-600">{formatDate(trip.createdAt)}</p>
                </div>
                <span className={cn('badge', STATUS_BADGE[trip.status] || STATUS_BADGE.default)}>
                  {BOOKING_STATUS_LABEL[trip.status] || trip.status}
                </span>
              </div>

              {/* Route */}
              <div className="space-y-2">
                <div className="flex items-start gap-2">
                  <RiMapPinLine size={14} className="text-brand-400 shrink-0 mt-0.5" />
                  <p className="text-sm text-content-muted leading-tight">{trip.pickupLocation}</p>
                </div>
                <div className="flex items-start gap-2">
                  <RiMapPin2Line size={14} className="text-red-400 shrink-0 mt-0.5" />
                  <p className="text-sm text-content-muted leading-tight">{trip.dropoffLocation}</p>
                </div>
              </div>

              {/* Driver + fare */}
              <div className="flex items-center justify-between pt-2 border-t border-surface-border">
                <div className="flex items-center gap-2">
                  {trip.driver && (
                    <>
                      <div className="w-7 h-7 rounded-full bg-brand-500/20 border border-brand-500/30 flex items-center justify-center text-xs font-bold text-brand-400 overflow-hidden">
                        {trip.driver.avatar
                          ? <img src={trip.driver.avatar} alt={trip.driver.name} className="w-full h-full object-cover" />
                          : trip.driver.name?.[0]
                        }
                      </div>
                      <span className="text-sm text-content-muted">{trip.driver.name}</span>
                      {trip.rating && (
                        <span className="flex items-center gap-1 text-xs text-yellow-400">
                          <RiStarLine size={12} /> {trip.rating}
                        </span>
                      )}
                    </>
                  )}
                </div>
                <span className="font-display font-bold text-brand-400">{formatCurrency(trip.totalFare)}</span>
              </div>

              {/* Actions */}
              <div className="flex gap-2">
                {trip.status === BOOKING_STATUS.COMPLETED && !trip.rated && (
                  <Button
                    variant="outline" size="sm"
                    onClick={() => navigate('/customer/rating', { state: { booking: trip } })}
                  >
                    <RiStarLine size={14} /> Đánh giá
                  </Button>
                )}
                <Button
                  variant="ghost" size="sm"
                  onClick={() => navigate('/customer/booking', {
                    state: { pickup: trip.pickupLocation, dropoff: trip.dropoffLocation }
                  })}
                >
                  Đặt lại <RiArrowRightLine size={14} />
                </Button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

export default TripHistoryPage
