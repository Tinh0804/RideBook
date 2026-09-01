import { initializeApp, getApps, getApp } from 'firebase/app'
import { getAuth } from 'firebase/auth'
import { getMessaging, isSupported } from 'firebase/messaging'

export const firebaseConfig = {
  apiKey: import.meta.env.VITE_FIREBASE_API_KEY,
  authDomain: import.meta.env.VITE_FIREBASE_AUTH_DOMAIN,
  projectId: import.meta.env.VITE_FIREBASE_PROJECT_ID,
  storageBucket: import.meta.env.VITE_FIREBASE_STORAGE_BUCKET,
  messagingSenderId: import.meta.env.VITE_FIREBASE_MESSAGING_SENDER_ID,
  appId: import.meta.env.VITE_FIREBASE_APP_ID,
}

export const VAPID_KEY = import.meta.env.VITE_FIREBASE_VAPID_KEY || ''

// Initialize Firebase App singleton
const app = getApps().length > 0 ? getApp() : initializeApp(firebaseConfig)

export const auth = getAuth(app)
auth.useDeviceLanguage()

let messagingInstance = null
let messagingSupportedPromise = null

/**
 * Lấy instance Firebase Messaging nếu môi trường hỗ trợ Service Worker và Push API
 */
export const getFirebaseMessaging = async () => {
  if (messagingInstance) return messagingInstance

  if (!messagingSupportedPromise) {
    messagingSupportedPromise = isSupported()
  }

  const supported = await messagingSupportedPromise
  if (supported && typeof window !== 'undefined') {
    try {
      messagingInstance = getMessaging(app)
      return messagingInstance
    } catch (err) {
      console.warn('[FCM] Không thể khởi tạo Firebase Messaging:', err)
      return null
    }
  }
  return null
}

export default app
