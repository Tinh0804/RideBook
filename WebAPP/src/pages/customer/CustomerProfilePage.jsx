import { useState, useEffect, useRef } from 'react'
import toast from 'react-hot-toast'
import { RiCameraLine, RiEditLine, RiSaveLine, RiCloseLine } from 'react-icons/ri'
import { useAuthStore } from '@/store/rootStore'
import { customerApi } from '@/features/customer/api/customerApi'
import Button from '@/components/Elements/Button'
import Input from '@/components/Elements/Input'
import FormField from '@/components/Form/FormField'
import Spinner from '@/components/Elements/Spinner'

const CustomerProfilePage = () => {
  const { user, updateUser } = useAuthStore()
  const fileRef = useRef()

  const [profile,  setProfile]  = useState(null)
  const [editing,  setEditing]  = useState(false)
  const [loading,  setLoading]  = useState(true)
  const [saving,   setSaving]   = useState(false)
  const [form,     setForm]     = useState({})
  const [preview,  setPreview]  = useState(null)
  const [newFile,  setNewFile]  = useState(null)

  useEffect(() => {
    customerApi.getMyInfo()
      .then((info) => {
        setProfile(info)
        if (info) {
          setForm({ name: info.name || '', phoneNumber: info.phoneNumber || '', address: info.address || '' })
        }
      })
      .catch(() => {})
      .finally(() => setLoading(false))
  }, [])

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
      fd.append('name',        form.name)
      fd.append('phoneNumber', form.phoneNumber)
      fd.append('address',     form.address)
      if (newFile) fd.append('avatar', newFile)

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
    setForm({ name: profile?.name || '', phoneNumber: profile?.phoneNumber || '', address: profile?.address || '' })
  }

  if (loading) return (
    <div className="flex justify-center py-16"><Spinner size="xl" /></div>
  )

  const avatarSrc = preview || profile?.avatar

  return (
    <div className="max-w-lg space-y-8">
      <div className="flex items-center justify-between">
        <h1 className="section-title">Hồ sơ cá nhân</h1>
        {!editing && (
          <Button variant="outline" size="sm" onClick={() => setEditing(true)}>
            <RiEditLine size={16} /> Chỉnh sửa
          </Button>
        )}
      </div>

      {/* Avatar */}
      <div className="flex flex-col items-center gap-4">
        <div className="relative">
          <div className="w-24 h-24 rounded-full bg-brand-500/20 border-2 border-brand-500/30 overflow-hidden flex items-center justify-center text-4xl font-bold text-brand-400">
            {avatarSrc
              ? <img src={avatarSrc} alt="avatar" className="w-full h-full object-cover" />
              : (profile?.name?.[0] || user?.userName?.[0] || 'U')
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
          <h2 className="font-display text-xl font-bold text-content-main">{profile?.name}</h2>
          <p className="text-content-muted text-sm">@{profile?.userName || user?.userName}</p>
        </div>
      </div>

      {/* Profile fields */}
      <div className="card p-6 space-y-5">
        <FormField label="Họ và tên">
          {editing
            ? <Input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} placeholder="Nguyễn Văn A" />
            : <p className="text-content-main font-medium py-2">{profile?.name || '—'}</p>
          }
        </FormField>

        <FormField label="Số điện thoại">
          {editing
            ? <Input value={form.phoneNumber} onChange={(e) => setForm({ ...form, phoneNumber: e.target.value })} placeholder="0912345678" />
            : <p className="text-content-main font-medium py-2">{profile?.phoneNumber || '—'}</p>
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
