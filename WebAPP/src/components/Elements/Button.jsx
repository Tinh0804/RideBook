import React from 'react'
import { cn } from '@/utils/cn'
import Spinner from './Spinner'

const variantMap = {
  primary: 'btn-primary',
  outline: 'btn-outline',
  ghost:   'btn-ghost',
  danger:  'btn-danger',
}

const sizeMap = {
  sm: 'btn-sm',
  md: '',
  lg: 'btn-lg',
}

/**
 * Button component
 * @param {string}  variant   - primary | outline | ghost | danger
 * @param {string}  size      - sm | md | lg
 * @param {boolean} loading   - show spinner
 * @param {boolean} fullWidth - stretch to container width
 */
const Button = React.forwardRef(
  (
    {
      children,
      variant   = 'primary',
      size      = 'md',
      loading   = false,
      fullWidth = false,
      className,
      disabled,
      ...props
    },
    ref
  ) => {
    return (
      <button
        ref={ref}
        className={cn(
          variantMap[variant],
          sizeMap[size],
          fullWidth && 'w-full',
          className
        )}
        disabled={disabled || loading}
        {...props}
      >
        {loading ? <Spinner size="sm" /> : children}
      </button>
    )
  }
)

Button.displayName = 'Button'
export default Button
