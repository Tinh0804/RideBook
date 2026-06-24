import { useState, useEffect, useCallback } from 'react'
import { bookingApi } from '@/features/booking/api/bookingApi'
import { driverApi } from '@/features/driver/api/driverApi'
import Spinner from '@/components/Elements/Spinner'
import {
  RiSearchLine, RiEyeLine, RiCloseLine, RiMapPinLine,
  RiUserLine, RiCarLine, RiTimeLine, RiDeleteBin6Line,
  RiUserAddLine, RiCalendarLine, RiFilterLine,
} from 'react-icons/ri'
import { cn } from '@/utils/cn'
import { BookingStatus } from '@/constants/enums'
import { BOOKING_STATUS_LABEL } from '@/config'
import { formatCurrency } from '@/utils/currency'

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
  { key: BookingStatus.ACCEPTED, label: 'Đã nhận' },
  { key: BookingStatus.ARRIVED, label: 'Đến đón' },
  { key: BookingStatus.IN_PROGRESS, label: 'Đang đi' },
  { key: BookingStatus.COMPLETED, label: 'Hoàn thành' },
  { key: BookingStatus.CANCELLED, label: 'Đã huỷ' },
]

const STATUS_BADGE = {
  [BookingStatus.PENDING]: 'bg-yellow-500/10 text-yellow-400 border-yellow-500/20',
  [BookingStatus.ACCEPTED]: 'bg-blue-500/10 text-blue-400 border-blue-500/20',
  [BookingStatus.ARRIVED]: 'bg-purple-500/10 text-purple-400 border-purple-500/20',
  [BookingStatus.IN_PROGRESS]: 'bg-cyan-500/10 text-cyan-400 border-cyan-500/20',
  [BookingStatus.COMPLETED]: 'bg-green-500/10 text-green-400 border-green-500/20',
  [BookingStatus.CANCELLED]: 'bg-red-500/10 text-red-400 border-red-500/20',
}

const formatTime = (t) => {
  if (!t) return '—'
  return new Date(t).toLocaleString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' })
}

const AdminBookingsPage = () => {
  const [bookings, setBookings] = useState([])
  const [loading, setLoading] = useState(true)
  const [search, setSearch] = useState('')
  const [inputValue, setInputValue] = useState('')
  const [statusFilter, setStatusFilter] = useState('ALL')
  const [fromDate, setFromDate] = useState('')
  const [toDate, setToDate] = useState('')
  const [pagination, setPagination] = useState({ page: 0, totalPages: 1, totalElements: 0 })

  const [selectedBooking, setSelectedBooking] = useState(null)
  const [modalOpen, setModalOpen] = useState(false)

  const [assignModalOpen, setAssignModalOpen] = useState(false)
  const [assignBookingId, setAssignBookingId] = useState(null)
  const [availableDrivers, setAvailableDrivers] = useState([])
  const [driverSearch, setDriverSearch] = useState('')
  const [driversLoading, setDriversLoading] = useState(false)
  const [assigning, setAssigning] = useState(false)

  const fetchBookings = useCallback(async (page = 0) => {
    setLoading(true)
    try {
      const res = await bookingApi.getAllForAdmin(page, 20, statusFilter, search, fromDate, toDate)
      const data = res.result
      setBookings(data?.content || [])
      setPagination({
        page: data?.page?.number ?? data?.number ?? 0,
        totalPages: data?.page?.totalPages ?? data?.totalPages ?? 1,
        totalElements: data?.page?.totalElements ?? data?.totalElements ?? 0,
      })
    } catch (e) {
      console.error(e)
    } finally {
      setLoading(false)
    }
  }, [statusFilter, search, fromDate, toDate])

  useEffect(() => {
    const timer = setTimeout(() => fetchBookings(0), 300)
    return () => clearTimeout(timer)
  }, [fetchBookings])

  const openDetail = (booking) => {
    setSelectedBooking(booking)
    setModalOpen(true)
  }

  const handleSearch = (e) => {
    e.preventDefault()
    setSearch(inputValue)
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
      const res = await driverApi.getAll(0, 50)
      // driverApi.getAll already unwraps r.data?.result, so res is the Page object directly
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

  const filteredDriversForAssign = availableDrivers.filter(d =>
    d.driverName?.toLowerCase().includes(driverSearch.toLowerCase()) ||
    d.phone?.includes(driverSearch)
  )

  const canCancel = (status) => [BookingStatus.PENDING, BookingStatus.ACCEPTED, BookingStatus.ARRIVED].includes(status)
  const canAssign = (booking) => booking.bookingStatus === BookingStatus.PENDING && !booking.driverId

  return (
    <div className="space-y-6">
      <div>
        <h1 className="section-title">Quản lý Chuyến đi</h1>
        <p className="text-content-muted text-sm mt-1">
          Tổng cộng <span className="text-brand-400 font-semibold">{pagination.totalElements}</span> chuyến đi
        </p>
      </div>

      {/* Status Tabs */}
      <div className="flex gap-2 overflow-x-auto pb-1">
        {STATUS_TABS.map(tab => (
          <button
            key={tab.key}
            onClick={() => setStatusFilter(tab.key)}
            className={cn(
              'px-4 py-2 rounded-xl text-sm font-medium whitespace-nowrap transition-all border',
              statusFilter === tab.key
                ? 'bg-brand-500/15 text-brand-400 border-brand-500/30'
                : 'bg-surface-card text-content-muted border-surface-border hover:border-brand-500/20'
            )}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {/* Search + Date filters */}
      <div className="card p-4 space-y-3">
        <form onSubmit={handleSearch} className="flex items-center gap-3">
          <RiSearchLine className="text-content-muted shrink-0" size={20} />
          <input
            type="text"
            placeholder="Tìm theo tên KH, tài xế, SĐT, mã chuyến..."
            value={inputValue}
            onChange={(e) => setInputValue(e.target.value)}
            className="flex-1 bg-transparent outline-none text-sm text-content-main placeholder:text-content-muted"
          />
          <button type="submit" className="px-4 py-2 bg-brand-500 hover:bg-brand-600 text-white rounded-lg text-sm font-medium transition-colors">
            Tìm kiếm
          </button>
        </form>

        <div className="flex items-center gap-3 flex-wrap">
          <div className="flex items-center gap-2">
            <RiCalendarLine className="text-content-muted" size={16} />
            <span className="text-xs text-content-muted">Từ:</span>
            <input
              type="date"
              value={fromDate}
              onChange={(e) => setFromDate(e.target.value)}
              className="input-field text-sm py-1.5 px-3 bg-surface-dark"
            />
          </div>
          <div className="flex items-center gap-2">
            <span className="text-xs text-content-muted">Đến:</span>
            <input
              type="date"
              value={toDate}
              onChange={(e) => setToDate(e.target.value)}
              className="input-field text-sm py-1.5 px-3 bg-surface-dark"
            />
          </div>
          {(fromDate || toDate || search) && (
            <button
              onClick={() => { setFromDate(''); setToDate(''); setSearch(''); setInputValue('') }}
              className="px-3 py-1.5 text-xs text-red-400 hover:bg-red-500/10 rounded-lg transition-colors"
            >
              Xoá bộ lọc
            </button>
          )}
        </div>
      </div>

      {/* Table */}
      {loading ? (
        <div className="flex justify-center py-16"><Spinner size="xl" /></div>
      ) : bookings.length === 0 ? (
        <div className="card p-12 text-center text-content-muted">Không tìm thấy chuyến đi nào</div>
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
                      {formatTime(b.bookingTime)}
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
                  className="flex-1 py-2.5 bg-red-500/10 hover:bg-red-500/20 text-red-400 rounded-xl text-sm font-medium transition-colors flex items-center justify-center gap-2"
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
          <div className="flex items-center gap-3 p-3 border border-surface-border rounded-xl">
            <RiSearchLine className="text-content-muted" size={18} />
            <input
              type="text"
              placeholder="Tìm tài xế theo tên, SĐT..."
              value={driverSearch}
              onChange={(e) => setDriverSearch(e.target.value)}
              className="flex-1 bg-transparent outline-none text-sm text-content-main placeholder:text-content-muted"
            />
          </div>

          {driversLoading ? (
            <div className="flex justify-center py-8"><Spinner size="lg" /></div>
          ) : filteredDriversForAssign.length === 0 ? (
            <div className="text-center text-content-muted py-8 text-sm">Không tìm thấy tài xế</div>
          ) : (
            <div className="space-y-2 max-h-[400px] overflow-y-auto">
              {filteredDriversForAssign.map(d => (
                <div key={d.driverId} className="flex items-center justify-between p-3 rounded-xl border border-surface-border hover:border-brand-500/30 transition-colors">
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 rounded-full bg-surface-dark flex items-center justify-center">
                      {d.avatar ? <img src={d.avatar} alt="" className="w-10 h-10 rounded-full object-cover" /> : <RiUserLine className="text-content-muted" size={18} />}
                    </div>
                    <div>
                      <p className="font-medium text-sm text-content-main">{d.driverName}</p>
                      <p className="text-xs text-content-muted">{d.phone} · {d.licensePlate || '—'}</p>
                    </div>
                  </div>
                  <button
                    onClick={() => handleAssignDriver(d.driverId)}
                    disabled={assigning || !d.activityStatus}
                    className={cn(
                      'px-4 py-2 rounded-lg text-sm font-medium transition-colors',
                      d.activityStatus
                        ? 'bg-brand-500 hover:bg-brand-600 text-white'
                        : 'bg-surface-dark text-content-muted cursor-not-allowed'
                    )}
                  >
                    {assigning ? 'Đang gán...' : d.activityStatus ? 'Chọn' : 'Offline'}
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
