import { useState, useEffect } from 'react'
import { adminApi } from '@/features/admin/api/adminApi'
import Spinner from '@/components/Elements/Spinner'
import {
  RiAddLine, RiEditLine, RiDeleteBinLine, RiCloseLine,
  RiSaveLine, RiCarLine, RiTimeLine, RiPriceTag3Line,
} from 'react-icons/ri'
import { cn } from '@/utils/cn'

const EMPTY_VEHICLE = { vehicleTypeName: '', pricePerKm: 0, maxPassengers: 4, icon: '🚗' }
const EMPTY_TIMESLOT = { slotName: '', startTime: '', endTime: '' }

const Modal = ({ open, onClose, title, children }) => {
  if (!open) return null
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm" onClick={onClose}>
      <div className="card w-full max-w-lg mx-4 p-0 animate-in fade-in zoom-in-95" onClick={(e) => e.stopPropagation()}>
        <div className="flex items-center justify-between px-6 py-4 border-b border-surface-border">
          <h3 className="font-display text-lg font-bold text-content-main">{title}</h3>
          <button onClick={onClose} className="p-1.5 rounded-lg hover:bg-surface-border/40 text-content-muted transition-colors">
            <RiCloseLine size={20} />
          </button>
        </div>
        <div className="p-6">{children}</div>
      </div>
    </div>
  )
}

const ConfirmDialog = ({ open, onClose, onConfirm, message }) => (
  <Modal open={open} onClose={onClose} title="Xác nhận">
    <p className="text-content-muted mb-6">{message}</p>
    <div className="flex justify-end gap-3">
      <button onClick={onClose} className="px-4 py-2 rounded-lg border border-surface-border text-content-muted hover:bg-surface-border/30 text-sm transition-colors">Hủy</button>
      <button onClick={onConfirm} className="px-4 py-2 rounded-lg bg-red-500/20 text-red-400 border border-red-500/30 hover:bg-red-500/30 text-sm font-medium transition-colors">Xóa</button>
    </div>
  </Modal>
)

const TABS = [
  { value: 'vehicle-types', label: 'Loại xe', icon: RiCarLine },
  { value: 'time-slots', label: 'Khung giờ', icon: RiTimeLine },
  { value: 'pricing', label: 'Bảng giá', icon: RiPriceTag3Line },
]

const AdminSettingsPage = () => {
  const [vehicleTypes, setVehicleTypes] = useState([])
  const [timeSlots, setTimeSlots] = useState([])
  const [pricing, setPricing] = useState([])
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [activeTab, setActiveTab] = useState('vehicle-types')

  // Vehicle type modal
  const [vtModal, setVtModal] = useState(false)
  const [vtForm, setVtForm] = useState(EMPTY_VEHICLE)
  const [vtEditId, setVtEditId] = useState(null)

  // Time slot modal
  const [tsModal, setTsModal] = useState(false)
  const [tsForm, setTsForm] = useState(EMPTY_TIMESLOT)
  const [tsEditId, setTsEditId] = useState(null)

  // Confirm delete
  const [confirmDelete, setConfirmDelete] = useState({ open: false, id: null, type: null })

  useEffect(() => { fetchData() }, [])

  const fetchData = async () => {
    setLoading(true)
    try {
      const [v, t, p] = await Promise.all([
        adminApi.getAllVehicleTypes(),
        adminApi.getAllTimeSlots(),
        adminApi.getAllPricing(),
      ])
      setVehicleTypes(v.result || [])
      setTimeSlots(t.result || [])
      setPricing(p.result || [])
    } catch (error) {
      console.error('Error fetching settings:', error)
    } finally {
      setLoading(false)
    }
  }

  // Vehicle Type handlers
  const openCreateVt = () => { setVtForm(EMPTY_VEHICLE); setVtEditId(null); setVtModal(true) }
  const openEditVt = (vt) => { setVtForm({ vehicleTypeName: vt.vehicleTypeName, pricePerKm: vt.pricePerKm, maxPassengers: vt.maxPassengers, icon: vt.icon || '🚗' }); setVtEditId(vt.vehicleTypeId); setVtModal(true) }
  const handleSaveVt = async () => {
    setSaving(true)
    try {
      if (vtEditId) {
        await adminApi.updateVehicleType(vtEditId, vtForm)
      } else {
        await adminApi.createVehicleType(vtForm)
      }
      setVtModal(false)
      await fetchData()
    } catch (e) { console.error(e) }
    finally { setSaving(false) }
  }

  // Time Slot handlers
  const openEditTs = (ts) => { setTsForm({ slotName: ts.slotName, startTime: ts.startTime, endTime: ts.endTime }); setTsEditId(ts.timeId); setTsModal(true) }
  const handleSaveTs = async () => {
    setSaving(true)
    try {
      await adminApi.updateTimeSlot(tsEditId, tsForm)
      setTsModal(false)
      await fetchData()
    } catch (e) { console.error(e) }
    finally { setSaving(false) }
  }

  // Pricing handler
  const handleUpdatePricing = async (id, data) => {
    try {
      await adminApi.updatePricing(id, data)
      await fetchData()
    } catch (e) { console.error(e) }
  }

  // Delete handler
  const handleDelete = async () => {
    const { id, type } = confirmDelete
    try {
      if (type === 'vehicle') await adminApi.deleteVehicleType(id)
      setConfirmDelete({ open: false, id: null, type: null })
      await fetchData()
    } catch (e) { console.error(e) }
  }

  if (loading) return <div className="flex justify-center py-16"><Spinner size="xl" /></div>

  return (
    <div className="space-y-6">
      <div>
        <h1 className="section-title">Cài đặt hệ thống</h1>
        <p className="text-content-muted text-sm mt-1">Quản lý loại xe, khung giờ và bảng giá</p>
      </div>

      {/* Tabs */}
      <div className="flex gap-1 p-1 bg-surface-dark border border-surface-border rounded-xl w-fit flex-wrap">
        {TABS.map((tab) => (
          <button key={tab.value} onClick={() => setActiveTab(tab.value)}
            className={cn(
              'flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-medium transition-all',
              activeTab === tab.value ? 'bg-brand-500 text-content-main' : 'text-content-muted hover:text-content-main'
            )}
          >
            <tab.icon size={16} />
            {tab.label}
          </button>
        ))}
      </div>

      {/* ========== LOẠI XE ========== */}
      {activeTab === 'vehicle-types' && (
        <div className="card overflow-hidden">
          <div className="flex items-center justify-between px-5 py-4 border-b border-surface-border">
            <h2 className="font-display text-base font-bold text-content-main">Danh sách loại xe ({vehicleTypes.length})</h2>
            <button onClick={openCreateVt}
              className="flex items-center gap-1.5 px-3 py-2 rounded-lg bg-brand-500 hover:bg-brand-600 text-white text-xs font-medium transition-colors">
              <RiAddLine size={16} /> Thêm loại xe
            </button>
          </div>
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-surface-border bg-surface-dark/50">
                  {['#', 'Icon', 'Tên loại xe', 'Giá/Km (VNĐ)', 'Số chỗ', 'Thao tác'].map((h) => (
                    <th key={h} className="text-left px-5 py-3 text-xs font-semibold text-content-muted uppercase tracking-wider">{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-surface-border">
                {vehicleTypes.map((v, i) => (
                  <tr key={v.vehicleTypeId} className="hover:bg-surface-border/10 transition-colors">
                    <td className="px-5 py-3 text-content-muted font-mono text-xs">{i + 1}</td>
                    <td className="px-5 py-3 text-2xl">{v.icon || '🚗'}</td>
                    <td className="px-5 py-3 font-medium text-content-main">{v.vehicleTypeName}</td>
                    <td className="px-5 py-3 text-brand-400 font-semibold">{v.pricePerKm?.toLocaleString()}</td>
                    <td className="px-5 py-3 text-content-muted">{v.maxPassengers}</td>
                    <td className="px-5 py-3">
                      <div className="flex items-center gap-2">
                        <button onClick={() => openEditVt(v)}
                          className="p-1.5 rounded-lg hover:bg-blue-500/10 text-blue-400 transition-colors" title="Sửa">
                          <RiEditLine size={16} />
                        </button>
                        <button onClick={() => setConfirmDelete({ open: true, id: v.vehicleTypeId, type: 'vehicle' })}
                          className="p-1.5 rounded-lg hover:bg-red-500/10 text-red-400 transition-colors" title="Xóa">
                          <RiDeleteBinLine size={16} />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            {vehicleTypes.length === 0 && (
              <p className="text-center text-content-muted py-8">Chưa có loại xe nào</p>
            )}
          </div>
        </div>
      )}

      {/* ========== KHUNG GIỜ ========== */}
      {activeTab === 'time-slots' && (
        <div className="card overflow-hidden">
          <div className="px-5 py-4 border-b border-surface-border">
            <h2 className="font-display text-base font-bold text-content-main">Danh sách khung giờ ({timeSlots.length})</h2>
          </div>
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-surface-border bg-surface-dark/50">
                  {['#', 'Tên khung giờ', 'Bắt đầu', 'Kết thúc', 'Thao tác'].map((h) => (
                    <th key={h} className="text-left px-5 py-3 text-xs font-semibold text-content-muted uppercase tracking-wider">{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-surface-border">
                {timeSlots.map((t, i) => (
                  <tr key={t.timeId} className="hover:bg-surface-border/10 transition-colors">
                    <td className="px-5 py-3 text-content-muted font-mono text-xs">{i + 1}</td>
                    <td className="px-5 py-3 font-medium text-content-main">{t.slotName}</td>
                    <td className="px-5 py-3 text-content-muted font-mono">{t.startTime}</td>
                    <td className="px-5 py-3 text-content-muted font-mono">{t.endTime}</td>
                    <td className="px-5 py-3">
                      <button onClick={() => openEditTs(t)}
                        className="p-1.5 rounded-lg hover:bg-blue-500/10 text-blue-400 transition-colors" title="Sửa">
                        <RiEditLine size={16} />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            {timeSlots.length === 0 && (
              <p className="text-center text-content-muted py-8">Chưa có khung giờ nào</p>
            )}
          </div>
        </div>
      )}

      {/* ========== BẢNG GIÁ ========== */}
      {activeTab === 'pricing' && (
        <div className="card overflow-hidden">
          <div className="px-5 py-4 border-b border-surface-border">
            <h2 className="font-display text-base font-bold text-content-main">Ma trận hệ số giá</h2>
            <p className="text-content-muted text-xs mt-1">Giá thực = Giá cơ bản (Loại xe) × Hệ số khung giờ. Click vào hệ số để chỉnh sửa.</p>
          </div>
          <div className="overflow-x-auto p-5">
            <table className="w-full text-sm border-collapse">
              <thead>
                <tr>
                  <th className="border border-surface-border p-3 text-left bg-surface-dark font-semibold text-content-muted text-xs uppercase">Loại xe \ Khung giờ</th>
                  {timeSlots.map(t => (
                    <th key={t.timeId} className="border border-surface-border p-3 text-center bg-surface-dark">
                      <span className="text-xs font-semibold text-content-muted uppercase">{t.slotName}</span>
                      <br />
                      <span className="text-[10px] text-content-muted/60">{t.startTime} - {t.endTime}</span>
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {vehicleTypes.map(v => (
                  <tr key={v.vehicleTypeId} className="hover:bg-surface-border/10 transition-colors">
                    <td className="border border-surface-border p-3">
                      <div className="flex items-center gap-2">
                        <span className="text-xl">{v.icon || '🚗'}</span>
                        <div>
                          <p className="font-medium text-content-main text-xs">{v.vehicleTypeName}</p>
                          <p className="text-[10px] text-content-muted">{v.pricePerKm?.toLocaleString()} đ/km</p>
                        </div>
                      </div>
                    </td>
                    {timeSlots.map(t => {
                      const priceItem = pricing.find(p => p.vehicleType?.vehicleTypeId === v.vehicleTypeId && p.time?.timeId === t.timeId)
                      return (
                        <td key={t.timeId} className="border border-surface-border p-3 text-center">
                          <input
                            type="number" step="0.1" min="0.1"
                            defaultValue={priceItem?.surcharge || 1.0}
                            onBlur={(e) => {
                              const val = parseFloat(e.target.value)
                              if (priceItem && val > 0 && val !== priceItem.surcharge) {
                                handleUpdatePricing(priceItem.id, { ...priceItem, surcharge: val })
                              }
                            }}
                            className="w-20 py-1.5 bg-transparent border border-surface-border rounded-lg text-center text-content-main font-mono text-sm focus:outline-none focus:border-brand-500 focus:ring-1 focus:ring-brand-500/30 transition-all"
                          />
                        </td>
                      )
                    })}
                  </tr>
                ))}
              </tbody>
            </table>
            {vehicleTypes.length === 0 && (
              <p className="text-center text-content-muted py-8">Chưa có dữ liệu bảng giá</p>
            )}
          </div>
        </div>
      )}

      {/* ========== MODAL: LOẠI XE ========== */}
      <Modal open={vtModal} onClose={() => setVtModal(false)} title={vtEditId ? 'Sửa loại xe' : 'Thêm loại xe mới'}>
        <div className="space-y-4">
          <div>
            <label className="block text-xs font-medium text-content-muted mb-1.5">Tên loại xe</label>
            <input value={vtForm.vehicleTypeName} onChange={(e) => setVtForm({ ...vtForm, vehicleTypeName: e.target.value })}
              className="input-field w-full" placeholder="VD: Xe 4 chỗ" />
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-medium text-content-muted mb-1.5">Giá/Km (VNĐ)</label>
              <input type="number" value={vtForm.pricePerKm} onChange={(e) => setVtForm({ ...vtForm, pricePerKm: parseFloat(e.target.value) || 0 })}
                className="input-field w-full" />
            </div>
            <div>
              <label className="block text-xs font-medium text-content-muted mb-1.5">Số chỗ ngồi</label>
              <input type="number" value={vtForm.maxPassengers} onChange={(e) => setVtForm({ ...vtForm, maxPassengers: parseInt(e.target.value) || 1 })}
                className="input-field w-full" />
            </div>
          </div>
          <div>
            <label className="block text-xs font-medium text-content-muted mb-1.5">Icon (Emoji)</label>
            <input value={vtForm.icon} onChange={(e) => setVtForm({ ...vtForm, icon: e.target.value })}
              className="input-field w-full text-2xl text-center" maxLength={4} />
          </div>
          <div className="flex justify-end gap-3 pt-2">
            <button onClick={() => setVtModal(false)}
              className="px-4 py-2 rounded-lg border border-surface-border text-content-muted hover:bg-surface-border/30 text-sm transition-colors">Hủy</button>
            <button onClick={handleSaveVt} disabled={saving || !vtForm.vehicleTypeName}
              className="flex items-center gap-1.5 px-4 py-2 rounded-lg bg-brand-500 hover:bg-brand-600 disabled:opacity-50 text-white text-sm font-medium transition-colors">
              <RiSaveLine size={16} /> {saving ? 'Đang lưu...' : 'Lưu'}
            </button>
          </div>
        </div>
      </Modal>

      {/* ========== MODAL: KHUNG GIỜ ========== */}
      <Modal open={tsModal} onClose={() => setTsModal(false)} title="Sửa khung giờ">
        <div className="space-y-4">
          <div>
            <label className="block text-xs font-medium text-content-muted mb-1.5">Tên khung giờ</label>
            <input value={tsForm.slotName} onChange={(e) => setTsForm({ ...tsForm, slotName: e.target.value })}
              className="input-field w-full" />
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-medium text-content-muted mb-1.5">Giờ bắt đầu</label>
              <input type="time" value={tsForm.startTime} onChange={(e) => setTsForm({ ...tsForm, startTime: e.target.value })}
                className="input-field w-full" />
            </div>
            <div>
              <label className="block text-xs font-medium text-content-muted mb-1.5">Giờ kết thúc</label>
              <input type="time" value={tsForm.endTime} onChange={(e) => setTsForm({ ...tsForm, endTime: e.target.value })}
                className="input-field w-full" />
            </div>
          </div>
          <div className="flex justify-end gap-3 pt-2">
            <button onClick={() => setTsModal(false)}
              className="px-4 py-2 rounded-lg border border-surface-border text-content-muted hover:bg-surface-border/30 text-sm transition-colors">Hủy</button>
            <button onClick={handleSaveTs} disabled={saving || !tsForm.slotName}
              className="flex items-center gap-1.5 px-4 py-2 rounded-lg bg-brand-500 hover:bg-brand-600 disabled:opacity-50 text-white text-sm font-medium transition-colors">
              <RiSaveLine size={16} /> {saving ? 'Đang lưu...' : 'Lưu'}
            </button>
          </div>
        </div>
      </Modal>

      {/* ========== CONFIRM DELETE ========== */}
      <ConfirmDialog
        open={confirmDelete.open}
        onClose={() => setConfirmDelete({ open: false, id: null, type: null })}
        onConfirm={handleDelete}
        message="Bạn có chắc muốn xóa? Thao tác này không thể hoàn tác."
      />
    </div>
  )
}

export default AdminSettingsPage
