import { useState, useEffect } from 'react'
import { adminApi } from '@/features/admin/api/adminApi'
import Spinner from '@/components/Elements/Spinner'
import { RiAddLine, RiEditLine, RiDeleteBinLine, RiSaveLine, RiCloseLine } from 'react-icons/ri'
import { cn } from '@/utils/cn'

const EMPTY_VEHICLE = { vehicleTypeName: '', pricePerKm: 0, maxPassengers: 4, icon: '' }

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
  <Modal open={open} onClose={onClose} title="Xác nhận xóa">
    <p className="text-content-muted mb-6">{message}</p>
    <div className="flex justify-end gap-3">
      <button onClick={onClose} className="px-4 py-2 rounded-lg border border-surface-border text-content-muted hover:bg-surface-border/30 text-sm transition-colors">Hủy</button>
      <button onClick={onConfirm} className="px-4 py-2 rounded-lg bg-red-500/20 text-red-400 border border-red-500/30 hover:bg-red-500/30 text-sm font-medium transition-colors">Xóa</button>
    </div>
  </Modal>
)

const AdminVehicleTypesPage = () => {
  const [vehicleTypes, setVehicleTypes] = useState([])
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)

  const [modal, setModal] = useState(false)
  const [form, setForm] = useState(EMPTY_VEHICLE)
  const [editId, setEditId] = useState(null)
  const [confirmDelete, setConfirmDelete] = useState({ open: false, id: null })

  const fetch = async () => {
    setLoading(true)
    try {
      const res = await adminApi.getAllVehicleTypes()
      setVehicleTypes(res.result || [])
    } catch (e) {
      console.error(e)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { fetch() }, [])

  const openCreate = () => { setForm(EMPTY_VEHICLE); setEditId(null); setModal(true) }
  const openEdit = (v) => {
    setForm({ vehicleTypeName: v.vehicleTypeName, pricePerKm: v.pricePerKm, maxPassengers: v.maxPassengers, icon: v.icon || '' })
    setEditId(v.vehicleTypeId)
    setModal(true)
  }

  const handleSave = async () => {
    setSaving(true)
    try {
      if (editId) {
        await adminApi.updateVehicleType(editId, form)
      } else {
        await adminApi.createVehicleType(form)
      }
      setModal(false)
      await fetch()
    } catch (e) {
      console.error(e)
    } finally {
      setSaving(false)
    }
  }

  const handleDelete = async () => {
    try {
      await adminApi.deleteVehicleType(confirmDelete.id)
      setConfirmDelete({ open: false, id: null })
      await fetch()
    } catch (e) {
      console.error(e)
    }
  }

  if (loading) return <div className="flex justify-center py-16"><Spinner size="xl" /></div>

  return (
    <div className="space-y-6">
      <div>
        <h1 className="section-title">Quản lý Loại xe</h1>
        <p className="text-content-muted text-sm mt-1">Quản lý các loại phương tiện và giá cơ bản</p>
      </div>

      <div className="card overflow-hidden">
        <div className="flex items-center justify-between px-5 py-4 border-b border-surface-border">
          <h2 className="font-display text-base font-bold text-content-main">Loại xe ({vehicleTypes.length})</h2>
          <button
            onClick={openCreate}
            className="flex items-center gap-1.5 px-3 py-2 rounded-xl bg-brand-500 hover:bg-brand-600 text-white text-xs font-semibold transition-colors"
          >
            <RiAddLine size={16} /> Thêm loại xe
          </button>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-surface-border bg-surface-dark/50">
                {['#', 'Ảnh / Icon', 'Tên loại xe', 'Giá/Km (VNĐ)', 'Số chỗ', 'Thao tác'].map((h) => (
                  <th key={h} className="text-left px-5 py-3 text-xs font-semibold text-content-muted uppercase tracking-wider">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-surface-border">
              {vehicleTypes.length === 0 ? (
                <tr>
                  <td colSpan={6} className="text-center text-content-muted py-12">Chưa có loại xe nào</td>
                </tr>
              ) : vehicleTypes.map((v, i) => (
                <tr key={v.vehicleTypeId} className="hover:bg-surface-border/10 transition-colors">
                  <td className="px-5 py-3 text-content-muted font-mono text-xs">{i + 1}</td>
                  <td className="px-5 py-3">
                    {v.icon ? (
                      <img src={v.icon} alt={v.vehicleTypeName} className="w-10 h-10 object-cover rounded-lg border border-surface-border" />
                    ) : (
                      <div className="w-10 h-10 rounded-lg bg-surface-dark border border-surface-border flex items-center justify-center text-xl">🚗</div>
                    )}
                  </td>
                  <td className="px-5 py-3 font-medium text-content-main">{v.vehicleTypeName}</td>
                  <td className="px-5 py-3 text-brand-400 font-semibold font-mono">{v.pricePerKm?.toLocaleString()} đ</td>
                  <td className="px-5 py-3 text-content-muted">{v.maxPassengers} chỗ</td>
                  <td className="px-5 py-3">
                    <div className="flex items-center gap-2">
                      <button
                        onClick={() => openEdit(v)}
                        className="p-1.5 rounded-lg hover:bg-blue-500/10 text-blue-400 transition-colors"
                        title="Sửa"
                      >
                        <RiEditLine size={16} />
                      </button>
                      <button
                        onClick={() => setConfirmDelete({ open: true, id: v.vehicleTypeId })}
                        className="p-1.5 rounded-lg hover:bg-red-500/10 text-red-400 transition-colors"
                        title="Xóa"
                      >
                        <RiDeleteBinLine size={16} />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      <Modal open={modal} onClose={() => setModal(false)} title={editId ? 'Sửa loại xe' : 'Thêm loại xe mới'}>
        <div className="space-y-4">
          <div>
            <label className="block text-xs font-medium text-content-muted mb-1.5">Tên loại xe *</label>
            <input
              value={form.vehicleTypeName}
              onChange={(e) => setForm({ ...form, vehicleTypeName: e.target.value })}
              className="input-field w-full"
              placeholder="VD: Xe 4 chỗ"
            />
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-medium text-content-muted mb-1.5">Giá/Km (VNĐ)</label>
              <input
                type="number"
                value={form.pricePerKm}
                onChange={(e) => setForm({ ...form, pricePerKm: parseFloat(e.target.value) || 0 })}
                className="input-field w-full"
              />
            </div>
            <div>
              <label className="block text-xs font-medium text-content-muted mb-1.5">Số chỗ ngồi</label>
              <input
                type="number"
                value={form.maxPassengers}
                onChange={(e) => setForm({ ...form, maxPassengers: parseInt(e.target.value) || 1 })}
                className="input-field w-full"
              />
            </div>
          </div>
          <div>
            <label className="block text-xs font-medium text-content-muted mb-1.5">URL Ảnh icon</label>
            <input
              value={form.icon}
              onChange={(e) => setForm({ ...form, icon: e.target.value })}
              className="input-field w-full"
              placeholder="https://... hoặc emoji 🚗"
            />
            {form.icon && (
              <div className="mt-2 flex items-center gap-2">
                <img src={form.icon} alt="preview" className="w-10 h-10 object-cover rounded-lg border border-surface-border" onError={(e) => e.target.style.display = 'none'} />
                <span className="text-xs text-content-muted">Preview</span>
              </div>
            )}
          </div>
          <div className="flex justify-end gap-3 pt-2">
            <button
              onClick={() => setModal(false)}
              className="px-4 py-2 rounded-xl border border-surface-border text-content-muted hover:bg-surface-border/30 text-sm transition-colors"
            >
              Hủy
            </button>
            <button
              onClick={handleSave}
              disabled={saving || !form.vehicleTypeName}
              className="flex items-center gap-1.5 px-4 py-2 rounded-xl bg-brand-500 hover:bg-brand-600 disabled:opacity-50 text-white text-sm font-semibold transition-colors"
            >
              <RiSaveLine size={16} /> {saving ? 'Đang lưu...' : 'Lưu'}
            </button>
          </div>
        </div>
      </Modal>

      <ConfirmDialog
        open={confirmDelete.open}
        onClose={() => setConfirmDelete({ open: false, id: null })}
        onConfirm={handleDelete}
        message="Bạn có chắc muốn xóa loại xe này? Thao tác này không thể hoàn tác."
      />
    </div>
  )
}

export default AdminVehicleTypesPage
