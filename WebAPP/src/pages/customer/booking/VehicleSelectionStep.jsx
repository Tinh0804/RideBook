import React, { useState } from 'react'
import { RiArrowLeftLine, RiCheckLine, RiTicketLine, RiBankCardLine, RiMoneyDollarCircleLine } from 'react-icons/ri'
import toast from 'react-hot-toast'
import Button from '@/components/Elements/Button'
import Spinner from '@/components/Elements/Spinner'
import Modal from '@/components/Elements/Modal'
import Input from '@/components/Elements/Input'
import { cn } from '@/utils/cn'
import { formatCurrency, formatDistance } from '@/utils/currency'
import { PAYMENT_METHOD } from '@/config'
import { masterDataApi } from '@/features/booking/api/masterDataApi'

const DUMMY_DISTANCE = 5.2

const VehicleSelectionStep = ({
  setStep,
  vehicleTypes,
  selectedVType,
  setSelectedVType,
  estimatedPrices,
  selectedPromos,
  setSelectedPromos,
  myPromotions,
  paymentMethod,
  setPaymentMethod,
  paymentProvider,
  setPaymentProvider,
  estimating,
  handleEstimatePrice,
  countdown,
  handleBook,
  loading,
  originalPrice,
  finalPrice,
  isDiscounted,
  selectedEstimate
}) => {
  const [isPromoModalOpen, setPromoModalOpen] = useState(false)
  const [promoInput, setPromoInput] = useState('')
  const [promoLoading, setPromoLoading] = useState(false)

  const applyPromoByCode = async () => {
    if (!promoInput.trim()) return
    setPromoLoading(true)
    try {
      const data = await masterDataApi.getPromotionByCode(promoInput.trim().toUpperCase())
      const alreadyAdded = selectedPromos.some(p => p.promotionCode === data.promotionCode)
      if (alreadyAdded) {
        toast.error('Mã này đã được thêm rồi!')
      } else {
        setSelectedPromos(prev => [...prev, data])
        toast.success('Thêm mã khúyến mãi thành công!')
        setPromoInput('')
      }
    } catch {
      toast.error('Mã khúyến mãi không hợp lệ hoặc đã hết hạn')
    } finally {
      setPromoLoading(false)
    }
  }

  const togglePromo = (promo) => {
    setSelectedPromos(prev => {
      const exists = prev.some(p => p.promotionCode === promo.promotionCode)
      if (exists) return prev.filter(p => p.promotionCode !== promo.promotionCode)
      return [...prev, promo]
    })
  }

  return (
    <div className="relative z-10 w-full lg:flex-1 h-[55vh] lg:h-full bg-surface-card flex flex-col shadow-2xl order-2 lg:order-1 border-t lg:border-t-0 lg:border-r border-surface-border rounded-t-3xl lg:rounded-none mt-[-20px] lg:mt-0 pointer-events-auto">
      
      {/* Header/Drag Handle */}
      <div className="shrink-0 bg-surface-card rounded-t-3xl lg:rounded-none">
        <div className="w-full flex justify-center py-3 lg:hidden">
          <div className="w-12 h-1.5 bg-surface-border rounded-full" />
        </div>
        <div className="hidden lg:flex items-center gap-3 p-4 border-b border-surface-border">
          <button 
            onClick={() => setStep(1)}
            className="w-10 h-10 bg-surface hover:bg-surface-hover rounded-full flex items-center justify-center border border-surface-border transition-colors"
          >
            <RiArrowLeftLine size={20} className="text-content-main" />
          </button>
          <h2 className="font-display font-bold text-lg text-content-main">Xác nhận chuyến đi</h2>
        </div>
      </div>

      {/* Scrollable form */}
      <div className="flex-1 overflow-y-auto no-scrollbar p-6 lg:pt-6 pt-0 space-y-6 flex flex-col">
        {/* Vehicle type selection */}
        <div className="space-y-3 shrink-0">
          <h3 className="font-semibold text-content-main text-sm">Loại xe</h3>
          {vehicleTypes.length === 0 ? (
            <div className="flex justify-center py-4"><Spinner /></div>
          ) : (
            <div className="flex gap-3 overflow-x-auto pb-2 no-scrollbar">
              {[...vehicleTypes].sort((a, b) => (a.maxPassengers || 0) - (b.maxPassengers || 0)).map((vt, idx) => {
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

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6 shrink-0">
           {/* Promo code */}
          <div className="space-y-3">
            <h3 className="font-semibold text-content-main text-sm flex items-center gap-2">
              <RiTicketLine size={16} className="text-brand-400" /> Khuyến mãi
            </h3>
            
            <div
              onClick={() => setPromoModalOpen(true)}
              className="w-full flex items-center justify-between p-3 rounded-xl border border-surface-border bg-surface hover:border-brand-500/50 hover:bg-surface-hover transition-all duration-200 cursor-pointer"
            >
              <div className="flex items-center gap-3">
                <div className="w-8 h-8 rounded-full bg-brand-500/10 flex items-center justify-center">
                  <RiTicketLine size={18} className="text-brand-400" />
                </div>
                <div className="text-left">
                  {selectedPromos.length > 0 ? (
                    <>
                      <p className="text-sm font-semibold text-content-main">
                        {selectedPromos.length} mã đang áp dụng
                      </p>
                      <p className="text-xs text-brand-400 font-medium">
                        {selectedPromos.map(p => p.promotionCode).join(', ')}
                      </p>
                    </>
                  ) : (
                    <>
                      <p className="text-sm font-semibold text-content-main">Chọn mã khuyến mãi</p>
                      <p className="text-xs text-content-muted">Có {myPromotions.length} ưu đãi chờ bạn</p>
                    </>
                  )}
                </div>
              </div>
              {selectedPromos.length > 0 ? (
                <button 
                  onClick={(e) => { e.stopPropagation(); setSelectedPromos([]); }}
                  className="text-xs text-red-400 hover:text-red-300 font-medium"
                >
                  Xóa tất cả
                </button>
              ) : (
                <span className="text-sm text-brand-400 font-medium">Chọn</span>
              )}
            </div>
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
      </div>

      {/* Fixed Bottom Summary & Book Button */}
      <div className="shrink-0 p-6 border-t border-surface-border bg-surface-card">
        <div className="bg-surface rounded-2xl p-5 border border-surface-border">
            <div className="flex items-center justify-between mb-2">
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
                        {isDiscounted && (
                          <p className="text-[11px] font-semibold text-green-400 mt-0.5">
                            Tiết kiệm được {formatCurrency(originalPrice - finalPrice)}
                          </p>
                        )}
                     </>
                  )}
               </div>
            </div>

            {/* Breakdown of discount per promo */}
            {!estimating && isDiscounted && selectedPromos.length > 0 && (
              <div className="mb-3 px-1 space-y-1">
                {selectedPromos.map(p => (
                  <div key={p.promotionCode} className="flex items-center justify-between text-xs">
                    <span className="text-brand-400 font-medium">🏷 {p.promotionCode}</span>
                    <span className="text-green-400 font-semibold">- {p.discountLimit ? formatCurrency(p.discountLimit) : `${p.discountValue || 0}%`}</span>
                  </div>
                ))}
              </div>
            )}
            
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


      {/* Promo Selection Modal */}
      <Modal isOpen={isPromoModalOpen} onClose={() => setPromoModalOpen(false)} title="Chọn mã giảm giá" size="md">
        <div className="space-y-4 pt-2">
          {/* Direct code input */}
          <div className="flex gap-2">
            <Input
              placeholder="Nhập mã khuyến mãi..."
              value={promoInput}
              onChange={(e) => setPromoInput(e.target.value.toUpperCase())}
              onKeyDown={(e) => e.key === 'Enter' && applyPromoByCode()}
              className="flex-1"
            />
            <Button 
              onClick={applyPromoByCode}
              disabled={promoLoading || !promoInput.trim()}
            >
              {promoLoading ? <Spinner size="sm" /> : 'Thêm'}
            </Button>
          </div>

          {/* Currently selected */}
          {selectedPromos.length > 0 && (
            <div className="bg-brand-500/5 border border-brand-500/20 rounded-xl p-3 space-y-2">
              <p className="text-xs font-semibold text-brand-400">Đang áp dụng ({selectedPromos.length})</p>
              <div className="flex flex-wrap gap-2">
                {selectedPromos.map(p => (
                  <div key={p.promotionCode} className="flex items-center gap-1 bg-brand-500/20 text-brand-400 rounded-lg px-2 py-1 text-xs font-semibold">
                    <RiTicketLine size={12} />
                    {p.promotionCode}
                    <button onClick={() => togglePromo(p)} className="ml-1 hover:text-red-400 transition-colors">×</button>
                  </div>
                ))}
              </div>
            </div>
          )}

          <div className="border-t border-surface-border" />

          {/* Saved Promos List */}
          <div className="space-y-2 max-h-[300px] overflow-y-auto no-scrollbar pb-4">
            <h4 className="text-sm font-semibold text-content-main">Mã của bạn ({myPromotions.length})</h4>
            {myPromotions.length === 0 ? (
              <div className="text-center py-6 text-content-muted text-sm">
                Bạn chưa có mã khuyến mãi nào.
              </div>
            ) : (
              [...myPromotions].sort((a, b) => {
                const isAEligible = !a.minTripValue || originalPrice >= a.minTripValue
                const isBEligible = !b.minTripValue || originalPrice >= b.minTripValue
                
                if (isAEligible && !isBEligible) return -1
                if (!isAEligible && isBEligible) return 1
                
                const getDiscount = (p) => {
                  if (p.discountType === 'PERCENTAGE') {
                    let d = ((p.discountValue || 0) / 100) * originalPrice
                    return p.discountLimit > 0 ? Math.min(d, p.discountLimit) : d
                  }
                  return p.discountLimit || p.discountValue || 0
                }
                
                return getDiscount(b) - getDiscount(a)
              }).map(p => {
                const isSelected = selectedPromos.some(s => s.promotionCode === p.promotionCode)
                const isEligible = !p.minTripValue || originalPrice >= p.minTripValue
                return (
                  <div 
                    key={p.promotionCode}
                    className={cn(
                      "p-3 rounded-xl border transition-all duration-200 flex items-center justify-between gap-3",
                      isEligible ? "cursor-pointer" : "cursor-not-allowed opacity-60 bg-surface-disabled",
                      isSelected
                        ? "border-brand-500 bg-brand-500/10" 
                        : isEligible ? "border-surface-border bg-surface hover:border-brand-500/50" : "border-surface-border"
                    )}
                    onClick={() => {
                      if (isEligible) togglePromo(p)
                      else toast.error(`Đơn tối thiểu ${formatCurrency(p.minTripValue)} để áp dụng mã này`)
                    }}
                  >
                    <div className="flex items-center gap-3 flex-1 min-w-0">
                      {/* Checkbox */}
                      <div className={cn(
                        "w-5 h-5 rounded-md border-2 flex items-center justify-center shrink-0 transition-all",
                        isSelected ? "bg-brand-500 border-brand-500" : "border-surface-border",
                        !isEligible && "bg-surface-border/20"
                      )}>
                        {isSelected && <RiCheckLine size={12} className="text-white" />}
                      </div>
                      <div className="min-w-0">
                        <p className="font-semibold text-brand-400 text-sm">{p.promotionCode}</p>
                        <p className="text-xs text-content-muted mt-0.5 truncate">{p.promotionName}</p>
                        {p.discountLimit > 0 && (
                          <p className="text-xs text-green-400 font-medium mt-0.5">
                            Giảm tối đa {formatCurrency(p.discountLimit)}
                          </p>
                        )}
                        {!isEligible && p.minTripValue > 0 && (
                          <p className="text-[10px] text-red-400 mt-1">
                            (Áp dụng cho cuốc xe từ {formatCurrency(p.minTripValue)})
                          </p>
                        )}
                      </div>
                    </div>
                  </div>
                )
              })
            )}
          </div>

          {/* Apply & Close */}
          <Button
            fullWidth
            onClick={() => setPromoModalOpen(false)}
          >
            Áp dụng {selectedPromos.length > 0 ? `(${selectedPromos.length} mã)` : ''}
          </Button>
        </div>
      </Modal>

    </div>
  )
}

export default VehicleSelectionStep
