import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { 
  RiHistoryLine, RiCouponLine, RiBankCardLine,
  RiHomeLine, RiBuildingLine, RiFlightTakeoffLine,
  RiStarLine, RiNavigationFill, RiTimeLine, RiMotorbikeFill
} from 'react-icons/ri'
import { useAuthStore, useBookingStore } from '@/store/rootStore'
import { masterDataApi } from '@/features/booking/api/masterDataApi'
import { bookingApi } from '@/features/booking/api/bookingApi'
import { formatCurrency } from '@/utils/currency'
import Button from '@/components/Elements/Button'

// Mock data
const MOCK_FAVORITES = [
  { id: 1, name: 'Nhà', icon: <RiHomeLine size={20} />, address: '123 Đường số 1, Quận 1' },
  { id: 2, name: 'Công ty', icon: <RiBuildingLine size={20} />, address: '456 Đường số 2, Quận 3' },
  { id: 3, name: 'Sân bay', icon: <RiFlightTakeoffLine size={20} />, address: 'Tân Sơn Nhất' },
]

const CustomerHomePage = () => {
  const navigate = useNavigate()
  const { user, userProfile } = useAuthStore()
  const { vehicleTypes, setVehicleTypes, currentBooking } = useBookingStore()
  
  const [recentTrips, setRecentTrips] = useState([])
  const [promotions, setPromotions] = useState([])

  useEffect(() => {
    masterDataApi.getActivePromotions()
      .then((data) => setPromotions(data || []))
      .catch(() => {})
  }, [])
  
  useEffect(() => {
    if (!vehicleTypes.length) {
      masterDataApi.getVehicleTypes()
        .then((types) => setVehicleTypes(types))
        .catch(() => {})
    }
    if (userProfile?.id) {
      bookingApi.getCustomerHistory(userProfile.id)
        .then((trips) => setRecentTrips(trips.slice(0, 3)))
        .catch(() => {})
    }
  }, [userProfile, vehicleTypes.length, setVehicleTypes])
  
  const greeting = "Chào bạn"
  
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
            <h1 className="text-lg font-bold text-gray-900 dark:text-white">
              {greeting}, {userProfile?.name || user?.userName?.split(' ')[0] || 'Khách hàng'}!
            </h1>
          </div>
          <div className="bg-white/90 dark:bg-surface-card/90 backdrop-blur-md px-4 py-2 rounded-full shadow-sm border border-gray-100 dark:border-surface-border flex items-center gap-2">
            <RiStarLine className="text-yellow-500" size={16} />
            <span className="font-semibold text-gray-900 dark:text-white text-sm">1,250 điểm</span>
          </div>
        </div>

        {/* 2. Floating Search Card (Where to?) */}
        <div className="bg-white dark:bg-surface-card rounded-2xl shadow-lg border border-gray-100 dark:border-surface-border p-5 mb-8">
          <h2 className="text-xl font-bold text-gray-900 dark:text-white mb-4">Bạn muốn đến đâu?</h2>
          <div 
            onClick={() => navigate('/customer/booking')}
            className="flex items-center h-14 w-full bg-gray-100 dark:bg-surface-dark rounded-xl px-4 cursor-text hover:bg-gray-200 dark:hover:bg-surface-border transition-colors"
          >
            <div className="w-8 h-8 flex items-center justify-center shrink-0 mr-3 text-brand-500">
              <RiNavigationFill size={20} />
            </div>
            <div className="flex-1 text-left">
              <span className="text-gray-500 dark:text-gray-400 font-medium text-lg">Tìm điểm đến...</span>
            </div>
          </div>
        </div>

        {/* 3. Main Services Grid (Image based) */}
        <div className="grid grid-cols-4 gap-3 md:gap-6 mb-10">
          {/* Car Ride */}
          <div 
            onClick={() => navigate('/customer/booking')}
            className="flex flex-col items-center gap-2 cursor-pointer group"
          >
            <div className="w-full aspect-square bg-white dark:bg-surface-card rounded-2xl shadow-sm border border-gray-100 dark:border-surface-border flex items-center justify-center p-3 group-hover:shadow-md group-active:scale-95 transition-all overflow-hidden">
              <img src="/assets/images/icon_car.jpg" alt="Car" className="w-full h-full object-contain mix-blend-multiply dark:mix-blend-normal rounded-xl" />
            </div>
            <span className="text-sm font-semibold text-gray-800 dark:text-gray-200">Ô tô</span>
          </div>
          
          {/* Bike Ride */}
          <div 
            onClick={() => navigate('/customer/booking')}
            className="flex flex-col items-center gap-2 cursor-pointer group"
          >
            <div className="w-full aspect-square bg-white dark:bg-surface-card rounded-2xl shadow-sm border border-gray-100 dark:border-surface-border flex items-center justify-center p-3 group-hover:shadow-md group-active:scale-95 transition-all overflow-hidden">
              <img src="/assets/images/icon_bike.jpg" alt="Bike" className="w-full h-full object-contain mix-blend-multiply dark:mix-blend-normal rounded-xl" />
            </div>
            <span className="text-sm font-semibold text-gray-800 dark:text-gray-200">Xe máy</span>
          </div>

          {/* Payment */}
          <div 
            onClick={() => navigate('/customer/payment')}
            className="flex flex-col items-center gap-2 cursor-pointer group"
          >
            <div className="w-full aspect-square bg-white dark:bg-surface-card rounded-2xl shadow-sm border border-gray-100 dark:border-surface-border flex items-center justify-center p-3 group-hover:shadow-md group-active:scale-95 transition-all overflow-hidden">
              <img src="/assets/images/icon_payment.jpg" alt="Payment" className="w-full h-full object-contain mix-blend-multiply dark:mix-blend-normal rounded-xl scale-110" />
            </div>
            <span className="text-sm font-semibold text-gray-800 dark:text-gray-200">Thanh toán</span>
          </div>

          {/* Promos */}
          <div 
            onClick={() => navigate('/customer/promotions')}
            className="flex flex-col items-center gap-2 cursor-pointer group"
          >
            <div className="w-full aspect-square bg-white dark:bg-surface-card rounded-2xl shadow-sm border border-gray-100 dark:border-surface-border flex items-center justify-center p-3 group-hover:shadow-md group-active:scale-95 transition-all overflow-hidden">
              <img src="/assets/images/icon_promo.jpg" alt="Promo" className="w-full h-full object-contain mix-blend-multiply dark:mix-blend-normal rounded-xl" />
            </div>
            <span className="text-sm font-semibold text-gray-800 dark:text-gray-200">Ưu đãi</span>
          </div>
        </div>

        {/* 4. Practical Layout below: Promo Banner & Recents */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          
          {/* Left Col */}
          <div className="lg:col-span-2 space-y-6">
            
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
                <div className="absolute inset-0 bg-gradient-to-r from-black/70 to-transparent flex flex-col justify-end p-6">
                  <h4 className="text-white text-xl md:text-2xl font-bold mb-2">Giảm 50% chuyến đầu</h4>
                  <p className="text-white/90 mb-4 text-sm md:text-base">Mã ưu đãi: BOOKCAR50</p>
                  <div>
                    <span className="bg-brand-500 text-white px-4 py-2 rounded-lg font-bold text-sm">Dùng ngay</span>
                  </div>
                </div>
              </div>
            </div>

            {/* Saved Places */}
            <div className="bg-white dark:bg-surface-card rounded-2xl p-6 shadow-sm border border-gray-100 dark:border-surface-border">
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-lg font-bold text-gray-900 dark:text-white">Địa điểm yêu thích</h3>
                <button className="text-brand-500 font-semibold text-sm hover:underline">Thêm mới</button>
              </div>
              <div className="space-y-1 mt-2">
                {MOCK_FAVORITES.map((place) => (
                  <div 
                    key={place.id} 
                    onClick={() => navigate('/customer/booking')}
                    className="flex items-center gap-4 cursor-pointer group py-3 border-b border-gray-50 dark:border-surface-border last:border-0"
                  >
                    <div className="w-10 h-10 rounded-full bg-gray-100 dark:bg-surface-dark flex items-center justify-center text-gray-500 dark:text-gray-400 group-hover:bg-brand-50 dark:group-hover:bg-brand-500/10 group-hover:text-brand-500 transition-colors">
                      {place.icon}
                    </div>
                    <div className="flex-1">
                      <h4 className="font-semibold text-gray-900 dark:text-white">{place.name}</h4>
                      <p className="text-sm text-gray-500 dark:text-gray-400 truncate">{place.address}</p>
                    </div>
                  </div>
                ))}
              </div>
            </div>

          </div>

          {/* Right Col: Recent Rides */}
          <div className="space-y-6">
            <div className="bg-white dark:bg-surface-card rounded-2xl p-6 shadow-sm border border-gray-100 dark:border-surface-border h-full">
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
                        className="mt-4 w-full py-2 bg-gray-100 dark:bg-surface-dark text-gray-900 dark:text-white hover:bg-brand-500 hover:text-white border border-gray-200 dark:border-surface-border rounded-xl text-sm font-semibold transition-colors"
                        disabled={!!currentBooking}
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
    </div>
  )
}

export default CustomerHomePage
