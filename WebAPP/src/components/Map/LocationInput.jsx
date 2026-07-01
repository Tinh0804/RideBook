// components/Map/LocationInput.jsx
import React, { useState } from 'react'
import { RiCrosshairLine, RiLoader4Line } from 'react-icons/ri'
import AddressInput from './AddressInput'
import { cn } from '@/utils/cn'

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
      (position) => {
        const { latitude, longitude } = position.coords
        
        if (window.google) {
           const geocoder = new window.google.maps.Geocoder()
           geocoder.geocode({ location: { lat: latitude, lng: longitude } }, (results, status) => {
              if (status === 'OK' && results[0]) {
                 const addressName = results[0].address_components[0]?.long_name + ' ' + (results[0].address_components[1]?.long_name || '') || results[0].formatted_address
                 onChange(addressName)
                 if (onLocationDetect) {
                   onLocationDetect({ lat: latitude, lng: longitude, name: addressName, address: results[0].formatted_address })
                 }
              } else {
                 onChange('Vị trí hiện tại')
                 if (onLocationDetect) {
                   onLocationDetect({ lat: latitude, lng: longitude, name: 'Vị trí hiện tại' })
                 }
              }
              setDetecting(false)
           })
        } else {
           onChange('Vị trí hiện tại')
           if (onLocationDetect) {
             onLocationDetect({ lat: latitude, lng: longitude, name: 'Vị trí hiện tại' })
           }
           setDetecting(false)
        }
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

export default LocationInput