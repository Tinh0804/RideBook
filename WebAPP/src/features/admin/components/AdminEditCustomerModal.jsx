import { useState, useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import toast from 'react-hot-toast'
import Button from '@/components/Elements/Button'
import Input from '@/components/Elements/Input'
import FormField from '@/components/Form/FormField'

const adminEditCustomerSchema = z.object({
  customerName: z.string().min(2, 'Tên ít nhất 2 ký tự').optional().or(z.literal('')),
  email: z.string().email('Email không hợp lệ').optional().or(z.literal('')),
  address: z.string().optional().or(z.literal('')),
  gender: z.string().optional().or(z.literal('')),
  birthDate: z.string().optional().or(z.literal('')),
})

const AdminEditCustomerModal = ({ open, onClose, onSubmit, customer }) => {
  const [loading, setLoading] = useState(false)
  const [avatarFile, setAvatarFile] = useState(null)
  
  const { register, handleSubmit, formState: { errors }, reset, setValue } = useForm({
    resolver: zodResolver(adminEditCustomerSchema)
  })

  useEffect(() => {
    if (customer && open) {
      reset({
        customerName: customer.customerName || '',
        email: customer.email || '',
        address: customer.address || '',
        gender: customer.gender || '',
        birthDate: customer.birthDate || '',
      })
      setAvatarFile(null)
    }
  }, [customer, open, reset])

  const handleFormSubmit = async (data) => {
    setLoading(true)
    try {
      const formData = new FormData()
      if (data.customerName) formData.append('customerName', data.customerName)
      if (data.email) formData.append('email', data.email)
      if (data.address) formData.append('address', data.address)
      if (data.gender) formData.append('gender', data.gender)
      if (data.birthDate) formData.append('birthDate', data.birthDate)
      
      if (avatarFile) {
        formData.append('avatar', avatarFile)
      }

      await onSubmit(formData)
      toast.success('Cập nhật thông tin thành công!')
      onClose()
    } catch (err) {
      toast.error(err?.response?.data?.message || 'Có lỗi xảy ra khi cập nhật')
    } finally {
      setLoading(false)
    }
  }

  if (!open) return null

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4">
      <div className="card w-full max-w-lg p-6 max-h-[90vh] overflow-y-auto animate-in zoom-in-95 shadow-2xl">
        <h3 className="text-xl font-bold text-content-main mb-6">Sửa thông tin Khách hàng</h3>
        <form onSubmit={handleSubmit(handleFormSubmit)} className="space-y-4">
          <FormField label="Tên khách hàng" error={errors.customerName?.message}>
            <Input
              type="text"
              placeholder="Nhập tên"
              {...register('customerName')}
              error={errors.customerName}
            />
          </FormField>
          
          <FormField label="Email" error={errors.email?.message}>
            <Input
              type="email"
              placeholder="Nhập email"
              {...register('email')}
              error={errors.email}
            />
          </FormField>

          <FormField label="Địa chỉ" error={errors.address?.message}>
            <Input
              type="text"
              placeholder="Nhập địa chỉ"
              {...register('address')}
              error={errors.address}
            />
          </FormField>

          <div className="grid grid-cols-2 gap-4">
            <FormField label="Giới tính" error={errors.gender?.message}>
              <select 
                {...register('gender')}
                className="w-full h-11 px-4 rounded-xl border border-surface-border bg-surface-dark text-content-main focus:ring-2 focus:ring-brand-500/20 focus:border-brand-500 outline-none transition-all"
              >
                <option value="">Chọn giới tính</option>
                <option value="Nam">Nam</option>
                <option value="Nữ">Nữ</option>
                <option value="Khác">Khác</option>
              </select>
            </FormField>

            <FormField label="Ngày sinh" error={errors.birthDate?.message}>
              <Input
                type="date"
                {...register('birthDate')}
                error={errors.birthDate}
              />
            </FormField>
          </div>

          <FormField label="Ảnh đại diện">
            <input 
              type="file" 
              accept="image/*"
              onChange={(e) => {
                if (e.target.files && e.target.files[0]) {
                  setAvatarFile(e.target.files[0])
                }
              }}
              className="w-full text-sm text-content-muted file:mr-4 file:py-2 file:px-4 file:rounded-full file:border-0 file:text-sm file:font-semibold file:bg-brand-500/10 file:text-brand-500 hover:file:bg-brand-500/20 transition-colors cursor-pointer"
            />
          </FormField>

          <div className="flex gap-3 justify-end pt-4 border-t border-surface-border">
            <Button variant="outline" onClick={onClose} disabled={loading} type="button">
              Hủy
            </Button>
            <Button type="submit" loading={loading}>
              Lưu thay đổi
            </Button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default AdminEditCustomerModal
