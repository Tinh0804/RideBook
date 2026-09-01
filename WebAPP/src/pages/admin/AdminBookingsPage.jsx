import { useState, useEffect, useCallback, useMemo } from 'react'
import { bookingApi } from '@/features/booking/api/bookingApi'
import { driverApi } from '@/features/driver/api/driverApi'
import Spinner from '@/components/Elements/Spinner'
import {
  RiSearchLine, RiEyeLine, RiCloseLine, RiMapPinLine,
  RiUserLine, RiCarLine, RiDeleteBin6Line,
  RiUserAddLine, RiCalendarLine, RiFilterLine, RiDownloadLine,
  RiArrowUpDownLine, RiRefreshLine
} from 'react-icons/ri'
import { cn } from '@/utils/cn'
import { BookingStatus } from '@/constants/enums'
import { BOOKING_STATUS_LABEL } from '@/config'
import { formatCurrency } from '@/utils/currency'
import { useDebounce } from '@/hooks/useDebounce'
import { exportToCSV } from '@/utils/exportUtils'

const Modal = ({ open, onClose, title, children }) => {
  if (!open) return null
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm" onClick={onClose}>
      <div className="card w-full max-w-3xl max-h-[90vh] mx-4 flex flex-col p-0 animate-in fade-in zoom-in-95" onClick={(e) => e.stopPropagation()}>
        <div className="flex items-center justify-between px-6 py-4 border-b border-surface-border">
          <h3 className="font-display text-lg font-bold text-content-main">{title}</h3>
          <button onClick={onClose} className="p-1.5 rounded-lg hover:bg-surface-border/40 text-content-muted transition-colors">
            <RiCloseLine size={20} />
          </button>
        </div>
        <div className="overflow-y-auto flex-1 p-6">{children}</div>
      </div>
    </div>
  )
}

const STATUS_TABS = [
  { key: 'ALL', label: 'Tất cả' },
  { key: BookingStatus.PENDING, label: 'Đang chờ' },
  { key: BookingStatus.QUEUED || 'QUEUED', label: 'Đã lên lịch' },
  { key: BookingStatus.ACCEPTED, label: 'Đã nhận' },
  { key: BookingStatus.ARRIVED, label: 'Đến đón' },
  { key: BookingStatus.IN_PROGRESS, label: 'Đang đi' },
  { key: BookingStatus.COMPLETED, label: 'Hoàn thành' },
  { key: BookingStatus.CANCELLED, label: 'Đã huỷ' },
]

const STATUS_BADGE = {
  [BookingStatus.PENDING]: 'bg-yellow-500/10 text-yellow-400 border-yellow-500/20',
  [BookingStatus.QUEUED || 'QUEUED']: 'bg-purple-500/10 text-purple-400 border-purple-500/20',
  [BookingStatus.ACCEPTED]: 'bg-blue-500/10 text-blue-400 border-blue-500/20',
  [BookingStatus.ARRIVED]: 'bg-indigo-500/10 text-indigo-400 border-indigo-500/20',
  [BookingStatus.IN_PROGRESS]: 'bg-cyan-500/10 text-cyan-400 border-cyan-500/20',
  [BookingStatus.COMPLETED]: 'bg-green-500/10 text-green-400 border-green-500/20',
  [BookingStatus.CANCELLED]: 'bg-red-500/10 text-red-400 border-red-500/20',
}

const formatTime = (t) => {
  if (!t) return '—'
  return new Date(t).toLocaleString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' })
}

const formatDateISO = (d) => {
  if (!d) return ''
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

const AdminBookingsPage = () => {
  const [bookings, setBookings] = useState([])
  const [loading, setLoading] = useState(true)
  const [exporting, setExporting] = useState(false)

  // Search state with useDebounce
  const [searchQuery, setSearchQuery] = useState('')
  const debouncedSearch = useDebounce(searchQuery, 400)

  // Filters state
  const [statusFilter, setStatusFilter] = useState('ALL')
  const [fromDate, setFromDate] = useState('')
  const [toDate, setToDate] = useState('')
  const [datePreset, setDatePreset] = useState('all')
  const [minPrice, setMinPrice] = useState('')
  const [maxPrice, setMaxPrice] = useState('')
  const [showAdvancedFilters, setShowAdvancedFilters] = useState(false)
  const [sortBy, setSortBy] = useState('bookingTime:desc')

  // Pagination state
  const [pagination, setPagination] = useState({ page: 0, totalPages: 1, totalElements: 0 })

  // Modal states
  const [selectedBooking, setSelectedBooking] = useState(null)
  const [modalOpen, setModalOpen] = useState(false)

  const [assignModalOpen, setAssignModalOpen] = useState(false)
  const [assignBookingId, setAssignBookingId] = useState(null)
  const [availableDrivers, setAvailableDrivers] = useState([])
  const [driverSearch, setDriverSearch] = useState('')
  const [driversLoading, setDriversLoading] = useState(false)
  const [assigning, setAssigning] = useState(false)

  // Preset Date Handlers
  const handleDatePreset = (preset) => {
    setDatePreset(preset)
    const now = new Date()
    if (preset === 'today') {
      const todayStr = formatDateISO(now)
      setFromDate(todayStr)
      setToDate(todayStr)
    } else if (preset === '7days') {
      const past = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000)
      setFromDate(formatDateISO(past))
      setToDate(formatDateISO(now))
    } else if (preset === '30days') {
      const past = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000)
      setFromDate(formatDateISO(past))
      setToDate(formatDateISO(now))
    } else if (preset === 'month') {
      const startOfMonth = new Date(now.getFullYear(), now.getMonth(), 1)
      setFromDate(formatDateISO(startOfMonth))
      setToDate(formatDateISO(now))
    } else if (preset === 'all') {
      setFromDate('')
      setToDate('')
    }
  }

  // Fetch Bookings with Query params + OpenAPI spec compatibility
  const fetchBookings = useCallback(async (page = 0) => {
    setLoading(true)
    try {
      const queryPayload = {
        page,
        size: 20,
        status: statusFilter !== 'ALL' ? statusFilter : undefined,
        statuses: statusFilter !== 'ALL' ? statusFilter : undefined,
        search: debouncedSearch || undefined,
        fromDate: fromDate || undefined,
        toDate: toDate || undefined,
        bookingFrom: fromDate ? `${fromDate}T00:00:00.000Z` : undefined,
        bookingTo: toDate ? `${toDate}T23:59:59.999Z` : undefined,
        minPrice: minPrice ? Number(minPrice) : undefined,
        maxPrice: maxPrice ? Number(maxPrice) : undefined,
        sort: sortBy,
      }

      const res = await bookingApi.getAllForAdmin(queryPayload)
      const data = res?.result ?? res

      let list = data?.content || (Array.isArray(data) ? data : [])

      // Client-side fallback filter & sort (gracefully guards if backend hasn't finished OpenAPI filtering)
      if (minPrice) {
        list = list.filter(b => (b.totalPrice ?? 0) >= Number(minPrice))
      }
      if (maxPrice) {
        list = list.filter(b => (b.totalPrice ?? 0) <= Number(maxPrice))
      }
      if (statusFilter !== 'ALL') {
        list = list.filter(b => b.bookingStatus === statusFilter)
      }

      // Defensive sort
      if (sortBy === 'totalPrice:desc') {
        list = [...list].sort((a, b) => (b.totalPrice ?? 0) - (a.totalPrice ?? 0))
      } else if (sortBy === 'totalPrice:asc') {
        list = [...list].sort((a, b) => (a.totalPrice ?? 0) - (b.totalPrice ?? 0))
      } else if (sortBy === 'distance:desc') {
        list = [...list].sort((a, b) => (b.distance ?? 0) - (a.distance ?? 0))
      } else if (sortBy === 'bookingTime:asc') {
        list = [...list].sort((a, b) => new Date(a.bookingTime || 0) - new Date(b.bookingTime || 0))
      } else {
        list = [...list].sort((a, b) => new Date(b.bookingTime || 0) - new Date(a.bookingTime || 0))
      }

      setBookings(list)
      setPagination({
        page: data?.page?.number ?? data?.number ?? page,
        totalPages: data?.page?.totalPages ?? data?.totalPages ?? 1,
        totalElements: data?.page?.totalElements ?? data?.totalElements ?? list.length,
      })
    } catch (e) {
      console.error('Error fetching bookings:', e)
    } finally {
      setLoading(false)
    }
  }, [statusFilter, debouncedSearch, fromDate, toDate, minPrice, maxPrice, sortBy])

  useEffect(() => {
    fetchBookings(0)
  }, [fetchBookings])

  const openDetail = (booking) => {
    setSelectedBooking(booking)
    setModalOpen(true)
  }

  const handleClearFilters = () => {
    setSearchQuery('')
    setStatusFilter('ALL')
    setFromDate('')
    setToDate('')
    setDatePreset('all')
    setMinPrice('')
    setMaxPrice('')
    setSortBy('bookingTime:desc')
  }

  const handleForceCancel = async (bookingId) => {
    if (!confirm('Bạn chắc chắn muốn huỷ chuyến đi này?')) return
    try {
      await bookingApi.adminForceCancel(bookingId)
      await fetchBookings(pagination.page)
      setModalOpen(false)
    } catch (e) {
      alert(e.response?.data?.message || 'Không thể huỷ chuyến đi')
    }
  }

  const openAssignModal = async (bookingId) => {
    setAssignBookingId(bookingId)
    setDriverSearch('')
    setAssignModalOpen(true)
    setDriversLoading(true)
    try {
      const res = await driverApi.getAll(0, 100)
      const data = res?.content || (Array.isArray(res) ? res : [])
      setAvailableDrivers(data)
    } catch (e) {
      console.error(e)
    } finally {
      setDriversLoading(false)
    }
  }

  const handleAssignDriver = async (driverId) => {
    setAssigning(true)
    try {
      await bookingApi.adminAssignDriver(assignBookingId, driverId)
      setAssignModalOpen(false)
      await fetchBookings(pagination.page)
    } catch (e) {
      alert(e.response?.data?.message || 'Không thể gán tài xế')
    } finally {
      setAssigning(false)
    }
  }

  // Export Data to CSV
  const handleExport = async () => {
    setExporting(true)
    try {
      // 1. First try API direct export if supported
      try {
        const blob = await bookingApi.exportBookings({
          status: statusFilter !== 'ALL' ? statusFilter : undefined,
          search: debouncedSearch || undefined,
          fromDate: fromDate || undefined,
          toDate: toDate || undefined,
          sort: sortBy,
        })
        if (blob && blob.size > 0) {
          const url = URL.createObjectURL(blob)
          const a = document.createElement('a')
          a.href = url
          a.download = `danh-sach-chuyen-di-${Date.now()}.csv`
          document.body.appendChild(a)
          a.click()
          document.body.removeChild(a)
          URL.revokeObjectURL(url)
          return
        }
      } catch {
        // Fallback to client-side exporter below
      }

      // 2. Client-side fallback exporter using exportToCSV
      const columns = [
        { label: 'Mã chuyến', key: 'bookingId' },
        { label: 'Khách hàng', format: r => r.customerName || '—' },
        { label: 'SĐT Khách', key: 'customerPhone' },
        { label: 'Tài xế', format: r => r.driverName || 'Chưa nhận' },
        { label: 'SĐT Tài xế', key: 'driverPhone' },
        { label: 'Biển số', key: 'licensePlate' },
        { label: 'Điểm đón', key: 'pickupLocation' },
        { label: 'Điểm trả', key: 'dropoffLocation' },
        { label: 'Khoảng cách (km)', format: r => r.distance ? r.distance.toFixed(1) : '—' },
        { label: 'Giá tiền (VND)', format: r => r.totalPrice ? formatCurrency(r.totalPrice) : '0' },
        { label: 'Phương thức TT', key: 'paymentMethod' },
        { label: 'Trạng thái', format: r => BOOKING_STATUS_LABEL[r.bookingStatus] || r.bookingStatus },
        { label: 'Thời gian đặt', format: r => formatTime(r.bookingTime) },
        { label: 'Thời gian hẹn (nếu có)', format: r => r.scheduledAt ? formatTime(r.scheduledAt) : 'Đi ngay' },
      ]

      exportToCSV(`danh-sach-chuyen-xe-${formatDateISO(new Date())}.csv`, columns, bookings)
    } catch (err) {
      alert(err.message || 'Lỗi khi xuất dữ liệu')
    } finally {
      setExporting(false)
    }
  }

  const filteredDriversForAssign = useMemo(() => {
    return availableDrivers.filter(d =>
      d.driverName?.toLowerCase().includes(driverSearch.toLowerCase()) ||
      d.phone?.includes(driverSearch)
    )
  }, [availableDrivers, driverSearch])

  const canCancel = (status) => [BookingStatus.PENDING, BookingStatus.QUEUED, BookingStatus.ACCEPTED, BookingStatus.ARRIVED].includes(status)
  const canAssign = (booking) => (booking.bookingStatus === BookingStatus.PENDING || booking.bookingStatus === 'QUEUED') && !booking.driverId
  const hasActiveFilters = Boolean(searchQuery || statusFilter !== 'ALL' || fromDate || toDate || minPrice || maxPrice || sortBy !== 'bookingTime:desc')

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="section-title">Quản lý Chuyến đi</h1>
          <p className="text-content-muted text-sm mt-1">
            Tổng cộng <span className="text-brand-400 font-semibold">{pagination.totalElements}</span> chuyến đi
          </p>
        </div>
        <div className="flex items-center gap-2">
          <button
            onClick={handleExport}
            disabled={exporting || bookings.length === 0}
            className="btn-ghost flex items-center gap-2 text-sm border border-surface-border px-3.5 py-2 rounded-xl hover:bg-surface-card hover:text-brand-400 disabled:opacity-50 transition-colors"
            title="Xuất dữ liệu ra file CSV Excel"
          >
            <RiDownloadLine size={16} />
            <span>{exporting ? 'Đang xuất...' : 'Xuất Excel / CSV'}</span>
          </button>
          <button
            onClick={() => fetchBookings(pagination.page)}
            className="p-2 rounded-xl border border-surface-border text-content-muted hover:text-content-main hover:bg-surface-card transition-colors"
            title="Làm mới"
          >
            <RiRefreshLine size={18} className={loading ? 'animate-spin' : ''} />
          </button>
        </div>
      </div>

      {/* Status Tabs */}
      <div className="flex gap-2 overflow-x-auto pb-1 scrollbar-none">
        {STATUS_TABS.map(tab => (
          <button
            key={tab.key}
            onClick={() => setStatusFilter(tab.key)}
            className={cn(
              'px-4 py-2 rounded-xl text-sm font-medium whitespace-nowrap transition-all border shrink-0',
              statusFilter === tab.key
                ? 'bg-brand-500/15 text-brand-400 border-brand-500/30 shadow-sm'
                : 'bg-surface-card text-content-muted border-surface-border hover:border-brand-500/20'
            )}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {/* Search & Filter Card */}
      <div className="card p-4 space-y-4">
        {/* Top bar: Search input & Sort dropdown */}
        <div className="flex flex-col md:flex-row items-stretch md:items-center gap-3">
          {/* Real-time Debounced Search Input */}
          <div className="flex-1 flex items-center gap-2.5 px-3.5 py-2 rounded-xl bg-surface-dark border border-surface-border focus-within:border-brand-500/50 transition-all">
            <RiSearchLine className="text-content-muted shrink-0" size={18} />
            <input
              type="text"
              placeholder="Tìm kiếm theo mã chuyến, tên KH, tài xế, SĐT, điểm đón/trả..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="flex-1 bg-transparent outline-none text-sm text-content-main placeholder:text-content-muted"
            />
            {searchQuery && (
              <button
                onClick={() => setSearchQuery('')}
                className="p-1 text-content-muted hover:text-content-main rounded-md transition-colors"
                title="Xóa tìm kiếm"
              >
                <RiCloseLine size={16} />
              </button>
            )}
          </div>

          {/* Sort Selector */}
          <div className="flex items-center gap-2 shrink-0">
            <div className="flex items-center gap-2 px-3 py-2 rounded-xl bg-surface-dark border border-surface-border text-sm">
              <RiArrowUpDownLine className="text-content-muted" size={16} />
              <select
                value={sortBy}
                onChange={(e) => setSortBy(e.target.value)}
                className="bg-transparent outline-none text-content-main cursor-pointer"
              >
                <option value="bookingTime:desc" className="bg-surface-card">Mới nhất (Thời gian ↓)</option>
                <option value="bookingTime:asc" className="bg-surface-card">Cũ nhất (Thời gian ↑)</option>
                <option value="totalPrice:desc" className="bg-surface-card">Giá cao nhất (Giá ↓)</option>
                <option value="totalPrice:asc" className="bg-surface-card">Giá thấp nhất (Giá ↑)</option>
                <option value="distance:desc" className="bg-surface-card">Quãng đường xa nhất</option>
              </select>
            </div>

            {/* Toggle Advanced Filters Button */}
            <button
              onClick={() => setShowAdvancedFilters(!showAdvancedFilters)}
              className={cn(
                'flex items-center gap-1.5 px-3 py-2 rounded-xl text-sm font-medium border transition-colors',
                showAdvancedFilters || hasActiveFilters
                  ? 'bg-brand-500/10 text-brand-400 border-brand-500/30'
                  : 'bg-surface-dark text-content-muted border-surface-border hover:text-content-main'
              )}
            >
              <RiFilterLine size={16} />
              <span>Lọc nâng cao</span>
              {hasActiveFilters && (
                <span className="w-2 h-2 rounded-full bg-brand-400 animate-pulse" />
              )}
            </button>
          </div>
        </div>

        {/* Quick Date Presets */}
        <div className="flex items-center gap-1.5 flex-wrap pt-1 border-t border-surface-border/50 text-xs">
          <span className="text-content-muted mr-1">Thời gian:</span>
          {[
            { id: 'all', label: 'Tất cả' },
            { id: 'today', label: 'Hôm nay' },
            { id: '7days', label: '7 ngày qua' },
            { id: '30days', label: '30 ngày qua' },
            { id: 'month', label: 'Tháng này' },
          ].map(p => (
            <button
              key={p.id}
              onClick={() => handleDatePreset(p.id)}
              className={cn(
                'px-2.5 py-1 rounded-lg font-medium transition-colors border',
                datePreset === p.id && !fromDate && !toDate && p.id === 'all'
                  ? 'bg-brand-500/20 text-brand-400 border-brand-500/30'
                  : datePreset === p.id && p.id !== 'all'
                    ? 'bg-brand-500/20 text-brand-400 border-brand-500/30'
                    : 'bg-surface-dark text-content-muted border-surface-border hover:text-content-main'
              )}
            >
              {p.label}
            </button>
          ))}
        </div>

        {/* Advanced Filters Expandable Panel */}
        {showAdvancedFilters && (
          <div className="p-3.5 bg-surface-dark/40 rounded-xl border border-surface-border/70 space-y-3 animate-in fade-in duration-200">
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3">
              {/* From Date */}
              <div>
                <label className="text-xs text-content-muted mb-1 block">Từ ngày:</label>
                <div className="flex items-center gap-2 px-3 py-1.5 rounded-lg bg-surface-dark border border-surface-border">
                  <RiCalendarLine className="text-content-muted" size={15} />
                  <input
                    type="date"
                    value={fromDate}
                    onChange={(e) => { setFromDate(e.target.value); setDatePreset('custom') }}
                    className="bg-transparent outline-none text-xs text-content-main w-full"
                  />
                </div>
              </div>

              {/* To Date */}
              <div>
                <label className="text-xs text-content-muted mb-1 block">Đến ngày:</label>
                <div className="flex items-center gap-2 px-3 py-1.5 rounded-lg bg-surface-dark border border-surface-border">
                  <RiCalendarLine className="text-content-muted" size={15} />
                  <input
                    type="date"
                    value={toDate}
                    onChange={(e) => { setToDate(e.target.value); setDatePreset('custom') }}
                    className="bg-transparent outline-none text-xs text-content-main w-full"
                  />
                </div>
              </div>

              {/* Min Price */}
              <div>
                <label className="text-xs text-content-muted mb-1 block">Giá tối thiểu (VND):</label>
                <input
                  type="number"
                  placeholder="Vd: 30000"
                  value={minPrice}
                  onChange={(e) => setMinPrice(e.target.value)}
                  className="w-full px-3 py-1.5 rounded-lg bg-surface-dark border border-surface-border outline-none text-xs text-content-main placeholder:text-content-muted"
                />
              </div>

              {/* Max Price */}
              <div>
                <label className="text-xs text-content-muted mb-1 block">Giá tối đa (VND):</label>
                <input
                  type="number"
                  placeholder="Vd: 500000"
                  value={maxPrice}
                  onChange={(e) => setMaxPrice(e.target.value)}
                  className="w-full px-3 py-1.5 rounded-lg bg-surface-dark border border-surface-border outline-none text-xs text-content-main placeholder:text-content-muted"
                />
              </div>
            </div>

            {/* Clear Filters Action */}
            {hasActiveFilters && (
              <div className="flex justify-end pt-1">
                <button
                  onClick={handleClearFilters}
                  className="text-xs text-red-400 hover:text-red-300 hover:underline transition-colors flex items-center gap-1"
                >
                  <RiCloseLine size={14} /> Xóa toàn bộ bộ lọc
                </button>
              </div>
            )}
          </div>
        )}
      </div>

      {/* Table */}
      {loading ? (
        <div className="flex justify-center py-16"><Spinner size="xl" /></div>
      ) : bookings.length === 0 ? (
        <div className="card p-12 text-center text-content-muted space-y-3">
          <p>Không tìm thấy chuyến đi nào khớp với điều kiện tìm kiếm.</p>
          {hasActiveFilters && (
            <button
              onClick={handleClearFilters}
              className="px-4 py-2 rounded-xl bg-brand-500/10 text-brand-400 border border-brand-500/20 text-xs hover:bg-brand-500/20 transition-colors"
            >
              Đặt lại bộ lọc
            </button>
          )}
        </div>
      ) : (
        <div className="card overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead className="bg-surface-dark">
                <tr className="border-b border-surface-border text-content-muted">
                  <th className="text-left px-4 py-3 font-medium">Mã chuyến</th>
                  <th className="text-left px-4 py-3 font-medium">Khách hàng</th>
                  <th className="text-left px-4 py-3 font-medium">Tài xế</th>
                  <th className="text-left px-4 py-3 font-medium">Tuyến đường</th>
                  <th className="text-left px-4 py-3 font-medium">Giá</th>
                  <th className="text-left px-4 py-3 font-medium">Trạng thái</th>
                  <th className="text-left px-4 py-3 font-medium">Thời gian</th>
                  <th className="text-center px-4 py-3 font-medium">Hành động</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-surface-border">
                {bookings.map(b => (
                  <tr key={b.bookingId} className="hover:bg-surface-dark/30 transition-colors">
                    <td className="px-4 py-3 font-mono text-xs text-content-muted" title={b.bookingId}>
                      {b.bookingId?.slice(0, 8)}...
                    </td>
                    <td className="px-4 py-3">
                      <p className="font-medium text-content-main">{b.customerName || '—'}</p>
                      <p className="text-xs text-content-muted">{b.customerPhone || ''}</p>
                    </td>
                    <td className="px-4 py-3">
                      {b.driverName ? (
                        <>
                          <p className="font-medium text-content-main">{b.driverName}</p>
                          <p className="text-xs text-content-muted">{b.driverPhone || ''}</p>
                        </>
                      ) : (
                        <span className="text-content-muted italic text-xs">Chưa có tài xế</span>
                      )}
                    </td>
                    <td className="px-4 py-3 max-w-[200px]">
                      <p className="text-xs text-content-main truncate" title={b.pickupLocation}>
                        <RiMapPinLine className="inline mr-1 text-green-400" size={12} />
                        {b.pickupLocation || '—'}
                      </p>
                      <p className="text-xs text-content-muted truncate" title={b.dropoffLocation}>
                        <RiMapPinLine className="inline mr-1 text-red-400" size={12} />
                        {b.dropoffLocation || '—'}
                      </p>
                    </td>
                    <td className="px-4 py-3 font-mono font-medium text-content-main whitespace-nowrap">
                      {b.totalPrice ? formatCurrency(b.totalPrice) : '—'}
                    </td>
                    <td className="px-4 py-3">
                      <span className={cn('badge border text-xs px-2.5 py-1', STATUS_BADGE[b.bookingStatus] || 'bg-gray-500/10 text-gray-400')}>
                        {BOOKING_STATUS_LABEL[b.bookingStatus] || b.bookingStatus}
                      </span>
                    </td>
                    <td className="px-4 py-3 text-xs text-content-muted whitespace-nowrap">
                      <div>{formatTime(b.bookingTime)}</div>
                      {b.scheduledAt && (
                        <div className="text-[10px] text-purple-400 font-medium mt-0.5">
                          Hẹn: {formatTime(b.scheduledAt)}
                        </div>
                      )}
                    </td>
                    <td className="px-4 py-3">
                      <div className="flex items-center justify-center gap-1">
                        <button onClick={() => openDetail(b)} className="p-1.5 rounded-lg hover:bg-brand-500/10 text-brand-400 transition-colors" title="Xem chi tiết">
                          <RiEyeLine size={16} />
                        </button>
                        {canAssign(b) && (
                          <button onClick={() => openAssignModal(b.bookingId)} className="p-1.5 rounded-lg hover:bg-blue-500/10 text-blue-400 transition-colors" title="Gán tài xế">
                            <RiUserAddLine size={16} />
                          </button>
                        )}
                        {canCancel(b.bookingStatus) && (
                          <button onClick={() => handleForceCancel(b.bookingId)} className="p-1.5 rounded-lg hover:bg-red-500/10 text-red-400 transition-colors" title="Huỷ chuyến">
                            <RiDeleteBin6Line size={16} />
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {/* Pagination */}
          {pagination.totalPages > 1 && (
            <div className="flex items-center justify-between px-4 py-3 border-t border-surface-border">
              <p className="text-xs text-content-muted">
                Trang {pagination.page + 1} / {pagination.totalPages}
              </p>
              <div className="flex gap-2">
                <button
                  onClick={() => fetchBookings(pagination.page - 1)}
                  disabled={pagination.page === 0}
                  className="px-3 py-1.5 rounded-lg border border-surface-border text-sm disabled:opacity-40 hover:bg-surface-dark transition-colors"
                >
                  Trước
                </button>
                <button
                  onClick={() => fetchBookings(pagination.page + 1)}
                  disabled={pagination.page >= pagination.totalPages - 1}
                  className="px-3 py-1.5 rounded-lg border border-surface-border text-sm disabled:opacity-40 hover:bg-surface-dark transition-colors"
                >
                  Sau
                </button>
              </div>
            </div>
          )}
        </div>
      )}

      {/* Detail Modal */}
      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title="Chi tiết Chuyến đi">
        {selectedBooking && (
          <div className="space-y-6">
            {/* Status banner */}
            <div className={cn('flex items-center justify-between p-4 rounded-xl border', STATUS_BADGE[selectedBooking.bookingStatus] || 'bg-gray-500/10')}>
              <div>
                <p className="text-xs opacity-70 mb-0.5">Trạng thái</p>
                <p className="font-bold text-lg">{BOOKING_STATUS_LABEL[selectedBooking.bookingStatus] || selectedBooking.bookingStatus}</p>
              </div>
              <p className="font-mono text-xs opacity-60">{selectedBooking.bookingId}</p>
            </div>

            {/* Info grid */}
            <div className="grid grid-cols-2 gap-4">
              <div className="p-4 bg-surface-dark/50 border border-surface-border rounded-xl">
                <p className="text-content-muted text-xs mb-1 flex items-center gap-1"><RiUserLine size={12} /> Khách hàng</p>
                <p className="font-medium">{selectedBooking.customerName || '—'}</p>
                <p className="text-xs text-content-muted">{selectedBooking.customerPhone || ''}</p>
              </div>
              <div className="p-4 bg-surface-dark/50 border border-surface-border rounded-xl">
                <p className="text-content-muted text-xs mb-1 flex items-center gap-1"><RiCarLine size={12} /> Tài xế</p>
                <p className="font-medium">{selectedBooking.driverName || <span className="italic text-content-muted">Chưa có</span>}</p>
                <p className="text-xs text-content-muted">{selectedBooking.driverPhone || ''}</p>
                {selectedBooking.licensePlate && <p className="text-xs font-mono mt-0.5">{selectedBooking.licensePlate}</p>}
              </div>
            </div>

            {/* Route */}
            <div className="space-y-2">
              <div className="flex items-start gap-3 p-3 bg-surface-dark/30 rounded-xl">
                <RiMapPinLine className="text-green-400 shrink-0 mt-0.5" size={16} />
                <div>
                  <p className="text-xs text-content-muted">Điểm đón</p>
                  <p className="text-sm font-medium">{selectedBooking.pickupLocation || '—'}</p>
                </div>
              </div>
              <div className="flex items-start gap-3 p-3 bg-surface-dark/30 rounded-xl">
                <RiMapPinLine className="text-red-400 shrink-0 mt-0.5" size={16} />
                <div>
                  <p className="text-xs text-content-muted">Điểm trả</p>
                  <p className="text-sm font-medium">{selectedBooking.dropoffLocation || '—'}</p>
                </div>
              </div>
            </div>

            {/* Price + details */}
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
              <div className="p-3 bg-surface-dark/50 border border-surface-border rounded-xl text-center">
                <p className="text-xs text-content-muted mb-1">Giá gốc</p>
                <p className="font-mono font-bold">{selectedBooking.originalPrice ? formatCurrency(selectedBooking.originalPrice) : '—'}</p>
              </div>
              <div className="p-3 bg-surface-dark/50 border border-surface-border rounded-xl text-center">
                <p className="text-xs text-content-muted mb-1">Thành tiền</p>
                <p className="font-mono font-bold text-brand-400">{selectedBooking.totalPrice ? formatCurrency(selectedBooking.totalPrice) : '—'}</p>
              </div>
              <div className="p-3 bg-surface-dark/50 border border-surface-border rounded-xl text-center">
                <p className="text-xs text-content-muted mb-1">Khoảng cách</p>
                <p className="font-mono font-bold">{selectedBooking.distance ? `${selectedBooking.distance.toFixed(1)} km` : '—'}</p>
              </div>
              <div className="p-3 bg-surface-dark/50 border border-surface-border rounded-xl text-center">
                <p className="text-xs text-content-muted mb-1">Thanh toán</p>
                <p className="font-mono font-bold">{selectedBooking.paymentMethod || '—'}</p>
              </div>
            </div>

            {/* Timestamps */}
            <div className="grid grid-cols-3 gap-3 text-center">
              <div className="p-3 bg-surface-dark/30 rounded-xl">
                <p className="text-xs text-content-muted mb-1">Đặt lúc</p>
                <p className="text-xs font-medium">{formatTime(selectedBooking.bookingTime)}</p>
              </div>
              <div className="p-3 bg-surface-dark/30 rounded-xl">
                <p className="text-xs text-content-muted mb-1">Đón lúc</p>
                <p className="text-xs font-medium">{formatTime(selectedBooking.pickupTime)}</p>
              </div>
              <div className="p-3 bg-surface-dark/30 rounded-xl">
                <p className="text-xs text-content-muted mb-1">Trả lúc</p>
                <p className="text-xs font-medium">{formatTime(selectedBooking.arrivalTime)}</p>
              </div>
            </div>

            {/* Extra info */}
            <div className="flex flex-wrap gap-3">
              {selectedBooking.vehicleTypeName && (
                <span className="badge bg-surface-dark border border-surface-border text-xs px-3 py-1">
                  🚗 {selectedBooking.vehicleTypeName}
                </span>
              )}
              {selectedBooking.promotionCode && (
                <span className="badge bg-brand-500/10 border border-brand-500/20 text-brand-400 text-xs px-3 py-1">
                  🎫 {selectedBooking.promotionCode}
                </span>
              )}
              <span className={cn('badge border text-xs px-3 py-1', selectedBooking.paymentStatus ? 'bg-green-500/10 text-green-400 border-green-500/20' : 'bg-yellow-500/10 text-yellow-400 border-yellow-500/20')}>
                {selectedBooking.paymentStatus ? '✅ Đã thanh toán' : '⏳ Chưa thanh toán'}
              </span>
              {selectedBooking.scheduledAt && (
                <span className="badge bg-purple-500/10 border border-purple-500/20 text-purple-400 text-xs px-3 py-1">
                  📅 Đặt lịch: {formatTime(selectedBooking.scheduledAt)}
                </span>
              )}
            </div>

            {/* Actions */}
            <div className="flex gap-3 pt-2 border-t border-surface-border">
              {canAssign(selectedBooking) && (
                <button
                  onClick={() => { setModalOpen(false); openAssignModal(selectedBooking.bookingId) }}
                  className="flex-1 py-2.5 bg-blue-500/10 hover:bg-blue-500/20 text-blue-400 rounded-xl text-sm font-medium transition-colors flex items-center justify-center gap-2"
                >
                  <RiUserAddLine size={16} /> Gán tài xế
                </button>
              )}
              {canCancel(selectedBooking.bookingStatus) && (
                <button
                  onClick={() => handleForceCancel(selectedBooking.bookingId)}
                  className="py-2.5 px-4 bg-red-500/10 hover:bg-red-500/20 text-red-400 rounded-xl text-sm font-medium transition-colors flex items-center justify-center gap-2"
                >
                  <RiDeleteBin6Line size={16} /> Huỷ chuyến
                </button>
              )}
            </div>
          </div>
        )}
      </Modal>

      {/* Assign Driver Modal */}
      <Modal open={assignModalOpen} onClose={() => setAssignModalOpen(false)} title="Gán tài xế cho chuyến đi">
        <div className="space-y-4">
          <div className="flex items-center gap-2 px-3 py-2 rounded-xl bg-surface-dark border border-surface-border">
            <RiSearchLine className="text-content-muted" size={16} />
            <input
              type="text"
              placeholder="Tìm tài xế theo tên, SĐT..."
              value={driverSearch}
              onChange={(e) => setDriverSearch(e.target.value)}
              className="bg-transparent outline-none text-sm text-content-main placeholder:text-content-muted w-full"
            />
          </div>

          {driversLoading ? (
            <div className="flex justify-center py-8"><Spinner size="md" /></div>
          ) : filteredDriversForAssign.length === 0 ? (
            <p className="text-center text-content-muted py-6 text-sm">Không tìm thấy tài xế khả dụng</p>
          ) : (
            <div className="divide-y divide-surface-border max-h-72 overflow-y-auto">
              {filteredDriversForAssign.map(driver => (
                <div key={driver.driverId} className="flex items-center justify-between py-3 px-2 hover:bg-surface-dark/50 rounded-lg transition-colors">
                  <div className="flex items-center gap-3">
                    <div className="w-8 h-8 rounded-full bg-surface-dark flex items-center justify-center text-sm font-bold text-content-muted">
                      {driver.driverName?.charAt(0) || 'D'}
                    </div>
                    <div>
                      <p className="text-sm font-medium text-content-main">{driver.driverName}</p>
                      <p className="text-xs text-content-muted">{driver.phone} • ⭐ {driver.score || 5.0} • {driver.vehicleTypeName || 'Xe'}</p>
                    </div>
                  </div>
                  <button
                    disabled={assigning}
                    onClick={() => handleAssignDriver(driver.driverId)}
                    className="px-3 py-1.5 bg-brand-500 hover:bg-brand-600 disabled:opacity-50 text-white rounded-lg text-xs font-medium transition-colors"
                  >
                    {assigning ? 'Đang gán...' : 'Chọn'}
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>
      </Modal>
    </div>
  )
}

export default AdminBookingsPage
