import { useState, useEffect, useCallback, useMemo } from 'react'
import { customerApi } from '@/features/customer/api/customerApi'
import Spinner from '@/components/Elements/Spinner'
import {
  RiSearchLine, RiEyeLine, RiCloseLine,
  RiLock2Line, RiLockUnlockLine, RiUserLine, RiMapPinLine,
  RiEditLine, RiKey2Line, RiDownloadLine, RiFilterLine,
  RiArrowUpDownLine, RiRefreshLine
} from 'react-icons/ri'
import { cn } from '@/utils/cn'
import AdminEditCustomerModal from '@/features/admin/components/AdminEditCustomerModal'
import AdminChangePasswordModal from '@/features/admin/components/AdminChangePasswordModal'
import { useDebounce } from '@/hooks/useDebounce'
import { exportToCSV } from '@/utils/exportUtils'

const Modal = ({ open, onClose, title, children }) => {
  if (!open) return null
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm" onClick={onClose}>
      <div className="card w-full max-w-xl max-h-[90vh] mx-4 flex flex-col p-0 animate-in fade-in zoom-in-95" onClick={e => e.stopPropagation()}>
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

const AdminCustomersPage = () => {
  const [customers, setCustomers] = useState([])
  const [loading, setLoading] = useState(true)
  const [exporting, setExporting] = useState(false)

  // Search with useDebounce
  const [searchQuery, setSearchQuery] = useState('')
  const debouncedSearch = useDebounce(searchQuery, 400)

  // Filters & Sorting
  const [accountStatusFilter, setAccountStatusFilter] = useState('ALL') // ALL, ACTIVE, LOCKED
  const [genderFilter, setGenderFilter] = useState('ALL') // ALL, Nam, Nữ
  const [sortBy, setSortBy] = useState('customerName:asc')
  const [showAdvancedFilters, setShowAdvancedFilters] = useState(false)

  // Pagination
  const [pagination, setPagination] = useState({ page: 0, totalPages: 1, totalElements: 0 })

  // Modals
  const [selectedCustomer, setSelectedCustomer] = useState(null)
  const [modalOpen, setModalOpen] = useState(false)
  const [editModalOpen, setEditModalOpen] = useState(false)
  const [passwordModalOpen, setPasswordModalOpen] = useState(false)
  const [customerToEdit, setCustomerToEdit] = useState(null)
  const [customerToChangePassword, setCustomerToChangePassword] = useState(null)

  const fetchCustomers = useCallback(async (page = 0) => {
    setLoading(true)
    try {
      const res = await customerApi.getAllForAdmin({
        page,
        size: 50,
        search: debouncedSearch || undefined,
        accountStatus: accountStatusFilter === 'ACTIVE' ? true : accountStatusFilter === 'LOCKED' ? false : undefined,
        genders: genderFilter !== 'ALL' ? genderFilter : undefined,
        sort: sortBy
      })
      const content = res?.content || (Array.isArray(res) ? res : [])
      setCustomers(content)
      setPagination({
        page: res?.page?.number ?? res?.number ?? page,
        totalPages: res?.page?.totalPages ?? res?.totalPages ?? 1,
        totalElements: res?.page?.totalElements ?? res?.totalElements ?? content.length
      })
    } catch (e) {
      console.error('Error fetching customers:', e)
    } finally {
      setLoading(false)
    }
  }, [debouncedSearch, accountStatusFilter, genderFilter, sortBy])

  useEffect(() => {
    fetchCustomers(0)
  }, [fetchCustomers])

  const openDetail = async (customer) => {
    try {
      const res = await customerApi.getById(customer.customerId)
      setSelectedCustomer(res.result || customer)
    } catch {
      setSelectedCustomer(customer)
    }
    setModalOpen(true)
  }

  const handleToggleAccount = async (customerId) => {
    try {
      await customerApi.toggleAccountStatus(customerId)
      await fetchCustomers(pagination.page)
      if (selectedCustomer?.customerId === customerId) {
        setSelectedCustomer(prev => ({
          ...prev,
          account: { ...prev.account, accountStatus: !prev.account?.accountStatus }
        }))
      }
    } catch (e) {
      console.error(e)
    }
  }

  // Filtered and sorted customers with client-side fallback
  const processedCustomers = useMemo(() => {
    let result = [...customers]

    // Client-side search fallback
    if (debouncedSearch) {
      const q = debouncedSearch.toLowerCase().trim()
      result = result.filter(c =>
        c.customerName?.toLowerCase().includes(q) ||
        c.phone?.includes(q) ||
        c.email?.toLowerCase().includes(q) ||
        c.address?.toLowerCase().includes(q)
      )
    }

    // Account status filter
    if (accountStatusFilter === 'ACTIVE') {
      result = result.filter(c => c.account?.accountStatus === true)
    } else if (accountStatusFilter === 'LOCKED') {
      result = result.filter(c => c.account?.accountStatus === false)
    }

    // Gender filter
    if (genderFilter !== 'ALL') {
      result = result.filter(c => c.gender?.toLowerCase() === genderFilter.toLowerCase())
    }

    // Sorting
    if (sortBy === 'customerName:asc') {
      result.sort((a, b) => (a.customerName || '').localeCompare(b.customerName || '', 'vi'))
    } else if (sortBy === 'customerName:desc') {
      result.sort((a, b) => (b.customerName || '').localeCompare(a.customerName || '', 'vi'))
    } else if (sortBy === 'createdAt:desc') {
      result.sort((a, b) => new Date(b.createdAt || 0) - new Date(a.createdAt || 0))
    }

    return result
  }, [customers, debouncedSearch, accountStatusFilter, genderFilter, sortBy])

  const handleClearFilters = () => {
    setSearchQuery('')
    setAccountStatusFilter('ALL')
    setGenderFilter('ALL')
    setSortBy('customerName:asc')
  }

  // Export CSV
  const handleExport = async () => {
    setExporting(true)
    try {
      const columns = [
        { label: 'Mã khách hàng', key: 'customerId' },
        { label: 'Họ và tên', key: 'customerName' },
        { label: 'Số điện thoại', key: 'phone' },
        { label: 'Email', key: 'email' },
        { label: 'Giới tính', key: 'gender' },
        { label: 'Địa chỉ', key: 'address' },
        { label: 'Trạng thái tài khoản', format: r => r.account?.accountStatus === false ? 'Đã khóa' : 'Hoạt động' },
      ]

      exportToCSV(`danh-sach-khach-hang-${Date.now()}.csv`, columns, processedCustomers)
    } catch (err) {
      alert(err.message || 'Lỗi khi xuất dữ liệu')
    } finally {
      setExporting(false)
    }
  }

  const hasActiveFilters = Boolean(
    searchQuery ||
    accountStatusFilter !== 'ALL' ||
    genderFilter !== 'ALL' ||
    sortBy !== 'customerName:asc'
  )

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="section-title">Quản lý Khách hàng</h1>
          <p className="text-content-muted text-sm mt-1">
            Tổng cộng <span className="text-brand-400 font-semibold">{pagination.totalElements}</span> khách hàng
          </p>
        </div>
        <div className="flex items-center gap-2">
          <button
            onClick={handleExport}
            disabled={exporting || processedCustomers.length === 0}
            className="btn-ghost flex items-center gap-2 text-sm border border-surface-border px-3.5 py-2 rounded-xl hover:bg-surface-card hover:text-brand-400 disabled:opacity-50 transition-colors"
            title="Xuất dữ liệu khách hàng ra file CSV Excel"
          >
            <RiDownloadLine size={16} />
            <span>{exporting ? 'Đang xuất...' : 'Xuất Excel / CSV'}</span>
          </button>
          <button
            onClick={() => fetchCustomers(pagination.page)}
            className="p-2 rounded-xl border border-surface-border text-content-muted hover:text-content-main hover:bg-surface-card transition-colors"
            title="Làm mới"
          >
            <RiRefreshLine size={18} className={loading ? 'animate-spin' : ''} />
          </button>
        </div>
      </div>

      {/* Search & Filter Card */}
      <div className="card p-4 space-y-3">
        <div className="flex flex-col md:flex-row items-stretch md:items-center gap-3">
          {/* Debounced Search */}
          <div className="flex-1 flex items-center gap-2.5 px-3.5 py-2 rounded-xl bg-surface-dark border border-surface-border focus-within:border-brand-500/50 transition-all">
            <RiSearchLine className="text-content-muted shrink-0" size={18} />
            <input
              type="text"
              placeholder="Tìm theo tên khách hàng, số điện thoại, email, địa chỉ..."
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
                <option value="customerName:asc" className="bg-surface-card">Tên: A → Z</option>
                <option value="customerName:desc" className="bg-surface-card">Tên: Z → A</option>
                <option value="createdAt:desc" className="bg-surface-card">Mới tham gia</option>
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
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 text-xs">
              {/* Account Status Filter */}
              <div>
                <label className="text-content-muted mb-1 block font-medium">Trạng thái tài khoản:</label>
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

              {/* Gender Filter */}
              <div>
                <label className="text-content-muted mb-1 block font-medium">Giới tính:</label>
                <select
                  value={genderFilter}
                  onChange={(e) => setGenderFilter(e.target.value)}
                  className="w-full px-3 py-2 rounded-lg bg-surface-dark border border-surface-border text-content-main outline-none"
                >
                  <option value="ALL">Tất cả giới tính</option>
                  <option value="Nam">Nam</option>
                  <option value="Nữ">Nữ</option>
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
      <div className="card overflow-hidden">
        {loading ? (
          <div className="flex justify-center py-16"><Spinner size="xl" /></div>
        ) : (
          <>
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-surface-border bg-surface-dark/50">
                    <th className="text-left px-5 py-3 text-xs font-semibold text-content-muted uppercase tracking-wider">#</th>
                    <th className="text-left px-5 py-3 text-xs font-semibold text-content-muted uppercase tracking-wider">Khách hàng</th>
                    <th className="text-left px-5 py-3 text-xs font-semibold text-content-muted uppercase tracking-wider">Liên hệ</th>
                    <th className="text-left px-5 py-3 text-xs font-semibold text-content-muted uppercase tracking-wider">Địa chỉ</th>
                    <th className="text-center px-5 py-3 text-xs font-semibold text-content-muted uppercase tracking-wider">Trạng thái</th>
                    <th className="text-center px-5 py-3 text-xs font-semibold text-content-muted uppercase tracking-wider">Thao tác</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-surface-border">
                  {processedCustomers.length === 0 ? (
                    <tr>
                      <td colSpan="6" className="text-center text-content-muted py-12">
                        Không tìm thấy khách hàng nào phù hợp
                      </td>
                    </tr>
                  ) : processedCustomers.map((c, i) => {
                    const isLocked = c.account?.accountStatus === false
                    return (
                      <tr key={c.customerId} className={cn('hover:bg-surface-border/10 transition-colors', isLocked && 'opacity-70')}>
                        <td className="px-5 py-3 text-content-muted text-xs font-mono">{pagination.page * 50 + i + 1}</td>
                        <td className="px-5 py-3">
                          <div className="flex items-center gap-3">
                            <div className="w-9 h-9 rounded-full overflow-hidden shrink-0 bg-surface-dark border border-surface-border">
                              {c.avatar
                                ? <img src={c.avatar} alt={c.customerName} className="w-full h-full object-cover" />
                                : <div className="w-full h-full flex items-center justify-center text-sm font-bold text-content-muted">
                                    {c.customerName?.charAt(0) || 'K'}
                                  </div>
                              }
                            </div>
                            <div>
                              <p className="font-medium text-content-main">{c.customerName || 'Chưa có tên'}</p>
                              {c.gender && <span className="text-xs text-content-muted">{c.gender}</span>}
                            </div>
                          </div>
                        </td>
                        <td className="px-5 py-3">
                          <p className="text-content-main">{c.phone}</p>
                          <p className="text-xs text-content-muted">{c.email || 'Chưa cập nhật'}</p>
                        </td>
                        <td className="px-5 py-3">
                          <div className="flex items-center gap-1 text-content-muted text-xs">
                            <RiMapPinLine size={13} />
                            <span>{c.address || 'Chưa cập nhật'}</span>
                          </div>
                        </td>
                        <td className="px-5 py-3 text-center">
                          {isLocked
                            ? <span className="inline-flex items-center px-2.5 py-1 rounded-md bg-red-500/10 text-red-400 border border-red-500/20 text-xs font-medium">Đã khóa</span>
                            : <span className="inline-flex items-center px-2.5 py-1 rounded-md bg-green-500/10 text-green-400 border border-green-500/20 text-xs font-medium">Hoạt động</span>
                          }
                        </td>
                        <td className="px-5 py-3 text-center">
                          <div className="flex items-center justify-center gap-2">
                            <button
                              onClick={() => { setCustomerToEdit(c); setEditModalOpen(true) }}
                              className="p-2 rounded-lg bg-orange-500/10 text-orange-400 hover:bg-orange-500/20 transition-colors"
                              title="Sửa thông tin"
                            >
                              <RiEditLine size={16} />
                            </button>
                            <button
                              onClick={() => { setCustomerToChangePassword(c); setPasswordModalOpen(true) }}
                              className="p-2 rounded-lg bg-blue-500/10 text-blue-400 hover:bg-blue-500/20 transition-colors"
                              title="Đổi mật khẩu"
                            >
                              <RiKey2Line size={16} />
                            </button>
                            <button
                              onClick={() => handleToggleAccount(c.customerId)}
                              className={cn(
                                'p-2 rounded-lg transition-colors',
                                isLocked
                                  ? 'bg-green-500/10 text-green-400 hover:bg-green-500/20'
                                  : 'bg-red-500/10 text-red-400 hover:bg-red-500/20'
                              )}
                              title={isLocked ? 'Mở khóa tài khoản' : 'Khóa tài khoản'}
                            >
                              {isLocked ? <RiLockUnlockLine size={16} /> : <RiLock2Line size={16} />}
                            </button>
                            <button
                              onClick={() => openDetail(c)}
                              className="p-2 rounded-lg bg-brand-500/10 text-brand-400 hover:bg-brand-500/20 transition-colors"
                              title="Xem chi tiết"
                            >
                              <RiEyeLine size={16} />
                            </button>
                          </div>
                        </td>
                      </tr>
                    )
                  })}
                </tbody>
              </table>
            </div>

            {/* Pagination */}
            {pagination.totalPages > 1 && (
              <div className="flex items-center justify-between px-5 py-3 border-t border-surface-border">
                <p className="text-xs text-content-muted">
                  Trang {pagination.page + 1} / {pagination.totalPages}
                </p>
                <div className="flex gap-2">
                  <button
                    onClick={() => fetchCustomers(pagination.page - 1)}
                    disabled={pagination.page === 0}
                    className="px-3 py-1 rounded-lg border border-surface-border text-xs text-content-muted hover:text-content-main hover:bg-surface-border/30 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
                  >
                    Trước
                  </button>
                  <button
                    onClick={() => fetchCustomers(pagination.page + 1)}
                    disabled={pagination.page >= pagination.totalPages - 1}
                    className="px-3 py-1 rounded-lg border border-surface-border text-xs text-content-muted hover:text-content-main hover:bg-surface-border/30 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
                  >
                    Sau
                  </button>
                </div>
              </div>
            )}
          </>
        )}
      </div>

      {/* Edit Customer Modal */}
      <AdminEditCustomerModal
        open={editModalOpen}
        onClose={() => setEditModalOpen(false)}
        customer={customerToEdit}
        onSuccess={() => {
          fetchCustomers(pagination.page)
          setEditModalOpen(false)
        }}
      />

      {/* Change Password Modal */}
      <AdminChangePasswordModal
        open={passwordModalOpen}
        onClose={() => setPasswordModalOpen(false)}
        target={customerToChangePassword}
        targetType="customer"
        onSuccess={() => {
          setPasswordModalOpen(false)
        }}
      />

      {/* Detail Modal */}
      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title="Thông tin Khách hàng">
        {selectedCustomer && (
          <div className="space-y-5">
            <div className="flex items-center gap-4 p-4 rounded-xl bg-surface-dark border border-surface-border">
              <div className="w-16 h-16 rounded-full overflow-hidden shrink-0 bg-surface-dark border border-surface-border">
                {selectedCustomer.avatar
                  ? <img src={selectedCustomer.avatar} alt={selectedCustomer.customerName} className="w-full h-full object-cover" />
                  : <div className="w-full h-full flex items-center justify-center text-2xl font-bold text-content-muted">
                      {selectedCustomer.customerName?.charAt(0) || 'K'}
                    </div>
                }
              </div>
              <div>
                <h4 className="text-base font-bold text-content-main">{selectedCustomer.customerName || 'Chưa có tên'}</h4>
                <p className="text-xs font-mono text-brand-400 mt-0.5">{selectedCustomer.customerId}</p>
                <span className={cn(
                  'inline-block mt-2 text-xs px-2.5 py-0.5 rounded-md font-medium',
                  selectedCustomer.account?.accountStatus === false
                    ? 'bg-red-500/10 text-red-400'
                    : 'bg-green-500/10 text-green-400'
                )}>
                  {selectedCustomer.account?.accountStatus === false ? 'Đã khóa' : 'Hoạt động'}
                </span>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-3 text-sm">
              <div className="p-3 rounded-xl bg-surface-dark/50 border border-surface-border">
                <p className="text-xs text-content-muted mb-0.5">Số điện thoại</p>
                <p className="font-medium text-content-main">{selectedCustomer.phone || '—'}</p>
              </div>
              <div className="p-3 rounded-xl bg-surface-dark/50 border border-surface-border">
                <p className="text-xs text-content-muted mb-0.5">Email</p>
                <p className="font-medium text-content-main">{selectedCustomer.email || '—'}</p>
              </div>
              <div className="p-3 rounded-xl bg-surface-dark/50 border border-surface-border">
                <p className="text-xs text-content-muted mb-0.5">Ngày sinh</p>
                <p className="font-medium text-content-main">{selectedCustomer.birthDate || '—'}</p>
              </div>
              <div className="p-3 rounded-xl bg-surface-dark/50 border border-surface-border">
                <p className="text-xs text-content-muted mb-0.5">Giới tính</p>
                <p className="font-medium text-content-main">{selectedCustomer.gender || '—'}</p>
              </div>
              <div className="col-span-2 p-3 rounded-xl bg-surface-dark/50 border border-surface-border">
                <p className="text-xs text-content-muted mb-0.5">Địa chỉ</p>
                <p className="font-medium text-content-main">{selectedCustomer.address || '—'}</p>
              </div>
            </div>

            <div className="flex justify-end gap-3 pt-2 border-t border-surface-border">
              <button
                onClick={() => handleToggleAccount(selectedCustomer.customerId)}
                className={cn(
                  'px-4 py-2 rounded-xl text-xs font-semibold transition-colors',
                  selectedCustomer.account?.accountStatus === false
                    ? 'bg-green-500/10 text-green-400 hover:bg-green-500/20'
                    : 'bg-red-500/10 text-red-400 hover:bg-red-500/20'
                )}
              >
                {selectedCustomer.account?.accountStatus === false ? 'Mở khóa tài khoản' : 'Khóa tài khoản'}
              </button>
            </div>
          </div>
        )}
      </Modal>
    </div>
  )
}

export default AdminCustomersPage
