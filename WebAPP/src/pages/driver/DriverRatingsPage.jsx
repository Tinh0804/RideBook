import { useState, useEffect } from 'react'
import { motion } from 'motion/react'
import { RiStarFill, RiStarLine, RiMessage2Line, RiUserLine } from 'react-icons/ri'
import { driverApi } from '@/features/driver/api/driverApi'
import { useAuthStore } from '@/store/rootStore'
import { formatDate } from '@/utils/formatDate'
import Spinner from '@/components/Elements/Spinner'
import { cn } from '@/utils/cn'

const DriverRatingsPage = () => {
  const { user, userProfile } = useAuthStore()
  const [ratings, setRatings] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const driverId = userProfile?.id || user?.id || user?.driverId
    if (!driverId) {
      setLoading(false)
      return
    }
    
    setLoading(true)
    driverApi.getRatings(driverId)
      .then((data) => {
        setRatings(data || [])
      })
      .catch(() => {
        setRatings([])
      })
      .finally(() => setLoading(false))
  }, [user, userProfile])

  if (loading) return (
    <div className="flex h-full items-center justify-center bg-[#e8ece3] dark:bg-surface-dark">
      <Spinner size="xl" />
    </div>
  )

  const averageRating = ratings.length > 0
    ? ratings.reduce((acc, curr) => acc + curr.score, 0) / ratings.length
    : 0;

  const getRatingSummary = () => {
    const counts = { 5: 0, 4: 0, 3: 0, 2: 0, 1: 0 }
    ratings.forEach(r => {
      const score = Math.round(r.score)
      if (counts[score] !== undefined) {
        counts[score]++
      }
    })
    return counts
  }

  const counts = getRatingSummary()

  return (
    <div className="h-full overflow-y-auto bg-[#e8ece3] p-fluid pb-10 dark:bg-surface-dark pointer-events-auto">
      <motion.div 
        initial={{ opacity: 0, x: -18 }}
        animate={{ opacity: 1, x: 0 }}
        transition={{ duration: 0.45, ease: [0.16, 1, 0.3, 1] }}
        className="mx-auto max-w-4xl space-y-6"
      >
        <div className="mb-2">
          <h1 className="font-display text-fluid-3xl font-bold text-gray-900 dark:text-white tracking-tight">Đánh giá từ khách hàng</h1>
          <p className="text-gray-500 dark:text-gray-400 text-sm mt-1">Xem phản hồi và điểm đánh giá của bạn</p>
        </div>

        {/* Overview Section */}
        <div className="bg-white dark:bg-surface-card rounded-2xl p-fluid md:p-8 border border-gray-100 dark:border-surface-border shadow-sm flex flex-col md:flex-row items-center gap-8 md:gap-12">
          {/* Average Score */}
          <div className="flex flex-col items-center justify-center text-center">
            <h2 className="text-[clamp(2.5rem,6vw,3.75rem)] font-display font-bold text-gray-900 dark:text-white mb-2">
              {averageRating.toFixed(1)}
            </h2>
            <div className="flex text-yellow-400 mb-2">
              {[1, 2, 3, 4, 5].map((star) => (
                <span key={star}>
                  {star <= Math.round(averageRating) ? (
                    <RiStarFill size={24} />
                  ) : (
                    <RiStarLine size={24} className="text-gray-300 dark:text-gray-600" />
                  )}
                </span>
              ))}
            </div>
            <p className="text-sm font-medium text-gray-500">{ratings.length} bài đánh giá</p>
          </div>

          {/* Rating Bars */}
          <div className="flex-1 w-full space-y-3">
            {[5, 4, 3, 2, 1].map((star) => {
              const count = counts[star]
              const percentage = ratings.length > 0 ? (count / ratings.length) * 100 : 0
              return (
                <div key={star} className="flex items-center gap-3">
                  <span className="w-4 text-sm font-bold text-gray-600 dark:text-gray-400">{star}</span>
                  <RiStarFill size={14} className="text-yellow-400 shrink-0" />
                  <div className="flex-1 h-2.5 bg-gray-100 dark:bg-surface-border rounded-full overflow-hidden">
                    <div 
                      className="h-full bg-yellow-400 rounded-full transition-all duration-1000"
                      style={{ width: `${percentage}%` }}
                    />
                  </div>
                  <span className="w-8 text-right text-xs font-medium text-gray-500">{count}</span>
                </div>
              )
            })}
          </div>
        </div>

        {/* Ratings List */}
        <div className="space-y-4">
          <h2 className="font-display text-fluid-xl font-bold text-gray-900 dark:text-white mt-8 mb-4">Chi tiết đánh giá</h2>
          
          {ratings.length === 0 ? (
            <div className="py-12 text-center bg-white dark:bg-surface-card rounded-2xl border border-dashed border-gray-200 dark:border-surface-border">
              <div className="w-16 h-16 rounded-full bg-gray-50 dark:bg-surface-dark flex items-center justify-center mx-auto mb-3">
                <RiMessage2Line size={24} className="text-gray-400" />
              </div>
              <p className="text-gray-500 font-medium">Chưa có đánh giá nào</p>
            </div>
          ) : (
            ratings.map((review, idx) => (
              <div key={idx} className="bg-white dark:bg-surface-card rounded-2xl p-fluid md:p-6 border border-gray-100 dark:border-surface-border shadow-sm flex flex-col sm:flex-row gap-5">
                <div className="w-12 h-12 rounded-full bg-brand-50 dark:bg-brand-500/10 flex items-center justify-center shrink-0">
                  <RiUserLine size={24} className="text-brand-500" />
                </div>
                <div className="flex-1">
                  <div className="flex flex-col sm:flex-row sm:justify-between sm:items-start gap-2 mb-2">
                    <div>
                      <h3 className="font-bold text-gray-900 dark:text-white">{review.customerName || 'Khách hàng'}</h3>
                      <p className="text-xs text-gray-500 mt-0.5">{formatDate(review.createdAt || new Date())}</p>
                    </div>
                    <div className="flex text-yellow-400 bg-yellow-50 dark:bg-yellow-500/10 px-2 py-1 rounded-md w-fit">
                      {[1, 2, 3, 4, 5].map((star) => (
                        <span key={star}>
                          {star <= (review.score || 0) ? (
                            <RiStarFill size={14} />
                          ) : (
                            <RiStarFill size={14} className="text-yellow-400/30" />
                          )}
                        </span>
                      ))}
                    </div>
                  </div>
                  {review.review && (
                    <p className="text-gray-700 dark:text-gray-300 text-sm mt-3 leading-relaxed bg-gray-50 dark:bg-surface-dark p-4 rounded-xl border border-gray-100 dark:border-surface-border italic">
                      "{review.review}"
                    </p>
                  )}
                </div>
              </div>
            ))
          )}
        </div>
      </motion.div>
    </div>
  )
}

export default DriverRatingsPage
