import { cn } from '@/utils/cn'

const Checkbox = ({ label, error, className, ...props }) => (
  <label className={cn('flex items-start gap-3 cursor-pointer group', className)}>
    <div className="relative mt-0.5">
      <input
        type="checkbox"
        className="peer sr-only"
        {...props}
      />
      <div className={cn(
        'w-5 h-5 rounded border-2 border-surface-muted bg-surface-dark',
        'peer-checked:bg-brand-500 peer-checked:border-brand-500',
        'peer-focus:ring-2 peer-focus:ring-brand-500/40',
        'transition-all duration-150',
        error && 'border-red-500',
      )}>
        <svg
          className="w-3 h-3 text-content-main absolute inset-0 m-auto opacity-0 peer-checked:opacity-100 transition-opacity"
          viewBox="0 0 12 10" fill="none"
        >
          <path d="M1 5l3.5 3.5L11 1" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
        </svg>
      </div>
    </div>
    {label && (
      <span className="text-sm text-content-muted group-hover:text-gray-200 transition-colors leading-5">
        {label}
      </span>
    )}
  </label>
)

export default Checkbox
