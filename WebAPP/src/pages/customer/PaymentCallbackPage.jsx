import { useEffect, useState } from 'react'
import { useSearchParams, useNavigate } from 'react-router-dom'
import { RiCheckboxCircleLine, RiCloseCircleLine, RiLoader4Line } from 'react-icons/ri'
import { paymentApi } from '@/features/payment/api/paymentApi'
import { useBookingStore } from '@/store/rootStore'
import Button from '@/components/Elements/Button'
import { formatCurrency } from '@/utils/currency'

/**
 * This page receives the redirect from VNPay / Momo after payment
 * URL: /payment/callback?bookingId=xxx&status=SUCCESS|FAILED&...
 */
const PaymentCallbackPage = () => {
  const [searchParams] = useSearchParams()
  const navigate       = useNavigate()
  const { currentBooking, clearCurrentBooking } = useBookingStore()

  const [status,  setStatus]  = useState('loading')  // loading | success | failed
  const [booking, setBooking] = useState(null)

  const bookingId = searchParams.get('bookingId') || currentBooking?.id
  const vnpStatus = searchParams.get('vnp_ResponseCode')   // '00' = success
  const momoCode  = searchParams.get('resultCode')         // '0' = success

  useEffect(() => {
    if (!bookingId) { navigate('/customer/home'); return }

    // Check payment status from backend
    paymentApi.getStatus(bookingId)
      .then((result) => {
        setBooking(result)
        const paid = result?.paymentStatus === 'PAID' ||
                     vnpStatus === '00' ||
                     momoCode  === '0'
        if (paid) {
          toast.success('Thanh toán thành công! Đang tìm tài xế...')
          navigate('/customer/booking', { state: { paymentSuccess: true }, replace: true })
        } else {
          setStatus('failed')
        }
      })
      .catch(() => {
        // Fallback to URL params
        const paid = vnpStatus === '00' || momoCode === '0'
        if (paid) {
          toast.success('Thanh toán thành công! Đang tìm tài xế...')
          navigate('/customer/booking', { state: { paymentSuccess: true }, replace: true })
        } else {
          setStatus('failed')
        }
      })
  }, [bookingId, vnpStatus, momoCode, navigate])

  const handleContinue = () => {
    if (status === 'success') {
      navigate('/customer/tracking', { state: { bookingId } })
    } else {
      clearCurrentBooking()
      navigate('/customer/booking')
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center p-6">
      <div className="card p-10 max-w-sm w-full text-center space-y-6 animate-slide-up">
        {status === 'loading' && (
          <>
            <RiLoader4Line size={56} className="text-brand-400 mx-auto animate-spin" />
            <div>
              <h2 className="font-display text-2xl font-bold text-content-main">Đang xử lý</h2>
              <p className="text-content-muted mt-2">Vui lòng chờ xác nhận thanh toán...</p>
            </div>
          </>
        )}

        {status === 'success' && (
          <>
            <div className="relative mx-auto w-20 h-20">
              <div className="absolute inset-0 rounded-full bg-brand-500/20 animate-ping" />
              <div className="relative w-20 h-20 rounded-full bg-brand-500/15 border-2 border-brand-500/40 flex items-center justify-center">
                <RiCheckboxCircleLine size={40} className="text-brand-400" />
              </div>
            </div>
            <div>
              <h2 className="font-display text-2xl font-bold text-content-main">Thanh toán thành công!</h2>
              <p className="text-content-muted mt-2">Đang chuyển hướng về trang chuyến đi...</p>
            </div>
          </>
        )}

        {status === 'failed' && (
          <>
            <div className="w-20 h-20 rounded-full bg-red-500/15 border-2 border-red-500/30 flex items-center justify-center mx-auto">
              <RiCloseCircleLine size={40} className="text-red-400" />
            </div>
            <div>
              <h2 className="font-display text-2xl font-bold text-content-main">Thanh toán thất bại</h2>
              <p className="text-content-muted mt-2">Giao dịch không thành công. Vui lòng thử lại.</p>
            </div>
            <div className="space-y-2">
              <Button fullWidth onClick={handleContinue}>Thử lại</Button>
              <Button variant="ghost" fullWidth onClick={() => navigate('/customer/home')}>
                Về trang chủ
              </Button>
            </div>
          </>
        )}
      </div>
    </div>
  )
}

export default PaymentCallbackPage
