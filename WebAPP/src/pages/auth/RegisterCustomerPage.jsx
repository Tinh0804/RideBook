import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
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
} from 'react-icons/ri'
import { motion } from 'motion/react'
import { customerApi } from '@/features/customer/api/customerApi'
import Button from '@/components/Elements/Button'
import Input from '@/components/Elements/Input'
import FormField from '@/components/Form/FormField'

const schema = z
  .object({
    userName:    z.string().min(4, 'Tên đăng nhập tối thiểu 4 ký tự'),
    name:        z.string().min(2, 'Họ tên tối thiểu 2 ký tự'),
    phoneNumber: z.string().regex(/^(0|\+84)[0-9]{9}$/, 'Số điện thoại không hợp lệ'),
    address:     z.string().min(5, 'Địa chỉ tối thiểu 5 ký tự'),
    passWord:    z.string().min(6, 'Mật khẩu tối thiểu 6 ký tự'),
    confirm:     z.string().min(1, 'Vui lòng xác nhận mật khẩu'),
  })
  .refine((d) => d.passWord === d.confirm, {
    message: 'Mật khẩu xác nhận không khớp',
    path:    ['confirm'],
  })

const RegisterCustomerPage = () => {
  const navigate = useNavigate()
  const [showPwd, setShowPwd] = useState(false)
  const [showConfirmPwd, setShowConfirmPwd] = useState(false)
  const [loading, setLoading] = useState(false)

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(schema),
  })

  const onSubmit = async ({ confirm, ...data }) => {
    setLoading(true)
    try {
      await customerApi.register(data)
      toast.success('Đăng ký tài khoản thành công! Vui lòng đăng nhập.')
      navigate('/login/customer')
    } catch (err) {
      toast.error(err?.response?.data?.message || 'Đăng ký thất bại, vui lòng thử lại!')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="grid min-h-[100dvh] lg:grid-cols-[minmax(480px,1.05fr)_minmax(0,0.95fr)]">
      {/* ── BÊN TRÁI: FORM ĐĂNG KÝ ── */}
      <section className="flex min-h-[100dvh] items-center px-5 pb-12 pt-28 sm:px-10 lg:px-14 xl:px-20">
        <motion.div
          initial={{ opacity: 0, y: 18 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.55, ease: [0.16, 1, 0.3, 1] }}
          className="mx-auto w-full max-w-[500px]"
        >
          {/* Nút quay lại đăng nhập */}
          <button
            type="button"
            onClick={() => navigate('/login/customer')}
            className="group mb-8 flex items-center gap-2 text-sm font-semibold text-content-muted transition-colors hover:text-content-main"
          >
            <RiArrowLeftLine className="transition-transform group-hover:-translate-x-1" />
            Đã có tài khoản? Đăng nhập
          </button>

          <p className="mb-3 text-sm font-bold text-brand-600 dark:text-brand-400">Khách hàng mới</p>
          <h2 className="font-display text-3xl font-bold leading-[1.08] tracking-[-0.04em] text-content-main sm:text-4xl">
            Tạo tài khoản BookCar.
          </h2>
          <p className="mt-3 text-sm sm:text-base leading-relaxed text-content-muted">
            Đăng ký nhanh chóng để trải nghiệm dịch vụ di chuyển thông minh và an toàn.
          </p>

          <form onSubmit={handleSubmit(onSubmit)} className="mt-7 space-y-4">
            {/* Tên đăng nhập & Họ và tên */}
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
              <FormField label="Tên đăng nhập" error={errors.userName?.message} required>
                <Input
                  autoComplete="username"
                  placeholder="username"
                  {...register('userName')}
                  error={errors.userName}
                  className="h-[50px] bg-surface-card"
                />
              </FormField>

              <FormField label="Họ và tên" error={errors.name?.message} required>
                <Input
                  autoComplete="name"
                  placeholder="Nguyễn Văn A"
                  {...register('name')}
                  error={errors.name}
                  className="h-[50px] bg-surface-card"
                />
              </FormField>
            </div>

            {/* Số điện thoại */}
            <FormField label="Số điện thoại" error={errors.phoneNumber?.message} required>
              <Input
                type="tel"
                autoComplete="tel"
                placeholder="0912345678"
                {...register('phoneNumber')}
                error={errors.phoneNumber}
                className="h-[50px] bg-surface-card"
              />
            </FormField>

            {/* Địa chỉ */}
            <FormField label="Địa chỉ" error={errors.address?.message} required>
              <Input
                autoComplete="street-address"
                placeholder="123 Đường ABC, Quận XYZ, TP.HCM"
                {...register('address')}
                error={errors.address}
                className="h-[50px] bg-surface-card"
              />
            </FormField>

            {/* Mật khẩu & Xác nhận mật khẩu */}
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
              <FormField label="Mật khẩu" error={errors.passWord?.message} required>
                <Input
                  type={showPwd ? 'text' : 'password'}
                  autoComplete="new-password"
                  placeholder="Tối thiểu 6 ký tự"
                  {...register('passWord')}
                  error={errors.passWord}
                  className="h-[50px] bg-surface-card pr-11"
                  suffix={
                    <button
                      type="button"
                      onClick={() => setShowPwd((v) => !v)}
                      className="grid h-8 w-8 place-items-center rounded-lg text-content-muted transition hover:bg-surface-muted hover:text-content-main"
                      aria-label={showPwd ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'}
                    >
                      {showPwd ? <RiEyeOffLine size={18} /> : <RiEyeLine size={18} />}
                    </button>
                  }
                />
              </FormField>

              <FormField label="Xác nhận mật khẩu" error={errors.confirm?.message} required>
                <Input
                  type={showConfirmPwd ? 'text' : 'password'}
                  autoComplete="new-password"
                  placeholder="Nhập lại mật khẩu"
                  {...register('confirm')}
                  error={errors.confirm}
                  className="h-[50px] bg-surface-card pr-11"
                  suffix={
                    <button
                      type="button"
                      onClick={() => setShowConfirmPwd((v) => !v)}
                      className="grid h-8 w-8 place-items-center rounded-lg text-content-muted transition hover:bg-surface-muted hover:text-content-main"
                      aria-label={showConfirmPwd ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'}
                    >
                      {showConfirmPwd ? <RiEyeOffLine size={18} /> : <RiEyeLine size={18} />}
                    </button>
                  }
                />
              </FormField>
            </div>

            {/* Nút gửi form */}
            <Button
              type="submit"
              fullWidth
              size="lg"
              loading={loading}
              className="group !mt-6 h-13 rounded-xl bg-content-main text-surface-dark shadow-none hover:bg-brand-500 hover:text-white focus:ring-brand-500 active:scale-[.98]"
            >
              <span>Đăng ký tài khoản</span>
              <RiArrowRightLine className="transition-transform group-hover:translate-x-1" />
            </Button>
          </form>

          {/* Chuyển sang đăng nhập */}
          <p className="mt-7 text-center text-sm text-content-muted">
            Đã có tài khoản?{' '}
            <Link
              to="/login/customer"
              className="font-bold text-content-main underline decoration-brand-500 underline-offset-4 transition hover:text-brand-500"
            >
              Đăng nhập ngay
            </Link>
          </p>
        </motion.div>
      </section>

      {/* ── BÊN PHẢI: HÌNH ẢNH BANNER ── */}
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
        {/* Gradient làm tối phần trên để làm nổi bật chữ trên nền trời */}
        <div className="absolute inset-0 bg-gradient-to-b from-slate-950/85 via-slate-950/35 to-transparent" />
        <div className="absolute inset-0 bg-gradient-to-t from-slate-950/50 via-transparent to-transparent" />

        {/* Nội dung chữ di chuyển lên phía trên */}
        <div className="absolute inset-x-0 top-0 p-12 pt-28 xl:p-16 xl:pt-32">
          <p className="mb-4 flex items-center gap-2 text-sm font-semibold text-white/90">
            <RiCheckboxCircleFill className="text-lime-accent" size={18} />
            Đồng hành trên mọi nẻo đường
          </p>
          <h1 className="max-w-[12ch] font-display text-4xl font-bold leading-[1.05] tracking-tight !text-white xl:text-5xl">
            Di chuyển dễ dàng, an toàn tuyệt đối.
          </h1>
          <div className="mt-6 h-1 w-20 rounded-full bg-lime-accent" />
        </div>
      </motion.aside>
    </div>
  )
}

export default RegisterCustomerPage

