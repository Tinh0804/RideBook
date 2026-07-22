import { useState, useEffect, useCallback, useRef } from 'react'
import { useNavigate } from 'react-router-dom'
import toast from 'react-hot-toast'
import {
  RiMapPinLine, RiMapPin2Line, RiMessage2Line,
  RiUserLine, RiPhoneLine, RiCheckLine, RiCarLine, RiCloseLine,
  RiNavigationFill, RiTimeLine
} from 'react-icons/ri'
import { bookingApi } from '@/features/booking/api/bookingApi'
import { driverApi } from '@/features/driver/api/driverApi'
import { useDriverStore, useAuthStore } from '@/store/rootStore'
import { useWebSocket } from '@/hooks/useWebSocket'
import { BOOKING_STATUS, BOOKING_STATUS_LABEL } from '@/config'
import { formatCurrency, formatDistance } from '@/utils/currency'
import Button from '@/components/Elements/Button'
import Spinner from '@/components/Elements/Spinner'
import ChatDialog from '@/features/chat/components/ChatDialog'
import Modal from '@/components/Elements/Modal'
import { cn } from '@/utils/cn'
import InteractiveMap from '@/components/Map/InteractiveMap'


const STATUS_FLOW = [
  { status: BOOKING_STATUS.ACCEPTED,    label: 'Đến đón khách',    next: BOOKING_STATUS.ARRIVED,     action: 'Tôi đã đến điểm đón' },
  { status: BOOKING_STATUS.ARRIVED,     label: 'Đã đến điểm đón',  next: BOOKING_STATUS.IN_PROGRESS, action: 'Đã đón khách, bắt đầu chạy' },
  { status: BOOKING_STATUS.IN_PROGRESS, label: 'Đang trên đường',  next: BOOKING_STATUS.COMPLETED,   action: 'Hoàn thành chuyến đi' },
]

const playSound = (audio) => {
  if (!audio) return
  audio.currentTime = 0
  audio.play().catch(() => {})
}

const DriverTripFlowPage = () => {
  const navigate = useNavigate()
  const { user, userProfile, updateUserProfile } = useAuthStore()
  const { isOnline, setOnline, currentTrip, setCurrentTrip, clearCurrentTrip } = useDriverStore()

  // State for Waiting phase
  const [incomingTrip, setIncomingTrip] = useState(null)
  const [accepting, setAccepting] = useState(false)
  const [togglingOnline, setTogglingOnline] = useState(false)
  
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
  const receiveBookingSoundRef = useRef(null)
  const statusBookingSoundRef = useRef(null)

  useEffect(() => {
    receiveBookingSoundRef.current = new Audio('/sounds/recivebooking.mp3')
    statusBookingSoundRef.current = new Audio('/sounds/statusbooking.mp3')
    receiveBookingSoundRef.current.preload = 'auto'
    statusBookingSoundRef.current.preload = 'auto'

    return () => {
      receiveBookingSoundRef.current?.pause()
      statusBookingSoundRef.current?.pause()
    }
  }, [])

  useEffect(() => {
    if (incomingTrip?.bookingId) playSound(receiveBookingSoundRef.current)
  }, [incomingTrip?.bookingId])

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
    // Only track if online or on an active trip
    if (!isOnline && !currentTrip) return;

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
          if (currentTrip) {
            localStorage.setItem(`driver_live_loc_${currentTrip.bookingId}`, JSON.stringify({ lat, lng }))
            
            if ([BOOKING_STATUS.ACCEPTED, BOOKING_STATUS.ARRIVED, BOOKING_STATUS.IN_PROGRESS].includes(currentTrip.bookingStatus)) {
               // Broadcast GPS via WebSocket to customer in real-time
               sendTripMessageRef.current?.('/app/driver/location', {
                 bookingId: currentTrip.bookingId,
                 lat,
                 lng,
               })
            }
          }
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
  }, [currentTrip, isOnline])

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
  const driverId = userProfile?.driverId || userProfile?.id || user?.id

  // Auto-sync profile online status & active trip on mount
  useEffect(() => {
    if (userProfile?.activityStatus !== undefined && userProfile?.activityStatus !== null) {
      setOnline(userProfile.activityStatus)
    }
    const activeDriverId = userProfile?.driverId || userProfile?.id
    if (activeDriverId) {
      bookingApi.getActiveByDriver(activeDriverId)
        .then((activeTrip) => {
          if (activeTrip) {
            setCurrentTrip(activeTrip)
            setOnline(true)
          }
        })
        .catch(() => {})
    }
  }, [userProfile?.driverId, userProfile?.id, userProfile?.activityStatus, setCurrentTrip, setOnline])

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

  const handleToggleOnline = async () => {
    setTogglingOnline(true)
    try {
      const data = await driverApi.toggleStatus()
      const newStatus = typeof data?.result === 'boolean' ? data.result : !isOnline
      setOnline(newStatus)
      if (updateUserProfile) {
        updateUserProfile({ activityStatus: newStatus })
      }
      toast.success(newStatus ? 'Đã bật trạng thái trực tuyến' : 'Đã tắt trạng thái trực tuyến')
    } catch (err) {
      toast.error('Lỗi khi thay đổi trạng thái')
    } finally {
      setTogglingOnline(false)
    }
  }

  // --- Handlers for Waiting Phase ---
  const handleAccept = async () => {
    const activeDriverId = userProfile?.driverId || userProfile?.id || driverId
    if (!incomingTrip || !activeDriverId) return
    receiveBookingSoundRef.current.pause()
    receiveBookingSoundRef.current.currentTime = 0
    setAccepting(true)
    try {
      const updatedTrip = await bookingApi.assignDriver(incomingTrip.bookingId, activeDriverId)
      toast.success('Nhận chuyến thành công!')
      playSound(statusBookingSoundRef.current)
      setIncomingTrip(null)
      setCurrentTrip(updatedTrip)
      setOnline(true)
    } catch (err) {
      toast.error(err?.response?.data?.message || 'Chuyến đã có người nhận hoặc bị lỗi')
      setIncomingTrip(null)
    } finally {
      setAccepting(false)
    }
  }

  const handleReject = async () => {
    const activeDriverId = userProfile?.driverId || userProfile?.id || driverId
    if (!incomingTrip || !activeDriverId) return
    try {
      await bookingApi.rejectBooking(incomingTrip.bookingId, activeDriverId)
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
        playSound(statusBookingSoundRef.current)
        clearCurrentTrip()
        navigate('/driver/dashboard')
      } else {
        const updated = await bookingApi.updateStatus(currentTrip.bookingId, currentStep.next)
        playSound(statusBookingSoundRef.current)
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
  // RENDER: OFFLINE (Only if no active trip AND driver is offline)
  // ==========================================
  if (!isOnline && !currentTrip) return (
    <div className="h-full flex flex-col items-center justify-center relative overflow-hidden bg-[#e8ece3] dark:bg-surface-dark w-full">
      <img src="/assets/images/map_bg.jpg" alt="Map" className="absolute inset-0 w-full h-full object-cover opacity-60 dark:opacity-20 pointer-events-none" />
      <div className="absolute inset-0 bg-white/40 dark:bg-surface-dark/40 backdrop-blur-[2px] pointer-events-none" />
      
      <div className="relative z-10 bg-white/95 dark:bg-surface-card/95 backdrop-blur-md p-8 md:p-10 rounded-3xl shadow-2xl flex flex-col items-center max-w-sm w-11/12 text-center border border-gray-200 dark:border-surface-border">
        <div className="w-24 h-24 rounded-full bg-gray-100 dark:bg-surface-dark flex items-center justify-center mb-6 shadow-inner">
          <RiCarLine size={48} className="text-gray-400" />
        </div>
        <h2 className="font-display text-2xl font-bold text-gray-900 dark:text-white mb-3">Đang ngoại tuyến</h2>
        <p className="text-gray-500 dark:text-gray-400 mb-8 font-medium">
          Vui lòng bật trạng thái hoạt động để bắt đầu nhận cuốc và kiếm thêm thu nhập.
        </p>
        <div className="flex flex-col gap-3 w-full">
          <button
            onClick={handleToggleOnline}
            disabled={togglingOnline}
            className="w-full bg-brand-500 hover:bg-brand-400 text-white font-bold py-4 rounded-xl shadow-[0_0_20px_rgba(34,197,94,0.3)] transition-all flex items-center justify-center gap-2"
          >
            {togglingOnline ? <Spinner size="sm" /> : <RiCheckLine size={20} />} Bật trực tuyến ngay
          </button>
          <button
            onClick={() => navigate('/driver/dashboard')}
            className="w-full bg-gray-100 dark:bg-surface-dark text-gray-700 dark:text-gray-300 font-bold py-3 rounded-xl transition-all"
          >
            Trở về Dashboard
          </button>
        </div>
      </div>
    </div>
  )

  // ==========================================
  // RENDER: WAITING FOR TRIP
  // ==========================================
  if (!currentTrip) {
    return (
      <div className="h-full flex flex-col relative bg-[#e8ece3] dark:bg-surface-dark w-full overflow-hidden">
        
        {/* Real Map or Background Map if real not available */}
        <div className="absolute inset-0 z-0">
          <InteractiveMap driver={driverCoord} zoom={15} />
          {/* Overlay to fade bottom for visual hierarchy */}
          <div className="absolute inset-x-0 bottom-0 h-32 bg-gradient-to-t from-white/90 to-transparent dark:from-surface-dark/90 pointer-events-none" />
        </div>

        {/* Top Floating Card */}
        <div className="absolute top-6 left-4 right-4 md:left-1/2 md:-translate-x-1/2 md:w-full md:max-w-lg z-10">
          <div className="bg-white/95 dark:bg-surface-card/95 backdrop-blur-md rounded-2xl shadow-xl border border-gray-100 dark:border-surface-border p-4 flex items-center justify-between animate-fade-in-down">
            <div className="flex items-center gap-4">
              <div className="w-12 h-12 rounded-full bg-brand-500/10 flex items-center justify-center relative shrink-0">
                <span className="absolute inset-0 rounded-full border-2 border-brand-500 animate-ping opacity-60"></span>
                <RiNavigationFill size={24} className="text-brand-500" />
              </div>
              <div>
                <h3 className="font-bold text-gray-900 dark:text-white text-base md:text-lg">Đang tìm chuyến xe...</h3>
                <p className="text-xs text-gray-500 dark:text-gray-400 font-medium">Hệ thống đang quét các cuốc xe quanh bạn</p>
              </div>
            </div>
          </div>
        </div>

        {/* Incoming Trip Popup Modal */}
        <Modal isOpen={!!incomingTrip} onClose={handleReject} title="🚀 Có chuyến mới!" size="sm" closeOnOverlayClick={false}>
          {incomingTrip && (
            <div className="space-y-6">
              <div className="flex items-start justify-between gap-3 p-4 bg-gray-50 dark:bg-surface-dark rounded-2xl border border-gray-100 dark:border-surface-border">
                <div className="space-y-1">
                  <div className="text-xs font-semibold uppercase tracking-wider text-gray-500">Thu nhập dự kiến</div>
                  <div className="flex items-center gap-2">
                    <span className="font-display text-3xl font-bold text-brand-500">
                      {formatCurrency(incomingTrip.totalPrice || incomingTrip.price)}
                    </span>
                  </div>
                  <div className="flex items-center gap-2 mt-2">
                    {incomingTrip.paymentMethod && (
                      <span className={cn(
                        'px-2 py-1 rounded-md text-[11px] font-bold',
                        incomingTrip.paymentMethod === 'CASH' ? 'bg-gray-200 text-gray-700 dark:bg-gray-700 dark:text-gray-300' : 'bg-blue-100 text-blue-700 dark:bg-blue-900/40 dark:text-blue-300',
                      )}>
                        {incomingTrip.paymentMethod === 'CASH' ? 'Tiền mặt' : 'Online'}
                      </span>
                    )}
                    <span className="text-xs text-gray-500 dark:text-gray-400 font-medium bg-gray-200 dark:bg-gray-800 px-2 py-1 rounded-md">
                      {formatDistance(incomingTrip.distance)}
                    </span>
                  </div>
                </div>
                <div className="w-16 h-16 rounded-full border-4 border-brand-500/20 flex items-center justify-center shrink-0 shadow-[0_0_15px_rgba(34,197,94,0.2)] bg-white dark:bg-surface-card">
                  <span className="text-xl animate-pulse text-brand-500 font-bold">15s</span>
                </div>
              </div>

              {/* Route */}
              <div className="relative">
                <div className="absolute left-6 top-6 bottom-6 w-0.5 bg-gray-200 dark:bg-surface-border"></div>
                <div className="space-y-6 relative z-10">
                  <div className="flex items-start gap-4">
                    <div className="w-12 h-12 rounded-full bg-brand-50 dark:bg-brand-500/10 border border-brand-100 dark:border-brand-500/20 flex items-center justify-center shrink-0">
                      <RiMapPinLine size={20} className="text-brand-500" />
                    </div>
                    <div className="pt-1">
                      <p className="text-xs text-gray-500 font-bold uppercase tracking-wider mb-1">Điểm đón</p>
                      <p className="text-sm font-semibold text-gray-900 dark:text-white leading-snug">{incomingTrip.pickupLocation}</p>
                    </div>
                  </div>
                  <div className="flex items-start gap-4">
                    <div className="w-12 h-12 rounded-full bg-red-50 dark:bg-red-500/10 border border-red-100 dark:border-red-500/20 flex items-center justify-center shrink-0">
                      <RiMapPin2Line size={20} className="text-red-500" />
                    </div>
                    <div className="pt-1">
                      <p className="text-xs text-gray-500 font-bold uppercase tracking-wider mb-1">Điểm đến</p>
                      <p className="text-sm font-semibold text-gray-900 dark:text-white leading-snug">{incomingTrip.dropoffLocation}</p>
                    </div>
                  </div>
                </div>
              </div>

              {/* Actions */}
              <div className="flex gap-3 pt-4 border-t border-gray-100 dark:border-surface-border">
                <button 
                  onClick={handleReject} 
                  disabled={accepting} 
                  className="flex-1 py-3.5 rounded-xl font-bold border-2 border-red-100 text-red-500 bg-red-50 hover:bg-red-100 dark:border-red-500/20 dark:bg-red-500/10 dark:hover:bg-red-500/20 transition-colors flex items-center justify-center gap-2"
                >
                  <RiCloseLine size={20} /> Bỏ qua
                </button>
                <button 
                  onClick={handleAccept} 
                  disabled={accepting} 
                  className="flex-[2] py-3.5 rounded-xl font-bold bg-brand-500 text-white shadow-[0_0_20px_rgba(34,197,94,0.4)] hover:bg-brand-400 transition-colors flex items-center justify-center gap-2 disabled:opacity-50"
                >
                  {accepting ? <Spinner size="sm" /> : <RiCheckLine size={20} />} Nhận chuyến
                </button>
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
    <div className="h-full flex flex-col lg:flex-row bg-[#e8ece3] dark:bg-surface-dark overflow-hidden relative">
      {/* Vùng 1: Thông tin chuyến đi */}
      <div className="w-full lg:w-[420px] flex flex-col h-[55vh] lg:h-full bg-white dark:bg-surface-card border-b lg:border-b-0 lg:border-r border-gray-200 dark:border-surface-border z-10 shadow-2xl shrink-0">
        <div className="p-5 md:p-6 border-b border-gray-100 dark:border-surface-border sticky top-0 z-20 bg-white/95 dark:bg-surface-card/95 backdrop-blur-sm">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h1 className="font-display font-bold text-gray-900 dark:text-white text-xl">Chuyến đi #{currentTrip.bookingId?.slice(-8)}</h1>
              <p className="text-xs text-gray-500 font-medium mt-1">Đang xử lý chuyến đi</p>
            </div>
            <span className="px-3 py-1.5 rounded-lg text-xs font-bold text-brand-500 bg-brand-50 dark:bg-brand-500/10 border border-brand-100 dark:border-brand-500/20 flex items-center">
              <span className="w-2 h-2 rounded-full bg-brand-500 animate-pulse mr-2" />
              {BOOKING_STATUS_LABEL[currentTrip.bookingStatus] || currentTrip.bookingStatus}
            </span>
          </div>

          {/* Progress steps */}
          <div className="flex items-center justify-between mt-2 px-2">
            {STATUS_FLOW.map((s, i) => (
              <div key={s.status} className={cn('flex items-center', i < STATUS_FLOW.length - 1 && 'flex-1')}>
                <div className={cn(
                  'w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold shrink-0 transition-all duration-500',
                  i <= stepIndex ? 'bg-brand-500 text-white shadow-[0_0_15px_rgba(34,197,94,0.4)]' : 'bg-gray-100 text-gray-400 dark:bg-surface-dark',
                )}>
                  {i < stepIndex ? <RiCheckLine size={16} /> : i + 1}
                </div>
                {i < STATUS_FLOW.length - 1 && (
                  <div className={cn('flex-1 h-1 mx-2 rounded-full transition-all duration-700', i < stepIndex ? 'bg-brand-500' : 'bg-gray-100 dark:bg-surface-dark')} />
                )}
              </div>
            ))}
          </div>
        </div>

        {/* Scrollable Info Area */}
        <div className="p-5 md:p-6 overflow-y-auto no-scrollbar space-y-6 flex-1 bg-gray-50/50 dark:bg-transparent">
          {/* Customer info */}
          {currentTrip.customerId && (
            <div className="bg-white dark:bg-surface-card rounded-2xl p-5 border border-gray-100 dark:border-surface-border shadow-sm">
              <h3 className="font-bold text-gray-900 dark:text-white text-sm mb-4 uppercase tracking-wider">Khách hàng</h3>
              <div className="flex items-center gap-4">
                <div className="w-14 h-14 rounded-full bg-blue-50 dark:bg-blue-500/10 border border-blue-100 dark:border-blue-500/20 flex items-center justify-center text-2xl font-bold text-blue-500 shrink-0">
                  {currentTrip.customerName?.[0] || <RiUserLine size={24} />}
                </div>
                <div className="flex-1">
                  <p className="font-bold text-gray-900 dark:text-white text-base">{currentTrip.customerName}</p>
                  <p className="text-sm text-gray-500 mt-0.5">{currentTrip.customerPhone}</p>
                </div>
                <div className="flex gap-2">
                  <button className="w-12 h-12 rounded-xl bg-gray-100 hover:bg-gray-200 dark:bg-surface-dark dark:hover:bg-surface-border flex items-center justify-center text-gray-700 dark:text-gray-300 transition-colors"
                    title={currentTrip.customerPhone}
                    onClick={() => window.open(`tel:${currentTrip.customerPhone}`)}
                  >
                    <RiPhoneLine size={20} />
                  </button>
                  <button
                    onClick={() => setChatOpen(true)}
                    className="w-12 h-12 rounded-xl bg-brand-50 hover:bg-brand-100 dark:bg-brand-500/10 dark:hover:bg-brand-500/20 border border-brand-100 dark:border-brand-500/20 flex items-center justify-center text-brand-500 transition-colors"
                  >
                    <RiMessage2Line size={20} />
                  </button>
                </div>
              </div>
            </div>
          )}

          {/* Route info */}
          <div className="bg-white dark:bg-surface-card rounded-2xl p-5 border border-gray-100 dark:border-surface-border shadow-sm">
            <h3 className="font-bold text-gray-900 dark:text-white text-sm mb-5 uppercase tracking-wider">Hành trình & Cước phí</h3>
            
            <div className="relative mb-6">
               <div className="absolute left-[11px] top-6 bottom-6 w-0.5 bg-gray-200 dark:bg-surface-border"></div>
               <div className="space-y-5 relative z-10">
                 <div className="flex items-start gap-4">
                   <div className="w-6 h-6 rounded-full bg-brand-50 dark:bg-brand-500/10 flex items-center justify-center shrink-0 mt-0.5">
                     <RiMapPinLine size={14} className="text-brand-500" />
                   </div>
                   <div>
                     <p className="text-gray-900 dark:text-white text-sm font-semibold">{currentTrip.pickupLocation}</p>
                   </div>
                 </div>
                 <div className="flex items-start gap-4">
                   <div className="w-6 h-6 rounded-full bg-red-50 dark:bg-red-500/10 flex items-center justify-center shrink-0 mt-0.5">
                     <RiMapPin2Line size={14} className="text-red-500" />
                   </div>
                   <div>
                     <p className="text-gray-900 dark:text-white text-sm font-semibold">{currentTrip.dropoffLocation}</p>
                   </div>
                 </div>
               </div>
            </div>
            
            <div className="border-t border-gray-100 dark:border-surface-border pt-4">
              <div className="flex justify-between items-center mb-2">
                <span className="text-gray-500 font-medium text-sm">Cước phí</span>
                <div className="text-right flex items-center gap-2">
                  {currentTrip.originalPrice && currentTrip.originalPrice > currentTrip.totalPrice && (
                    <span className="text-xs text-gray-400 line-through">{formatCurrency(currentTrip.originalPrice)}</span>
                  )}
                  <span className="font-display font-bold text-brand-500 text-xl">{formatCurrency(currentTrip.totalPrice)}</span>
                </div>
              </div>
              <div className="flex justify-between items-center text-sm">
                <span className="text-gray-500 font-medium">Thanh toán</span>
                <span className="text-gray-900 dark:text-white font-bold px-2 py-1 bg-gray-100 dark:bg-surface-dark rounded-md">
                  {currentTrip.paymentMethod === 'CASH' ? '💵 Tiền mặt' : '💳 Trực tuyến'}
                </span>
              </div>
            </div>
          </div>

          {/* Actions */}
          <div className="space-y-3 pt-2">
            {currentStep && (
              <button 
                onClick={handleNextStatus} 
                disabled={updating || cancelingTrip}
                className={cn(
                  "w-full py-4 rounded-xl font-bold text-white text-base transition-all flex justify-center items-center gap-2",
                  currentStep.next === BOOKING_STATUS.COMPLETED 
                    ? 'bg-blue-500 hover:bg-blue-600 shadow-[0_0_20px_rgba(59,130,246,0.4)]' 
                    : 'bg-brand-500 hover:bg-brand-600 shadow-[0_0_20px_rgba(34,197,94,0.4)]'
                )}
              >
                {updating ? <Spinner size="sm" color="white" /> : null}
                {currentStep.action}
              </button>
            )}
            
            {currentTrip && currentTrip.bookingStatus !== BOOKING_STATUS.IN_PROGRESS && (
              <button 
                onClick={handleCancelTrip} 
                disabled={cancelingTrip || updating}
                className="w-full py-4 rounded-xl font-bold text-red-500 bg-red-50 hover:bg-red-100 dark:bg-red-500/10 dark:hover:bg-red-500/20 transition-all flex items-center justify-center gap-2"
              >
                {cancelingTrip ? <Spinner size="sm" /> : <RiCloseLine size={20} />} Hủy chuyến đi
              </button>
            )}
          </div>
        </div>
      </div>

      {/* Vùng 2: Bản đồ Realtime */}
      <div className="flex-1 relative h-[45vh] lg:h-full bg-[#e8ece3] dark:bg-surface-dark z-0">
        <InteractiveMap pickup={pickupCoord} dropoff={dropoffCoord} driver={driverCoord} />
        
        {/* Map Overlay Indicator */}
        <div className="absolute top-6 right-6 z-10 bg-white/90 dark:bg-surface-card/90 backdrop-blur-md px-4 py-2.5 rounded-full border border-gray-100 dark:border-surface-border shadow-lg flex items-center gap-2">
          <div className="w-2.5 h-2.5 bg-brand-500 rounded-full animate-pulse" />
          <span className="text-xs font-bold text-gray-900 dark:text-white uppercase tracking-wider">Đường đi trực tiếp</span>
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
