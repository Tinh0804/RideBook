import { Outlet, Link } from 'react-router-dom'
import { RiSunLine, RiMoonLine } from 'react-icons/ri'
import { useUIStore } from '@/store/rootStore'

const AuthLayout = () => {
  const { theme, toggleTheme } = useUIStore()

  return (
    <div className="min-h-screen flex">
    {/* Left branding panel */}
    <div className="hidden lg:flex lg:w-1/2 relative overflow-hidden bg-mesh-green">
      <div className="absolute inset-0 bg-gradient-to-br from-brand-950/80 via-brand-900/60 to-surface-dark/90" />

      {/* Decorative circles */}
      <div className="absolute -top-24 -left-24 w-96 h-96 rounded-full bg-brand-500/10 blur-3xl" />
      <div className="absolute -bottom-24 -right-24 w-96 h-96 rounded-full bg-brand-400/8 blur-3xl" />
      <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[500px] h-[500px] rounded-full border border-brand-500/10" />
      <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[350px] h-[350px] rounded-full border border-brand-500/15" />

      <div className="relative z-10 flex flex-col justify-between p-12 w-full">
        {/* Logo */}
        <Link to="/" className="flex items-center gap-3">
          <img src="/logo.png" alt="" className="w-10 h-10 rounded-xl object-cover shadow-glow-green" />
          <span className="font-display text-2xl font-bold text-white">BookCar</span>
        </Link>

        {/* Central content */}
        <div className="space-y-6">
          <div className="space-y-2">
            <p className="text-brand-400 font-mono text-sm tracking-widest uppercase">
              Di chuyển thông minh
            </p>
            <h1 className="font-display text-5xl font-bold text-white leading-tight">
              Đặt xe nhanh.<br />
              <span className="text-gradient">An toàn.</span><br />
              Tiện lợi.
            </h1>
          </div>
          <p className="text-gray-400 text-lg leading-relaxed max-w-sm">
            Nền tảng kết nối hàng nghìn tài xế và khách hàng trên khắp cả nước.
          </p>

          {/* Stats */}
          <div className="grid grid-cols-3 gap-4 pt-4">
            {[
              { value: '10K+', label: 'Tài xế' },
              { value: '50K+', label: 'Khách hàng' },
              { value: '99%',  label: 'Hài lòng' },
            ].map((s) => (
              <div key={s.label} className="glass rounded-xl p-4">
                <div className="font-display text-2xl font-bold text-brand-400">{s.value}</div>
                <div className="text-xs text-gray-400 mt-0.5">{s.label}</div>
              </div>
            ))}
          </div>
        </div>

        {/* Footer */}
        <p className="text-gray-600 text-sm">
          © {new Date().getFullYear()} BookCar. All rights reserved.
        </p>
      </div>
    </div>

    {/* Right auth form panel */}
    <div className="flex-1 flex flex-col items-center justify-center p-6 lg:p-12 relative overflow-y-auto">
      
      {/* Theme Toggle Overlay */}
      <button
        onClick={toggleTheme}
        className="absolute top-6 right-6 p-2 rounded-lg text-content-muted hover:text-content-main hover:bg-surface-border transition-colors"
        title="Đổi giao diện"
      >
        {theme === 'dark' ? <RiSunLine size={20} /> : <RiMoonLine size={20} />}
      </button>

      {/* Mobile logo */}
      <Link to="/" className="flex items-center gap-2 mb-8 lg:hidden">
        <img src="/logo.png" alt="" className="w-9 h-9 rounded-xl object-cover" />
        <span className="font-display text-xl font-bold text-content-main">BookCar</span>
      </Link>

      <div className="w-full max-w-md animate-slide-up">
        <Outlet />
      </div>
    </div>
  </div>
  )
}

export default AuthLayout
