import { useState, useEffect, useMemo, useCallback } from 'react'
import { driverApi } from '@/features/driver/api/driverApi'
import { adminApi } from '@/features/admin/api/adminApi'
import Spinner from '@/components/Elements/Spinner'
import { 
  RiLockUnlockLine, RiLock2Line, RiMapPinLine, RiSearchLine, 
  RiEyeLine, RiCloseLine, RiCarLine, RiWallet3Line, RiUserLine,
  RiEditLine, RiKey2Line, RiDownloadLine, RiFilterLine,
  RiArrowUpDownLine, RiRefreshLine
} from 'react-icons/ri'
import { cn } from '@/utils/cn'
import { WalletStatus, TransactionType } from '@/constants/enums'
import AdminEditDriverModal from '@/features/admin/components/AdminEditDriverModal'
import AdminChangePasswordModal from '@/features/admin/components/AdminChangePasswordModal'
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

const formatMoney = (amount) => {
  if (!amount && amount !== 0) return '0 đ'
  return amount.toLocaleString('vi-VN') + ' đ'
}

const AdminDriversPage = () => {
  const [drivers, setDrivers] = useState([])
  const [vehicleTypes, setVehicleTypes] = useState([])
  const [loading, setLoading] = useState(true)
  const [exporting, setExporting] = useState(false)

  // Search state with useDebounce
  const [searchQuery, setSearchQuery] = useState('')
  const debouncedSearch = useDebounce(searchQuery, 400)

  // Filters
  const [accountStatusFilter, setAccountStatusFilter] = useState('ALL') // ALL, ACTIVE, LOCKED
  const [activityStatusFilter, setActivityStatusFilter] = useState('ALL') // ALL, ONLINE, OFFLINE
  const [vehicleTypeFilter, setVehicleTypeFilter] = useState('ALL')
  const [minRatingFilter, setMinRatingFilter] = useState('ALL')
  const [sortBy, setSortBy] = useState('driverName:asc')
  const [showAdvancedFilters, setShowAdvancedFilters] = useState(false)

  // Detail Modal State
  const [modalOpen, setModalOpen] = useState(false)
  const [selectedDriver, setSelectedDriver] = useState(null)
  const [activeTab, setActiveTab] = useState('info') // info, vehicle, wallet

  const [editModalOpen, setEditModalOpen] = useState(false)
  const [passwordModalOpen, setPasswordModalOpen] = useState(false)
  const [driverToEdit, setDriverToEdit] = useState(null)
  const [driverToChangePassword, setDriverToChangePassword] = useState(null)

  // Wallet State
  const [wallet, setWallet] = useState(null)
  const [transactions, setTransactions] = useState([])
  const [adjustForm, setAdjustForm] = useState({ amount: '', reason: '', type: 'ADD' })
  const [adjusting, setAdjusting] = useState(false)

  // Vehicle Form State
  const [vehicleForm, setVehicleForm] = useState({ licensePlate: '', vehicleTypeId: '' })
  const [updatingVehicle, setUpdatingVehicle] = useState(false)

  const fetchDrivers = useCallback(async () => {
    setLoading(true)
    try {
      const response = await driverApi.getAll({
        page: 0,
        size: 1000,
        search: debouncedSearch || undefined,
        accountStatus: accountStatusFilter === 'ACTIVE' ? true : accountStatusFilter === 'LOCKED' ? false : undefined,
        activityStatus: activityStatusFilter === 'ONLINE' ? true : activityStatusFilter === 'OFFLINE' ? false : undefined,
        vehicleTypeIds: vehicleTypeFilter !== 'ALL' ? vehicleTypeFilter : undefined,
        minRating: minRatingFilter !== 'ALL' ? Number(minRatingFilter) : undefined,
        sort: sortBy
      })
      const list = response?.content || (Array.isArray(response) ? response : [])
      setDrivers(list)
    } catch (error) {
      console.error('Error fetching drivers:', error)
    } finally {
      setLoading(false)
    }
  }, [debouncedSearch, accountStatusFilter, activityStatusFilter, vehicleTypeFilter, minRatingFilter, sortBy])

  const fetchVehicleTypes = async () => {
    try {
      const res = await adminApi.getAllVehicleTypes()
      setVehicleTypes(res.result || [])
    } catch (e) {
      console.error(e)
    }
  }

  useEffect(() => {
    fetchVehicleTypes()
  }, [])

  useEffect(() => {
    fetchDrivers()
  }, [fetchDrivers])

  const handleToggleAccount = async (driverId) => {
    try {
      await driverApi.toggleAccountStatus(driverId)
      await fetchDrivers()
      if (selectedDriver && selectedDriver.driverId === driverId) {
        setSelectedDriver({
          ...selectedDriver,
          account: { ...selectedDriver.account, accountStatus: !selectedDriver.account.accountStatus }
        })
      }
    } catch (error) {
      console.error('Error toggling account status:', error)
    }
  }

  const openDetail = async (driver) => {
    setSelectedDriver(driver)
    setActiveTab('info')
    setVehicleForm({ licensePlate: driver.licensePlate || '', vehicleTypeId: driver.vehicleTypeId || '' })
    setModalOpen(true)
  }

  const fetchWalletData = async () => {
    if (!selectedDriver) return
    try {
      const [w, t] = await Promise.all([
        driverApi.getDriverWallet(selectedDriver.driverId),
        driverApi.getDriverTransactions(selectedDriver.driverId)
      ])
      setWallet(w?.result || null)
      setTransactions(t?.result?.content || [])
    } catch (e) {
      console.error('Lỗi khi tải ví:', e)
    }
  }

  useEffect(() => {
    if (activeTab === 'wallet' && selectedDriver) {
      fetchWalletData()
    }
  }, [activeTab, selectedDriver])

  const handleAdjustBalance = async (e) => {
    e.preventDefault()
    if (!adjustForm.amount || !adjustForm.reason) return
    setAdjusting(true)
    try {
      const val = adjustForm.type === 'ADD' ? Math.abs(parseFloat(adjustForm.amount)) : -Math.abs(parseFloat(adjustForm.amount))
      await driverApi.adjustDriverBalance(selectedDriver.driverId, val, adjustForm.reason)
      setAdjustForm({ amount: '', reason: '', type: 'ADD' })
      await fetchWalletData()
    } catch (error) {
      console.error('Lỗi điều chỉnh số dư:', error)
    } finally {
      setAdjusting(false)
    }
  }

  const handleUpdateVehicle = async (e) => {
    e.preventDefault()
    setUpdatingVehicle(true)
    try {
      const payload = {
        licensePlate: vehicleForm.licensePlate,
        vehicleTypeId: vehicleForm.vehicleTypeId
      }
      const updated = await driverApi.updateDriver(selectedDriver.driverId, payload)
      setSelectedDriver(updated)
      await fetchDrivers()
    } catch (error) {
      console.error('Lỗi cập nhật phương tiện:', error)
    } finally {
      setUpdatingVehicle(false)
    }
  }

  // Filtered & Sorted Drivers with Client-side Fallback
  const processedDrivers = useMemo(() => {
    let result = [...drivers]

    // Search filter
    if (debouncedSearch) {
      const q = debouncedSearch.toLowerCase().trim()
      result = result.filter(d =>
        d.driverName?.toLowerCase().includes(q) ||
        d.phone?.includes(q) ||
        d.email?.toLowerCase().includes(q) ||
        d.licensePlate?.toLowerCase().includes(q) ||
        d.citizenId?.toLowerCase().includes(q) ||
        d.area?.toLowerCase().includes(q)
      )
    }

    // Account status filter
    if (accountStatusFilter === 'ACTIVE') {
      result = result.filter(d => d.account?.accountStatus === true)
    } else if (accountStatusFilter === 'LOCKED') {
      result = result.filter(d => d.account?.accountStatus === false)
    }

    // Activity status filter
    if (activityStatusFilter === 'ONLINE') {
      result = result.filter(d => d.activityStatus === true)
    } else if (activityStatusFilter === 'OFFLINE') {
      result = result.filter(d => d.activityStatus === false)
    }

    // Vehicle type filter
    if (vehicleTypeFilter !== 'ALL') {
      result = result.filter(d => d.vehicleTypeId === vehicleTypeFilter || d.vehicleTypeName === vehicleTypeFilter)
    }

    // Rating filter
    if (minRatingFilter !== 'ALL') {
      result = result.filter(d => (d.score ?? 5.0) >= Number(minRatingFilter))
    }

    // Sorting
    if (sortBy === 'driverName:asc') {
      result.sort((a, b) => (a.driverName || '').localeCompare(b.driverName || '', 'vi'))
    } else if (sortBy === 'driverName:desc') {
      result.sort((a, b) => (b.driverName || '').localeCompare(a.driverName || '', 'vi'))
    } else if (sortBy === 'score:desc') {
      result.sort((a, b) => (b.score ?? 0) - (a.score ?? 0))
    } else if (sortBy === 'score:asc') {
      result.sort((a, b) => (a.score ?? 0) - (b.score ?? 0))
    }

    return result
  }, [drivers, debouncedSearch, accountStatusFilter, activityStatusFilter, vehicleTypeFilter, minRatingFilter, sortBy])

  const handleClearFilters = () => {
    setSearchQuery('')
    setAccountStatusFilter('ALL')
    setActivityStatusFilter('ALL')
    setVehicleTypeFilter('ALL')
    setMinRatingFilter('ALL')
    setSortBy('driverName:asc')
  }

  // Export to CSV
  const handleExport = async () => {
    setExporting(true)
    try {
      const columns = [
        { label: 'Mã tài xế', key: 'driverId' },
        { label: 'Họ và tên', key: 'driverName' },
        { label: 'Số điện thoại', key: 'phone' },
        { label: 'Email', key: 'email' },
        { label: 'CCCD', key: 'citizenId' },
        { label: 'Biển số xe', key: 'licensePlate' },
        { label: 'Loại phương tiện', key: 'vehicleTypeName' },
        { label: 'Khu vực', key: 'area' },
        { label: 'Đánh giá (sao)', format: r => r.score ?? 5.0 },
        { label: 'Trạng thái trực tuyến', format: r => r.activityStatus ? 'Trực tuyến' : 'Ngoại tuyến' },
        { label: 'Trạng thái tài khoản', format: r => r.account?.accountStatus === false ? 'Đã khóa' : 'Hoạt động' },
      ]

      exportToCSV(`danh-sach-tai-xe-${Date.now()}.csv`, columns, processedDrivers)
    } catch (err) {
      alert(err.message || 'Lỗi khi xuất dữ liệu')
    } finally {
      setExporting(false)
    }
  }

  const hasActiveFilters = Boolean(
    searchQuery ||
    accountStatusFilter !== 'ALL' ||
    activityStatusFilter !== 'ALL' ||
    vehicleTypeFilter !== 'ALL' ||
    minRatingFilter !== 'ALL' ||
    sortBy !== 'driverName:asc'
  )

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="section-title">Quản lý Tài xế</h1>
          <p className="text-content-muted text-sm mt-1">
            Tổng cộng <span className="text-brand-400 font-semibold">{processedDrivers.length}</span> / {drivers.length} tài xế
          </p>
        </div>
        <div className="flex items-center gap-2">
          <button
            onClick={handleExport}
            disabled={exporting || processedDrivers.length === 0}
            className="btn-ghost flex items-center gap-2 text-sm border border-surface-border px-3.5 py-2 rounded-xl hover:bg-surface-card hover:text-brand-400 disabled:opacity-50 transition-colors"
            title="Xuất dữ liệu danh sách tài xế ra CSV Excel"
          >
            <RiDownloadLine size={16} />
            <span>{exporting ? 'Đang xuất...' : 'Xuất Excel / CSV'}</span>
          </button>
          <button
            onClick={fetchDrivers}
            className="p-2 rounded-xl border border-surface-border text-content-muted hover:text-content-main hover:bg-surface-card transition-colors"
            title="Làm mới danh sách"
          >
            <RiRefreshLine size={18} className={loading ? 'animate-spin' : ''} />
          </button>
        </div>
      </div>

      {/* Search & Filter Bar */}
      <div className="card p-4 space-y-3">
        <div className="flex flex-col md:flex-row items-stretch md:items-center gap-3">
          {/* Debounced Search */}
          <div className="flex-1 flex items-center gap-2.5 px-3.5 py-2 rounded-xl bg-surface-dark border border-surface-border focus-within:border-brand-500/50 transition-all">
            <RiSearchLine className="text-content-muted shrink-0" size={18} />
            <input 
              type="text" 
              placeholder="Tìm theo tên tài xế, SĐT, email, biển số, CCCD, khu vực..." 
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="bg-transparent border-none outline-none flex-1 text-sm text-content-main placeholder:text-content-muted"
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
                <option value="driverName:asc" className="bg-surface-card">Tên: A → Z</option>
                <option value="driverName:desc" className="bg-surface-card">Tên: Z → A</option>
                <option value="score:desc" className="bg-surface-card">Đánh giá cao nhất</option>
                <option value="score:asc" className="bg-surface-card">Đánh giá thấp nhất</option>
              </select>
            </div>

            {/* Toggle Filters Button */}
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
              <span>Bộ lọc</span>
              {hasActiveFilters && (
                <span className="w-2 h-2 rounded-full bg-brand-400 animate-pulse" />
              )}
            </button>
          </div>
        </div>

        {/* Expandable Filters Panel */}
        {showAdvancedFilters && (
          <div className="p-3.5 bg-surface-dark/40 rounded-xl border border-surface-border/70 space-y-3 animate-in fade-in duration-200">
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3 text-xs">
              {/* Account Status Filter */}
              <div>
                <label className="text-content-muted mb-1 block font-medium">Tài khoản:</label>
                <select
                  value={accountStatusFilter}
                  onChange={(e) => setAccountStatusFilter(e.target.value)}
                  className="w-full px-3 py-2 rounded-lg bg-surface-dark border border-surface-border text-content-main outline-none"
                >
                  <option value="ALL">Tất cả tài khoản</option>
                  <option value="ACTIVE">Đang hoạt động</option>
                  <option value="LOCKED">Đã khóa</option>
                </select>
              </div>

              {/* Activity Status Filter */}
              <div>
                <label className="text-content-muted mb-1 block font-medium">Trực tuyến:</label>
                <select
                  value={activityStatusFilter}
                  onChange={(e) => setActivityStatusFilter(e.target.value)}
                  className="w-full px-3 py-2 rounded-lg bg-surface-dark border border-surface-border text-content-main outline-none"
                >
                  <option value="ALL">Tất cả trạng thái</option>
                  <option value="ONLINE">Đang online (sẵn sàng đón)</option>
                  <option value="OFFLINE">Ngoại tuyến</option>
                </select>
              </div>

              {/* Vehicle Type Filter */}
              <div>
                <label className="text-content-muted mb-1 block font-medium">Loại xe:</label>
                <select
                  value={vehicleTypeFilter}
                  onChange={(e) => setVehicleTypeFilter(e.target.value)}
                  className="w-full px-3 py-2 rounded-lg bg-surface-dark border border-surface-border text-content-main outline-none"
                >
                  <option value="ALL">Tất cả loại xe</option>
                  {vehicleTypes.map(vt => (
                    <option key={vt.vehicleTypeId} value={vt.vehicleTypeId}>{vt.typeName}</option>
                  ))}
                </select>
              </div>

              {/* Rating Filter */}
              <div>
                <label className="text-content-muted mb-1 block font-medium">Đánh giá:</label>
                <select
                  value={minRatingFilter}
                  onChange={(e) => setMinRatingFilter(e.target.value)}
                  className="w-full px-3 py-2 rounded-lg bg-surface-dark border border-surface-border text-content-main outline-none"
                >
                  <option value="ALL">Tất cả đánh giá</option>
                  <option value="4.5">Từ 4.5 ★ trở lên</option>
                  <option value="4.0">Từ 4.0 ★ trở lên</option>
                </select>
              </div>
            </div>

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
      ) : processedDrivers.length === 0 ? (
        <div className="card p-12 text-center text-content-muted space-y-3">
          <p>Không tìm thấy tài xế nào khớp với điều kiện tìm kiếm.</p>
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
        <div className="card overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-surface-border bg-surface-dark/50">
                <th className="text-left px-5 py-3 text-xs font-semibold text-content-muted uppercase tracking-wider">Tài xế</th>
                <th className="text-left px-5 py-3 text-xs font-semibold text-content-muted uppercase tracking-wider">Liên hệ</th>
                <th className="text-left px-5 py-3 text-xs font-semibold text-content-muted uppercase tracking-wider">Xe</th>
                <th className="text-left px-5 py-3 text-xs font-semibold text-content-muted uppercase tracking-wider">Khu vực</th>
                <th className="text-center px-5 py-3 text-xs font-semibold text-content-muted uppercase tracking-wider">Trực tuyến</th>
                <th className="text-center px-5 py-3 text-xs font-semibold text-content-muted uppercase tracking-wider">Tài khoản</th>
                <th className="text-center px-5 py-3 text-xs font-semibold text-content-muted uppercase tracking-wider">Thao tác</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-surface-border">
              {processedDrivers.map(d => {
                const isLocked = d.account?.accountStatus === false
                return (
                  <tr key={d.driverId} className={`hover:bg-surface-border/10 transition-colors ${isLocked ? 'opacity-75' : ''}`}>
                    <td className="px-5 py-3">
                      <div className="flex items-center gap-3">
                        <div className="w-10 h-10 rounded-full bg-surface-border overflow-hidden shrink-0">
                          {d.avatar ? (
                            <img src={d.avatar} alt={d.driverName} className="w-full h-full object-cover" />
                          ) : (
                            <div className="w-full h-full flex items-center justify-center font-bold text-content-muted bg-surface-dark">
                              {d.driverName?.charAt(0) || 'D'}
                            </div>
                          )}
                        </div>
                        <div>
                          <p className="font-medium text-content-main">{d.driverName}</p>
                          <p className="text-xs text-content-muted flex items-center gap-1">
                            ⭐ {d.score || 5.0}
                          </p>
                        </div>
                      </div>
                    </td>
                    <td className="px-5 py-3">
                      <p className="text-content-main">{d.phone}</p>
                      <p className="text-xs text-content-muted">{d.email}</p>
                    </td>
                    <td className="px-5 py-3">
                      <p className="text-content-main">{d.vehicleTypeName}</p>
                      <p className="text-xs font-mono text-content-muted">{d.licensePlate}</p>
                    </td>
                    <td className="px-5 py-3">
                      <div className="flex items-center gap-1 text-content-muted">
                        <RiMapPinLine size={14} />
                        <span>{d.area || 'Chưa cập nhật'}</span>
                      </div>
                    </td>
                    <td className="px-5 py-3 text-center">
                      {d.activityStatus ? (
                        <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 text-xs font-medium">
                          <span className="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse" /> Online
                        </span>
                      ) : (
                        <span className="inline-flex items-center px-2 py-1 rounded-full bg-surface-border/50 text-content-muted text-xs">
                          Offline
                        </span>
                      )}
                    </td>
                    <td className="px-5 py-3 text-center">
                      {isLocked ? (
                        <span className="inline-flex items-center px-2.5 py-1 rounded-md bg-red-500/10 text-red-500 border border-red-500/20 text-xs font-medium">
                          Đã khóa
                        </span>
                      ) : (
                        <span className="inline-flex items-center px-2.5 py-1 rounded-md bg-green-500/10 text-green-500 border border-green-500/20 text-xs font-medium">
                          Hoạt động
                        </span>
                      )}
                    </td>
                    <td className="px-5 py-3 text-center">
                      <div className="flex justify-center gap-2">
                        <button 
                          onClick={() => {
                            setDriverToEdit(d)
                            setEditModalOpen(true)
                          }}
                          className="p-2 rounded-lg bg-orange-500/10 text-orange-500 hover:bg-orange-500/20 transition-colors"
                          title="Sửa thông tin"
                        >
                          <RiEditLine size={18} />
                        </button>
                        <button 
                          onClick={() => {
                            setDriverToChangePassword(d)
                            setPasswordModalOpen(true)
                          }}
                          className="p-2 rounded-lg bg-blue-500/10 text-blue-500 hover:bg-blue-500/20 transition-colors"
                          title="Đổi mật khẩu"
                        >
                          <RiKey2Line size={18} />
                        </button>
                        <button 
                          onClick={() => handleToggleAccount(d.driverId)}
                          className={`p-2 rounded-lg transition-colors ${
                            isLocked 
                              ? 'bg-green-500/10 text-green-500 hover:bg-green-500/20' 
                              : 'bg-red-500/10 text-red-500 hover:bg-red-500/20'
                          }`}
                          title={isLocked ? 'Mở khóa tài khoản' : 'Khóa tài khoản'}
                        >
                          {isLocked ? <RiLockUnlockLine size={18} /> : <RiLock2Line size={18} />}
                        </button>
                        <button 
                          onClick={() => openDetail(d)}
                          className="p-2 rounded-lg bg-brand-500/10 text-brand-500 hover:bg-brand-500/20 transition-colors"
                          title="Xem chi tiết, xe và ví tiền"
                        >
                          <RiEyeLine size={18} />
                        </button>
                      </div>
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        </div>
      )}

      {/* Edit Driver Modal */}
      <AdminEditDriverModal
        open={editModalOpen}
        onClose={() => setEditModalOpen(false)}
        driver={driverToEdit}
        onSuccess={() => {
          fetchDrivers()
          setEditModalOpen(false)
        }}
      />

      {/* Change Password Modal */}
      <AdminChangePasswordModal
        open={passwordModalOpen}
        onClose={() => setPasswordModalOpen(false)}
        target={driverToChangePassword}
        targetType="driver"
        onSuccess={() => {
          setPasswordModalOpen(false)
        }}
      />

      {/* Detail Modal */}
      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title="Thông tin chi tiết tài xế">
        {selectedDriver && (
          <div className="space-y-6">
            {/* Tabs */}
            <div className="flex border-b border-surface-border">
              <button
                className={`flex items-center gap-2 py-3 px-4 text-sm font-medium border-b-2 transition-colors ${
                  activeTab === 'info' ? 'border-brand-500 text-brand-500' : 'border-transparent text-content-muted hover:text-content-main'
                }`}
                onClick={() => setActiveTab('info')}
              >
                <RiUserLine size={16} /> Thông tin cá nhân
              </button>
              <button
                className={`flex items-center gap-2 py-3 px-4 text-sm font-medium border-b-2 transition-colors ${
                  activeTab === 'vehicle' ? 'border-brand-500 text-brand-500' : 'border-transparent text-content-muted hover:text-content-main'
                }`}
                onClick={() => setActiveTab('vehicle')}
              >
                <RiCarLine size={16} /> Phương tiện
              </button>
              <button
                className={`flex items-center gap-2 py-3 px-4 text-sm font-medium border-b-2 transition-colors ${
                  activeTab === 'wallet' ? 'border-brand-500 text-brand-500' : 'border-transparent text-content-muted hover:text-content-main'
                }`}
                onClick={() => setActiveTab('wallet')}
              >
                <RiWallet3Line size={16} /> Ví & Giao dịch
              </button>
            </div>

            {/* TAB: INFO */}
            {activeTab === 'info' && (
              <div className="space-y-4">
                <div className="flex items-center gap-4 p-4 rounded-xl bg-surface-dark border border-surface-border">
                  <div className="w-16 h-16 rounded-full bg-surface-border overflow-hidden shrink-0">
                    {selectedDriver.avatar ? (
                      <img src={selectedDriver.avatar} alt="" className="w-full h-full object-cover" />
                    ) : (
                      <div className="w-full h-full flex items-center justify-center font-bold text-2xl text-content-muted">
                        {selectedDriver.driverName?.charAt(0) || 'D'}
                      </div>
                    )}
                  </div>
                  <div>
                    <h4 className="text-lg font-bold text-content-main">{selectedDriver.driverName}</h4>
                    <p className="text-sm text-content-muted">⭐ Đánh giá: {selectedDriver.score || 5.0} sao</p>
                    <p className="text-xs text-brand-500 font-medium mt-1">ID: {selectedDriver.driverId}</p>
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div className="p-3 bg-surface-dark rounded-xl border border-surface-border">
                    <p className="text-xs text-content-muted">Số điện thoại</p>
                    <p className="font-medium text-content-main">{selectedDriver.phone || 'Chưa có'}</p>
                  </div>
                  <div className="p-3 bg-surface-dark rounded-xl border border-surface-border">
                    <p className="text-xs text-content-muted">Email</p>
                    <p className="font-medium text-content-main">{selectedDriver.email || 'Chưa có'}</p>
                  </div>
                  <div className="p-3 bg-surface-dark rounded-xl border border-surface-border">
                    <p className="text-xs text-content-muted">CCCD / CMND</p>
                    <p className="font-medium text-content-main">{selectedDriver.citizenId || 'Chưa có'}</p>
                  </div>
                  <div className="p-3 bg-surface-dark rounded-xl border border-surface-border">
                    <p className="text-xs text-content-muted">Khu vực hoạt động</p>
                    <p className="font-medium text-content-main">{selectedDriver.area || 'Chưa có'}</p>
                  </div>
                </div>

                <div className="p-3 bg-surface-dark rounded-xl border border-surface-border flex items-center justify-between">
                  <div>
                    <p className="text-xs text-content-muted">Trạng thái tài khoản</p>
                    <p className="font-medium text-content-main">
                      {selectedDriver.account?.accountStatus === false ? 'Đang bị khóa' : 'Đang hoạt động'}
                    </p>
                  </div>
                  <button 
                    onClick={() => handleToggleAccount(selectedDriver.driverId)}
                    className={`px-3 py-1.5 rounded-lg text-xs font-semibold ${
                      selectedDriver.account?.accountStatus === false
                        ? 'bg-green-500/10 text-green-500 hover:bg-green-500/20' 
                        : 'bg-red-500/10 text-red-500 hover:bg-red-500/20'
                    }`}
                  >
                    {selectedDriver.account?.accountStatus === false ? 'Mở khóa ngay' : 'Khóa tài khoản'}
                  </button>
                </div>
              </div>
            )}

            {/* TAB: VEHICLE */}
            {activeTab === 'vehicle' && (
              <div className="space-y-4">
                <form onSubmit={handleUpdateVehicle} className="space-y-4">
                  <div>
                    <label className="text-sm font-medium text-content-muted block mb-1">Loại phương tiện</label>
                    <select
                      value={vehicleForm.vehicleTypeId}
                      onChange={(e) => setVehicleForm({ ...vehicleForm, vehicleTypeId: e.target.value })}
                      className="input-field bg-surface-dark"
                    >
                      <option value="">-- Chọn loại xe --</option>
                      {vehicleTypes.map(vt => (
                        <option key={vt.vehicleTypeId} value={vt.vehicleTypeId}>{vt.typeName}</option>
                      ))}
                    </select>
                  </div>

                  <div>
                    <label className="text-sm font-medium text-content-muted block mb-1">Biển số xe</label>
                    <input
                      type="text"
                      placeholder="Ví dụ: 29A-12345"
                      value={vehicleForm.licensePlate}
                      onChange={(e) => setVehicleForm({ ...vehicleForm, licensePlate: e.target.value })}
                      className="input-field bg-surface-dark font-mono"
                    />
                  </div>

                  <div className="flex justify-end gap-2 pt-2">
                    <button
                      type="submit"
                      disabled={updatingVehicle}
                      className="btn-primary"
                    >
                      {updatingVehicle ? 'Đang cập nhật...' : 'Lưu thay đổi'}
                    </button>
                  </div>
                </form>
              </div>
            )}

            {/* TAB: WALLET */}
            {activeTab === 'wallet' && (
              <div className="space-y-4">
                {wallet ? (
                  <div className="p-4 rounded-xl bg-surface-dark border border-surface-border flex items-center justify-between">
                    <div>
                      <p className="text-xs text-content-muted">Số dư hiện tại</p>
                      <h3 className="text-2xl font-bold text-green-500 font-mono">
                        {formatMoney(wallet.balance)}
                      </h3>
                      <p className="text-xs text-content-muted mt-1">Trạng thái ví: <span className="text-content-main uppercase">{wallet.walletStatus}</span></p>
                    </div>
                  </div>
                ) : (
                  <p className="text-content-muted text-sm italic">Đang tải thông tin ví...</p>
                )}

                {/* Adjust balance form */}
                <form onSubmit={handleAdjustBalance} className="p-4 rounded-xl border border-surface-border bg-surface-dark/50 space-y-3">
                  <h4 className="font-semibold text-sm text-content-main">Điều chỉnh số dư ví (Admin)</h4>
                  <div className="grid grid-cols-2 gap-3">
                    <div>
                      <label className="text-xs text-content-muted mb-1 block">Loại thao tác</label>
                      <select
                        value={adjustForm.type}
                        onChange={(e) => setAdjustForm({ ...adjustForm, type: e.target.value })}
                        className="input-field bg-surface-dark text-sm"
                      >
                        <option value="ADD">Cộng tiền (+)</option>
                        <option value="SUBTRACT">Trừ tiền (-)</option>
                      </select>
                    </div>
                    <div>
                      <label className="text-xs text-content-muted mb-1 block">Số tiền (VNĐ)</label>
                      <input
                        type="number"
                        placeholder="100000"
                        value={adjustForm.amount}
                        onChange={(e) => setAdjustForm({ ...adjustForm, amount: e.target.value })}
                        className="input-field bg-surface-dark text-sm font-mono"
                      />
                    </div>
                  </div>
                  <div>
                    <label className="text-xs text-content-muted mb-1 block">Lý do điều chỉnh</label>
                    <input
                      type="text"
                      placeholder="Ví dụ: Thưởng hiệu suất, Hoàn tiền..."
                      value={adjustForm.reason}
                      onChange={(e) => setAdjustForm({ ...adjustForm, reason: e.target.value })}
                      className="input-field bg-surface-dark text-sm"
                    />
                  </div>
                  <div className="flex justify-end">
                    <button
                      type="submit"
                      disabled={adjusting || !adjustForm.amount || !adjustForm.reason}
                      className="btn-primary py-1.5 px-3 text-xs"
                    >
                      {adjusting ? 'Đang thực hiện...' : 'Xác nhận điều chỉnh'}
                    </button>
                  </div>
                </form>

                {/* Transaction history */}
                <div>
                  <h4 className="font-semibold text-sm text-content-main mb-2">Lịch sử giao dịch gần nhất</h4>
                  {transactions.length === 0 ? (
                    <p className="text-xs text-content-muted italic">Chưa có giao dịch nào.</p>
                  ) : (
                    <div className="divide-y divide-surface-border max-h-48 overflow-y-auto">
                      {transactions.map(t => (
                        <div key={t.transactionId} className="py-2 flex items-center justify-between text-xs">
                          <div>
                            <p className="font-medium text-content-main">{t.description || t.transactionType}</p>
                            <p className="text-content-muted">{new Date(t.createdAt).toLocaleString('vi-VN')}</p>
                          </div>
                          <span className={`font-mono font-bold ${
                            t.transactionType === TransactionType.DEPOSIT || t.transactionType === TransactionType.TRIP_INCOME || t.amount > 0
                              ? 'text-green-500' : 'text-red-500'
                          }`}>
                            {t.amount > 0 ? `+${formatMoney(t.amount)}` : formatMoney(t.amount)}
                          </span>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              </div>
            )}
          </div>
        )}
      </Modal>
    </div>
  )
}

export default AdminDriversPage
