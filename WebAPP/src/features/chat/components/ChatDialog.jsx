import { useState, useEffect, useRef, useCallback } from 'react'
import { RiCloseLine, RiSendPlane2Line } from 'react-icons/ri'
import { chatApi } from '@/features/chat/api/chatApi'
import { useAuthStore, useChatStore } from '@/store/rootStore'
import { useWebSocket } from '@/hooks/useWebSocket'
import { formatTime } from '@/utils/formatDate'
import { cn } from '@/utils/cn'

const ChatDialog = ({ bookingId, driverName, customerName, onClose }) => {
  const { user }                            = useAuthStore()
  const { messages, addMessage, setMessages } = useChatStore()
  const [input,   setInput]   = useState('')
  const [sending, setSending] = useState(false)
  const bottomRef             = useRef()

  const chatMessages = messages[bookingId] || []

  // Load existing messages
  useEffect(() => {
    chatApi.getMessages(bookingId)
      .then((d) => setMessages(bookingId, d?.result || d || []))
      .catch(() => {})
  }, [bookingId, setMessages])

  // Scroll to bottom on new messages
  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [chatMessages.length])

  // Real-time messages via WebSocket
  const onWsMessage = useCallback((_, payload) => {
    if (payload?.bookingId === bookingId && payload?.senderId !== user?.id) {
      addMessage(bookingId, payload)
    }
  }, [bookingId, user?.id, addMessage])
  useWebSocket([`/topic/chat/${bookingId}`], onWsMessage)

  const handleSend = async () => {
    const text = input.trim()
    if (!text || sending) return
    setSending(true)

    const tempMsg = {
      id:         Date.now(),
      bookingId,
      senderId:   user?.id,
      senderRole: user?.role,
      message:    text,
      createdAt:  new Date().toISOString(),
      _temp:      true,
    }
    addMessage(bookingId, tempMsg)
    setInput('')

    try {
      await chatApi.sendMessage({
        bookingId,
        senderId:   user?.id,
        message:    text,
        senderRole: user?.role,
      })
    } catch {
      // Optimistic message stays in UI anyway
    } finally {
      setSending(false)
    }
  }

  const otherName = driverName || customerName || 'Người dùng'

  return (
    <div className="fixed bottom-6 right-6 w-80 z-50 card shadow-2xl flex flex-col animate-slide-up"
      style={{ height: 420 }}
    >
      {/* Header */}
      <div className="flex items-center justify-between px-4 py-3 border-b border-surface-border shrink-0">
        <div>
          <p className="font-semibold text-content-main text-sm">{otherName}</p>
          <p className="text-[10px] text-brand-400">● Đang hoạt động</p>
        </div>
        <button
          onClick={onClose}
          className="p-1 text-content-muted hover:text-content-main hover:bg-surface-border rounded-lg transition-colors"
        >
          <RiCloseLine size={18} />
        </button>
      </div>

      {/* Messages */}
      <div className="flex-1 overflow-y-auto p-3 space-y-2">
        {chatMessages.length === 0 && (
          <p className="text-center text-xs text-gray-600 py-4">Bắt đầu cuộc trò chuyện</p>
        )}
        {chatMessages.map((msg) => {
          const isMine = msg.senderId === user?.id
          return (
            <div
              key={msg.id}
              className={cn('flex', isMine ? 'justify-end' : 'justify-start')}
            >
              <div className={cn(
                'max-w-[75%] px-3 py-2 rounded-2xl text-sm',
                isMine
                  ? 'bg-brand-500 text-content-main rounded-br-sm'
                  : 'bg-surface-border text-gray-200 rounded-bl-sm',
              )}>
                <p>{msg.message}</p>
                <p className={cn('text-[10px] mt-0.5', isMine ? 'text-brand-200/70 text-right' : 'text-content-muted')}>
                  {formatTime(msg.createdAt)}
                </p>
              </div>
            </div>
          )
        })}
        <div ref={bottomRef} />
      </div>

      {/* Input */}
      <div className="flex items-center gap-2 p-3 border-t border-surface-border shrink-0">
        <input
          className="flex-1 bg-surface-dark border border-surface-border rounded-xl px-3 py-2 text-sm text-gray-100 placeholder-gray-600 focus:outline-none focus:border-brand-500 transition-colors"
          placeholder="Nhắn tin..."
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && !e.shiftKey && handleSend()}
        />
        <button
          onClick={handleSend}
          disabled={!input.trim() || sending}
          className="w-9 h-9 rounded-xl bg-brand-500 hover:bg-brand-400 disabled:opacity-40 flex items-center justify-center text-content-main transition-all active:scale-95 shrink-0"
        >
          <RiSendPlane2Line size={16} />
        </button>
      </div>
    </div>
  )
}

export default ChatDialog
