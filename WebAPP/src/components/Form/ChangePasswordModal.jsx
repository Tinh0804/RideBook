import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import toast from 'react-hot-toast'
import Button from '@/components/Elements/Button'
import Input from '@/components/Elements/Input'
import FormField from '@/components/Form/FormField'
import { authApi } from '@/features/auth/api/authApi'

const changePasswordSchema = z.object({
  oldPassword: z.string().min(1, 'Vui lòng nhập mật khẩu cũ'),
  newPassword: z.string().min(6, 'Mật khẩu mới tối thiểu 6 ký tự'),
  confirm: z.string()
}).refine(data => data.newPassword === data.confirm, {
  message: 'Mật khẩu xác nhận không khớp',
  path: ['confirm']
})

const ChangePasswordModal = ({ onClose }) => {
  const [loading, setLoading] = useState(false)
  const { register, handleSubmit, formState: { errors } } = useForm({
    resolver: zodResolver(changePasswordSchema)
  })

  const onSubmit = async (data) => {
    setLoading(true)
    try {
      await authApi.changePassword({ 
        oldPassword: data.oldPassword, 
        newPassword: data.newPassword 
      })
      toast.success('Đổi mật khẩu thành công!')
      onClose()
    } catch (err) {
      toast.error(err?.response?.data?.message || 'Có lỗi xảy ra khi đổi mật khẩu')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
      <div className="bg-white dark:bg-gray-800 rounded-xl shadow-xl w-full max-w-md p-6">
        <h3 className="text-xl font-bold text-gray-900 dark:text-white mb-4">Đổi mật khẩu</h3>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <FormField label="Mật khẩu hiện tại" error={errors.oldPassword?.message} required>
            <Input
              type="password"
              placeholder="Nhập mật khẩu hiện tại"
              {...register('oldPassword')}
              error={errors.oldPassword}
            />
          </FormField>
          
          <FormField label="Mật khẩu mới" error={errors.newPassword?.message} required>
            <Input
              type="password"
              placeholder="Tối thiểu 6 ký tự"
              {...register('newPassword')}
              error={errors.newPassword}
            />
          </FormField>
          
          <FormField label="Xác nhận mật khẩu mới" error={errors.confirm?.message} required>
            <Input
              type="password"
              placeholder="Nhập lại mật khẩu mới"
              {...register('confirm')}
              error={errors.confirm}
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

export default ChangePasswordModal
