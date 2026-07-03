import React from 'react'
import { useForm, Controller } from 'react-hook-form'
import { toast } from 'react-hot-toast'
import { RiSendPlaneFill, RiNotification3Line } from 'react-icons/ri'
import { notificationApi } from '@/features/booking/api/masterDataApi'
import Button from '@/components/Elements/Button'

const AdminNotificationsPage = () => {
  const { register, handleSubmit, watch, control, reset, formState: { isSubmitting, errors } } = useForm({
    defaultValues: {
      title: '',
      message: '',
      targetType: 'ALL',
      targetUsername: ''
    }
  })

  const targetType = watch('targetType')

  const onSubmit = async (data) => {
    try {
      await notificationApi.sendAdminNotification(data)
      toast.success('Gửi thông báo thành công!')
      reset()
    } catch (error) {
      toast.error(error.message || 'Có lỗi xảy ra khi gửi thông báo.')
    }
  }

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div className="flex items-center gap-3 mb-6">
        <div className="w-12 h-12 bg-brand-500/20 text-brand-500 rounded-xl flex items-center justify-center">
          <RiNotification3Line size={24} />
        </div>
        <div>
          <h1 className="text-2xl font-bold text-content-main">Gửi thông báo</h1>
          <p className="text-content-muted">Tạo và gửi thông báo hệ thống hoặc khuyến mãi.</p>
        </div>
      </div>

      <div className="card p-6 bg-surface-card border border-surface-border">
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-content-main mb-1">Tiêu đề thông báo <span className="text-red-500">*</span></label>
              <input
                type="text"
                {...register('title', { required: 'Vui lòng nhập tiêu đề' })}
                className="input-field w-full"
                placeholder="VD: Cập nhật hệ thống / Khuyến mãi hấp dẫn..."
              />
              {errors.title && <p className="text-red-500 text-xs mt-1">{errors.title.message}</p>}
            </div>

            <div>
              <label className="block text-sm font-medium text-content-main mb-1">Nội dung <span className="text-red-500">*</span></label>
              <textarea
                {...register('message', { required: 'Vui lòng nhập nội dung' })}
                className="input-field w-full min-h-[120px] resize-y"
                placeholder="Nội dung thông báo muốn gửi..."
              />
              {errors.message && <p className="text-red-500 text-xs mt-1">{errors.message.message}</p>}
            </div>

            <div>
              <label className="block text-sm font-medium text-content-main mb-2">Gửi tới <span className="text-red-500">*</span></label>
              <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
                {['ALL', 'CUSTOMER', 'DRIVER', 'SPECIFIC'].map((type) => (
                  <label key={type} className={`flex items-center gap-2 p-3 rounded-xl border cursor-pointer transition-colors ${targetType === type ? 'border-brand-500 bg-brand-500/10' : 'border-surface-border bg-surface-dark hover:bg-surface-border/50'}`}>
                    <input
                      type="radio"
                      value={type}
                      {...register('targetType')}
                      className="text-brand-500 focus:ring-brand-500"
                    />
                    <span className="text-sm text-content-main font-medium">
                      {type === 'ALL' ? 'Tất cả' : type === 'CUSTOMER' ? 'Khách hàng' : type === 'DRIVER' ? 'Tài xế' : 'Chỉ định'}
                    </span>
                  </label>
                ))}
              </div>
            </div>

            {targetType === 'SPECIFIC' && (
              <div className="animate-fade-in">
                <label className="block text-sm font-medium text-content-main mb-1">Tài khoản nhận (Username / Số điện thoại) <span className="text-red-500">*</span></label>
                <input
                  type="text"
                  {...register('targetUsername', { required: targetType === 'SPECIFIC' ? 'Vui lòng nhập username' : false })}
                  className="input-field w-full"
                  placeholder="Nhập tên đăng nhập của người dùng..."
                />
                {errors.targetUsername && <p className="text-red-500 text-xs mt-1">{errors.targetUsername.message}</p>}
              </div>
            )}
          </div>

          <div className="flex justify-end pt-4 border-t border-surface-border">
            <Button
              type="submit"
              variant="primary"
              className="px-8"
              disabled={isSubmitting}
            >
              <RiSendPlaneFill size={18} className="mr-2" />
              {isSubmitting ? 'Đang gửi...' : 'Gửi thông báo'}
            </Button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default AdminNotificationsPage
