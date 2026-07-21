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
import BookingJourneyProgress from './booking/BookingJourneyProgress'
import { cn } from '@/utils/cn'
import { motion, AnimatePresence } from 'motion/react'

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
      .catch((error) => console.error('Estimate price failed', error))
      .finally(() => setEstimating(false))
  }, [pickup, dropoff, selectedPromos, vehicleTypes])

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
    <div className="relative flex min-h-[calc(100dvh-5rem)] flex-col overflow-hidden bg-[#e9ede7] dark:bg-surface-dark lg:h-[calc(100dvh-5rem)] lg:min-h-0 lg:flex-row">
      {/* Background Map - Always present, absolute on selectingLocationFor and step 3 */}
      <div className={cn(
        "z-0 pointer-events-auto",
        (selectingLocationFor || step === 3)
          ? "absolute inset-0" // Full screen
          : "relative order-1 h-[34dvh] min-h-64 shrink-0 lg:order-2 lg:h-full lg:min-h-0 lg:flex-1"
      )}>
        <MemoizedMap
          pickup={selectingLocationFor === 'pickup' ? null : (isValidLocation(pickup) ? pickup : { name: pickup?.name, ...DEFAULT_COORDINATES })}
          dropoff={selectingLocationFor === 'dropoff' ? null : (isValidLocation(dropoff) ? dropoff : { name: dropoff?.name, ...DEFAULT_COORDINATES })}
          selectingMode={!!selectingLocationFor}
          initialCenter={tempMapLocation ? [tempMapLocation.lat, tempMapLocation.lng] : null}
          onLocationSelect={selectingLocationFor ? handleLocationSelect : undefined}
        />
      </div>

      {/* Overlay UI Layer */}
      <div className={cn(
        "z-10 pointer-events-none flex flex-col",
        (selectingLocationFor || step === 3)
          ? "absolute inset-0"
          : cn(
              "relative order-2 min-h-[66dvh] w-full lg:order-1 lg:h-full lg:min-h-0 lg:flex-none",
              step === 2 ? "lg:w-[min(760px,65vw)]" : "lg:w-[470px]",
            )
      )}>
        {step === 1 && (
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

        <AnimatePresence>
          {step === 3 && (
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              className="absolute inset-0 pointer-events-none flex flex-col z-30"
            >
              <button
                onClick={handleCancelSearch}
                disabled={isCanceling}
                className="absolute top-6 left-6 pointer-events-auto z-10 w-12 h-12 bg-white/80 dark:bg-surface-card/80 backdrop-blur-xl rounded-full flex items-center justify-center shadow-lg border border-gray-200/50 dark:border-surface-border hover:bg-white dark:hover:bg-surface-muted transition-all active:scale-95"
              >
                {isCanceling ? <Spinner size="sm" /> : <RiArrowLeftLine size={24} className="text-gray-900 dark:text-white" />}
              </button>
              <div className="flex-1 min-h-0" />
              <motion.div
                initial={{ y: '100%' }}
                animate={{ y: 0 }}
                transition={{ type: 'spring', damping: 25, stiffness: 200 }}
                className="flex flex-col items-center justify-center rounded-t-3xl border-t border-white/20 bg-white/95 p-6 pb-8 shadow-[0_-20px_80px_rgba(0,0,0,0.2)] backdrop-blur-2xl dark:border-surface-border dark:bg-surface-card/95 pointer-events-auto"
              >
                <BookingJourneyProgress step={3} className="mb-5 max-w-md" />
                <div className="mb-3 flex items-center gap-2 rounded-full bg-brand-500/10 px-3 py-1.5 text-sm font-semibold text-brand-600 dark:text-brand-400">
                  <span className="h-2 w-2 animate-pulse rounded-full bg-brand-500" />
                  Đang kết nối
                </div>
                <div className="space-y-2 text-center">
                  <h3 className="font-display text-2xl font-bold text-content-main">Đang tìm tài xế gần bạn</h3>
                  <p className="text-sm font-medium text-content-muted">Giữ màn hình này mở, BookCar sẽ báo ngay khi có tài xế nhận chuyến.</p>
                </div>
                <Button variant="outline" size="lg" className="mt-6 w-full max-w-sm rounded-xl font-bold" onClick={handleCancelSearch} loading={isCanceling}>
                  Hủy yêu cầu
                </Button>
              </motion.div>
            </motion.div>
          )}
        </AnimatePresence>
      </div>
    </div>
  )
}

export default BookingPage
