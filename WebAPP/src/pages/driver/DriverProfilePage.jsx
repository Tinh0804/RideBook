import { useState, useEffect, useRef } from 'react'
import toast from 'react-hot-toast'
import {
  RiCameraLine, RiEditLine, RiSaveLine, RiCloseLine, RiStarFill, RiStarLine,
  RiCarLine, RiMapPinLine, RiFileTextLine, RiIdCardLine, RiShieldCheckLine, RiMotorbikeLine, RiKeyLine
} from 'react-icons/ri'
import { useAuthStore } from '@/store/rootStore'
import { driverApi } from '@/features/driver/api/driverApi'
import { ratingApi } from '@/features/booking/api/masterDataApi'
import Button from '@/components/Elements/Button'
import Input from '@/components/Elements/Input'
import FormField from '@/components/Form/FormField'
import Spinner from '@/components/Elements/Spinner'
import ChangePasswordModal from '@/components/Form/ChangePasswordModal'
import { formatDate } from '@/utils/formatDate'
import { motion } from 'motion/react'
import { cn } from '@/utils/cn'

const DriverProfilePage = () => {
  const { user, updateUser } = useAuthStore()
  const fileRef = useRef()

  const [profile,  setProfile]  = useState(null)
  const [ratings,  setRatings]  = useState([])
  const [editing,  setEditing]  = useState(false)
  const [loading,  setLoading]  = useState(true)
  const [saving,   setSaving]   = useState(false)
  const [form,     setForm]     = useState({})
  const [preview,  setPreview]  = useState(null)
  const [newFile,  setNewFile]  = useState(null)
  const [showPasswordModal, setShowPasswordModal] = useState(false)

  useEffect(() => {
    Promise.all([
      driverApi.getMyInfo(),
      user?.id ? ratingApi.getByDriver(user.id) : Promise.resolve({ result: [] }),
    ])
      .then(([info, rev]) => {
        setProfile(info)
        if (info) {
          setForm({ driverName: info.driverName || info.name || '', phone: info.phone || '', email: info.email || '', address: info.address || '' })
        }
        setRatings(rev || [])
      })
      .catch(() => {})
      .finally(() => setLoading(false))
  }, [user])

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
      Object.entries(form).forEach(([k, v]) => fd.append(k, v))
      if (newFile) fd.append('avatar', newFile)
      
      const updated = await driverApi.updateMyInfo(fd)
      
      setProfile(updated)
      updateUser(updated)
      setEditing(false)
      setNewFile(null)
      setPreview(null)
      toast.success('Cập nhật hồ sơ thành công!')
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
      driverName: profile?.driverName || profile?.name || '', 
      phone: profile?.phone || '', 
      email: profile?.email || '', 
      address: profile?.address || '' 
    })
  }

  const avgScore = ratings.length
    ? ratings.reduce((s, r) => s + (r.score || 0), 0) / ratings.length
    : profile?.score || profile?.rating || 0

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
        className="mx-auto w-full max-w-4xl space-y-8 p-5 pb-12 lg:p-8"
      >
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <h1 className="font-display text-3xl font-bold text-gray-900 dark:text-white tracking-tight">Hồ sơ tài xế</h1>
            <p className="text-sm text-gray-500 dark:text-gray-400 mt-1">Quản lý thông tin cá nhân và phương tiện hoạt động</p>
          </div>
          {!editing && (
            <div className="flex gap-2">
              <Button variant="outline" size="sm" onClick={() => setShowPasswordModal(true)} className="rounded-xl border-gray-200 dark:border-surface-border font-bold text-gray-700 dark:text-white hover:border-gray-400">
                <RiKeyLine size={16} /> Đổi mật khẩu
              </Button>
              <Button size="sm" onClick={() => setEditing(true)} className="rounded-xl bg-brand-500 font-bold text-white hover:bg-brand-600 shadow-lg shadow-brand-500/20">
                <RiEditLine size={16} /> Chỉnh sửa
              </Button>
            </div>
          )}
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          {/* Left Column: Driver Info */}
          <div className="space-y-6">
            <div className="rounded-2xl border border-gray-100 dark:border-surface-border bg-white dark:bg-surface-card p-6 flex flex-col items-center text-center shadow-sm relative overflow-hidden">
              <div className="absolute top-0 left-0 right-0 h-28 bg-gradient-to-r from-brand-600 to-brand-400" />
              <div className="relative mb-4 mt-6">
                <div className="h-32 w-32 rounded-full border-4 border-white dark:border-surface-card bg-gray-100 dark:bg-gray-800 overflow-hidden flex items-center justify-center text-5xl font-bold text-brand-500 shadow-md">
                  {avatarSrc
                    ? <img src={avatarSrc} alt="avatar" className="w-full h-full object-cover" />
                    : (profile?.driverName?.[0] || profile?.name?.[0] || 'D')
                  }
                </div>
                {editing && (
                  <>
                    <button onClick={() => fileRef.current?.click()}
                      className="absolute bottom-1 right-1 grid h-10 w-10 place-items-center rounded-full bg-brand-500 text-white shadow-lg hover:scale-105 transition-transform"
                    >
                      <RiCameraLine size={18} />
                    </button>
                    <input ref={fileRef} type="file" accept="image/*" className="sr-only" onChange={handleFileChange} />
                  </>
                )}
              </div>
              <div className="text-center">
                <h2 className="font-display text-2xl font-bold text-gray-900 dark:text-white">{profile?.driverName || profile?.name}</h2>
                {avgScore > 0 ? (
                  <div className="flex flex-col items-center mt-2 gap-1">
                    <div className="flex items-center gap-2">
                      <span className="font-bold text-2xl text-gray-900 dark:text-white">{avgScore.toFixed(1)}</span>
                      <div className="flex text-yellow-400">
                        {[1, 2, 3, 4, 5].map((star) => (
                          <RiStarFill
                            key={star}
                            size={20}
                            className={star <= Math.round(avgScore) ? "text-yellow-400" : "text-gray-300 dark:text-surface-muted"}
                          />
                        ))}
                      </div>
                    </div>
                    <span className="text-sm font-medium text-gray-500">
                      {ratings.length > 0 ? `(Dựa trên ${ratings.length} đánh giá)` : '(Chưa có đánh giá mới)'}
                    </span>
                  </div>
                ) : (
                  <div className="flex flex-col items-center mt-2 gap-1">
                    <div className="flex text-gray-300 dark:text-surface-muted">
                      {[1, 2, 3, 4, 5].map((star) => (
                        <RiStarFill key={star} size={20} />
                      ))}
                    </div>
                    <span className="text-sm font-medium text-gray-500">Chưa có đánh giá</span>
                  </div>
                )}
              </div>
            </div>

            {/* Editable info */}
            <div className="rounded-2xl border border-gray-100 dark:border-surface-border bg-white dark:bg-surface-card p-6 shadow-sm">
              <h3 className="font-bold text-lg mb-6 text-gray-900 dark:text-white">Thông tin cá nhân</h3>
              <div className="space-y-5">
                {[
                  { key: 'driverName', label: 'Họ và tên',      placeholder: 'Nguyễn Văn A' },
                  { key: 'phone',      label: 'Số điện thoại',  placeholder: '0912345678'   },
                  { key: 'email',      label: 'Email',           placeholder: 'a@gmail.com'  },
                  { key: 'address',    label: 'Địa chỉ',         placeholder: '123 Đường...' },
                ].map(({ key, label, placeholder }) => (
                  <FormField key={key} label={label}>
                    {editing
                      ? <Input value={form[key] || ''} onChange={(e) => setForm({ ...form, [key]: e.target.value })} placeholder={placeholder} className="!rounded-xl !bg-gray-50 dark:!bg-surface-dark !border-gray-200 dark:!border-surface-border" />
                      : <p className="text-gray-900 dark:text-white font-bold py-2 border-b border-gray-100 dark:border-surface-border/50">{profile?.[key] || '—'}</p>
                    }
                  </FormField>
                ))}
              </div>

              {editing && (
                <div className="mt-8 flex gap-3 pt-6 border-t border-gray-100 dark:border-surface-border">
                  <Button variant="outline" className="flex-1 rounded-xl h-12 font-bold border-gray-200 dark:border-surface-border text-gray-700 dark:text-white" onClick={handleCancel}>
                    Hủy thay đổi
                  </Button>
                  <Button className="flex-1 rounded-xl h-12 bg-brand-500 text-white font-bold hover:bg-brand-600 shadow-lg shadow-brand-500/20" onClick={handleSave} loading={saving}>
                    Lưu thông tin
                  </Button>
                </div>
              )}
            </div>
          </div>

          {/* Right Column: Vehicle & Ratings */}
          <div className="space-y-6">
            {/* Vehicle info */}
            <div className="rounded-2xl border border-gray-100 dark:border-surface-border bg-white dark:bg-surface-card p-6 shadow-sm">
              <div className="flex items-center gap-3 mb-6">
                <div className="w-10 h-10 rounded-xl bg-brand-50 dark:bg-brand-500/10 flex items-center justify-center">
                  <RiCarLine size={24} className="text-brand-500" />
                </div>
                <h3 className="font-bold text-lg text-gray-900 dark:text-white">Phương tiện đăng ký</h3>
              </div>

              {/* Vehicle highlight card */}
              <div className="flex items-center gap-4 bg-brand-50 dark:bg-brand-500/5 border border-brand-100 dark:border-brand-500/15 rounded-2xl p-5 mb-6">
                <div className="w-14 h-14 rounded-2xl bg-white dark:bg-brand-500/15 shadow-sm flex items-center justify-center shrink-0">
                  <RiMotorbikeLine size={28} className="text-brand-500" />
                </div>
                <div className="flex-1 min-w-0">
                  <p className="font-bold text-gray-900 dark:text-white text-lg">{profile?.vehicleName || 'Chưa cập nhật'}</p>
                  <p className="text-gray-500 dark:text-gray-400 text-sm font-medium mt-0.5">{profile?.vehicleTypeName || 'Loại xe chưa xác định'}</p>
                </div>
                <div className="shrink-0 text-right">
                  <span className="inline-block bg-white dark:bg-surface-dark text-gray-900 dark:text-white font-mono font-bold text-base px-3 py-1.5 rounded-xl border border-gray-200 dark:border-surface-border shadow-sm tracking-widest">
                    {profile?.licensePlate || '---'}
                  </span>
                  <p className="text-xs text-gray-500 font-medium mt-1">Biển số</p>
                </div>
              </div>

              {/* Details grid */}
              <div className="grid grid-cols-2 gap-x-6 gap-y-6">
                <div className="flex items-start gap-3">
                  <div className="w-10 h-10 rounded-xl bg-gray-50 dark:bg-surface-dark flex items-center justify-center shrink-0">
                    <RiFileTextLine size={20} className="text-gray-500" />
                  </div>
                  <div>
                    <p className="text-gray-500 text-xs font-bold uppercase tracking-wider mb-1">Số bằng lái</p>
                    <p className="text-gray-900 dark:text-white font-bold">{profile?.drivingLicense || '—'}</p>
                  </div>
                </div>

                <div className="flex items-start gap-3">
                  <div className="w-10 h-10 rounded-xl bg-gray-50 dark:bg-surface-dark flex items-center justify-center shrink-0">
                    <RiIdCardLine size={20} className="text-gray-500" />
                  </div>
                  <div>
                    <p className="text-gray-500 text-xs font-bold uppercase tracking-wider mb-1">CMND/CCCD</p>
                    <p className="text-gray-900 dark:text-white font-bold">{profile?.citizenId || '—'}</p>
                  </div>
                </div>

                <div className="flex items-start gap-3">
                  <div className="w-10 h-10 rounded-xl bg-gray-50 dark:bg-surface-dark flex items-center justify-center shrink-0">
                    <RiMapPinLine size={20} className="text-gray-500" />
                  </div>
                  <div>
                    <p className="text-gray-500 text-xs font-bold uppercase tracking-wider mb-1">Khu vực hoạt động</p>
                    <p className="text-gray-900 dark:text-white font-bold">{profile?.area || '—'}</p>
                  </div>
                </div>

                <div className="flex items-start gap-3">
                  <div className="w-10 h-10 rounded-xl bg-gray-50 dark:bg-surface-dark flex items-center justify-center shrink-0">
                    <RiShieldCheckLine size={20} className="text-gray-500" />
                  </div>
                  <div>
                    <p className="text-gray-500 text-xs font-bold uppercase tracking-wider mb-1">Lý lịch tư pháp</p>
                    <p className="text-gray-900 dark:text-white font-bold">{profile?.criminalRecord || '—'}</p>
                  </div>
                </div>
              </div>
            </div>

            {/* Recent ratings */}
            {ratings.length > 0 && (
              <div className="rounded-2xl border border-gray-100 dark:border-surface-border bg-white dark:bg-surface-card p-6 shadow-sm">
                <h3 className="font-bold text-lg text-gray-900 dark:text-white mb-4">Đánh giá gần đây</h3>
                <div className="space-y-4">
                  {ratings.slice(0, 5).map((r, index) => (
                    <div key={r.id} className={cn("pb-4", index !== ratings.slice(0, 5).length - 1 && "border-b border-gray-100 dark:border-surface-border/50")}>
                      <div className="flex items-center justify-between mb-2">
                        <div className="flex items-center gap-1">
                          {[1,2,3,4,5].map((s) => (
                            <RiStarFill key={s} size={16} className={s <= (r.score || 0) ? 'text-yellow-400' : 'text-gray-200 dark:text-surface-muted'} />
                          ))}
                        </div>
                        <span className="text-sm font-medium text-gray-500">{formatDate(r.createdAt, 'dd/MM/yyyy')}</span>
                      </div>
                      {r.review && <p className="text-gray-700 dark:text-gray-300 font-medium">"{r.review}"</p>}
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        </div>
      </motion.div>

      {showPasswordModal && (
        <ChangePasswordModal onClose={() => setShowPasswordModal(false)} />
      )}
    </div>
  )
}

export default DriverProfilePage
