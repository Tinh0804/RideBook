// components/Map/AddressInput.jsx
import React, { useState, useRef, useEffect } from 'react'
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
  disabled = false,
  showDetectButton = true
}) => {
  const [query, setQuery] = useState(value || '')
  const [suggestions, setSuggestions] = useState([])
  const [loading, setLoading] = useState(false)
  const [showDropdown, setShowDropdown] = useState(false)
  const [detectingLocation, setDetectingLocation] = useState(false)

  const containerRef = useRef(null)
  const inputRef = useRef(null)
  const debounceRef = useRef(null)
  const isInternalChangeRef = useRef(false)

  const autocompleteService = useRef(null)
  const geocoder = useRef(null)
  const sessionToken = useRef(null)

  useEffect(() => {
    if (window.google && !autocompleteService.current) {
      autocompleteService.current = new window.google.maps.places.AutocompleteService()
      geocoder.current = new window.google.maps.Geocoder()
      sessionToken.current = new window.google.maps.places.AutocompleteSessionToken()
    }
  }, [])

  useEffect(() => {
    if (!isInternalChangeRef.current && value !== undefined && value !== query) {
      setQuery(value)
    }
    isInternalChangeRef.current = false
  }, [value])

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (containerRef.current && !containerRef.current.contains(event.target)) {
        setShowDropdown(false)
      }
    }
    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [])

  const fetchSuggestions = (searchText) => {
    if (!searchText.trim() || !autocompleteService.current) {
      setSuggestions([])
      return
    }

    setLoading(true)
    autocompleteService.current.getPlacePredictions({
      input: searchText,
      sessionToken: sessionToken.current,
      componentRestrictions: { country: 'vn' }
    }, (predictions, status) => {
      setLoading(false)
      if (status === window.google.maps.places.PlacesServiceStatus.OK && predictions) {
        setSuggestions(predictions)
      } else {
        setSuggestions([])
      }
    })
  }

  const handleInputChange = (e) => {
    const text = e.target.value
    setQuery(text)
    isInternalChangeRef.current = true
    onChange?.(text)
    setShowDropdown(true)

    if (debounceRef.current) clearTimeout(debounceRef.current)
    debounceRef.current = setTimeout(() => {
      if (text.trim()) {
        fetchSuggestions(text)
      } else {
        setSuggestions([])
      }
    }, 500)
  }

  const handleSelectSuggestion = (suggestion) => {
    const selectedName = suggestion.structured_formatting?.main_text || suggestion.description

    setQuery(selectedName)
    setShowDropdown(false)
    setSuggestions([])
    isInternalChangeRef.current = true
    onChange?.(selectedName)

    if (onLocationDetect && geocoder.current) {
      geocoder.current.geocode({ placeId: suggestion.place_id }, (results, status) => {
        if (status === 'OK' && results[0]) {
          // Reset session token
          sessionToken.current = new window.google.maps.places.AutocompleteSessionToken()

          onLocationDetect({
            name: selectedName,
            lat: results[0].geometry.location.lat(),
            lng: results[0].geometry.location.lng(),
            address: results[0].formatted_address,
            isSuggestion: true
          })
        }
      })
    }
  }

  const handleClear = () => {
    setQuery('')
    setSuggestions([])
    setShowDropdown(false)
    isInternalChangeRef.current = true
    onChange?.('')
    if (onLocationDetect) {
      onLocationDetect(null)
    }
  }

  const detectCurrentLocation = () => {
    setDetectingLocation(true)

    if (!navigator.geolocation) {
      alert('Trình duyệt của bạn không hỗ trợ định vị')
      setDetectingLocation(false)
      return
    }

    navigator.geolocation.getCurrentPosition(
      (position) => {
        const { latitude, longitude } = position.coords

        if (geocoder.current) {
          geocoder.current.geocode({ location: { lat: latitude, lng: longitude } }, (results, status) => {
            if (status === 'OK' && results[0]) {
              const addressName = results[0].address_components[0]?.long_name + ' ' + (results[0].address_components[1]?.long_name || '') || results[0].formatted_address

              setQuery(addressName)
              setShowDropdown(false)
              setSuggestions([])
              isInternalChangeRef.current = true
              onChange?.(addressName)

              if (onLocationDetect) {
                onLocationDetect({
                  name: addressName,
                  lat: latitude,
                  lng: longitude,
                  address: results[0].formatted_address,
                  isCurrentLocation: true
                })
              }
            } else {
              // Fallback
              const fallbackName = `Vị trí hiện tại (${latitude.toFixed(4)}, ${longitude.toFixed(4)})`
              setQuery(fallbackName)
              onChange?.(fallbackName)
              if (onLocationDetect) onLocationDetect({ name: fallbackName, lat: latitude, lng: longitude, isCurrentLocation: true })
            }
            setDetectingLocation(false)
          })
        } else {
          const fallbackName = `Vị trí hiện tại (${latitude.toFixed(4)}, ${longitude.toFixed(4)})`
          setQuery(fallbackName)
          onChange?.(fallbackName)
          if (onLocationDetect) onLocationDetect({ name: fallbackName, lat: latitude, lng: longitude, isCurrentLocation: true })
          setDetectingLocation(false)
        }
      },
      (error) => {
        let errorMessage = 'Không thể xác định vị trí'
        if (error.code === error.PERMISSION_DENIED) errorMessage = 'Vui lòng cấp quyền truy cập vị trí để sử dụng tính năng này'
        alert(errorMessage)
        setDetectingLocation(false)
      },
      { enableHighAccuracy: true, timeout: 10000, maximumAge: 0 }
    )
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
            if (query && query.trim()) fetchSuggestions(query)
          }}
          placeholder={placeholder}
          disabled={disabled}
          className={cn(
            "w-full",
            prefixIcon && "pl-10",
            showDetectButton
              ? (query || detectingLocation) && "pr-16"
              : query && "pr-10"
          )}
        />

        <div className="absolute right-2 top-1/2 -translate-y-1/2 flex items-center gap-1">
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

          {showDetectButton && (
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
          )}
        </div>
      </div>

      {showDropdown && query.trim() !== '' && (
        <div className="absolute top-full left-0 right-0 mt-2 bg-surface-card border border-surface-border rounded-xl shadow-lg z-50 max-h-80 overflow-y-auto">
          {loading ? (
            <div className="p-4 flex justify-center">
              <RiLoader4Line className="animate-spin text-brand-500" size={24} />
            </div>
          ) : suggestions.length > 0 ? (
            <ul className="py-2">
              {suggestions.map((suggestion) => (
                <li
                  key={suggestion.place_id}
                  onClick={() => handleSelectSuggestion(suggestion)}
                  className="px-4 py-3 hover:bg-surface-hover cursor-pointer flex gap-3 transition-colors border-b border-surface-border last:border-0"
                >
                  <RiMapPinLine size={18} className="text-brand-400 shrink-0 mt-0.5" />
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium text-content-main line-clamp-1">
                      {suggestion.structured_formatting?.main_text || suggestion.description}
                    </p>
                    <p className="text-xs text-content-muted line-clamp-1 mt-0.5">
                      {suggestion.structured_formatting?.secondary_text || ''}
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