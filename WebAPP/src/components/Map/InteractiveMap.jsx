import React, { useState, useEffect, useRef, useCallback, useMemo } from 'react'
import { GoogleMap, Marker, DirectionsRenderer } from '@react-google-maps/api'

const mapContainerStyle = {
  width: '100%',
  height: '100%'
}

// Map styles for dark mode
const darkMapStyle = [
  { elementType: "geometry", stylers: [{ color: "#242f3e" }] },
  { elementType: "labels.text.stroke", stylers: [{ color: "#242f3e" }] },
  { elementType: "labels.text.fill", stylers: [{ color: "#746855" }] },
  {
    featureType: "administrative.locality",
    elementType: "labels.text.fill",
    stylers: [{ color: "#d59563" }],
  },
  {
    featureType: "poi",
    elementType: "labels.text.fill",
    stylers: [{ color: "#d59563" }],
  },
  {
    featureType: "poi.park",
    elementType: "geometry",
    stylers: [{ color: "#263c3f" }],
  },
  {
    featureType: "poi.park",
    elementType: "labels.text.fill",
    stylers: [{ color: "#6b9a76" }],
  },
  {
    featureType: "road",
    elementType: "geometry",
    stylers: [{ color: "#38414e" }],
  },
  {
    featureType: "road",
    elementType: "geometry.stroke",
    stylers: [{ color: "#212a37" }],
  },
  {
    featureType: "road",
    elementType: "labels.text.fill",
    stylers: [{ color: "#9ca5b3" }],
  },
  {
    featureType: "road.highway",
    elementType: "geometry",
    stylers: [{ color: "#746855" }],
  },
  {
    featureType: "road.highway",
    elementType: "geometry.stroke",
    stylers: [{ color: "#1f2835" }],
  },
  {
    featureType: "road.highway",
    elementType: "labels.text.fill",
    stylers: [{ color: "#f3d19c" }],
  },
  {
    featureType: "transit",
    elementType: "geometry",
    stylers: [{ color: "#2f3948" }],
  },
  {
    featureType: "transit.station",
    elementType: "labels.text.fill",
    stylers: [{ color: "#d59563" }],
  },
  {
    featureType: "water",
    elementType: "geometry",
    stylers: [{ color: "#17263c" }],
  },
  {
    featureType: "water",
    elementType: "labels.text.fill",
    stylers: [{ color: "#515c6d" }],
  },
  {
    featureType: "water",
    elementType: "labels.text.stroke",
    stylers: [{ color: "#17263c" }],
  },
]

const InteractiveMap = ({ pickup, dropoff, driver, className, selectingMode = false, onLocationSelect, initialCenter }) => {
  const [directions, setDirections] = useState(null)
  const mapRef = useRef(null)
  const geocoderRef = useRef(null)

  const defaultCenter = useMemo(() => ({ lat: 16.0544, lng: 108.2022 }), [])

  const mapCenter = useMemo(() => {
    if (initialCenter) return { lat: initialCenter[0], lng: initialCenter[1] }
    if (pickup?.lat && pickup?.lng) return { lat: Number(pickup.lat), lng: Number(pickup.lng) }
    if (driver?.lat && driver?.lng) return { lat: Number(driver.lat), lng: Number(driver.lng) }
    return defaultCenter
  }, [initialCenter, pickup?.lat, pickup?.lng, driver?.lat, driver?.lng, defaultCenter])

  const onMapLoad = useCallback((map) => {
    mapRef.current = map
    geocoderRef.current = new window.google.maps.Geocoder()
  }, [])

  // Auto zoom to bounds (Run ONCE when pickup/dropoff are set so camera doesn't jump on every driver movement)
  const boundsFittedRef = useRef(false)
  useEffect(() => {
    if (directions) return // Let DirectionsRenderer handle zoom when route exists

    if (mapRef.current && !selectingMode) {
      const bounds = new window.google.maps.LatLngBounds()
      let hasPoints = false

      if (pickup?.lat && pickup?.lng) {
        bounds.extend({ lat: Number(pickup.lat), lng: Number(pickup.lng) })
        hasPoints = true
      }
      if (dropoff?.lat && dropoff?.lng) {
        bounds.extend({ lat: Number(dropoff.lat), lng: Number(dropoff.lng) })
        hasPoints = true
      }
      if (!pickup && !dropoff && driver?.lat && driver?.lng) {
        bounds.extend({ lat: Number(driver.lat), lng: Number(driver.lng) })
        hasPoints = true
      }

      if (hasPoints && !boundsFittedRef.current) {
        boundsFittedRef.current = true
        setTimeout(() => {
          if (mapRef.current) {
            const padding = { top: 60, bottom: 60, left: 60, right: 60 }
            mapRef.current.fitBounds(bounds, padding)
            const listener = window.google.maps.event.addListenerOnce(mapRef.current, 'idle', () => {
              if (mapRef.current.getZoom() > 16) mapRef.current.setZoom(16)
            })
          }
        }, 100)
      }
    }
  }, [pickup?.lat, pickup?.lng, dropoff?.lat, dropoff?.lng, selectingMode, directions])

  // Get Directions
  useEffect(() => {
    if (pickup?.lat && pickup?.lng && dropoff?.lat && dropoff?.lng && !selectingMode) {
      setDirections(null) // Force unmount previous renderer to avoid route ghosting
      const directionsService = new window.google.maps.DirectionsService()
      directionsService.route(
        {
          origin: { lat: Number(pickup.lat), lng: Number(pickup.lng) },
          destination: { lat: Number(dropoff.lat), lng: Number(dropoff.lng) },
          travelMode: window.google.maps.TravelMode.DRIVING
        },
        (result, status) => {
          if (status === window.google.maps.DirectionsStatus.OK) {
            setDirections(result)
          } else {
            console.error(`Error fetching directions ${status}`)
            setDirections(null)
          }
        }
      )
    } else {
      setDirections(null)
    }
  }, [pickup?.lat, pickup?.lng, dropoff?.lat, dropoff?.lng, selectingMode])

  const handleCenterChanged = () => {
    if (!selectingMode || !onLocationSelect || !mapRef.current || !geocoderRef.current) return
    const currentCenter = mapRef.current.getCenter()
    const lat = currentCenter.lat()
    const lng = currentCenter.lng()

    // Reverse geocoding
    geocoderRef.current.geocode({ location: { lat, lng } }, (results, status) => {
      if (status === 'OK' && results[0]) {
        let addressName = results[0].formatted_address
        const route = results[0].address_components.find(c => c.types.includes('route'))
        const sublocal = results[0].address_components.find(c => c.types.includes('sublocality'))
        if (route && sublocal) {
          addressName = `${route.long_name}, ${sublocal.long_name}`
        }
        onLocationSelect({ lat, lng, name: addressName || results[0].formatted_address, fullAddress: results[0].formatted_address })
      } else {
        onLocationSelect({ lat, lng, name: `Vị trí (${lat.toFixed(4)}, ${lng.toFixed(4)})` })
      }
    })
  }

  // Use debounced center change to avoid API quota limits
  const debouncedCenterChangedRef = useRef(null)
  const onIdle = useCallback(() => {
    if (debouncedCenterChangedRef.current) clearTimeout(debouncedCenterChangedRef.current)
    debouncedCenterChangedRef.current = setTimeout(handleCenterChanged, 500)
  }, [selectingMode, onLocationSelect])

  return (
    <div className={`w-full h-full relative z-0 ${className || ''}`}>
      {selectingMode && (
        <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-full z-[1000] pointer-events-none drop-shadow-md">
          <img
            src="https://maps.gstatic.com/mapfiles/api-3/images/spotlight-poi2_hdpi.png"
            alt="center pin"
            className="w-6 h-10 object-contain -mt-2"
          />
          <div className="w-2 h-1 bg-black/30 rounded-[50%] absolute bottom-[-2px] left-1/2 -translate-x-1/2 blur-[1px]"></div>
        </div>
      )}

      <GoogleMap
        mapContainerStyle={mapContainerStyle}
        center={mapCenter}
        zoom={14}
        onLoad={onMapLoad}
        onIdle={selectingMode ? onIdle : undefined}
        options={{
          disableDefaultUI: true,
          zoomControl: false,
        }}
      >
        {pickup && !selectingMode && !directions && (
          <Marker
            position={{ lat: Number(pickup.lat), lng: Number(pickup.lng) }}
          />
        )}

        {dropoff && !selectingMode && !directions && (
          <Marker
            position={{ lat: Number(dropoff.lat), lng: Number(dropoff.lng) }}
          />
        )}

        {driver && driver.lat && driver.lng && !selectingMode && (
          <Marker
            position={{ lat: Number(driver.lat), lng: Number(driver.lng) }}
            icon={{
              path: 'M29.395,0H17.636c-3.117,0-5.643,3.467-5.643,6.584v34.804c0,3.116,2.526,5.644,5.643,5.644h11.759   c3.116,0,5.644-2.527,5.644-5.644V6.584C35.037,3.467,32.511,0,29.395,0z M34.05,14.188v11.665l-2.729,0.351v-4.806L34.05,14.188z    M32.618,10.773c-1.016,3.9-2.219,8.51-2.219,8.51H16.631l-2.222-8.51C14.41,10.773,23.293,7.755,32.618,10.773z M15.741,21.713   v4.492l-2.73-0.349V14.502L15.741,21.713z M13.011,37.938V27.579l2.73,0.343v8.196L13.011,37.938z M14.568,40.882l2.218-3.336   h13.771l2.219,3.336H14.568z M31.321,35.805v-7.872l2.729-0.355v10.048L31.321,35.805z',
              fillColor: '#22c55e',
              fillOpacity: 1,
              strokeWeight: 1.5,
              strokeColor: '#ffffff',
              scale: 0.8,
              anchor: window.google?.maps?.Point ? new window.google.maps.Point(23, 23) : undefined
            }}
          />
        )}

        {directions && !selectingMode && (
          <DirectionsRenderer
            key={directions.routes?.[0]?.overview_polyline || Date.now()}
            directions={directions}
            options={{
              suppressMarkers: false,
              polylineOptions: {
                strokeColor: '#10b981',
                strokeWeight: 5,
                strokeOpacity: 0.8
              }
            }}
          />
        )}
      </GoogleMap>
    </div>
  )
}

export default InteractiveMap
