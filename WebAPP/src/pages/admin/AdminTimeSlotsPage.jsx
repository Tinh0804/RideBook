import { useState, useEffect } from 'react'
import { adminApi } from '@/features/admin/api/adminApi'
import Spinner from '@/components/Elements/Spinner'
import { RiAddLine, RiEditLine, RiDeleteBinLine, RiSaveLine, RiCloseLine } from 'react-icons/ri'
import { cn } from '@/utils/cn'

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
  <Modal open={open} onClose={onClose} title="Xác nhận xóa">
    <p className="text-content-muted mb-6">{message}</p>
    <div className="flex justify-end gap-3">
      <button onClick={onClose} className="px-4 py-2 rounded-lg border border-surface-border text-content-muted hover:bg-surface-border/30 text-sm transition-colors">Hủy</button>
      <button onClick={onConfirm} className="px-4 py-2 rounded-lg bg-red-500/20 text-red-400 border border-red-500/30 hover:bg-red-500/30 text-sm font-medium transition-colors">Xóa</button>
    </div>
  </Modal>
)

const AdminTimeSlotsPage = () => {
  const [timeSlots, setTimeSlots] = useState([])
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)

  const [modal, setModal] = useState(false)
  const [form, setForm] = useState(EMPTY_TIMESLOT)
  const [editId, setEditId] = useState(null)
  const [confirmDelete, setConfirmDelete] = useState({ open: false, id: null })

  const fetch = async () => {
    setLoading(true)
    try {
      const res = await adminApi.getAllTimeSlots()
      setTimeSlots(res.result || [])
    } catch (e) {
      console.error(e)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { fetch() }, [])

  const openCreate = () => { setForm(EMPTY_TIMESLOT); setEditId(null); setModal(true) }
  const openEdit = (ts) => {
    setForm({ slotName: ts.slotName, startTime: ts.startTime, endTime: ts.endTime })
    setEditId(ts.timeId)
    setModal(true)
  }

  const handleSave = async () => {
    setSaving(true)
    try {
      if (editId) {
        await adminApi.updateTimeSlot(editId, form)
      } else {
        await adminApi.createTimeSlot(form)
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
      await adminApi.deleteTimeSlot(confirmDelete.id)
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
        <h1 className="section-title">Quản lý Khung giờ</h1>
        <p className="text-content-muted text-sm mt-1">Thiết lập các khung giờ cao điểm và thấp điểm để áp dụng hệ số giá</p>
      </div>

      <div className="card overflow-hidden">
        <div className="flex items-center justify-between px-5 py-4 border-b border-surface-border">
          <div>
            <h2 className="font-display text-base font-bold text-content-main">Danh sách khung giờ ({timeSlots.length})</h2>
            <p className="text-xs text-content-muted mt-0.5">Nhấn ✏️ để chỉnh sửa.</p>
          </div>
          <button
            onClick={openCreate}
            className="flex items-center gap-1.5 px-3 py-2 rounded-xl bg-brand-500 hover:bg-brand-600 text-white text-xs font-semibold transition-colors"
          >
            <RiAddLine size={16} /> Thêm khung giờ
          </button>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-surface-border bg-surface-dark/50">
                {['#', 'Tên khung giờ', 'Giờ bắt đầu', 'Giờ kết thúc', 'Khoảng thời gian', 'Thao tác'].map((h) => (
                  <th key={h} className="text-left px-5 py-3 text-xs font-semibold text-content-muted uppercase tracking-wider">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-surface-border">
              {timeSlots.length === 0 ? (
                <tr>
                  <td colSpan={6} className="text-center text-content-muted py-12">Chưa có khung giờ nào</td>
                </tr>
              ) : timeSlots.map((t, i) => (
                <tr key={t.timeId} className="hover:bg-surface-border/10 transition-colors">
                  <td className="px-5 py-3 text-content-muted font-mono text-xs">{i + 1}</td>
                  <td className="px-5 py-3 font-semibold text-content-main">{t.slotName}</td>
                  <td className="px-5 py-3">
                    <span className="font-mono text-brand-400 bg-brand-500/10 px-2 py-0.5 rounded-lg text-xs">{t.startTime}</span>
                  </td>
                  <td className="px-5 py-3">
                    <span className="font-mono text-brand-400 bg-brand-500/10 px-2 py-0.5 rounded-lg text-xs">{t.endTime}</span>
                  </td>
                  <td className="px-5 py-3 text-content-muted text-xs">
                    {t.startTime} – {t.endTime}
                  </td>
                  <td className="px-5 py-3">
                    <div className="flex items-center gap-2">
                      <button
                        onClick={() => openEdit(t)}
                        className="p-1.5 rounded-lg hover:bg-blue-500/10 text-blue-400 transition-colors"
                        title="Sửa khung giờ"
                      >
                        <RiEditLine size={16} />
                      </button>
                      <button
                        onClick={() => setConfirmDelete({ open: true, id: t.timeId })}
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

      <div className="card p-4 border-l-4 border-yellow-500/50 bg-yellow-500/5">
        <p className="text-xs text-yellow-400 font-medium">💡 Lưu ý</p>
        <p className="text-xs text-content-muted mt-1">
          Khung giờ ảnh hưởng trực tiếp đến hệ số giá trong Bảng giá.
          Hãy đảm bảo các khung giờ không bị trùng lặp.
        </p>
      </div>

      <Modal open={modal} onClose={() => setModal(false)} title="Sửa khung giờ">
        <div className="space-y-4">
          <div>
            <label className="block text-xs font-medium text-content-muted mb-1.5">Tên khung giờ *</label>
            <input
              value={form.slotName}
              onChange={(e) => setForm({ ...form, slotName: e.target.value })}
              className="input-field w-full"
              placeholder="VD: Giờ cao điểm sáng"
            />
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-medium text-content-muted mb-1.5">Giờ bắt đầu</label>
              <input
                type="time"
                value={form.startTime}
                onChange={(e) => setForm({ ...form, startTime: e.target.value })}
                className="input-field w-full"
              />
            </div>
            <div>
              <label className="block text-xs font-medium text-content-muted mb-1.5">Giờ kết thúc</label>
              <input
                type="time"
                value={form.endTime}
                onChange={(e) => setForm({ ...form, endTime: e.target.value })}
                className="input-field w-full"
              />
            </div>
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
              disabled={saving || !form.slotName}
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
        message="Bạn có chắc muốn xóa khung giờ này? Bảng giá liên quan (nếu có) cũng sẽ bị xóa vĩnh viễn."
      />
    </div>
  )
}

export default AdminTimeSlotsPage
