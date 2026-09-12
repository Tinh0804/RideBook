import { useEffect } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { toast } from 'react-hot-toast'
import { motion } from 'motion/react'
import {
  RiArrowRightLine,
  RiCarLine,
  RiFlashlightLine,
  RiShieldCheckLine,
  RiShieldStarLine,
  RiStarLine,
  RiSteering2Line,
  RiUser3Line,
} from 'react-icons/ri'

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
    <div className="relative min-h-[100dvh] overflow-x-hidden bg-slate-950 text-white selection:bg-lime-accent/30">
      {/* Background Image */}
      <motion.img
        initial={{ opacity: 0, scale: 1.04 }}
        animate={{ opacity: 1, scale: 1 }}
        transition={{ duration: 1, ease: [0.16, 1, 0.3, 1] }}
        src="/images/bookcar-welcome-open-road.webp"
        alt="BookCar - Cung đường mở ra tương lai"
        className="absolute inset-0 h-full w-full object-cover object-center"
      />

      {/* Atmospheric dark overlays: Darker on the left for contrast, lighter on the right to reveal scenery */}
      <div className="absolute inset-0 bg-[linear-gradient(90deg,rgba(2,6,23,.88)_0%,rgba(2,6,23,.50)_50%,rgba(2,6,23,.15)_100%)]" />
      <div className="absolute inset-0 bg-gradient-to-t from-slate-950 via-slate-950/30 to-slate-950/50" />

      {/* Main Content */}
      <main className="relative z-10 mx-auto flex min-h-[100dvh] max-w-[1500px] flex-col justify-between px-5 pb-6 pt-28 sm:px-8 sm:pb-8 sm:pt-32 lg:px-12">
        
        {/* Middle/Hero section: Left Title + Right Subtle Glass Card */}
        <div className="my-auto grid grid-cols-1 items-center gap-10 py-6 lg:grid-cols-12 lg:gap-14">
          
          {/* Left Column: Heading & Concise Value Proposition */}
          <motion.div
            initial={{ opacity: 0, y: 24 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.15, duration: 0.65, ease: [0.16, 1, 0.3, 1] }}
            className="lg:col-span-7"
          >
            <div className="inline-flex items-center gap-2 rounded-full border border-lime-accent/30 bg-lime-accent/10 px-3.5 py-1.5 text-xs font-semibold uppercase tracking-wider text-lime-accent backdrop-blur-md">
              <span className="h-2 w-2 rounded-full bg-lime-accent animate-pulse" />
              BookCar đưa bạn đi xa hơn
            </div>

            <h1 className="mt-5 font-display text-4xl font-bold leading-[1.05] tracking-tight !text-white sm:text-6xl lg:text-[70px]">
              <span className="block !text-white">Đi theo</span>
              <span className="block translate-x-[.3em] !text-white/80">cách của</span>
              <span className="block translate-x-[.15em] !text-white">chính bạn.</span>
            </h1>

            <p className="mt-5 max-w-lg text-base text-white/80 sm:text-lg">
              Đặt xe thông minh, an toàn và minh bạch trên mọi hành trình.
            </p>

            {/* Quick perk badges - Clean inline list */}
            <div className="mt-7 flex flex-wrap items-center gap-5 text-xs sm:text-sm font-medium text-white/75">
              <div className="flex items-center gap-1.5">
                <RiFlashlightLine className="text-lime-accent" size={16} />
                <span>Đón xe trong 3 - 5p</span>
              </div>
              <span className="h-1 w-1 rounded-full bg-white/30" />
              <div className="flex items-center gap-1.5">
                <RiShieldCheckLine className="text-lime-accent" size={16} />
                <span>An tâm 100%</span>
              </div>
              <span className="h-1 w-1 rounded-full bg-white/30" />
              <div className="flex items-center gap-1.5">
                <RiStarLine className="text-lime-accent" size={16} />
                <span>Đánh giá 4.9★</span>
              </div>
            </div>
          </motion.div>

          {/* Right Column: Decorative Card mờ trong nền (Frosted Glass) */}
          <motion.div
            initial={{ opacity: 0, scale: 0.95, y: 20 }}
            animate={{ opacity: 1, scale: 1, y: 0 }}
            transition={{ delay: 0.25, duration: 0.65, ease: [0.16, 1, 0.3, 1] }}
            className="hidden lg:block lg:col-span-5"
          >
            {/* Card vỏ kính mờ xuyên thấu hậu cảnh */}
            <div className="relative rounded-3xl border border-white/10 bg-slate-950/30 p-6 backdrop-blur-xl shadow-[0_8px_32px_rgba(0,0,0,0.37)]">
              
              {/* Card Header gọn nhẹ */}
              <div className="flex items-center justify-between border-b border-white/10 pb-4">
                <div className="flex items-center gap-3">
                  <div className="grid h-9 w-9 place-items-center rounded-xl bg-lime-accent/15 text-lime-accent">
                    <RiCarLine size={20} />
                  </div>
                  <div>
                    <h3 className="text-sm font-semibold !text-white">Mạng lưới BookCar</h3>
                  </div>
                </div>
                <span className="flex items-center gap-1.5 rounded-full bg-emerald-500/15 px-2.5 py-1 text-xs font-medium text-emerald-400">
                  <span className="h-1.5 w-1.5 rounded-full bg-emerald-400 animate-ping" />
                  Sẵn sàng 24/7
                </span>
              </div>

              {/* 2 Thống kê chính - Tối giản chữ */}
              <div className="mt-4 grid grid-cols-2 gap-3">
                <div className="rounded-2xl border border-white/5 bg-white/[0.03] p-3.5 backdrop-blur-sm">
                  <span className="text-xs text-white/55">Tài xế hoạt động</span>
                  <p className="mt-0.5 font-display text-2xl font-bold text-lime-accent">15,000+</p>
                </div>
                <div className="rounded-2xl border border-white/5 bg-white/[0.03] p-3.5 backdrop-blur-sm">
                  <span className="text-xs text-white/55">Hài lòng dịch vụ</span>
                  <p className="mt-0.5 font-display text-2xl font-bold !text-white">99.8%</p>
                </div>
              </div>

              {/* Lựa chọn xe - Tag tinh gọn */}
              <div className="mt-3 flex items-center justify-between gap-2 text-xs">
                <span className="flex-1 rounded-xl border border-white/5 bg-white/[0.03] py-2 text-center text-white/80">Xe 2 bánh</span>
                <span className="flex-1 rounded-xl border border-white/5 bg-white/[0.03] py-2 text-center text-white/80">Car 4 chỗ</span>
                <span className="flex-1 rounded-xl border border-white/5 bg-white/[0.03] py-2 text-center text-white/80">Car 7 chỗ</span>
              </div>
            </div>
          </motion.div>

        </div>

        {/* Bottom Action Cards: CÙNG MỘT HÀNG NGANG (EXACT SAME ROW) */}
        <motion.div
          initial={{ opacity: 0, y: 28 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.35, duration: 0.65, ease: [0.16, 1, 0.3, 1] }}
          className="w-full"
        >
          {/* 2 Buttons Row: Customer on Left, Driver on Right, Exactly Equal Heights & Alignment */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-5 lg:gap-6 items-stretch w-full">
            
            {/* Customer Card (Left) */}
            <Link
              to="/login/customer"
              className="group relative flex min-h-[135px] flex-col justify-between rounded-2xl bg-lime-accent p-6 text-slate-950 shadow-xl transition-all duration-300 hover:-translate-y-1 hover:bg-[#b8ff59] hover:shadow-[0_20px_40px_-15px_rgba(184,255,89,0.35)] active:scale-[0.99]"
            >
              <div className="flex items-start justify-between">
                <div className="rounded-xl bg-slate-950/10 p-3 text-slate-950">
                  <RiUser3Line size={28} />
                </div>
                <div className="grid h-10 w-10 place-items-center rounded-full bg-slate-950/10 transition-transform duration-300 group-hover:translate-x-1">
                  <RiArrowRightLine size={22} />
                </div>
              </div>
              <div className="mt-3">
                <strong className="block text-2xl font-bold tracking-tight text-slate-950 sm:text-3xl">
                  Đặt một chuyến xe
                </strong>
                <span className="mt-0.5 block text-sm font-semibold text-slate-800/80">
                  Dành cho khách hàng
                </span>
              </div>
            </Link>

            {/* Driver Card (Right) */}
            <Link
              to="/login/driver"
              className="group relative flex min-h-[135px] flex-col justify-between rounded-2xl border border-white/15 bg-slate-900/60 p-6 text-white backdrop-blur-xl shadow-xl transition-all duration-300 hover:-translate-y-1 hover:border-lime-accent/50 hover:bg-slate-900/80 hover:shadow-[0_20px_40px_-15px_rgba(0,0,0,0.6)] active:scale-[0.99]"
            >
              <div className="flex items-start justify-between">
                <div className="rounded-xl bg-white/10 p-3 text-lime-accent">
                  <RiSteering2Line size={28} />
                </div>
                <div className="grid h-10 w-10 place-items-center rounded-full bg-white/10 text-white transition-transform duration-300 group-hover:translate-x-1 group-hover:text-lime-accent">
                  <RiArrowRightLine size={22} />
                </div>
              </div>
              <div className="mt-3">
                <strong className="block text-2xl font-bold tracking-tight !text-white sm:text-3xl">
                  Bắt đầu cầm lái
                </strong>
                <span className="mt-0.5 block text-sm font-semibold text-white/70">
                  Dành cho đối tác tài xế
                </span>
              </div>
            </Link>

          </div>

          {/* Sub-footer Bar: Slogan on Left, Admin on Right */}
          <div className="mt-4 flex flex-col sm:flex-row items-center justify-between gap-3 text-xs sm:text-sm text-white/60">
            <div className="flex items-center gap-2">
              <span className="h-1.5 w-1.5 rounded-full bg-lime-accent" />
              <span>BookCar — Đồng hành cùng bạn trên mọi nẻo đường</span>
            </div>
            
            <Link
              to="/login/admin"
              className="group flex items-center gap-1.5 text-white/60 transition-colors hover:text-white font-medium"
            >
              <RiShieldStarLine size={16} />
              <span>Truy cập dành cho quản trị viên</span>
              <RiArrowRightLine className="transition-transform group-hover:translate-x-1" />
            </Link>
          </div>
        </motion.div>

      </main>
    </div>
  )
}

export default WelcomePage


