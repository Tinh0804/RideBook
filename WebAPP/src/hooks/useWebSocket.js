import { useEffect, useRef, useCallback } from 'react'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { WS_URL } from '@/config'
import { useAuthStore } from '@/store/rootStore'

/**
 * WebSocket hook using STOMP over SockJS
 * @param {string[]} topics        - STOMP topics to subscribe to
 * @param {function} onMessage     - Callback for incoming messages
 */
export const useWebSocket = (topics = [], onMessage) => {
  const clientRef   = useRef(null)
  const { accessToken } = useAuthStore()

  const connect = useCallback(() => {
    if (!accessToken || topics.length === 0) return

    const client = new Client({
      webSocketFactory: () => new SockJS(WS_URL),
      connectHeaders: { Authorization: `Bearer ${accessToken}` },
      reconnectDelay: 5000,

      onConnect: () => {
        topics.forEach((topic) => {
          client.subscribe(topic, (frame) => {
            try {
              const payload = JSON.parse(frame.body)
              onMessage?.(topic, payload)
            } catch {
              onMessage?.(topic, frame.body)
            }
          })
        })
      },

      onStompError: (frame) => {
        console.error('STOMP error:', frame)
      },
    })

    client.activate()
    clientRef.current = client
  }, [accessToken, topics, onMessage])

  useEffect(() => {
    connect()
    return () => {
      clientRef.current?.deactivate()
    }
  }, [connect])

  const sendMessage = useCallback((destination, body) => {
    if (clientRef.current?.connected) {
      clientRef.current.publish({
        destination,
        body: JSON.stringify(body),
      })
    }
  }, [])

  return { sendMessage }
}
