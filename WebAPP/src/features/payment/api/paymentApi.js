import apiClient from '@/services/apiClient'
import {
  parseApiResponse,
  parseApiArrayResponse,
  PaymentResponseSchema,
  WalletSchema,
  WalletTransactionSchema,
} from '@/schemas/dto'

export const paymentApi = {
  createVNPayUrl: (payload) =>
    apiClient.post('/payments/vnpay/create', payload).then((r) => parseApiResponse(PaymentResponseSchema, r.data)),

  createMomoUrl: (payload) =>
    apiClient.post('/payments/momo/create', payload).then((r) => parseApiResponse(PaymentResponseSchema, r.data)),

  getStatus: (bookingId) =>
    apiClient.get(`/payments/status/${bookingId}`).then((r) => r.data), // status usually doesn't have a specific schema or is just a string/boolean
}

export const walletApi = {
  getMyWallet: () =>
    apiClient.get('/wallets/my-wallet').then((r) => parseApiResponse(WalletSchema, r.data)),

  deposit: (payload) =>
    apiClient.post('/wallets/deposit', payload).then((r) => r.data),

  withdraw: (amount) =>
    apiClient.post(`/wallets/withdraw?amount=${amount}`).then((r) => r.data),

  getTransactionHistory: (walletId) =>
    apiClient.get(`/wallets/history-transactions?walletId=${walletId}`).then((r) => parseApiArrayResponse(WalletTransactionSchema, r.data)),
}
