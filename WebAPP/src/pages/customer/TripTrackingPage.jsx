import { useState, useEffect, useCallback } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import toast from 'react-hot-toast'
import axios from 'axios'
import {
  RiMapPinLine, RiMapPin2Line, RiUserStarLine,
  RiPhoneLine, RiMessage2Line, RiStarLine, RiCarLine,RiCheckLine
} from 'react-icons/ri'
import { useBookingStore, useAuthStore } from '@/store/rootStore'
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
  BOOKING_STATUS.ARRIVED,
  BOOKING_STATUS.IN_PROGRESS,
  BOOKING_STATUS.COMPLETED,
]

const STATUS_COLOR = {
  [BOOKING_STATUS.PENDING]:    'text-yellow-400 bg-yellow-400/10 border-yellow-400/20',
  [BOOKING_STATUS.ACCEPTED]:   'text-blue-400 bg-blue-400/10 border-blue-400/20',
  [BOOKING_STATUS.ARRIVED]:    'text-brand-400 bg-brand-400/10 border-brand-400/20',
  [BOOKING_STATUS.IN_PROGRESS]:'text-brand-400 bg-brand-400/10 border-brand-400/20',
  [BOOKING_STATUS.COMPLETED]:  'text-brand-400 bg-brand-400/10 border-brand-400/20',
  [BOOKING_STATUS.CANCELLED]:  'text-red-400 bg-red-400/10 border-red-400/20',
}


const TripTrackingPage = () => {
  const location  = useLocation()
  const navigate  = useNavigate()
  const { currentBooking, setCurrentBooking, clearCurrentBooking } = useBookingStore()
  const { user, userProfile } = useAuthStore()

  const bookingId = location.state?.bookingId || currentBooking?.bookingId
  const customerId = userProfile?.id || user?.id

  const [booking,    setBooking]    = useState(currentBooking)
  const [loading,    setLoading]    = useState(!currentBooking)
  const [chatOpen,   setChatOpen]   = useState(false)
  const [cancelling, setCancelling] = useState(false)

  // Sync URL with booking ID silently
  useEffect(() => {
    if (bookingId) {
      window.history.replaceState(null, '', `/customer/tracking/${bookingId}`)
    } else {
      window.history.replaceState(null, '', `/customer/tracking`)
    }
  }, [bookingId])

  const [pickupCoord, setPickupCoord] = useState(
    location.state?.pickup || null
  )
  const [dropoffCoord, setDropoffCoord] = useState(
    location.state?.dropoff || null
  )
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

  // Set coords directly from backend data (no Nominatim geocoding needed anymore)
  useEffect(() => {
    if (booking) {
      if (booking.pickupLat && booking.pickupLng) {
        setPickupCoord({
          lat: booking.pickupLat,
          lng: booking.pickupLng,
          name: booking.pickupLocation
        })
      }
      if (booking.dropoffLat && booking.dropoffLng) {
        setDropoffCoord({
          lat: booking.dropoffLat,
          lng: booking.dropoffLng,
          name: booking.dropoffLocation
        })
      }
    }
  }, [booking])

  // Driver location is received via WebSocket (see onWsMessage below)
  // No API polling needed — driver pushes GPS updates through /app/driver/location -> /topic/booking/{bookingId}/driver-location

  // Fetch initial driver location from Redis when mounting/reloading
  useEffect(() => {
    if (!bookingId || driverCoord) return
    
    const status = booking?.bookingStatus
    if (status && ![BOOKING_STATUS.ACCEPTED, BOOKING_STATUS.ARRIVED, BOOKING_STATUS.IN_PROGRESS].includes(status)) {
      return
    }

    bookingApi.getDriverLocation(bookingId)
      .then((loc) => {
        if (loc && loc.lat && loc.lng) {
          setDriverCoord({
            lat: loc.lat,
            lng: loc.lng,
            name: booking?.driverName || 'Tài xế'
          })
        }
      })
      .catch(() => {})
  }, [bookingId, booking?.bookingStatus, booking?.driverName, driverCoord])

  // Poll for status updates every 5s when pending/accepted
  useEffect(() => {
    const shouldPoll = [BOOKING_STATUS.PENDING, BOOKING_STATUS.ACCEPTED, BOOKING_STATUS.ARRIVED, BOOKING_STATUS.IN_PROGRESS]
      .includes(booking?.bookingStatus)
    if (!shouldPoll) return
    const interval = setInterval(() => {
      bookingApi.getById(bookingId)
        .then((updated) => {
          if (updated.bookingStatus !== booking.bookingStatus) {
             toast.success(`Trạng thái: ${BOOKING_STATUS_LABEL[updated.bookingStatus]}`)
          }
          setBooking(updated)
          setCurrentBooking(updated)
          if (updated.bookingStatus === BOOKING_STATUS.COMPLETED) {
            clearInterval(interval)
            toast.success('Chuyến đi hoàn thành!')
          }
        })
        .catch(() => {})
    }, 5000)
    return () => clearInterval(interval)
  }, [booking?.bookingStatus, bookingId, setCurrentBooking])

  // WebSocket for realtime updates
  const onWsMessage = useCallback((topic, payload) => {
    // Driver location push from /topic/booking/{bookingId}/driver-location
    if (topic === `/topic/booking/${bookingId}/driver-location`) {
      if (payload?.lat && payload?.lng) {
        setDriverCoord({
          lat: payload.lat,
          lng: payload.lng,
          name: booking?.driverName || 'Tài xế',
          vehicleTypeName: booking?.vehicleTypeName,
          vehicleTypeIcon: booking?.vehicleTypeIcon
        })
      }
      return
    }

    if (typeof payload === 'string' && payload.startsWith('DRIVER_CANCELLED:')) {
      const canceledBookingId = payload.split(':')[1]
      if (bookingId === canceledBookingId) {
        toast.error('Tài xế đã huỷ chuyến đi. Vui lòng đặt lại!', { duration: 5000 })
        clearCurrentBooking()
        navigate('/customer/home')
      }
      return
    }

    if (payload?.bookingId === bookingId) {
      setBooking((prev) => ({ ...prev, ...payload }))
      setCurrentBooking((prev) => ({ ...prev, ...payload }))
      toast.success(`Trạng thái: ${BOOKING_STATUS_LABEL[payload.bookingStatus]}`)
    }
  }, [bookingId, booking?.driverName, setCurrentBooking, clearCurrentBooking, navigate])

  useWebSocket([
    `/topic/booking/${bookingId}`,
    `/topic/booking/${bookingId}/driver-location`,
    customerId ? `/topic/customer/${customerId}` : null
  ].filter(Boolean), onWsMessage)

  const handleCancel = () => {
    if (!window.confirm('Bạn có chắc muốn hủy chuyến này?')) return
    setCancelling(true)
    bookingApi.cancelBooking(bookingId)
      .then((res) => {
        toast.success('Đã hủy chuyến')
        setBooking(res)
        clearCurrentBooking()
      })
      .catch(() => toast.error('Không thể hủy chuyến lúc này'))
      .finally(() => setCancelling(false))
  }

  const handleRateDriver = () => {
    clearCurrentBooking()
    navigate('/customer/rating', { state: { booking } })
  }

  const stepIndex = STATUS_STEPS.indexOf(booking?.bookingStatus)

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
    <div className="-m-6 h-[calc(100vh-64px)] flex flex-col lg:flex-row bg-surface-dark overflow-hidden">
      {/* Vùng 1: Thông tin chuyến đi */}
      <div className="w-full lg:w-[450px] flex flex-col h-[55vh] lg:h-full bg-surface-card border-b lg:border-b-0 lg:border-r border-surface-border z-10 shadow-2xl shrink-0">
        
        {/* Header & Status (Sticky) */}
        <div className="p-5 border-b border-surface-border bg-surface-card/95 backdrop-blur-md sticky top-0 z-20">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h1 className="font-display font-bold text-content-main text-xl">Chuyến đi #{booking.bookingId?.slice(-8)}</h1>
              <p className="text-xs text-content-muted mt-1">Đang theo dõi hành trình</p>
            </div>
            <span className={cn('badge border text-xs px-3 py-1.5 shadow-sm', STATUS_COLOR[booking.bookingStatus] || STATUS_COLOR[BOOKING_STATUS.PENDING])}>
              <span className="w-2 h-2 rounded-full bg-current animate-pulse mr-2 inline-block" />
              {BOOKING_STATUS_LABEL[booking.bookingStatus] || booking.bookingStatus}
            </span>
          </div>

          {/* Progress steps */}
          {booking.bookingStatus !== BOOKING_STATUS.CANCELLED && (
            <div className="flex items-center justify-between mt-2">
              {STATUS_STEPS.slice(0, -1).map((s, i) => (
                <div key={s} className="flex items-center flex-1">
                  <div className={cn(
                    'w-7 h-7 rounded-full flex items-center justify-center text-xs font-bold shrink-0 transition-all duration-500',
                    i <= stepIndex ? 'bg-brand-500 text-content-main shadow-glow-green' : 'bg-surface-border text-gray-500',
                  )}>
                    {i < stepIndex ? <RiCheckLine size={14} /> : i + 1}
                  </div>
                  {i < STATUS_STEPS.length - 2 && (
                    <div className={cn('flex-1 h-1 mx-1.5 rounded-full transition-all duration-700', i < stepIndex ? 'bg-brand-500' : 'bg-surface-border')} />
                  )}
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Scrollable Info Area */}
        <div className="p-5 overflow-y-auto no-scrollbar space-y-6 flex-1">
          {/* Driver info */}
          {booking.driverId ? (
            <div className="bg-surface rounded-2xl p-5 border border-surface-border shadow-sm space-y-4">
              <h3 className="font-semibold text-content-main text-sm">Thông tin tài xế</h3>
              <div className="flex items-center gap-4">
                <div className="w-14 h-14 rounded-full bg-brand-500/20 border-2 border-brand-500/40 flex items-center justify-center text-2xl font-bold text-brand-400 overflow-hidden shrink-0 shadow-glow-green">
                  {booking.driverName?.[0] || <RiUserStarLine />}
                </div>
                <div className="flex-1">
                  <p className="font-bold text-content-main text-lg">{booking.driverName}</p>
                  <div className="flex items-center gap-1 text-yellow-400 mt-0.5">
                    <RiStarLine size={16} />
                    <span className="text-sm font-medium">5.0</span>
                  </div>
                  <div className="flex items-center gap-1.5 mt-2 bg-surface-dark px-2 py-1 rounded-md inline-flex">
                    <RiCarLine size={14} className="text-content-muted" />
                    <span className="text-xs text-content-muted font-medium">{booking.vehicleTypeName} · {booking.licensePlate}</span>
                  </div>
                </div>
                <div className="flex flex-col gap-2">
                  <button className="w-10 h-10 rounded-xl bg-surface-border hover:bg-surface-muted flex items-center justify-center text-content-muted hover:text-content-main transition-colors" 
                    title={booking.driverPhone}
                    onClick={() => window.open(`tel:${booking.driverPhone}`)}
                  >
                    <RiPhoneLine size={18} />
                  </button>
                  <button
                    onClick={() => setChatOpen(true)}
                    className="w-10 h-10 rounded-xl bg-brand-500/15 border border-brand-500/30 hover:bg-brand-500/25 flex items-center justify-center text-brand-400 transition-colors shadow-sm"
                  >
                    <RiMessage2Line size={18} />
                  </button>
                </div>
              </div>
            </div>
          ) : (
            <div className="bg-surface rounded-2xl p-6 border border-surface-border text-center flex flex-col items-center justify-center min-h-[140px] shadow-sm">
              <Spinner className="mb-4" />
              <p className="text-sm font-medium text-content-main">Đang tìm tài xế phù hợp cho bạn...</p>
              <p className="text-xs text-content-muted mt-1">Vui lòng đợi trong giây lát</p>
            </div>
          )}

          {/* Route info */}
          <div className="bg-surface rounded-2xl p-5 border border-surface-border space-y-4 shadow-sm">
            <h3 className="font-semibold text-content-main text-sm">Hành trình & Cước phí</h3>
            <div className="space-y-4">
              <div className="flex items-start gap-3">
                <div className="w-8 h-8 rounded-full bg-brand-500/10 flex items-center justify-center shrink-0">
                  <RiMapPinLine size={18} className="text-brand-400" />
                </div>
                <div>
                  <p className="text-xs text-content-muted uppercase tracking-wider font-semibold">Điểm đón</p>
                  <p className="text-content-main text-sm font-medium mt-0.5">{booking.pickupLocation}</p>
                </div>
              </div>
              <div className="w-0.5 h-6 bg-surface-border ml-4" />
              <div className="flex items-start gap-3">
                <div className="w-8 h-8 rounded-full bg-red-500/10 flex items-center justify-center shrink-0">
                  <RiMapPin2Line size={18} className="text-red-400" />
                </div>
                <div>
                  <p className="text-xs text-content-muted uppercase tracking-wider font-semibold">Điểm đến</p>
                  <p className="text-content-main text-sm font-medium mt-0.5">{booking.dropoffLocation}</p>
                </div>
              </div>
            </div>
            
            <div className="border-t border-surface-border pt-4 mt-2 flex justify-between items-center">
              <span className="text-content-muted font-medium">Tổng thanh toán</span>
              <span className="font-display font-bold text-brand-400 text-xl">{formatCurrency(booking.totalPrice)}</span>
            </div>
          </div>

          {/* Actions */}
          <div className="space-y-3 pt-4">
            {booking.bookingStatus === BOOKING_STATUS.COMPLETED && (
              <Button fullWidth size="lg" onClick={handleRateDriver} className="shadow-lg shadow-brand-500/20">
                <RiStarLine size={18} className="mr-2" /> Đánh giá chuyến đi
              </Button>
            )}
            {[BOOKING_STATUS.PENDING, BOOKING_STATUS.ACCEPTED].includes(booking.bookingStatus) && (
              <Button variant="danger" fullWidth onClick={handleCancel} loading={cancelling}>
                Hủy chuyến đi
              </Button>
            )}
            <Button variant="ghost" fullWidth onClick={() => {
              if (booking.bookingStatus === BOOKING_STATUS.COMPLETED || booking.bookingStatus === BOOKING_STATUS.CANCELLED) {
                clearCurrentBooking()
              }
              navigate('/customer/home')
            }}>
              Về trang chủ
            </Button>
          </div>
        </div>
      </div>

      {/* Vùng 2: Bản đồ Realtime */}
      <div className="flex-1 relative h-[45vh] lg:h-full bg-surface-dark z-0">
        <InteractiveMap pickup={pickupCoord} dropoff={dropoffCoord} driver={driverCoord} />
        
        {/* Map Overlay Indicator */}
        <div className="absolute top-4 right-4 z-10 bg-surface-card/90 backdrop-blur-md px-4 py-2 rounded-full border border-surface-border shadow-lg flex items-center gap-2">
          <div className="w-2 h-2 bg-brand-500 rounded-full animate-pulse" />
          <span className="text-xs font-semibold text-content-main">Bản đồ trực tiếp</span>
        </div>
      </div>

      {/* Chat dialog */}
      {chatOpen && booking.driverId && (
        <div className="absolute inset-0 z-50">
          <ChatDialog
            bookingId={booking.bookingId}
            receiverId={booking.driverId}
            otherName={booking.driverName}
            onClose={() => setChatOpen(false)}
          />
        </div>
      )}
    </div>
  )
}

export default TripTrackingPage
