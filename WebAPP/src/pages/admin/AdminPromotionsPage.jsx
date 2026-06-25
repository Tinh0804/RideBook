import { useState, useEffect, useMemo } from 'react'
import { masterDataApi } from '@/features/booking/api/masterDataApi'
import Spinner from '@/components/Elements/Spinner'
import toast from 'react-hot-toast'
import {
  RiAddLine, RiEditLine, RiDeleteBin6Line, RiCloseLine,
  RiPercentLine, RiMoneyDollarCircleLine, RiSearchLine,
  RiToggleLine, RiToggleFill, RiCalendarLine, RiGroupLine,
  RiCoupon3Line, RiTimeLine, RiCheckLine,
  RiEyeLine, RiEyeOffLine
} from 'react-icons/ri'
import { cn } from '@/utils/cn'

// ─── Helpers ──────────────────────────────────────────────────────────────────
const formatCurrency = (v) =>
  v != null ? v.toLocaleString('vi-VN') + ' đ' : '—'

const formatDate = (ts) => {
  if (!ts) return '—'
  return new Date(ts).toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' })
}

const getDaysLeft = (endTime) => {
  if (!endTime) return null
  const diff = new Date(endTime) - new Date()
  return Math.ceil(diff / (1000 * 60 * 60 * 24))
}

// ─── Modal ────────────────────────────────────────────────────────────────────
const Modal = ({ open, onClose, title, children, maxWidth = 'max-w-xl' }) => {
  if (!open) return null
  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4"
      onClick={onClose}
    >
      <div
        className={cn('card w-full max-h-[92vh] mx-4 flex flex-col p-0 animate-in fade-in zoom-in-95', maxWidth)}
        onClick={(e) => e.stopPropagation()}
      >
        <div className="flex items-center justify-between px-6 py-4 border-b border-surface-border shrink-0">
          <h3 className="font-display text-lg font-bold text-content-main">{title}</h3>
          <button
            onClick={onClose}
            className="p-1.5 rounded-lg hover:bg-surface-border/40 text-content-muted transition-colors"
          >
            <RiCloseLine size={20} />
          </button>
        </div>
        <div className="overflow-y-auto flex-1 p-6">{children}</div>
      </div>
    </div>
  )
}

// ─── Promotion Form ───────────────────────────────────────────────────────────
const EMPTY_FORM = {
  promotionCode: '',
  promotionName: '',
  discountType: 'PERCENTAGE',
  discountValue: '',
  discountLimit: '',
  minTripValue: '',
  quantity: '',
  usageLimitPerUser: '',
  applicationCondition: '',
  startTime: '',
  endTime: '',
  isActive: true,
  isPublic: true,
  promotionImage: '',
}

const PromotionForm = ({ initial = EMPTY_FORM, onSubmit, submitting }) => {
  const [form, setForm] = useState(initial)
  const set = (k, v) => setForm((f) => ({ ...f, [k]: v }))

  const handleSubmit = (e) => {
    e.preventDefault()
    const payload = {
      ...form,
      discountValue: form.discountValue !== '' ? Number(form.discountValue) : undefined,
      discountLimit: form.discountLimit !== '' ? Number(form.discountLimit) : undefined,
      minTripValue: form.minTripValue !== '' ? Number(form.minTripValue) : undefined,
      quantity: form.quantity !== '' ? Number(form.quantity) : undefined,
      usageLimitPerUser: form.usageLimitPerUser !== '' ? Number(form.usageLimitPerUser) : undefined,
      startTime: form.startTime ? new Date(form.startTime).getTime() : undefined,
      endTime: form.endTime ? new Date(form.endTime).getTime() : undefined,
    }
    onSubmit(payload)
  }

  const inputCls = 'w-full px-3 py-2 bg-surface-dark border border-surface-border rounded-xl text-sm text-content-main placeholder:text-content-muted focus:outline-none focus:border-brand-500 transition-colors'
  const labelCls = 'block text-xs font-medium text-content-muted mb-1.5'

  return (
    <form onSubmit={handleSubmit} className="space-y-5">
      {/* Code & Name */}
      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className={labelCls}>Mã khuyến mãi *</label>
          <input
            className={cn(inputCls, 'uppercase font-mono tracking-widest')}
            placeholder="VD: SUMMER50"
            value={form.promotionCode}
            onChange={(e) => set('promotionCode', e.target.value.toUpperCase())}
            required
          />
        </div>
        <div>
          <label className={labelCls}>Tên khuyến mãi *</label>
          <input
            className={inputCls}
            placeholder="VD: Giảm mùa hè"
            value={form.promotionName}
            onChange={(e) => set('promotionName', e.target.value)}
            required
          />
        </div>
      </div>

      {/* Discount type */}
      <div>
        <label className={labelCls}>Loại giảm giá *</label>
        <div className="grid grid-cols-2 gap-3">
          {[
            { value: 'PERCENTAGE', label: 'Giảm theo %', icon: RiPercentLine, color: 'brand' },
            { value: 'FIXED_AMOUNT', label: 'Giảm cố định (đ)', icon: RiMoneyDollarCircleLine, color: 'yellow' },
          ].map(({ value, label, icon: Icon, color }) => (
            <button
              key={value}
              type="button"
              onClick={() => set('discountType', value)}
              className={cn(
                'flex items-center gap-2 p-3 rounded-xl border text-sm font-medium transition-all',
                form.discountType === value
                  ? color === 'brand'
                    ? 'bg-brand-500/15 border-brand-500/50 text-brand-400'
                    : 'bg-yellow-500/15 border-yellow-500/50 text-yellow-400'
                  : 'bg-surface-dark border-surface-border text-content-muted hover:border-surface-border/80'
              )}
            >
              <Icon size={16} />
              {label}
            </button>
          ))}
        </div>
      </div>

      {/* Discount value + limit */}
      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className={labelCls}>
            Giá trị giảm *{form.discountType === 'PERCENTAGE' ? ' (%)' : ' (VND)'}
          </label>
          <input
            type="number"
            className={inputCls}
            placeholder={form.discountType === 'PERCENTAGE' ? '0 - 100' : '50000'}
            min={0}
            max={form.discountType === 'PERCENTAGE' ? 100 : undefined}
            value={form.discountValue}
            onChange={(e) => set('discountValue', e.target.value)}
            required
          />
        </div>
        <div>
          <label className={labelCls}>Giảm tối đa (VND)</label>
          <input
            type="number"
            className={inputCls}
            placeholder="200000"
            min={0}
            value={form.discountLimit}
            onChange={(e) => set('discountLimit', e.target.value)}
          />
        </div>
      </div>

      {/* Min trip + quantity */}
      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className={labelCls}>Giá trị chuyến tối thiểu (VND)</label>
          <input
            type="number"
            className={inputCls}
            placeholder="50000"
            min={0}
            value={form.minTripValue}
            onChange={(e) => set('minTripValue', e.target.value)}
          />
        </div>
        <div>
          <label className={labelCls}>Số lượng mã *</label>
          <input
            type="number"
            className={inputCls}
            placeholder="100"
            min={1}
            value={form.quantity}
            onChange={(e) => set('quantity', e.target.value)}
            required
          />
        </div>
      </div>

      {/* Per-user limit */}
      <div>
        <label className={labelCls}>Giới hạn mỗi người dùng (lần)</label>
        <input
          type="number"
          className={cn(inputCls, 'max-w-xs')}
          placeholder="1"
          min={1}
          value={form.usageLimitPerUser}
          onChange={(e) => set('usageLimitPerUser', e.target.value)}
        />
      </div>

      {/* Time range */}
      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className={labelCls}>Ngày bắt đầu *</label>
          <input
            type="datetime-local"
            className={inputCls}
            value={form.startTime}
            onChange={(e) => set('startTime', e.target.value)}
            required
          />
        </div>
        <div>
          <label className={labelCls}>Ngày kết thúc *</label>
          <input
            type="datetime-local"
            className={inputCls}
            value={form.endTime}
            onChange={(e) => set('endTime', e.target.value)}
            required
          />
        </div>
      </div>

      {/* Application condition */}
      <div>
        <label className={labelCls}>Điều kiện áp dụng</label>
        <textarea
          className={cn(inputCls, 'resize-none')}
          rows={2}
          placeholder="VD: Áp dụng cho khách hàng mới, chuyến đi trong nội thành..."
          value={form.applicationCondition}
          onChange={(e) => set('applicationCondition', e.target.value)}
        />
      </div>

      {/* Image URL */}
      <div>
        <label className={labelCls}>Ảnh banner (URL)</label>
        <input
          className={inputCls}
          placeholder="https://..."
          value={form.promotionImage}
          onChange={(e) => set('promotionImage', e.target.value)}
        />
        {form.promotionImage && (
          <img src={form.promotionImage} alt="preview" className="mt-2 h-16 rounded-lg object-cover border border-surface-border" />
        )}
      </div>

      {/* Active & Public toggles */}
      <div className="grid grid-cols-2 gap-4">
        <div className="flex items-center gap-3 p-3 bg-surface-dark/50 border border-surface-border rounded-xl">
          <div className="flex-1">
            <span className="text-sm font-medium text-content-main block">Trạng thái</span>
            <span className="text-xs text-content-muted">Kích hoạt mã</span>
          </div>
          <button
            type="button"
            onClick={() => set('isActive', !form.isActive)}
            className={cn(
              'relative w-11 h-6 rounded-full transition-colors',
              form.isActive ? 'bg-brand-500' : 'bg-surface-border'
            )}
          >
            <span
              className={cn(
                'absolute top-1 left-1 w-4 h-4 bg-white rounded-full shadow transition-transform',
                form.isActive && 'translate-x-5'
              )}
            />
          </button>
        </div>

        <div className="flex items-center gap-3 p-3 bg-surface-dark/50 border border-surface-border rounded-xl">
          <div className="flex-1">
            <span className="text-sm font-medium text-content-main block">Hiển thị</span>
            <span className="text-xs text-content-muted">Công khai (Public)</span>
          </div>
          <button
            type="button"
            onClick={() => set('isPublic', !form.isPublic)}
            className={cn(
              'relative w-11 h-6 rounded-full transition-colors',
              form.isPublic ? 'bg-blue-500' : 'bg-surface-border'
            )}
          >
            <span
              className={cn(
                'absolute top-1 left-1 w-4 h-4 bg-white rounded-full shadow transition-transform',
                form.isPublic && 'translate-x-5'
              )}
            />
          </button>
        </div>
      </div>

      {/* Submit */}
      <div className="flex justify-end gap-3 pt-2 border-t border-surface-border">
        <button
          type="submit"
          disabled={submitting}
          className="flex items-center gap-2 px-5 py-2.5 bg-brand-500 hover:bg-brand-600 text-white rounded-xl text-sm font-semibold transition-colors disabled:opacity-50"
        >
          {submitting ? <Spinner size="sm" /> : <RiCheckLine size={16} />}
          {submitting ? 'Đang lưu...' : 'Lưu khuyến mãi'}
        </button>
      </div>
    </form>
  )
}

// ─── Promotion Card ───────────────────────────────────────────────────────────
const PromotionCard = ({ promo, onEdit, onDelete, onToggle, onToggleVisibility }) => {
  const isPercent = promo.discountType === 'PERCENTAGE'
  const daysLeft = getDaysLeft(promo.endTime)
  const isExpired = promo.isExpired || daysLeft < 0
  const isActive = promo.isActive === true

  const usedPct = promo.quantity > 0
    ? Math.min(100, Math.round(((promo.usedCount || 0) / promo.quantity) * 100))
    : 0

  return (
    <div
      className={cn(
        'card p-0 overflow-hidden transition-all hover:shadow-lg hover:-translate-y-0.5',
        !isActive && 'opacity-60',
        isExpired && 'border-red-500/20'
      )}
    >
      {/* Banner */}
      <div
        className={cn(
          'h-24 flex items-center justify-center relative overflow-hidden',
          isPercent
            ? 'bg-gradient-to-br from-brand-900/60 to-brand-700/30'
            : 'bg-gradient-to-br from-yellow-900/60 to-yellow-700/30'
        )}
      >
        {promo.promotionImage && (
          <img src={promo.promotionImage} alt="" className="absolute inset-0 w-full h-full object-cover opacity-30" />
        )}
        <div className="relative text-center">
          <div className={cn('text-3xl font-display font-black', isPercent ? 'text-brand-400' : 'text-yellow-400')}>
            {isPercent ? `${promo.discountValue}%` : formatCurrency(promo.discountValue)}
          </div>
          <div className="text-xs text-content-muted mt-0.5">
            {isPercent ? 'Giảm phần trăm' : 'Giảm cố định'}
          </div>
        </div>

        {/* Status badge */}
        <div className="absolute top-2 right-2">
          {isExpired ? (
            <span className="text-[10px] px-2 py-0.5 rounded-full bg-red-500/20 text-red-400 border border-red-500/30 font-semibold">Hết hạn</span>
          ) : !isActive ? (
            <span className="text-[10px] px-2 py-0.5 rounded-full bg-gray-500/20 text-gray-400 border border-gray-500/30 font-semibold">Tạm dừng</span>
          ) : (
            <span className="text-[10px] px-2 py-0.5 rounded-full bg-green-500/20 text-green-400 border border-green-500/30 font-semibold">Hoạt động</span>
          )}
        </div>

        {/* Type badge */}
        <div className="absolute top-2 left-2">
          <span className={cn('text-[10px] px-2 py-0.5 rounded-full font-semibold border',
            isPercent ? 'bg-brand-500/20 text-brand-400 border-brand-500/30' : 'bg-yellow-500/20 text-yellow-400 border-yellow-500/30'
          )}>
            {isPercent ? '%' : 'VND'}
          </span>
        </div>
      </div>

      {/* Body */}
      <div className="p-4 space-y-3">
        {/* Code + actions */}
        <div className="flex items-center justify-between gap-2">
          <code className="text-sm font-black text-content-main tracking-widest bg-surface-dark px-2.5 py-1 rounded-lg border border-surface-border truncate">
            {promo.promotionCode}
          </code>
          <div className="flex items-center gap-1 shrink-0">
            <button
              onClick={() => onToggleVisibility(promo.promotionId)}
              className={cn('p-1.5 rounded-lg transition-colors',
                promo.isPublic ? 'text-blue-400 hover:bg-blue-500/10' : 'text-content-muted hover:bg-surface-border/40'
              )}
              title={promo.isPublic ? 'Đang công khai - Bấm để ẩn' : 'Đang ẩn - Bấm để công khai'}
            >
              {promo.isPublic ? <RiEyeLine size={16} /> : <RiEyeOffLine size={16} />}
            </button>
            <button
              onClick={() => onToggle(promo.promotionId)}
              className={cn('p-1.5 rounded-lg transition-colors',
                isActive ? 'text-brand-400 hover:bg-brand-500/10' : 'text-content-muted hover:bg-surface-border/40'
              )}
              title={isActive ? 'Tắt khuyến mãi' : 'Bật khuyến mãi'}
            >
              {isActive ? <RiToggleFill size={18} /> : <RiToggleLine size={18} />}
            </button>
            <button
              onClick={() => onEdit(promo)}
              className="p-1.5 rounded-lg text-blue-400 hover:bg-blue-500/10 transition-colors"
              title="Chỉnh sửa"
            >
              <RiEditLine size={15} />
            </button>
            <button
              onClick={() => onDelete(promo)}
              className="p-1.5 rounded-lg text-red-400 hover:bg-red-500/10 transition-colors"
              title="Xóa"
            >
              <RiDeleteBin6Line size={15} />
            </button>
          </div>
        </div>

        {/* Name */}
        <p className="text-sm font-medium text-content-main truncate">{promo.promotionName}</p>

        {/* Stats */}
        <div className="grid grid-cols-3 gap-1.5 text-center">
          <div className="p-1.5 bg-surface-dark/50 rounded-lg">
            <p className="text-[10px] text-content-muted">Còn lại</p>
            <p className="text-xs font-bold text-content-main">{(promo.quantity || 0) - (promo.usedCount || 0)}</p>
          </div>
          <div className="p-1.5 bg-surface-dark/50 rounded-lg">
            <p className="text-[10px] text-content-muted">Đã dùng</p>
            <p className="text-xs font-bold text-green-400">{promo.usedCount ?? 0}</p>
          </div>
          <div className="p-1.5 bg-surface-dark/50 rounded-lg">
            <p className="text-[10px] text-content-muted">Đã lưu</p>
            <p className="text-xs font-bold text-yellow-400">{promo.savedCount ?? 0}</p>
          </div>
        </div>

        {/* Progress bar */}
        <div>
          <div className="flex justify-between text-[10px] text-content-muted mb-1">
            <span>Tỷ lệ sử dụng</span>
            <span>{usedPct}%</span>
          </div>
          <div className="h-1.5 bg-surface-dark rounded-full overflow-hidden">
            <div
              className={cn('h-full rounded-full transition-all', usedPct >= 80 ? 'bg-red-500' : 'bg-brand-500')}
              style={{ width: `${usedPct}%` }}
            />
          </div>
        </div>

        {/* Footer */}
        <div className="flex items-center justify-between text-[11px] text-content-muted pt-1 border-t border-surface-border/50">
          <div className="flex items-center gap-1">
            <RiCalendarLine size={11} />
            <span>{formatDate(promo.endTime)}</span>
          </div>
          {!isExpired && daysLeft != null && (
            <span className={cn('font-medium',
              daysLeft <= 7 ? 'text-red-400' : daysLeft <= 30 ? 'text-yellow-400' : 'text-content-muted'
            )}>
              {daysLeft > 0 ? `Còn ${daysLeft} ngày` : 'Hết hạn hôm nay'}
            </span>
          )}
        </div>

        {promo.minTripValue > 0 && (
          <p className="text-[11px] text-content-muted">
            Tối thiểu: <span className="text-content-main font-medium">{formatCurrency(promo.minTripValue)}</span>
          </p>
        )}
      </div>
    </div>
  )
}

// ─── Main Page ────────────────────────────────────────────────────────────────
const FILTER_TABS = [
  { key: 'ALL',      label: 'Tất cả' },
  { key: 'ACTIVE',   label: 'Hoạt động' },
  { key: 'EXPIRED',  label: 'Hết hạn' },
  { key: 'INACTIVE', label: 'Tạm dừng' },
]

const AdminPromotionsPage = () => {
  const [promotions, setPromotions] = useState([])
  const [loading, setLoading] = useState(true)
  const [search, setSearch] = useState('')
  const [filter, setFilter] = useState('ALL')

  const [formOpen, setFormOpen] = useState(false)
  const [editTarget, setEditTarget] = useState(null)
  const [submitting, setSubmitting] = useState(false)

  const [deleteTarget, setDeleteTarget] = useState(null)
  const [deleting, setDeleting] = useState(false)

  const fetchPromotions = async () => {
    setLoading(true)
    try {
      const data = await masterDataApi.getAllPromotionsForAdmin()
      setPromotions(data || [])
    } catch (e) {
      console.error(e)
      toast.error('Không thể tải danh sách khuyến mãi')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { fetchPromotions() }, [])

  // Filtered list
  const filtered = useMemo(() => {
    const now = new Date()
    return promotions.filter((p) => {
      const matchSearch =
        !search ||
        p.promotionCode?.toLowerCase().includes(search.toLowerCase()) ||
        p.promotionName?.toLowerCase().includes(search.toLowerCase())

      const isExpired = p.isExpired || (p.endTime && new Date(p.endTime) < now)
      const isActive = p.isActive === true && !isExpired

      const matchFilter =
        filter === 'ALL' ||
        (filter === 'ACTIVE'   && isActive) ||
        (filter === 'EXPIRED'  && isExpired) ||
        (filter === 'INACTIVE' && !p.isActive && !isExpired)

      return matchSearch && matchFilter
    })
  }, [promotions, search, filter])

  const openCreate = () => { setEditTarget(null); setFormOpen(true) }
  const openEdit = (promo) => {
    const toLocalDT = (ts) => {
      if (!ts) return ''
      return new Date(ts).toISOString().slice(0, 16)
    }
    setEditTarget({
      ...promo,
      discountValue: promo.discountValue ?? '',
      discountLimit: promo.discountLimit ?? '',
      minTripValue: promo.minTripValue ?? '',
      quantity: promo.quantity ?? '',
      usageLimitPerUser: promo.usageLimitPerUser ?? '',
      applicationCondition: promo.applicationCondition ?? '',
      isPublic: promo.isPublic ?? true,
      promotionImage: promo.promotionImage ?? '',
      startTime: toLocalDT(promo.startTime),
      endTime: toLocalDT(promo.endTime),
    })
    setFormOpen(true)
  }

  const handleSubmit = async (payload) => {
    setSubmitting(true)
    try {
      if (editTarget?.promotionId) {
        await masterDataApi.updatePromotion(editTarget.promotionId, payload)
        toast.success('Cập nhật khuyến mãi thành công!')
      } else {
        await masterDataApi.createPromotion(payload)
        toast.success('Tạo khuyến mãi thành công!')
      }
      setFormOpen(false)
      setEditTarget(null)
      fetchPromotions()
    } catch (e) {
      toast.error(e.response?.data?.message || 'Có lỗi xảy ra, vui lòng thử lại')
    } finally {
      setSubmitting(false)
    }
  }

  const handleToggle = async (id) => {
    try {
      await masterDataApi.togglePromotion(id)
      toast.success('Đã thay đổi trạng thái khuyến mãi')
      fetchPromotions()
    } catch {
      toast.error('Không thể thay đổi trạng thái')
    }
  }

  const handleToggleVisibility = async (id) => {
    try {
      await masterDataApi.toggleVisibility(id)
      toast.success('Đã thay đổi hiển thị (Công khai/Nội bộ)')
      fetchPromotions()
    } catch {
      toast.error('Không thể thay đổi trạng thái hiển thị')
    }
  }

  const handleDelete = async () => {
    if (!deleteTarget) return
    setDeleting(true)
    try {
      await masterDataApi.deletePromotion(deleteTarget.promotionId)
      toast.success(`Đã xóa mã "${deleteTarget.promotionCode}"`)
      setDeleteTarget(null)
      fetchPromotions()
    } catch (e) {
      toast.error(e.response?.data?.message || 'Không thể xóa khuyến mãi')
    } finally {
      setDeleting(false)
    }
  }

  // Summary stats
  const now = new Date()
  const activeCount  = promotions.filter((p) => p.isActive && !p.isExpired && p.endTime && new Date(p.endTime) > now).length
  const expiredCount = promotions.filter((p) => p.isExpired || (p.endTime && new Date(p.endTime) < now)).length
  const totalUsed    = promotions.reduce((acc, p) => acc + (p.usedCount || 0), 0)

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="section-title">Quản lý Khuyến mãi</h1>
          <p className="text-content-muted text-sm mt-1">
            Tổng cộng <span className="text-brand-400 font-semibold">{promotions.length}</span> mã khuyến mãi
          </p>
        </div>
        <button
          onClick={openCreate}
          className="flex items-center gap-2 px-4 py-2.5 bg-brand-500 hover:bg-brand-600 text-white rounded-xl text-sm font-semibold transition-colors shadow-glow-green self-start sm:self-auto"
        >
          <RiAddLine size={18} />
          Tạo mã mới
        </button>
      </div>

      {/* Summary stats */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
        {[
          { label: 'Tổng mã',       value: promotions.length, icon: RiCoupon3Line,         color: 'text-blue-400',   bg: 'bg-blue-400/10' },
          { label: 'Hoạt động',     value: activeCount,       icon: RiToggleFill,           color: 'text-brand-400',  bg: 'bg-brand-400/10' },
          { label: 'Hết hạn',       value: expiredCount,      icon: RiTimeLine,             color: 'text-red-400',    bg: 'bg-red-400/10' },
          { label: 'Lượt đã dùng',  value: totalUsed,         icon: RiGroupLine,            color: 'text-yellow-400', bg: 'bg-yellow-400/10' },
        ].map((s) => (
          <div key={s.label} className="stat-card">
            <div className="flex items-center justify-between">
              <div className={cn('w-9 h-9 rounded-xl flex items-center justify-center', s.bg)}>
                <s.icon size={18} className={s.color} />
              </div>
            </div>
            <div className="stat-value mt-3">{s.value}</div>
            <div className="stat-label">{s.label}</div>
          </div>
        ))}
      </div>

      {/* Filters & Search */}
      <div className="flex flex-col sm:flex-row gap-3 items-start sm:items-center">
        <div className="flex gap-1 p-1 bg-surface-dark border border-surface-border rounded-xl flex-wrap">
          {FILTER_TABS.map((tab) => (
            <button
              key={tab.key}
              onClick={() => setFilter(tab.key)}
              className={cn(
                'px-3 py-1.5 rounded-lg text-xs font-medium transition-all whitespace-nowrap',
                filter === tab.key ? 'bg-brand-500 text-white' : 'text-content-muted hover:text-content-main'
              )}
            >
              {tab.label}
            </button>
          ))}
        </div>

        <div className="flex items-center gap-2 card p-2.5 max-w-xs w-full sm:w-auto">
          <RiSearchLine className="text-content-muted shrink-0" size={16} />
          <input
            type="text"
            placeholder="Tìm mã hoặc tên..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="bg-transparent border-none outline-none flex-1 text-sm text-content-main placeholder:text-content-muted"
          />
          {search && (
            <button onClick={() => setSearch('')} className="text-content-muted hover:text-content-main">
              <RiCloseLine size={14} />
            </button>
          )}
        </div>
      </div>

      {/* Grid */}
      {loading ? (
        <div className="flex justify-center py-20"><Spinner size="xl" /></div>
      ) : filtered.length === 0 ? (
        <div className="card p-16 text-center space-y-3">
          <RiCoupon3Line size={40} className="text-content-muted mx-auto" />
          <p className="text-content-muted text-sm">
            {search || filter !== 'ALL' ? 'Không tìm thấy mã khuyến mãi phù hợp' : 'Chưa có mã khuyến mãi nào'}
          </p>
          {!search && filter === 'ALL' && (
            <button
              onClick={openCreate}
              className="px-4 py-2 bg-brand-500 hover:bg-brand-600 text-white rounded-xl text-sm font-medium transition-colors"
            >
              + Tạo mã đầu tiên
            </button>
          )}
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
          {filtered.map((promo) => (
            <PromotionCard
              key={promo.promotionId}
              promo={promo}
              onEdit={openEdit}
              onDelete={setDeleteTarget}
              onToggle={handleToggle}
              onToggleVisibility={handleToggleVisibility}
            />
          ))}
        </div>
      )}

      {/* Create / Edit Modal */}
      <Modal
        open={formOpen}
        onClose={() => { setFormOpen(false); setEditTarget(null) }}
        title={editTarget ? `Sửa mã: ${editTarget.promotionCode}` : 'Tạo mã khuyến mãi mới'}
        maxWidth="max-w-2xl"
      >
        <PromotionForm
          key={editTarget?.promotionId || 'new'}
          initial={editTarget || EMPTY_FORM}
          onSubmit={handleSubmit}
          submitting={submitting}
        />
      </Modal>

      {/* Delete Confirm */}
      <Modal
        open={!!deleteTarget}
        onClose={() => setDeleteTarget(null)}
        title="Xác nhận xóa"
        maxWidth="max-w-sm"
      >
        <div className="space-y-4">
          <p className="text-content-muted text-sm">
            Bạn có chắc chắn muốn xóa mã{' '}
            <span className="font-mono font-bold text-content-main">{deleteTarget?.promotionCode}</span>?
            Hành động này không thể hoàn tác.
          </p>
          <div className="p-3 bg-red-500/10 border border-red-500/20 rounded-xl text-xs text-red-400">
            ⚠️ Dữ liệu liên quan đến mã khuyến mãi này sẽ bị xóa vĩnh viễn.
          </div>
          <div className="flex gap-3 justify-end">
            <button
              onClick={() => setDeleteTarget(null)}
              className="px-4 py-2 border border-surface-border text-content-muted rounded-xl text-sm hover:bg-surface-border/30 transition-colors"
            >
              Hủy
            </button>
            <button
              onClick={handleDelete}
              disabled={deleting}
              className="flex items-center gap-2 px-4 py-2 bg-red-500/20 hover:bg-red-500/30 text-red-400 border border-red-500/30 rounded-xl text-sm font-medium transition-colors disabled:opacity-50"
            >
              {deleting ? <Spinner size="sm" /> : <RiDeleteBin6Line size={14} />}
              {deleting ? 'Đang xóa...' : 'Xóa khuyến mãi'}
            </button>
          </div>
        </div>
      </Modal>
    </div>
  )
}

export default AdminPromotionsPage
