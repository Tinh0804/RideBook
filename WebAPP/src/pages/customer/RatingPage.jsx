import { useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import toast from 'react-hot-toast'
import { RiStarFill, RiStarLine } from 'react-icons/ri'
import { ratingApi } from '@/features/booking/api/masterDataApi'
import { useAuthStore } from '@/store/rootStore'
import Button from '@/components/Elements/Button'
import { cn } from '@/utils/cn'

const QUICK_REVIEWS = [
  'Tài xế thân thiện', 'Lái xe an toàn', 'Đúng giờ', 'Xe sạch sẽ', 'Tuyến đường tốt',
]

const RatingPage = () => {
  const navigate  = useNavigate()
  const location  = useLocation()
  const { user }  = useAuthStore()
  const booking   = location.state?.booking

  const [stars,    setStars]    = useState(5)
  const [hovered,  setHovered]  = useState(0)
  const [review,   setReview]   = useState('')
  const [tags,     setTags]     = useState([])
  const [loading,  setLoading]  = useState(false)

  if (!booking) {
    navigate('/customer/history')
    return null
  }

  const toggleTag = (tag) =>
    setTags((prev) =>
      prev.includes(tag) ? prev.filter((t) => t !== tag) : [...prev, tag]
    )

  const handleSubmit = async () => {
    setLoading(true)
    try {
      await ratingApi.create({
        bookingId:   booking.bookingId || booking.id,
        rating:      stars,
        feedback:    [review, ...tags].filter(Boolean).join('. '),
      })
      toast.success('Cảm ơn bạn đã đánh giá!')
      navigate('/customer/history')
    } catch (err) {
      toast.error(err?.response?.data?.message || 'Gửi đánh giá thất bại')
    } finally {
      setLoading(false)
    }
  }

  const displayStars = hovered || stars
  const starLabel = ['', 'Tệ', 'Không tốt', 'Bình thường', 'Tốt', 'Xuất sắc'][displayStars]

  return (
    <div className="max-w-md mx-auto space-y-8">
      {/* Driver info */}
      <div className="text-center space-y-4 pt-4">
        <div className="w-20 h-20 rounded-full bg-brand-500/20 border-2 border-brand-500/30 flex items-center justify-center text-3xl font-bold text-brand-400 mx-auto overflow-hidden">
          {booking.driver?.avatar
            ? <img src={booking.driver.avatar} alt={booking.driver.name} className="w-full h-full object-cover" />
            : booking.driver?.name?.[0] || '?'
          }
        </div>
        <div>
          <h2 className="font-display text-xl font-bold text-content-main">{booking.driver?.name}</h2>
          <p className="text-content-muted text-sm mt-1">Đánh giá chuyến đi vừa rồi</p>
        </div>
      </div>

      {/* Star rating */}
      <div className="text-center space-y-3">
        <div className="flex items-center justify-center gap-2">
          {[1, 2, 3, 4, 5].map((s) => (
            <button
              key={s}
              onClick={() => setStars(s)}
              onMouseEnter={() => setHovered(s)}
              onMouseLeave={() => setHovered(0)}
              className="text-4xl transition-transform hover:scale-110 active:scale-95"
            >
              {s <= displayStars
                ? <RiStarFill className="text-yellow-400" />
                : <RiStarLine className="text-surface-muted" />
              }
            </button>
          ))}
        </div>
        <p className="font-display text-lg font-semibold text-content-main transition-all">{starLabel}</p>
      </div>

      {/* Quick tags */}
      <div className="space-y-3">
        <p className="text-sm font-medium text-content-muted text-center">Chọn những điểm nổi bật</p>
        <div className="flex flex-wrap justify-center gap-2">
          {QUICK_REVIEWS.map((tag) => (
            <button
              key={tag}
              onClick={() => toggleTag(tag)}
              className={cn(
                'px-3 py-1.5 rounded-full text-sm border transition-all duration-200',
                tags.includes(tag)
                  ? 'bg-brand-500/20 border-brand-500 text-brand-400'
                  : 'border-surface-border text-content-muted hover:border-brand-500/40 hover:text-content-muted',
              )}
            >
              {tag}
            </button>
          ))}
        </div>
      </div>

      {/* Text review */}
      <div className="space-y-2">
        <label className="text-sm font-medium text-content-muted">Nhận xét thêm (tuỳ chọn)</label>
        <textarea
          className="input-field resize-none h-24"
          placeholder="Chia sẻ trải nghiệm của bạn..."
          value={review}
          onChange={(e) => setReview(e.target.value)}
        />
      </div>

      {/* Submit */}
      <div className="space-y-3 pb-4">
        <Button fullWidth size="lg" onClick={handleSubmit} loading={loading}>
          Gửi đánh giá
        </Button>
        <Button variant="ghost" fullWidth onClick={() => navigate('/customer/history')}>
          Bỏ qua
        </Button>
      </div>
    </div>
  )
}

export default RatingPage
