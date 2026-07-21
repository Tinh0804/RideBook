/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        // BookCar Brand Colors
        brand: {
          50:  '#f0fdf4',
          100: '#dcfce7',
          200: '#bbf7d0',
          300: '#86efac',
          400: '#4ade80',
          500: '#22c55e',
          600: '#16a34a',
          700: '#15803d',
          800: '#166534',
          900: '#14532d',
          950: '#052e16',
        },
        lime: {
          accent: '#A8FF3E',
        },
        surface: {
          dark:    'rgb(var(--color-surface-dark) / <alpha-value>)',
          card:    'rgb(var(--color-surface-card) / <alpha-value>)',
          border:  'rgb(var(--color-surface-border) / <alpha-value>)',
          muted:   'rgb(var(--color-surface-muted) / <alpha-value>)',
        },
        content: {
          main:    'rgb(var(--color-content-main) / <alpha-value>)',
          muted:   'rgb(var(--color-content-muted) / <alpha-value>)',
        },
      },
      fontFamily: {
        sans:   ['Inter', 'sans-serif'],
        mono:   ['"Space Mono"', 'monospace'],
        display:['"Plus Jakarta Sans"', 'sans-serif'],
      },
      animation: {
        'slide-up':    'slideUp 0.4s ease-out',
        'fade-in':     'fadeIn 0.3s ease-out',
        'pulse-slow':  'pulse 3s cubic-bezier(0.4, 0, 0.6, 1) infinite',
        'spin-slow':   'spin 3s linear infinite',
        'bounce-sm':   'bounceSm 1s infinite',
      },
      keyframes: {
        slideUp: {
          '0%':   { opacity: 0, transform: 'translateY(20px)' },
          '100%': { opacity: 1, transform: 'translateY(0)' },
        },
        fadeIn: {
          '0%':   { opacity: 0 },
          '100%': { opacity: 1 },
        },
        bounceSm: {
          '0%, 100%': { transform: 'translateY(0)' },
          '50%':      { transform: 'translateY(-4px)' },
        },
      },
      backgroundImage: {
        'gradient-radial':  'radial-gradient(var(--tw-gradient-stops))',
        'mesh-green':       'radial-gradient(at 40% 20%, hsla(134,70%,20%,1) 0px, transparent 50%), radial-gradient(at 80% 0%, hsla(145,60%,10%,1) 0px, transparent 50%), radial-gradient(at 0% 50%, hsla(130,80%,15%,1) 0px, transparent 50%)',
      },
      boxShadow: {
        'glow-green': '0 0 20px rgba(34, 197, 94, 0.3)',
        'glow-lime':  '0 0 30px rgba(168, 255, 62, 0.25)',
        'card':       '0 4px 24px rgba(0,0,0,0.4)',
      },
    },
  },
  plugins: [],
}
