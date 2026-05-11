import { cn } from '@/utils/cn'

const sizeMap = {
  xs: 'w-3 h-3 border',
  sm: 'w-4 h-4 border-2',
  md: 'w-6 h-6 border-2',
  lg: 'w-8 h-8 border-[3px]',
  xl: 'w-12 h-12 border-4',
}

const Spinner = ({ size = 'md', className }) => (
  <span
    className={cn(
      'inline-block rounded-full border-brand-500 border-t-transparent animate-spin',
      sizeMap[size],
      className
    )}
    role="status"
    aria-label="Loading"
  />
)

export default Spinner
