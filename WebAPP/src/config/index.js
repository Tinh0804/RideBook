export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/RideBook'
export const APP_NAME     = import.meta.env.VITE_APP_NAME || 'RideBook'
export const WS_URL       = import.meta.env.VITE_WS_URL || 'http://localhost:8080/RideBook/ws'
export const MAPS_KEY     = import.meta.env.VITE_GOOGLE_MAPS_KEY || ''

export const TOKEN_KEY         = 'bookcar_access_token'
export const REFRESH_TOKEN_KEY = 'bookcar_refresh_token'
export const USER_KEY          = 'bookcar_user'

export const ROLES = {
  CUSTOMER: 'CUSTOMER',
  DRIVER:   'DRIVER',
  ADMIN:    'ADMIN',
}

export const BOOKING_STATUS = {
  PENDING:    'PENDING',
  ACCEPTED:   'ACCEPTED',
  PICKING_UP: 'PICKING_UP',
  IN_TRANSIT: 'IN_TRANSIT',
  COMPLETED:  'COMPLETED',
  CANCELLED:  'CANCELLED',
}

export const BOOKING_STATUS_LABEL = {
  PENDING:    'Đang tìm tài xế',
  ACCEPTED:   'Tài xế đã nhận',
  PICKING_UP: 'Đang đến đón',
  IN_TRANSIT: 'Đang trên đường',
  COMPLETED:  'Hoàn thành',
  CANCELLED:  'Đã hủy',
}

export const PAYMENT_METHOD = {
  CASH:   'CASH',
  ONLINE: 'ONLINE',
}

export const PAYMENT_PROVIDERS = {
  VNPAY: 'VNPAY',
  MOMO:  'MOMO',
}

export const VEHICLE_TYPE_ICONS = {
  default: '🚗',
  bike:    '🏍️',
  sedan:   '🚗',
  suv:     '🚙',
  van:     '🚐',
}

export const PAGE_SIZE = 10
