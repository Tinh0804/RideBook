import apiClient from '@/services/apiClient'
import { parseApiResponse, parseApiArrayResponse, ChatMessageSchema } from '@/schemas/dto'

export const chatApi = {
  getMessages: (bookingId) =>
    apiClient.get(`/chats/${bookingId}`).then((r) => parseApiArrayResponse(ChatMessageSchema, r.data)),

  sendMessage: (payload) =>
    apiClient.post('/chats/send', payload).then((r) => parseApiResponse(ChatMessageSchema, r.data)),
}
