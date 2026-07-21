import { useState } from 'react'
import {
  RiArrowLeftLine,
  RiArrowRightLine,
  RiBankCardLine,
  RiCheckLine,
  RiMoneyDollarCircleLine,
  RiRefreshLine,
  RiTicketLine,
  RiTimeLine,
  RiUser3Line,
} from 'react-icons/ri'
import { AnimatePresence, motion } from 'motion/react'
import toast from 'react-hot-toast'
import Button from '@/components/Elements/Button'
import Spinner from '@/components/Elements/Spinner'
import Modal from '@/components/Elements/Modal'
import Input from '@/components/Elements/Input'
import { cn } from '@/utils/cn'
import { formatCurrency, formatDistance } from '@/utils/currency'
import { PAYMENT_METHOD } from '@/config'
import { masterDataApi } from '@/features/booking/api/masterDataApi'
import BookingJourneyProgress from './BookingJourneyProgress'

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
  selectedEstimate,
}) => {
  const [isPromoModalOpen, setPromoModalOpen] = useState(false)
  const [promoInput, setPromoInput] = useState('')
  const [promoLoading, setPromoLoading] = useState(false)

  const applyPromoByCode = async () => {
    if (!promoInput.trim()) return
    setPromoLoading(true)
    try {
      const promo = await masterDataApi.getPromotionByCode(promoInput.trim().toUpperCase())
      if (selectedPromos.some((item) => item.promotionCode === promo.promotionCode)) {
        toast.error('Mã này đã được thêm rồi')
      } else {
        setSelectedPromos((current) => [...current, promo])
        setPromoInput('')
        toast.success('Đã thêm mã khuyến mãi')
      }
    } catch {
      toast.error('Mã khuyến mãi không hợp lệ hoặc đã hết hạn')
    } finally {
      setPromoLoading(false)
    }
  }

  const togglePromo = (promo) => {
    setSelectedPromos((current) =>
      current.some((item) => item.promotionCode === promo.promotionCode)
        ? current.filter((item) => item.promotionCode !== promo.promotionCode)
        : [...current, promo]
    )
  }

  const sortedVehicles = [...vehicleTypes].sort(
    (a, b) => (a.maxPassengers || 0) - (b.maxPassengers || 0)
  )

  return (
    <motion.div
      initial={{ opacity: 0, x: -18 }}
      animate={{ opacity: 1, x: 0 }}
      exit={{ opacity: 0, x: -18 }}
      transition={{ duration: 0.45, ease: [0.16, 1, 0.3, 1] }}
      className="pointer-events-auto relative z-10 flex h-full min-h-[66dvh] w-full flex-col rounded-t-3xl border-t border-surface-border bg-[#f3f5f1] shadow-[0_-16px_50px_rgba(15,23,42,.14)] dark:bg-surface-dark lg:min-h-0 lg:rounded-none lg:border-r lg:border-t-0"
    >
      <header className="shrink-0 rounded-t-3xl bg-[#f3f5f1]/95 backdrop-blur dark:bg-surface-dark/95 lg:rounded-none">
        <div className="flex w-full justify-center py-3 lg:hidden">
          <span className="h-1 w-12 rounded-full bg-surface-border" />
        </div>
        <div className="flex items-center gap-3 border-b border-surface-border px-5 pb-4 lg:p-5">
          <button
            type="button"
            onClick={() => setStep(1)}
            className="grid h-10 w-10 place-items-center rounded-full border border-surface-border bg-surface-card text-content-main transition hover:border-brand-500 active:scale-95"
            aria-label="Quay lại chọn địa điểm"
          >
            <RiArrowLeftLine size={20} />
          </button>
          <BookingJourneyProgress step={2} className="max-w-md flex-1" />
        </div>
      </header>

      <div className="no-scrollbar flex-1 overflow-y-auto p-5 lg:p-6">
        <section className="relative mb-6 min-h-32 overflow-hidden rounded-2xl bg-[#dfe7d9] p-5 text-slate-950 dark:bg-slate-900 dark:text-white">
          <div className="relative z-10 max-w-[70%]">
            <p className="text-sm font-semibold text-emerald-700 dark:text-lime-accent">BookCar fleet</p>
            <h3 className="mt-2 font-display text-2xl font-bold leading-[1.05] tracking-tight">
              Chọn chiếc xe hợp với hành trình.
            </h3>
          </div>
          <span className="absolute -bottom-5 right-3 font-display text-8xl font-bold tracking-[-0.08em] text-slate-950/[.06] dark:text-white/[.06]">02</span>
        </section>

        <div className="grid items-start gap-5 lg:grid-cols-[minmax(0,1fr)_260px]">
          <div className="space-y-6">
            <section>
              <div className="mb-3 flex items-center justify-between">
                <div>
                  <h3 className="font-display text-lg font-bold text-content-main">Hạng xe</h3>
                  <p className="text-sm text-content-muted">Chạm để xem giá tương ứng</p>
                </div>
                {estimating && <Spinner size="sm" />}
              </div>

              {vehicleTypes.length === 0 ? (
                <div className="grid min-h-40 place-items-center rounded-2xl border border-surface-border bg-surface-card">
                  <Spinner />
                </div>
              ) : (
                <div className="grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-2">
                  {sortedVehicles.map((vehicle, index) => {
                    const estimate = estimatedPrices.find(
                      (item) => item.vehicleTypeId === vehicle.vehicleTypeId
                    )
                    const selected = selectedVType?.vehicleTypeId === vehicle.vehicleTypeId

                    return (
                      <motion.button
                        key={vehicle.vehicleTypeId || index}
                        type="button"
                        whileTap={{ scale: 0.98 }}
                        onClick={() => setSelectedVType(vehicle)}
                        className={cn(
                          'relative min-w-0 overflow-hidden rounded-2xl border p-4 text-left transition-colors',
                          selected
                            ? 'border-brand-500 bg-brand-50 dark:bg-brand-500/10'
                            : 'border-surface-border bg-surface-card hover:border-brand-500/40'
                        )}
                      >
                        {selected && (
                          <motion.span
                            layoutId="selected-vehicle"
                            className="absolute right-3 top-3 grid h-6 w-6 place-items-center rounded-full bg-brand-500 text-white"
                          >
                            <RiCheckLine size={14} />
                          </motion.span>
                        )}
                        <img
                          src={vehicle.icon || '/images/bookcar-booking-car.webp'}
                          alt={vehicle.vehicleTypeName}
                          onError={(event) => {
                            event.currentTarget.src = '/images/bookcar-booking-car.webp'
                          }}
                          className="h-16 w-24 object-contain"
                        />
                        <p className="mt-2 truncate font-bold text-content-main">{vehicle.vehicleTypeName}</p>
                        <p className="mt-1 flex items-center gap-1 text-xs text-content-muted">
                          <RiUser3Line size={13} /> {vehicle.maxPassengers || 4} chỗ
                        </p>
                        <p className="mt-3 border-t border-surface-border pt-3 text-base font-bold text-brand-600 dark:text-brand-400">
                          {estimate
                            ? formatCurrency(estimate.totalPrice)
                            : `${formatCurrency(vehicle.pricePerKm)}/km`}
                        </p>
                      </motion.button>
                    )
                  })}
                </div>
              )}
            </section>

            <section className="grid gap-3 sm:grid-cols-2 lg:grid-cols-1">
              <button
                type="button"
                onClick={() => setPromoModalOpen(true)}
                className="flex items-center gap-3 rounded-2xl border border-surface-border bg-surface-card p-4 text-left transition hover:border-brand-500/40"
              >
                <span className="grid h-11 w-11 shrink-0 place-items-center rounded-xl bg-brand-500/10 text-brand-600 dark:text-brand-400">
                  <RiTicketLine size={21} />
                </span>
                <span className="min-w-0 flex-1">
                  <strong className="block font-bold text-content-main">
                    {selectedPromos.length ? `${selectedPromos.length} ưu đãi đã chọn` : 'Thêm ưu đãi'}
                  </strong>
                  <span className="block truncate text-sm text-content-muted">
                    {selectedPromos.length
                      ? selectedPromos.map((promo) => promo.promotionCode).join(', ')
                      : `${myPromotions.length} mã có sẵn`}
                  </span>
                </span>
                <RiArrowRightLine className="text-content-muted" />
              </button>

              <div className="rounded-2xl border border-surface-border bg-surface-card p-4">
                <p className="mb-3 font-bold text-content-main">Thanh toán</p>
                <div className="grid grid-cols-2 gap-2">
                  {[
                    { value: PAYMENT_METHOD.CASH, label: 'Tiền mặt', icon: RiMoneyDollarCircleLine },
                    { value: PAYMENT_METHOD.ONLINE, label: 'Thẻ / Ví', icon: RiBankCardLine },
                  ].map(({ value, label, icon: Icon }) => {
                    const selected = paymentMethod === value
                    return (
                      <button
                        key={value}
                        type="button"
                        onClick={() => setPaymentMethod(value)}
                        className={cn(
                          'flex items-center justify-center gap-2 rounded-xl border px-3 py-3 text-sm font-semibold transition',
                          selected
                            ? 'border-brand-500 bg-brand-50 text-brand-700 dark:bg-brand-500/10 dark:text-brand-400'
                            : 'border-surface-border bg-surface-dark text-content-muted'
                        )}
                      >
                        <Icon size={18} /> {label}
                      </button>
                    )
                  })}
                </div>

                <AnimatePresence initial={false}>
                  {paymentMethod === PAYMENT_METHOD.ONLINE && (
                    <motion.div
                      initial={{ opacity: 0, height: 0 }}
                      animate={{ opacity: 1, height: 'auto' }}
                      exit={{ opacity: 0, height: 0 }}
                      className="mt-3 grid grid-cols-2 gap-2 overflow-hidden"
                    >
                      {['VNPAY', 'MOMO'].map((provider) => (
                        <button
                          key={provider}
                          type="button"
                          onClick={() => setPaymentProvider(provider)}
                          className={cn(
                            'rounded-xl border px-3 py-2 text-sm font-bold transition',
                            paymentProvider === provider
                              ? 'border-brand-500 text-brand-600 dark:text-brand-400'
                              : 'border-surface-border text-content-muted'
                          )}
                        >
                          {provider}
                        </button>
                      ))}
                    </motion.div>
                  )}
                </AnimatePresence>
              </div>
            </section>
          </div>

          <aside className="rounded-2xl bg-slate-950 p-5 text-white lg:sticky lg:top-0">
            <p className="text-sm font-semibold text-lime-accent">Chuyến đi của bạn</p>
            <div className="mt-5 space-y-4">
              <div className="flex items-center justify-between border-b border-white/10 pb-4">
                <span className="text-sm text-white/55">Hạng xe</span>
                <span className="max-w-[130px] truncate text-sm font-bold">
                  {selectedVType?.vehicleTypeName || 'Chưa chọn'}
                </span>
              </div>
              <div className="flex items-center justify-between border-b border-white/10 pb-4">
                <span className="text-sm text-white/55">Khoảng cách</span>
                <span className="text-sm font-bold">
                  {formatDistance(selectedEstimate?.distance || DUMMY_DISTANCE)}
                </span>
              </div>
              {selectedPromos.length > 0 && (
                <div className="flex items-center justify-between border-b border-white/10 pb-4">
                  <span className="text-sm text-white/55">Ưu đãi</span>
                  <span className="text-sm font-bold text-lime-accent">{selectedPromos.length} mã</span>
                </div>
              )}
            </div>

            <div className="mt-6">
              <p className="text-sm text-white/55">Tổng cộng</p>
              {estimating ? (
                <div className="mt-3"><Spinner /></div>
              ) : (
                <>
                  {isDiscounted && (
                    <p className="mt-2 text-sm text-white/35 line-through">{formatCurrency(originalPrice)}</p>
                  )}
                  <p className="mt-1 font-display text-4xl font-bold tracking-tight text-white">
                    {formatCurrency(finalPrice)}
                  </p>
                  {isDiscounted && (
                    <p className="mt-2 text-sm font-semibold text-lime-accent">
                      Tiết kiệm {formatCurrency(originalPrice - finalPrice)}
                    </p>
                  )}
                </>
              )}
            </div>

            {estimatedPrices.length > 0 && (
              <div className="mt-5 flex items-center gap-2 rounded-xl bg-white/8 px-3 py-2.5">
                <RiTimeLine className={countdown > 10 ? 'text-lime-accent' : 'text-red-400'} />
                <span className="flex-1 text-xs font-semibold text-white/65">
                  {countdown > 0 ? `Giữ giá trong ${countdown}s` : 'Báo giá đã hết hạn'}
                </span>
                {countdown <= 0 && (
                  <button
                    type="button"
                    onClick={handleEstimatePrice}
                    className="text-lime-accent transition hover:rotate-90"
                    aria-label="Làm mới báo giá"
                  >
                    <RiRefreshLine size={18} />
                  </button>
                )}
              </div>
            )}

            <Button
              fullWidth
              size="lg"
              onClick={handleBook}
              loading={loading}
              disabled={countdown <= 0}
              className="group mt-5 h-[52px] rounded-xl bg-lime-accent font-bold text-slate-950 shadow-none hover:bg-[#b8ff59] focus:ring-lime-accent"
            >
              Xác nhận đặt xe
              <RiArrowRightLine className="transition-transform group-hover:translate-x-1" />
            </Button>
            <p className="mt-3 text-center text-xs leading-relaxed text-white/40">
              Giá cuối cùng được xác nhận trước khi tìm tài xế.
            </p>
          </aside>
        </div>
      </div>

      <Modal
        isOpen={isPromoModalOpen}
        onClose={() => setPromoModalOpen(false)}
        title="Ưu đãi của bạn"
        size="lg"
      >
        <div className="space-y-4">
          <div className="flex gap-2">
            <Input
              placeholder="Nhập mã khuyến mãi"
              value={promoInput}
              onChange={(event) => setPromoInput(event.target.value.toUpperCase())}
              onKeyDown={(event) => event.key === 'Enter' && applyPromoByCode()}
            />
            <Button
              onClick={applyPromoByCode}
              disabled={promoLoading || !promoInput.trim()}
              className="rounded-xl"
            >
              {promoLoading ? <Spinner size="sm" /> : 'Thêm'}
            </Button>
          </div>

          <div className="no-scrollbar max-h-[360px] space-y-2 overflow-y-auto">
            {myPromotions.length === 0 ? (
              <p className="rounded-2xl bg-surface-dark p-8 text-center text-sm text-content-muted">
                Chưa có ưu đãi lưu sẵn. Bạn vẫn có thể nhập mã phía trên.
              </p>
            ) : (
              myPromotions.map((promo) => {
                const selected = selectedPromos.some(
                  (item) => item.promotionCode === promo.promotionCode
                )
                const eligible = !promo.minTripValue || originalPrice >= promo.minTripValue

                return (
                  <button
                    key={promo.promotionCode}
                    type="button"
                    disabled={!eligible}
                    onClick={() => togglePromo(promo)}
                    className={cn(
                      'flex w-full items-center gap-3 rounded-2xl border p-4 text-left transition',
                      selected
                        ? 'border-brand-500 bg-brand-50 dark:bg-brand-500/10'
                        : 'border-surface-border bg-surface-card hover:border-brand-500/40',
                      !eligible && 'cursor-not-allowed opacity-45'
                    )}
                  >
                    <span className="grid h-11 w-11 shrink-0 place-items-center rounded-xl bg-brand-500/10 text-brand-600 dark:text-brand-400">
                      <RiTicketLine size={20} />
                    </span>
                    <span className="min-w-0 flex-1">
                      <strong className="block truncate text-content-main">{promo.promotionCode}</strong>
                      <span className="block truncate text-sm text-content-muted">{promo.promotionName}</span>
                    </span>
                    <span className={cn(
                      'grid h-6 w-6 place-items-center rounded-full border',
                      selected ? 'border-brand-500 bg-brand-500 text-white' : 'border-surface-border'
                    )}>
                      {selected && <RiCheckLine size={14} />}
                    </span>
                  </button>
                )
              })
            )}
          </div>

          <Button fullWidth onClick={() => setPromoModalOpen(false)} className="rounded-xl">
            Xong
          </Button>
        </div>
      </Modal>
    </motion.div>
  )
}

export default VehicleSelectionStep
