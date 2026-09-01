import { motion, useReducedMotion } from 'motion/react'
import { RiMapPin2Fill } from 'react-icons/ri'
import { cn } from '@/utils/cn'

const stages = ['Điểm đón', 'Chọn xe', 'Tìm tài xế']

const BookingJourneyProgress = ({ step, className }) => {
  const reduceMotion = useReducedMotion()
  const progress = Math.max(0, Math.min(2, step - 1)) * 50

  return (
    <div className={cn('w-full px-3 pb-1 pt-5', className)}>
      <div className="relative mx-6 h-5">
        <div className="absolute inset-x-0 top-2 z-0 h-px bg-surface-border" />
        <motion.div
          className="absolute left-0 top-[-20px] z-20 w-14 -translate-x-1/2"
          animate={{ left: `${progress}%` }}
          transition={reduceMotion ? { duration: 0 } : { type: 'spring', stiffness: 90, damping: 16 }}
        >
          <img src="/images/bookcar-booking-car.webp" alt="" className="w-full object-contain" />
        </motion.div>
        {stages.map((label, index) => (
          <span
            key={label}
            className={cn(
              'absolute top-[-4px] z-0 -translate-x-1/2 transition-all',
              index === step - 1
                ? 'scale-110 text-brand-500'
                : index < step - 1
                  ? 'text-brand-400'
                  : 'text-content-muted/35'
            )}
            style={{ left: `${index * 50}%` }}
          >
            <RiMapPin2Fill size={22} />
          </span>
        ))}
      </div>
      <div className="grid grid-cols-3 text-center text-[11px] font-semibold text-content-muted">
        {stages.map((label, index) => (
          <span key={label} className={index === step - 1 ? 'text-brand-600 dark:text-brand-400' : ''}>
            {label}
          </span>
        ))}
      </div>
    </div>
  )
}

export default BookingJourneyProgress
