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
  [BOOKING_STATUS.PENDING]:    'text-amber-600 bg-amber-500/10 border-amber-500/20',
  [BOOKING_STATUS.ACCEPTED]:   'text-emerald-600 bg-emerald-500/10 border-emerald-500/20',
  [BOOKING_STATUS.ARRIVED]:    'text-blue-600 bg-blue-500/10 border-blue-500/20',
  [BOOKING_STATUS.IN_PROGRESS]:'text-brand-600 bg-brand-500/10 border-brand-500/20',
  [BOOKING_STATUS.COMPLETED]:  'text-slate-800 bg-slate-200 border-slate-300 dark:bg-white/10 dark:text-white',
  [BOOKING_STATUS.CANCELLED]:  'text-red-600 bg-red-500/10 border-red-500/20',
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
    if (topic === `/topic/booking/${bookingId}/driver-location`) {
      if (payload?.lat && payload?.lng) {
        setDriverCoord({
          lat: payload.lat,
          lng: payload.lng,
          name: booking?.driverName || 'Tài xế'
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
    <div className="flex items-center justify-center h-full min-h-[60vh] bg-[#e8ece3] dark:bg-surface-dark">
      <div className="text-center space-y-4">
        <Spinner size="xl" />
        <p className="font-bold text-content-main">Đang tải thông tin chuyến đi...</p>
      </div>
    </div>
  )

  if (!booking) return null

  return (
    <div className="h-full flex flex-col lg:flex-row bg-[#e8ece3] dark:bg-surface-dark overflow-hidden pointer-events-auto">
      {/* Vùng 1: Thông tin chuyến đi */}
      <div className="w-full lg:w-[420px] flex flex-col h-[55vh] lg:h-full bg-surface-card border-b lg:border-b-0 lg:border-r border-[#cdd4c8] dark:border-surface-border z-10 shadow-[8px_0_30px_rgba(0,0,0,0.04)] shrink-0">
        
        {/* Header & Status (Sticky) */}
        <div className="p-6 border-b border-[#cdd4c8] dark:border-surface-border bg-surface-card sticky top-0 z-20">
          <div className="flex items-start justify-between mb-6">
            <div>
              <p className="text-[10px] font-bold text-content-muted uppercase tracking-wider mb-1">Mã chuyến đi</p>
              <h1 className="font-mono font-bold text-content-main text-xl tracking-tight">#{booking.bookingId?.slice(-8)}</h1>
            </div>
            <span className={cn('flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-bold uppercase tracking-wider border', STATUS_COLOR[booking.bookingStatus] || STATUS_COLOR[BOOKING_STATUS.PENDING])}>
              <span className="w-1.5 h-1.5 rounded-full bg-current animate-pulse" />
              {BOOKING_STATUS_LABEL[booking.bookingStatus] || booking.bookingStatus}
            </span>
          </div>

          {/* Sleek Progress Bar */}
          {booking.bookingStatus !== BOOKING_STATUS.CANCELLED && (
            <div className="relative mt-2">
              <div className="absolute top-1/2 left-0 w-full h-1 bg-surface-muted -translate-y-1/2 rounded-full" />
              <div 
                className="absolute top-1/2 left-0 h-1 bg-slate-950 dark:bg-white -translate-y-1/2 rounded-full transition-all duration-700 ease-out" 
                style={{ width: `${(Math.max(0, stepIndex) / (STATUS_STEPS.length - 2)) * 100}%` }}
              />
              <div className="relative flex justify-between">
                {STATUS_STEPS.slice(0, -1).map((s, i) => {
                  const isCompleted = i <= stepIndex;
                  const isCurrent = i === stepIndex;
                  return (
                    <div 
                      key={s} 
                      className={cn(
                        "w-3.5 h-3.5 rounded-full border-[3px] bg-surface-card transition-all duration-500 z-10 flex items-center justify-center",
                        isCompleted ? "border-slate-950 dark:border-white" : "border-surface-muted",
                        isCurrent && "shadow-[0_0_0_4px_rgba(15,23,42,0.1)] dark:shadow-[0_0_0_4px_rgba(255,255,255,0.1)] scale-125"
                      )}
                    />
                  )
                })}
              </div>
            </div>
          )}
        </div>

        {/* Scrollable Info Area */}
        <div className="p-6 overflow-y-auto no-scrollbar space-y-6 flex-1">
          {/* Driver info */}
          {booking.driverId ? (
            <div className="rounded-2xl border border-[#cdd4c8] dark:border-surface-border bg-surface-card p-5 shadow-sm">
              <h3 className="text-[10px] font-bold text-content-muted uppercase tracking-wider mb-4">Tài xế của bạn</h3>
              <div className="flex items-center gap-4">
                <div className="w-16 h-16 rounded-2xl bg-slate-100 dark:bg-slate-800 flex items-center justify-center text-3xl font-bold text-slate-400 overflow-hidden shrink-0 shadow-sm border border-surface-border">
                  {booking.driverName?.[0] || <RiUserStarLine />}
                </div>
                <div className="flex-1">
                  <h3 className="font-display font-bold text-content-main text-lg">{booking.driverName}</h3>
                  <div className="flex items-center gap-1.5 mt-0.5">
                    <RiStarLine size={14} className="text-amber-500" />
                    <span className="text-sm font-bold text-content-main">5.0</span>
                  </div>
                  <div className="inline-flex items-center gap-1.5 mt-2 bg-surface-muted px-2.5 py-1 rounded-lg border border-surface-border">
                    <RiCarLine size={13} className="text-content-muted" />
                    <span className="font-mono text-xs font-bold text-content-main tracking-wider">
                      {booking.vehicleTypeName} · {booking.licensePlate}
                    </span>
                  </div>
                </div>
                <div className="flex flex-col gap-2">
                  <button className="w-10 h-10 rounded-xl bg-slate-950 dark:bg-white flex items-center justify-center text-white dark:text-slate-950 hover:scale-105 transition-transform shadow-md" 
                    onClick={() => window.open(`tel:${booking.driverPhone}`)}
                  >
                    <RiPhoneLine size={16} />
                  </button>
                  <button
                    onClick={() => setChatOpen(true)}
                    className="w-10 h-10 rounded-xl bg-lime-accent flex items-center justify-center text-slate-950 hover:bg-[#b8ff59] hover:scale-105 transition-transform shadow-md"
                  >
                    <RiMessage2Line size={16} />
                  </button>
                </div>
              </div>
            </div>
          ) : (
            <div className="rounded-2xl border border-[#cdd4c8] dark:border-surface-border bg-[#f8faf6] dark:bg-surface-dark p-8 text-center flex flex-col items-center justify-center relative overflow-hidden min-h-[160px]">
               {/* Animated radar effect for finding driver */}
               <div className="absolute inset-0 flex items-center justify-center opacity-30 pointer-events-none">
                 <div className="w-40 h-40 border-2 border-brand-500 rounded-full animate-ping" />
               </div>
               <Spinner className="mb-4 text-brand-500 relative z-10" size="lg" />
               <h3 className="font-display font-bold text-lg text-content-main relative z-10">Đang tìm tài xế...</h3>
               <p className="text-sm text-content-muted mt-1 relative z-10">Hệ thống đang quét các tài xế gần bạn nhất.</p>
            </div>
          )}

          {/* Route info */}
          <div className="rounded-2xl border border-[#cdd4c8] dark:border-surface-border bg-surface-card p-5 shadow-sm">
            <h3 className="text-[10px] font-bold text-content-muted uppercase tracking-wider mb-4">Hành trình</h3>
            <div className="relative pl-6 space-y-6">
              <div className="absolute left-1.5 top-2 bottom-2 w-0.5 bg-surface-border rounded-full" />
              
              <div className="relative">
                <div className="absolute -left-[23px] top-1 w-3.5 h-3.5 rounded-full border-[3px] border-surface-card bg-slate-950 dark:bg-white shadow-sm" />
                <p className="text-[10px] text-content-muted uppercase font-bold tracking-wider mb-0.5">Điểm đón</p>
                <p className="text-content-main text-sm font-bold leading-tight">{booking.pickupLocation}</p>
              </div>

              <div className="relative">
                <div className="absolute -left-[23px] top-1 w-3.5 h-3.5 rounded-full border-[3px] border-surface-card bg-brand-500 shadow-sm" />
                <p className="text-[10px] text-content-muted uppercase font-bold tracking-wider mb-0.5">Điểm đến</p>
                <p className="text-content-main text-sm font-bold leading-tight">{booking.dropoffLocation}</p>
              </div>
            </div>
            
            <div className="border-t border-[#cdd4c8] dark:border-surface-border pt-4 mt-6 flex justify-between items-end">
              <span className="text-[10px] font-bold text-content-muted uppercase tracking-wider">Tổng thanh toán</span>
              <span className="font-display font-bold text-2xl tracking-tight text-slate-950 dark:text-white">{formatCurrency(booking.totalPrice)}</span>
            </div>
          </div>

          {/* Actions */}
          <div className="space-y-3 pt-2">
            {booking.bookingStatus === BOOKING_STATUS.COMPLETED && (
              <Button fullWidth className="h-12 rounded-xl bg-slate-950 dark:bg-white text-white dark:text-slate-950 font-bold" onClick={handleRateDriver}>
                Đánh giá chuyến đi
              </Button>
            )}
            {[BOOKING_STATUS.PENDING, BOOKING_STATUS.ACCEPTED].includes(booking.bookingStatus) && (
              <Button fullWidth className="h-12 rounded-xl bg-red-50 dark:bg-red-500/10 hover:bg-red-100 dark:hover:bg-red-500/20 text-red-600 font-bold border-none" onClick={handleCancel} loading={cancelling}>
                Hủy chuyến đi
              </Button>
            )}
            <Button variant="outline" fullWidth className="h-12 rounded-xl border-[#cdd4c8] dark:border-surface-border font-bold text-content-main hover:border-slate-400" onClick={() => {
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
      <div className="flex-1 relative h-[45vh] lg:h-full bg-[#e8ece3] dark:bg-surface-dark z-0">
        <InteractiveMap pickup={pickupCoord} dropoff={dropoffCoord} driver={driverCoord} />
        
        {/* Map Overlay Indicator */}
        <div className="absolute top-4 right-4 z-10 bg-surface-card/95 backdrop-blur-md px-4 py-2 rounded-xl border border-surface-border shadow-md flex items-center gap-2">
          <div className="w-2 h-2 bg-brand-500 rounded-full animate-pulse" />
          <span className="text-xs font-bold text-content-main">Trực tiếp</span>
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
