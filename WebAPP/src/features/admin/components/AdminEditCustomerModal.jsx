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
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
      <div className="bg-white dark:bg-gray-800 rounded-xl shadow-xl w-full max-w-lg p-6 max-h-[90vh] overflow-y-auto animate-in zoom-in-95">
        <h3 className="text-xl font-bold text-gray-900 dark:text-white mb-6">Sửa thông tin Khách hàng</h3>
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
                className="w-full h-11 px-4 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-900 focus:ring-2 focus:ring-primary/20 focus:border-primary outline-none transition-all dark:text-white"
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
              className="w-full text-sm text-gray-500 file:mr-4 file:py-2 file:px-4 file:rounded-full file:border-0 file:text-sm file:font-semibold file:bg-primary/10 file:text-primary hover:file:bg-primary/20 transition-colors"
            />
          </FormField>

          <div className="flex gap-3 justify-end pt-4">
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
