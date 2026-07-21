import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import toast from 'react-hot-toast'
import {
  RiArrowLeftLine,
  RiArrowRightLine,
  RiCheckboxCircleFill,
  RiEyeLine,
  RiEyeOffLine,
  RiFacebookCircleFill,
  RiGoogleFill,
} from 'react-icons/ri'
import { motion } from 'motion/react'
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

const roleConfig = {
  driver: {
    roleValue: ROLES.DRIVER,
    kicker: 'Cổng đối tác',
    title: 'Sẵn sàng cho chuyến tiếp theo?',
    subtitle: 'Đăng nhập để nhận chuyến và quản lý thu nhập.',
    registerLink: '/register/driver',
    registerText: 'Trở thành đối tác',
    registerPrompt: 'Chưa chạy cùng BookCar?',
  },
  admin: {
    roleValue: ROLES.ADMIN,
    kicker: 'Khu vực quản trị',
    title: 'Điều hành mọi hành trình.',
    subtitle: 'Đăng nhập để truy cập trung tâm quản lý.',
  },
  customer: {
    roleValue: ROLES.CUSTOMER,
    kicker: 'Chào mừng trở lại',
    title: 'Thành phố đang chờ bạn.',
    subtitle: 'Đăng nhập để đặt chuyến chỉ trong vài chạm.',
    showSocial: true,
    registerLink: '/register/customer',
    registerText: 'Tạo tài khoản',
    registerPrompt: 'Mới đến BookCar?',
  },
}

const LoginPage = () => {
  const { role: roleParam } = useParams()
  const navigate = useNavigate()
  const { handleLogin } = useAuth()
  const [showPwd, setShowPwd] = useState(false)
  const [loading, setLoading] = useState(false)
  const config = roleConfig[roleParam] || roleConfig.customer

  const {
    register,
    handleSubmit,
    setValue,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(schema),
    defaultValues: { roleName: config.roleValue },
  })

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

  const loginWith = (provider) => {
    const isGoogle = provider === 'google'
    const clientId = import.meta.env[isGoogle ? 'VITE_GOOGLE_CLIENT_ID' : 'VITE_FACEBOOK_APP_ID']
    const redirectUri = `${window.location.origin}/oauth2/callback/${provider}`
    const base = isGoogle
      ? 'https://accounts.google.com/o/oauth2/v2/auth'
      : 'https://www.facebook.com/v20.0/dialog/oauth'
    const scope = isGoogle ? 'email%20profile' : 'public_profile,email'
    window.location.href = `${base}?client_id=${clientId}&redirect_uri=${encodeURIComponent(redirectUri)}&response_type=code&scope=${scope}`
  }

  return (
    <div className="grid min-h-[100dvh] lg:grid-cols-[minmax(0,1.08fr)_minmax(480px,.92fr)]">
      <motion.aside
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ duration: 0.7 }}
        className="relative hidden overflow-hidden bg-slate-950 lg:block"
      >
        <img
          src="/images/bookcar-login-city.webp"
          alt="Xe BookCar di chuyển trong thành phố vào buổi tối"
          className="absolute inset-0 h-full w-full object-cover"
        />
        <div className="absolute inset-0 bg-gradient-to-b from-slate-950/20 via-transparent to-slate-950/90" />
        <div className="absolute inset-x-0 bottom-0 p-12 xl:p-16">
          <p className="mb-5 flex items-center gap-2 text-sm font-semibold text-white/75">
            <RiCheckboxCircleFill className="text-lime-accent" size={18} />
            Di chuyển thông minh, an tâm mỗi ngày
          </p>
          <h1 className="max-w-[9ch] font-display text-5xl font-bold leading-[.94] tracking-[-0.055em] text-white xl:text-7xl">
            Một chạm. Mọi hành trình.
          </h1>
          <div className="mt-9 h-1 w-20 rounded-full bg-lime-accent" />
        </div>
      </motion.aside>

      <section className="flex min-h-[100dvh] items-center px-5 pb-12 pt-28 sm:px-10 lg:px-14 xl:px-24">
        <motion.div
          initial={{ opacity: 0, y: 18 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.55, ease: [0.16, 1, 0.3, 1] }}
          className="mx-auto w-full max-w-[460px]"
        >
          <button
            type="button"
            onClick={() => navigate('/welcome')}
            className="group mb-10 flex items-center gap-2 text-sm font-semibold text-content-muted transition-colors hover:text-content-main"
          >
            <RiArrowLeftLine className="transition-transform group-hover:-translate-x-1" />
            Chọn vai trò khác
          </button>

          <p className="mb-4 text-sm font-bold text-brand-600 dark:text-brand-400">{config.kicker}</p>
          <h2 className="max-w-[12ch] font-display text-4xl font-bold leading-[1.02] tracking-[-0.05em] text-content-main sm:text-5xl">
            {config.title}
          </h2>
          <p className="mt-4 text-base leading-relaxed text-content-muted">{config.subtitle}</p>

          <form onSubmit={handleSubmit(onSubmit)} className="mt-9 space-y-5">
            <FormField label="Tên đăng nhập" error={errors.userName?.message} required>
              <Input
                autoComplete="username"
                placeholder="Nhập tên đăng nhập"
                {...register('userName')}
                error={errors.userName}
                className="h-[52px] bg-surface-card"
              />
            </FormField>

            <FormField label="Mật khẩu" error={errors.passWord?.message} required>
              <Input
                type={showPwd ? 'text' : 'password'}
                autoComplete="current-password"
                placeholder="Nhập mật khẩu"
                {...register('passWord')}
                error={errors.passWord}
                className="h-[52px] bg-surface-card pr-12"
                suffix={
                  <button
                    type="button"
                    onClick={() => setShowPwd((visible) => !visible)}
                    className="grid h-9 w-9 place-items-center rounded-lg text-content-muted transition hover:bg-surface-muted hover:text-content-main"
                    aria-label={showPwd ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'}
                  >
                    {showPwd ? <RiEyeOffLine size={19} /> : <RiEyeLine size={19} />}
                  </button>
                }
              />
            </FormField>

            <div className="flex justify-end">
              <Link to="/forgot-password" className="text-sm font-semibold text-content-main underline decoration-surface-border underline-offset-4 transition hover:decoration-brand-500">
                Quên mật khẩu?
              </Link>
            </div>

            <Button
              type="submit"
              fullWidth
              size="lg"
              loading={loading}
              className="group !mt-7 h-14 rounded-xl bg-content-main text-surface-dark shadow-none hover:bg-brand-500 hover:text-white focus:ring-brand-500 active:scale-[.98]"
            >
              <span>Đăng nhập</span>
              <RiArrowRightLine className="transition-transform group-hover:translate-x-1" />
            </Button>
          </form>

          {config.showSocial && (
            <div className="mt-7">
              <div className="mb-5 flex items-center gap-4 text-xs font-medium text-content-muted">
                <span className="h-px flex-1 bg-surface-border" />
                hoặc tiếp tục với
                <span className="h-px flex-1 bg-surface-border" />
              </div>
              <div className="grid grid-cols-2 gap-3">
                <button type="button" onClick={() => loginWith('google')} className="flex h-12 items-center justify-center gap-2 rounded-xl border border-surface-border bg-surface-card text-sm font-semibold transition hover:border-content-main/30 active:scale-[.98]">
                  <RiGoogleFill className="text-[#DB4437]" size={19} /> Google
                </button>
                <button type="button" onClick={() => loginWith('facebook')} className="flex h-12 items-center justify-center gap-2 rounded-xl border border-surface-border bg-surface-card text-sm font-semibold transition hover:border-content-main/30 active:scale-[.98]">
                  <RiFacebookCircleFill className="text-[#4267B2]" size={20} /> Facebook
                </button>
              </div>
            </div>
          )}

          {config.registerLink && (
            <p className="mt-8 text-sm text-content-muted">
              {config.registerPrompt}{' '}
              <Link to={config.registerLink} className="font-bold text-content-main underline decoration-brand-500 underline-offset-4">
                {config.registerText}
              </Link>
            </p>
          )}
        </motion.div>
      </section>
    </div>
  )
}

export default LoginPage
