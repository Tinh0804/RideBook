import { Outlet, Link, useLocation } from 'react-router-dom'
import { RiMoonLine, RiRouteLine, RiSunLine } from 'react-icons/ri'
import { useUIStore } from '@/store/rootStore'

const AuthLayout = () => {
  const { theme, toggleTheme } = useUIStore()
  const pathname = useLocation().pathname
  const isLogin = pathname.startsWith('/login/')
  const isWelcome = pathname === '/welcome'
  const isSplit = isLogin || isWelcome

  return (
    <div className="min-h-[100dvh] bg-surface-dark text-content-main selection:bg-lime-accent/30">
      <header className="absolute inset-x-0 top-0 z-30 flex h-20 items-center justify-between px-5 sm:px-8 lg:px-12">
        <Link to="/" className="group flex items-center gap-3" aria-label="BookCar - Trang chủ">
          <span className={`grid h-10 w-10 place-items-center rounded-[14px] bg-content-main text-surface-dark shadow-sm transition-transform duration-300 group-hover:-rotate-6 ${isSplit ? 'lg:bg-white lg:text-slate-950' : ''} ${isWelcome ? 'bg-white text-slate-950' : ''}`}>
            <RiRouteLine size={21} aria-hidden="true" />
          </span>
          <span className={`font-display text-xl font-bold tracking-[-0.04em] ${isSplit ? 'lg:text-white' : ''} ${isWelcome ? 'text-white' : ''}`}>
            BookCar<span className="text-lime-accent">/</span>
          </span>
        </Link>

        <button
          type="button"
          onClick={toggleTheme}
          className={`grid h-10 w-10 place-items-center rounded-full border border-surface-border bg-surface-card/80 text-content-muted backdrop-blur transition hover:border-content-main/30 hover:text-content-main active:scale-95 ${isWelcome ? 'border-white/20 bg-slate-950/35 text-white hover:border-white/40 hover:text-white' : ''}`}
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
