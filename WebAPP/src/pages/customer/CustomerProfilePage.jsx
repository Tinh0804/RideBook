import { useState, useEffect, useRef } from 'react'
import toast from 'react-hot-toast'
import { 
  RiCameraLine, RiEditLine, RiSaveLine, RiCloseLine, 
  RiStarLine, RiVipCrownLine, RiWalletLine, RiMedalLine,
  RiTrophyLine, RiArrowRightLine, RiKeyLine
} from 'react-icons/ri'
import { useAuthStore } from '@/store/rootStore'
import { customerApi } from '@/features/customer/api/customerApi'
import Button from '@/components/Elements/Button'
import Input from '@/components/Elements/Input'
import FormField from '@/components/Form/FormField'
import Spinner from '@/components/Elements/Spinner'
import ChangePasswordModal from '@/components/Form/ChangePasswordModal'
import { cn } from '@/utils/cn'
import { formatCurrency } from '@/utils/currency'
import { motion } from 'motion/react'

// Mock API call
const getLoyaltyInfo = async (customerId) => {
  return {
    tier: 'Platinum',
    currentPoints: 1250,
    lifetimePoints: 3420,
    totalSpent: 2450000,
    totalRides: 24,
    nextTierPoints: 5000,
    tierBenefits: {
      discountRate: 15,
      prioritySupport: true,
      freeCancel: true,
      exclusivePromotions: true
    }
  }
}

const CustomerProfilePage = () => {
  const { user, updateUser } = useAuthStore()
  const fileRef = useRef()

  const [profile,  setProfile]  = useState(null)
  const [loyalty,  setLoyalty]  = useState(null)
  const [editing,  setEditing]  = useState(false)
  const [loading,  setLoading]  = useState(true)
  const [saving,   setSaving]   = useState(false)
  const [form,     setForm]     = useState({})
  const [preview,  setPreview]  = useState(null)
  const [newFile,  setNewFile]  = useState(null)
  const [showPasswordModal, setShowPasswordModal] = useState(false)

  useEffect(() => {
    Promise.all([
      customerApi.getMyInfo(),
      getLoyaltyInfo(user?.id)
    ])
      .then(([info, loyaltyInfo]) => {
        setProfile(info)
        setLoyalty(loyaltyInfo)
        if (info) {
          setForm({ 
            customerName: info.customerName || '', 
            phone: info.phone || '', 
            address: info.address || '', 
            email: info.email || '',
            avatar: info.avatar || '' , 
            gender: info.gender || '' , 
            birthDate: info.birthDate ? info.birthDate.split('T')[0] : '', 
            account: info.account || null
          })
        }
      })
      .catch(() => {})
      .finally(() => setLoading(false))
  }, [user?.id])

  const handleFileChange = (e) => {
    const file = e.target.files[0]
    if (!file) return
    setNewFile(file)
    setPreview(URL.createObjectURL(file))
  }

  const handleSave = async () => {
    setSaving(true)
    try {
      const fd = new FormData()
      if (form.customerName) fd.append('customerName', form.customerName)
      if (form.phone) fd.append('phone', form.phone)
      if (form.address) fd.append('address', form.address)
      if (form.email) fd.append('email', form.email)
      if (form.gender) fd.append('gender', form.gender)
      if (form.birthDate) fd.append('birthDate', form.birthDate)
      
      if (newFile) {
        fd.append('avatar', newFile)
      }

      const updated = await customerApi.updateMyInfo(fd)
      setProfile(updated)
      updateUser(updated)
      setEditing(false)
      setNewFile(null)
      setPreview(null)
      toast.success('Cập nhật thông tin thành công!')
    } catch (err) {
      toast.error(err?.response?.data?.message || 'Cập nhật thất bại')
    } finally {
      setSaving(false)
    }
  }

  const handleCancel = () => {
    setEditing(false)
    setNewFile(null)
    setPreview(null)
    setForm({ 
      customerName: profile?.customerName || '', 
      phone: profile?.phone || '', 
      address: profile?.address || '', 
      email: profile?.email || '',
      avatar: profile?.avatar || '',
      gender: profile?.gender || '',
      birthDate: profile?.birthDate ? profile.birthDate.split('T')[0] : '',
      account: profile?.account || null
    })
  }

  const getTierInfo = (tier) => {
    switch(tier?.toLowerCase()) {
      case 'platinum':
        return { 
          bgClass: 'bg-slate-950 text-white border-slate-900',
          accent: 'text-white',
          muted: 'text-white/60',
          progressBg: 'bg-white/20',
          progressFill: 'bg-white',
          icon: <RiTrophyLine size={24} />,
          label: 'Kim Cương'
        }
      case 'gold':
        return { 
          bgClass: 'bg-yellow-500/10 border-yellow-500/20 text-yellow-800 dark:text-yellow-500',
          accent: 'text-yellow-600 dark:text-yellow-400',
          muted: 'text-yellow-700/60 dark:text-yellow-500/60',
          progressBg: 'bg-yellow-500/20',
          progressFill: 'bg-yellow-500',
          icon: <RiVipCrownLine size={24} />,
          label: 'Vàng'
        }
      default:
        return { 
          bgClass: 'bg-surface-card border-surface-border text-content-main',
          accent: 'text-brand-500',
          muted: 'text-content-muted',
          progressBg: 'bg-surface-muted',
          progressFill: 'bg-brand-500',
          icon: <RiStarLine size={24} />,
          label: 'Thành viên'
        }
    }
  }

  const tierInfo = getTierInfo(loyalty?.tier)
  
  const progressPercent = loyalty?.nextTierPoints 
    ? (loyalty.lifetimePoints / loyalty.nextTierPoints) * 100 
    : 100

  if (loading) return (
    <div className="flex h-full items-center justify-center bg-[#e8ece3] dark:bg-surface-dark">
      <Spinner size="xl" />
    </div>
  )

  const avatarSrc = preview || profile?.avatar

  return (
    <div className="h-full overflow-y-auto bg-[#e8ece3] dark:bg-surface-dark pointer-events-auto">
      <motion.div 
        initial={{ opacity: 0, x: -18 }}
        animate={{ opacity: 1, x: 0 }}
        transition={{ duration: 0.45, ease: [0.16, 1, 0.3, 1] }}
        className="mx-auto w-full max-w-3xl space-y-8 p-5 pb-12 lg:p-8"
      >
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <h1 className="font-display text-3xl font-bold text-content-main tracking-tight">Hồ sơ cá nhân</h1>
            <p className="text-sm text-content-muted mt-1">Quản lý thông tin và trạng thái thành viên</p>
          </div>
          {!editing && (
            <div className="flex gap-2">
              <Button variant="outline" size="sm" onClick={() => setShowPasswordModal(true)} className="rounded-xl border-surface-border font-bold text-content-main hover:border-slate-400">
                <RiKeyLine size={16} /> Đổi mật khẩu
              </Button>
              <Button size="sm" onClick={() => setEditing(true)} className="rounded-xl bg-slate-950 font-bold text-white hover:bg-slate-800 dark:bg-white dark:text-slate-950 dark:hover:bg-slate-200">
                <RiEditLine size={16} /> Chỉnh sửa
              </Button>
            </div>
          )}
        </div>

        {/* Profile Header & Tier */}
        <div className="grid gap-6 md:grid-cols-[1fr_1.5fr]">
          {/* Avatar Area */}
          <div className="rounded-2xl border border-surface-border bg-surface-card p-6 flex flex-col items-center justify-center text-center shadow-sm relative overflow-hidden">
            <div className="absolute top-0 left-0 w-full h-24 bg-surface-muted/50" />
            <div className="relative mb-4 mt-6">
              <div className="h-28 w-28 rounded-full border-4 border-surface-card bg-slate-100 dark:bg-slate-800 overflow-hidden flex items-center justify-center text-5xl font-bold text-slate-400 shadow-md">
                {avatarSrc
                  ? <img src={avatarSrc} alt="avatar" className="h-full w-full object-cover" />
                  : (profile?.customerName?.[0] || user?.userName?.[0] || 'U')
                } 
              </div>
              {editing && (
                <>
                  <button
                    onClick={() => fileRef.current?.click()}
                    className="absolute bottom-1 right-1 grid h-8 w-8 place-items-center rounded-full bg-slate-950 text-white dark:bg-white dark:text-slate-950 shadow-md hover:scale-105 transition-transform"
                  >
                    <RiCameraLine size={16} />
                  </button>
                  <input ref={fileRef} type="file" accept="image/*" className="sr-only" onChange={handleFileChange} />
                </>
              )}
            </div>
            <h2 className="font-display text-xl font-bold text-content-main">{profile?.customerName}</h2>
            <p className="text-sm font-mono text-content-muted mt-1">{profile?.userName || user?.userName}</p>
          </div>

          {/* Membership Tier */}
          {loyalty && (
            <div className={cn("relative overflow-hidden rounded-2xl border p-6 shadow-sm flex flex-col justify-between", tierInfo.bgClass)}>
              <div className="flex items-start justify-between relative z-10">
                <div className="flex items-center gap-3">
                  <div className={cn("grid h-12 w-12 place-items-center rounded-xl", tierInfo.progressBg)}>
                    {tierInfo.icon}
                  </div>
                  <div>
                    <p className={cn("text-xs font-bold uppercase tracking-wider", tierInfo.muted)}>Hạng thành viên</p>
                    <p className="font-display text-2xl font-bold">{tierInfo.label}</p>
                  </div>
                </div>
                <div className="text-right">
                  <p className={cn("text-xs font-bold uppercase tracking-wider", tierInfo.muted)}>Điểm thưởng</p>
                  <p className="font-display text-2xl font-bold">{loyalty.currentPoints.toLocaleString()}</p>
                </div>
              </div>

              <div className="mt-8 relative z-10">
                {loyalty.nextTierPoints && (
                  <div className="space-y-2">
                    <div className="flex justify-between text-xs font-bold">
                      <span className={tierInfo.muted}>Tiến trình lên hạng</span>
                      <span className={tierInfo.muted}>{loyalty.lifetimePoints.toLocaleString()} / {loyalty.nextTierPoints.toLocaleString()}</span>
                    </div>
                    <div className={cn("h-1.5 w-full rounded-full overflow-hidden", tierInfo.progressBg)}>
                      <div 
                        className={cn("h-full rounded-full transition-all duration-1000", tierInfo.progressFill)}
                        style={{ width: `${Math.min(progressPercent, 100)}%` }}
                      />
                    </div>
                  </div>
                )}
                
                <div className="mt-6 flex gap-6 border-t border-current/10 pt-4">
                  <div>
                    <p className={cn("text-[10px] font-bold uppercase tracking-wider mb-1", tierInfo.muted)}>Tổng chi tiêu</p>
                    <p className="text-sm font-bold">{formatCurrency(loyalty.totalSpent)}</p>
                  </div>
                  <div>
                    <p className={cn("text-[10px] font-bold uppercase tracking-wider mb-1", tierInfo.muted)}>Số chuyến</p>
                    <p className="text-sm font-bold">{loyalty.totalRides}</p>
                  </div>
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Profile Fields */}
        <div className="rounded-2xl border border-surface-border bg-surface-card p-6 shadow-sm">
          <h3 className="font-bold text-lg mb-6">Thông tin chi tiết</h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-x-8 gap-y-6">
            <FormField label="Họ và tên">
              {editing
                ? <Input value={form.customerName} onChange={(e) => setForm({ ...form, customerName: e.target.value })} className="!rounded-xl !bg-surface-dark !border-surface-border" />
                : <p className="text-content-main font-bold py-2 border-b border-surface-border/50">{profile?.customerName || '—'}</p>
              }
            </FormField>

            <FormField label="Số điện thoại">
              {editing
                ? <Input value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })} className="!rounded-xl !bg-surface-dark !border-surface-border" />
                : <p className="text-content-main font-bold py-2 border-b border-surface-border/50">{profile?.phone || '—'}</p>
              }
            </FormField>

            <FormField label="Email">
              {editing
                ? <Input type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} className="!rounded-xl !bg-surface-dark !border-surface-border" />
                : <p className="text-content-main font-bold py-2 border-b border-surface-border/50">{profile?.email || '—'}</p>
              }
            </FormField>

            <FormField label="Giới tính">
              {editing
                ? (
                  <select 
                    className="w-full rounded-xl bg-surface-dark border border-surface-border px-4 py-2.5 text-content-main focus:outline-none focus:border-slate-500"
                    value={form.gender} 
                    onChange={(e) => setForm({ ...form, gender: e.target.value })}
                  >
                    <option value="">Chọn giới tính</option>
                    <option value="Nam">Nam</option>
                    <option value="Nữ">Nữ</option>
                    <option value="Khác">Khác</option>
                  </select>
                )
                : <p className="text-content-main font-bold py-2 border-b border-surface-border/50">{profile?.gender || '—'}</p>
              }
            </FormField>

            <FormField label="Ngày sinh">
              {editing
                ? <Input type="date" value={form.birthDate} onChange={(e) => setForm({ ...form, birthDate: e.target.value })} className="!rounded-xl !bg-surface-dark !border-surface-border" />
                : <p className="text-content-main font-bold py-2 border-b border-surface-border/50">{profile?.birthDate ? new Date(profile.birthDate).toLocaleDateString('vi-VN') : '—'}</p>
              }
            </FormField>

            <div className="col-span-1 md:col-span-2">
              <FormField label="Địa chỉ">
                {editing
                  ? <Input value={form.address} onChange={(e) => setForm({ ...form, address: e.target.value })} className="!rounded-xl !bg-surface-dark !border-surface-border" />
                  : <p className="text-content-main font-bold py-2 border-b border-surface-border/50">{profile?.address || '—'}</p>
                }
              </FormField>
            </div>
          </div>

          {/* Action Buttons */}
          {editing && (
            <div className="mt-8 flex gap-3 pt-6 border-t border-surface-border">
              <Button variant="outline" className="flex-1 rounded-xl h-12 font-bold border-surface-border" onClick={handleCancel}>
                Hủy thay đổi
              </Button>
              <Button className="flex-1 rounded-xl h-12 bg-lime-accent text-slate-950 font-bold hover:bg-[#b8ff59]" onClick={handleSave} loading={saving}>
                Lưu thông tin
              </Button>
            </div>
          )}
        </div>

        {/* Benefits Section */}
        {loyalty?.tierBenefits && (
          <div className="rounded-2xl border border-surface-border bg-surface-card p-6 shadow-sm">
            <h3 className="font-bold text-lg mb-4 flex items-center gap-2">
              <RiVipCrownLine className="text-yellow-500" /> Đặc quyền {tierInfo.label}
            </h3>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              {loyalty.tierBenefits.discountRate > 0 && (
                <div className="flex items-center gap-3 p-3 rounded-xl bg-surface-muted/50 border border-surface-border">
                  <span className="grid h-8 w-8 place-items-center rounded-lg bg-emerald-500/10 text-emerald-500">✓</span>
                  <span className="text-sm font-bold text-content-main">Giảm {loyalty.tierBenefits.discountRate}% mỗi chuyến</span>
                </div>
              )}
              {loyalty.tierBenefits.prioritySupport && (
                <div className="flex items-center gap-3 p-3 rounded-xl bg-surface-muted/50 border border-surface-border">
                  <span className="grid h-8 w-8 place-items-center rounded-lg bg-emerald-500/10 text-emerald-500">✓</span>
                  <span className="text-sm font-bold text-content-main">Hỗ trợ ưu tiên 24/7</span>
                </div>
              )}
              {loyalty.tierBenefits.freeCancel && (
                <div className="flex items-center gap-3 p-3 rounded-xl bg-surface-muted/50 border border-surface-border">
                  <span className="grid h-8 w-8 place-items-center rounded-lg bg-emerald-500/10 text-emerald-500">✓</span>
                  <span className="text-sm font-bold text-content-main">Miễn phí hủy chuyến</span>
                </div>
              )}
              {loyalty.tierBenefits.exclusivePromotions && (
                <div className="flex items-center gap-3 p-3 rounded-xl bg-surface-muted/50 border border-surface-border">
                  <span className="grid h-8 w-8 place-items-center rounded-lg bg-emerald-500/10 text-emerald-500">✓</span>
                  <span className="text-sm font-bold text-content-main">Khuyến mãi độc quyền</span>
                </div>
              )}
            </div>
          </div>
        )}

      </motion.div>
      {showPasswordModal && (
        <ChangePasswordModal onClose={() => setShowPasswordModal(false)} />
      )}
    </div>
  )
}

export default CustomerProfilePage