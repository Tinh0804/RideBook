import { useState, useEffect, useRef } from 'react'
import toast from 'react-hot-toast'
import { RiCameraLine, RiEditLine, RiSaveLine, RiCloseLine, RiStarFill } from 'react-icons/ri'
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

  const avgRating = ratings.length
    ? (ratings.reduce((s, r) => s + (r.ratingValue || 0), 0) / ratings.length).toFixed(1)
    : null

  if (loading) return <div className="flex justify-center py-16"><Spinner size="xl" /></div>

  const avatarSrc = preview || profile?.avatar

  return (
    <div className="max-w-lg space-y-8">
      <div className="flex items-center justify-between">
        <h1 className="section-title">Hồ sơ tài xế</h1>
        {!editing && (
          <Button variant="outline" size="sm" onClick={() => setEditing(true)}>
            <RiEditLine size={16} /> Chỉnh sửa
          </Button>
        )}
      </div>

      {/* Avatar + name */}
      <div className="flex flex-col items-center gap-4">
        <div className="relative">
          <div className="w-24 h-24 rounded-full bg-brand-500/20 border-2 border-brand-500/30 overflow-hidden flex items-center justify-center text-4xl font-bold text-brand-400">
            {avatarSrc
              ? <img src={avatarSrc} alt="avatar" className="w-full h-full object-cover" />
              : (profile?.driverName?.[0] || profile?.name?.[0] || 'D')
            }
          </div>
          {editing && (
            <>
              <button onClick={() => fileRef.current?.click()}
                className="absolute bottom-0 right-0 w-8 h-8 rounded-full bg-brand-500 flex items-center justify-center shadow-glow-green hover:bg-brand-400 transition-colors"
              >
                <RiCameraLine size={16} className="text-content-main" />
              </button>
              <input ref={fileRef} type="file" accept="image/*" className="sr-only" onChange={handleFileChange} />
            </>
          )}
        </div>
        <div className="text-center">
          <h2 className="font-display text-xl font-bold text-content-main">{profile?.driverName || profile?.name}</h2>
          {avgRating && (
            <div className="flex items-center justify-center gap-1 mt-1">
              <RiStarFill size={14} className="text-yellow-400" />
              <span className="text-sm font-semibold text-yellow-400">{avgRating}</span>
              <span className="text-xs text-content-muted">({ratings.length} đánh giá)</span>
            </div>
          )}
        </div>
      </div>

      {/* Vehicle info */}
      <div className="card p-5 space-y-3">
        <h3 className="font-semibold text-content-main text-sm">Thông tin xe</h3>
        <div className="grid grid-cols-2 gap-3 text-sm">
          <div>
            <p className="text-content-muted text-xs">Tên xe</p>
            <p className="text-content-main font-medium mt-0.5">{profile?.vehicleName || '—'}</p>
          </div>
          <div>
            <p className="text-content-muted text-xs">Biển số</p>
            <p className="text-content-main font-mono font-medium mt-0.5">{profile?.licensePlate || '—'}</p>
          </div>
          <div>
            <p className="text-content-muted text-xs">Số bằng lái</p>
            <p className="text-content-main font-medium mt-0.5">{profile?.drivingLicense || '—'}</p>
          </div>
          <div>
            <p className="text-content-muted text-xs">Khu vực</p>
            <p className="text-content-main font-medium mt-0.5">{profile?.area || '—'}</p>
          </div>
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
      </div>

      {editing && (
        <div className="flex gap-3">
          <Button variant="outline" fullWidth onClick={() => { setEditing(false); setNewFile(null); setPreview(null) }}>
            <RiCloseLine size={16} /> Hủy
          </Button>
          <Button fullWidth onClick={handleSave} loading={saving}>
            <RiSaveLine size={16} /> Lưu
          </Button>
        </div>
      )}

      {/* Recent ratings */}
      {ratings.length > 0 && (
        <div className="space-y-3">
          <h3 className="font-display text-lg font-bold text-content-main">Đánh giá gần đây</h3>
          <div className="space-y-2">
            {ratings.slice(0, 5).map((r) => (
              <div key={r.id} className="card p-4 space-y-2">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-1">
                    {[1,2,3,4,5].map((s) => (
                      <RiStarFill key={s} size={12} className={s <= r.ratingValue ? 'text-yellow-400' : 'text-surface-muted'} />
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
  )
}

export default DriverProfilePage
