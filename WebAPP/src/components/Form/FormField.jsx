import { cn } from '@/utils/cn'

/**
 * FormField wraps a label + input + error message
 */
const FormField = ({
  label,
  error,
  required,
  hint,
  children,
  className,
}) => (
  <div className={cn('flex flex-col gap-1.5', className)}>
    {label && (
      <label className="text-sm font-medium text-content-muted">
        {label}
        {required && <span className="text-red-400 ml-1">*</span>}
      </label>
    )}
    {children}
    {hint && !error && (
      <p className="text-xs text-content-muted">{hint}</p>
    )}
    {error && (
      <p className="text-xs text-red-400 flex items-center gap-1">
        <span>⚠</span> {error}
      </p>
    )}
  </div>
)

export default FormField
