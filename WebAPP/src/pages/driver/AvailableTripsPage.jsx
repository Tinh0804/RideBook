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
  { status: BOOKING_STATUS.ACCEPTED, label: 'Đến đón khách', next: BOOKING_STATUS.ARRIVED, action: 'Tôi đã đến điểm đón' },
  { status: BOOKING_STATUS.ARRIVED, label: 'Đã đến điểm đón', next: BOOKING_STATUS.IN_PROGRESS, action: 'Đã đón khách, bắt đầu chạy' },
  { status: BOOKING_STATUS.IN_PROGRESS, label: 'Đang trên đường', next: BOOKING_STATUS.COMPLETED, action: 'Hoàn thành chuyến đi' },
]

const CANCEL_REASONS = [
  "Khách hàng không xuất hiện",
  "Khách hàng mang theo quá nhiều hành lý/thú cưng",
  "Xe gặp sự cố kỹ thuật/hỏng hóc",
  "Kẹt xe nghiêm trọng/đường cấm",
  "Lý do khác"
]

const playSound = (audio) => {
  if (!audio) return
  audio.currentTime = 0
  audio.play().catch(() => { })
}

const splitAddress = (address) => {
  if (!address) return { main: '', sub: '' }
  const parts = address.split(',').map(s => s.trim())
  if (parts.length <= 1) return { main: address, sub: '' }
  return {
    main: parts[0],
    sub: parts.slice(1).join(', ')
  }
}

const DriverTripFlowPage = () => {
  const navigate = useNavigate()
  const { user, userProfile, updateUserProfile } = useAuthStore()
  const { isOnline, setOnline, currentTrip, setCurrentTrip, clearCurrentTrip } = useDriverStore()

  // State for Waiting phase
  const [incomingTrip, setIncomingTrip] = useState(null)
  const [accepting, setAccepting] = useState(false)
  const [togglingOnline, setTogglingOnline] = useState(false)
  const [countdown, setCountdown] = useState(0)
  const [totalTimeout, setTotalTimeout] = useState(15)

  // State for Active Trip phase
  const [loadingTrip, setLoadingTrip] = useState(false)
  const [updating, setUpdating] = useState(false)
  const [cancelingTrip, setCancelingTrip] = useState(false)
  
  // Cancel Modal states
  const [isCancelModalOpen, setIsCancelModalOpen] = useState(false)
  const [selectedCancelReason, setSelectedCancelReason] = useState('')
  const [otherCancelReason, setOtherCancelReason] = useState('')
  const [chatOpen, setChatOpen] = useState(false)

  // Coords states for active trip — start null, always geocode from trip data
  const [pickupCoord, setPickupCoord] = useState(null)
  const [dropoffCoord, setDropoffCoord] = useState(null)
  const [driverCoord, setDriverCoord] = useState(null)

  // Ref to hold sendMessage from the active-trip WebSocket hook (avoids hook-ordering issues)
  const sendTripMessageRef = useRef(null)
  const receiveBookingSoundRef = useRef(null)
  const statusBookingSoundRef = useRef(null)
  const cancelBookingSoundRef = useRef(null)

  useEffect(() => {
    receiveBookingSoundRef.current = new Audio('/sounds/recivebooking.mp3')
    statusBookingSoundRef.current = new Audio('/sounds/statusbooking.mp3')
    cancelBookingSoundRef.current = new Audio('/sounds/cancelbooking.mp3')
    receiveBookingSoundRef.current.preload = 'auto'
    statusBookingSoundRef.current.preload = 'auto'
    cancelBookingSoundRef.current.preload = 'auto'

    return () => {
      receiveBookingSoundRef.current?.pause()
      statusBookingSoundRef.current?.pause()
      cancelBookingSoundRef.current?.pause()
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
          } else {
            // Cập nhật vị trí tự do lên Redis GEO khi tài xế di chuyển mà chưa có chuyến
            driverApi.updateFreeLocation(lat, lng).catch(() => { })
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
        // Phát âm thanh huỷ chuyến
        playSound(cancelBookingSoundRef.current)
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
        const parts = payload.split(':')
        const bookingId = parts[1]
        const timeout = parseInt(parts[2], 10) || 20
        bookingApi.getById(bookingId)
          .then(b => {
             setTotalTimeout(timeout)
             setCountdown(timeout)
             setIncomingTrip(b)
          })
          .catch(() => toast.error('Lỗi khi tải thông tin cuốc xe mới'))
        return
      }
    }

    if (payload?.type === 'NEW_BOOKING' && payload?.booking) {
      setTotalTimeout(20)
      setCountdown(20)
      setIncomingTrip(payload.booking)
      // Optional: play sound here
    } else if (payload?.type === 'BOOKING_TAKEN' || payload?.type === 'BOOKING_CANCELLED') {
      if (incomingTrip?.bookingId === payload.bookingId) {
        setIncomingTrip(null)
        if (payload?.type === 'BOOKING_CANCELLED') {
          playSound(cancelBookingSoundRef.current)
          toast.error('Chuyến đi đã bị khách hủy')
        } else {
          toast.error('Chuyến đã có tài xế khác nhận')
        }
      }
    }
  }, [isOnline, currentTrip, incomingTrip])

  // Countdown effect for incoming trip
  useEffect(() => {
    let timer
    if (incomingTrip && countdown > 0) {
      timer = setInterval(() => {
        setCountdown(c => {
          if (c <= 1) {
            clearInterval(timer)
            setIncomingTrip(null)
            return 0
          }
          return c - 1
        })
      }, 1000)
    }
    return () => {
      if (timer) clearInterval(timer)
    }
  }, [incomingTrip, countdown])

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
        .catch(() => { })
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
      // Lấy tọa độ hiện tại từ thiết bị trước khi gửi
      let lat = null, lng = null
      try {
        const pos = await new Promise((resolve, reject) =>
          navigator.geolocation.getCurrentPosition(resolve, reject, { timeout: 5000, enableHighAccuracy: true })
        )
        lat = pos.coords.latitude
        lng = pos.coords.longitude
      } catch (geoErr) {
        console.warn('Could not get geolocation:', geoErr)
      }

      const data = await driverApi.toggleStatus(lat, lng)
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

  const handleConfirmCancel = async () => {
    if (!selectedCancelReason) {
      toast.error('Vui lòng chọn lý do hủy chuyến')
      return
    }
    if (selectedCancelReason === 'Lý do khác' && !otherCancelReason.trim()) {
      toast.error('Vui lòng nhập chi tiết lý do hủy')
      return
    }

    setCancelingTrip(true);
    try {
      // In the future, we can pass finalReason to the backend here
      const finalReason = selectedCancelReason === 'Lý do khác' ? otherCancelReason.trim() : selectedCancelReason;
      await bookingApi.cancelBookingByDriver(currentTrip.bookingId, user.id);
      toast.success('Đã huỷ chuyến thành công');
      clearCurrentTrip();
      setIsCancelModalOpen(false);
      setSelectedCancelReason('');
      setOtherCancelReason('');
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
        <Modal isOpen={!!incomingTrip} onClose={handleReject} title="🚀 Có chuyến mới!" size="md" closeOnOverlayClick={false}>
          {incomingTrip && (
            <div className="space-y-4 animate-fade-in-up bg-gray-50 dark:bg-surface-dark p-2 -mx-4 -mb-4">
              {/* Price Card */}
              <div className="bg-white dark:bg-surface-card rounded-xl p-4 shadow-sm border border-gray-100 dark:border-surface-border flex justify-between items-start">
                <div>
                  <div className="text-xs font-semibold text-gray-500 mb-1">Cước phí</div>
                  <div className="font-display text-3xl font-bold text-gray-900 dark:text-white">
                    {formatCurrency(incomingTrip.totalPrice || incomingTrip.price)}
                  </div>
                  {incomingTrip.paymentMethod && (
                    <div className="mt-2">
                      <span className="px-2 py-1 rounded bg-gray-100 text-gray-600 dark:bg-gray-800 dark:text-gray-300 text-xs font-semibold">
                        {incomingTrip.paymentMethod === 'CASH' ? 'Tiền mặt' : 'Online'}
                      </span>
                    </div>
                  )}
                </div>
                
                {/* Circular Progress Countdown */}
                <div className="relative w-16 h-16 flex items-center justify-center shrink-0">
                  <svg className="absolute inset-0 w-full h-full transform -rotate-90" viewBox="0 0 64 64">
                    <circle cx="32" cy="32" r="28" stroke="currentColor" strokeWidth="4" fill="transparent" className="text-gray-100 dark:text-gray-800" />
                    <circle cx="32" cy="32" r="28" stroke="currentColor" strokeWidth="4" fill="transparent" 
                      strokeDasharray="176" 
                      strokeDashoffset={176 - (176 * countdown) / totalTimeout} 
                      className={cn("transition-all duration-1000 ease-linear", countdown <= 5 ? "text-red-500" : "text-brand-500")} />
                  </svg>
                  <span className={cn("text-xl font-bold font-display absolute", countdown <= 5 ? "text-red-500 animate-pulse" : "text-gray-900 dark:text-white")}>
                    {countdown}s
                  </span>
                </div>
              </div>

              {/* Route Card */}
              <div className="bg-white dark:bg-surface-card rounded-xl shadow-sm border border-gray-100 dark:border-surface-border overflow-hidden">
                <div className="py-3 text-center border-b border-gray-100 dark:border-surface-border bg-gray-50/50 dark:bg-surface-dark/50">
                  <span className="text-sm font-bold text-gray-800 dark:text-gray-200">
                    {formatDistance(incomingTrip.distance)} - {incomingTrip.vehicleTypeName || 'Ô tô'}
                  </span>
                </div>
                
                <div className="p-4">
                  <div className="relative">
                    <div className="absolute left-[11px] top-4 bottom-4 w-0 border-l-2 border-dotted border-gray-300 dark:border-gray-600"></div>
                    <div className="space-y-6 relative z-10">
                      <div className="flex items-start gap-4">
                        <div className="w-6 h-6 rounded-full bg-black dark:bg-white flex items-center justify-center shrink-0 mt-0.5 shadow-sm ring-4 ring-white dark:ring-surface-card">
                          <div className="w-2 h-2 rounded-full bg-white dark:bg-black" />
                        </div>
                        <div className="flex-1 min-w-0">
                          <p className="text-sm font-bold text-gray-900 dark:text-white leading-snug truncate">
                            {splitAddress(incomingTrip.pickupLocation).main}
                          </p>
                          <p className="text-xs text-gray-500 mt-0.5 truncate">
                            {splitAddress(incomingTrip.pickupLocation).sub || 'Điểm đón'}
                          </p>
                        </div>
                      </div>
                      
                      <div className="flex items-start gap-4">
                        <div className="w-6 h-6 rounded-full bg-brand-500 flex items-center justify-center shrink-0 mt-0.5 shadow-sm ring-4 ring-white dark:ring-surface-card">
                          <div className="w-2 h-2 rounded-full bg-white" />
                        </div>
                        <div className="flex-1 min-w-0">
                          <p className="text-sm font-bold text-gray-900 dark:text-white leading-snug truncate">
                            {splitAddress(incomingTrip.dropoffLocation).main}
                          </p>
                          <p className="text-xs text-gray-500 mt-0.5 truncate">
                            {splitAddress(incomingTrip.dropoffLocation).sub || 'Điểm đến'}
                          </p>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              {/* Actions */}
              <div className="flex gap-3 pt-2 pb-2 px-2">
                <button
                  onClick={handleReject}
                  disabled={accepting}
                  className="w-14 h-14 shrink-0 rounded-xl flex items-center justify-center border border-gray-200 dark:border-surface-border text-gray-500 hover:bg-gray-100 dark:hover:bg-surface-border transition-colors active:scale-95 bg-white dark:bg-surface-card shadow-sm"
                  title="Bỏ qua"
                >
                  <RiCloseLine size={24} />
                </button>
                <button
                  onClick={handleAccept}
                  disabled={accepting}
                  className="flex-1 h-14 rounded-xl font-bold text-lg bg-brand-500 hover:bg-brand-600 text-white shadow-sm transition-all flex items-center justify-center gap-2 disabled:opacity-70"
                >
                  {accepting ? <Spinner size="sm" color="white" /> : <RiCheckLine size={24} />} 
                  Nhận chuyến ngay
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
                  <div className="flex-1">
                    <p className="text-gray-900 dark:text-white text-sm font-semibold leading-tight">
                      {splitAddress(currentTrip.pickupLocation).main}
                    </p>
                    <p className="text-xs text-gray-500 mt-0.5">
                      {splitAddress(currentTrip.pickupLocation).sub || 'Điểm đón'}
                    </p>
                  </div>
                </div>
                <div className="flex items-start gap-4">
                  <div className="w-6 h-6 rounded-full bg-red-50 dark:bg-red-500/10 flex items-center justify-center shrink-0 mt-0.5">
                    <RiMapPin2Line size={14} className="text-red-500" />
                  </div>
                  <div className="flex-1">
                    <p className="text-gray-900 dark:text-white text-sm font-semibold leading-tight">
                      {splitAddress(currentTrip.dropoffLocation).main}
                    </p>
                    <p className="text-xs text-gray-500 mt-0.5">
                      {splitAddress(currentTrip.dropoffLocation).sub || 'Điểm đến'}
                    </p>
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
                onClick={() => setIsCancelModalOpen(true)}
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

      {/* Cancel Trip Modal */}
      <Modal isOpen={isCancelModalOpen} onClose={() => !cancelingTrip && setIsCancelModalOpen(false)} title="Lý do hủy chuyến" size="md">
        <div className="space-y-4">
          <p className="text-sm text-gray-500 dark:text-gray-400">
            Việc hủy chuyến thường xuyên có thể ảnh hưởng đến tỷ lệ nhận chuyến của bạn. Vui lòng chọn lý do hủy:
          </p>

          <div className="space-y-3 mt-4">
            {CANCEL_REASONS.map((reason) => (
              <div
                key={reason}
                onClick={() => setSelectedCancelReason(reason)}
                className={cn(
                  "flex items-center gap-3 p-3 rounded-xl border cursor-pointer transition-colors",
                  selectedCancelReason === reason
                    ? "border-red-500 bg-red-50 dark:bg-red-500/10"
                    : "border-gray-200 dark:border-surface-border hover:bg-gray-50 dark:hover:bg-surface-hover"
                )}
              >
                <div className={cn(
                  "w-5 h-5 rounded-full border-2 flex items-center justify-center shrink-0",
                  selectedCancelReason === reason ? "border-red-500" : "border-gray-300 dark:border-gray-600"
                )}>
                  {selectedCancelReason === reason && <div className="w-2.5 h-2.5 rounded-full bg-red-500" />}
                </div>
                <span className={cn(
                  "text-sm font-medium",
                  selectedCancelReason === reason ? "text-red-700 dark:text-red-400" : "text-gray-700 dark:text-gray-300"
                )}>
                  {reason}
                </span>
              </div>
            ))}
          </div>

          {selectedCancelReason === 'Lý do khác' && (
            <textarea
              className="w-full mt-3 p-3 text-sm rounded-xl border border-gray-200 dark:border-surface-border bg-white dark:bg-surface-dark text-gray-900 dark:text-white focus:ring-2 focus:ring-red-500 focus:border-red-500 outline-none resize-none transition-all"
              rows={3}
              placeholder="Nhập lý do hủy chuyến của bạn..."
              value={otherCancelReason}
              onChange={(e) => setOtherCancelReason(e.target.value)}
            />
          )}

          <div className="flex gap-3 pt-4 border-t border-gray-100 dark:border-surface-border mt-6">
            <button
              onClick={() => setIsCancelModalOpen(false)}
              disabled={cancelingTrip}
              className="flex-1 py-3 rounded-xl font-bold text-gray-700 bg-gray-100 hover:bg-gray-200 dark:bg-surface-dark dark:text-gray-300 dark:hover:bg-surface-border transition-colors"
            >
              Quay lại
            </button>
            <button
              onClick={handleConfirmCancel}
              disabled={cancelingTrip || !selectedCancelReason || (selectedCancelReason === 'Lý do khác' && !otherCancelReason.trim())}
              className="flex-1 py-3 rounded-xl font-bold text-white bg-red-500 hover:bg-red-600 disabled:opacity-50 disabled:cursor-not-allowed shadow-[0_0_15px_rgba(239,68,68,0.3)] transition-colors flex items-center justify-center gap-2"
            >
              {cancelingTrip ? <Spinner size="sm" color="white" /> : 'Xác nhận hủy'}
            </button>
          </div>
        </div>
      </Modal>

    </div>
  )
}

export default DriverTripFlowPage
