import { useState, useEffect, useRef } from 'react'
import { useNavigate } from 'react-router-dom'
import { 
  RiMapPinLine, RiMapPin2Line, RiArrowRightLine, RiHistoryLine, 
  RiCarLine, RiCouponLine, RiHeartLine, RiBankCardLine,
  RiHomeLine, RiBuildingLine, RiGraduationCapLine, RiFlightTakeoffLine,
  RiStoreLine, RiMap2Line, RiTimerLine, RiStarLine, RiUserStarLine,
  RiBellLine, RiPercentLine, RiShieldCheckLine, RiHeadphoneLine,
  RiFacebookLine, RiInstagramLine, RiYoutubeLine, RiMessageLine,
  RiThumbUpLine, RiWalletLine, RiTrophyLine, RiRoadMapLine,
  RiShoppingCartLine  // Thay thế cho RiBoxLine
} from 'react-icons/ri'
import { useAuthStore, useBookingStore } from '@/store/rootStore'
import LocationInput  from '@/components/Map/AddressInput'
import  AddressInput from '@/components/Map/AddressInput'
import { masterDataApi,ratingApi } from '@/features/booking/api/masterDataApi'
import { bookingApi } from '@/features/booking/api/bookingApi'
import { formatCurrency } from '@/utils/currency'
import { formatDate } from '@/utils/formatDate'
import Button from '@/components/Elements/Button'
import Input from '@/components/Elements/Input'
import { cn } from '@/utils/cn'

// Mock data for demonstration
const MOCK_FAVORITES = [
  { id: 1, name: 'Nhà', icon: <RiHomeLine size={24} />, address: '123 Đường số 1, Quận 1' },
  { id: 2, name: 'Công ty', icon: <RiBuildingLine size={24} />, address: '456 Đường số 2, Quận 3' },
  { id: 3, name: 'Trường học', icon: <RiGraduationCapLine size={24} />, address: 'ĐH Bách Khoa, Quận 10' },
  { id: 4, name: 'Sân bay', icon: <RiFlightTakeoffLine size={24} />, address: 'Sân bay Tân Sơn Nhất' },
  { id: 5, name: 'Trung tâm thương mại', icon: <RiStoreLine size={24} />, address: 'Vincom Đồng Khởi' },
]

const PROMO_COLORS = [
  'from-pink-500 to-rose-500',
  'from-blue-500 to-cyan-500',
  'from-green-500 to-emerald-500',
  'from-orange-500 to-red-500',
  'from-purple-500 to-fuchsia-500'
]
const MOCK_NOTIFICATIONS = [
  { id: 1, type: 'promotion', title: 'Khuyến mãi đặc biệt', message: 'Nhận ngay 50% cho chuyến đi đầu tiên', time: '2 giờ trước', icon: <RiPercentLine /> },
  { id: 2, type: 'trip', title: 'Hoàn thành chuyến đi', message: 'Bạn đã hoàn thành chuyến đi đến Quận 1', time: 'Hôm qua', icon: <RiCarLine /> },
  { id: 3, type: 'voucher', title: 'Voucher mới', message: 'Bạn có 1 voucher giảm 20,000đ', time: '2 ngày trước', icon: <RiCouponLine /> },
]

const CustomerHomePage = () => {
  const navigate = useNavigate()
  const { user, userProfile } = useAuthStore()
  const { vehicleTypes, setVehicleTypes, currentBooking } = useBookingStore()
  
  const [pickup, setPickup] = useState('')
  const [dropoff, setDropoff] = useState('')
  const [recentTrips, setRecentTrips] = useState([])
  const [currentSlide, setCurrentSlide] = useState(0)
  const [ratings, setRatings] = useState([])
  const [weather, setWeather] = useState({ temp: 28, condition: 'Nắng', icon: '☀️' })
  const [currentTime, setCurrentTime] = useState(new Date())
const [avgScore, setAvgScore] = useState(0)   // ← thêm dòng này
  const [pickupLocation, setPickupLocation] = useState(null) // Lưu tọa độ điểm đón
  const [dropoffLocation, setDropoffLocation] = useState(null) // Lưu tọa độ điểm đến
  const [scoreDistribution, setScoreDistribution] = useState({1:0,2:0,3:0,4:0,5:0})
  const [promotions, setPromotions] = useState([])

  
  // Fetch promotions
  useEffect(() => {
    masterDataApi.getActivePromotions()
      .then((data) => setPromotions(data || []))
      .catch(() => {})
  }, [])

  // Auto-rotate promotions
  useEffect(() => {
    if (promotions.length <= 1) return;
    const interval = setInterval(() => {
      setCurrentSlide((prev) => (prev + 1) % promotions.length)
    }, 5000)
    return () => clearInterval(interval)
  }, [promotions.length])
  
  // Update time every minute
  useEffect(() => {
    const interval = setInterval(() => setCurrentTime(new Date()), 60000)
    return () => clearInterval(interval)
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
      
      ratingApi.getByCustomer(userProfile.id)
          .then((ratingsList) => {
            setRatings(ratingsList.slice(0, 3))
            if (ratingsList.length > 0) {
              const total = ratingsList.reduce((sum, r) => sum + r.score, 0)
              setAvgScore(total / ratingsList.length)
              
              // Calculate distribution
              const dist = {1:0,2:0,3:0,4:0,5:0}
              ratingsList.forEach(r => {
                const s = Math.floor(r.score)
                if (s >= 1 && s <= 5) dist[s]++
              })
              setScoreDistribution(dist)
            } else {
              setAvgScore(0)
              setScoreDistribution({1:0,2:0,3:0,4:0,5:0})
            }
          })
          .catch(() => {})
    }

   
  }, [userProfile, vehicleTypes.length, setVehicleTypes])
  
  const handleBook = () => {
    if (currentBooking) {
      navigate('/customer/tracking')
      return
    }
    if (!pickup.trim() || !dropoff.trim()) return
    
    // Chuyển đến trang booking với đầy đủ thông tin bao gồm tọa độ
    navigate('/customer/booking', { 
      state: { 
        pickup: { 
          name: pickup,
          location: pickupLocation // Có tọa độ nếu đã định vị
        }, 
        dropoff: { 
          name: dropoff,
          location: dropoffLocation // Có tọa độ nếu đã định vị
        } 
      } 
    })
  }
  
  // const handleLocationDetect = (location, type) => {
  //   // Lưu tọa độ khi phát hiện vị trí
  //   if (type === 'pickup') {
  //     setPickupLocation(location)
  //     console.log('Điểm đón:', location)
  //   } else if (type === 'dropoff') {
  //     setDropoffLocation(location)
  //     console.log('Điểm đến:', location)
  //   }
  // }

  const formatTime = (dateInput) => {
    if (!dateInput) return '';
    const date = new Date(dateInput);
    if (isNaN(date.getTime())) return String(dateInput);
    return date.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })
  }
  
  const formatDate = (dateInput) => {
    if (!dateInput) return '';
    const date = new Date(dateInput);
    if (isNaN(date.getTime())) return String(dateInput);
    return date.toLocaleDateString('vi-VN', { weekday: 'long', day: 'numeric', month: 'numeric' })
  }
  
  const hour = new Date().getHours()
  const greeting = hour < 12 ? 'Chào buổi sáng' : hour < 18 ? 'Chào buổi chiều' : 'Chào buổi tối'
  
  return (
    <div className="space-y-8 max-w-7xl mx-auto px-4 pb-8">
      {/* Welcome Section with Gradient Background */}
      <div className="relative overflow-hidden rounded-2xl bg-gradient-to-br from-brand-600 via-brand-500 to-brand-700 p-6 md:p-8 shadow-2xl">
        {/* Animated background elements */}
        <div className="absolute top-0 right-0 w-64 h-64 bg-white/5 rounded-full blur-3xl animate-pulse"></div>
        <div className="absolute bottom-0 left-0 w-48 h-48 bg-white/5 rounded-full blur-2xl animate-pulse delay-1000"></div>
        
        <div className="relative overflow-hidden rounded-2xl bg-gradient-to-br from-brand-600 via-brand-500 to-brand-700 p-6 md:p-8 shadow-2xl">
          {/* Animated background elements */}
          <div className="absolute top-0 right-0 w-64 h-64 bg-white/5 rounded-full blur-3xl animate-pulse"></div>
          <div className="absolute bottom-0 left-0 w-48 h-48 bg-white/5 rounded-full blur-2xl animate-pulse delay-1000"></div>
          
          {/* Mini map/illustration */}
          <div className="absolute bottom-0 right-0 opacity-10">
            <svg width="200" height="200" viewBox="0 0 200 200" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M100 20 L180 60 L180 140 L100 180 L20 140 L20 60 L100 20Z" stroke="white" strokeWidth="2" fill="none"/>
              <circle cx="100" cy="100" r="10" fill="white"/>
            </svg>
          </div>
          
          <div className="relative z-10">
            <div className="flex justify-between items-start flex-wrap gap-4">
              <div className="space-y-2">
                <p className="text-white/90 text-lg flex items-center gap-2">
                  {greeting} <span className="text-2xl inline-block animate-wave">👋</span>
                </p>
                <h1 className="font-display text-4xl md:text-5xl font-bold text-white">
                  {userProfile?.name || user?.userName || 'Bạn'}
                </h1>
                <p className="text-white/80 text-lg mt-2 flex items-center gap-2">
                  {weather.icon} {weather.temp}°C • {weather.condition}
                  <span className="w-1 h-1 bg-white/50 rounded-full"></span>
                  🕐 {formatTime(currentTime)}
                </p>
                <p className="text-white/90 text-xl mt-1 font-medium">
                  "Bạn muốn đi đâu hôm nay?" ✨
                </p>
              </div>
              
              {/* Quick stats */}
              <div className="bg-white/10 backdrop-blur-sm rounded-xl p-4">
                <div className="text-white/80 text-sm">Thành viên</div>
                <div className="text-white font-semibold text-lg">⭐ Hạng Bạch Kim</div>
                <div className="text-white/70 text-xs mt-1">1,250 điểm thưởng</div>
              </div>
            </div>
          </div>
        </div>

        
        </div>
      <div 
          id="booking-card" 
          onClick={() => navigate('/customer/booking')}
          className="card p-6 md:p-8 space-y-4 shadow-xl hover:shadow-2xl transition-all duration-300 cursor-pointer group"
        >
          <h2 className="font-semibold text-content-main text-xl">🚗 Đặt chuyến ngay</h2>
          
          {/* Search input style - looks like a text field but redirects on click */}
          <div className="relative">
            
            <div className="flex items-center gap-3 p-4 rounded-xl bg-surface border border-surface-border group-hover:border-brand-500/50 transition-all duration-300">
              <div className="w-10 h-10 rounded-full bg-brand-500/15 border border-brand-500/30 flex items-center justify-center shrink-0">
                <RiMapPinLine size={20} className="text-brand-400" />
              </div>
              <div className="flex-1">
                <p className="text-sm text-content-muted mb-1">Điểm đến</p>
                <Input
                  value={dropoff}
                  onChange={(e) => setDropoff(e.target.value)}
                  placeholder="Bạn muốn đi đâu?"
                  className="bg-transparent border-none p-0 text-content-main focus:ring-0 focus:outline-none"
                />
              
              </div>
              <div className="w-8 h-8 rounded-full bg-brand-500/10 flex items-center justify-center group-hover:bg-brand-500/20 transition-all duration-300">
                <RiArrowRightLine size={18} className="text-brand-400" />
              </div>
            </div>
          </div>
          
          <p className="text-xs text-content-muted text-center mt-2">
            ✨ Nhấn để tìm kiếm điểm đến và đặt xe
          </p>
        </div>
      
      {/* Quick Action Cards Grid */}
      <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
        {[
          { icon: <RiCarLine size={28} />, label: 'Đặt xe', color: 'from-blue-500 to-blue-600', onClick: () => navigate('/customer/booking') },
          { icon: <RiShoppingCartLine size={28} />, label: 'Giao hàng', color: 'from-green-500 to-green-600', onClick: () => navigate('/customer/delivery') },
          { icon: <RiHistoryLine size={28} />, label: 'Lịch sử', color: 'from-purple-500 to-purple-600', onClick: () => navigate('/customer/history') },
          { icon: <RiCouponLine size={28} />, label: 'Voucher', color: 'from-pink-500 to-pink-600', onClick: () => navigate('/customer/promotions') },
          { icon: <RiHeartLine size={28} />, label: 'Yêu thích', color: 'from-red-500 to-red-600', onClick: () => document.getElementById('favorites')?.scrollIntoView({ behavior: 'smooth' }) },
          { icon: <RiBankCardLine size={28} />, label: 'Thanh toán', color: 'from-cyan-500 to-cyan-600', onClick: () => navigate('/customer/payment') },
        ].map((action) => (
          <button
            key={action.label}
            onClick={action.onClick}
            className="group relative overflow-hidden border border-surface-border rounded-xl bg-gradient-to-br p-4 text-white shadow-lg transition-all duration-300 hover:scale-105 hover:shadow-2xl"
            style={{ backgroundImage: `linear-gradient(135deg, ${action.color})` }}
          >
            <div className="absolute inset-0 bg-white/20 translate-y-full group-hover:translate-y-0 transition-transform duration-300"></div>
            <div className="relative z-10 space-y-2 text-center">
              <div className="flex justify-center">{action.icon}</div>
              <p className="font-semibold text-sm">{action.label}</p>
            </div>
          </button>
        ))}
      </div>
      
      
      {/* Promotion Carousel */}
      {promotions.length > 0 && (
        <div className="space-y-3">
          <h3 className="text-sm font-semibold text-content-muted uppercase tracking-wider">🎁 Khuyến mãi hấp dẫn</h3>
          <div className="relative overflow-hidden rounded-2xl">
            <div 
              className="flex transition-transform duration-500 ease-out"
              style={{ transform: `translateX(-${currentSlide * 100}%)` }}
            >
              {promotions.map((promo, idx) => (
                <div key={promo.promotionId || idx} className="w-full flex-shrink-0 px-1">
                  <div className={cn("relative overflow-hidden rounded-2xl bg-gradient-to-r p-6 md:p-8 text-white shadow-xl", PROMO_COLORS[idx % PROMO_COLORS.length])}>
                    <div className="absolute top-0 right-0 w-32 h-32 bg-white/10 rounded-full -mr-16 -mt-16"></div>
                    <div className="relative z-10">
                      <h3 className="text-2xl md:text-3xl font-bold mb-2">
                        {promo.promotionName || `Giảm giá đến ${formatCurrency(promo.discountLimit)}`}
                      </h3>
                      <p className="text-white/90 mb-4">Mã khuyến mãi: <strong>{promo.promotionCode}</strong></p>
                      <div className="flex items-center justify-between flex-wrap gap-3">
                        <span className="text-sm bg-white/20 px-3 py-1 rounded-full">
                          {promo.endTime ? `⏰ HSD: ${formatDate(promo.endTime)}` : '⏰ Không giới hạn'}
                        </span>
                        <button 
                          onClick={() => {
                            navigator.clipboard.writeText(promo.promotionCode)
                            navigate('/customer/booking')
                          }}
                          className="bg-white text-gray-900 px-4 py-2 rounded-lg font-semibold hover:scale-105 transition-transform"
                        >
                          Lưu & Đặt ngay →
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
            
            {/* Carousel dots */}
            {promotions.length > 1 && (
              <div className="absolute bottom-4 left-1/2 -translate-x-1/2 flex gap-2">
                {promotions.map((_, idx) => (
                  <button
                    key={idx}
                    onClick={() => setCurrentSlide(idx)}
                    className={cn(
                      "w-2 h-2 rounded-full transition-all duration-300",
                      currentSlide === idx ? "bg-white w-6" : "bg-white/50 hover:bg-white/75"
                    )}
                  />
                ))}
              </div>
            )}
          </div>
        </div>
      )}
      
      {/* Two Column Layout for Recent Trips & Favorite Places */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Recent Trips Section */}
        {recentTrips.length > 0 && (
          <div className="space-y-3">
            <div className="flex items-center justify-between">
              <h3 className="text-sm font-semibold text-content-muted uppercase tracking-wider">🕘 Chuyến gần đây</h3>
              <button onClick={() => navigate('/customer/history')} className="text-xs text-brand-400 hover:text-brand-300 flex items-center gap-1">
                Xem tất cả <RiArrowRightLine size={12} />
              </button>
            </div>
            <div className="space-y-3">
              {recentTrips.map((trip) => (
                <div key={trip.bookingId} className="card p-4 hover:shadow-xl transition-all duration-300 group">
                  <div className="flex items-start gap-4">
                    <div className="w-12 h-12 rounded-xl bg-gradient-to-br from-brand-500/20 to-brand-500/5 flex items-center justify-center shrink-0">
                      <RiHistoryLine size={20} className="text-brand-400" />
                    </div>
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center justify-between mb-1">
                        <p className="text-sm font-semibold text-content-main truncate">{trip.dropoffLocation}</p>
                        <span className="text-xs px-2 py-0.5 rounded-full bg-green-500/10 text-green-600">Hoàn thành</span>
                      </div>
                      <p className="text-xs text-content-muted truncate">Từ: {trip.pickupLocation}</p>
                      <p className="text-xs text-content-muted mt-1">🕐 {new Date(trip.bookingTime).toLocaleDateString('vi-VN')}</p>
                    </div>
                    <div className="text-right shrink-0">
                      <p className="text-base font-bold text-brand-500">{formatCurrency(trip.totalPrice)}</p>
                      <button
                        onClick={() => navigate('/customer/booking', { state: { pickup: { name: trip.pickupLocation }, dropoff: { name: trip.dropoffLocation } } })}
                        className="text-xs text-content-muted hover:text-brand-400 transition-colors mt-1"
                        disabled={!!currentBooking}
                      >
                        Đặt lại →
                      </button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
        
        
        <div id="favorites" className="space-y-3">
          <h3 className="text-sm font-semibold text-content-muted uppercase tracking-wider">⭐ Địa điểm yêu thích</h3>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            {MOCK_FAVORITES.map((place) => (
              <div key={place.id} className="card p-4 hover:shadow-xl transition-all duration-300 group">
                <div className="flex items-start gap-3">
                  <div className="w-10 h-10 rounded-xl bg-brand-500/10 flex items-center justify-center text-brand-500">
                    {place.icon}
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="font-semibold text-content-main">{place.name}</p>
                    <p className="text-xs text-content-muted truncate">{place.address}</p>
                  </div>
                  <button
                    onClick={() => {
                      setPickup(place.address)
                      document.getElementById('booking-card')?.scrollIntoView({ behavior: 'smooth' })
                    }}
                    className="text-xs bg-brand-500/10 text-brand-500 px-3 py-1 rounded-lg hover:bg-brand-500 hover:text-white transition-colors"
                  >
                    Đặt
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
      
      {/* Nearby Drivers & Statistics Row */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Nearby Drivers Section */}
        <div className="space-y-3">
          <h3 className="text-sm font-semibold text-content-muted uppercase tracking-wider">🚖 Tài xế quanh bạn</h3>
          <div className="relative rounded-2xl overflow-hidden bg-gradient-to-br from-gray-900 to-gray-800 h-64">
            {/* Mini Map Simulation */}
            <div className="absolute inset-0 opacity-20">
              <svg width="100%" height="100%" viewBox="0 0 400 300" xmlns="http://www.w3.org/2000/svg">
                <path d="M0,150 L400,150" stroke="white" strokeWidth="1" fill="none"/>
                <path d="M200,0 L200,300" stroke="white" strokeWidth="1" fill="none"/>
                <rect x="50" y="100" width="30" height="30" fill="white" opacity="0.3"/>
                <rect x="300" y="150" width="40" height="40" fill="white" opacity="0.3"/>
              </svg>
            </div>
            
            {/* Driver markers */}
            {[
              { x: 30, y: 40, delay: 0 },
              { x: 60, y: 70, delay: 1 },
              { x: 80, y: 45, delay: 2 },
              { x: 150, y: 120, delay: 0.5 },
              { x: 250, y: 80, delay: 1.5 },
            ].map((driver, idx) => (
              <div
                key={idx}
                className="absolute animate-bounce"
                style={{ left: `${driver.x}%`, top: `${driver.y}%`, animationDelay: `${driver.delay}s` }}
              >
                <div className="w-8 h-8 bg-brand-500 rounded-full flex items-center justify-center shadow-lg">
                  <div className="w-3 h-3 bg-white rounded-full"></div>
                </div>
              </div>
            ))}
            
            {/* Center marker (user) */}
            <div className="absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2">
              <div className="w-10 h-10 bg-blue-500 rounded-full flex items-center justify-center shadow-xl ring-4 ring-blue-300">
                <div className="w-4 h-4 bg-white rounded-full"></div>
              </div>
            </div>
            
            {/* Floating info card */}
            <div className="absolute bottom-4 left-4 right-4 bg-white/95 backdrop-blur-sm rounded-xl p-3 shadow-lg">
              <div className="flex justify-between items-center">
                <div>
                  <p className="text-xs text-content-muted">Tài xế gần bạn</p>
                  <p className="text-lg font-bold text-content-main">12 tài xế</p>
                </div>
                <div className="text-right">
                  <p className="text-xs text-content-muted">Thời gian chờ TB</p>
                  <p className="text-lg font-bold text-green-600">3 phút</p>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        {/* User Statistics Dashboard */}
        <div className="space-y-3">
          <h3 className="text-sm font-semibold text-content-muted uppercase tracking-wider">📊 Thống kê của bạn</h3>
          <div className="card p-6 space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div className="text-center p-3 bg-gradient-to-br from-brand-50 to-brand-100 dark:from-brand-900/20 dark:to-brand-800/20 rounded-xl">
                <RiCarLine className="mx-auto text-brand-500 mb-2" size={24} />
                <p className="text-2xl font-bold text-content-main">24</p>
                <p className="text-xs text-content-muted">Tổng chuyến</p>
              </div>
              <div className="text-center p-3 bg-gradient-to-br from-green-50 to-green-100 dark:from-green-900/20 dark:to-green-800/20 rounded-xl">
                <RiRoadMapLine className="mx-auto text-green-600 mb-2" size={24} />
                <p className="text-2xl font-bold text-content-main">342</p>
                <p className="text-xs text-content-muted">Tổng km</p>
              </div>
              <div className="text-center p-3 bg-gradient-to-br from-purple-50 to-purple-100 dark:from-purple-900/20 dark:to-purple-800/20 rounded-xl">
                <RiWalletLine className="mx-auto text-purple-600 mb-2" size={24} />
                <p className="text-2xl font-bold text-content-main">2.4tr</p>
                <p className="text-xs text-content-muted">Tổng chi</p>
              </div>
              <div className="text-center p-3 bg-gradient-to-br from-yellow-50 to-yellow-100 dark:from-yellow-900/20 dark:to-yellow-800/20 rounded-xl">
                <RiTrophyLine className="mx-auto text-yellow-600 mb-2" size={24} />
                <p className="text-2xl font-bold text-content-main">1,250</p>
                <p className="text-xs text-content-muted">Điểm thưởng</p>
              </div>
            </div>
            
            {/* Membership progress */}
            <div className="space-y-2">
              <div className="flex justify-between text-sm">
                <span className="text-content-muted">Hạng Bạch Kim</span>
                <span className="text-content-muted">750/1000 điểm</span>
              </div>
              <div className="h-2 bg-surface-border rounded-full overflow-hidden">
                <div className="h-full w-3/4 bg-gradient-to-r from-brand-500 to-brand-600 rounded-full"></div>
              </div>
              <p className="text-xs text-content-muted text-center">Còn 250 điểm để lên hạng Kim Cương</p>
            </div>
          </div>
        </div>
      </div>
      
      
      
      {/* Reviews / Rating Section */}
      <div className="space-y-3">
        <h3 className="text-sm font-semibold text-content-muted uppercase tracking-wider">⭐ Đánh giá của bạn</h3>
        <div className="card p-6">
          <div className="flex items-center justify-between flex-wrap gap-4">
            {/* Điểm trung bình */}
            <div className="text-center">
              <div className="text-4xl font-bold text-content-main">
                {avgScore > 0 ? avgScore.toFixed(1) : '—'}
              </div>
              <div className="flex text-yellow-400 mt-1">
                {[1,2,3,4,5].map((star) => (
                  <RiStarLine 
                    key={star} 
                    className={star <= Math.round(avgScore) ? "text-yellow-400 fill-current" : "text-gray-300"}
                    size={18} 
                  />
                ))}
              </div>
              <p className="text-xs text-content-muted mt-1">
                Dựa trên {ratings.length} đánh giá
              </p>
            </div>

            {/* Biểu đồ phân bố sao */}
            <div className="flex-1 space-y-1">
              {[5,4,3,2,1].map((star) => {
                const count = scoreDistribution[star] || 0
                const total = ratings.length
                const percent = total > 0 ? (count / total) * 100 : 0
                return (
                  <div key={star} className="flex items-center gap-2 text-sm">
                    <span className="w-8">{star}★</span>
                    <div className="flex-1 h-1.5 bg-surface-border rounded-full overflow-hidden">
                      <div 
                        className="h-full bg-yellow-400 rounded-full" 
                        style={{ width: `${percent}%` }}
                      />
                    </div>
                    <span className="w-8 text-content-muted text-xs">{count}</span>
                  </div>
                )
              })}
            </div>

            <Button variant="outline" size="sm" onClick={() => navigate('/customer/reviews')}>
              Viết đánh giá
            </Button>
          </div>

          {/* Danh sách đánh giá gần đây */}
          {ratings.length > 0 && (
            <div className="mt-4 pt-4 border-t border-surface-border space-y-3">
              {ratings.map((rating) => (
                <div key={rating.ratingId} className="flex items-start gap-3">
                  <div className="w-8 h-8 rounded-full bg-brand-500 flex items-center justify-center text-white text-sm">
                    {user?.name?.[0] || user?.userName?.[0] || 'U'}
                  </div>
                  <div className="flex-1">
                    <div className="flex items-center gap-2">
                      <p className="text-sm font-semibold text-content-main">Bạn</p>
                      <div className="flex text-yellow-400">
                        {[1,2,3,4,5].map((star) => (
                          <RiStarLine 
                            key={star} 
                            className={star <= rating.score ? "text-yellow-400 fill-current" : "text-gray-300"}
                            size={12} 
                          />
                        ))}
                      </div>
                    </div>
                    <p className="text-sm text-content-muted mt-1">{rating.review || "Không có nội dung"}</p>
                    <p className="text-xs text-content-muted mt-1">
                      {new Date(rating.createdAt).toLocaleDateString('vi-VN')}
                    </p>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
      
      {/* Footer */}
      <footer className="bg-surface-card rounded-2xl p-6 md:p-8 shadow-lg mt-8">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
          <div>
            <h4 className="font-bold text-content-main mb-3">🚗 RIDE APP</h4>
            <p className="text-sm text-content-muted">Đặt xe an toàn, nhanh chóng và tiện lợi</p>
            <div className="flex gap-3 mt-3">
              <RiFacebookLine className="text-content-muted hover:text-brand-500 cursor-pointer transition-colors" size={20} />
              <RiInstagramLine className="text-content-muted hover:text-brand-500 cursor-pointer transition-colors" size={20} />
              <RiYoutubeLine className="text-content-muted hover:text-brand-500 cursor-pointer transition-colors" size={20} />
            </div>
          </div>
          <div>
            <h4 className="font-semibold text-content-main mb-3">Về chúng tôi</h4>
            <ul className="space-y-2 text-sm text-content-muted">
              <li className="hover:text-brand-500 cursor-pointer">Giới thiệu</li>
              <li className="hover:text-brand-500 cursor-pointer">Tuyển dụng</li>
              <li className="hover:text-brand-500 cursor-pointer">Blog</li>
            </ul>
          </div>
          <div>
            <h4 className="font-semibold text-content-main mb-3">Chính sách</h4>
            <ul className="space-y-2 text-sm text-content-muted">
              <li className="hover:text-brand-500 cursor-pointer">Điều khoản sử dụng</li>
              <li className="hover:text-brand-500 cursor-pointer">Chính sách bảo mật</li>
              <li className="hover:text-brand-500 cursor-pointer">Quy chế hoạt động</li>
            </ul>
          </div>
          <div>
            <h4 className="font-semibold text-content-main mb-3">Hỗ trợ</h4>
            <ul className="space-y-2 text-sm text-content-muted">
              <li className="hover:text-brand-500 cursor-pointer flex items-center gap-2">
                <RiHeadphoneLine size={16} /> Hotline: 1900 1234
              </li>
              <li className="hover:text-brand-500 cursor-pointer flex items-center gap-2">
                <RiMessageLine size={16} /> Chat hỗ trợ
              </li>
              <li className="hover:text-brand-500 cursor-pointer">FAQ</li>
            </ul>
          </div>
        </div>
        <div className="border-t border-surface-border mt-6 pt-6 text-center text-sm text-content-muted">
          © 2024 RIDE APP. All rights reserved.
        </div>
      </footer>
      
      <style jsx="true">{`
        @keyframes wave {
          0%, 100% { transform: rotate(0deg); }
          25% { transform: rotate(20deg); }
          75% { transform: rotate(-20deg); }
        }
        .animate-wave {
          animation: wave 1s ease-in-out infinite;
          display: inline-block;
        }
        @keyframes bounce {
          0%, 100% { transform: translateY(0); }
          50% { transform: translateY(-10px); }
        }
        .animate-bounce {
          animation: bounce 2s ease-in-out infinite;
        }
      `}</style>
    </div>
  )
}

export default CustomerHomePage