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
            name: info.customerName || '', phoneNumber: info.phone || '', address: info.address || '', 
            avatar: info.avatar || '' , gender : info.gender || '' , birthDate : info.birthDate || '',
            account : info.account || null
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
      fd.append('name',        form.customerName)
      fd.append('phoneNumber', form.phone)
      fd.append('address',     form.address)
      if (newFile) 
        fd.append('avatar', newFile)

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
      name: profile?.customerName || '', 
      phoneNumber: profile?.phone || '', 
      address: profile?.address || '', 
      avatar: profile?.avatar || '',
      gender: profile?.gender || '',
      birthDate: profile?.birthDate || '',
      account: profile?.account || null
    })
  }

  // Hàm lấy màu sắc và icon theo hạng
  const getTierInfo = (tier) => {
    switch(tier?.toLowerCase()) {
      case 'platinum':
        return { 
          color: 'from-gray-300 to-gray-400', 
          bgColor: 'bg-gradient-to-r from-gray-100 to-gray-200',
          textColor: 'text-gray-700',
          borderColor: 'border-gray-400',
          icon: <RiTrophyLine size={24} />,
          label: 'Kim Cương'
        }
      case 'gold':
        return { 
          color: 'from-yellow-500 to-yellow-600', 
          bgColor: 'bg-gradient-to-r from-yellow-50 to-yellow-100',
          textColor: 'text-yellow-700',
          borderColor: 'border-yellow-500',
          icon: <RiVipCrownLine size={24} />,
          label: 'Vàng'
        }
      case 'silver':
        return { 
          color: 'from-gray-400 to-gray-500', 
          bgColor: 'bg-gradient-to-r from-gray-50 to-gray-100',
          textColor: 'text-gray-600',
          borderColor: 'border-gray-400',
          icon: <RiMedalLine size={24} />,
          label: 'Bạc'
        }
      default:
        return { 
          color: 'from-amber-600 to-amber-700', 
          bgColor: 'bg-gradient-to-r from-amber-50 to-amber-100',
          textColor: 'text-amber-700',
          borderColor: 'border-amber-600',
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
          <div className="absolute top-0 right-0 w-32 h-32 bg-white/20 rounded-full -mr-16 -mt-16" />
          <div className="relative z-10">
            <div className="flex items-center justify-between mb-4">
              <div className="flex items-center gap-3">
                <div className={cn("w-12 h-12 rounded-full bg-white/80 flex items-center justify-center", tierInfo.textColor)}>
                  {tierInfo.icon}
                </div>
                <div>
                  <p className="text-sm text-content-muted">Hạng thành viên</p>
                  <p className={cn("text-2xl font-bold", tierInfo.textColor)}>{tierInfo.label}</p>
                </div>
              </div>
              <div className="text-right">
                <p className="text-sm text-content-muted">Điểm thưởng</p>
                <p className="text-2xl font-bold text-content-main">{loyalty.currentPoints.toLocaleString()}</p>
              </div>
            </div>

            {/* Progress to next tier */}
            {loyalty.nextTierPoints && (
              <div className="space-y-2 mt-4">
                <div className="flex justify-between text-xs">
                  <span className="text-content-muted">Tiến trình lên hạng</span>
                  <span className="text-content-muted">{loyalty.lifetimePoints.toLocaleString()} / {loyalty.nextTierPoints.toLocaleString()} điểm</span>
                </div>
                <div className="h-2 bg-white/30 rounded-full overflow-hidden">
                  <div 
                    className="h-full bg-gradient-to-r from-brand-500 to-brand-600 rounded-full transition-all duration-500"
                    style={{ width: `${Math.min(progressPercent, 100)}%` }}
                  />
                </div>
              </div>
            )}

            {/* Quick stats */}
            <div className="grid grid-cols-3 gap-3 mt-6 pt-4 border-t border-white/20">
              <div className="text-center">
                <p className="text-xs text-content-muted">Tổng chi tiêu</p>
                <p className="text-sm font-semibold text-content-main">{formatCurrency(loyalty.totalSpent)}</p>
              </div>
              <div className="text-center">
                <p className="text-xs text-content-muted">Số chuyến</p>
                <p className="text-sm font-semibold text-content-main">{loyalty.totalRides}</p>
              </div>
              <div className="text-center">
                <p className="text-xs text-content-muted">Giảm giá</p>
                <p className="text-sm font-semibold text-green-600">-{loyalty.tierBenefits?.discountRate || 0}%</p>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Profile fields */}
      <div className="card p-6 space-y-5">
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

        <FormField label="Địa chỉ">
          {editing
            ? <Input value={form.address} onChange={(e) => setForm({ ...form, address: e.target.value })} placeholder="123 Đường ABC..." />
            : <p className="text-content-main font-medium py-2">{profile?.address || '—'}</p>
          }
        </FormField>

        <FormField label="Tên đăng nhập">
          <p className="text-content-muted font-mono text-sm py-2">{profile?.userName || user?.userName}</p>
        </FormField>
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