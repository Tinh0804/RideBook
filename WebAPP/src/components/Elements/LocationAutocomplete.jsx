import React, { useState, useEffect, useRef } from 'react'
import axios from 'axios'
import { RiMapPinLine, RiSearchLine, RiCloseLine } from 'react-icons/ri'
import Input from './Input'
import { cn } from '@/utils/cn'
import Spinner from './Spinner'

const LocationAutocomplete = ({
  value,
  onChange,
  onSelectLocation, // called with { display_name, lat, lon }
  placeholder = "Nhập địa điểm...",
  prefixIcon,
  className
}) => {
  const [query, setQuery] = useState(value || '')
  const [suggestions, setSuggestions] = useState([])
  const [loading, setLoading] = useState(false)
  const [showDropdown, setShowDropdown] = useState(false)
  
  const containerRef = useRef(null)
  const debounceRef = useRef(null)

  // Update query if value prop changes externally
  useEffect(() => {
    if (value !== undefined) {
      setQuery(value)
    }
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

  const fetchSuggestions = async (searchText) => {
    if (!searchText.trim()) {
      setSuggestions([])
      return
    }
    setLoading(true)
    try {
      const res = await axios.get('https://nominatim.openstreetmap.org/search', {
        params: {
          format: 'json',
          q: searchText,
          countrycodes: 'vn', // Limit to Vietnam for better results
          limit: 5
        }
      })
      setSuggestions(res.data)
    } catch (err) {
      console.error('Lỗi khi tìm kiếm địa điểm:', err)
    } finally {
      setLoading(false)
    }
  }

  const handleInputChange = (e) => {
    const text = e.target.value
    setQuery(text)
    onChange?.(text)
    setShowDropdown(true)

    if (debounceRef.current) clearTimeout(debounceRef.current)
    debounceRef.current = setTimeout(() => {
      fetchSuggestions(text)
    }, 500) // 500ms debounce
  }

  const handleSelect = (item) => {
    setQuery(item.display_name)
    onChange?.(item.display_name)
    setShowDropdown(false)
    
    if (onSelectLocation) {
      onSelectLocation({
        name: item.display_name,
        lat: parseFloat(item.lat),
        lng: parseFloat(item.lon)
      })
    }
  }

  const handleClear = () => {
    setQuery('')
    onChange?.('')
    setSuggestions([])
    setShowDropdown(false)
    if (onSelectLocation) onSelectLocation(null)
  }

  return (
    <div className={cn("relative", className)} ref={containerRef}>
      <div className="relative flex items-center">
        {prefixIcon && (
          <span className="absolute left-3 text-content-muted pointer-events-none z-10 flex items-center justify-center">
            {prefixIcon}
          </span>
        )}
        <input
          type="text"
          value={query}
          onChange={handleInputChange}
          onFocus={() => setShowDropdown(true)}
          placeholder={placeholder}
          className={cn(
            'input-field w-full',
            prefixIcon && 'pl-10',
            query && 'pr-10'
          )}
        />
        {query && (
          <button
            type="button"
            onClick={handleClear}
            className="absolute right-3 text-content-muted hover:text-content-main transition-colors"
          >
            <RiCloseLine size={18} />
          </button>
        )}
      </div>

      {showDropdown && (query.trim() !== '') && (
        <div className="absolute top-full left-0 right-0 mt-2 bg-surface border border-surface-border rounded-xl bg-surface-dark z-50 max-h-64 overflow-y-auto">
          {loading ? (
            <div className="p-4 flex justify-center text-brand-400">
              <Spinner size="sm" />
            </div>
          ) : suggestions.length > 0 ? (
            <ul className="py-2">
              {suggestions.map((item, index) => (
                <li
                  key={index}
                  onClick={() => handleSelect(item)}
                  className="px-4 py-3 hover:bg-surface-muted cursor-pointer flex gap-3 transition-colors border-b border-surface-border last:border-0"
                >
                  <RiMapPinLine size={18} className="text-brand-400 shrink-0 mt-0.5" />
                  <div>
                    <p className="text-sm text-content-main line-clamp-1">{item.name || item.display_name.split(',')[0]}</p>
                    <p className="text-xs text-content-muted line-clamp-1 mt-0.5">{item.display_name}</p>
                  </div>
                </li>
              ))}
            </ul>
          ) : (
            <div className="p-4 text-center text-content-muted text-sm">
              Không tìm thấy địa điểm
            </div>
          )}
        </div>
      )}
    </div>
  )
}

export default LocationAutocomplete
