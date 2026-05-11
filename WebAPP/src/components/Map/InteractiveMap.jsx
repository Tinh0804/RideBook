import React, { useState, useEffect } from 'react'
import { MapContainer, TileLayer, Marker, Popup, Polyline, useMap } from 'react-leaflet'
import axios from 'axios'
import L from 'leaflet'

// Fix Leaflet marker icons not showing in Vite/Webpack
delete L.Icon.Default.prototype._getIconUrl
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
})

const customPickupIcon = new L.Icon({
  iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-green.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  shadowSize: [41, 41]
})

const customDropoffIcon = new L.Icon({
  iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-red.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  shadowSize: [41, 41]
})

// Component to handle auto zooming to bounds
const MapBounds = ({ pickup, dropoff, driver }) => {
  const map = useMap()

  useEffect(() => {
    const points = []
    if (pickup) points.push([pickup.lat, pickup.lng])
    if (dropoff) points.push([dropoff.lat, dropoff.lng])
    if (driver) points.push([driver.lat, driver.lng])

    if (points.length === 1) {
      map.setView(points[0], 15)
    } else if (points.length > 1) {
      const bounds = L.latLngBounds(points)
      map.fitBounds(bounds, { padding: [50, 50] })
    }
  }, [map, pickup, dropoff, driver])

  return null
}

const InteractiveMap = ({ pickup, dropoff, driver, className }) => {
  const [routeCoords, setRouteCoords] = useState([])

  // Default center (Hanoi)
  const defaultCenter = [21.0285, 105.8542]

  useEffect(() => {
    const getRoute = async () => {
      if (pickup?.lat && pickup?.lng && dropoff?.lat && dropoff?.lng) {
        try {
          const res = await axios.get(
            `https://router.project-osrm.org/route/v1/driving/${pickup.lng},${pickup.lat};${dropoff.lng},${dropoff.lat}?overview=full&geometries=geojson`
          )
          if (res.data.routes && res.data.routes[0]) {
            const coords = res.data.routes[0].geometry.coordinates.map(coord => [coord[1], coord[0]]) // reverse lon,lat to lat,lon
            setRouteCoords(coords)
          }
        } catch (error) {
          console.error("Error fetching route:", error)
          setRouteCoords([])
        }
      } else {
        setRouteCoords([])
      }
    }
    getRoute()
  }, [pickup, dropoff])

  return (
    <div className={`w-full h-full relative z-0 ${className || ''}`}>
      <MapContainer
        center={pickup ? [pickup.lat, pickup.lng] : defaultCenter}
        zoom={pickup ? 15 : 13}
        style={{ height: '100%', width: '100%' }}
        zoomControl={false}
      >
        <TileLayer
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
        />
        
        {pickup && (
          <Marker position={[pickup.lat, pickup.lng]} icon={customPickupIcon}>
            <Popup>Điểm đón: {pickup.name}</Popup>
          </Marker>
        )}
        
        {dropoff && (
          <Marker position={[dropoff.lat, dropoff.lng]} icon={customDropoffIcon}>
            <Popup>Điểm đến: {dropoff.name}</Popup>
          </Marker>
        )}

        {driver && (
          <Marker position={[driver.lat, driver.lng]}>
             <Popup>Vị trí tài xế</Popup>
          </Marker>
        )}
        
        {routeCoords.length > 0 && (
          <Polyline positions={routeCoords} color="#10b981" weight={5} opacity={0.8} />
        )}

        <MapBounds pickup={pickup} dropoff={dropoff} driver={driver} />
      </MapContainer>
    </div>
  )
}

export default InteractiveMap
