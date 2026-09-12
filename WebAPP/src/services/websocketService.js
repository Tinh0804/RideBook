import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { WS_URL } from '@/config/index'

class WebSocketService {
  constructor() {
    this.client = null
    this.token = null
    this.isConnected = false
    this.isConnecting = false
    // Map of topic -> Set of callback functions
    this.topicListeners = new Map()
    // Map of topic -> STOMP subscription object
    this.stompSubscriptions = new Map()
  }

  /**
   * Khởi tạo kết nối STOMP duy nhất
   * @param {string} accessToken
   */
  connect(accessToken) {
    if (!accessToken) {
      this.disconnect()
      return
    }

    // Nếu đã kết nối với cùng token, không tạo kết nối mới
    if (this.token === accessToken && (this.isConnected || this.isConnecting)) {
      return
    }

    // Nếu đổi token hoặc đang có kết nối cũ, ngắt kết nối cũ trước
    if (this.client) {
      try {
        this.client.deactivate()
      } catch (err) {
        console.warn('[WebSocketService] Lỗi khi dọn dẹp client cũ:', err)
      }
      this.client = null
      this.isConnected = false
      this.stompSubscriptions.clear()
    }

    this.token = accessToken
    this.isConnecting = true

    const client = new Client({
      webSocketFactory: () => new SockJS(WS_URL),
      connectHeaders: {
        Authorization: `Bearer ${accessToken}`,
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,

      onConnect: () => {
        this.isConnected = true
        this.isConnecting = false
        console.log('[WebSocketService] Đã kết nối STOMP thành công')

        // Re-subscribe toàn bộ các topic đang có listener
        this.resubscribeAll()
      },

      onDisconnect: () => {
        this.isConnected = false
        this.isConnecting = false
        this.stompSubscriptions.clear()
        console.log('[WebSocketService] STOMP ngắt kết nối')
      },

      onStompError: (frame) => {
        console.error('[WebSocketService] STOMP error frame:', frame)
      },

      onWebSocketClose: () => {
        this.isConnected = false
        this.isConnecting = false
        this.stompSubscriptions.clear()
      },
    })

    client.activate()
    this.client = client
  }

  /**
   * Đăng ký lắng nghe một topic STOMP
   * @param {string} topic Tên topic (ví dụ: /topic/booking/123)
   * @param {function} callback Hàm callback (topic, payload)
   * @returns {function} Hàm hủy đăng ký
   */
  subscribe(topic, callback) {
    if (!topic || typeof callback !== 'function') return () => {}

    if (!this.topicListeners.has(topic)) {
      this.topicListeners.set(topic, new Set())
    }
    const listeners = this.topicListeners.get(topic)
    listeners.add(callback)

    // Nếu đã kết nối STOMP và chưa subscribe topic này trên broker
    if (this.isConnected && !this.stompSubscriptions.has(topic)) {
      this.subscribeStompTopic(topic)
    }

    // Trả về hàm hủy đăng ký
    return () => {
      this.unsubscribe(topic, callback)
    }
  }

  /**
   * Hủy đăng ký một callback khỏi topic
   */
  unsubscribe(topic, callback) {
    if (!this.topicListeners.has(topic)) return

    const listeners = this.topicListeners.get(topic)
    listeners.delete(callback)

    // Nếu không còn listener nào nghe topic này nữa, hủy STOMP subscription
    if (listeners.size === 0) {
      this.topicListeners.delete(topic)
      const sub = this.stompSubscriptions.get(topic)
      if (sub) {
        try {
          sub.unsubscribe()
        } catch (e) {
          console.warn('[WebSocketService] Lỗi khi hủy STOMP subscription:', e)
        }
        this.stompSubscriptions.delete(topic)
      }
    }
  }

  /**
   * Đăng ký STOMP broker cho 1 topic cụ thể
   */
  subscribeStompTopic(topic) {
    if (!this.client || !this.isConnected) return

    try {
      const sub = this.client.subscribe(topic, (frame) => {
        let payload
        try {
          payload = JSON.parse(frame.body)
        } catch {
          payload = frame.body
        }

        const listeners = this.topicListeners.get(topic)
        if (listeners) {
          listeners.forEach((cb) => {
            try {
              cb(topic, payload)
            } catch (cbErr) {
              console.error(`[WebSocketService] Lỗi xử lý callback cho topic ${topic}:`, cbErr)
            }
          })
        }
      })

      this.stompSubscriptions.set(topic, sub)
    } catch (err) {
      console.error(`[WebSocketService] Lỗi subscribe topic ${topic}:`, err)
    }
  }

  /**
   * Re-subscribe lại tất cả các topics khi kết nối lại
   */
  resubscribeAll() {
    this.stompSubscriptions.clear()
    for (const topic of this.topicListeners.keys()) {
      this.subscribeStompTopic(topic)
    }
  }

  /**
   * Gửi tin nhắn qua STOMP
   */
  send(destination, body) {
    if (!this.client || !this.isConnected) {
      console.warn('[WebSocketService] Không thể gửi tin: WebSocket chưa kết nối')
      return false
    }

    try {
      this.client.publish({
        destination,
        body: typeof body === 'string' ? body : JSON.stringify(body),
      })
      return true
    } catch (err) {
      console.error('[WebSocketService] Lỗi khi publish message:', err)
      return false
    }
  }

  /**
   * Ngắt kết nối và dọn dẹp
   */
  disconnect() {
    this.token = null
    this.isConnected = false
    this.isConnecting = false

    if (this.client) {
      try {
        this.client.deactivate()
      } catch (err) {
        console.warn('[WebSocketService] Lỗi khi deactivate client:', err)
      }
      this.client = null
    }

    this.stompSubscriptions.clear()
    this.topicListeners.clear()
  }
}

export const websocketService = new WebSocketService()
export default websocketService
