import { useState, useEffect, useRef } from 'react'
import toast from 'react-hot-toast'
import {
  RiCameraLine, RiEditLine, RiSaveLine, RiCloseLine, RiStarFill, RiStarLine,
  RiCarLine, RiMapPinLine, RiFileTextLine, RiIdCardLine, RiShieldCheckLine, RiMotorbikeLine
} from 'react-icons/ri'
import { useAuthStore } from '@/store/rootStore'
import { driverApi } from '@/features/driver/api/driverApi'
import { ratingApi } from '@/features/booking/api/masterDataApi'
import Button from '@/components/Elements/Button'
import Input from '@/components/Elements/Input'
import FormField from '@/components/Form/FormField'
import Spinner from '@/components/Elements/Spinner'
import { formatDate } from '@/utils/formatDate'

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
      const updated = await driverApi.updateDriver(user?.id, fd)
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

  const avgScore = ratings.length
    ? ratings.reduce((s, r) => s + (r.score || 0), 0) / ratings.length
    : profile?.score || profile?.rating || 0

  if (loading) return <div className="flex justify-center py-16"><Spinner size="xl" /></div>

  const avatarSrc = preview || profile?.avatar

  return (
    <div className="max-w-5xl mx-auto space-y-8">
      <div className="flex items-center justify-between">
        <h1 className="section-title">Hồ sơ tài xế</h1>
        {!editing && (
          <Button variant="outline" size="sm" onClick={() => setEditing(true)}>
            <RiEditLine size={16} /> Chỉnh sửa
          </Button>
        )}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        {/* Left Column: Driver Info */}
        <div className="space-y-6">
          <div className="card p-6 flex flex-col items-center gap-4">
            <div className="relative">
              <div className="w-32 h-32 rounded-full bg-brand-500/20 border-4 border-brand-500/30 overflow-hidden flex items-center justify-center text-5xl font-bold text-brand-400">
                {avatarSrc
                  ? <img src={avatarSrc} alt="avatar" className="w-full h-full object-cover" />
                  : (profile?.driverName?.[0] || profile?.name?.[0] || 'D')
                }
              </div>
              {editing && (
                <>
                  <button onClick={() => fileRef.current?.click()}
                    className="absolute bottom-0 right-0 w-10 h-10 rounded-full bg-brand-500 flex items-center justify-center shadow-glow-green hover:bg-brand-400 transition-colors"
                  >
                    <RiCameraLine size={20} className="text-content-main" />
                  </button>
                  <input ref={fileRef} type="file" accept="image/*" className="sr-only" onChange={handleFileChange} />
                </>
              )}
            </div>
            <div className="text-center">
              <h2 className="font-display text-2xl font-bold text-content-main">{profile?.driverName || profile?.name}</h2>
              {avgScore > 0 ? (
                <div className="flex flex-col items-center mt-2 gap-1">
                  <div className="flex items-center gap-2">
                    <span className="font-bold text-xl text-content-main">{avgScore.toFixed(1)}</span>
                    <div className="flex text-yellow-400">
                      {[1, 2, 3, 4, 5].map((star) => (
                        <RiStarFill
                          key={star}
                          size={18}
                          className={star <= Math.round(avgScore) ? "text-yellow-400" : "text-surface-muted"}
                        />
                      ))}
                    </div>
                  </div>
                  <span className="text-xs text-content-muted">
                    {ratings.length > 0 ? `(Dựa trên ${ratings.length} đánh giá)` : '(Chưa có đánh giá mới)'}
                  </span>
                </div>
              ) : (
                <div className="flex flex-col items-center mt-2 gap-1">
                  <div className="flex text-surface-muted">
                    {[1, 2, 3, 4, 5].map((star) => (
                      <RiStarFill key={star} size={18} />
                    ))}
                  </div>
                  <span className="text-xs text-content-muted">Chưa có đánh giá</span>
                </div>
              )}
            </div>
          </div>

          {/* Editable info */}
          <div className="card p-6 space-y-5">
            {[
              { key: 'driverName', label: 'Họ và tên',      placeholder: 'Nguyễn Văn A' },
              { key: 'phone',      label: 'Số điện thoại',  placeholder: '0912345678'   },
              { key: 'email',      label: 'Email',           placeholder: 'a@gmail.com'  },
              { key: 'address',    label: 'Địa chỉ',         placeholder: '123 Đường...' },
            ].map(({ key, label, placeholder }) => (
              <FormField key={key} label={label}>
                {editing
                  ? <Input value={form[key] || ''} onChange={(e) => setForm({ ...form, [key]: e.target.value })} placeholder={placeholder} />
                  : <p className="text-content-main font-medium py-2">{profile?.[key] || '—'}</p>
                }
              </FormField>
            ))}

            {editing && (
              <div className="flex gap-4 pt-2">
                <Button variant="outline" fullWidth onClick={() => { setEditing(false); setNewFile(null); setPreview(null) }}>
                  <RiCloseLine size={16} /> Hủy
                </Button>
                <Button fullWidth onClick={handleSave} loading={saving}>
                  <RiSaveLine size={16} /> Lưu thay đổi
                </Button>
              </div>
            )}
          </div>
        </div>

        {/* Right Column: Vehicle & Ratings */}
        <div className="space-y-6">
          {/* Vehicle info */}
          <div className="card p-5 space-y-5">
            <div className="flex items-center gap-3">
              <div className="w-9 h-9 rounded-xl bg-brand-500/10 flex items-center justify-center">
                <RiCarLine size={20} className="text-brand-400" />
              </div>
              <h3 className="font-semibold text-content-main">Thông tin phương tiện</h3>
            </div>

            {/* Vehicle highlight card */}
            <div className="flex items-center gap-4 bg-brand-500/5 border border-brand-500/15 rounded-2xl p-4">
              <div className="w-14 h-14 rounded-2xl bg-brand-500/15 flex items-center justify-center shrink-0">
                <RiMotorbikeLine size={28} className="text-brand-400" />
              </div>
              <div className="flex-1 min-w-0">
                <p className="font-bold text-content-main text-base">{profile?.vehicleName || 'Chưa cập nhật'}</p>
                <p className="text-content-muted text-sm mt-0.5">{profile?.vehicleTypeName || 'Loại xe chưa xác định'}</p>
              </div>
              <div className="shrink-0 text-right">
                <span className="inline-block bg-surface-border text-content-main font-mono font-bold text-sm px-3 py-1 rounded-lg border border-surface-border tracking-widest">
                  {profile?.licensePlate || '---'}
                </span>
                <p className="text-xs text-content-muted mt-1">Biển số</p>
              </div>
            </div>

            {/* Details grid */}
            <div className="grid grid-cols-2 gap-x-4 gap-y-4">
              <div className="flex items-start gap-2.5">
                <div className="w-7 h-7 rounded-lg bg-surface-border flex items-center justify-center shrink-0 mt-0.5">
                  <RiFileTextLine size={14} className="text-content-muted" />
                </div>
                <div>
                  <p className="text-content-muted text-xs">Số bằng lái</p>
                  <p className="text-content-main font-medium text-sm mt-0.5">{profile?.drivingLicense || '—'}</p>
                </div>
              </div>

              <div className="flex items-start gap-2.5">
                <div className="w-7 h-7 rounded-lg bg-surface-border flex items-center justify-center shrink-0 mt-0.5">
                  <RiIdCardLine size={14} className="text-content-muted" />
                </div>
                <div>
                  <p className="text-content-muted text-xs">CMND/CCCD</p>
                  <p className="text-content-main font-medium text-sm mt-0.5">{profile?.citizenId || '—'}</p>
                </div>
              </div>

              <div className="flex items-start gap-2.5">
                <div className="w-7 h-7 rounded-lg bg-surface-border flex items-center justify-center shrink-0 mt-0.5">
                  <RiMapPinLine size={14} className="text-content-muted" />
                </div>
                <div>
                  <p className="text-content-muted text-xs">Khu vực hoạt động</p>
                  <p className="text-content-main font-medium text-sm mt-0.5">{profile?.area || '—'}</p>
                </div>
              </div>

              <div className="flex items-start gap-2.5">
                <div className="w-7 h-7 rounded-lg bg-surface-border flex items-center justify-center shrink-0 mt-0.5">
                  <RiShieldCheckLine size={14} className="text-content-muted" />
                </div>
                <div>
                  <p className="text-content-muted text-xs">Lý lịch tư pháp</p>
                  <p className="text-content-main font-medium text-sm mt-0.5">{profile?.criminalRecord || '—'}</p>
                </div>
              </div>
            </div>
          </div>

          {/* Recent ratings */}
          {ratings.length > 0 && (
            <div className="space-y-4">
              <h3 className="font-display text-lg font-bold text-content-main">Đánh giá gần đây</h3>
              <div className="space-y-3">
                {ratings.slice(0, 5).map((r) => (
                  <div key={r.id} className="card p-4 space-y-2">
                    <div className="flex items-center justify-between">
                      <div className="flex items-center gap-1">
                        {[1,2,3,4,5].map((s) => (
                          <RiStarFill key={s} size={12} className={s <= (r.score || 0) ? 'text-yellow-400' : 'text-surface-muted'} />
                        ))}
                      </div>
                      <span className="text-xs text-gray-600">{formatDate(r.createdAt, 'dd/MM/yyyy')}</span>
                    </div>
                    {r.review && <p className="text-sm text-content-muted italic">"{r.review}"</p>}
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

export default DriverProfilePage
