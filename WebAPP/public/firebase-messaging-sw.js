/* eslint-disable no-undef */
// Scripts for firebase and firebase messaging
importScripts('https://www.gstatic.com/firebasejs/10.13.0/firebase-app-compat.js')
importScripts('https://www.gstatic.com/firebasejs/10.13.0/firebase-messaging-compat.js')

// Extract config from URL search params if provided, or default
const urlParams = new URL(location).searchParams
const apiKey = urlParams.get('apiKey')
const projectId = urlParams.get('projectId')
const messagingSenderId = urlParams.get('messagingSenderId')
const appId = urlParams.get('appId')

if (apiKey && projectId) {
  firebase.initializeApp({
    apiKey: apiKey,
    projectId: projectId,
    messagingSenderId: messagingSenderId,
    appId: appId,
  })

  const messaging = firebase.messaging()

  messaging.onBackgroundMessage((payload) => {
    console.log('[firebase-messaging-sw.js] Nhận background message:', payload)
    
    const notificationTitle = payload.notification?.title || payload.data?.title || 'Thông báo từ RideBook'
    const notificationOptions = {
      body: payload.notification?.body || payload.data?.message || '',
      icon: '/logo.png',
      badge: '/logo.png',
      data: payload.data || {},
      vibrate: [200, 100, 200],
      tag: payload.data?.bookingId || 'ridebook-notification',
      renotify: true,
    }

    self.registration.showNotification(notificationTitle, notificationOptions)
  })
}

// Xử lý khi người dùng click vào thông báo của hệ điều hành
self.addEventListener('notificationclick', (event) => {
  event.notification.close()

  const data = event.notification.data || {}
  const targetUrl = data.url || '/'

  event.waitUntil(
    clients.matchAll({ type: 'window', includeUncontrolled: true }).then((windowClients) => {
      // Focus vào tab đang mở nếu có
      for (const client of windowClients) {
        if (client.url.includes(self.location.origin) && 'focus' in client) {
          return client.focus()
        }
      }
      // Nếu chưa có tab nào mở thì mở tab mới
      if (clients.openWindow) {
        return clients.openWindow(targetUrl)
      }
    })
  )
})
