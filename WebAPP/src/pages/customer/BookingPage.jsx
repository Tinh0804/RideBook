import { useState, useEffect } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import toast from 'react-hot-toast'
import {
  RiMapPinLine, RiMapPin2Line, RiTicketLine,
  RiBankCardLine, RiMoneyDollarCircleLine, RiCheckLine,
  RiArrowLeftLine
} from 'react-icons/ri'
import { useAuthStore, useBookingStore } from '@/store/rootStore'
import { bookingApi } from '@/features/booking/api/bookingApi'
import { masterDataApi } from '@/features/booking/api/masterDataApi'
import { paymentApi } from '@/features/payment/api/paymentApi'
import { formatCurrency, formatDistance } from '@/utils/currency'
import { PAYMENT_METHOD, BOOKING_STATUS } from '@/config'
import Button from '@/components/Elements/Button'
import AddressInput from '@/components/Map/AddressInput'
import Input from '@/components/Elements/Input'
import Spinner from '@/components/Elements/Spinner'
import LocationAutocomplete from '@/components/Elements/LocationAutocomplete'
import InteractiveMap from '@/components/Map/InteractiveMap'
import { cn } from '@/utils/cn'

const DUMMY_DISTANCE = 5.2  // km - in real app use Google Maps/OSRM Distance Matrix

const BookingPage = () => {
  const navigate = useNavigate()
  const location = useLocation()
  const { user }  = useAuthStore()
  const { vehicleTypes, setVehicleTypes, currentBooking, setCurrentBooking, setEstimatedPrice, estimatedPrice } = useBookingStore()

  // Prevent accessing booking page if there is an active booking that is NOT PENDING
  useEffect(() => {
    if (currentBooking && currentBooking.bookingStatus !== BOOKING_STATUS.PENDING) {
      navigate('/customer/tracking', { replace: true })
    }
  }, [currentBooking, navigate])

  // Locations state: objects with { name, lat, lng }
  const [pickup,          setPickup]          = useState(null)
  const [pickupDetail,    setPickupDetail]    = useState('')
  const [dropoff,         setDropoff]         = useState(null)
  const [dropoffDetail,   setDropoffDetail]   = useState('')
  
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
        pickupLocation:  pickupDetail ? `${pickupDetail.trim()}, ${pickup.name}` : pickup.name,
        dropoffLocation: dropoffDetail ? `${dropoffDetail.trim()}, ${dropoff.name}` : dropoff.name,
        distance:        selectedEstimate?.distance || DUMMY_DISTANCE,
        vehicleTypeId:   selectedVType.vehicleTypeId,
        promotionCode:   promoData?.promotionCode || null,
        quoteId:         selectedEstimate?.quoteId,
      }
      const booking = await bookingApi.createBooking(payload)
      setCurrentBooking(booking)

      if (paymentMethod === PAYMENT_METHOD.ONLINE) {
        let pmData
        if (paymentProvider === 'VNPAY') {
          pmData = await paymentApi.createVNPayUrl({ bookingId: booking.bookingId, amount: selectedEstimate?.totalPrice || 0 })
        } else {
          pmData = await paymentApi.createMomoUrl({ bookingId: booking.bookingId, amount: selectedEstimate?.totalPrice || 0 })
        }
        const url = pmData?.paymentUrl || pmData?.payUrl // Handle momo vs vnpay differences
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
    if (currentBooking && currentBooking.bookingStatus === BOOKING_STATUS.PENDING && step !== 3) {
      setStep(3)
    }
  }, [currentBooking, step])

  if (step === 1) {
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
                  {isValidLocation(pickup) && (
                    <div className="mt-2 animate-slide-up">
                      <Input
                        placeholder="Số nhà, tòa nhà, ngõ... (tùy chọn)"
                        value={pickupDetail}
                        onChange={(e) => setPickupDetail(e.target.value)}
                        className="bg-surface/50 border-surface-border text-sm"
                      />
                    </div>
                  )}
              
            
              </div>
            </div>

            <div className="flex items-center gap-4">
              <div className="w-8 h-8 rounded-full bg-red-500/20 flex items-center justify-center shrink-0 shadow-[0_0_15px_rgba(239,68,68,0.2)]">
                <RiMapPin2Line size={18} className="text-red-400" />
              </div>
              <div className="flex-1">
                <p className="text-xs text-content-muted mb-1 ml-1">Điểm đến</p>
                <LocationAutocomplete 
                  placeholder="Điểm đến của bạn"
                  value={dropoff?.name || ''}
                  onChange={(name) => {
                    setDropoff({ name })
                  }}
                  onLocationDetect={(locationData) => {
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
                {isValidLocation(dropoff) && (
                  <div className="mt-2 animate-slide-up">
                    <Input
                      placeholder="Số nhà, tòa nhà, ngõ... (tùy chọn)"
                      value={dropoffDetail}
                      onChange={(e) => setDropoffDetail(e.target.value)}
                      className="bg-surface/50 border-surface-border text-sm"
                    />
                  </div>
                )}
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
          onClick={() => {
            // Note: Currently we just go back to step 2 visually.
            // Ideally we should call a cancel API here to stop broadcasting
            setStep(2)
          }}
          className="absolute top-4 left-4 z-10 w-10 h-10 bg-surface-card/90 backdrop-blur-md rounded-full flex items-center justify-center shadow-lg border border-surface-border hover:bg-surface-muted transition-colors"
        >
          <RiArrowLeftLine size={20} className="text-content-main" />
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
            onClick={() => {
              // Add cancel logic
              setStep(2)
            }} 
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
                      <p className="text-xs font-semibold text-brand-400 mt-2">
                        {formatCurrency(est.totalPrice)}
                      </p>
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
              <div className="flex gap-2">
                <Input
                  placeholder="Nhập mã..."
                  value={promoCode}
                  onChange={(e) => setPromoCode(e.target.value.toUpperCase())}
                  className="flex-1 bg-surface border-surface-border"
                />
                <Button variant="outline" onClick={applyPromo} loading={promoLoading}>
                  Áp dụng
                </Button>
              </div>
              {promoData && (
                <div className="flex items-center gap-2 text-xs text-brand-400 bg-brand-500/10 border border-brand-500/20 rounded-lg px-3 py-2">
                  <RiCheckLine size={14} />
                  Giảm {promoData.discountLimit * 100}%
                </div>
              )}
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
    </div>
  )
}


export default BookingPage
