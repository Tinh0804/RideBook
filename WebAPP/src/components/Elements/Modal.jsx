import { useEffect } from 'react'
import { createPortal } from 'react-dom'
import { RiCloseLine } from 'react-icons/ri'
import { cn } from '@/utils/cn'

const sizeMap = {
  sm: 'max-w-sm',
  md: 'max-w-md',
  lg: 'max-w-lg',
  xl: 'max-w-xl',
  '2xl': 'max-w-2xl',
  full: 'max-w-full mx-4',
}

const Modal = ({
  isOpen,
  onClose,
  title,
  children,
  size     = 'md',
  showClose = true,
  className,
}) => {
  // Lock body scroll while modal open
  useEffect(() => {
    if (isOpen) document.body.style.overflow = 'hidden'
    else        document.body.style.overflow = ''
    return ()   => { document.body.style.overflow = '' }
  }, [isOpen])

  if (!isOpen) return null

  return createPortal(
    <div
      className="fixed inset-0 z-50 flex items-center justify-center p-4"
      role="dialog"
      aria-modal="true"
    >
      {/* Backdrop */}
      <div
        className="absolute inset-0 bg-black/70 backdrop-blur-sm animate-fade-in"
        onClick={onClose}
      />

      {/* Panel */}
      <div
        className={cn(
          'relative w-full card shadow-2xl animate-slide-up',
          sizeMap[size],
          className
        )}
      >
        {/* Header */}
        {(title || showClose) && (
          <div className="flex items-center justify-between px-6 py-4 border-b border-surface-border">
            {title && (
              <h3 className="font-display text-lg font-semibold text-content-main">
                {title}
              </h3>
            )}
            {showClose && (
              <button
                onClick={onClose}
                className="ml-auto p-1 text-content-muted hover:text-content-main hover:bg-surface-border rounded-lg transition-colors"
              >
                <RiCloseLine size={20} />
              </button>
            )}
          </div>
        )}

        {/* Body */}
        <div className="p-6">{children}</div>
      </div>
    </div>,
    document.body
  )
}

export default Modal
