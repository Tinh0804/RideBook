import { useState, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import toast from 'react-hot-toast'
import { RiUploadLine, RiCheckLine } from 'react-icons/ri'
import { driverApi } from '@/features/driver/api/driverApi'
import { masterDataApi } from '@/features/booking/api/masterDataApi'
import Button from '@/components/Elements/Button'
import Input from '@/components/Elements/Input'
import FormField from '@/components/Form/FormField'

const STEPS = ['Thông tin cá nhân', 'Thông tin xe', 'Tài liệu & Ảnh']

const schema = z.object({
  driverName:    z.string().min(2, 'Tối thiểu 2 ký tự'),
  birthDate:     z.string().min(1, 'Chọn ngày sinh'),
  citizenId:     z.string().min(9, 'CCCD không hợp lệ'),
  drivingLicense:z.string().min(6, 'Số bằng lái không hợp lệ'),
  phone:         z.string().regex(/^(0|\+84)[0-9]{9}$/, 'Số điện thoại không hợp lệ'),
  email:         z.string().email('Email không hợp lệ'),
  address:       z.string().min(5, 'Địa chỉ tối thiểu 5 ký tự'),
  area:          z.string().min(1, 'Chọn khu vực hoạt động'),
  gender:        z.enum(['MALE', 'FEMALE', 'OTHER']),
  licensePlate:  z.string().min(6, 'Biển số không hợp lệ'),
  vehicleName:   z.string().min(2, 'Tên xe không hợp lệ'),
  vehicleTypeId: z.string().min(1, 'Chọn loại xe'),
  password:      z.string().min(6, 'Mật khẩu tối thiểu 6 ký tự'),
})

const RegisterDriverPage = () => {
  const navigate = useNavigate()
  const [step, setStep]               = useState(0)
  const [loading, setLoading]         = useState(false)
  const [vehicleTypes, setVehicleTypes] = useState([])
  const [files, setFiles]             = useState({})

  const { register, handleSubmit, trigger, formState: { errors } } = useForm({
    resolver: zodResolver(schema),
    defaultValues: { gender: 'MALE' },
  })

  useEffect(() => {
    masterDataApi.getVehicleTypes()
      .then((types) => setVehicleTypes(types))
      .catch(() => {})
  }, [])

  const STEP_FIELDS = [
    ['driverName','birthDate','citizenId','phone','email','address','gender','area','password'],
    ['vehicleTypeId','vehicleName','licensePlate'],
    [],
  ]

  const nextStep = async () => {
    const valid = await trigger(STEP_FIELDS[step])
    if (valid) setStep((s) => Math.min(s + 1, 2))
  }

  const handleFile = (key, file) => setFiles((f) => ({ ...f, [key]: file }))

  const onSubmit = async (data) => {
    setLoading(true)
    try {
      const fd = new FormData()
      Object.entries(data).forEach(([k, v]) => fd.append(k, v))
      Object.entries(files).forEach(([k, v]) => { if (v) fd.append(k, v) })

      await driverApi.register(fd)
      toast.success('Đăng ký tài xế thành công! Chờ admin phê duyệt.')
      navigate('/login')
    } catch (err) {
      toast.error(err?.response?.data?.message || 'Đăng ký thất bại')
    } finally {
      setLoading(false)
    }
  }

  const FileUpload = ({ label, fileKey, required }) => (
    <FormField label={label} required={required}>
      <label className={`
        flex flex-col items-center gap-2 p-4 rounded-xl border-2 border-dashed cursor-pointer transition-colors
        ${files[fileKey] ? 'border-brand-500 bg-brand-500/5' : 'border-surface-muted hover:border-brand-500/50 bg-surface-dark'}
      `}>
        <input
          type="file" className="sr-only"
          accept="image/*"
          onChange={(e) => handleFile(fileKey, e.target.files[0])}
        />
        {files[fileKey] ? (
          <>
            <RiCheckLine size={24} className="text-brand-400" />
            <span className="text-xs text-brand-400 font-medium">{files[fileKey].name}</span>
          </>
        ) : (
          <>
            <RiUploadLine size={24} className="text-content-muted" />
            <span className="text-xs text-content-muted">Nhấn để tải ảnh lên</span>
          </>
        )}
      </label>
    </FormField>
  )

  return (
    <div className="space-y-6">
      <div className="space-y-1">
        <h2 className="font-display text-3xl font-bold text-content-main">Đăng ký tài xế</h2>
        <p className="text-content-muted">Hoàn thành {STEPS.length} bước để trở thành tài xế</p>
      </div>

      {/* Step indicator */}
      <div className="flex items-center gap-2">
        {STEPS.map((s, i) => (
          <div key={i} className="flex items-center gap-2 flex-1">
            <div className={`
              w-7 h-7 rounded-full flex items-center justify-center text-xs font-bold shrink-0 transition-all
              ${i < step ? 'bg-brand-500 text-content-main' : i === step ? 'bg-brand-500/20 border-2 border-brand-500 text-brand-400' : 'bg-surface-muted text-gray-600'}
            `}>
              {i < step ? <RiCheckLine size={14} /> : i + 1}
            </div>
            <span className={`text-xs hidden sm:block ${i === step ? 'text-brand-400 font-medium' : 'text-gray-600'}`}>{s}</span>
            {i < STEPS.length - 1 && <div className="flex-1 h-px bg-surface-border mx-1" />}
          </div>
        ))}
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        {/* Step 0: Personal info */}
        {step === 0 && (
          <>
            <div className="grid grid-cols-2 gap-4">
              <FormField label="Họ và tên" error={errors.driverName?.message} required className="col-span-2 sm:col-span-1">
                <Input placeholder="Nguyễn Văn A" {...register('driverName')} error={errors.driverName} />
              </FormField>
              <FormField label="Ngày sinh" error={errors.birthDate?.message} required className="col-span-2 sm:col-span-1">
                <Input type="date" {...register('birthDate')} error={errors.birthDate} />
              </FormField>
              <FormField label="Số CCCD" error={errors.citizenId?.message} required className="col-span-2 sm:col-span-1">
                <Input placeholder="012345678901" {...register('citizenId')} error={errors.citizenId} />
              </FormField>
              <FormField label="Số bằng lái" error={errors.drivingLicense?.message} required className="col-span-2 sm:col-span-1">
                <Input placeholder="B2-012345" {...register('drivingLicense')} error={errors.drivingLicense} />
              </FormField>
            </div>
            <FormField label="Số điện thoại" error={errors.phone?.message} required>
              <Input placeholder="0912345678" {...register('phone')} error={errors.phone} />
            </FormField>
            <FormField label="Email" error={errors.email?.message} required>
              <Input type="email" placeholder="example@gmail.com" {...register('email')} error={errors.email} />
            </FormField>
            <FormField label="Địa chỉ" error={errors.address?.message} required>
              <Input placeholder="123 Đường ABC..." {...register('address')} error={errors.address} />
            </FormField>
            <div className="grid grid-cols-2 gap-4">
              <FormField label="Khu vực" error={errors.area?.message} required>
                <Input placeholder="TP.HCM" {...register('area')} error={errors.area} />
              </FormField>
              <FormField label="Giới tính" error={errors.gender?.message} required>
                <select {...register('gender')} className="input-field">
                  <option value="MALE">Nam</option>
                  <option value="FEMALE">Nữ</option>
                  <option value="OTHER">Khác</option>
                </select>
              </FormField>
            </div>
            <FormField label="Mật khẩu" error={errors.password?.message} required>
              <Input type="password" placeholder="Tối thiểu 6 ký tự" {...register('password')} error={errors.password} />
            </FormField>
          </>
        )}

        {/* Step 1: Vehicle info */}
        {step === 1 && (
          <>
            <FormField label="Loại xe" error={errors.vehicleTypeId?.message} required>
              <select {...register('vehicleTypeId')} className="input-field">
                <option value="">Chọn loại xe</option>
                {vehicleTypes.map((vt) => (
                  <option key={vt.id} value={vt.id}>{vt.name}</option>
                ))}
              </select>
            </FormField>
            <FormField label="Tên xe" error={errors.vehicleName?.message} required>
              <Input placeholder="Toyota Vios 2022" {...register('vehicleName')} error={errors.vehicleName} />
            </FormField>
            <FormField label="Biển số xe" error={errors.licensePlate?.message} required>
              <Input placeholder="51A-12345" {...register('licensePlate')} error={errors.licensePlate} />
            </FormField>
          </>
        )}

        {/* Step 2: Documents */}
        {step === 2 && (
          <div className="grid grid-cols-2 gap-4">
            <FileUpload label="Ảnh đại diện"       fileKey="avatar"               />
            <FileUpload label="Ảnh CCCD"            fileKey="citizenIdImage"       required />
            <FileUpload label="Ảnh bằng lái xe"     fileKey="drivingLicenseImage"  required className="col-span-2" />
          </div>
        )}

        {/* Navigation */}
        <div className="flex gap-3 pt-2">
          {step > 0 && (
            <Button type="button" variant="outline" onClick={() => setStep((s) => s - 1)}>
              Quay lại
            </Button>
          )}
          {step < 2 ? (
            <Button type="button" fullWidth onClick={nextStep}>
              Tiếp theo
            </Button>
          ) : (
            <Button type="submit" fullWidth size="lg" loading={loading}>
              Hoàn tất đăng ký
            </Button>
          )}
        </div>
      </form>

      <p className="text-center text-sm text-content-muted">
        <Link to="/login" className="text-brand-400 font-semibold hover:text-brand-300 transition-colors">
          ← Quay lại đăng nhập
        </Link>
      </p>
    </div>
  )
}

export default RegisterDriverPage
