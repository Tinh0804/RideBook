import { getToken, onMessage } from 'firebase/messaging'
import { getFirebaseMessaging, firebaseConfig, VAPID_KEY } from '@/config/firebase'
import { notificationApi } from '@/features/booking/api/masterDataApi'

const FCM_TOKEN_KEY = 'bookcar_fcm_token'

/**
 * Đăng ký Service Worker và lấy FCM Token cho Web
 */
export const requestFcmToken = async () => {
  try {
    if (typeof window === 'undefined' || !('Notification' in window) || !('serviceWorker' in navigator)) {
      console.log('[FCM] Trình duyệt không hỗ trợ Push Notifications')
      return null
    }

    if (Notification.permission === 'denied') {
      console.log('[FCM] Người dùng đã từ chối quyền thông báo trên trình duyệt')
      return null
    }

    const permission = await Notification.requestPermission()
    if (permission !== 'granted') {
      console.log('[FCM] Quyền thông báo chưa được cấp:', permission)
      return null
    }

    const messaging = await getFirebaseMessaging()
    if (!messaging) {
      console.log('[FCM] Firebase Messaging không khả dụng trên môi trường này')
      return null
    }

    // Đăng ký Service Worker kèm tham số cấu hình
    const swParams = new URLSearchParams({
      apiKey: firebaseConfig.apiKey || '',
      projectId: firebaseConfig.projectId || '',
      messagingSenderId: firebaseConfig.messagingSenderId || '',
      appId: firebaseConfig.appId || '',
    }).toString()

    const swUrl = `/firebase-messaging-sw.js?${swParams}`
    const registration = await navigator.serviceWorker.register(swUrl, { scope: '/' })
    await navigator.serviceWorker.ready

    const tokenOptions = { serviceWorkerRegistration: registration }
    if (VAPID_KEY && VAPID_KEY.trim() !== '') {
      tokenOptions.vapidKey = VAPID_KEY
    }

    const currentToken = await getToken(messaging, tokenOptions)
    if (currentToken) {
      const savedToken = sessionStorage.getItem(FCM_TOKEN_KEY)
      if (savedToken !== currentToken) {
        try {
          await notificationApi.registerDeviceToken(currentToken, 'WEB')
          sessionStorage.setItem(FCM_TOKEN_KEY, currentToken)
          console.log('[FCM] Đã đăng ký FCM Web Token thành công lên máy chủ')
        } catch (apiErr) {
          console.warn('[FCM] Gửi device token lên backend thất bại:', apiErr)
        }
      }
      return currentToken
    } else {
      console.warn('[FCM] Không lấy được FCM Token (chưa có token hoặc quyền bị chặn)')
      return null
    }
  } catch (error) {
    console.warn('[FCM] Lỗi khi lấy FCM Token:', error)
    return null
  }
}

/**
 * Lắng nghe thông báo khi ứng dụng đang mở (Foreground)
 * @param {Function} onMessageReceived Callback khi có tin nhắn tới
 * @returns {Promise<Function>} Hàm unsubscribe
 */
export const listenForegroundMessages = async (onMessageReceived) => {
  try {
    const messaging = await getFirebaseMessaging()
    if (!messaging) return () => {}

    const unsubscribe = onMessage(messaging, (payload) => {
      console.log('[FCM] Nhận foreground message:', payload)
      if (typeof onMessageReceived === 'function') {
        onMessageReceived(payload)
      }
    })

    return unsubscribe
  } catch (error) {
    console.warn('[FCM] Lỗi thiết lập listener foreground:', error)
    return () => {}
  }
}
