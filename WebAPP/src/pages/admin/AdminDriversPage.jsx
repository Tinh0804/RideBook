import { useState, useEffect } from 'react'
import { driverApi } from '@/features/driver/api/driverApi'
import { adminApi } from '@/features/admin/api/adminApi'
import Spinner from '@/components/Elements/Spinner'
import { 
  RiLockUnlockLine, RiLock2Line, RiMapPinLine, RiSearchLine, 
  RiEyeLine, RiCloseLine, RiCarLine, RiWallet3Line, RiUserLine 
} from 'react-icons/ri'
import { cn } from '@/utils/cn'
import { WalletStatus, TransactionType } from '@/constants/enums'

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
  const [search, setSearch] = useState('')

  // Detail Modal State
  const [modalOpen, setModalOpen] = useState(false)
  const [selectedDriver, setSelectedDriver] = useState(null)
  const [activeTab, setActiveTab] = useState('info') // info, vehicle, wallet

  // Wallet State
  const [wallet, setWallet] = useState(null)
  const [transactions, setTransactions] = useState([])
  const [adjustForm, setAdjustForm] = useState({ amount: '', reason: '', type: 'ADD' })
  const [adjusting, setAdjusting] = useState(false)

  // Vehicle Form State
  const [vehicleForm, setVehicleForm] = useState({ licensePlate: '', vehicleTypeId: '' })
  const [updatingVehicle, setUpdatingVehicle] = useState(false)

  useEffect(() => {
    fetchDrivers()
    fetchVehicleTypes()
  }, [])

  const fetchDrivers = async () => {
    setLoading(true)
    try {
      const response = await driverApi.getAll(0, 1000)
      // driverApi.getAll already unwraps r.data?.result, so response IS the Page object
      setDrivers(response?.content || (Array.isArray(response) ? response : []))
    } catch (error) {
      console.error('Error fetching drivers:', error)
    } finally {
      setLoading(false)
    }
  }

  const fetchVehicleTypes = async () => {
    try {
      const res = await adminApi.getAllVehicleTypes()
      setVehicleTypes(res.result || [])
    } catch (e) {
      console.error(e)
    }
  }

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
      await fetchWalletData() // Refresh
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

  const filteredDrivers = drivers.filter(d => 
    d.driverName?.toLowerCase().includes(search.toLowerCase()) || 
    d.phone?.includes(search) ||
    d.email?.toLowerCase().includes(search.toLowerCase())
  )

  if (loading) return <div className="flex justify-center py-16"><Spinner size="xl" /></div>

  return (
    <div className="space-y-6">
      <div>
        <h1 className="section-title">Quản lý Tài xế</h1>
        <p className="text-content-muted text-sm mt-1">Danh sách tất cả tài xế và chức năng quản lý chi tiết</p>
      </div>

      <div className="card p-4 flex items-center gap-3">
        <RiSearchLine className="text-content-muted" size={20} />
        <input 
          type="text" 
          placeholder="Tìm kiếm theo tên, sđt, email..." 
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="bg-transparent border-none outline-none flex-1 text-sm text-content-main placeholder:text-content-muted"
        />
      </div>

      <div className="card overflow-x-auto">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-surface-border bg-surface-dark/50">
              <th className="text-left px-5 py-3 text-xs font-semibold text-content-muted uppercase tracking-wider">Tài xế</th>
              <th className="text-left px-5 py-3 text-xs font-semibold text-content-muted uppercase tracking-wider">Liên hệ</th>
              <th className="text-left px-5 py-3 text-xs font-semibold text-content-muted uppercase tracking-wider">Xe</th>
              <th className="text-left px-5 py-3 text-xs font-semibold text-content-muted uppercase tracking-wider">Khu vực</th>
              <th className="text-center px-5 py-3 text-xs font-semibold text-content-muted uppercase tracking-wider">Trạng thái</th>
              <th className="text-center px-5 py-3 text-xs font-semibold text-content-muted uppercase tracking-wider">Thao tác</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-surface-border">
            {filteredDrivers.map(d => {
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
                    {isLocked ? (
                      <span className="inline-flex items-center px-2 py-1 rounded-md bg-red-500/10 text-red-500 text-xs font-medium">Đã khóa</span>
                    ) : (
                      <span className="inline-flex items-center px-2 py-1 rounded-md bg-green-500/10 text-green-500 text-xs font-medium">Hoạt động</span>
                    )}
                  </td>
                  <td className="px-5 py-3 text-center">
                    <div className="flex justify-center gap-2">
                      <button 
                        onClick={() => openDetail(d)}
                        className="p-2 rounded-lg bg-blue-500/10 text-blue-500 hover:bg-blue-500/20 transition-colors"
                        title="Xem chi tiết"
                      >
                        <RiEyeLine size={18} />
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
                    </div>
                  </td>
                </tr>
              )
            })}
          </tbody>
        </table>
        {filteredDrivers.length === 0 && (
          <p className="text-center text-content-muted py-8">Không tìm thấy tài xế nào</p>
        )}
      </div>

      {/* DETAIL MODAL */}
      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title={`Chi tiết Tài xế: ${selectedDriver?.driverName}`}>
        {selectedDriver && (
          <div>
            {/* Tabs Header */}
            <div className="flex gap-1 p-1 bg-surface-dark border border-surface-border rounded-xl w-fit flex-wrap mb-6">
              {[
                { id: 'info', label: 'Thông tin chung', icon: RiUserLine },
                { id: 'vehicle', label: 'Phương tiện', icon: RiCarLine },
                { id: 'wallet', label: 'Ví & Lịch sử', icon: RiWallet3Line },
              ].map((tab) => (
                <button key={tab.id} onClick={() => setActiveTab(tab.id)}
                  className={cn(
                    'flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-medium transition-all',
                    activeTab === tab.id ? 'bg-brand-500 text-content-main' : 'text-content-muted hover:text-content-main'
                  )}
                >
                  <tab.icon size={16} />
                  {tab.label}
                </button>
              ))}
            </div>

            {/* TAB: THÔNG TIN CHUNG */}
            {activeTab === 'info' && (
              <div className="space-y-6 animate-in fade-in">
                <div className="grid grid-cols-2 gap-4 text-sm">
                  <div className="p-4 bg-surface-dark/50 border border-surface-border rounded-xl">
                    <p className="text-content-muted mb-1">Mã tài xế</p>
                    <p className="font-mono">{selectedDriver.driverId}</p>
                  </div>
                  <div className="p-4 bg-surface-dark/50 border border-surface-border rounded-xl">
                    <p className="text-content-muted mb-1">CCCD / CMND</p>
                    <p className="font-medium">{selectedDriver.citizenId || 'N/A'}</p>
                  </div>
                  <div className="p-4 bg-surface-dark/50 border border-surface-border rounded-xl">
                    <p className="text-content-muted mb-1">Số điện thoại</p>
                    <p className="font-medium">{selectedDriver.phone}</p>
                  </div>
                  <div className="p-4 bg-surface-dark/50 border border-surface-border rounded-xl">
                    <p className="text-content-muted mb-1">Email</p>
                    <p className="font-medium">{selectedDriver.email || 'N/A'}</p>
                  </div>
                  <div className="p-4 bg-surface-dark/50 border border-surface-border rounded-xl">
                    <p className="text-content-muted mb-1">Địa chỉ</p>
                    <p className="font-medium">{selectedDriver.address || 'N/A'}</p>
                  </div>
                  <div className="p-4 bg-surface-dark/50 border border-surface-border rounded-xl">
                    <p className="text-content-muted mb-1">Giới tính</p>
                    <p className="font-medium">{selectedDriver.gender === 'MALE' ? 'Nam' : selectedDriver.gender === 'FEMALE' ? 'Nữ' : 'Khác'}</p>
                  </div>
                </div>

                {/* Các ảnh giấy tờ (Avatar, GPLX, Lý lịch tư pháp nếu có) */}
                <div className="grid grid-cols-2 gap-4">
                  {selectedDriver.drivingLicense && (
                    <div>
                      <p className="text-sm font-medium mb-2 text-content-muted">Giấy phép lái xe</p>
                      <img src={selectedDriver.drivingLicense} alt="GPLX" className="w-full h-40 object-cover rounded-lg border border-surface-border" />
                    </div>
                  )}
                  {selectedDriver.criminalRecord && (
                    <div>
                      <p className="text-sm font-medium mb-2 text-content-muted">Lý lịch tư pháp</p>
                      <img src={selectedDriver.criminalRecord} alt="LLTP" className="w-full h-40 object-cover rounded-lg border border-surface-border" />
                    </div>
                  )}
                </div>
              </div>
            )}

            {/* TAB: PHƯƠNG TIỆN */}
            {activeTab === 'vehicle' && (
              <div className="space-y-6 animate-in fade-in">
                <div className="card p-6 bg-surface-dark/30">
                  <h4 className="font-medium text-content-main mb-4">Cập nhật phương tiện</h4>
                  <form onSubmit={handleUpdateVehicle} className="space-y-4">
                    <div>
                      <label className="block text-sm font-medium text-content-muted mb-1">Biển số xe</label>
                      <input 
                        type="text" 
                        value={vehicleForm.licensePlate} 
                        onChange={e => setVehicleForm({...vehicleForm, licensePlate: e.target.value})}
                        className="input-field w-full max-w-sm uppercase font-mono"
                        required
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-content-muted mb-1">Loại xe</label>
                      <select 
                        value={vehicleForm.vehicleTypeId} 
                        onChange={e => setVehicleForm({...vehicleForm, vehicleTypeId: e.target.value})}
                        className="input-field w-full max-w-sm bg-surface-dark"
                        required
                      >
                        <option value="">-- Chọn loại xe --</option>
                        {vehicleTypes.map(v => (
                          <option key={v.vehicleTypeId} value={v.vehicleTypeId}>
                           
                             {v.vehicleTypeName}
                          </option>
                        ))}
                      </select>
                    </div>
                    <div className="pt-2">
                      <button 
                        type="submit" 
                        disabled={updatingVehicle || !vehicleForm.licensePlate || !vehicleForm.vehicleTypeId}
                        className="px-4 py-2 bg-brand-500 hover:bg-brand-600 text-white rounded-lg text-sm font-medium transition-colors disabled:opacity-50"
                      >
                        {updatingVehicle ? 'Đang lưu...' : 'Lưu thay đổi'}
                      </button>
                    </div>
                  </form>
                </div>
              </div>
            )}

            {/* TAB: VÍ & GIAO DỊCH */}
            {activeTab === 'wallet' && (
              <div className="space-y-6 animate-in fade-in">
                <div className="flex items-center justify-between p-6 bg-gradient-to-br from-brand-900/40 to-surface-dark border border-brand-500/20 rounded-xl">
                  <div>
                    <p className="text-content-muted text-sm mb-1">Số dư ví hiện tại</p>
                    <p className="text-3xl font-display font-bold text-brand-400">
                      {wallet ? formatMoney(wallet.balance) : 'Đang tải...'}
                    </p>
                  </div>
                  <div className={`px-4 py-2 rounded-lg font-medium text-sm ${wallet?.status?.toUpperCase() === WalletStatus.ACTIVE ? 'bg-green-500/10 text-green-500' : 'bg-red-500/10 text-red-500'}`}>
                    {wallet?.status?.toUpperCase() === WalletStatus.ACTIVE ? 'Ví hoạt động' : 'Ví bị khóa'}
                  </div>
                </div>

                {/* Form Adjust Balance */}
                <div className="p-4 border border-surface-border rounded-xl">
                  <h4 className="font-medium text-sm mb-3">Điều chỉnh số dư (Admin)</h4>
                  <form onSubmit={handleAdjustBalance} className="flex gap-3 flex-wrap">
                    <select 
                      value={adjustForm.type} 
                      onChange={e => setAdjustForm({...adjustForm, type: e.target.value})}
                      className="input-field bg-surface-dark text-sm w-32"
                    >
                      <option value="ADD">Cộng tiền (+)</option>
                      <option value="SUB">Trừ tiền (-)</option>
                    </select>
                    <input 
                      type="number" 
                      placeholder="Số tiền (VNĐ)" 
                      value={adjustForm.amount}
                      onChange={e => setAdjustForm({...adjustForm, amount: e.target.value})}
                      className="input-field text-sm w-40"
                      min="1"
                      required
                    />
                    <input 
                      type="text" 
                      placeholder="Lý do điều chỉnh..." 
                      value={adjustForm.reason}
                      onChange={e => setAdjustForm({...adjustForm, reason: e.target.value})}
                      className="input-field flex-1 text-sm min-w-[200px]"
                      required
                    />
                    <button 
                      type="submit" 
                      disabled={adjusting || !adjustForm.amount || !adjustForm.reason}
                      className="px-4 py-2 bg-brand-500 hover:bg-brand-600 text-white rounded-lg text-sm font-medium disabled:opacity-50"
                    >
                      {adjusting ? 'Đang xử lý...' : 'Thực hiện'}
                    </button>
                  </form>
                </div>

                {/* Transaction History */}
                <div>
                  <h4 className="font-medium text-sm mb-3 text-content-muted uppercase tracking-wider">Lịch sử giao dịch</h4>
                  <div className="border border-surface-border rounded-xl overflow-hidden">
                    <table className="w-full text-sm">
                      <thead className="bg-surface-dark">
                        <tr className="border-b border-surface-border text-content-muted">
                          <th className="text-left px-4 py-3 font-medium">Thời gian</th>
                          <th className="text-left px-4 py-3 font-medium">Loại</th>
                          <th className="text-left px-4 py-3 font-medium">Số tiền</th>
                          <th className="text-left px-4 py-3 font-medium">Mã chiếu</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-surface-border">
                        {transactions.length === 0 ? (
                          <tr>
                            <td colSpan="4" className="px-4 py-8 text-center text-content-muted">Chưa có giao dịch nào</td>
                          </tr>
                        ) : (
                          transactions.map(t => (
                            <tr key={t.transactionId} className="hover:bg-surface-border/5">
                              <td className="px-4 py-3 text-content-muted">{new Date(t.createdAt).toLocaleString('vi-VN')}</td>
                              <td className="px-4 py-3">
                                {t.type === TransactionType.DEPOSIT ? <span className="text-green-500">Nạp tiền</span> :
                                 t.type === TransactionType.WITHDRAWAL ? <span className="text-red-500">Rút tiền</span> :
                                 t.type === TransactionType.TRIP_INCOME ? <span className="text-blue-500">Thu nhập chuyến</span> :
                                 t.type === TransactionType.TRIP_FEE ? <span className="text-orange-500">Phí nền tảng</span> : 
                                 t.type}
                              </td>
                              <td className="px-4 py-3 font-mono font-medium">
                                {(t.type === TransactionType.WITHDRAWAL || t.type === TransactionType.TRIP_FEE) ? '-' : '+'}{formatMoney(t.amount)}
                              </td>
                              <td className="px-4 py-3 text-xs font-mono text-content-muted">{t.referenceId}</td>
                            </tr>
                          ))
                        )}
                      </tbody>
                    </table>
                  </div>
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
