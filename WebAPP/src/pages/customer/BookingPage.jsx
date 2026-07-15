import React, { useState, useEffect, useCallback } from 'react'
import { useLocation, useNavigate, useSearchParams } from 'react-router-dom'
import toast from 'react-hot-toast'
import { RiArrowLeftLine } from 'react-icons/ri'
import { useAuthStore, useBookingStore } from '@/store/rootStore'
import { useWebSocket } from '@/hooks/useWebSocket'
import { bookingApi } from '@/features/booking/api/bookingApi'
import { masterDataApi } from '@/features/booking/api/masterDataApi'
import { PAYMENT_METHOD, BOOKING_STATUS } from '@/config'
import Spinner from '@/components/Elements/Spinner'
import InteractiveMap from '@/components/Map/InteractiveMap'
import Button from '@/components/Elements/Button'
import LocationSelectionStep from './booking/LocationSelectionStep'
import VehicleSelectionStep from './booking/VehicleSelectionStep'
import { cn } from '@/utils/cn'

const DUMMY_DISTANCE = 5.2
const DEFAULT_COORDINATES = { lat: 16.0544, lng: 108.2022 }
const isValidLocation = (loc) => loc && typeof loc.lat === 'number' && typeof loc.lng === 'number'

// Memoized InteractiveMap to prevent unmounting or unnecessary re-renders
const MemoizedMap = React.memo(InteractiveMap, (prev, next) => {
  return prev.pickup?.lat === next.pickup?.lat &&
         prev.pickup?.lng === next.pickup?.lng &&
         prev.dropoff?.lat === next.dropoff?.lat &&
         prev.dropoff?.lng === next.dropoff?.lng &&
         prev.selectingMode === next.selectingMode &&
         prev.initialCenter?.[0] === next.initialCenter?.[0] &&
         prev.initialCenter?.[1] === next.initialCenter?.[1] &&
         prev.onLocationSelect === next.onLocationSelect
})

const BookingPage = () => {
  const navigate = useNavigate()
  const location = useLocation()
  const [searchParams, setSearchParams] = useSearchParams()
  const { user, userProfile }  = useAuthStore()
  const { vehicleTypes, setVehicleTypes, currentBooking, setCurrentBooking, clearCurrentBooking } = useBookingStore()

  const customerId = userProfile?.id || user?.id

  // ── Locations state ───────────────────────
  const [pickup, setPickup] = useState(() => {
    const saved = localStorage.getItem('temp_pickup')
    return saved ? JSON.parse(saved) : null
  })
  const [dropoff, setDropoff] = useState(() => {
    const saved = localStorage.getItem('temp_dropoff')
    return saved ? JSON.parse(saved) : null
  })

  useEffect(() => {
    if (pickup) localStorage.setItem('temp_pickup', JSON.stringify(pickup))
    else localStorage.removeItem('temp_pickup')
  }, [pickup])

  useEffect(() => {
    if (dropoff) localStorage.setItem('temp_dropoff', JSON.stringify(dropoff))
    else localStorage.removeItem('temp_dropoff')
  }, [dropoff])

  const [selectingLocationFor, setSelectingLocationFor] = useState(null)
  const [tempMapLocation, setTempMapLocation] = useState(null)
  const [mapLoading, setMapLoading] = useState(false)
  
  const [step, setStep] = useState(() => {
    const saved = localStorage.getItem('temp_step')
    return saved ? parseInt(saved, 10) : 1
  })

  useEffect(() => {
    localStorage.setItem('temp_step', step)
  }, [step])

  const [selectedVType, setSelectedVType] = useState(null)
  const [paymentMethod, setPaymentMethod] = useState(PAYMENT_METHOD.CASH)
  const [paymentProvider, setPaymentProvider] = useState('VNPAY')
  const [selectedPromos, setSelectedPromos] = useState([])
  const [loading, setLoading] = useState(false)
  const [estimating, setEstimating] = useState(false)
  const [estimatedPrices, setEstimatedPrices] = useState([])
  const [countdown, setCountdown] = useState(0)
  const [isCanceling, setCanceling] = useState(false)
  const [myPromotions, setMyPromotions] = useState([])

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
    if (step === 2 && (!pickup || !dropoff)) setStep(1)
    if (step === 3 && !currentBooking) setStep(1)
  }, [step, pickup, dropoff, currentBooking])

  useEffect(() => {
    if (step === 2 && customerId) {
      masterDataApi.getMyPromotions(customerId)
        .then(data => {
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
          const sorted = [...types].sort((a, b) => (a.maxPassengers || 0) - (b.maxPassengers || 0))
          setVehicleTypes(sorted)
          if (sorted.length) setSelectedVType(sorted[0])
        })
        .catch(() => {})
    } else if (!selectedVType && vehicleTypes.length) {
      const sorted = [...vehicleTypes].sort((a, b) => (a.maxPassengers || 0) - (b.maxPassengers || 0))
      setSelectedVType(sorted[0])
    }
  }, [vehicleTypes, selectedVType, setVehicleTypes])

  const handleEstimatePrice = useCallback(() => {
    if (!pickup || !dropoff) return
    const sp = isValidLocation(pickup) ? pickup : { ...pickup, lat: 10.8231, lng: 106.6297 }
    const sd = isValidLocation(dropoff) ? dropoff : { ...dropoff, lat: 10.8231, lng: 106.6297 }
    setEstimating(true)
    const payload = { 
      pickupLat: sp.lat,
      pickupLng: sp.lng,
      dropoffLat: sd.lat,
      dropoffLng: sd.lng,
      promotionCodes: selectedPromos.map(p => p.promotionCode),
    }
    bookingApi.estimatePrice(payload)
      .then((estimates) => {
        if (Array.isArray(estimates)) {
          setEstimatedPrices(estimates)
          setCountdown(120)
        }
      })
      .catch((e) => {
        console.error("=== API ERROR ===", e)
      })
      .finally(() => setEstimating(false))
  }, [pickup, dropoff, selectedPromos])

  useEffect(() => {
    if (step !== 2) return
    handleEstimatePrice()
  }, [step, handleEstimatePrice])

  useEffect(() => {
    if (countdown <= 0) return
    const timer = setInterval(() => setCountdown(c => c - 1), 1000)
    return () => clearInterval(timer)
  }, [countdown])

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
        promotionCodes:  selectedPromos.map(p => p.promotionCode),
        quoteId:         selectedEstimate?.quoteId,
        returnUrl: `${window.location.origin}/customer/booking`
      }
      const booking = await bookingApi.createBooking(payload)
      setCurrentBooking(booking)

      if (pickup) localStorage.setItem(`booking_pickup_${booking.bookingId}`, JSON.stringify(pickup))
      if (dropoff) localStorage.setItem(`booking_dropoff_${booking.bookingId}`, JSON.stringify(dropoff))

      if (paymentMethod === PAYMENT_METHOD.ONLINE && booking.paymentUrl) {
        window.location.href = booking.paymentUrl
      } else {
        toast.success('Đặt xe thành công! Đang tìm tài xế...')
        setStep(3)
      }
    } catch (error) {
      toast.error(error.response?.data?.message || 'Có lỗi xảy ra khi đặt xe')
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
      toast.success('Đã huỷ tìm kiếm')
      setStep(1)
    } catch (err) {
      toast.error('Không thể huỷ lúc này, vui lòng thử lại')
    } finally {
      setCanceling(false)
    }
  }

  useEffect(() => {
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
          setSearchParams({}, { replace: true })
        } else {
          toast.error('Thanh toán thất bại hoặc bị huỷ.')
        }
      }).catch(err => {
        console.error('Failed to fetch booking after payment', err)
      })
      return
    }

    if (currentBooking && currentBooking.bookingStatus === BOOKING_STATUS.PENDING && step !== 3) {
      const isOnline = currentBooking.paymentMethod === 'ONLINE'
      const isPaid = currentBooking.paymentStatus === true || location.state?.paymentSuccess || vnpStatus === '00' || momoCode === '0'
      if (!isOnline || isPaid) {
        setStep(3)
      }
    }
  }, [currentBooking, step, location.state, searchParams, setCurrentBooking])

  // --- Map Select Location logic ---
  const openMapSelection = (type) => {
    setSelectingLocationFor(type)
    setMapLoading(type)
    if (type === 'pickup' && pickup?.lat) setTempMapLocation(pickup)
    else if (type === 'dropoff' && dropoff?.lat) setTempMapLocation(dropoff)
    else {
      navigator.geolocation.getCurrentPosition(
        (pos) => setTempMapLocation({ lat: pos.coords.latitude, lng: pos.coords.longitude }),
        () => setTempMapLocation({ lat: 10.8231, lng: 106.6297 }),
        { timeout: 5000 }
      )
    }
    setMapLoading(false)
  }
  
  // Memoize the map callbacks so MemoizedMap doesn't re-render unless necessary
  const handleLocationSelect = useCallback((data) => {
    setTempMapLocation(data)
  }, [])

  // Derived state for Step 2
  const selectedEstimate = estimatedPrices.find(e => e.vehicleTypeId === selectedVType?.vehicleTypeId)
  const originalPrice = selectedEstimate?.originalPrice || 0
  const finalPrice = selectedEstimate?.totalPrice || 0
  const isDiscounted = originalPrice > finalPrice

  return (
    <div className="-m-6 h-[calc(100vh-64px)] relative flex flex-col lg:flex-row bg-surface-dark overflow-hidden">
      
      {/* Background Map - Always present, absolute on selectingLocationFor and step 3 */}
      <div className={cn(
        "z-0 pointer-events-auto",
        (selectingLocationFor || step === 3) 
          ? "absolute inset-0" // Full screen
          : "relative flex-1 order-1 lg:order-2 h-[50vh] lg:h-full" // Split screen
      )}>
        <MemoizedMap 
          pickup={selectingLocationFor === 'pickup' ? null : (isValidLocation(pickup) ? pickup : { name: pickup?.name, ...DEFAULT_COORDINATES })} 
          dropoff={selectingLocationFor === 'dropoff' ? null : (isValidLocation(dropoff) ? dropoff : { name: dropoff?.name, ...DEFAULT_COORDINATES })} 
          selectingMode={!!selectingLocationFor}
          initialCenter={tempMapLocation ? [tempMapLocation.lat, tempMapLocation.lng] : null}
          onLocationSelect={selectingLocationFor ? handleLocationSelect : undefined}
        />
        {/* Nút Back overlay cho Mobile trong Step 2 */}
        {step === 2 && !selectingLocationFor && (
          <button 
            onClick={() => setStep(1)}
            className="lg:hidden absolute top-4 left-4 z-10 w-10 h-10 bg-surface-card/90 backdrop-blur-md rounded-full flex items-center justify-center shadow-lg border border-surface-border hover:bg-surface-muted transition-colors"
          >
            <RiArrowLeftLine size={20} className="text-content-main" />
          </button>
        )}
      </div>

      {/* Overlay UI Layer */}
      <div className={cn(
        "z-10 pointer-events-none flex flex-col",
        (selectingLocationFor || step === 3) 
          ? "absolute inset-0"
          : "relative w-full lg:flex-1 h-[55vh] lg:h-full order-2 lg:order-1"
      )}>
        {step === 1 && selectingLocationFor && (
          <LocationSelectionStep
            pickup={pickup}
            setPickup={setPickup}
            dropoff={dropoff}
            setDropoff={setDropoff}
            selectingLocationFor={selectingLocationFor}
            setSelectingLocationFor={setSelectingLocationFor}
            tempMapLocation={tempMapLocation}
            setTempMapLocation={setTempMapLocation}
            mapLoading={mapLoading}
            openMapSelection={openMapSelection}
            handleNextStep={() => setStep(2)}
          />
        )}

        {step === 1 && !selectingLocationFor && (
          <LocationSelectionStep
            pickup={pickup}
            setPickup={setPickup}
            dropoff={dropoff}
            setDropoff={setDropoff}
            selectingLocationFor={selectingLocationFor}
            setSelectingLocationFor={setSelectingLocationFor}
            tempMapLocation={tempMapLocation}
            setTempMapLocation={setTempMapLocation}
            mapLoading={mapLoading}
            openMapSelection={openMapSelection}
            handleNextStep={() => setStep(2)}
          />
        )}

        {step === 2 && (
          <VehicleSelectionStep
            setStep={setStep}
            vehicleTypes={vehicleTypes}
            selectedVType={selectedVType}
            setSelectedVType={setSelectedVType}
            estimatedPrices={estimatedPrices}
            selectedPromos={selectedPromos}
            setSelectedPromos={setSelectedPromos}
            myPromotions={myPromotions}
            paymentMethod={paymentMethod}
            setPaymentMethod={setPaymentMethod}
            paymentProvider={paymentProvider}
            setPaymentProvider={setPaymentProvider}
            estimating={estimating}
            handleEstimatePrice={handleEstimatePrice}
            countdown={countdown}
            handleBook={handleBook}
            loading={loading}
            originalPrice={originalPrice}
            finalPrice={finalPrice}
            isDiscounted={isDiscounted}
            selectedEstimate={selectedEstimate}
          />
        )}

        {step === 3 && (
          <>
            <button 
              onClick={handleCancelSearch}
              disabled={isCanceling}
              className="absolute top-4 left-4 pointer-events-auto z-10 w-10 h-10 bg-surface-card/90 backdrop-blur-md rounded-full flex items-center justify-center shadow-lg border border-surface-border hover:bg-surface-muted transition-colors"
            >
              {isCanceling ? <Spinner size="sm" /> : <RiArrowLeftLine size={20} className="text-content-main" />}
            </button>
            <div className="flex-1 min-h-0" />
            <div className="bg-surface-card rounded-t-3xl shadow-[0_-10px_40px_rgba(0,0,0,0.5)] border-t border-surface-border p-6 flex flex-col items-center justify-center space-y-6 pointer-events-auto pb-10">
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
              <Button variant="outline" fullWidth onClick={handleCancelSearch} loading={isCanceling}>
                Hủy tìm kiếm
              </Button>
            </div>
          </>
        )}
      </div>
    </div>
  )
}

export default BookingPage
