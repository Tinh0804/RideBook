import React from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { RiCloseLine } from 'react-icons/ri'
import Input from '@/components/Elements/Input'
import Button from '@/components/Elements/Button'
import FormField from '@/components/Form/FormField'
import toast from 'react-hot-toast'

const schema = z.object({
  newPassword: z.string().min(6, 'Mật khẩu phải từ 6 ký tự trở lên'),
  confirmPassword: z.string()
}).refine(data => data.newPassword === data.confirmPassword, {
  message: "Mật khẩu xác nhận không khớp",
  path: ["confirmPassword"]
})

const AdminChangePasswordModal = ({ open, onClose, onSubmit, targetName }) => {
  const { register, handleSubmit, formState: { errors, isSubmitting }, reset } = useForm({
    resolver: zodResolver(schema),
    defaultValues: { newPassword: '', confirmPassword: '' }
  })

  React.useEffect(() => {
    if (open) reset()
  }, [open, reset])

  const handleFormSubmit = async (data) => {
    try {
      await onSubmit({ newPassword: data.newPassword })
      toast.success('Đổi mật khẩu thành công!')
      onClose()
    } catch (err) {
      toast.error(err?.response?.data?.message || 'Lỗi khi đổi mật khẩu')
    }
  }

  if (!open) return null

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4">
      <div className="card w-full max-w-md p-6 max-h-[90vh] overflow-y-auto animate-in zoom-in-95 shadow-2xl">
        <h3 className="text-xl font-bold text-content-main mb-2">Đổi Mật Khẩu</h3>
        {targetName && <p className="text-sm text-content-muted mb-6">Cho: {targetName}</p>}

        <form onSubmit={handleSubmit(handleFormSubmit)} className="space-y-4">
          <FormField label="Mật khẩu mới" error={errors.newPassword?.message}>
            <Input type="password" placeholder="Nhập mật khẩu mới" {...register('newPassword')} error={errors.newPassword} />
          </FormField>
          <FormField label="Xác nhận mật khẩu" error={errors.confirmPassword?.message}>
            <Input type="password" placeholder="Nhập lại mật khẩu mới" {...register('confirmPassword')} error={errors.confirmPassword} />
          </FormField>

          <div className="flex gap-3 justify-end pt-4 border-t border-surface-border">
            <Button variant="outline" onClick={onClose} disabled={isSubmitting} type="button">Hủy</Button>
            <Button type="submit" loading={isSubmitting}>Đổi mật khẩu</Button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default AdminChangePasswordModal
