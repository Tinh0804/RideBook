import { useState, useEffect, useCallback } from 'react'
import { useLocation, useNavigate, useSearchParams } from 'react-router-dom'
import toast from 'react-hot-toast'
import {
  RiMapPinLine, RiMapPin2Line, RiTicketLine,
  RiBankCardLine, RiMoneyDollarCircleLine, RiCheckLine,
  RiArrowLeftLine
} from 'react-icons/ri'
import { useAuthStore, useBookingStore } from '@/store/rootStore'
import { useWebSocket } from '@/hooks/useWebSocket'
import { bookingApi } from '@/features/booking/api/bookingApi'
import { masterDataApi } from '@/features/booking/api/masterDataApi'
import { paymentApi } from '@/features/payment/api/paymentApi'
import { formatCurrency, formatDistance } from '@/utils/currency'
import { PAYMENT_METHOD, BOOKING_STATUS } from '@/config'
import Button from '@/components/Elements/Button'
import AddressInput from '@/components/Map/AddressInput'
import Input from '@/components/Elements/Input'
import Spinner from '@/components/Elements/Spinner'
import Modal from '@/components/Elements/Modal'
import LocationAutocomplete from '@/components/Elements/LocationAutocomplete'
import InteractiveMap from '@/components/Map/InteractiveMap'
import { cn } from '@/utils/cn'

const DUMMY_DISTANCE = 5.2  // km - in real app use Google Maps/OSRM Distance Matrix

const BookingPage = () => {
  const navigate = useNavigate()
  const location = useLocation()
  const [searchParams, setSearchParams] = useSearchParams()
  const { user, userProfile }  = useAuthStore()
  const { vehicleTypes, setVehicleTypes, currentBooking, setCurrentBooking, setEstimatedPrice, estimatedPrice, clearCurrentBooking } = useBookingStore()

  const customerId = userProfile?.id || user?.id

  // ── Locations state: objects with { name, lat, lng } ───────────────────────
  const [pickup,          setPickup]          = useState(() => {
    const saved = localStorage.getItem('temp_pickup')
    return saved ? JSON.parse(saved) : null
  })
  const [dropoff,         setDropoff]         = useState(() => {
    const saved = localStorage.getItem('temp_dropoff')
    return saved ? JSON.parse(saved) : null
  })

  useEffect(() => {
    if (pickup) {
      localStorage.setItem('temp_pickup', JSON.stringify(pickup))
    } else {
      localStorage.removeItem('temp_pickup')
    }
  }, [pickup])

  useEffect(() => {
    if (dropoff) {
      localStorage.setItem('temp_dropoff', JSON.stringify(dropoff))
    } else {
      localStorage.removeItem('temp_dropoff')
    }
  }, [dropoff])

  const [selectingLocationFor, setSelectingLocationFor] = useState(null)
  const [tempMapLocation, setTempMapLocation] = useState(null)
  const [mapLoading, setMapLoading] = useState(false)
  
  const [step,            setStep]            = useState(1) // 1: Select loc, 2: Details, 3: Finding Driver
  const [selectedVType,   setSelectedVType]   = useState(null)
  const [paymentMethod,   setPaymentMethod]   = useState(PAYMENT_METHOD.CASH)
  const [paymentProvider, setPaymentProvider] = useState('VNPAY')
  const [promoCode,       setPromoCode]       = useState('')
  const [promoData,       setPromoData]       = useState(null)
  const [promoLoading,    setPromoLoading]    = useState(false)
  const [loading,         setLoading]         = useState(false)
  const [estimating,      setEstimating]      = useState(false)
  const [estimatedPrices, setEstimatedPrices] = useState([])
  const [countdown,       setCountdown]       = useState(0)
  const [isCanceling,     setCanceling]       = useState(false)
  const [myPromotions,    setMyPromotions]    = useState([])
  const [isPromoModalOpen, setPromoModalOpen] = useState(false)

  // ── WebSocket ───────────────────────────────────────────────────────────────
  const onWsMessage = useCallback((topic, payload) => {
    if (typeof payload === 'string') {
      if (payload.startsWith('DRIVER_ASSIGNED:')) {
        const bookingId = payload.split(':')[1]
        if (currentBooking?.bookingId === bookingId) {
          bookingApi.getById(bookingId).then((b) => {
            setCurrentBooking(b)
            toast.success('Đã tìm thấy tài xế!')
            navigate('/customer/tracking', { state: { pickup, dropoff } })
          })
        }
      } else if (payload.startsWith('NO_DRIVER_FOUND:')) {
        const bookingId = payload.split(':')[1]
        if (currentBooking?.bookingId === bookingId) {
          toast.error('Không tìm thấy tài xế. Vui lòng thử lại sau.')
          clearCurrentBooking()
          setStep(1)
        }
      }
    }
  }, [currentBooking, setCurrentBooking, clearCurrentBooking, navigate, pickup, dropoff])

  useWebSocket(customerId ? [`/topic/customer/${customerId}`] : [], onWsMessage)

  // Prevent accessing booking page if there is an active booking that is NOT PENDING and NOT CANCELLED
  useEffect(() => {
    if (
      currentBooking &&
      currentBooking.bookingStatus !== BOOKING_STATUS.PENDING &&
      currentBooking.bookingStatus !== BOOKING_STATUS.CANCELLED
    ) {
      navigate('/customer/tracking', { replace: true, state: { pickup, dropoff } })
    }
  }, [currentBooking, navigate, pickup, dropoff])

  useEffect(() => {
    if (step === 2 && customerId) {
      masterDataApi.getMyPromotions(customerId)
        .then(data => {
          // Deduplicate by promotionCode to avoid React key warning
          const uniquePromos = Array.from(new Map((data || []).map(p => [p.promotionCode, p])).values());
          setMyPromotions(uniquePromos);
        })
        .catch(() => {})
    }
  }, [step, customerId])

  useEffect(() => {
    if (!vehicleTypes.length) {
      masterDataApi.getVehicleTypes()
        .then((types) => {
          setVehicleTypes(types)
          if (types.length) setSelectedVType(types[0])
        })
        .catch(() => {})
    } else if (!selectedVType && vehicleTypes.length) {
      setSelectedVType(vehicleTypes[0])
    }
  }, [vehicleTypes, selectedVType, setVehicleTypes])

  const handleEstimatePrice = () => {
    if (!pickup || !dropoff) return
    setEstimating(true)
    bookingApi.estimatePrice({ 
      pickupLat: safePickup.lat,
      pickupLng: safePickup.lng,
      dropoffLat: safeDropoff.lat,
      dropoffLng: safeDropoff.lng,
      promotionCode: promoData?.promotionCode || null
    })
      .then((estimates) => {
        if (Array.isArray(estimates)) {
          setEstimatedPrices(estimates)
          setCountdown(120) // 120s expiration
        }
      })
      .catch(() => {})
      .finally(() => setEstimating(false))
  }

  // Estimate price when step 2 is active
  useEffect(() => {
    if (step !== 2) return
    handleEstimatePrice()
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [pickup, dropoff, step, promoData])

  // Countdown timer
  useEffect(() => {
    if (countdown <= 0) return
    const timer = setInterval(() => setCountdown(c => c - 1), 1000)
    return () => clearInterval(timer)
  }, [countdown])

  const applyPromo = async () => {
    if (!promoCode.trim()) return
    setPromoLoading(true)
    try {
      const data = await masterDataApi.getPromotionByCode(promoCode)
      setPromoData(data)
      toast.success('Áp dụng mã khuyến mãi thành công!')
    } catch {
      toast.error('Mã khuyến mãi không hợp lệ hoặc đã hết hạn')
      setPromoData(null)
    } finally {
      setPromoLoading(false)
    }
  }

  const handleBook = async () => {
    if (!selectedVType || !pickup || !dropoff) {
      toast.error('Vui lòng điền đầy đủ thông tin')
      return
    }
    if (countdown <= 0) {
      toast.error('Báo giá đã hết hạn, vui lòng làm mới giá')
      return
    }
    setLoading(true)
    const selectedEstimate = estimatedPrices.find(e => e.vehicleTypeId === selectedVType.vehicleTypeId)

    try {
      const payload = {
        customerId:      user?.id,
        paymentMethod,
        pickupLocation:  pickup.name,
        dropoffLocation: dropoff.name,
        pickupLat:       pickup.lat,
        pickupLng:       pickup.lng,
        dropoffLat:      dropoff.lat,
        dropoffLng:      dropoff.lng,
        distance:        selectedEstimate?.distance || DUMMY_DISTANCE,
        vehicleTypeId:   selectedVType.vehicleTypeId,
        promotionCode:   promoData?.promotionCode || null,
        quoteId:         selectedEstimate?.quoteId,
        returnUrl: `${window.location.origin}/customer/booking`
      }
      const booking = await bookingApi.createBooking(payload)
      setCurrentBooking(booking)

      // Save pickup and dropoff coords to localStorage
      if (pickup) {
        localStorage.setItem(`booking_pickup_${booking.bookingId}`, JSON.stringify(pickup))
      }
      if (dropoff) {
        localStorage.setItem(`booking_dropoff_${booking.bookingId}`, JSON.stringify(dropoff))
      }

      if (paymentMethod === PAYMENT_METHOD.ONLINE) {
        let pmData
        const paymentPayload = {
          referenceId: booking.bookingId,
          amount: selectedEstimate?.totalPrice || 0,
          orderInfo: `Thanh toan chuyen xe ${booking.bookingId}`,
          method: paymentProvider
        }
        if (paymentProvider === 'VNPAY') {
          pmData = await paymentApi.createVNPayUrl(paymentPayload)
        } else {
          pmData = await paymentApi.createMomoUrl(paymentPayload)
        }
        const url = pmData?.paymentUrl || pmData?.payUrl || pmData?.result?.paymentUrl || pmData?.result?.payUrl // Handle momo vs vnpay differences
        if (url) {
          window.location.href = url
          return // Stop execution, let the page redirect
        }
      }

      toast.success('Đặt xe thành công! Đang tìm tài xế...')
      setStep(3)
    } catch (err) {
      toast.error(err?.response?.data?.message || 'Đặt xe thất bại')
    } finally {
      setLoading(false)
    }
  }

  const handleCancelSearch = async () => {
    if (!currentBooking) {
      setStep(1)
      return
    }
    setCanceling(true)
    try {
      await bookingApi.cancelBooking(currentBooking.bookingId)
      clearCurrentBooking()
      setStep(1)
      toast.success('Đã hủy tìm kiếm tài xế')
    } catch (err) {
      toast.error('Không thể hủy tìm kiếm. Vui lòng thử lại.')
    } finally {
      setCanceling(false)
    }
  }

  const selectedEstimate = estimatedPrices.find(e => e.vehicleTypeId === selectedVType?.vehicleTypeId)
  const finalPrice  = selectedEstimate?.totalPrice || 0
  const originalPrice = (selectedEstimate?.basePrice || 0) * (selectedEstimate?.surgeMultiplier || 1) * (selectedEstimate?.surcharge || 1)
  const isDiscounted = originalPrice > finalPrice

  const isValidLocation = (location) => {
    return location && 
          typeof location === 'object' &&
          typeof location.lat === 'number' && 
          typeof location.lng === 'number' &&
          !isNaN(location.lat) && 
          !isNaN(location.lng)
  }

  // Tọa độ mặc định (TP.HCM)
  const DEFAULT_COORDINATES = { lat: 10.8231, lng: 106.6297 }

  const openMapSelection = (type) => {
    const existingLoc = type === 'pickup' ? pickup : dropoff
    if (existingLoc && isValidLocation(existingLoc)) {
      setTempMapLocation(existingLoc)
      setSelectingLocationFor(type)
    } else {
      setMapLoading(type) // show loading spinner on the button
      if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
          (position) => {
            setTempMapLocation({
              lat: position.coords.latitude,
              lng: position.coords.longitude,
              name: 'Vị trí hiện tại...'
            })
            setSelectingLocationFor(type)
            setMapLoading(false)
          },
          (error) => {
            setTempMapLocation({ ...DEFAULT_COORDINATES, name: 'Vị trí mặc định' })
            setSelectingLocationFor(type)
            setMapLoading(false)
            toast.error('Không thể lấy vị trí, dùng vị trí mặc định')
          },
          { enableHighAccuracy: true, timeout: 5000 }
        )
      } else {
        setTempMapLocation({ ...DEFAULT_COORDINATES, name: 'Vị trí mặc định' })
        setSelectingLocationFor(type)
        setMapLoading(false)
      }
    }
  }

  // Khi truyền vào InteractiveMap, đảm bảo tọa độ hợp lệ
  const safePickup = isValidLocation(pickup) ? pickup : { ...pickup, ...DEFAULT_COORDINATES }
  const safeDropoff = isValidLocation(dropoff) ? dropoff : { ...dropoff, ...DEFAULT_COORDINATES }
  const handleNextStep = () => {
    if (!isValidLocation(pickup) || !isValidLocation(dropoff)) {
      toast.error('Vui lòng chọn địa điểm chính xác từ danh sách gợi ý')
      return
    }
    
    // If they were searching before and the status is still pending, 
    // go straight to step 3
    if (currentBooking && currentBooking.bookingStatus === BOOKING_STATUS.PENDING) {
      setStep(3)
    } else {
      setStep(2)
    }
  }

  // Effect to automatically switch to step 3 if there's an active pending booking on mount
  useEffect(() => {
    // 1. Check if returning from payment gateway (VNPay/MoMo) directly via URL params
    const urlBookingId = searchParams.get('bookingId')
    const vnpStatus = searchParams.get('vnp_ResponseCode')
    const momoCode = searchParams.get('resultCode')

    if (urlBookingId && (vnpStatus || momoCode) && !currentBooking) {
      bookingApi.getById(urlBookingId).then(result => {
        setCurrentBooking(result)
        const isPaid = result?.paymentStatus === true || result?.paymentStatus === 'PAID' || vnpStatus === '00' || momoCode === '0'
        if (isPaid) {
          toast.success('Thanh toán thành công! Đang tìm tài xế...')
          setStep(3)
          // Clean up the URL to remove the payment query parameters via React Router
          setSearchParams({}, { replace: true })
        } else {
          toast.error('Thanh toán thất bại hoặc bị huỷ.')
        }
      }).catch(err => {
        console.error('Failed to fetch booking after payment', err)
      })
      return // Wait for API
    }

    // 2. Normal flow (already have currentBooking in store)
    if (currentBooking && currentBooking.bookingStatus === BOOKING_STATUS.PENDING && step !== 3) {
      const isOnline = currentBooking.paymentMethod === 'ONLINE'
      const isPaid = currentBooking.paymentStatus === true || location.state?.paymentSuccess || vnpStatus === '00' || momoCode === '0'
      
      // Nếu thanh toán tiền mặt, hoặc thanh toán online nhưng đã trả tiền -> hiện UI tìm tài xế
      if (!isOnline || isPaid) {
        setStep(3)
      }
    }
  }, [currentBooking, step, location.state, searchParams, setCurrentBooking])

  if (step === 1) {
    if (selectingLocationFor) {
      return (
        <div className="-m-6 h-[calc(100vh-64px)] relative flex flex-col bg-surface-dark">
          <div className="absolute inset-0 z-0">
            <InteractiveMap 
              pickup={selectingLocationFor === 'pickup' ? null : pickup} 
              dropoff={selectingLocationFor === 'dropoff' ? null : dropoff} 
              selectingMode={true}
              initialCenter={tempMapLocation ? [tempMapLocation.lat, tempMapLocation.lng] : null}
              onLocationSelect={(data) => setTempMapLocation(data)}
            />
          </div>
          <button 
            onClick={() => setSelectingLocationFor(null)}
            className="absolute top-4 left-4 z-10 w-10 h-10 bg-surface-card/90 backdrop-blur-md rounded-full flex items-center justify-center shadow-lg border border-surface-border hover:bg-surface-muted transition-colors"
          >
            <RiArrowLeftLine size={20} className="text-content-main" />
          </button>
          
          <div className="flex-1 min-h-0 pointer-events-none" />
          
          <div className="relative z-10 bg-surface-card rounded-t-3xl shadow-[0_-10px_40px_rgba(0,0,0,0.5)] border-t border-surface-border p-6 pointer-events-auto pb-8">
             <div className="flex items-start gap-4 mb-6">
                <div className={`w-10 h-10 rounded-full flex items-center justify-center shrink-0 ${selectingLocationFor === 'pickup' ? 'bg-brand-500/20 text-brand-400' : 'bg-red-500/20 text-red-400'}`}>
                  {selectingLocationFor === 'pickup' ? <RiMapPinLine size={24} /> : <RiMapPin2Line size={24} />}
                </div>
                <div>
                   <p className="text-sm text-content-muted font-medium mb-1">
                     {selectingLocationFor === 'pickup' ? 'Chọn điểm đón trên bản đồ' : 'Chọn điểm đến trên bản đồ'}
                   </p>
                   <p className="text-content-main font-semibold line-clamp-2 leading-snug">
                     {tempMapLocation?.name || 'Đang xác định vị trí...'}
                   </p>
                </div>
             </div>
             <Button 
               fullWidth size="lg" 
               disabled={!tempMapLocation}
               onClick={() => {
                 if (selectingLocationFor === 'pickup') setPickup(tempMapLocation)
                 else setDropoff(tempMapLocation)
                 setSelectingLocationFor(null)
               }}
             >
               Xác nhận vị trí
             </Button>
          </div>
        </div>
      )
    }

    return (
      <div className="max-w-2xl mx-auto space-y-8 mt-10">
        <div className="text-center space-y-2">
          <h1 className="text-3xl font-display font-bold text-content-main">Bạn muốn đi đâu?</h1>
          <p className="text-content-muted">Nhập điểm đón và điểm đến để bắt đầu hành trình</p>
        </div>
            
      
        <div className="card p-6 space-y-6 relative">
          
          {/* Vertical line connecting inputs */}
          <div className="absolute left-[41px] top-[50px] bottom-[50px] w-0.5 bg-surface-border border-dashed border-l-2" />
         
          <div className="space-y-6 relative z-10">
            <div className="flex items-center gap-4">
              <div className="w-8 h-8 rounded-full bg-brand-500/20 flex items-center justify-center shrink-0 shadow-glow-green">
                <RiMapPinLine size={18} className="text-brand-400" />
              </div>
              <div className="flex-1 flex gap-2 items-start">
                <div className="flex-1">
                  <p className="text-xs text-content-muted mb-1 ml-1">Điểm đón</p>
                  <AddressInput
                    placeholder="Điểm đón của bạn"
                    value={pickup?.name || ''}
                    onChange={(name) => {
                      setPickup({ name })
                    }}
                    onLocationDetect={(locationData) => {
                      if (locationData) {
                        setPickup({
                          name: locationData.name,
                          lat: locationData.lat,
                          lng: locationData.lng
                        })
                      } else {
                        setPickup(null)
                      }
                    }}
                  />
                </div>
                <button 
                  onClick={() => openMapSelection('pickup')}
                  disabled={mapLoading === 'pickup'}
                  className="w-[42px] h-[42px] mt-6 shrink-0 bg-surface border border-surface-border rounded-xl flex items-center justify-center hover:bg-surface-hover hover:border-brand-500/50 transition-all shadow-sm group"
                  title="Chọn trên bản đồ"
                >
                  {mapLoading === 'pickup' ? (
                    <Spinner size="sm" />
                  ) : (
                    <RiMapPinLine size={20} className="text-content-muted group-hover:text-brand-400 transition-colors" />
                  )}
                </button>
              </div>
            </div>

            <div className="flex items-center gap-4">
              <div className="w-8 h-8 rounded-full bg-red-500/20 flex items-center justify-center shrink-0 shadow-[0_0_15px_rgba(239,68,68,0.2)]">
                <RiMapPin2Line size={18} className="text-red-400" />
              </div>
              <div className="flex-1 flex gap-2 items-start">
                <div className="flex-1">
                  <p className="text-xs text-content-muted mb-1 ml-1">Điểm đến</p>
                  <LocationAutocomplete 
                    placeholder="Điểm đến của bạn"
                    value={dropoff?.name || ''}
                    onChange={(name) => {
                      setDropoff({ name })
                    }}
                    onSelectLocation={(locationData) => {
                      if (locationData) {
                        setDropoff({
                          name: locationData.name,
                          lat: locationData.lat,
                          lng: locationData.lng
                        })
                      } else {
                        setDropoff(null)
                      }
                    }}
                  />
                </div>
                <button 
                  onClick={() => openMapSelection('dropoff')}
                  disabled={mapLoading === 'dropoff'}
                  className="w-[42px] h-[42px] mt-6 shrink-0 bg-surface border border-surface-border rounded-xl flex items-center justify-center hover:bg-surface-hover hover:border-red-500/50 transition-all shadow-sm group"
                  title="Chọn trên bản đồ"
                >
                  {mapLoading === 'dropoff' ? (
                    <Spinner size="sm" />
                  ) : (
                    <RiMapPin2Line size={20} className="text-content-muted group-hover:text-red-400 transition-colors" />
                  )}
                </button>
              </div>
            </div>
          </div>

          <Button 
            fullWidth 
            size="lg" 
            onClick={handleNextStep} 
            disabled={!pickup || !dropoff}
            className="mt-6"
          >
            Tiếp tục
          </Button>
        </div>
      </div>
    )
  }

  // Step 3: Finding Driver
  if (step === 3) {
    return (
      <div className="-m-6 h-[calc(100vh-64px)] relative flex flex-col bg-surface-dark">
        {/* Background Map */}
        <div className="absolute inset-0 z-0">
        <InteractiveMap 
          pickup={isValidLocation(pickup) ? pickup : { name: pickup?.name, ...DEFAULT_COORDINATES }} 
          dropoff={isValidLocation(dropoff) ? dropoff : { name: dropoff?.name, ...DEFAULT_COORDINATES }} 
        />
        </div>

        {/* Back button overlay - allow them to cancel searching or modify details */}
        <button 
          onClick={handleCancelSearch}
          disabled={isCanceling}
          className="absolute top-4 left-4 z-10 w-10 h-10 bg-surface-card/90 backdrop-blur-md rounded-full flex items-center justify-center shadow-lg border border-surface-border hover:bg-surface-muted transition-colors"
        >
          {isCanceling ? <Spinner size="sm" /> : <RiArrowLeftLine size={20} className="text-content-main" />}
        </button>

        {/* Spacer */}
        <div className="flex-1 min-h-0 pointer-events-none" />

        {/* Bottom Sheet Card */}
        <div className="relative z-10 bg-surface-card rounded-t-3xl shadow-[0_-10px_40px_rgba(0,0,0,0.5)] border-t border-surface-border p-6 flex flex-col items-center justify-center space-y-6 pointer-events-auto pb-10">
          
          <div className="relative flex items-center justify-center w-24 h-24">
            <div className="absolute inset-0 bg-brand-500/20 rounded-full animate-ping" />
            <div className="absolute inset-2 bg-brand-500/40 rounded-full animate-pulse" />
            <div className="absolute inset-4 bg-brand-500 rounded-full flex items-center justify-center shadow-glow-green">
              <span className="text-3xl">📡</span>
            </div>
          </div>
          
          <div className="text-center space-y-2">
            <h3 className="text-xl font-display font-bold text-content-main">Đang tìm tài xế...</h3>
            <p className="text-sm text-content-muted">Vui lòng đợi trong giây lát, hệ thống đang kết nối với các tài xế gần bạn nhất.</p>
          </div>

          <Button 
            variant="outline" 
            fullWidth 
            onClick={handleCancelSearch} 
            loading={isCanceling}
          >
            Hủy tìm kiếm
          </Button>
        </div>
      </div>
    )
  }

  // Step 2: Map & Booking Details
  return (
    <div className="-m-6 h-[calc(100vh-64px)] relative flex flex-col bg-surface-dark">
      {/* Background Map */}
      <div className="absolute inset-0 z-0">
        <InteractiveMap 
          pickup={isValidLocation(pickup) ? pickup : { name: pickup?.name, ...DEFAULT_COORDINATES }} 
          dropoff={isValidLocation(dropoff) ? dropoff : { name: dropoff?.name, ...DEFAULT_COORDINATES }} 
        />
      </div>

      {/* Back button overlay */}
      <button 
        onClick={() => setStep(1)}
        className="absolute top-4 left-4 z-10 w-10 h-10 bg-surface-card/90 backdrop-blur-md rounded-full flex items-center justify-center shadow-lg border border-surface-border hover:bg-surface-muted transition-colors"
      >
        <RiArrowLeftLine size={20} className="text-content-main" />
      </button>

      {/* Spacer to push Bottom Sheet down */}
      <div className="flex-1 min-h-0 pointer-events-none" />

      {/* Bottom Sheet Card */}
      <div className="relative z-10 bg-surface-card rounded-t-3xl shadow-[0_-10px_40px_rgba(0,0,0,0.5)] border-t border-surface-border flex flex-col max-h-[65vh] pointer-events-auto">
        {/* Drag handle pill */}
        <div className="w-full flex justify-center py-3 shrink-0">
          <div className="w-12 h-1.5 bg-surface-border rounded-full" />
        </div>

        <div className="p-6 pt-0 overflow-y-auto no-scrollbar space-y-6 pb-8">
          {/* Vehicle type selection */}
          <div className="space-y-3">
            <h3 className="font-semibold text-content-main text-sm">Loại xe</h3>
            {vehicleTypes.length === 0 ? (
              <div className="flex justify-center py-4"><Spinner /></div>
            ) : (
              <div className="flex gap-3 overflow-x-auto pb-2 no-scrollbar">
                {vehicleTypes.map((vt, idx) => {
                  const est = estimatedPrices.find(e => e.vehicleTypeId === vt.vehicleTypeId)
                  return (
                  <button
                    key={vt.vehicleTypeId || idx}
                    onClick={() => setSelectedVType(vt)}
                    className={cn(
                      'card p-4 text-left transition-all duration-200 min-w-[140px] shrink-0 border border-surface-border',
                      selectedVType?.vehicleTypeId === vt.vehicleTypeId
                        ? 'border-brand-500 bg-brand-500/10 shadow-glow-green'
                        : 'hover:border-brand-500/30 bg-surface',
                    )}
                  >
                    <div className="flex items-start justify-between">
                      <img className="text-3xl mb-2 w-10 h-10 object-contain" src={vt.icon} />
                      {selectedVType?.vehicleTypeId === vt.vehicleTypeId && (
                        <RiCheckLine size={18} className="text-brand-400" />
                      )}
                    </div>
                    
                    {/* Tên loại xe */}
                    <p className="font-semibold text-content-main text-sm leading-tight">{vt.vehicleTypeName}</p>
                    
                    {/* Hiển thị số chỗ ngồi */}
                    {vt.maxPassengers && (
                      <p className="text-xs text-content-main/60 mt-0.5">
                        👤 {vt.maxPassengers}
                      </p>
                    )}

                    {/* Giá tiền */}
                    {est ? (
                      <div className="mt-2">
                        {est.originalPrice && est.originalPrice > est.totalPrice && (
                          <p className="text-[10px] text-content-muted line-through mb-0.5">
                            {formatCurrency(est.originalPrice)}
                          </p>
                        )}
                        <p className="text-xs font-semibold text-brand-400">
                          {formatCurrency(est.totalPrice)}
                        </p>
                      </div>
                    ) : vt.pricePerKm ? (
                      <p className="text-xs font-semibold text-brand-400 mt-2">
                        {formatCurrency(vt.pricePerKm)}/km
                      </p>
                    ) : null}
                  </button>
                  )
                })}
              </div>
            )}
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
             {/* Promo code */}
            <div className="space-y-3">
              <h3 className="font-semibold text-content-main text-sm flex items-center gap-2">
                <RiTicketLine size={16} className="text-brand-400" /> Khuyến mãi
              </h3>
              
              <button
                onClick={() => setPromoModalOpen(true)}
                className="w-full flex items-center justify-between p-3 rounded-xl border border-surface-border bg-surface hover:border-brand-500/50 hover:bg-surface-hover transition-all duration-200"
              >
                <div className="flex items-center gap-3">
                  <div className="w-8 h-8 rounded-full bg-brand-500/10 flex items-center justify-center">
                    <RiTicketLine size={18} className="text-brand-400" />
                  </div>
                  <div className="text-left">
                    {promoCode ? (
                      <>
                        <p className="text-sm font-semibold text-content-main">Mã: {promoCode}</p>
                        {promoData && <p className="text-xs text-brand-400 font-medium">Đã áp dụng giảm giá</p>}
                      </>
                    ) : (
                      <>
                        <p className="text-sm font-semibold text-content-main">Chọn mã khuyến mãi</p>
                        <p className="text-xs text-content-muted">Có {myPromotions.length} ưu đãi chờ bạn</p>
                      </>
                    )}
                  </div>
                </div>
                {promoCode ? (
                  <button 
                    onClick={(e) => { e.stopPropagation(); setPromoCode(''); setPromoData(null); }}
                    className="text-xs text-red-400 hover:text-red-300 font-medium"
                  >
                    Bỏ chọn
                  </button>
                ) : (
                  <span className="text-sm text-brand-400 font-medium">Chọn</span>
                )}
              </button>
            </div>

            {/* Payment method */}
            <div className="space-y-3">
              <h3 className="font-semibold text-content-main text-sm">Thanh toán</h3>
              <div className="flex gap-3">
                {[
                  { value: PAYMENT_METHOD.CASH,   label: 'Tiền mặt',  icon: RiMoneyDollarCircleLine },
                  { value: PAYMENT_METHOD.ONLINE,  label: 'Trực tuyến', icon: RiBankCardLine },
                ].map(({ value, label, icon: Icon }) => (
                  <button
                    key={value}
                    onClick={() => setPaymentMethod(value)}
                    className={cn(
                      'flex-1 p-3 rounded-xl border flex items-center justify-center gap-2 transition-all duration-200',
                      paymentMethod === value
                        ? 'border-brand-500 bg-brand-500/10 text-content-main'
                        : 'border-surface-border bg-surface text-content-muted hover:border-brand-500/30',
                    )}
                  >
                    <Icon size={18} className={paymentMethod === value ? 'text-brand-400' : ''} />
                    <span className="text-sm font-medium">{label}</span>
                  </button>
                ))}
              </div>

              {/* Online payment providers */}
              {paymentMethod === PAYMENT_METHOD.ONLINE && (
                <div className="grid grid-cols-2 gap-3 mt-3 animate-slide-up">
                  {['VNPAY', 'MOMO'].map((provider) => (
                    <button
                      key={provider}
                      onClick={() => setPaymentProvider(provider)}
                      className={cn(
                        'p-2 rounded-xl border flex items-center justify-center gap-2 transition-all duration-200',
                        paymentProvider === provider
                          ? 'border-brand-500 bg-brand-500/10 text-brand-400 font-semibold'
                          : 'border-surface-border bg-surface/50 text-content-muted hover:border-brand-500/30 hover:text-content-muted',
                      )}
                    >
                      <img 
                        src={provider === 'VNPAY' 
                          ? 'https://vnpay.vn/s1/statics.vnpay.vn/2023/6/0oxhzjmxbksr1686814746087.png' 
                          : 'https://cdn.haitrieu.com/wp-content/uploads/2022/10/Logo-MoMo-Square.png'
                        } 
                        alt={provider} 
                        className={cn('h-4 object-contain', provider === 'VNPAY' ? 'h-3' : 'h-4')} 
                      />
                      <span className="text-xs">{provider}</span>
                    </button>
                  ))}
                </div>
              )}
            </div>
          </div>

          {/* Price summary & Book */}
          <div className="bg-surface rounded-2xl p-5 border border-surface-border mt-4">
            <div className="flex items-center justify-between mb-4">
               <div>
                  <p className="text-xs text-content-muted mb-1">Khoảng cách: ~{formatDistance(selectedEstimate?.distance || DUMMY_DISTANCE)}</p>
                  <p className="text-lg font-display font-bold text-content-main">Tổng cộng</p>
               </div>
                <div className="text-right">
                  {estimating ? (
                     <Spinner size="sm" />
                  ) : (
                     <>
                        {isDiscounted && <p className="text-xs text-content-muted line-through mb-1">{formatCurrency(originalPrice)}</p>}
                        <p className="text-2xl font-display font-bold text-brand-400">{formatCurrency(finalPrice)}</p>
                     </>
                  )}
               </div>
            </div>
            
            {/* Countdown and Refresh button */}
            {!estimating && estimatedPrices.length > 0 && (
              <div className="flex items-center justify-between mb-4 px-2">
                <span className={cn("text-sm font-medium", countdown > 10 ? "text-brand-400" : "text-red-500 animate-pulse")}>
                  {countdown > 0 ? `Giá được giữ trong: ${countdown}s` : 'Giá đã hết hạn'}
                </span>
                {countdown <= 0 && (
                  <Button size="sm" variant="outline" onClick={handleEstimatePrice}>
                    Làm mới giá
                  </Button>
                )}
              </div>
            )}
            
            <Button fullWidth size="lg" onClick={handleBook} loading={loading} disabled={countdown <= 0}>
              Xác nhận đặt xe
            </Button>
          </div>
        </div>
      </div>

      {/* Promo Selection Modal */}
      <Modal isOpen={isPromoModalOpen} onClose={() => setPromoModalOpen(false)} title="Ví Voucher" size="md">
        <div className="space-y-4 pt-2">
          {/* Direct Input */}
          <div className="flex gap-2">
            <Input
              placeholder="Nhập mã khuyến mãi..."
              value={promoCode}
              onChange={(e) => setPromoCode(e.target.value.toUpperCase())}
              className="flex-1"
            />
            <Button 
              onClick={() => {
                applyPromo();
                setPromoModalOpen(false);
              }}
              disabled={promoLoading || !promoCode.trim()}
            >
              {promoLoading ? <Spinner size="sm" /> : 'Áp dụng'}
            </Button>
          </div>
          
          <div className="border-t border-surface-border my-4" />
          
          {/* Saved Promos List */}
          <div className="space-y-3 max-h-[350px] overflow-y-auto no-scrollbar pb-4">
            <h4 className="text-sm font-semibold text-content-main mb-2">Mã của bạn ({myPromotions.length})</h4>
            {myPromotions.length === 0 ? (
              <div className="text-center py-6 text-content-muted text-sm">
                Bạn chưa có mã khuyến mãi nào trong ví.
              </div>
            ) : (
              myPromotions.map(p => (
                <div 
                  key={p.promotionCode}
                  className={cn(
                    "p-3 rounded-xl border transition-all duration-200 cursor-pointer flex items-center justify-between",
                    promoCode === p.promotionCode 
                      ? "border-brand-500 bg-brand-500/10" 
                      : "border-surface-border bg-surface hover:border-brand-500/50"
                  )}
                  onClick={() => {
                    setPromoCode(p.promotionCode);
                    setTimeout(() => {
                      applyPromo();
                      setPromoModalOpen(false);
                    }, 100);
                  }}
                >
                  <div>
                    <p className="font-semibold text-brand-400 text-sm">Mã: {p.promotionCode}</p>
                    <p className="text-xs text-content-muted mt-0.5 max-w-[200px] truncate">{p.promotionName}</p>
                  </div>
                  <div className="shrink-0">
                    {promoCode === p.promotionCode ? (
                      <div className="w-6 h-6 rounded-full bg-brand-500 flex items-center justify-center text-white">
                        <RiCheckLine size={14} />
                      </div>
                    ) : (
                      <Button variant="outline" size="sm" className="text-xs py-1 h-auto">Dùng ngay</Button>
                    )}
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      </Modal>

    </div>
  )
}

export default BookingPage
