import React from 'react'
import { cn } from '@/utils/cn'

/**
 * Input component
 */
const Input = React.forwardRef(
  ({ className, type = 'text', prefix, suffix, error, ...props }, ref) => {
    if (prefix || suffix) {
      return (
        <div className="relative flex items-center">
          {prefix && (
            <span className="absolute left-3 text-content-muted pointer-events-none">
              {prefix}
            </span>
          )}
          <input
            ref={ref}
            type={type}
            className={cn(
              'input-field text-ellipsis',
              prefix && 'pl-10',
              suffix && 'pr-10',
              error && 'border-red-500 focus:border-red-500 focus:ring-red-500/30',
              className
            )}
            {...props}
          />
          {suffix && (
            <span className="absolute right-3 text-content-muted">{suffix}</span>
          )}
        </div>
      )
    }

    return (
      <input
        ref={ref}
        type={type}
        className={cn(
          'input-field text-ellipsis',
          error && 'border-red-500 focus:border-red-500 focus:ring-red-500/30',
          className
        )}
        {...props}
      />
    )
  }
)

Input.displayName = 'Input'
export default Input
