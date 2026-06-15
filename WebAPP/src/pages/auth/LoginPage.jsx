import { useState, useEffect } from 'react'
import { Link, useParams, useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import toast from 'react-hot-toast'
import { RiEyeLine, RiEyeOffLine, RiArrowLeftLine, RiGoogleFill, RiFacebookCircleFill } from 'react-icons/ri'
import { useAuth } from '@/hooks/useAuth'
import { ROLES } from '@/config'
import Button from '@/components/Elements/Button'
import Input from '@/components/Elements/Input'
import FormField from '@/components/Form/FormField'

const schema = z.object({
  userName: z.string().min(1, 'Vui lòng nhập tên đăng nhập'),
  passWord: z.string().min(5, 'Mật khẩu tối thiểu 5 ký tự'),
  roleName: z.enum([ROLES.CUSTOMER, ROLES.DRIVER, ROLES.ADMIN], {
    errorMap: () => ({ message: 'Vai trò không hợp lệ' }),
  }),
})

const getRoleConfig = (roleParam) => {
  switch (roleParam) {
    case 'driver':
      return {
        roleValue: ROLES.DRIVER,
        title: 'Đăng nhập Tài xế',
        showSocial: false,
        registerLink: '/register/driver',
        registerText: 'Đăng ký tài xế',
        registerPrompt: 'Muốn trở thành tài xế?',
      }
    case 'admin':
      return {
        roleValue: ROLES.ADMIN,
        title: 'Đăng nhập Quản trị viên',
        showSocial: false,
        registerLink: null,
      }
    case 'customer':
    default:
      return {
        roleValue: ROLES.CUSTOMER,
        title: 'Đăng nhập Khách hàng',
        showSocial: true,
        registerLink: '/register/customer',
        registerText: 'Đăng ký ngay',
        registerPrompt: 'Chưa có tài khoản khách hàng?',
      }
  }
}

const LoginPage = () => {
  const { role: roleParam } = useParams()
  const navigate = useNavigate()
  const { handleLogin } = useAuth()
  const [showPwd, setShowPwd] = useState(false)
  const [loading, setLoading] = useState(false)

  const config = getRoleConfig(roleParam)

  const {
    register,
    handleSubmit,
    setValue,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(schema),
    defaultValues: { roleName: config.roleValue },
  })

  // Đảm bảo roleName trong form luôn đồng bộ với URL params
  useEffect(() => {
    setValue('roleName', config.roleValue)
  }, [config.roleValue, setValue])

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
    <div className="space-y-8 animate-slide-up w-full">
      {/* Back button */}
      <div>
        <button
          onClick={() => navigate('/welcome')}
          className="flex items-center gap-2 text-sm font-semibold text-content-muted hover:text-content-main transition-colors"
        >
          <RiArrowLeftLine size={18} />
          Quay lại
        </button>
      </div>

      {/* Header */}
      <div className="space-y-2">
        <h2 className="font-display text-3xl font-bold text-content-main">{config.title}</h2>
        <p className="text-content-muted">Chào mừng bạn quay trở lại 👋</p>
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

      {config.showSocial && (
        <>
          <div className="divider-label text-xs text-gray-600">hoặc</div>

          {/* Social Login */}
          <div className="grid grid-cols-2 gap-4">
            <button
              type="button"
              onClick={handleGoogleLogin}
              className="flex items-center justify-center gap-2 py-2.5 px-4 rounded-xl border border-surface-border bg-surface-dark text-content-main font-semibold text-sm hover:bg-surface-hover transition-colors"
            >
              <RiGoogleFill size={20} className="text-[#DB4437]" />
              Google
            </button>
            <button
              type="button"
              onClick={handleFacebookLogin}
              className="flex items-center justify-center gap-2 py-2.5 px-4 rounded-xl border border-surface-border bg-surface-dark text-content-main font-semibold text-sm hover:bg-surface-hover transition-colors"
            >
              <RiFacebookCircleFill size={20} className="text-[#4267B2]" />
              Facebook
            </button>
          </div>
        </>
      )}

      {/* Register link */}
      {config.registerLink && (
        <div className="pt-4 text-center">
          <p className="text-sm text-content-muted">
            {config.registerPrompt}{' '}
            <Link to={config.registerLink} className="text-brand-400 font-semibold hover:text-brand-300 transition-colors">
              {config.registerText}
            </Link>
          </p>
        </div>
      )}
    </div>
  )
}

export default LoginPage
