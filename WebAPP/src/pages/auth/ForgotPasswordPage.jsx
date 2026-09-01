import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import toast from 'react-hot-toast'
import { authApi } from '@/features/auth/api/authApi'
import Button from '@/components/Elements/Button'
import Input from '@/components/Elements/Input'
import FormField from '@/components/Form/FormField'
import { RiArrowLeftLine } from 'react-icons/ri'
import { RecaptchaVerifier, signInWithPhoneNumber } from 'firebase/auth'
import { auth } from '@/config/firebase'

const checkSchema = z.object({
  phone: z.string().regex(/^(0|\+84)[0-9]{9}$/, 'Số điện thoại không hợp lệ'),
})

const otpSchema = z.object({
  otp: z.string().length(6, 'Mã OTP gồm 6 số'),
})

const resetSchema = z.object({
  newPassword: z.string().min(6, 'Mật khẩu tối thiểu 6 ký tự'),
  confirm:     z.string(),
}).refine((d) => d.newPassword === d.confirm, {
  message: 'Mật khẩu xác nhận không khớp',
  path:    ['confirm'],
})

const ForgotPasswordPage = () => {
  const [step, setStep] = useState('check') // 'check' | 'otp' | 'reset' | 'done'
  const [phone, setPhone] = useState('')
  const [loading, setLoading] = useState(false)
  const [confirmationResult, setConfirmationResult] = useState(null)
  const [firebaseToken, setFirebaseToken] = useState('')

  const checkForm = useForm({ resolver: zodResolver(checkSchema) })
  const otpForm = useForm({ resolver: zodResolver(otpSchema) })
  const resetForm = useForm({ resolver: zodResolver(resetSchema) })

  useEffect(() => {
    // Khởi tạo Recaptcha
    if (!window.recaptchaVerifier) {
      window.recaptchaVerifier = new RecaptchaVerifier(auth, 'recaptcha-container', {
        size: 'invisible'
      })
    }
  }, [])

  const onCheck = async (data) => {
    setLoading(true)
    try {
      // 1. Kiểm tra SĐT có tồn tại ở Backend không
      await authApi.checkPhone(data.phone)
      
      // 2. Định dạng lại SĐT sang chuẩn quốc tế để gửi Firebase
      let formattedPhone = data.phone
      if (formattedPhone.startsWith('0')) {
        formattedPhone = '+84' + formattedPhone.slice(1)
      }

      // 3. Gọi Firebase gửi OTP
      const appVerifier = window.recaptchaVerifier
      const confResult = await signInWithPhoneNumber(auth, formattedPhone, appVerifier)
      
      setConfirmationResult(confResult)
      setPhone(data.phone)
      setStep('otp')
      toast.success('Mã OTP đã được gửi đến số điện thoại của bạn')
    } catch (err) {
      console.error(err)
      toast.error('Số điện thoại không tồn tại hoặc lỗi gửi SMS')
    } finally {
      setLoading(false)
    }
  }

  const onVerifyOtp = async (data) => {
    setLoading(true)
    try {
      const result = await confirmationResult.confirm(data.otp)
      const token = await result.user.getIdToken()
      setFirebaseToken(token)
      setStep('reset')
      toast.success('Xác thực OTP thành công!')
    } catch (err) {
      console.error(err)
      toast.error('Mã OTP không chính xác')
    } finally {
      setLoading(false)
    }
  }

  const onReset = async (data) => {
    setLoading(true)
    try {
      await authApi.resetPassword({ firebaseToken, newPassword: data.newPassword })
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
      <div id="recaptcha-container"></div>
      
      <div className="space-y-2">
        <h2 className="font-display text-3xl font-bold text-content-main">Quên mật khẩu</h2>
        <p className="text-content-muted">
          {step === 'check' && 'Nhập số điện thoại để xác thực tài khoản'}
          {step === 'otp'   && `Nhập mã OTP vừa được gửi đến ${phone}`}
          {step === 'reset' && 'Tạo mật khẩu mới cho tài khoản của bạn'}
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
            Gửi mã OTP
          </Button>
        </form>
      )}

      {step === 'otp' && (
        <form onSubmit={otpForm.handleSubmit(onVerifyOtp)} className="space-y-5">
          <FormField label="Mã OTP" error={otpForm.formState.errors.otp?.message} required>
            <Input
              placeholder="123456"
              maxLength={6}
              {...otpForm.register('otp')}
              error={otpForm.formState.errors.otp}
            />
          </FormField>
          <Button type="submit" fullWidth size="lg" loading={loading}>
            Xác nhận
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
        <Link to="/welcome">
          <Button fullWidth size="lg">Về trang chào mừng</Button>
        </Link>
      )}

      <div className="flex justify-between items-center text-sm font-semibold text-content-muted">
        <Link to="/welcome">
          <Button variant="ghost" type="button" className="gap-2">
            <RiArrowLeftLine /> Quay lại
          </Button>
        </Link>
        <Link to="/welcome" className="text-brand-400 hover:text-brand-300 transition-colors">
          Đăng nhập ngay
        </Link>
      </div>
    </div>
  )
}

export default ForgotPasswordPage
