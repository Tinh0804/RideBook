import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import toast from 'react-hot-toast'
import { RiEyeLine, RiEyeOffLine, RiCarFill, RiUserLine, RiShieldLine } from 'react-icons/ri'
import { useAuth } from '@/hooks/useAuth'
import { ROLES } from '@/config'
import Button from '@/components/Elements/Button'
import Input from '@/components/Elements/Input'
import FormField from '@/components/Form/FormField'
import { cn } from '@/utils/cn'

const schema = z.object({
  userName: z.string().min(1, 'Vui lòng nhập tên đăng nhập'),
  passWord: z.string().min(5, 'Mật khẩu tối thiểu 5 ký tự'),
  roleName: z.enum([ROLES.CUSTOMER, ROLES.DRIVER, ROLES.ADMIN], {
    errorMap: () => ({ message: 'Chọn loại tài khoản' }),
  }),
})

const ROLE_TABS = [
  { value: ROLES.CUSTOMER, label: 'Khách hàng', icon: RiUserLine },
  { value: ROLES.DRIVER,   label: 'Tài xế',     icon: RiCarFill  },
  { value: ROLES.ADMIN,    label: 'Quản trị',    icon: RiShieldLine },
]

const LoginPage = () => {
  const { handleLogin } = useAuth()
  const [showPwd, setShowPwd] = useState(false)
  const [loading, setLoading] = useState(false)

  const {
    register,
    handleSubmit,
    setValue,
    watch,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(schema),
    defaultValues: { roleName: ROLES.CUSTOMER },
  })

  const selectedRole = watch('roleName')

  const onSubmit = async (data) => {
    setLoading(true)
    try {
      await handleLogin(data)
      toast.success('Đăng nhập thành công!')
    } catch (err) {
      toast.error(err?.response?.data?.message || 'Sai tên đăng nhập hoặc mật khẩu')
    } finally {
      setLoading(false)
    }
  }

  const handleGoogleLogin = () => {
    const clientId = import.meta.env.VITE_GOOGLE_CLIENT_ID
    const redirectUri = `${window.location.origin}/oauth2/callback/google`
    const url = `https://accounts.google.com/o/oauth2/v2/auth?client_id=${clientId}&redirect_uri=${encodeURIComponent(redirectUri)}&response_type=code&scope=email%20profile`
    window.location.href = url
  }

  const handleFacebookLogin = () => {
    const clientId = import.meta.env.VITE_FACEBOOK_APP_ID
    const redirectUri = `${window.location.origin}/oauth2/callback/facebook`
    const url = `https://www.facebook.com/v20.0/dialog/oauth?client_id=${clientId}&redirect_uri=${encodeURIComponent(redirectUri)}&response_type=code&scope=public_profile,email`
    window.location.href = url
  }

  return (
    <div className="space-y-8">
      {/* Header */}
      <div className="space-y-2">
        <h2 className="font-display text-3xl font-bold text-content-main">Đăng nhập</h2>
        <p className="text-content-muted">Chào mừng bạn quay trở lại 👋</p>
      </div>

      {/* Role selector */}
      <div className="grid grid-cols-3 gap-2 p-1 bg-surface-dark rounded-2xl border border-surface-border">
        {ROLE_TABS.map(({ value, label, icon: Icon }) => (
          <button
            key={value}
            type="button"
            onClick={() => setValue('roleName', value)}
            className={cn(
              'flex flex-col items-center gap-1 py-3 px-2 rounded-xl text-xs font-semibold transition-all duration-200',
              selectedRole === value
                ? 'bg-brand-500 text-content-main shadow-glow-green'
                : 'text-content-muted hover:text-content-muted',
            )}
          >
            <Icon size={18} />
            {label}
          </button>
        ))}
      </div>

      {/* Form */}
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
        <FormField label="Tên đăng nhập" error={errors.userName?.message} required>
          <Input
            placeholder="Nhập tên đăng nhập"
            {...register('userName')}
            error={errors.userName}
          />
        </FormField>

        <FormField label="Mật khẩu" error={errors.passWord?.message} required>
          <Input
            type={showPwd ? 'text' : 'password'}
            placeholder="Nhập mật khẩu"
            {...register('passWord')}
            error={errors.passWord}
            suffix={
              <button type="button" onClick={() => setShowPwd((s) => !s)} className="text-content-muted hover:text-brand-400 transition-colors">
                {showPwd ? <RiEyeOffLine size={18} /> : <RiEyeLine size={18} />}
              </button>
            }
          />
        </FormField>

        <div className="flex justify-end">
          <Link to="/forgot-password" className="text-sm text-brand-400 hover:text-brand-300 transition-colors">
            Quên mật khẩu?
          </Link>
        </div>

        <Button type="submit" fullWidth size="lg" loading={loading}>
          Đăng nhập
        </Button>
      </form>

      {/* Divider */}
      <div className="divider-label text-xs text-gray-600">hoặc</div>

      {/* Register links */}
      <div className="space-y-3">
        <p className="text-center text-sm text-content-muted">
          Chưa có tài khoản khách hàng?{' '}
          <Link to="/register/customer" className="text-brand-400 font-semibold hover:text-brand-300 transition-colors">
            Đăng ký ngay
          </Link>
        </p>
        <p className="text-center text-sm text-content-muted">
          Muốn trở thành tài xế?{' '}
          <Link to="/register/driver" className="text-brand-400 font-semibold hover:text-brand-300 transition-colors">
            Đăng ký tài xế
          </Link>
        </p>
      </div>
    </div>
  )
}

export default LoginPage
