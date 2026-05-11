import { useState, useEffect, useCallback } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import toast from 'react-hot-toast'
import axios from 'axios'
import {
  RiMapPinLine, RiMapPin2Line, RiUserStarLine,
  RiPhoneLine, RiMessage2Line, RiStarLine, RiCarLine,
} from 'react-icons/ri'
import { useBookingStore } from '@/store/rootStore'
import { bookingApi } from '@/features/booking/api/bookingApi'
import { useWebSocket } from '@/hooks/useWebSocket'
import { BOOKING_STATUS, BOOKING_STATUS_LABEL } from '@/config'
import { formatCurrency } from '@/utils/currency'
import Button from '@/components/Elements/Button'
import Spinner from '@/components/Elements/Spinner'
import ChatDialog from '@/features/chat/components/ChatDialog'
import InteractiveMap from '@/components/Map/InteractiveMap'
import { cn } from '@/utils/cn'

const STATUS_STEPS = [
  BOOKING_STATUS.PENDING,
  BOOKING_STATUS.ACCEPTED,
  BOOKING_STATUS.PICKING_UP,
  BOOKING_STATUS.IN_TRANSIT,
  BOOKING_STATUS.COMPLETED,
]

const STATUS_COLOR = {
  [BOOKING_STATUS.PENDING]:    'text-yellow-400 bg-yellow-400/10 border-yellow-400/20',
  [BOOKING_STATUS.ACCEPTED]:   'text-blue-400 bg-blue-400/10 border-blue-400/20',
  [BOOKING_STATUS.PICKING_UP]: 'text-brand-400 bg-brand-400/10 border-brand-400/20',
  [BOOKING_STATUS.IN_TRANSIT]: 'text-brand-400 bg-brand-400/10 border-brand-400/20',
  [BOOKING_STATUS.COMPLETED]:  'text-brand-400 bg-brand-400/10 border-brand-400/20',
  [BOOKING_STATUS.CANCELLED]:  'text-red-400 bg-red-400/10 border-red-400/20',
}

// Fallback dummy coordinates for Hanoi if geocoding fails
const DUMMY_PICKUP = { lat: 21.0285, lng: 105.8542 }
const DUMMY_DROPOFF = { lat: 21.0029, lng: 105.8202 }

const TripTrackingPage = () => {
  const location  = useLocation()
  const navigate  = useNavigate()
  const { currentBooking, setCurrentBooking, clearCurrentBooking } = useBookingStore()

  const bookingId = location.state?.bookingId || currentBooking?.bookingId

  const [booking,    setBooking]    = useState(currentBooking)
  const [loading,    setLoading]    = useState(!currentBooking)
  const [chatOpen,   setChatOpen]   = useState(false)
  const [cancelling, setCancelling] = useState(false)

  const [pickupCoord, setPickupCoord] = useState(location.state?.pickup || null)
  const [dropoffCoord, setDropoffCoord] = useState(location.state?.dropoff || null)
  const [driverCoord, setDriverCoord] = useState(null)

  // Fetch initial booking data
  useEffect(() => {
    if (!bookingId) { navigate('/customer/home'); return }
    if (!booking) {
      setLoading(true)
      bookingApi.getById(bookingId)
        .then((b) => { setBooking(b); setCurrentBooking(b) })
        .catch(() => toast.error('Không tìm thấy thông tin chuyến đi'))
        .finally(() => setLoading(false))
    }
  }, [bookingId, booking, navigate, setCurrentBooking])

  // Geocode addresses if coords are missing
  useEffect(() => {
    if (booking && !pickupCoord) {
      axios.get('https://nominatim.openstreetmap.org/search', {
        params: { q: booking.pickupLocation, format: 'json', limit: 1 }
      }).then(res => {
        if (res.data[0]) setPickupCoord({ lat: parseFloat(res.data[0].lat), lng: parseFloat(res.data[0].lon), name: booking.pickupLocation })
        else setPickupCoord({ ...DUMMY_PICKUP, name: booking.pickupLocation })
      }).catch(() => setPickupCoord({ ...DUMMY_PICKUP, name: booking.pickupLocation }))
    }
    
    if (booking && !dropoffCoord) {
      axios.get('https://nominatim.openstreetmap.org/search', {
        params: { q: booking.dropoffLocation, format: 'json', limit: 1 }
      }).then(res => {
        if (res.data[0]) setDropoffCoord({ lat: parseFloat(res.data[0].lat), lng: parseFloat(res.data[0].lon), name: booking.dropoffLocation })
        else setDropoffCoord({ ...DUMMY_DROPOFF, name: booking.dropoffLocation })
      }).catch(() => setDropoffCoord({ ...DUMMY_DROPOFF, name: booking.dropoffLocation }))
    }
  }, [booking, pickupCoord, dropoffCoord])

  // Update driver coordinates based on status (mocking movement)
  useEffect(() => {
    if (booking?.status === BOOKING_STATUS.PICKING_UP && pickupCoord) {
      setDriverCoord({ lat: pickupCoord.lat - 0.005, lng: pickupCoord.lng - 0.005 })
    } else if (booking?.status === BOOKING_STATUS.IN_TRANSIT && dropoffCoord) {
      setDriverCoord({ lat: dropoffCoord.lat - 0.005, lng: dropoffCoord.lng - 0.005 })
    }
  }, [booking?.status, pickupCoord, dropoffCoord])

  // Poll for status updates every 5s when pending/accepted
  useEffect(() => {
    const shouldPoll = [BOOKING_STATUS.PENDING, BOOKING_STATUS.ACCEPTED, BOOKING_STATUS.PICKING_UP, BOOKING_STATUS.IN_TRANSIT]
      .includes(booking?.status)
    if (!shouldPoll) return
    const interval = setInterval(() => {
      bookingApi.getById(bookingId)
        .then((updated) => {
          if (updated.status !== booking.status) {
             toast.success(`Trạng thái: ${BOOKING_STATUS_LABEL[updated.status]}`)
          }
          setBooking(updated)
          setCurrentBooking(updated)
          if (updated.status === BOOKING_STATUS.COMPLETED) {
            clearInterval(interval)
            toast.success('Chuyến đi hoàn thành!')
          }
        })
        .catch(() => {})
    }, 5000)
    return () => clearInterval(interval)
  }, [booking?.status, bookingId, setCurrentBooking])

  // WebSocket for realtime updates
  const onWsMessage = useCallback((topic, payload) => {
    if (payload?.bookingId === bookingId) {
      setBooking((prev) => ({ ...prev, ...payload }))
      setCurrentBooking((prev) => ({ ...prev, ...payload }))
      toast.success(`Trạng thái: ${BOOKING_STATUS_LABEL[payload.status]}`)
    }
  }, [bookingId, setCurrentBooking])

  useWebSocket([`/topic/booking/${bookingId}`], onWsMessage)

  const handleCancel = async () => {
    if (!window.confirm('Bạn có chắc muốn hủy chuyến này?')) return
    setCancelling(true)
    try {
      await bookingApi.cancelBooking(bookingId)
      toast.success('Đã hủy chuyến đi')
      clearCurrentBooking()
      navigate('/customer/home')
    } catch (err) {
      toast.error(err?.response?.data?.message || 'Không thể hủy chuyến')
    } finally {
      setCancelling(false)
    }
  }

  const handleRateDriver = () =>
    navigate('/customer/rating', { state: { booking } })

  const stepIndex = STATUS_STEPS.indexOf(booking?.status)

  if (loading) return (
    <div className="flex items-center justify-center h-full min-h-[60vh]">
      <div className="text-center space-y-4">
        <Spinner size="xl" />
        <p className="text-content-muted">Đang tải thông tin chuyến đi...</p>
      </div>
    </div>
  )

  if (!booking) return null

  return (
    <div className="-m-6 h-[calc(100vh-64px)] relative flex flex-col bg-surface-dark">
      {/* Background Map */}
      <div className="absolute inset-0 z-0">
         <InteractiveMap pickup={pickupCoord} dropoff={dropoffCoord} driver={driverCoord} />
      </div>

      {/* Floating Header */}
      <div className="relative z-10 p-4 w-full max-w-2xl mx-auto">
        <div className="bg-surface-card/90 backdrop-blur-md rounded-2xl p-4 shadow-lg border border-surface-border">
          <div className="flex items-center justify-between mb-3">
            <div>
              <h1 className="font-semibold text-content-main">Chuyến đi #{booking.bookingId?.slice(-8)}</h1>
            </div>
            <span className={cn('badge border text-xs', STATUS_COLOR[booking.status] || STATUS_COLOR[BOOKING_STATUS.PENDING])}>
              <span className="w-1.5 h-1.5 rounded-full bg-current animate-pulse" />
              {BOOKING_STATUS_LABEL[booking.status] || booking.status}
            </span>
          </div>

          {/* Progress steps */}
          {booking.status !== BOOKING_STATUS.CANCELLED && (
            <div className="flex items-center justify-between mt-2">
              {STATUS_STEPS.slice(0, -1).map((s, i) => (
                <div key={s} className="flex items-center flex-1">
                  <div className={cn(
                    'w-6 h-6 rounded-full flex items-center justify-center text-[10px] font-bold shrink-0 transition-all duration-500',
                    i <= stepIndex ? 'bg-brand-500 text-content-main shadow-glow-green' : 'bg-surface-border text-gray-600',
                  )}>
                    {i < stepIndex ? '✓' : i + 1}
                  </div>
                  {i < STATUS_STEPS.length - 2 && (
                    <div className={cn('flex-1 h-0.5 mx-1 transition-all duration-700', i < stepIndex ? 'bg-brand-500' : 'bg-surface-border')} />
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Spacer to push Bottom Sheet down */}
      <div className="flex-1 min-h-0 pointer-events-none" />

      {/* Bottom Sheet Card */}
      <div className="relative z-10 w-full max-w-2xl mx-auto bg-surface-card rounded-t-3xl shadow-[0_-10px_40px_rgba(0,0,0,0.5)] border-t border-x border-surface-border flex flex-col pointer-events-auto">
        {/* Drag handle pill */}
        <div className="w-full flex justify-center py-3 shrink-0">
          <div className="w-12 h-1.5 bg-surface-border rounded-full" />
        </div>

        <div className="p-5 pt-0 overflow-y-auto no-scrollbar space-y-4 max-h-[60vh]">
          {/* Driver info */}
          {booking.driverId ? (
            <div className="bg-surface rounded-2xl p-4 border border-surface-border space-y-4">
              <div className="flex items-center gap-4">
                <div className="w-12 h-12 rounded-full bg-brand-500/20 border-2 border-brand-500/30 flex items-center justify-center text-xl font-bold text-brand-400 overflow-hidden shrink-0">
                  {booking.driverName?.[0] || <RiUserStarLine />}
                </div>
                <div className="flex-1">
                  <p className="font-semibold text-content-main">{booking.driverName}</p>
                  <div className="flex items-center gap-1 text-yellow-400">
                    <RiStarLine size={14} />
                    <span className="text-xs font-medium">4.8</span>
                  </div>
                  <div className="flex items-center gap-1 mt-1">
                    <RiCarLine size={14} className="text-content-muted" />
                    <span className="text-xs text-content-muted">{booking.vehicleTypeName} · {booking.licensePlate}</span>
                  </div>
                </div>
                <div className="flex gap-2">
                  <button className="w-10 h-10 rounded-xl bg-surface-border hover:bg-surface-muted flex items-center justify-center text-content-muted hover:text-content-main transition-colors" title={booking.driverPhone}>
                    <RiPhoneLine size={18} />
                  </button>
                  <button
                    onClick={() => setChatOpen(true)}
                    className="w-10 h-10 rounded-xl bg-brand-500/15 border border-brand-500/30 hover:bg-brand-500/25 flex items-center justify-center text-brand-400 transition-colors"
                  >
                    <RiMessage2Line size={18} />
                  </button>
                </div>
              </div>
            </div>
          ) : (
            <div className="bg-surface rounded-2xl p-4 border border-surface-border text-center flex flex-col items-center justify-center py-6">
              <Spinner className="mb-3" />
              <p className="text-sm text-content-muted">Đang tìm tài xế phù hợp cho bạn...</p>
            </div>
          )}

          {/* Route info */}
          <div className="bg-surface rounded-2xl p-4 border border-surface-border space-y-3">
            <div className="flex items-start gap-3">
              <RiMapPinLine size={18} className="text-brand-400 shrink-0 mt-0.5" />
              <div>
                <p className="text-xs text-content-muted">Điểm đón</p>
                <p className="text-content-main text-sm font-medium line-clamp-1">{booking.pickupLocation}</p>
              </div>
            </div>
            <div className="w-px h-4 bg-surface-border ml-[8px]" />
            <div className="flex items-start gap-3">
              <RiMapPin2Line size={18} className="text-red-400 shrink-0 mt-0.5" />
              <div>
                <p className="text-xs text-content-muted">Điểm đến</p>
                <p className="text-content-main text-sm font-medium line-clamp-1">{booking.dropoffLocation}</p>
              </div>
            </div>
            <div className="border-t border-surface-border pt-3 flex justify-between text-sm">
              <span className="text-content-muted">Tổng cước phí</span>
              <span className="font-display font-bold text-brand-400">{formatCurrency(booking.totalPrice)}</span>
            </div>
          </div>

          {/* Actions */}
          <div className="space-y-3 pt-2">
            {booking.status === BOOKING_STATUS.COMPLETED && (
              <Button fullWidth size="lg" onClick={handleRateDriver}>
                <RiStarLine size={18} /> Đánh giá chuyến đi
              </Button>
            )}
            {[BOOKING_STATUS.PENDING, BOOKING_STATUS.ACCEPTED].includes(booking.status) && (
              <Button variant="danger" fullWidth onClick={handleCancel} loading={cancelling}>
                Hủy chuyến đi
              </Button>
            )}
            <Button variant="ghost" fullWidth onClick={() => navigate('/customer/home')}>
              Về trang chủ
            </Button>
          </div>
        </div>
      </div>

      {/* Chat dialog */}
      {chatOpen && booking.driverId && (
        <div className="absolute inset-0 z-50">
          <ChatDialog
            bookingId={booking.bookingId}
            driverName={booking.driverName}
            onClose={() => setChatOpen(false)}
          />
        </div>
      )}
    </div>
  )
}

export default TripTrackingPage
