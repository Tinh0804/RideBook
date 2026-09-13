import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { 
  RiHistoryLine, RiCouponLine, RiBankCardLine,
  RiHomeLine, RiBuildingLine, RiFlightTakeoffLine,
  RiStarLine, RiNavigationFill, RiTimeLine, RiMotorbikeFill,
  RiMapPinLine
} from 'react-icons/ri'
import { useAuthStore, useBookingStore } from '@/store/rootStore'
import { masterDataApi } from '@/features/booking/api/masterDataApi'
import { bookingApi } from '@/features/booking/api/bookingApi'
import { favoritePlaceApi } from '@/features/customer/api/favoritePlaceApi'
import { loyaltyApi } from '@/features/loyalty/api/loyaltyApi'
import { formatCurrency } from '@/utils/currency'
import Button from '@/components/Elements/Button'
import { BOOKING_STATUS } from '@/config'
import { RiCarLine, RiMapPin2Fill } from 'react-icons/ri'

const CustomerHomePage = () => {
  const navigate = useNavigate()
  const { user, userProfile } = useAuthStore()
  const { vehicleTypes, setVehicleTypes, currentBooking } = useBookingStore()
  
  const [recentTrips, setRecentTrips] = useState([])
  const [queuedTrips, setQueuedTrips] = useState([])
  const [promotions, setPromotions] = useState([])
  const [favoritePlaces, setFavoritePlaces] = useState([])
  const [loyaltyAccount, setLoyaltyAccount] = useState(null)
  const [activeHistoryTrips, setActiveHistoryTrips] = useState([])

  useEffect(() => {
    masterDataApi.getActivePromotions()
      .then((data) => setPromotions(data || []))
      .catch(() => {})
      
    favoritePlaceApi.getMyFavoritePlaces()
      .then((data) => setFavoritePlaces(data || []))
      .catch(() => {})
      
    if (user?.id) {
      loyaltyApi.getMyAccount(user.id)
        .then((data) => setLoyaltyAccount(data))
        .catch(() => {})
    }
  }, [user?.id])
  
  useEffect(() => {
    if (!vehicleTypes.length) {
      masterDataApi.getVehicleTypes()
        .then((types) => setVehicleTypes(types))
        .catch(() => {})
    }
    if (userProfile?.id) {
      bookingApi.getCustomerHistory(userProfile.id)
        .then((trips) => {
          if (Array.isArray(trips)) {
            setRecentTrips(trips.slice(0, 3))
            setQueuedTrips(trips.filter(t => t.bookingStatus === BOOKING_STATUS.QUEUED))
            
            // Tìm tất cả chuyến đi đang hoạt động từ API
            const activeStatuses = [BOOKING_STATUS.PENDING, BOOKING_STATUS.ACCEPTED, BOOKING_STATUS.IN_PROGRESS, BOOKING_STATUS.PICKED_UP, BOOKING_STATUS.QUEUED]
            const activeTrips = trips.filter(t => activeStatuses.includes(t.bookingStatus))
            setActiveHistoryTrips(activeTrips)
          }
        })
        .catch(() => {})
    }
  }, [userProfile, vehicleTypes.length, setVehicleTypes])
  
  const greeting = "Chào bạn"
  
  // Danh sách chuyến đi hiển thị trên overlay
  const activeTripsMap = new Map()
  if (currentBooking) {
    activeTripsMap.set(currentBooking.bookingId, currentBooking)
  }
  activeHistoryTrips.forEach(t => {
    if (!activeTripsMap.has(t.bookingId)) {
      activeTripsMap.set(t.bookingId, t)
    }
  })
  const overlayTrips = Array.from(activeTripsMap.values())

  
  return (
    <div className="bg-[#e8ece3] dark:bg-surface-dark min-h-screen pb-20 w-full relative">
      
      {/* 1. Map Header Background */}
      <div className="absolute top-0 left-0 right-0 h-[45vh] lg:h-[55vh] z-0 overflow-hidden pointer-events-none">
        <img 
          src="/assets/images/map_bg.jpg" 
          alt="Map Background" 
          className="w-full h-full object-cover opacity-100 dark:opacity-30"
        />
        {/* Gradient fade to bottom */}
        <div className="absolute inset-0 bg-gradient-to-b from-transparent via-[#e8ece3]/50 to-[#e8ece3] dark:via-surface-dark/50 dark:to-surface-dark" />
      </div>

      <div className="relative z-10 max-w-5xl mx-auto w-full px-4 pt-6 lg:pt-10">
        
        {/* User Greeting & Status */}
        <div className="flex items-center justify-between mb-8">
          <div className="bg-white/90 dark:bg-surface-card/90 backdrop-blur-md px-4 py-2 rounded-full shadow-sm border border-gray-100 dark:border-surface-border">
            <h1 className="text-fluid-lg font-bold text-gray-900 dark:text-white">
              {greeting}, {userProfile?.name || user?.userName?.split(' ')[0] || 'Khách hàng'}!
            </h1>
          </div>
          <button 
            onClick={() => navigate('/customer/loyalty')}
            className="bg-white/90 dark:bg-surface-card/90 backdrop-blur-md px-4 py-2 rounded-full shadow-sm border border-gray-100 dark:border-surface-border flex items-center gap-2 hover:bg-gray-50 dark:hover:bg-surface-border transition-colors active:scale-95"
          >
            <RiStarLine className="text-yellow-500" size={16} />
            <span className="font-semibold text-gray-900 dark:text-white text-sm">
              {loyaltyAccount ? loyaltyAccount.currentPoints.toLocaleString() : '0'} xu
            </span>
          </button>
        </div>

        {/* 2. Floating Search Card (Where to?) */}
        <div className="bg-white dark:bg-surface-card rounded-2xl shadow-lg border border-gray-100 dark:border-surface-border p-fluid mb-8">
          <h2 className="text-fluid-xl font-bold text-gray-900 dark:text-white mb-4">Bạn muốn đến đâu?</h2>
          <div 
            onClick={() => navigate('/customer/booking')}
            className="flex items-center h-11 sm:h-14 w-full bg-gray-100 dark:bg-surface-dark rounded-xl px-4 cursor-text hover:bg-gray-200 dark:hover:bg-surface-border transition-colors mb-4"
          >
            <div className="w-8 h-8 flex items-center justify-center shrink-0 mr-3 text-brand-500">
              <RiNavigationFill size={20} />
            </div>
            <div className="flex-1 text-left">
              <span className="text-gray-500 dark:text-gray-400 font-medium text-lg">Tìm điểm đến...</span>
            </div>
          </div>
          
          {/* Favorite Places Quick Actions */}
          {favoritePlaces.length > 0 && (
            <div className="flex items-center gap-3 overflow-x-auto pb-2 scrollbar-hide">
              {favoritePlaces.map(place => (
                <button
                  key={place.favoritePlaceId}
                  onClick={() => navigate('/customer/booking', { state: { dropoff: { name: place.label, address: place.address } } })}
                  className="flex items-center gap-2 bg-gray-50 dark:bg-surface-dark/50 hover:bg-gray-100 dark:hover:bg-surface-dark border border-gray-200 dark:border-surface-border rounded-full px-4 py-2 shrink-0 transition-colors"
                >
                  <div className="text-brand-500">
                    {place.icon === 'HOME' ? <RiHomeLine size={16} /> : 
                     place.icon === 'WORK' ? <RiBuildingLine size={16} /> :
                     place.icon === 'TRAVEL' ? <RiFlightTakeoffLine size={16} /> :
                     <RiMapPinLine size={16} />}
                  </div>
                  <span className="text-sm font-semibold text-gray-800 dark:text-gray-200">{place.label}</span>
                </button>
              ))}
            </div>
          )}
        </div>

        {/* 3. Main Services Grid (Image based) */}
        <div className="grid grid-cols-4 gap-fluid mb-10">
          {/* Car Ride */}
          <div 
            onClick={() => navigate('/customer/booking')}
            className="flex flex-col items-center gap-2 cursor-pointer group"
          >
            <div className="w-full aspect-square bg-white dark:bg-surface-card rounded-2xl shadow-sm border border-gray-100 dark:border-surface-border flex items-center justify-center p-2 sm:p-3 group-hover:shadow-md group-active:scale-95 transition-all overflow-hidden">
              <img src="/assets/images/icon_car.jpg" alt="Car" className="w-full h-full object-contain mix-blend-multiply dark:mix-blend-normal rounded-xl" />
            </div>
            <span className="text-fluid-sm font-semibold text-gray-800 dark:text-gray-200">Ô tô</span>
          </div>
          
          {/* Bike Ride */}
          <div 
            onClick={() => navigate('/customer/booking')}
            className="flex flex-col items-center gap-2 cursor-pointer group"
          >
            <div className="w-full aspect-square bg-white dark:bg-surface-card rounded-2xl shadow-sm border border-gray-100 dark:border-surface-border flex items-center justify-center p-2 sm:p-3 group-hover:shadow-md group-active:scale-95 transition-all overflow-hidden">
              <img src="/assets/images/icon_bike.jpg" alt="Bike" className="w-full h-full object-contain mix-blend-multiply dark:mix-blend-normal rounded-xl" />
            </div>
            <span className="text-fluid-sm font-semibold text-gray-800 dark:text-gray-200">Xe máy</span>
          </div>

          {/* Payment */}
          <div 
            onClick={() => navigate('/customer/payment')}
            className="flex flex-col items-center gap-2 cursor-pointer group"
          >
            <div className="w-full aspect-square bg-white dark:bg-surface-card rounded-2xl shadow-sm border border-gray-100 dark:border-surface-border flex items-center justify-center p-2 sm:p-3 group-hover:shadow-md group-active:scale-95 transition-all overflow-hidden">
              <img src="/assets/images/icon_payment.jpg" alt="Payment" className="w-full h-full object-contain mix-blend-multiply dark:mix-blend-normal rounded-xl scale-110" />
            </div>
            <span className="text-fluid-sm font-semibold text-gray-800 dark:text-gray-200">Thanh toán</span>
          </div>

          {/* Promos */}
          <div 
            onClick={() => navigate('/customer/promotions')}
            className="flex flex-col items-center gap-2 cursor-pointer group"
          >
            <div className="w-full aspect-square bg-white dark:bg-surface-card rounded-2xl shadow-sm border border-gray-100 dark:border-surface-border flex items-center justify-center p-2 sm:p-3 group-hover:shadow-md group-active:scale-95 transition-all overflow-hidden">
              <img src="/assets/images/icon_promo.jpg" alt="Promo" className="w-full h-full object-contain mix-blend-multiply dark:mix-blend-normal rounded-xl" />
            </div>
            <span className="text-fluid-sm font-semibold text-gray-800 dark:text-gray-200">Ưu đãi</span>
          </div>
        </div>

        {/* 4. Practical Layout below: Promo Banner & Recents */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          
          {/* Left Col */}
          <div className="lg:col-span-2 space-y-6">
            
            {/* Scheduled Trips (QUEUED) Alert */}
            {queuedTrips.length > 0 && (
              <div className="bg-purple-500/10 dark:bg-purple-900/20 border border-purple-500/20 rounded-2xl p-6 shadow-sm mb-6">
                <div className="flex items-center justify-between mb-4">
                  <h3 className="text-lg font-bold text-purple-900 dark:text-purple-100">Chuyến đi đã hẹn giờ</h3>
                  <button onClick={() => navigate('/customer/history')} className="text-purple-700 dark:text-purple-300 font-semibold text-sm hover:underline">Xem tất cả</button>
                </div>
                <div className="space-y-4">
                  {queuedTrips.slice(0, 2).map((trip) => (
                    <div key={trip.bookingId} className="bg-white dark:bg-surface-card rounded-xl p-4 shadow-sm border border-purple-100 dark:border-purple-800/30">
                      <div className="flex justify-between items-start mb-2">
                        <span className="text-sm font-bold text-purple-700 dark:text-purple-400">
                          Đón lúc: {new Date(trip.scheduledAt).toLocaleString('vi-VN', { hour: '2-digit', minute: '2-digit', day: '2-digit', month: '2-digit' })}
                        </span>
                        <span className="text-sm font-bold text-gray-900 dark:text-white">
                          {formatCurrency(trip.totalPrice)}
                        </span>
                      </div>
                      <div className="flex items-start gap-3 mt-3">
                        <div className="mt-1 flex flex-col items-center">
                          <div className="w-2 h-2 rounded-full bg-brand-500" />
                          <div className="w-px h-6 bg-gray-300 dark:bg-surface-border my-1" />
                          <div className="w-2 h-2 rounded-full bg-blue-500" />
                        </div>
                        <div className="flex-1 min-w-0">
                          <p className="text-sm text-gray-500 dark:text-gray-400 truncate mb-2">{trip.pickupLocation}</p>
                          <p className="text-sm font-medium text-gray-900 dark:text-white truncate">{trip.dropoffLocation}</p>
                        </div>
                      </div>
                      <div className="mt-4 flex gap-2">
                        <Button
                          onClick={() => navigate(`/customer/tracking/${trip.bookingId}`)}
                          className="w-full py-2 bg-purple-100 dark:bg-purple-900/40 text-purple-700 dark:text-purple-300 hover:bg-purple-200 dark:hover:bg-purple-800 border border-purple-200 dark:border-purple-700/50 rounded-xl text-sm font-bold transition-colors"
                        >
                          Chi tiết
                        </Button>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Promo Banner Realistic */}
            <div>
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-lg font-bold text-gray-900 dark:text-white">Khuyến mãi cho bạn</h3>
                <button onClick={() => navigate('/customer/promotions')} className="text-brand-500 font-semibold text-sm">Xem tất cả</button>
              </div>
              <div 
                onClick={() => navigate('/customer/promotions')}
                className="w-full rounded-2xl overflow-hidden shadow-sm cursor-pointer relative group aspect-[21/9]"
              >
                <img 
                  src="/assets/images/promo_banner.jpg" 
                  alt="Promotion" 
                  className="w-full h-full object-cover transition-transform duration-500 group-hover:scale-105"
                />
                <div className="absolute inset-0 bg-gradient-to-r from-black/70 to-transparent flex flex-col justify-end p-fluid">
                  <h4 className="text-white text-fluid-xl md:text-fluid-2xl font-bold mb-2">Giảm 50% chuyến đầu</h4>
                  <p className="text-white/90 mb-4 text-fluid-sm md:text-fluid-base">Mã ưu đãi: BOOKCAR50</p>
                  <div>
                    <span className="bg-brand-500 text-white px-fluid py-2 rounded-lg font-bold text-fluid-sm">Dùng ngay</span>
                  </div>
                </div>
              </div>
            </div>

          </div>

          {/* Right Col: Recent Rides */}
          <div className="space-y-6">
            <div className="bg-white dark:bg-surface-card rounded-2xl p-fluid shadow-sm border border-gray-100 dark:border-surface-border h-full">
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-lg font-bold text-gray-900 dark:text-white">Chuyến đi gần đây</h3>
                <button onClick={() => navigate('/customer/history')} className="text-brand-500 font-semibold text-sm hover:underline">Tất cả</button>
              </div>

              {recentTrips.length > 0 ? (
                <div className="space-y-5">
                  {recentTrips.map((trip) => (
                    <div key={trip.bookingId} className="group">
                      <div className="flex items-start justify-between mb-2">
                        <div className="flex items-center gap-1.5 text-xs text-gray-500 dark:text-gray-400">
                          <RiTimeLine size={14} />
                          {new Date(trip.bookingTime).toLocaleDateString('vi-VN')}
                        </div>
                        <span className="text-sm font-bold text-gray-900 dark:text-white">
                          {formatCurrency(trip.totalPrice)}
                        </span>
                      </div>
                      
                      <div className="flex items-start gap-3">
                        <div className="mt-1 flex flex-col items-center">
                          <div className="w-2 h-2 rounded-full bg-brand-500" />
                          <div className="w-px h-6 bg-gray-300 dark:bg-surface-border my-1" />
                          <div className="w-2 h-2 rounded-full bg-blue-500" />
                        </div>
                        <div className="flex-1 min-w-0">
                          <p className="text-sm text-gray-500 dark:text-gray-400 truncate mb-3">{trip.pickupLocation}</p>
                          <p className="text-sm font-medium text-gray-900 dark:text-white truncate">{trip.dropoffLocation}</p>
                        </div>
                      </div>
                      
                      <Button
                        onClick={() => navigate('/customer/booking', { state: { pickup: { name: trip.pickupLocation }, dropoff: { name: trip.dropoffLocation } } })}
                        className="mt-4 w-full py-fluid bg-gray-100 dark:bg-surface-dark text-gray-900 dark:text-white hover:bg-brand-500 hover:text-white border border-gray-200 dark:border-surface-border rounded-xl text-fluid-sm font-semibold transition-colors"
                      >
                        Đặt lại chuyến
                      </Button>
                      <div className="border-b border-gray-100 dark:border-surface-border mt-5 last:hidden" />
                    </div>
                  ))}
                </div>
              ) : (
                <div className="text-center py-10">
                  <div className="w-12 h-12 rounded-full bg-gray-100 dark:bg-surface-dark flex items-center justify-center mx-auto mb-3">
                    <RiHistoryLine size={24} className="text-gray-400 dark:text-gray-500" />
                  </div>
                  <p className="text-gray-500 dark:text-gray-400 text-sm">Chưa có chuyến đi nào.</p>
                </div>
              )}
            </div>
          </div>

        </div>
      </div>

      {/* Floating Active Trip Overlay */}
      {overlayTrips.length > 0 && (
        <div className="fixed bottom-20 right-4 lg:right-8 z-50 pointer-events-none w-[calc(100%-2rem)] md:w-auto flex flex-col gap-4 items-end">
          {overlayTrips.map(overlayTrip => (
            <div 
              key={overlayTrip.bookingId}
              onClick={() => {
                if (!currentBooking || currentBooking.bookingId !== overlayTrip.bookingId) {
                  useBookingStore.getState().setCurrentBooking(overlayTrip)
                }
                navigate(`/customer/tracking/${overlayTrip.bookingId}`)
              }}
              className="w-full md:w-[340px] max-w-[95vw] bg-white/95 dark:bg-surface-card/95 backdrop-blur-md rounded-2xl shadow-xl border border-brand-500/30 p-fluid cursor-pointer pointer-events-auto flex flex-col gap-4 hover:shadow-2xl transition-all active:scale-[0.98]"
            >
              <div className="flex items-center justify-between gap-3">
                <div className="flex items-center gap-3 min-w-0">
                  <div className="w-12 h-12 rounded-full bg-brand-50 dark:bg-brand-500/20 flex items-center justify-center shrink-0">
                    <RiCarLine size={24} className="text-brand-600 dark:text-brand-400" />
                  </div>
                  <div className="flex-1 min-w-0">
                    <h4 className="font-bold text-gray-900 dark:text-white text-[15px]">
                      {overlayTrip.bookingStatus === BOOKING_STATUS.QUEUED ? 'Chuyến xe đã hẹn' : 'Chuyến xe đang diễn ra'}
                    </h4>
                    <span className="text-xs font-semibold text-brand-600 dark:text-brand-400">Chạm để xem chi tiết</span>
                  </div>
                </div>
              </div>
              
              <div className="bg-gray-50 dark:bg-gray-800/50 rounded-xl p-3 flex flex-col gap-2">
                <div className="flex items-start gap-2 text-sm text-gray-700 dark:text-gray-300">
                  <RiMapPin2Fill size={16} className="text-red-500 shrink-0 mt-0.5" />
                  <span className="line-clamp-2 leading-relaxed">{overlayTrip.dropoffLocation}</span>
                </div>
                
                {overlayTrip.driver && (
                  <div className="flex items-center gap-2 mt-2 pt-2 border-t border-gray-200 dark:border-gray-700">
                    <div className="w-7 h-7 rounded-full bg-gray-200 dark:bg-gray-700 overflow-hidden shrink-0">
                      <img 
                        src={overlayTrip.driver.avatar || `https://ui-avatars.com/api/?name=${encodeURIComponent(overlayTrip.driver.fullName)}&background=random`} 
                        alt="Driver" 
                        className="w-full h-full object-cover" 
                      />
                    </div>
                    <div className="flex-1 min-w-0 text-sm">
                      <p className="font-semibold text-gray-900 dark:text-white truncate">{overlayTrip.driver.fullName}</p>
                      <p className="text-xs text-gray-500 dark:text-gray-400 font-medium">{overlayTrip.driver.licensePlate}</p>
                    </div>
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

    </div>
  )
}

export default CustomerHomePage
