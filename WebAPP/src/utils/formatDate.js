import { format, formatDistanceToNow, parseISO } from 'date-fns'
import { vi } from 'date-fns/locale'

/**
 * Format a date string or Date object
 * @param {string|Date} date
 * @param {string} fmt  - date-fns format string
 */
export const formatDate = (date, fmt = 'dd/MM/yyyy HH:mm') => {
  if (!date) return '—'
  try {
    const d = typeof date === 'string' ? parseISO(date) : date
    return format(d, fmt, { locale: vi })
  } catch {
    return String(date)
  }
}

export const formatDateOnly = (date) => formatDate(date, 'dd/MM/yyyy')
export const formatTime     = (date) => formatDate(date, 'HH:mm')
export const formatRelative = (date) => {
  if (!date) return '—'
  try {
    const d = typeof date === 'string' ? parseISO(date) : date
    return formatDistanceToNow(d, { addSuffix: true, locale: vi })
  } catch {
    return String(date)
  }
}
