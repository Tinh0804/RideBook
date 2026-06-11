import { useState, useEffect, useRef } from 'react'
import toast from 'react-hot-toast'
import { 
  RiCameraLine, RiEditLine, RiSaveLine, RiCloseLine, 
  RiStarLine, RiVipCrownLine, RiWalletLine, RiMedalLine,
  RiTrophyLine, RiArrowRightLine
} from 'react-icons/ri'
import { useAuthStore } from '@/store/rootStore'
import { customerApi } from '@/features/customer/api/customerApi'
import Button from '@/components/Elements/Button'
import Input from '@/components/Elements/Input'
import FormField from '@/components/Form/FormField'
import Spinner from '@/components/Elements/Spinner'
import { cn } from '@/utils/cn'
import { formatCurrency } from '@/utils/currency'

// Mock API call - Thay thế bằng API thực tế
const getLoyaltyInfo = async (customerId) => {
  // TODO: Gọi API thực tế từ backend
  // return await loyaltyApi.getCustomerLoyalty(customerId)
  
  // Mock data cho demo
  return {
    tier: 'Platinum', // Bronze, Silver, Gold, Platinum
    currentPoints: 1250,
    lifetimePoints: 3420,
    totalSpent: 2450000,
    totalRides: 24,
    nextTierPoints: 5000, // Điểm cần để lên hạng tiếp theo
    tierBenefits: {
      discountRate: 15, // Giảm giá %
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
            birthDate: info.birthDate ? info.birthDate.split('T')[0] : '', // format to YYYY-MM-DD
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

  // Hàm lấy màu sắc và icon theo hạng
  const getTierInfo = (tier) => {
    switch(tier?.toLowerCase()) {
      case 'platinum':
        return { 
          color: 'from-gray-300 to-gray-400 dark:from-gray-400 dark:to-gray-500', 
          bgColor: 'bg-gradient-to-r from-gray-100 to-gray-200 dark:from-gray-800 dark:to-gray-700 border border-gray-200 dark:border-gray-600',
          textColor: 'text-gray-800 dark:text-gray-100',
          subTextColor: 'text-gray-500 dark:text-gray-400',
          iconBg: 'bg-white/80 dark:bg-gray-900/50',
          borderColor: 'border-gray-400 dark:border-gray-500',
          circleBg: 'bg-black/5 dark:bg-white/10',
          divider: 'border-gray-300 dark:border-gray-600',
          icon: <RiTrophyLine size={24} />,
          label: 'Kim Cương'
        }
      case 'gold':
        return { 
          color: 'from-yellow-500 to-yellow-600 dark:from-yellow-400 dark:to-yellow-500', 
          bgColor: 'bg-gradient-to-r from-yellow-50 to-yellow-100 dark:from-yellow-900/40 dark:to-amber-800/40 border border-yellow-200 dark:border-yellow-700/50',
          textColor: 'text-yellow-800 dark:text-yellow-400',
          subTextColor: 'text-yellow-600 dark:text-yellow-500/80',
          iconBg: 'bg-white/80 dark:bg-yellow-900/50',
          borderColor: 'border-yellow-500 dark:border-yellow-600',
          circleBg: 'bg-yellow-500/10 dark:bg-yellow-400/10',
          divider: 'border-yellow-200 dark:border-yellow-700/50',
          icon: <RiVipCrownLine size={24} />,
          label: 'Vàng'
        }
      case 'silver':
        return { 
          color: 'from-gray-400 to-gray-500 dark:from-gray-300 dark:to-gray-400', 
          bgColor: 'bg-gradient-to-r from-gray-50 to-gray-100 dark:from-gray-800/80 dark:to-gray-700/80 border border-gray-200 dark:border-gray-600',
          textColor: 'text-gray-700 dark:text-gray-200',
          subTextColor: 'text-gray-500 dark:text-gray-400',
          iconBg: 'bg-white/80 dark:bg-gray-900/50',
          borderColor: 'border-gray-400 dark:border-gray-500',
          circleBg: 'bg-black/5 dark:bg-white/10',
          divider: 'border-gray-200 dark:border-gray-600',
          icon: <RiMedalLine size={24} />,
          label: 'Bạc'
        }
      default:
        return { 
          color: 'from-amber-600 to-amber-700 dark:from-amber-500 dark:to-amber-600', 
          bgColor: 'bg-gradient-to-r from-amber-50 to-amber-100 dark:from-amber-900/40 dark:to-orange-900/40 border border-amber-200 dark:border-amber-700/50',
          textColor: 'text-amber-800 dark:text-amber-400',
          subTextColor: 'text-amber-600 dark:text-amber-500/80',
          iconBg: 'bg-white/80 dark:bg-amber-900/50',
          borderColor: 'border-amber-600 dark:border-amber-500',
          circleBg: 'bg-amber-500/10 dark:bg-amber-400/10',
          divider: 'border-amber-200 dark:border-amber-700/50',
          icon: <RiStarLine size={24} />,
          label: 'Đồng'
        }
    }
  }

  const tierInfo = getTierInfo(loyalty?.tier)
  
  // Tính phần trăm tiến trình lên hạng tiếp theo
  const progressPercent = loyalty?.nextTierPoints 
    ? (loyalty.lifetimePoints / loyalty.nextTierPoints) * 100 
    : 100

  if (loading) return (
    <div className="flex justify-center py-16"><Spinner size="xl" /></div>
  )

  const avatarSrc = preview || profile?.avatar

  return (
    <div className="max-w-2xl mx-auto space-y-8">
      <div className="flex items-center justify-between">
        <h1 className="section-title">Hồ sơ cá nhân</h1>
        {!editing && (
          <Button variant="outline" size="sm" onClick={() => setEditing(true)}>
            <RiEditLine size={16} /> Chỉnh sửa
          </Button>
        )}
      </div>

      {/* Avatar section */}
      <div className="flex flex-col items-center gap-4">
        <div className="relative">
          <div className="w-24 h-24 rounded-full bg-brand-500/20 border-2 border-brand-500/30 overflow-hidden flex items-center justify-center text-4xl font-bold text-brand-400">
            {avatarSrc
              ? <img src={avatarSrc} alt="avatar" className="w-full h-full object-cover" />
              : (profile?.customerName?.[0] || user?.userName?.[0] || 'U')
            } 
          </div>
          {editing && (
            <>
              <button
                onClick={() => fileRef.current?.click()}
                className="absolute bottom-0 right-0 w-8 h-8 rounded-full bg-brand-500 flex items-center justify-center shadow-glow-green hover:bg-brand-400 transition-colors"
              >
                <RiCameraLine size={16} className="text-content-main" />
              </button>
              <input ref={fileRef} type="file" accept="image/*" className="sr-only" onChange={handleFileChange} />
            </>
          )}
        </div>
        <div className="text-center">
          <h2 className="font-display text-xl font-bold text-content-main">{profile?.customerName}</h2>
          <p className="text-content-muted text-sm">{profile?.userName || user?.userName}</p>
        </div>
      </div>

      {/* Membership Tier Card */}
      {loyalty && (
        <div className={cn("relative overflow-hidden rounded-2xl bg-gradient-to-r p-6 shadow-xl", tierInfo.bgColor)}>
          <div className={cn("absolute top-0 right-0 w-32 h-32 backdrop-blur-sm rounded-full -mr-16 -mt-16", tierInfo.circleBg)} />
          <div className="relative z-10">
            <div className="flex items-center justify-between mb-4">
              <div className="flex items-center gap-3">
                <div className={cn("w-12 h-12 rounded-full flex items-center justify-center", tierInfo.iconBg, tierInfo.textColor)}>
                  {tierInfo.icon}
                </div>
                <div>
                  <p className={cn("text-sm", tierInfo.subTextColor)}>Hạng thành viên</p>
                  <p className={cn("text-2xl font-bold", tierInfo.textColor)}>{tierInfo.label}</p>
                </div>
              </div>
              <div className="text-right">
                <p className={cn("text-sm", tierInfo.subTextColor)}>Điểm thưởng</p>
                <p className={cn("text-2xl font-bold", tierInfo.textColor)}>{loyalty.currentPoints.toLocaleString()}</p>
              </div>
            </div>

            {/* Progress to next tier */}
            {loyalty.nextTierPoints && (
              <div className="space-y-2 mt-4">
                <div className="flex justify-between text-xs">
                  <span className={tierInfo.subTextColor}>Tiến trình lên hạng</span>
                  <span className={tierInfo.subTextColor}>{loyalty.lifetimePoints.toLocaleString()} / {loyalty.nextTierPoints.toLocaleString()} điểm</span>
                </div>
                <div className={cn("h-2 rounded-full overflow-hidden", tierInfo.circleBg)}>
                  <div 
                    className={cn("h-full bg-gradient-to-r rounded-full transition-all duration-500", tierInfo.color)}
                    style={{ width: `${Math.min(progressPercent, 100)}%` }}
                  />
                </div>
              </div>
            )}

            {/* Quick stats */}
            <div className={cn("grid grid-cols-3 gap-3 mt-6 pt-4 border-t", tierInfo.divider)}>
              <div className="text-center">
                <p className={cn("text-xs", tierInfo.subTextColor)}>Tổng chi tiêu</p>
                <p className={cn("text-sm font-semibold", tierInfo.textColor)}>{formatCurrency(loyalty.totalSpent)}</p>
              </div>
              <div className="text-center">
                <p className={cn("text-xs", tierInfo.subTextColor)}>Số chuyến</p>
                <p className={cn("text-sm font-semibold", tierInfo.textColor)}>{loyalty.totalRides}</p>
              </div>
              <div className="text-center">
                <p className={cn("text-xs", tierInfo.subTextColor)}>Giảm giá</p>
                <p className="text-sm font-semibold text-brand-500">-{loyalty.tierBenefits?.discountRate || 0}%</p>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Profile fields */}
      <div className="card p-6 grid grid-cols-1 md:grid-cols-2 gap-6">
        <FormField label="Họ và tên">
          {editing
            ? <Input value={form.customerName} onChange={(e) => setForm({ ...form, customerName: e.target.value })} placeholder="Nguyễn Văn A" />
            : <p className="text-content-main font-medium py-2">{profile?.customerName || '—'}</p>
          }
        </FormField>

        <FormField label="Số điện thoại">
          {editing
            ? <Input value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })} placeholder="0912345678" />
            : <p className="text-content-main font-medium py-2">{profile?.phone || '—'}</p>
          }
        </FormField>

        <FormField label="Email">
          {editing
            ? <Input type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} placeholder="email@example.com" />
            : <p className="text-content-main font-medium py-2">{profile?.email || '—'}</p>
          }
        </FormField>

        <FormField label="Giới tính">
          {editing
            ? (
              <select 
                className="input-field"
                value={form.gender} 
                onChange={(e) => setForm({ ...form, gender: e.target.value })}
              >
                <option value="">Chọn giới tính</option>
                <option value="Nam">Nam</option>
                <option value="Nữ">Nữ</option>
                <option value="Khác">Khác</option>
              </select>
            )
            : <p className="text-content-main font-medium py-2">{profile?.gender || '—'}</p>
          }
        </FormField>

        <FormField label="Ngày sinh">
          {editing
            ? <Input type="date" value={form.birthDate} onChange={(e) => setForm({ ...form, birthDate: e.target.value })} />
            : <p className="text-content-main font-medium py-2">{profile?.birthDate ? new Date(profile.birthDate).toLocaleDateString('vi-VN') : '—'}</p>
          }
        </FormField>

        <div className="col-span-full">
          <FormField label="Địa chỉ">
            {editing
              ? <Input value={form.address} onChange={(e) => setForm({ ...form, address: e.target.value })} placeholder="123 Đường ABC..." />
              : <p className="text-content-main font-medium py-2">{profile?.address || '—'}</p>
            }
          </FormField>
        </div>

        <div className="col-span-full">
          <FormField label="Tên đăng nhập">
            <p className="text-content-muted font-mono text-sm py-2">{profile?.userName || user?.userName}</p>
          </FormField>
        </div>
      </div>

      {/* Tier Benefits Section */}
      {loyalty && loyalty.tierBenefits && (
        <div className="card p-6 space-y-4">
          <div className="flex items-center gap-2">
            <RiVipCrownLine size={20} className="text-yellow-500" />
            <h3 className="font-semibold text-content-main">Quyền lợi {tierInfo.label}</h3>
          </div>
          <div className="grid grid-cols-2 gap-3">
            {loyalty.tierBenefits.discountRate > 0 && (
              <div className="flex items-center gap-2 text-sm">
                <span className="w-1.5 h-1.5 rounded-full bg-green-500"></span>
                <span className="text-content-muted">Giảm {loyalty.tierBenefits.discountRate}% mỗi chuyến</span>
              </div>
            )}
            {loyalty.tierBenefits.prioritySupport && (
              <div className="flex items-center gap-2 text-sm">
                <span className="w-1.5 h-1.5 rounded-full bg-green-500"></span>
                <span className="text-content-muted">Hỗ trợ ưu tiên 24/7</span>
              </div>
            )}
            {loyalty.tierBenefits.freeCancel && (
              <div className="flex items-center gap-2 text-sm">
                <span className="w-1.5 h-1.5 rounded-full bg-green-500"></span>
                <span className="text-content-muted">Miễn phí hủy chuyến</span>
              </div>
            )}
            {loyalty.tierBenefits.exclusivePromotions && (
              <div className="flex items-center gap-2 text-sm">
                <span className="w-1.5 h-1.5 rounded-full bg-green-500"></span>
                <span className="text-content-muted">Khuyến mãi độc quyền</span>
              </div>
            )}
          </div>
          <Button 
            variant="outline" 
            size="sm" 
            fullWidth
            onClick={() => window.location.href = '/customer/membership'}
            className="mt-2"
          >
            Xem chi tiết quyền lợi <RiArrowRightLine size={14} />
          </Button>
        </div>
      )}

      {/* Action buttons */}
      {editing && (
        <div className="flex gap-3">
          <Button variant="outline" fullWidth onClick={handleCancel}>
            <RiCloseLine size={16} /> Hủy
          </Button>
          <Button fullWidth onClick={handleSave} loading={saving}>
            <RiSaveLine size={16} /> Lưu thay đổi
          </Button>
        </div>
      )}
    </div>
  )
}

export default CustomerProfilePage