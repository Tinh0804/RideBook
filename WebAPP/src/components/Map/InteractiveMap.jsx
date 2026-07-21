import React, { useState, useEffect, useRef, useCallback } from 'react'
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

const getVehicleIcon = (driver) => {
  if (driver?.vehicleTypeIcon) {
    return driver.vehicleTypeIcon;
  }
  if (!driver || !driver.vehicleTypeName) {
    return 'https://cdn-icons-png.flaticon.com/128/1048/1048315.png'; // default 4-seater
  }
  const type = driver.vehicleTypeName.toLowerCase();
  if (type.includes('máy') || type.includes('bike')) {
    return 'https://cdn-icons-png.flaticon.com/128/1048/1048315.png'; // motorbike
  }
  if (type.includes('7') || type.includes('suv')) {
    return 'https://cdn-icons-png.flaticon.com/128/1048/1048313.png'; // 7-seater
  }
  if (type.includes('sang trọng') || type.includes('luxury') || type.includes('vip')) {
    return 'https://cdn-icons-png.flaticon.com/128/1048/1048333.png'; // luxury
  }
  return 'https://cdn-icons-png.flaticon.com/128/1048/1048315.png'; // default 4-seater
}

const InteractiveMap = ({ pickup, dropoff, driver, className, selectingMode = false, onLocationSelect, initialCenter, onRouteReady }) => {
  const [directions, setDirections] = useState(null)
  const mapRef = useRef(null)
  const geocoderRef = useRef(null)

  const defaultCenter = { lat: 21.0285, lng: 105.8542 }
  const [mapCenter] = useState(
    initialCenter ? { lat: initialCenter[0], lng: initialCenter[1] } : (pickup ? { lat: pickup.lat, lng: pickup.lng } : defaultCenter)
  )

  const onMapLoad = useCallback((map) => {
    mapRef.current = map
    geocoderRef.current = new window.google.maps.Geocoder()
  }, [])

  // Auto zoom to bounds
  useEffect(() => {
    if (directions) return // Let DirectionsRenderer handle zoom when route exists

    if (mapRef.current && !selectingMode) {
      const bounds = new window.google.maps.LatLngBounds()
      let hasPoints = false

      if (pickup?.lat && pickup?.lng) {
        bounds.extend({ lat: pickup.lat, lng: pickup.lng })
        hasPoints = true
      }
      if (dropoff?.lat && dropoff?.lng) {
        bounds.extend({ lat: dropoff.lat, lng: dropoff.lng })
        hasPoints = true
      }
      if (driver?.lat && driver?.lng) {
        bounds.extend({ lat: driver.lat, lng: driver.lng })
        hasPoints = true
      }

      if (hasPoints) {
        // Use a small timeout to ensure the map is ready for bounds change
        setTimeout(() => {
          if (mapRef.current) {
            mapRef.current.fitBounds(bounds)
            const padding = { top: 50, bottom: 50, left: 50, right: 50 }
            mapRef.current.fitBounds(bounds, padding)
            const listener = window.google.maps.event.addListenerOnce(mapRef.current, 'idle', () => {
              if (mapRef.current.getZoom() > 16) mapRef.current.setZoom(16)
            })
          }
        }, 100)
      }
    }
  }, [pickup, dropoff, driver, selectingMode, directions])

  // Get Directions
  useEffect(() => {
    if (pickup?.lat && pickup?.lng && dropoff?.lat && dropoff?.lng && !selectingMode) {
      setDirections(null) // Force unmount previous renderer to avoid route ghosting
      const directionsService = new window.google.maps.DirectionsService()
      directionsService.route(
        {
          origin: { lat: pickup.lat, lng: pickup.lng },
          destination: { lat: dropoff.lat, lng: dropoff.lng },
          travelMode: window.google.maps.TravelMode.DRIVING
        },
        (result, status) => {
          if (status === window.google.maps.DirectionsStatus.OK) {
            setDirections(result)
            if (onRouteReady && result.routes && result.routes.length > 0) {
              onRouteReady(result.routes[0].overview_path)
            }
          } else {
            console.error(`Error fetching directions ${status}`)
            setDirections(null)
          }
        }
      )
    } else {
      setDirections(null)
    }
  }, [pickup, dropoff, selectingMode])

  const handleCenterChanged = () => {
    if (!selectingMode || !onLocationSelect || !mapRef.current || !geocoderRef.current) return
    const currentCenter = mapRef.current.getCenter()
    const lat = currentCenter.lat()
    const lng = currentCenter.lng()

    // Reverse geocoding
    geocoderRef.current.geocode({ location: { lat, lng } }, (results, status) => {
      if (status === 'OK' && results[0]) {
        // Find a good name
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
        zoom={13}
        onLoad={onMapLoad}
        onIdle={selectingMode ? onIdle : undefined}
        options={{
          disableDefaultUI: true,
          zoomControl: false,
        }}
      >
        {pickup && !selectingMode && !directions && (
          <Marker
            position={{ lat: pickup.lat, lng: pickup.lng }}
          />
        )}

        {dropoff && !selectingMode && !directions && (
          <Marker
            position={{ lat: dropoff.lat, lng: dropoff.lng }}
          />
        )}

        {driver && !selectingMode && (
          <Marker
            key={driver.lat ? `driver-${driver.lat}-${driver.lng}` : 'driver-marker'}
            position={{ lat: Number(driver.lat), lng: Number(driver.lng) }}
            icon={{
               url: getVehicleIcon(driver),
               scaledSize: new window.google.maps.Size(40, 40),
               anchor: new window.google.maps.Point(20, 35)
            }}
            zIndex={99999}
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
