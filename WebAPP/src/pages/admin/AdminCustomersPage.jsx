import { useState, useEffect, useCallback } from 'react'
import { customerApi } from '@/features/customer/api/customerApi'
import Spinner from '@/components/Elements/Spinner'
import {
  RiSearchLine, RiEyeLine, RiCloseLine,
  RiLock2Line, RiLockUnlockLine, RiUserLine, RiMapPinLine,
} from 'react-icons/ri'
import { cn } from '@/utils/cn'

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
  const [search, setSearch] = useState('')
  const [inputValue, setInputValue] = useState('')
  const [pagination, setPagination] = useState({ page: 0, totalPages: 1, totalElements: 0 })

  const [selectedCustomer, setSelectedCustomer] = useState(null)
  const [modalOpen, setModalOpen] = useState(false)

  const fetchCustomers = useCallback(async (page = 0) => {
    setLoading(true)
    try {
      const res = await customerApi.getAllForAdmin(page, 20, search)
      const data = res.result
      setCustomers(data?.content || [])
      setPagination({ page: data?.number || 0, totalPages: data?.totalPages || 1, totalElements: data?.totalElements || 0 })
    } catch (e) {
      console.error(e)
    } finally {
      setLoading(false)
    }
  }, [search])

  useEffect(() => {
    const timer = setTimeout(() => fetchCustomers(0), 300)
    return () => clearTimeout(timer)
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

  const handleSearch = (e) => {
    e.preventDefault()
    setSearch(inputValue)
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="section-title">Quản lý Khách hàng</h1>
        <p className="text-content-muted text-sm mt-1">
          Tổng cộng <span className="text-brand-400 font-semibold">{pagination.totalElements}</span> khách hàng
        </p>
      </div>

      {/* Search bar */}
      <form onSubmit={handleSearch} className="card p-3 flex items-center gap-3">
        <RiSearchLine className="text-content-muted shrink-0" size={20} />
        <input
          type="text"
          placeholder="Tìm kiếm theo tên, số điện thoại, email..."
          value={inputValue}
          onChange={e => setInputValue(e.target.value)}
          className="bg-transparent border-none outline-none flex-1 text-sm text-content-main placeholder:text-content-muted"
        />
        <button type="submit" className="px-3 py-1.5 bg-brand-500 text-white rounded-lg text-xs font-medium hover:bg-brand-600 transition-colors">
          Tìm
        </button>
        {search && (
          <button type="button" onClick={() => { setInputValue(''); setSearch('') }}
            className="px-3 py-1.5 border border-surface-border text-content-muted rounded-lg text-xs hover:bg-surface-border/30 transition-colors">
            Xóa
          </button>
        )}
      </form>

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
                  {customers.length === 0 ? (
                    <tr><td colSpan="6" className="text-center text-content-muted py-12">Không tìm thấy khách hàng nào</td></tr>
                  ) : customers.map((c, i) => {
                    const isLocked = c.account?.accountStatus === false
                    return (
                      <tr key={c.customerId} className={cn('hover:bg-surface-border/10 transition-colors', isLocked && 'opacity-70')}>
                        <td className="px-5 py-3 text-content-muted text-xs font-mono">{pagination.page * 20 + i + 1}</td>
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
                            <p className="font-medium text-content-main">{c.customerName || 'Chưa có tên'}</p>
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
                            ? <span className="inline-flex items-center px-2 py-1 rounded-md bg-red-500/10 text-red-400 text-xs font-medium">Đã khóa</span>
                            : <span className="inline-flex items-center px-2 py-1 rounded-md bg-green-500/10 text-green-400 text-xs font-medium">Hoạt động</span>
                          }
                        </td>
                        <td className="px-5 py-3 text-center">
                          <div className="flex justify-center gap-2">
                            <button
                              onClick={() => openDetail(c)}
                              className="p-2 rounded-lg bg-blue-500/10 text-blue-400 hover:bg-blue-500/20 transition-colors"
                              title="Xem chi tiết"
                            >
                              <RiEyeLine size={16} />
                            </button>
                            <button
                              onClick={() => handleToggleAccount(c.customerId)}
                              className={cn('p-2 rounded-lg transition-colors',
                                isLocked
                                  ? 'bg-green-500/10 text-green-400 hover:bg-green-500/20'
                                  : 'bg-red-500/10 text-red-400 hover:bg-red-500/20'
                              )}
                              title={isLocked ? 'Mở khóa tài khoản' : 'Khóa tài khoản'}
                            >
                              {isLocked ? <RiLockUnlockLine size={16} /> : <RiLock2Line size={16} />}
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
              <div className="flex items-center justify-between px-5 py-4 border-t border-surface-border">
                <p className="text-xs text-content-muted">
                  Trang {pagination.page + 1} / {pagination.totalPages}
                </p>
                <div className="flex gap-2">
                  <button
                    onClick={() => fetchCustomers(pagination.page - 1)}
                    disabled={pagination.page === 0}
                    className="px-3 py-1.5 rounded-lg border border-surface-border text-sm text-content-muted hover:bg-surface-border/30 disabled:opacity-40 transition-colors"
                  >
                    Trước
                  </button>
                  <button
                    onClick={() => fetchCustomers(pagination.page + 1)}
                    disabled={pagination.page >= pagination.totalPages - 1}
                    className="px-3 py-1.5 rounded-lg border border-surface-border text-sm text-content-muted hover:bg-surface-border/30 disabled:opacity-40 transition-colors"
                  >
                    Sau
                  </button>
                </div>
              </div>
            )}
          </>
        )}
      </div>

      {/* Detail Modal */}
      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title="Chi tiết Khách hàng">
        {selectedCustomer && (
          <div className="space-y-5">
            {/* Avatar + Tên */}
            <div className="flex items-center gap-4">
              <div className="w-16 h-16 rounded-full overflow-hidden bg-surface-dark border border-surface-border shrink-0">
                {selectedCustomer.avatar
                  ? <img src={selectedCustomer.avatar} alt={selectedCustomer.customerName} className="w-full h-full object-cover" />
                  : <div className="w-full h-full flex items-center justify-center">
                      <RiUserLine size={30} className="text-content-muted" />
                    </div>
                }
              </div>
              <div>
                <p className="text-xl font-display font-bold text-content-main">{selectedCustomer.customerName || 'Chưa có tên'}</p>
                <span className={cn('inline-flex items-center px-2 py-0.5 rounded text-xs font-medium mt-1',
                  selectedCustomer.account?.accountStatus === false
                    ? 'bg-red-500/10 text-red-400'
                    : 'bg-green-500/10 text-green-400'
                )}>
                  {selectedCustomer.account?.accountStatus === false ? 'Đã khóa' : 'Đang hoạt động'}
                </span>
              </div>
            </div>

            {/* Thông tin chi tiết */}
            <div className="grid grid-cols-2 gap-3 text-sm">
              {[
                { label: 'Mã KH', value: selectedCustomer.customerId, mono: true },
                { label: 'Số điện thoại', value: selectedCustomer.phone },
                { label: 'Email', value: selectedCustomer.email || 'Chưa cập nhật' },
                { label: 'Giới tính', value: selectedCustomer.gender === 'MALE' ? 'Nam' : selectedCustomer.gender === 'FEMALE' ? 'Nữ' : 'Chưa cập nhật' },
                { label: 'Ngày sinh', value: selectedCustomer.birthDate || 'Chưa cập nhật' },
                { label: 'Địa chỉ', value: selectedCustomer.address || 'Chưa cập nhật' },
              ].map(({ label, value, mono }) => (
                <div key={label} className="p-3 bg-surface-dark/50 border border-surface-border rounded-xl">
                  <p className="text-xs text-content-muted mb-1">{label}</p>
                  <p className={cn('font-medium text-content-main break-all', mono && 'font-mono text-xs')}>{value}</p>
                </div>
              ))}
            </div>

            {/* Action */}
            <div className="flex justify-end pt-2">
              <button
                onClick={() => handleToggleAccount(selectedCustomer.customerId)}
                className={cn('flex items-center gap-2 px-4 py-2 rounded-lg font-medium text-sm transition-colors',
                  selectedCustomer.account?.accountStatus === false
                    ? 'bg-green-500/10 text-green-400 border border-green-500/20 hover:bg-green-500/20'
                    : 'bg-red-500/10 text-red-400 border border-red-500/20 hover:bg-red-500/20'
                )}
              >
                {selectedCustomer.account?.accountStatus === false
                  ? <><RiLockUnlockLine size={16} /> Mở khóa tài khoản</>
                  : <><RiLock2Line size={16} /> Khóa tài khoản</>
                }
              </button>
            </div>
          </div>
        )}
      </Modal>
    </div>
  )
}

export default AdminCustomersPage
