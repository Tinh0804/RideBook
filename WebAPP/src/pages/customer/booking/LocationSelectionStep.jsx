import {
  RiArrowLeftLine,
  RiArrowRightLine,
  RiFocus3Line,
  RiMapPin2Line,
  RiMapPinLine,
  RiCrosshairLine,
  RiLoader4Line,
  RiHome4Line,
  RiBuilding4Line,
  RiSuitcaseLine
} from 'react-icons/ri'
import { useRef, useState, useEffect } from 'react'
import { AnimatePresence, motion } from 'motion/react'
import Button from '@/components/Elements/Button'
import AddressInput from '@/components/Map/AddressInput'
import Spinner from '@/components/Elements/Spinner'
import BookingJourneyProgress from './BookingJourneyProgress'
import { favoritePlaceApi } from '@/features/customer/api/favoritePlaceApi'

const LocationSelectionStep = ({
  pickup,
  setPickup,
  dropoff,
  setDropoff,
  selectingLocationFor,
  setSelectingLocationFor,
  tempMapLocation,
  mapLoading,
  openMapSelection,
  handleNextStep,
}) => {
  const pickupInputRef = useRef(null)
  const [detectingPickup, setDetectingPickup] = useState(false)
  const [favoritePlaces, setFavoritePlaces] = useState([])

  useEffect(() => {
    favoritePlaceApi.getMyFavoritePlaces()
      .then(setFavoritePlaces)
      .catch(console.error)
  }, [])

  const handleDetectLocation = async () => {
    setDetectingPickup(true)
    await pickupInputRef.current?.detectCurrentLocation()
    setDetectingPickup(false)
  }

  if (selectingLocationFor) {
    const isPickup = selectingLocationFor === 'pickup'

    return (
      <AnimatePresence>
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          className="absolute inset-0 z-20 flex flex-col pointer-events-none"
        >
          <button
            type="button"
            onClick={() => setSelectingLocationFor(null)}
            className="pointer-events-auto absolute left-5 top-5 z-10 grid h-11 w-11 place-items-center rounded-full border border-white/40 bg-white/85 text-slate-950 shadow-lg backdrop-blur active:scale-95"
            aria-label="Quay lại"
          >
            <RiArrowLeftLine size={21} />
          </button>

          <div className="flex-1" />

          <motion.div
            initial={{ y: '100%' }}
            animate={{ y: 0 }}
            exit={{ y: '100%' }}
            transition={{ type: 'spring', damping: 28, stiffness: 240 }}
            className="pointer-events-auto rounded-t-3xl border-t border-surface-border bg-surface-card/95 p-5 pb-7 shadow-[0_-16px_50px_rgba(15,23,42,.18)] backdrop-blur-xl sm:p-7"
          >
            <div className="mx-auto mb-6 h-1 w-12 rounded-full bg-surface-border" />
            <div className="mb-6 flex items-center gap-4">
              <span className={`grid h-12 w-12 shrink-0 place-items-center rounded-xl ${isPickup ? 'bg-brand-500 text-white' : 'bg-slate-950 text-white dark:bg-white dark:text-slate-950'}`}>
                {isPickup ? <RiMapPinLine size={23} /> : <RiMapPin2Line size={23} />}
              </span>
              <div className="min-w-0">
                <p className="text-sm font-semibold text-content-muted">
                  {isPickup ? 'Điểm đón trên bản đồ' : 'Điểm đến trên bản đồ'}
                </p>
                <p className="mt-1 line-clamp-2 font-bold text-content-main">
                  {tempMapLocation?.name || 'Di chuyển ghim đến vị trí mong muốn'}
                </p>
              </div>
            </div>
            <Button
              fullWidth
              size="lg"
              disabled={!tempMapLocation}
              className="h-[52px] rounded-xl font-bold"
              onClick={() => {
                if (isPickup) setPickup(tempMapLocation)
                else setDropoff(tempMapLocation)
                setSelectingLocationFor(null)
              }}
            >
              Xác nhận vị trí
            </Button>
          </motion.div>
        </motion.div>
      </AnimatePresence>
    )
  }

  return (
    <div className="pointer-events-auto h-full overflow-y-auto bg-[#e8ece3] dark:bg-surface-dark">
      <motion.div
        initial={{ opacity: 0, x: -18 }}
        animate={{ opacity: 1, x: 0 }}
        transition={{ duration: 0.45, ease: [0.16, 1, 0.3, 1] }}
        className="mx-auto w-full max-w-xl space-y-5 p-5 pb-8 lg:p-7"
      >
        <BookingJourneyProgress step={1} />

        <section className="relative min-h-40 overflow-hidden rounded-2xl bg-slate-950 p-6 text-white">
          <div className="relative z-10 max-w-[75%]">
            <p className="mb-3 text-sm font-semibold text-lime-accent">Tạo hành trình mới</p>
            <h1 className="font-display text-3xl font-bold leading-[1.02] tracking-[-0.04em]">
              Bạn muốn đi đâu?
            </h1>
            <p className="mt-3 text-sm leading-relaxed text-white/55">Chọn hai điểm, phần còn lại để BookCar lo.</p>
          </div>
          <span className="absolute -bottom-5 right-3 font-display text-8xl font-bold tracking-[-0.08em] text-white/[.06]">01</span>
        </section>

        <section className="rounded-2xl border border-surface-border bg-surface-card p-5 shadow-sm">
          <div className="relative space-y-5">
            <span className="absolute bottom-12 left-5 top-12 w-px bg-surface-border" />

            <div className="relative flex gap-3">
              <span className="z-10 mt-6 grid h-10 w-10 shrink-0 place-items-center rounded-xl bg-brand-500 text-white">
                <RiMapPinLine size={19} />
              </span>
              <div className="min-w-0 flex-1">
                <label className="mb-1.5 block text-sm font-semibold text-content-muted">Điểm đón</label>
                <div className="flex gap-2">
                  <AddressInput
                    ref={pickupInputRef}
                    placeholder="Tìm điểm đón..."
                    value={pickup?.name || ''}
                    onChange={(name) => setPickup((prev) => ({ ...prev, name }))}
                    onLocationDetect={(data) => setPickup(data || null)}
                    showDetectButton={false}
                    className="!rounded-xl !border-surface-border !bg-surface-dark"
                  />
                  <button
                    type="button"
                    onClick={handleDetectLocation}
                    disabled={detectingPickup || mapLoading === 'pickup'}
                    className="grid h-12 w-12 shrink-0 place-items-center rounded-xl border border-surface-border bg-surface-dark text-content-muted transition hover:border-brand-500 hover:text-brand-500 active:scale-95"
                    title="Sử dụng vị trí hiện tại"
                  >
                    {detectingPickup ? (
                      <RiLoader4Line className="animate-spin text-brand-500" size={20} />
                    ) : (
                      <RiCrosshairLine size={20} />
                    )}
                  </button>
                </div>
              </div>
            </div>

            <div className="relative flex gap-3">
              <span className="z-10 mt-6 grid h-10 w-10 shrink-0 place-items-center rounded-xl bg-slate-950 text-white dark:bg-white dark:text-slate-950">
                <RiMapPin2Line size={19} />
              </span>
              <div className="min-w-0 flex-1">
                <label className="mb-1.5 block text-sm font-semibold text-content-muted">Điểm đến</label>
                <div className="flex gap-2">
                  <AddressInput
                    placeholder="Bạn muốn đến đâu?"
                    value={dropoff?.name || ''}
                    onChange={(name) => setDropoff((prev) => ({ ...prev, name }))}
                    onLocationDetect={(data) => setDropoff(data || null)}
                    showDetectButton={false}
                    className="!rounded-xl !border-surface-border !bg-surface-dark"
                  />
                  <button
                    type="button"
                    onClick={() => openMapSelection('dropoff')}
                    disabled={mapLoading === 'dropoff'}
                    className="grid h-12 w-12 shrink-0 place-items-center rounded-xl border border-surface-border bg-surface-dark text-content-muted transition hover:border-brand-500 hover:text-brand-500 active:scale-95"
                    title="Chọn điểm đến trên bản đồ"
                  >
                    {mapLoading === 'dropoff' ? <Spinner size="sm" /> : <RiFocus3Line size={20} />}
                  </button>
                </div>
              </div>
            </div>
          </div>
        </section>

        {favoritePlaces.length > 0 && (
          <section className="rounded-2xl border border-surface-border bg-surface-card p-5 shadow-sm">
            <h3 className="mb-4 text-sm font-bold text-content-main">Địa điểm đã lưu</h3>
            <div className="flex gap-3 overflow-x-auto pb-2 scrollbar-hide">
              {favoritePlaces.map(place => (
                <button
                  key={place.favoritePlaceId}
                  type="button"
                  onClick={() => setDropoff({ name: place.address, lat: place.lat, lng: place.lng })}
                  className="flex shrink-0 items-center gap-3 rounded-xl border border-surface-border bg-surface-dark p-3 text-left transition hover:border-brand-500 active:scale-95"
                >
                  <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-brand-500/10 text-brand-500">
                    {place.icon === 'HOME' ? <RiHome4Line size={20} /> : 
                     place.icon === 'WORK' ? <RiBuilding4Line size={20} /> :
                     place.icon === 'TRAVEL' ? <RiSuitcaseLine size={20} /> :
                     <RiMapPinLine size={20} />}
                  </div>
                  <div>
                    <p className="font-bold text-content-main">{place.label}</p>
                    <p className="w-32 truncate text-xs text-content-muted">{place.address}</p>
                  </div>
                </button>
              ))}
            </div>
          </section>
        )}

        <Button
          fullWidth
          size="lg"
          onClick={handleNextStep}
          disabled={!pickup || !dropoff}
          className="group h-14 rounded-xl font-bold"
        >
          Tiếp tục
          <RiArrowRightLine className="transition-transform group-hover:translate-x-1" />
        </Button>
      </motion.div>
    </div>
  )
}

export default LocationSelectionStep
