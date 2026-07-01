import React from 'react'
import { RiMapPinLine, RiMapPin2Line, RiArrowLeftLine } from 'react-icons/ri'
import Button from '@/components/Elements/Button'
import AddressInput from '@/components/Map/AddressInput'
import LocationAutocomplete from '@/components/Elements/LocationAutocomplete'
import Spinner from '@/components/Elements/Spinner'

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
  handleNextStep
}) => {

  if (selectingLocationFor) {
    return (
      <>
        <button 
          onClick={() => setSelectingLocationFor(null)}
          className="absolute top-4 left-4 pointer-events-auto z-10 w-10 h-10 bg-surface-card/90 backdrop-blur-md rounded-full flex items-center justify-center shadow-lg border border-surface-border hover:bg-surface-muted transition-colors"
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
      </>
    )
  }

  return (
    <div className="w-full h-full bg-surface-dark p-6 pointer-events-auto overflow-y-auto">
      <div className="max-w-2xl mx-auto space-y-8 mt-4 lg:mt-10">
        <div className="text-center space-y-2">
          <h1 className="text-3xl font-display font-bold text-content-main">Bạn muốn đi đâu?</h1>
          <p className="text-content-muted">Nhập điểm đón và điểm đến để bắt đầu hành trình</p>
        </div>
            
      
        <div className="card p-6 space-y-6 relative">
          
          <div className="space-y-6 relative z-10">
            {/* Vertical line connecting inputs */}
            <div className="absolute left-[15px] top-[32px] bottom-[32px] w-0.5 bg-surface-border border-dashed border-l-2 -z-10" />

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
    </div>
  )
}

export default LocationSelectionStep
