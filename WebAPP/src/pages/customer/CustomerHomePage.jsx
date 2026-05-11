import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { RiMapPinLine, RiMapPin2Line, RiArrowRightLine, RiHistoryLine, RiUserStarLine } from 'react-icons/ri'
import { useAuthStore, useBookingStore } from '@/store/rootStore'
import { masterDataApi } from '@/features/booking/api/masterDataApi'
import { bookingApi } from '@/features/booking/api/bookingApi'
import { formatCurrency } from '@/utils/currency'
import Button from '@/components/Elements/Button'
import Input from '@/components/Elements/Input'
import { cn } from '@/utils/cn'

const QUICK_DESTINATIONS = [
  { label: 'Sân bay', icon: '✈️', address: 'Sân bay Tân Sơn Nhất' },
  { label: 'Trung tâm', icon: '🏢', address: 'Trung tâm TP.HCM' },
  { label: 'Bệnh viện', icon: '🏥', address: 'Bệnh viện Chợ Rẫy' },
  { label: 'Trường học', icon: '🏫', address: 'ĐH Bách Khoa TP.HCM' },
]

const CustomerHomePage = () => {
  const navigate  = useNavigate()
  const { user }  = useAuthStore()
  const { vehicleTypes, setVehicleTypes, currentBooking } = useBookingStore()

  const [pickup,  setPickup]  = useState('')
  const [dropoff, setDropoff] = useState('')
  const [recentTrips, setRecentTrips] = useState([])

  useEffect(() => {
    // Load vehicle types
    if (!vehicleTypes.length) {
      masterDataApi.getVehicleTypes()
        .then((types) => setVehicleTypes(types))
        .catch(() => {})
    }
    // Load recent trips
    if (user?.id) {
      bookingApi.getCustomerHistory(user.id)
        .then((trips) => setRecentTrips(trips.slice(0, 3)))
        .catch(() => {})
    }
  }, [user, vehicleTypes.length, setVehicleTypes])

  const handleBook = () => {
    if (currentBooking) {
      navigate('/customer/tracking')
      return
    }
    if (!pickup.trim() || !dropoff.trim()) return
    navigate('/customer/booking', { state: { pickup: { name: pickup }, dropoff: { name: dropoff } } })
  }

  const setQuickDest = (addr) => setDropoff(addr)

  const hour = new Date().getHours()
  const greeting =
    hour < 12 ? 'Chào buổi sáng' :
    hour < 18 ? 'Chào buổi chiều' : 'Chào buổi tối'

  return (
    <div className="space-y-8 max-w-2xl">
      {/* Greeting */}
      <div>
        <p className="text-content-muted text-sm">{greeting} 👋</p>
        <h1 className="font-display text-3xl font-bold text-content-main mt-1">
          {user?.name || user?.userName || 'Bạn'}
        </h1>
        <p className="text-content-muted mt-1">Bạn muốn đi đâu hôm nay?</p>
      </div>

      {/* Booking card */}
      <div className="card p-6 space-y-4">
        <h2 className="font-semibold text-content-main">Đặt chuyến</h2>

        {/* Pickup input */}
        <div className="flex items-center gap-3">
          <div className="w-8 h-8 rounded-full bg-brand-500/15 border border-brand-500/30 flex items-center justify-center shrink-0">
            <RiMapPinLine size={16} className="text-brand-400" />
          </div>
          <Input
            placeholder="Điểm đón của bạn"
            value={pickup}
            onChange={(e) => setPickup(e.target.value)}
            className="flex-1"
          />
        </div>

        {/* Connector line */}
        <div className="flex items-center gap-3">
          <div className="w-8 flex flex-col items-center gap-1">
            {[...Array(3)].map((_, i) => (
              <div key={i} className="w-1 h-1 rounded-full bg-surface-muted" />
            ))}
          </div>
          <div className="flex-1 h-px bg-surface-border" />
        </div>

        {/* Dropoff input */}
        <div className="flex items-center gap-3">
          <div className="w-8 h-8 rounded-full bg-red-500/15 border border-red-500/30 flex items-center justify-center shrink-0">
            <RiMapPin2Line size={16} className="text-red-400" />
          </div>
          <Input
            placeholder="Điểm đến"
            value={dropoff}
            onChange={(e) => setDropoff(e.target.value)}
            className="flex-1"
          />
        </div>

        <Button
          fullWidth size="lg"
          onClick={handleBook}
          disabled={!pickup.trim() || !dropoff.trim() || !!currentBooking}
          className="mt-2"
        >
          {currentBooking ? 'Xem chuyến đang đi' : 'Tìm xe ngay'} <RiArrowRightLine size={18} />
        </Button>
      </div>

      {/* Quick destinations */}
      <div className="space-y-3">
        <h3 className="text-sm font-semibold text-content-muted uppercase tracking-wider">Điểm đến phổ biến</h3>
        <div className="grid grid-cols-2 gap-3">
          {QUICK_DESTINATIONS.map((d) => (
            <button
              key={d.label}
              onClick={() => setQuickDest(d.address)}
              className={cn(
                'card-hover p-4 text-left space-y-1 group',
                dropoff === d.address && 'border-brand-500/40 bg-brand-500/5'
              )}
            >
              <span className="text-2xl">{d.icon}</span>
              <p className="font-medium text-content-main text-sm group-hover:text-brand-400 transition-colors">{d.label}</p>
              <p className="text-xs text-content-muted truncate">{d.address}</p>
            </button>
          ))}
        </div>
      </div>

      {/* Vehicle types */}
      {vehicleTypes.length > 0 && (
        <div className="space-y-3">
          <h3 className="text-sm font-semibold text-content-muted uppercase tracking-wider">Loại xe</h3>
          <div className="flex gap-3 overflow-x-auto no-scrollbar pb-2">
            {vehicleTypes.map((vt) => (
              <div key={vt.id} className="card p-4 shrink-0 w-32 text-center space-y-2">
                <img
                  className="w-10 h-10 object-contain mx-auto mb-1"
                  src={vt.icon || '🚗'}
                  alt={vt.vehicleTypeName}
                />
                <p className="text-sm font-medium text-content-main">{vt.vehicleTypeName}</p>
                {vt.baseFare && (
                  <p className="text-xs text-content-muted">{formatCurrency(vt.baseFare)}</p>
                )}
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Recent trips */}
      {recentTrips.length > 0 && (
        <div className="space-y-3">
          <div className="flex items-center justify-between">
            <h3 className="text-sm font-semibold text-content-muted uppercase tracking-wider">Chuyến gần đây</h3>
            <button
              onClick={() => navigate('/customer/history')}
              className="text-xs text-brand-400 hover:text-brand-300 flex items-center gap-1 transition-colors"
            >
              Xem tất cả <RiArrowRightLine size={12} />
            </button>
          </div>
          <div className="space-y-2">
            {recentTrips.map((trip) => (
              <div key={trip.bookingId} className="card p-4 flex items-center gap-4">
                <div className="w-10 h-10 rounded-xl bg-surface-border flex items-center justify-center shrink-0">
                  <RiHistoryLine size={18} className="text-content-muted" />
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium text-content-main truncate">{trip.dropoffLocation}</p>
                  <p className="text-xs text-content-muted truncate">Từ: {trip.pickupLocation}</p>
                </div>
                <div className="text-right shrink-0">
                  <p className="text-sm font-semibold text-brand-400">{formatCurrency(trip.totalPrice)}</p>
                  <button
                    onClick={() => {
                      if (currentBooking) {
                        navigate('/customer/tracking')
                      } else {
                        navigate('/customer/booking', { state: { pickup: { name: trip.pickupLocation }, dropoff: { name: trip.dropoffLocation } } })
                      }
                    }}
                    className="text-xs text-content-muted hover:text-brand-400 transition-colors"
                    disabled={!!currentBooking}
                  >
                    Đặt lại
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}

export default CustomerHomePage
