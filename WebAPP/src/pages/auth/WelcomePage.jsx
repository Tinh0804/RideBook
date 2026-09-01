import { useEffect } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { toast } from 'react-hot-toast'
import { motion } from 'motion/react'
import { RiArrowRightLine, RiShieldStarLine, RiSteering2Line, RiUser3Line } from 'react-icons/ri'

const WelcomePage = () => {
  const location = useLocation()
  const navigate = useNavigate()

  useEffect(() => {
    const params = new URLSearchParams(location.search)
    if (params.get('sessionExpired') === 'true') {
      toast.error('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại!')
      navigate('/welcome', { replace: true })
    }
  }, [location, navigate])

  return (
    <div className="relative min-h-[100dvh] overflow-x-hidden bg-slate-950 text-white">
      <motion.img
        initial={{ opacity: 0, scale: 1.04 }}
        animate={{ opacity: 1, scale: 1 }}
        transition={{ duration: 1, ease: [0.16, 1, 0.3, 1] }}
        src="/images/bookcar-welcome-open-road.webp"
        alt="Xe di chuyển trên cung đường rộng hướng về thành phố lúc bình minh"
        className="absolute inset-0 h-full w-full object-cover object-center"
      />
      <div className="absolute inset-0 bg-[linear-gradient(90deg,rgba(2,6,23,.78)_0%,rgba(2,6,23,.36)_48%,rgba(2,6,23,.08)_100%)]" />
      <div className="absolute inset-0 bg-gradient-to-t from-slate-950 via-transparent to-slate-950/30" />

      <main className="relative z-10 mx-auto flex min-h-[100dvh] max-w-[1500px] flex-col justify-between px-5 pb-6 pt-28 sm:px-8 sm:pb-8 lg:px-12 lg:pt-28">
        <motion.div
          initial={{ opacity: 0, y: 24 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.15, duration: 0.65, ease: [0.16, 1, 0.3, 1] }}
        >
          <p className="mb-4 flex items-center gap-2 text-sm font-bold text-lime-accent">
            <span className="h-2 w-2 rounded-full bg-lime-accent" />
            BookCar đưa bạn đi xa hơn
          </p>
          <h1 className="max-w-[11ch] font-display text-[42px] font-bold leading-[.88] tracking-[-0.055em] sm:text-[58px] lg:text-[66px] xl:text-[72px]">
            <span className="block">Đi theo</span>
            <span className="block translate-x-[.7em] text-white/65">cách của</span>
            <span className="block translate-x-[.2em]">chính bạn.</span>
          </h1>
          <div className="mt-5 flex items-center gap-3">
            <span className="h-px w-20 bg-lime-accent sm:w-28" />
            <p className="text-sm font-medium text-white/60">Chọn vai trò để bắt đầu hành trình.</p>
          </div>
        </motion.div>

        <motion.div
          initial={{ opacity: 0, y: 28 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.35, duration: 0.65, ease: [0.16, 1, 0.3, 1] }}
          className="mt-10"
        >
          <div className="grid gap-3 md:grid-cols-[170px_1fr_1fr]">
            <div className="flex items-center justify-between rounded-2xl border border-white/20 bg-slate-950/55 p-4 text-white/55 backdrop-blur-md md:min-h-32 md:flex-col md:items-start md:p-5">
              <span className="text-xs font-bold">Chọn điểm khởi hành</span>
              <RiArrowRightLine className="rotate-90 text-lime-accent" size={20} />
            </div>

            <Link
              to="/login/customer"
              className="group flex min-h-28 flex-col justify-between rounded-2xl bg-lime-accent p-5 text-slate-950 transition-all hover:-translate-y-1 hover:bg-[#b8ff59] active:scale-[.98] md:min-h-32"
            >
              <span className="flex items-start justify-between">
                <RiUser3Line size={25} />
                <RiArrowRightLine className="transition-transform group-hover:translate-x-1" size={22} />
              </span>
              <span>
                <strong className="block text-xl font-bold tracking-tight sm:text-2xl">Đặt một chuyến xe</strong>
                <span className="mt-1 block text-sm font-medium text-slate-700">Dành cho khách hàng</span>
              </span>
            </Link>

            <Link
              to="/login/driver"
              className="group flex min-h-28 flex-col justify-between rounded-2xl border border-white/20 bg-slate-950/55 p-5 text-white backdrop-blur-md transition-all hover:-translate-y-1 hover:border-white/40 hover:bg-slate-950/70 active:scale-[.98] md:min-h-32"
            >
              <span className="flex items-start justify-between">
                <RiSteering2Line size={26} />
                <RiArrowRightLine className="transition-transform group-hover:translate-x-1" size={22} />
              </span>
              <span>
                <strong className="block text-xl font-bold tracking-tight sm:text-2xl">Bắt đầu cầm lái</strong>
                <span className="mt-1 block text-sm font-medium text-white/55">Dành cho đối tác tài xế</span>
              </span>
            </Link>
          </div>

          <Link
            to="/login/admin"
            className="group ml-auto mt-4 flex w-fit items-center gap-2 text-sm font-semibold text-white/55 transition-colors hover:text-white"
          >
            <RiShieldStarLine size={17} />
            Truy cập dành cho quản trị viên
            <RiArrowRightLine className="transition-transform group-hover:translate-x-1" />
          </Link>
        </motion.div>
      </main>
    </div>
  )
}

export default WelcomePage
