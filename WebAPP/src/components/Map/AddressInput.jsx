// components/Map/AddressInput.jsx
import React, { useState, useRef, useEffect } from 'react'
import axios from 'axios'
import { RiMapPinLine, RiCrosshairLine, RiLoader4Line, RiCloseLine } from 'react-icons/ri'
import Input from '@/components/Elements/Input'
import { cn } from '@/utils/cn'

const AddressInput = ({ 
  value, 
  onChange, 
  placeholder = "Nhập địa điểm...", 
  onLocationDetect,
  className,
  prefixIcon,
  disabled = false
}) => {
  const [query, setQuery] = useState(value || '')
  const [suggestions, setSuggestions] = useState([])
  const [loading, setLoading] = useState(false)
  const [showDropdown, setShowDropdown] = useState(false)
  const [detectingLocation, setDetectingLocation] = useState(false)
  
  const containerRef = useRef(null)
  const inputRef = useRef(null)
  const debounceRef = useRef(null)
  const isInternalChangeRef = useRef(false) // Track internal changes

  // Update query if value prop changes externally (only for external changes)
  useEffect(() => {
    if (!isInternalChangeRef.current && value !== undefined && value !== query) {
      setQuery(value)
    }
    isInternalChangeRef.current = false
  }, [value])

  // Handle click outside
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (containerRef.current && !containerRef.current.contains(event.target)) {
        setShowDropdown(false)
      }
    }
    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [])

  // Fetch address suggestions from Nominatim API
  const fetchSuggestions = async (searchText) => {
    if (!searchText.trim()) {
      setSuggestions([])
      return
    }
    
    setLoading(true)
    try {
      const response = await axios.get('https://nominatim.openstreetmap.org/search', {
        params: {
          format: 'json',
          q: searchText,
          countrycodes: 'vn',
          limit: 5,
          addressdetails: 1,
          'accept-language': 'vi'
        }
      })
      
      setSuggestions(response.data.map(item => ({
        name: item.display_name,
        display_name: item.display_name,
        lat: parseFloat(item.lat),
        lon: parseFloat(item.lon),
        address: item.address
      })))
    } catch (error) {
      console.error('Lỗi khi tìm kiếm địa điểm:', error)
      setSuggestions([])
    } finally {
      setLoading(false)
    }
  }

  // Handle input change
  const handleInputChange = (e) => {
    const text = e.target.value
    setQuery(text)
    
    // Mark as internal change to avoid external override
    isInternalChangeRef.current = true
    
    // Notify parent
    onChange?.(text)
    
    // Show dropdown and fetch suggestions
    setShowDropdown(true)

    // Debounce API calls
    if (debounceRef.current) clearTimeout(debounceRef.current)
    debounceRef.current = setTimeout(() => {
      if (text.trim()) {
        fetchSuggestions(text)
      } else {
        setSuggestions([])
      }
    }, 500)
  }

  // Handle suggestion selection
  const handleSelectSuggestion = (suggestion) => {
    const selectedName = suggestion.display_name || suggestion.name
    
    // Update internal state
    setQuery(selectedName)
    setShowDropdown(false)
    setSuggestions([])
    
    // Mark as internal change
    isInternalChangeRef.current = true
    
    // Notify parent
    onChange?.(selectedName)
    
    // Notify parent with location data
    if (onLocationDetect) {
      onLocationDetect({
        name: selectedName,
        lat: suggestion.lat,
        lng: suggestion.lon,
        address: suggestion.address,
        isSuggestion: true
      })
    }
  }

  // Clear input
  const handleClear = () => {
    setQuery('')
    setSuggestions([])
    setShowDropdown(false)
    
    // Mark as internal change
    isInternalChangeRef.current = true
    
    // Notify parent
    onChange?.('')
    
    // Notify parent that location is cleared
    if (onLocationDetect) {
      onLocationDetect(null)
    }
  }

  // Detect current location
  const detectCurrentLocation = () => {
    setDetectingLocation(true)
    
    if (!navigator.geolocation) {
      alert('Trình duyệt của bạn không hỗ trợ định vị')
      setDetectingLocation(false)
      return
    }

    navigator.geolocation.getCurrentPosition(
      async (position) => {
        const { latitude, longitude } = position.coords
        
        try {
          const response = await axios.get('https://nominatim.openstreetmap.org/reverse', {
            params: {
              format: 'json',
              lat: latitude,
              lon: longitude,
              'accept-language': 'vi',
              addressdetails: 1
            }
          })
          
          const data = response.data
          const addressName = data.display_name || 'Vị trí hiện tại của bạn'
          
          // Update internal state
          setQuery(addressName)
          setShowDropdown(false)
          setSuggestions([])
          
          // Mark as internal change
          isInternalChangeRef.current = true
          
          // Notify parent
          onChange?.(addressName)
          
          // Notify parent with location data
          if (onLocationDetect) {
            onLocationDetect({
              name: addressName,
              lat: latitude,
              lng: longitude,
              address: data.address,
              isCurrentLocation: true
            })
          }
        } catch (error) {
          console.error('Error reverse geocoding:', error)
          const fallbackName = `Vị trí hiện tại (${latitude.toFixed(4)}, ${longitude.toFixed(4)})`
          
          setQuery(fallbackName)
          onChange?.(fallbackName)
          
          if (onLocationDetect) {
            onLocationDetect({
              name: fallbackName,
              lat: latitude,
              lng: longitude,
              isCurrentLocation: true
            })
          }
        }
        
        setDetectingLocation(false)
      },
      (error) => {
        let errorMessage = 'Không thể xác định vị trí'
        switch(error.code) {
          case error.PERMISSION_DENIED:
            errorMessage = 'Vui lòng cấp quyền truy cập vị trí để sử dụng tính năng này'
            break
          case error.POSITION_UNAVAILABLE:
            errorMessage = 'Không thể lấy thông tin vị trí'
            break
          case error.TIMEOUT:
            errorMessage = 'Yêu cầu định vị quá thời gian'
            break
        }
        alert(errorMessage)
        setDetectingLocation(false)
      },
      {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 0
      }
    )
  }

  // Format display name for suggestion item
  const getDisplayName = (suggestion) => {
    const mainName = suggestion.name?.split(',')[0] || suggestion.display_name?.split(',')[0] || 'Địa điểm'
    return mainName
  }

  return (
    <div className={cn("relative flex-1", className)} ref={containerRef}>
      <div className="relative flex items-center">
        {prefixIcon && (
          <span className="absolute left-3 text-content-muted pointer-events-none z-10">
            {prefixIcon}
          </span>
        )}
        
        <Input
          ref={inputRef}
          type="text"
          value={query}
          onChange={handleInputChange}
          onFocus={() => {
            setShowDropdown(true)
            if (query && query.trim()) {
              fetchSuggestions(query)
            }
          }}
          placeholder={placeholder}
          disabled={disabled}
          className={cn(
            "w-full",
            prefixIcon && "pl-10",
            (query || detectingLocation) && "pr-16"
          )}
        />
        
        <div className="absolute right-2 top-1/2 -translate-y-1/2 flex items-center gap-1">
          {/* Clear button */}
          {query && !detectingLocation && (
            <button
              type="button"
              onClick={handleClear}
              className="p-1.5 rounded-lg hover:bg-surface-border transition-colors"
              title="Xóa"
            >
              <RiCloseLine size={16} className="text-content-muted" />
            </button>
          )}
          
          {/* Location detection button */}
          <button
            type="button"
            onClick={detectCurrentLocation}
            disabled={detectingLocation || disabled}
            className="p-1.5 rounded-lg hover:bg-surface-border transition-colors disabled:opacity-50"
            title="Sử dụng vị trí hiện tại"
          >
            {detectingLocation ? (
              <RiLoader4Line className="animate-spin text-brand-500" size={18} />
            ) : (
              <RiCrosshairLine className="text-content-muted hover:text-brand-500" size={18} />
            )}
          </button>
        </div>
      </div>

      {/* Suggestions dropdown */}
      {showDropdown && query.trim() !== '' && (
        <div className="absolute top-full left-0 right-0 mt-2 bg-surface-card border border-surface-border rounded-xl shadow-lg z-50 max-h-80 overflow-y-auto">
          {loading ? (
            <div className="p-4 flex justify-center">
              <RiLoader4Line className="animate-spin text-brand-500" size={24} />
            </div>
          ) : suggestions.length > 0 ? (
            <ul className="py-2">
              {suggestions.map((suggestion, index) => (
                <li
                  key={index}
                  onClick={() => handleSelectSuggestion(suggestion)}
                  className="px-4 py-3 hover:bg-surface-hover cursor-pointer flex gap-3 transition-colors border-b border-surface-border last:border-0"
                >
                  <RiMapPinLine size={18} className="text-brand-400 shrink-0 mt-0.5" />
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium text-content-main line-clamp-1">
                      {getDisplayName(suggestion)}
                    </p>
                    <p className="text-xs text-content-muted line-clamp-1 mt-0.5">
                      {suggestion.display_name || suggestion.name}
                    </p>
                  </div>
                </li>
              ))}
            </ul>
          ) : (
            <div className="p-4 text-center text-content-muted text-sm">
              <RiMapPinLine className="mx-auto mb-2 opacity-50" size={24} />
              Không tìm thấy địa điểm
            </div>
          )}
        </div>
      )}
    </div>
  )
}

export default AddressInput