import { Outlet, Link, useLocation } from 'react-router-dom'
import { RiMoonLine, RiSunLine } from 'react-icons/ri'
import { useUIStore } from '@/store/rootStore'

const AuthLayout = () => {
  const { theme, toggleTheme } = useUIStore()
  const pathname = useLocation().pathname
  const isLogin = pathname.startsWith('/login/')
  const isWelcome = pathname === '/welcome'
  const isRegisterCustomer = pathname === '/register/customer'
  const isSplit = isLogin || isWelcome || isRegisterCustomer

  return (
    <div className="min-h-[100dvh] bg-surface-dark text-content-main selection:bg-lime-accent/30">
      <header className="absolute inset-x-0 top-0 z-30 flex h-16 sm:h-20 items-center justify-between px-4 sm:px-8 lg:px-12">
        <Link to="/" className="group flex items-center gap-3" aria-label="BookCar - Trang chủ">
          <img src="/logo.png" alt="" className="h-8 w-8 sm:h-10 sm:w-10 rounded-[12px] sm:rounded-[14px] object-cover shadow-sm transition-transform duration-300 group-hover:-rotate-6" />
          <span className={`font-display text-lg sm:text-xl font-bold tracking-[-0.04em] ${isLogin ? 'lg:text-white' : ''} ${isWelcome ? 'text-white' : ''}`}>
            BookCar<span className="text-lime-accent">/</span>
          </span>
        </Link>

        <button
          type="button"
          onClick={toggleTheme}
          className={`grid h-8 w-8 sm:h-10 sm:w-10 place-items-center rounded-full border border-surface-border bg-surface-card/80 text-content-muted backdrop-blur transition hover:border-content-main/30 hover:text-content-main active:scale-95 ${isWelcome ? 'border-white/20 bg-slate-950/35 text-white hover:border-white/40 hover:text-white' : ''} ${isRegisterCustomer ? 'lg:border-white/20 lg:bg-slate-950/35 lg:text-white lg:hover:border-white/40 lg:hover:text-white' : ''}`}
          title="Đổi giao diện"
          aria-label="Đổi giao diện sáng tối"
        >
          {theme === 'dark' ? <RiSunLine size={18} /> : <RiMoonLine size={18} />}
        </button>
      </header>

      <main className={isSplit ? 'min-h-[100dvh]' : 'flex min-h-[100dvh] items-center justify-center px-6 pb-20 pt-28'}>
        <Outlet />
      </main>
    </div>
  )
}

export default AuthLayout
