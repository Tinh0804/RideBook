import { useNavigate } from 'react-router-dom'
import Button from '@/components/Elements/Button'

const NotFoundPage = () => {
  const navigate = useNavigate()
  return (
    <div className="min-h-screen flex flex-col items-center justify-center text-center p-6 space-y-6">
      <div className="space-y-2">
        <p className="font-mono text-8xl font-bold text-gradient">404</p>
        <h1 className="font-display text-3xl font-bold text-content-main">Trang không tồn tại</h1>
        <p className="text-content-muted max-w-sm">
          Trang bạn đang tìm kiếm đã bị xóa hoặc không tồn tại.
        </p>
      </div>
      <div className="flex gap-3">
        <Button onClick={() => navigate(-1)} variant="outline">← Quay lại</Button>
        <Button onClick={() => navigate('/')}>Về trang chủ</Button>
      </div>
    </div>
  )
}

export default NotFoundPage
