import { Link, useLocation, useNavigate } from 'react-router-dom'
import { RiUserLine, RiCarFill, RiShieldLine } from 'react-icons/ri'
import { useEffect } from 'react'
import { toast } from 'react-hot-toast'

const WelcomePage = () => {
  const location = useLocation()
  const navigate = useNavigate()

  useEffect(() => {
    const params = new URLSearchParams(location.search)
    if (params.get('sessionExpired') === 'true') {
      toast.error('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại!')
      // Clear param without reloading
      navigate('/welcome', { replace: true })
    }
  }, [location, navigate])
  return (
    <div className="flex flex-col items-center justify-center space-y-10 animate-slide-up w-full">
      <div className="text-center space-y-3">
        <h2 className="font-display text-4xl font-bold text-content-main">
          Chào mừng bạn đến với BookCar
        </h2>
        <p className="text-content-muted text-lg">
          Vui lòng chọn vai trò để tiếp tục
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6 w-full max-w-lg">
        <Link
          to="/login/customer"
          className="group relative overflow-hidden rounded-2xl border border-surface-border bg-surface-dark p-6 transition-all duration-300 hover:border-brand-500 hover:shadow-glow-green"
        >
          <div className="absolute top-0 right-0 p-4 opacity-10 transition-opacity duration-300 group-hover:opacity-20">
            <RiUserLine size={80} className="text-brand-500" />
          </div>
          <div className="relative z-10 flex flex-col items-center gap-4 text-center">
            <div className="flex h-16 w-16 items-center justify-center rounded-full bg-brand-500/10 text-brand-500 transition-transform duration-300 group-hover:scale-110">
              <RiUserLine size={32} />
            </div>
            <div>
              <h3 className="font-display text-xl font-bold text-content-main">Khách hàng</h3>
              <p className="mt-2 text-sm text-content-muted">
                Đặt xe di chuyển nhanh chóng, an toàn và tiện lợi.
              </p>
            </div>
          </div>
        </Link>

        <Link
          to="/login/driver"
          className="group relative overflow-hidden rounded-2xl border border-surface-border bg-surface-dark p-6 transition-all duration-300 hover:border-blue-500 hover:shadow-glow-blue"
        >
          <div className="absolute top-0 right-0 p-4 opacity-10 transition-opacity duration-300 group-hover:opacity-20">
            <RiCarFill size={80} className="text-blue-500" />
          </div>
          <div className="relative z-10 flex flex-col items-center gap-4 text-center">
            <div className="flex h-16 w-16 items-center justify-center rounded-full bg-blue-500/10 text-blue-500 transition-transform duration-300 group-hover:scale-110">
              <RiCarFill size={32} />
            </div>
            <div>
              <h3 className="font-display text-xl font-bold text-content-main">Tài xế</h3>
              <p className="mt-2 text-sm text-content-muted">
                Nhận chuyến xe, quản lý thu nhập dễ dàng.
              </p>
            </div>
          </div>
        </Link>
      </div>

      <div className="mt-8 text-center">
        <Link
          to="/login/admin"
          className="flex items-center justify-center gap-2 text-sm text-content-muted transition-colors hover:text-content-main"
        >
          <RiShieldLine size={16} />
          Đăng nhập với tư cách Quản trị viên
        </Link>
      </div>
    </div>
  )
}

export default WelcomePage
