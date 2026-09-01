import { useEffect, useRef, useCallback, useMemo } from 'react'
import { useAuthStore } from '@/store/rootStore'
import { websocketService } from '@/services/websocketService'

/**
 * WebSocket hook using Singleton STOMP WebSocketService
 * Giữ kết nối duy nhất, không reconnect khi component re-render,
 * hỗ trợ dynamic subscribe/unsubscribe theo reference counting.
 *
 * @param {string|string[]} topics    - STOMP topics to subscribe to
 * @param {function} onMessage        - Callback for incoming messages (topic, payload)
 */
export const useWebSocket = (topics = [], onMessage) => {
  const { accessToken } = useAuthStore()

  // 1. Giữ callback mới nhất trong ref để không làm trigger re-subscribe khi hàm đổi
  const onMessageRef = useRef(onMessage)
  useEffect(() => {
    onMessageRef.current = onMessage
  }, [onMessage])

  // 2. Chuẩn hóa topics thành mảng các chuỗi hợp lệ
  const normalizedTopics = useMemo(() => {
    const list = Array.isArray(topics) ? topics : [topics]
    return list.filter((t) => typeof t === 'string' && t.trim() !== '')
  }, [JSON.stringify(topics)]) // Dùng JSON.stringify để so sánh giá trị mảng thay vì tham chiếu

  // 3. Đảm bảo kết nối Singleton đã được kích hoạt khi có token
  useEffect(() => {
    if (accessToken) {
      websocketService.connect(accessToken)
    }
  }, [accessToken])

  // 4. Subscribe vào các topics, tự động hủy khi unmount hoặc topics thay đổi
  useEffect(() => {
    if (!accessToken || normalizedTopics.length === 0) return

    const unsubs = normalizedTopics.map((topic) =>
      websocketService.subscribe(topic, (t, payload) => {
        onMessageRef.current?.(t, payload)
      })
    )

    return () => {
      unsubs.forEach((unsub) => {
        try {
          unsub()
        } catch (e) {
          console.warn('[useWebSocket] Lỗi khi hủy đăng ký topic:', e)
        }
      })
    }
  }, [accessToken, normalizedTopics])

  // 5. Hàm gửi tin nhắn qua WebSocket Singleton
  const sendMessage = useCallback((destination, body) => {
    return websocketService.send(destination, body)
  }, [])

  return { sendMessage }
}

export default useWebSocket
