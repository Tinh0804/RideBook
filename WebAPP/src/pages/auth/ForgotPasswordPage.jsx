import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import toast from 'react-hot-toast'
import { authApi } from '@/features/auth/api/authApi'
import Button from '@/components/Elements/Button'
import Input from '@/components/Elements/Input'
import FormField from '@/components/Form/FormField'

const checkSchema = z.object({
  phone: z.string().regex(/^(0|\+84)[0-9]{9}$/, 'Số điện thoại không hợp lệ'),
})

const resetSchema = z.object({
  newPassword: z.string().min(6, 'Mật khẩu tối thiểu 6 ký tự'),
  confirm:     z.string(),
}).refine((d) => d.newPassword === d.confirm, {
  message: 'Mật khẩu xác nhận không khớp',
  path:    ['confirm'],
})

const ForgotPasswordPage = () => {
  const [step, setStep]   = useState('check')  // 'check' | 'reset'
  const [phone, setPhone] = useState('')
  const [loading, setLoading] = useState(false)

  const checkForm = useForm({ resolver: zodResolver(checkSchema) })
  const resetForm = useForm({ resolver: zodResolver(resetSchema) })

  const onCheck = async (data) => {
    setLoading(true)
    try {
      await authApi.checkPhone(data.phone)
      setPhone(data.phone)
      setStep('reset')
      toast.success('Số điện thoại hợp lệ, đặt lại mật khẩu mới')
    } catch {
      toast.error('Số điện thoại không tồn tại trong hệ thống')
    } finally {
      setLoading(false)
    }
  }

  const onReset = async (data) => {
    setLoading(true)
    try {
      await authApi.resetPassword({ phone, newPassword: data.newPassword })
      toast.success('Đặt lại mật khẩu thành công!')
      setStep('done')
    } catch (err) {
      toast.error(err?.response?.data?.message || 'Có lỗi xảy ra')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="space-y-8">
      <div className="space-y-2">
        <h2 className="font-display text-3xl font-bold text-content-main">Quên mật khẩu</h2>
        <p className="text-content-muted">
          {step === 'check' && 'Nhập số điện thoại để xác thực tài khoản'}
          {step === 'reset' && `Tạo mật khẩu mới cho tài khoản: ${phone}`}
          {step === 'done'  && 'Mật khẩu đã được cập nhật thành công!'}
        </p>
      </div>

      {step === 'check' && (
        <form onSubmit={checkForm.handleSubmit(onCheck)} className="space-y-5">
          <FormField label="Số điện thoại" error={checkForm.formState.errors.phone?.message} required>
            <Input
              placeholder="0912345678"
              {...checkForm.register('phone')}
              error={checkForm.formState.errors.phone}
            />
          </FormField>
          <Button type="submit" fullWidth size="lg" loading={loading}>
            Xác thực số điện thoại
          </Button>
        </form>
      )}

      {step === 'reset' && (
        <form onSubmit={resetForm.handleSubmit(onReset)} className="space-y-5">
          <FormField label="Mật khẩu mới" error={resetForm.formState.errors.newPassword?.message} required>
            <Input
              type="password"
              placeholder="Tối thiểu 6 ký tự"
              {...resetForm.register('newPassword')}
              error={resetForm.formState.errors.newPassword}
            />
          </FormField>
          <FormField label="Xác nhận mật khẩu" error={resetForm.formState.errors.confirm?.message} required>
            <Input
              type="password"
              placeholder="Nhập lại mật khẩu"
              {...resetForm.register('confirm')}
              error={resetForm.formState.errors.confirm}
            />
          </FormField>
          <Button type="submit" fullWidth size="lg" loading={loading}>
            Đặt lại mật khẩu
          </Button>
        </form>
      )}

      {step === 'done' && (
        <Link to="/login">
          <Button fullWidth size="lg">Về trang đăng nhập</Button>
        </Link>
      )}

      <p className="text-center text-sm">
        <Link to="/login" className="text-brand-400 hover:text-brand-300 transition-colors">
          ← Quay lại đăng nhập
        </Link>
      </p>
    </div>
  )
}

export default ForgotPasswordPage
