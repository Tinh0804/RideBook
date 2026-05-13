// hooks/useGeolocation.js
import { useState, useEffect } from 'react'

export const useGeolocation = () => {
  const [location, setLocation] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  const getCurrentPosition = () => {
    setLoading(true)
    setError(null)

    if (!navigator.geolocation) {
      setError('Trình duyệt của bạn không hỗ trợ định vị')
      setLoading(false)
      return
    }

    navigator.geolocation.getCurrentPosition(
      (position) => {
        setLocation({
          lat: position.coords.latitude,
          lng: position.coords.longitude,
          name: 'Vị trí hiện tại của bạn'
        })
        setLoading(false)
      },
      (error) => {
        let errorMessage = 'Không thể xác định vị trí'
        switch(error.code) {
          case error.PERMISSION_DENIED:
            errorMessage = 'Bạn cần cấp quyền truy cập vị trí'
            break
          case error.POSITION_UNAVAILABLE:
            errorMessage = 'Không thể lấy thông tin vị trí'
            break
          case error.TIMEOUT:
            errorMessage = 'Yêu cầu định vị quá thời gian'
            break
        }
        setError(errorMessage)
        setLoading(false)
      },
      {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 0
      }
    )
  }

  return { location, loading, error, getCurrentPosition }
}