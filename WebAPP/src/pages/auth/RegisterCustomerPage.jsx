import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import toast from 'react-hot-toast'
import { RiEyeLine, RiEyeOffLine } from 'react-icons/ri'
import { customerApi } from '@/features/customer/api/customerApi'
import Button from '@/components/Elements/Button'
import Input from '@/components/Elements/Input'
import FormField from '@/components/Form/FormField'

const schema = z.object({
  userName:    z.string().min(4, 'Tên đăng nhập tối thiểu 4 ký tự'),
  passWord:    z.string().min(6, 'Mật khẩu tối thiểu 6 ký tự'),
  confirm:     z.string(),
  name:        z.string().min(2, 'Họ tên tối thiểu 2 ký tự'),
  phoneNumber: z.string().regex(/^(0|\+84)[0-9]{9}$/, 'Số điện thoại không hợp lệ'),
  address:     z.string().min(5, 'Địa chỉ tối thiểu 5 ký tự'),
}).refine((d) => d.passWord === d.confirm, {
  message: 'Mật khẩu xác nhận không khớp',
  path:    ['confirm'],
})

const RegisterCustomerPage = () => {
  const navigate = useNavigate()
  const [showPwd, setShowPwd] = useState(false)
  const [loading, setLoading] = useState(false)

  const { register, handleSubmit, formState: { errors } } = useForm({
    resolver: zodResolver(schema),
  })

  const onSubmit = async ({ confirm, ...data }) => {
    setLoading(true)
    try {
      await customerApi.register(data)
      toast.success('Đăng ký thành công! Vui lòng đăng nhập.')
      navigate('/login/customer')
    } catch (err) {
      toast.error(err?.response?.data?.message || 'Đăng ký thất bại')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="space-y-6">
      <div className="space-y-1">
        <h2 className="font-display text-3xl font-bold text-content-main">Đăng ký tài khoản</h2>
        <p className="text-content-muted">Tạo tài khoản khách hàng miễn phí</p>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <div className="grid grid-cols-2 gap-4">
          <FormField label="Tên đăng nhập" error={errors.userName?.message} required className="col-span-2 sm:col-span-1">
            <Input placeholder="username" {...register('userName')} error={errors.userName} />
          </FormField>
          <FormField label="Họ và tên" error={errors.name?.message} required className="col-span-2 sm:col-span-1">
            <Input placeholder="Nguyễn Văn A" {...register('name')} error={errors.name} />
          </FormField>
        </div>

        <FormField label="Số điện thoại" error={errors.phoneNumber?.message} required>
          <Input placeholder="0912345678" {...register('phoneNumber')} error={errors.phoneNumber} />
        </FormField>

        <FormField label="Địa chỉ" error={errors.address?.message} required>
          <Input placeholder="123 Đường ABC, Quận XYZ, TP.HCM" {...register('address')} error={errors.address} />
        </FormField>

        <FormField label="Mật khẩu" error={errors.passWord?.message} required>
          <Input
            type={showPwd ? 'text' : 'password'}
            placeholder="Tối thiểu 6 ký tự"
            {...register('passWord')}
            error={errors.passWord}
            suffix={
              <button type="button" onClick={() => setShowPwd((s) => !s)} className="text-content-muted hover:text-brand-400 transition-colors">
                {showPwd ? <RiEyeOffLine size={18} /> : <RiEyeLine size={18} />}
              </button>
            }
          />
        </FormField>

        <FormField label="Xác nhận mật khẩu" error={errors.confirm?.message} required>
          <Input
            type={showPwd ? 'text' : 'password'}
            placeholder="Nhập lại mật khẩu"
            {...register('confirm')}
            error={errors.confirm}
          />
        </FormField>

        <Button type="submit" fullWidth size="lg" loading={loading} className="mt-2">
          Tạo tài khoản
        </Button>
      </form>

      <p className="text-center text-sm text-content-muted">
        Đã có tài khoản?{' '}
        <Link to="/login/customer" className="text-brand-400 font-semibold hover:text-brand-300 transition-colors">
          Đăng nhập
        </Link>
      </p>
    </div>
  )
}

export default RegisterCustomerPage
