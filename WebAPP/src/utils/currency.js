/**
 * Format a number as Vietnamese Dong currency
 * @param {number} amount
 * @param {boolean} compact  - use K/M shorthand
 */
export const formatCurrency = (amount, compact = false) => {
  if (amount == null || isNaN(amount)) return '—'
  if (compact) {
    if (amount >= 1_000_000) return `${(amount / 1_000_000).toFixed(1)}M ₫`
    if (amount >= 1_000)     return `${(amount / 1_000).toFixed(0)}K ₫`
  }
  return new Intl.NumberFormat('vi-VN', {
    style:    'currency',
    currency: 'VND',
  }).format(amount)
}

/**
 * Format distance in km
 * @param {number} km
 */
export const formatDistance = (km) => {
  if (km == null) return '—'
  if (km < 1) return `${(km * 1000).toFixed(0)} m`
  return `${Number(km).toFixed(1)} km`
}

/**
 * Format duration in minutes
 * @param {number} minutes
 */
export const formatDuration = (minutes) => {
  if (minutes == null) return '—'
  if (minutes < 60) return `${minutes} phút`
  const h = Math.floor(minutes / 60)
  const m = minutes % 60
  return m ? `${h}h ${m}p` : `${h} giờ`
}
