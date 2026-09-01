import { useEffect, useRef } from 'react'
import { useAuthStore, useUIStore } from '@/store/rootStore'
import { requestFcmToken, listenForegroundMessages } from '@/services/fcmService'
import { toast } from 'react-hot-toast'

/**
 * Hook quản lý đăng ký và nhận thông báo FCM
 * @param {Function} onNewNotification Callback tùy chọn khi có thông báo mới
 */
export const useFCM = (onNewNotification) => {
  const { user, isAuth } = useAuthStore()
  const { setNotifCount } = useUIStore()
  const soundRef = useRef(null)

  // Khởi tạo audio sound
  useEffect(() => {
    soundRef.current = new Audio('/sounds/notification.mp3')
    soundRef.current.preload = 'auto'
    return () => {
      soundRef.current?.pause()
    }
  }, [])

  useEffect(() => {
    if (!isAuth || !user) return

    let unsubscribe = null

    const initFCM = async () => {
      // 1. Chỉ xin quyền khi Notification permission là default hoặc đã granted
      if (typeof window !== 'undefined' && 'Notification' in window) {
        if (Notification.permission !== 'denied') {
          // Lấy token và đăng ký với backend
          await requestFcmToken()
        }
      }

      // 2. Thiết lập lắng nghe tin nhắn khi tab đang mở (Foreground)
      unsubscribe = await listenForegroundMessages((payload) => {
        const title = payload.notification?.title || payload.data?.title || 'Thông báo từ RideBook'
        const message = payload.notification?.body || payload.data?.message || ''

        // Tăng đếm badge
        setNotifCount((prev) => (typeof prev === 'number' ? prev + 1 : 1))

        // Phát âm thanh
        if (soundRef.current) {
          soundRef.current.currentTime = 0
          soundRef.current.play().catch(() => {})
        }

        // Hiện Toast
        toast.success(`${title}\n${message}`, {
          duration: 5000,
          icon: '🔔',
        })

        if (typeof onNewNotification === 'function') {
          onNewNotification(payload)
        }
      })
    }

    initFCM()

    return () => {
      if (typeof unsubscribe === 'function') {
        unsubscribe()
      }
    }
  }, [isAuth, user, setNotifCount, onNewNotification])
}

export default useFCM
