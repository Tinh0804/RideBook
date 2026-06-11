import { useState, useEffect, useCallback, useRef } from 'react'
import { useNavigate } from 'react-router-dom'
import toast from 'react-hot-toast'
import {
  RiMapPinLine, RiMapPin2Line, RiMessage2Line,
  RiUserLine, RiPhoneLine, RiCheckLine, RiCarLine, RiCloseLine
} from 'react-icons/ri'
import { bookingApi } from '@/features/booking/api/bookingApi'
import { useDriverStore, useAuthStore } from '@/store/rootStore'
import { useWebSocket } from '@/hooks/useWebSocket'
import { BOOKING_STATUS, BOOKING_STATUS_LABEL } from '@/config'
import { formatCurrency, formatDistance } from '@/utils/currency'
import Button from '@/components/Elements/Button'
import Spinner from '@/components/Elements/Spinner'
import ChatDialog from '@/features/chat/components/ChatDialog'
import Modal from '@/components/Elements/Modal'
import { cn } from '@/utils/cn'
import axios from 'axios'
import InteractiveMap from '@/components/Map/InteractiveMap'


const STATUS_FLOW = [
  { status: BOOKING_STATUS.ACCEPTED,    label: 'Đến đón khách',    next: BOOKING_STATUS.ARRIVED,     action: 'Tôi đã đến điểm đón' },
  { status: BOOKING_STATUS.ARRIVED,     label: 'Đã đến điểm đón',  next: BOOKING_STATUS.IN_PROGRESS, action: 'Đã đón khách, bắt đầu chạy' },
  { status: BOOKING_STATUS.IN_PROGRESS, label: 'Đang trên đường',  next: BOOKING_STATUS.COMPLETED,   action: 'Hoàn thành chuyến đi' },
]

const DriverTripFlowPage = () => {
  const navigate = useNavigate()
  const { user, userProfile } = useAuthStore()
  const { isOnline, currentTrip, setCurrentTrip, clearCurrentTrip } = useDriverStore()

  // State for Waiting phase
  const [incomingTrip, setIncomingTrip] = useState(null)
  const [accepting, setAccepting] = useState(false)
  
  // State for Active Trip phase
  const [loadingTrip, setLoadingTrip] = useState(false)
  const [updating, setUpdating] = useState(false)
  const [cancelingTrip, setCancelingTrip] = useState(false)
  const [chatOpen, setChatOpen] = useState(false)

  // Coords states for active trip — start null, always geocode from trip data
  const [pickupCoord, setPickupCoord] = useState(null)
  const [dropoffCoord, setDropoffCoord] = useState(null)
  const [driverCoord, setDriverCoord] = useState(null)

  // Ref to hold sendMessage from the active-trip WebSocket hook (avoids hook-ordering issues)
  const sendTripMessageRef = useRef(null)

  // Reset coords whenever the trip changes so geocoding always re-runs for new trip
  useEffect(() => {
    setPickupCoord(null)
    setDropoffCoord(null)
  }, [currentTrip?.bookingId])

  // Set coords directly from backend data (no Nominatim geocoding needed anymore)
  useEffect(() => {
    if (currentTrip) {
      if (currentTrip.pickupLat && currentTrip.pickupLng) {
        setPickupCoord({
          lat: currentTrip.pickupLat,
          lng: currentTrip.pickupLng,
          name: currentTrip.pickupLocation
        })
      }
      if (currentTrip.dropoffLat && currentTrip.dropoffLng) {
        setDropoffCoord({
          lat: currentTrip.dropoffLat,
          lng: currentTrip.dropoffLng,
          name: currentTrip.dropoffLocation
        })
      }
    }
  }, [currentTrip])

  // Live location tracking (No simulation, pure real-time GPS)
  useEffect(() => {
    if (!currentTrip) return
    const status = currentTrip.bookingStatus

    if (![BOOKING_STATUS.ACCEPTED, BOOKING_STATUS.ARRIVED, BOOKING_STATUS.IN_PROGRESS].includes(status)) {
      setDriverCoord(null)
      return
    }

    let watchId = null

    if (navigator.geolocation) {
      watchId = navigator.geolocation.watchPosition(
        (position) => {
          const lat = position.coords.latitude
          const lng = position.coords.longitude
          
          setDriverCoord({
            lat,
            lng,
            name: 'Vị trí của bạn'
          })

          // Also keep localStorage as same-device fallback
          localStorage.setItem(`driver_live_loc_${currentTrip.bookingId}`, JSON.stringify({ lat, lng }))

          // Broadcast GPS via WebSocket to customer in real-time
          sendTripMessageRef.current?.('/app/driver/location', {
            bookingId: currentTrip.bookingId,
            lat,
            lng,
          })
        },
        (error) => {
          console.error('Lỗi lấy toạ độ GPS tài xế:', error)
        },
        { enableHighAccuracy: true, maximumAge: 0, timeout: 5000 }
      )
    }

    return () => {
      if (watchId) navigator.geolocation.clearWatch(watchId)
    }
  }, [currentTrip?.bookingStatus, currentTrip?.bookingId])

  // 1. Fetch current trip if any (on mount)
  useEffect(() => {
    if (currentTrip?.bookingId) {
      // Update URL silently so user sees the ID in address bar
      window.history.replaceState(null, '', `/driver/trips/${currentTrip.bookingId}`)
      
      // Refresh current trip info
      setLoadingTrip(true)
      bookingApi.getById(currentTrip.bookingId)
        .then((b) => { setCurrentTrip(b) })
        .catch(() => toast.error('Không tìm thấy thông tin chuyến đi'))
        .finally(() => setLoadingTrip(false))
    } else {
      window.history.replaceState(null, '', `/driver/trips`)
    }
  }, [currentTrip?.bookingId, setCurrentTrip])

  // 2. WebSocket for Incoming Trips & Driver Specific Messages
  const onWsAvailableMessage = useCallback((topic, payload) => {
    // Handle plain string payload
    if (typeof payload === 'string') {
      if (payload.startsWith('CUSTOMER_CANCELLED:')) {
        const bookingId = payload.split(':')[1]
        if (currentTrip?.bookingId === bookingId) {
          toast.error('Khách hàng đã hủy chuyến đi này!', { duration: 5000 })
          setCurrentTrip(null)
        } else if (incomingTrip?.bookingId === bookingId) {
          toast.error('Khách hàng đã hủy chuyến đi này!', { duration: 5000 })
          setIncomingTrip(null)
        }
        return
      }

      // Only process new rides if we are online and don't have an active trip
      if (!isOnline || currentTrip) return

      if (payload.startsWith('NEW_RIDE:')) {
        const bookingId = payload.split(':')[1]
        bookingApi.getById(bookingId)
          .then(b => setIncomingTrip(b))
          .catch(() => toast.error('Lỗi khi tải thông tin cuốc xe mới'))
        return
      }
    }

    if (payload?.type === 'NEW_BOOKING' && payload?.booking) {
      setIncomingTrip(payload.booking)
      // Optional: play sound here
    } else if (payload?.type === 'BOOKING_TAKEN' || payload?.type === 'BOOKING_CANCELLED') {
      if (incomingTrip?.bookingId === payload.bookingId) {
        setIncomingTrip(null)
        if (payload?.type === 'BOOKING_CANCELLED') {
          toast.error('Chuyến đi đã bị khách hủy')
        } else {
          toast.error('Chuyến đã có tài xế khác nhận')
        }
      }
    }
  }, [isOnline, currentTrip, incomingTrip])

  // Listen to global available bookings and personal driver topic
  const driverId = userProfile?.driverId || user?.id
  const topicsToListen = driverId 
    ? ['/topic/available-bookings', `/topic/driver/${driverId}`]
    : ['/topic/available-bookings']

  useWebSocket(topicsToListen, onWsAvailableMessage)

  // 3. WebSocket for Active Trip (when processing)
  const onWsTripMessage = useCallback((_, payload) => {
    if (payload?.bookingId === currentTrip?.bookingId) {
      setCurrentTrip({ ...currentTrip, ...payload })
    }
  }, [currentTrip, setCurrentTrip])

  // Listen to specific booking updates — sync sendMessage into ref for GPS effect
  const { sendMessage: sendTripMessage } = useWebSocket(
    currentTrip?.bookingId ? [`/topic/booking/${currentTrip.bookingId}`] : [],
    onWsTripMessage
  )
  // Keep ref up-to-date so GPS watchPosition callback can always call the latest sendMessage
  sendTripMessageRef.current = sendTripMessage

  // --- Handlers for Waiting Phase ---
  const handleAccept = async () => {
    if (!incomingTrip || !driverId) return
    setAccepting(true)
    try {
      const updatedTrip = await bookingApi.assignDriver(incomingTrip.bookingId, driverId)
      toast.success('Nhận chuyến thành công!')
      setIncomingTrip(null)
      setCurrentTrip(updatedTrip)
    } catch (err) {
      toast.error(err?.response?.data?.message || 'Chuyến đã có người nhận hoặc bị lỗi')
      setIncomingTrip(null)
    } finally {
      setAccepting(false)
    }
  }

  const handleReject = async () => {
    if (!incomingTrip || !driverId) return
    try {
      await bookingApi.rejectBooking(incomingTrip.bookingId, driverId)
    } catch (err) {
      console.error('Lỗi khi từ chối chuyến', err)
    }
    setIncomingTrip(null)
  }

  // --- Handlers for Active Trip Phase ---
  const handleNextStatus = async () => {
    const currentStep = STATUS_FLOW.find((s) => s.status === currentTrip?.bookingStatus)
    if (!currentStep || !currentTrip) return
    setUpdating(true)
    try {
      if (currentStep.next === BOOKING_STATUS.COMPLETED) {
        await bookingApi.completeBooking(currentTrip.bookingId)
        toast.success('Chuyến đi hoàn thành!')
        clearCurrentTrip()
        navigate('/driver/dashboard')
      } else {
        const updated = await bookingApi.updateStatus(currentTrip.bookingId, currentStep.next)
        setCurrentTrip(updated)
      }
    } catch (err) {
      toast.error(err?.response?.data?.message || 'Cập nhật trạng thái thất bại')
    } finally {
      setUpdating(false)
    }
  }

  const handleCancelTrip = async () => {
    if (!window.confirm('Bạn có chắc chắn muốn huỷ chuyến đi này không?')) return;
    
    setCancelingTrip(true);
    try {
      await bookingApi.cancelBookingByDriver(currentTrip.bookingId, driverId);
      toast.success('Đã huỷ chuyến thành công');
      clearCurrentTrip();
    } catch (err) {
      toast.error(err?.response?.data?.message || 'Lỗi khi huỷ chuyến');
    } finally {
      setCancelingTrip(false);
    }
  }

  // ==========================================
  // RENDER: OFFLINE
  // ==========================================
  if (!isOnline) return (
    <div className="flex flex-col items-center justify-center py-24 space-y-4">
      <div className="w-20 h-20 rounded-full bg-surface-border flex items-center justify-center">
        <RiCarLine size={36} className="text-gray-600" />
      </div>
      <h2 className="font-display text-xl font-bold text-content-main">Bạn đang ngoại tuyến</h2>
      <p className="text-content-muted text-center max-w-xs">
        Bật trạng thái hoạt động trên trang Dashboard để nhận chuyến
      </p>
      <Button variant="outline" onClick={() => navigate('/driver/dashboard')}>
        Về Dashboard
      </Button>
    </div>
  )

  // ==========================================
  // RENDER: WAITING FOR TRIP
  // ==========================================
  if (!currentTrip) {
    return (
      <div className="space-y-4 h-[calc(100vh-100px)] flex flex-col animate-fade-in">
        {/* Header */}
        <div className="shrink-0 flex items-center justify-between">
          <div>
            <h1 className="section-title">Chờ nhận chuyến</h1>
            <div className="flex items-center gap-2 text-xs text-brand-400 mt-1">
              <span className="w-2 h-2 rounded-full bg-brand-500 animate-pulse" />
              Hệ thống đang tự động quét chuyến...
            </div>
          </div>
        </div>

        {/* Mockup Map */}
        <div className="flex-1 rounded-2xl overflow-hidden relative border border-brand-500/20 bg-[#0f172a]">
          {/* Grid background representing map */}
          <div className="absolute inset-0 opacity-20 pointer-events-none" style={{
            backgroundImage: `linear-gradient(#334155 1px, transparent 1px), linear-gradient(90deg, #334155 1px, transparent 1px)`,
            backgroundSize: '40px 40px'
          }} />
          
          {/* Radar effect */}
          <div className="absolute inset-0 flex items-center justify-center pointer-events-none">
            <div className="w-[300px] h-[300px] rounded-full border border-brand-500/30 animate-ping absolute opacity-20" />
            <div className="w-[200px] h-[200px] rounded-full border border-brand-500/50 animate-ping absolute opacity-40 delay-300" />
            <div className="w-[100px] h-[100px] rounded-full border border-brand-500/80 animate-ping absolute opacity-60 delay-700" />
            
            {/* Driver Marker */}
            <div className="relative z-10 w-12 h-12 rounded-full bg-brand-500/20 border-2 border-brand-400 flex items-center justify-center shadow-[0_0_30px_rgba(34,197,94,0.4)]">
              <RiCarLine size={24} className="text-content-main" />
            </div>
          </div>
          
          {/* Status Overlay */}
          <div className="absolute top-4 left-4 bg-surface-dark/90 backdrop-blur-md px-4 py-2 rounded-xl border border-surface-border flex items-center gap-3 shadow-lg">
            <Spinner size="sm" className="text-brand-400" />
            <span className="text-sm font-medium text-content-main">Đang tìm chuyến quanh bạn...</span>
          </div>
        </div>

        {/* Incoming Trip Popup Modal */}
        <Modal isOpen={!!incomingTrip} onClose={handleReject} title="Chuyến đi mới!" size="sm" closeOnOverlayClick={false}>
          {incomingTrip && (
            <div className="space-y-5 animate-slide-up">
              <div className="flex items-start justify-between gap-3">
                <div className="space-y-1">
                  <div className="flex items-center gap-2">
                    <span className="font-display text-2xl font-bold text-brand-400">
                      {formatCurrency(incomingTrip.totalPrice || incomingTrip.price)}
                    </span>
                    {incomingTrip.originalPrice && incomingTrip.originalPrice > (incomingTrip.totalPrice || incomingTrip.price) && (
                      <span className="text-sm text-content-muted line-through font-medium">
                        {formatCurrency(incomingTrip.originalPrice)}
                      </span>
                    )}
                    {incomingTrip.paymentMethod && (
                      <span className={cn(
                        'badge text-[10px] ml-1',
                        incomingTrip.paymentMethod === 'CASH' ? 'badge-gray' : 'badge-blue',
                      )}>
                        {incomingTrip.paymentMethod === 'CASH' ? 'Tiền mặt' : 'Online'}
                      </span>
                    )}
                  </div>
                  <div className="text-xs text-content-muted mt-1">
                    Khoảng cách: <span className="font-semibold text-content-main">{formatDistance(incomingTrip.distance)}</span>
                  </div>
                </div>
                <div className="w-12 h-12 rounded-full border-4 border-brand-500/20 flex items-center justify-center shrink-0 shadow-glow-green">
                  <span className="text-lg animate-pulse text-brand-400 font-bold">15s</span>
                </div>
              </div>

              {/* Route */}
              <div className="bg-surface-dark rounded-xl p-3 space-y-2 border border-surface-border">
                <div className="flex items-start gap-2">
                  <RiMapPinLine size={16} className="text-brand-400 shrink-0 mt-0.5" />
                  <div>
                    <p className="text-[10px] text-gray-600 uppercase tracking-wider">Đón</p>
                    <p className="text-sm text-content-main">{incomingTrip.pickupLocation}</p>
                  </div>
                </div>
                <div className="flex items-start gap-2">
                  <RiMapPin2Line size={16} className="text-red-400 shrink-0 mt-0.5" />
                  <div>
                    <p className="text-[10px] text-gray-600 uppercase tracking-wider">Đến</p>
                    <p className="text-sm text-content-main">{incomingTrip.dropoffLocation}</p>
                  </div>
                </div>
              </div>

              {/* Actions */}
              <div className="flex gap-3 pt-2">
                <Button variant="outline" fullWidth onClick={handleReject} disabled={accepting} className="border-red-500/30 text-red-400 hover:bg-red-500/10 hover:border-red-500">
                  <RiCloseLine size={18} /> Bỏ qua
                </Button>
                <Button fullWidth onClick={handleAccept} loading={accepting} className="bg-brand-500 hover:bg-brand-400 shadow-[0_0_20px_rgba(34,197,94,0.3)] text-content-main">
                  <RiCheckLine size={18} /> Nhận chuyến
                </Button>
              </div>
            </div>
          )}
        </Modal>
      </div>
    )
  }

  // ==========================================
  // RENDER: ACTIVE TRIP
  // ==========================================
  if (loadingTrip) return <div className="flex justify-center py-16"><Spinner size="xl" /></div>

  const stepIndex = STATUS_FLOW.findIndex(s => s.status === currentTrip.bookingStatus)
  const currentStep = STATUS_FLOW.find((s) => s.status === currentTrip.bookingStatus)

  return (
    <div className="-m-6 h-[calc(100vh-64px)] flex flex-col lg:flex-row bg-surface-dark overflow-hidden animate-fade-in">
      {/* Vùng 1: Thông tin chuyến đi */}
      <div className="w-full lg:w-[450px] flex flex-col h-[55vh] lg:h-full bg-surface-card border-b lg:border-b-0 lg:border-r border-surface-border z-10 shadow-2xl shrink-0 overflow-y-auto no-scrollbar">
        <div className="p-5 border-b border-surface-border bg-surface-card/95 backdrop-blur-md sticky top-0 z-20">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h1 className="font-display font-bold text-content-main text-xl">Chuyến đi #{currentTrip.bookingId?.slice(-8)}</h1>
              <p className="text-xs text-content-muted mt-1">Đang xử lý chuyến đi</p>
            </div>
            <span className="badge border text-xs px-3 py-1.5 shadow-sm text-brand-400 bg-brand-400/10 border-brand-400/20">
              <span className="w-2 h-2 rounded-full bg-current animate-pulse mr-2 inline-block" />
              {BOOKING_STATUS_LABEL[currentTrip.bookingStatus] || currentTrip.bookingStatus}
            </span>
          </div>

          {/* Progress steps */}
          <div className="flex items-center justify-between mt-2">
            {STATUS_FLOW.map((s, i) => (
              <div key={s.status} className="flex items-center flex-1">
                <div className={cn(
                  'w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold shrink-0 transition-all duration-500',
                  i <= stepIndex ? 'bg-brand-500 text-content-main shadow-glow-green' : 'bg-surface-border text-gray-500',
                )}>
                  {i < stepIndex ? <RiCheckLine size={14} /> : i + 1}
                </div>
                {i < STATUS_FLOW.length - 1 && (
                  <div className={cn('flex-1 h-0.5 mx-1 transition-all duration-700', i < stepIndex ? 'bg-brand-500' : 'bg-surface-border')} />
                )}
              </div>
            ))}
          </div>
        </div>

        {/* Scrollable Info Area */}
        <div className="p-5 space-y-6 flex-1">
          {/* Customer info */}
          {currentTrip.customerId && (
            <div className="bg-surface rounded-2xl p-5 border border-surface-border shadow-sm space-y-4">
              <h3 className="font-semibold text-content-main text-sm">Thông tin khách hàng</h3>
              <div className="flex items-center gap-4">
                <div className="w-12 h-12 rounded-full bg-blue-500/20 border border-blue-500/30 flex items-center justify-center text-xl font-bold text-blue-400 overflow-hidden shrink-0">
                  {currentTrip.customerName?.[0] || <RiUserLine size={24} />}
                </div>
                <div className="flex-1">
                  <p className="font-bold text-content-main text-base">{currentTrip.customerName}</p>
                  <p className="text-xs text-content-muted mt-0.5">{currentTrip.customerPhone}</p>
                </div>
                <div className="flex gap-2">
                  <button className="w-10 h-10 rounded-xl bg-surface-border hover:bg-surface-muted flex items-center justify-center text-content-muted hover:text-content-main transition-colors"
                    title={currentTrip.customerPhone}
                    onClick={() => window.open(`tel:${currentTrip.customerPhone}`)}
                  >
                    <RiPhoneLine size={18} />
                  </button>
                  <button
                    onClick={() => setChatOpen(true)}
                    className="w-10 h-10 rounded-xl bg-brand-500/15 border border-brand-500/30 flex items-center justify-center text-brand-400 hover:bg-brand-500/25 transition-colors shadow-glow-green"
                  >
                    <RiMessage2Line size={18} />
                  </button>
                </div>
              </div>
            </div>
          )}

          {/* Route info */}
          <div className="bg-surface rounded-2xl p-5 border border-surface-border space-y-4 shadow-sm">
            <h3 className="font-semibold text-content-main text-sm">Hành trình & Cước phí</h3>
            <div className="space-y-4">
              <div className="flex items-start gap-3">
                <div className="w-7 h-7 rounded-full bg-brand-500/10 flex items-center justify-center shrink-0">
                  <RiMapPinLine size={14} className="text-brand-400" />
                </div>
                <div>
                  <p className="text-xs text-content-muted uppercase tracking-wider font-semibold">Điểm đón</p>
                  <p className="text-content-main text-sm font-medium mt-0.5">{currentTrip.pickupLocation}</p>
                </div>
              </div>
              <div className="w-0.5 h-6 bg-surface-border ml-3.5" />
              <div className="flex items-start gap-3">
                <div className="w-7 h-7 rounded-full bg-red-500/10 flex items-center justify-center shrink-0">
                  <RiMapPin2Line size={14} className="text-red-400" />
                </div>
                <div>
                  <p className="text-xs text-content-muted uppercase tracking-wider font-semibold">Điểm đến</p>
                  <p className="text-content-main text-sm font-medium mt-0.5">{currentTrip.dropoffLocation}</p>
                </div>
              </div>
            </div>
            
            <div className="border-t border-surface-border pt-4 mt-2 flex justify-between items-center text-sm">
              <span className="text-content-muted font-medium">Cước phí</span>
              <div className="text-right flex items-center gap-2">
                {currentTrip.originalPrice && currentTrip.originalPrice > currentTrip.totalPrice && (
                  <span className="text-xs text-content-muted line-through">{formatCurrency(currentTrip.originalPrice)}</span>
                )}
                <span className="font-display font-bold text-brand-400 text-lg">{formatCurrency(currentTrip.totalPrice)}</span>
              </div>
            </div>
            <div className="flex justify-between text-sm pt-1">
              <span className="text-content-muted font-medium">Thanh toán</span>
              <span className="text-content-main font-medium">{currentTrip.paymentMethod === 'CASH' ? '💵 Tiền mặt' : '💳 Trực tuyến'}</span>
            </div>
          </div>

          {/* Actions */}
          <div className="space-y-3 pt-4">
            {currentStep && (
              <Button fullWidth size="lg" onClick={handleNextStatus} loading={updating} disabled={cancelingTrip}
                className={currentStep.next === BOOKING_STATUS.COMPLETED ? 'bg-blue-500 hover:bg-blue-400 shadow-[0_0_20px_rgba(59,130,246,0.3)] shadow-lg' : 'shadow-lg shadow-brand-500/20'}
              >
                {currentStep.action}
              </Button>
            )}
            
            {currentTrip && currentTrip.bookingStatus !== BOOKING_STATUS.IN_PROGRESS && (
              <Button fullWidth variant="outline" onClick={handleCancelTrip} loading={cancelingTrip} disabled={updating}
                className="border-red-500/30 text-red-400 hover:bg-red-500/10 hover:border-red-500"
              >
                <RiCloseLine size={18} className="mr-1" /> Hủy chuyến
              </Button>
            )}
          </div>
        </div>
      </div>

      {/* Vùng 2: Bản đồ Realtime */}
      <div className="flex-1 relative h-[45vh] lg:h-full bg-surface-dark z-0">
        <InteractiveMap pickup={pickupCoord} dropoff={dropoffCoord} driver={driverCoord} />
        
        {/* Map Overlay Indicator */}
        <div className="absolute top-4 right-4 z-10 bg-surface-card/90 backdrop-blur-md px-4 py-2 rounded-full border border-surface-border shadow-lg flex items-center gap-2">
          <div className="w-2 h-2 bg-brand-500 rounded-full animate-pulse" />
          <span className="text-xs font-semibold text-content-main">Đường đi trực tiếp</span>
        </div>
      </div>

      {/* Chat Dialog */}
      {chatOpen && (
        <div className="absolute inset-0 z-50">
          <ChatDialog
            bookingId={currentTrip.bookingId}
            receiverId={currentTrip.customerId}
            otherName={currentTrip.customerName}
            onClose={() => setChatOpen(false)}
          />
        </div>
      )}
    </div>
  )
}

export default DriverTripFlowPage