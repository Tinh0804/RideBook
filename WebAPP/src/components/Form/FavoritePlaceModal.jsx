import { useState } from 'react'
import { motion, AnimatePresence } from 'motion/react'
import { RiCloseLine, RiMapPinLine, RiHome4Line, RiBuilding4Line, RiSuitcaseLine } from 'react-icons/ri'
import Button from '@/components/Elements/Button'
import FormField from '@/components/Form/FormField'
import Input from '@/components/Elements/Input'
import AddressInput from '@/components/Map/AddressInput'

const ICONS = [
  { id: 'HOME', label: 'Nhà', icon: RiHome4Line },
  { id: 'WORK', label: 'Công ty', icon: RiBuilding4Line },
  { id: 'OTHER', label: 'Khác', icon: RiMapPinLine },
  { id: 'TRAVEL', label: 'Du lịch', icon: RiSuitcaseLine }
]

const FavoritePlaceModal = ({ isOpen, onClose, onSave, initialData }) => {
  const [label, setLabel] = useState(initialData?.label || '')
  const [icon, setIcon] = useState(initialData?.icon || 'HOME')
  const [location, setLocation] = useState(initialData ? {
    name: initialData.address,
    lat: initialData.lat,
    lng: initialData.lng
  } : null)
  const [loading, setLoading] = useState(false)

  const handleSave = async () => {
    if (!label || !location) return
    setLoading(true)
    try {
      await onSave({
        label,
        icon,
        address: location.name,
        lat: location.lat,
        lng: location.lng
      })
      onClose()
    } finally {
      setLoading(false)
    }
  }

  return (
    <AnimatePresence>
      {isOpen && (
        <>
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 z-50 bg-slate-900/40 backdrop-blur-sm"
            onClick={onClose}
          />
          <motion.div
            initial={{ opacity: 0, scale: 0.95, y: 20 }}
            animate={{ opacity: 1, scale: 1, y: 0 }}
            exit={{ opacity: 0, scale: 0.95, y: 20 }}
            className="fixed left-1/2 top-1/2 z-50 w-full max-w-md -translate-x-1/2 -translate-y-1/2 p-4"
          >
            <div className="overflow-hidden rounded-2xl bg-white dark:bg-surface-card shadow-2xl">
              <div className="flex items-center justify-between border-b border-surface-border p-4">
                <h3 className="font-bold text-lg text-content-main">
                  {initialData ? 'Sửa địa điểm' : 'Thêm địa điểm yêu thích'}
                </h3>
                <button
                  onClick={onClose}
                  className="rounded-full p-2 text-content-muted hover:bg-surface-dark transition"
                >
                  <RiCloseLine size={20} />
                </button>
              </div>
              
              <div className="p-5 space-y-5">
                <FormField label="Loại địa điểm">
                  <div className="flex gap-2">
                    {ICONS.map((ic) => {
                      const Icon = ic.icon
                      const selected = icon === ic.id
                      return (
                        <button
                          key={ic.id}
                          type="button"
                          onClick={() => setIcon(ic.id)}
                          className={`flex-1 flex flex-col items-center justify-center gap-1.5 p-3 rounded-xl border transition-colors ${
                            selected 
                              ? 'border-brand-500 bg-brand-50 text-brand-600 dark:bg-brand-500/10' 
                              : 'border-surface-border text-content-muted hover:bg-surface-dark'
                          }`}
                        >
                          <Icon size={20} />
                          <span className="text-[10px] font-bold uppercase">{ic.label}</span>
                        </button>
                      )
                    })}
                  </div>
                </FormField>

                <FormField label="Tên gọi (VD: Nhà riêng, Công ty,...)">
                  <Input 
                    value={label} 
                    onChange={e => setLabel(e.target.value)} 
                    placeholder="Nhập tên gọi gợi nhớ..." 
                    className="!rounded-xl !bg-surface-dark !border-surface-border"
                  />
                </FormField>

                <FormField label="Địa chỉ">
                  <AddressInput
                    placeholder="Tìm địa chỉ..."
                    value={location?.name || ''}
                    onChange={(name) => setLocation(prev => prev ? { ...prev, name } : { name, lat: 0, lng: 0 })}
                    onLocationDetect={setLocation}
                    showDetectButton={true}
                    className="!rounded-xl !border-surface-border !bg-surface-dark"
                  />
                </FormField>
              </div>

              <div className="border-t border-surface-border p-4 flex gap-3 bg-surface-dark/50">
                <Button variant="outline" className="flex-1 rounded-xl font-bold" onClick={onClose}>
                  Hủy
                </Button>
                <Button 
                  className="flex-1 rounded-xl bg-brand-500 text-white font-bold hover:bg-brand-600" 
                  onClick={handleSave}
                  loading={loading}
                  disabled={!label || !location?.lat}
                >
                  Lưu địa điểm
                </Button>
              </div>
            </div>
          </motion.div>
        </>
      )}
    </AnimatePresence>
  )
}

export default FavoritePlaceModal
