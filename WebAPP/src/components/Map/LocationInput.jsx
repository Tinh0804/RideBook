// components/LocationInput.jsx (Wrapper nếu cần)
import { RiCrosshairLine, RiLoader4Line } from 'react-icons/ri'
import AddressInput from './AddressInput'

const LocationInput = ({ 
  placeholder, 
  value, 
  onChange, 
  onLocationDetect,
  icon: Icon,
  iconColor 
}) => {
  const [detecting, setDetecting] = useState(false)

  const handleDetectLocation = () => {
    setDetecting(true)
    
    if (!navigator.geolocation) {
      alert('Trình duyệt không hỗ trợ định vị')
      setDetecting(false)
      return
    }

    navigator.geolocation.getCurrentPosition(
      async (position) => {
        const { latitude, longitude } = position.coords
        
        // Reverse geocoding để lấy tên địa chỉ
        try {
          const response = await fetch(
            `https://nominatim.openstreetmap.org/reverse?lat=${latitude}&lon=${longitude}&format=json&accept-language=vi`
          )
          const data = await response.json()
          const addressName = data.display_name || 'Vị trí hiện tại'
          
          onChange(addressName)
          if (onLocationDetect) {
            onLocationDetect({ lat: latitude, lng: longitude, name: addressName })
          }
        } catch (error) {
          console.error('Error:', error)
          onChange('Vị trí hiện tại')
          if (onLocationDetect) {
            onLocationDetect({ lat: latitude, lng: longitude, name: 'Vị trí hiện tại' })
          }
        }
        setDetecting(false)
      },
      (error) => {
        alert('Không thể xác định vị trí. Vui lòng kiểm tra quyền truy cập.')
        setDetecting(false)
      }
    )
  }

  return (
    <div className="flex items-center gap-3 flex-1">
      <div className={cn(
        "w-10 h-10 rounded-full border flex items-center justify-center shrink-0",
        iconColor === 'brand' && "bg-brand-500/15 border-brand-500/30",
        iconColor === 'red' && "bg-red-500/15 border-red-500/30"
      )}>
        {Icon && <Icon size={20} className={iconColor === 'brand' ? "text-brand-400" : "text-red-400"} />}
      </div>
      <div className="relative flex-1">
        <AddressInput
          placeholder={placeholder}
          value={value}
          onChange={onChange}
          onLocationDetect={onLocationDetect}
        />
        <button
          onClick={handleDetectLocation}
          disabled={detecting}
          className="absolute right-2 top-1/2 -translate-y-1/2 p-1.5 rounded-lg hover:bg-surface-border transition-colors z-10"
          title="Sử dụng vị trí hiện tại"
        >
          {detecting ? (
            <RiLoader4Line className="animate-spin text-brand-500" size={18} />
          ) : (
            <RiCrosshairLine className="text-content-muted hover:text-brand-500" size={18} />
          )}
        </button>
      </div>
    </div>
  )
}