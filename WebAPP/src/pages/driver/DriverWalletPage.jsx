import { useState, useEffect } from 'react'
import { useSearchParams } from 'react-router-dom'
import toast from 'react-hot-toast'
import {
  RiWalletLine, RiArrowUpLine, RiArrowDownLine,
  RiBankLine, RiAddLine, RiLoader4Line,
} from 'react-icons/ri'
import { walletApi } from '@/features/payment/api/paymentApi'
import { formatCurrency } from '@/utils/currency'
import { formatDate } from '@/utils/formatDate'
import Button from '@/components/Elements/Button'
import Input from '@/components/Elements/Input'
import Modal from '@/components/Elements/Modal'
import Spinner from '@/components/Elements/Spinner'
import { cn } from '@/utils/cn'
import { TransactionType } from '@/constants/enums'
import { motion } from 'motion/react'

const QUICK_AMOUNTS = [100_000, 200_000, 500_000, 1_000_000]

const DriverWalletPage = () => {
  const [wallet,       setWallet]       = useState(null)
  const [transactions, setTransactions] = useState([])
  const [loading,      setLoading]      = useState(true)
  const [depositOpen,  setDepositOpen]  = useState(false)
  const [withdrawOpen, setWithdrawOpen] = useState(false)
  const [amount,       setAmount]       = useState('')
  const [paymentProvider, setPaymentProvider] = useState('VNPAY')
  const [actionLoading,setActionLoading]= useState(false)
  const [searchParams, setSearchParams] = useSearchParams()

  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)

  useEffect(() => {
      // Check payment status from query parameters (Return URL)
      const vnpCode = searchParams.get('vnp_ResponseCode')
      const momoCode = searchParams.get('resultCode')

      if (vnpCode) {
        if (vnpCode === '00') {
          const amt = parseInt(searchParams.get('vnp_Amount') || '0') / 100
          toast.success(`Nạp thành công ${formatCurrency(amt)} vào ví`)
        } else {
          toast.error('Giao dịch VNPay thất bại hoặc đã bị huỷ')
        }
        setSearchParams({})
      } else if (momoCode) {
        if (momoCode === '0') {
          const amt = parseInt(searchParams.get('amount') || '0')
          toast.success(`Nạp thành công ${formatCurrency(amt)} vào ví`)
        } else {
          toast.error('Giao dịch MoMo thất bại hoặc đã bị huỷ')
        }
        setSearchParams({})
      }

      walletApi.getMyWallet()
      .then((w) => {
        setWallet(w)
      })
      .catch(() => {})
      .finally(() => setLoading(false))
  }, [])

  useEffect(() => {
    if (wallet?.walletId) {
      walletApi.getTransactionHistory(wallet.walletId, page, 10)
        .then((res) => {
          setTransactions(res?.content || [])
          setTotalPages(res?.page?.totalPages ?? res?.totalPages ?? 0)
        })
        .catch(() => {})
    }
  }, [wallet?.walletId, page])

  const handleDeposit = async () => {
    const num = parseInt(amount.replace(/\D/g, ''))
    if (!num || num < 10000) { toast.error('Nhập số tiền tối thiểu 10,000đ'); return }
    setActionLoading(true)
    try {
      const data = await walletApi.deposit({ 
        amount: num, 
        walletId: wallet?.id,
        method: paymentProvider,
        returnUrl: `${window.location.origin}/driver/wallet`
      })
      const url = data?.result?.paymentUrl || data?.result?.payUrl || data?.paymentUrl || data?.payUrl
      if (url) 
        window.open(url, '_blank')
      else toast.success('Đang xử lý...')
    } catch (err) {
      toast.error(err?.response?.data?.message || 'Nạp tiền thất bại')
      setActionLoading(false)
    }
  }

  const handleWithdraw = async () => {
    const num = parseInt(amount.replace(/\D/g, ''))
    if (!num || num < 50000) { toast.error('Số tiền rút tối thiểu 50,000đ'); return }
    if (num > (wallet?.balance || 0)) { toast.error('Số dư không đủ'); return }
    setActionLoading(true)
    try {
      await walletApi.withdraw(num)
      toast.success('Yêu cầu rút tiền đã được ghi nhận')
      setWithdrawOpen(false)
      setWallet((w) => ({ ...w, balance: (w?.balance || 0) - num }))
      setPage(0) // Reset to page 0 to see the new transaction
    } catch (err) {
      toast.error(err?.response?.data?.message || 'Rút tiền thất bại')
    } finally {
      setActionLoading(false)
    }
  }

  const TX_ICON  = { [TransactionType.DEPOSIT]: RiArrowDownLine, [TransactionType.WITHDRAWAL]: RiArrowUpLine, default: RiWalletLine }
  const TX_COLOR = { [TransactionType.DEPOSIT]: 'text-emerald-500 bg-emerald-50 dark:bg-emerald-500/10', [TransactionType.WITHDRAWAL]: 'text-red-500 bg-red-50 dark:bg-red-500/10', default: 'text-gray-500 bg-gray-100 dark:bg-surface-muted dark:text-content-muted' }

  if (loading) return (
    <div className="flex h-full items-center justify-center bg-[#e8ece3] dark:bg-surface-dark">
      <Spinner size="xl" />
    </div>
  )

  return (
    <div className="h-full overflow-y-auto bg-[#e8ece3] p-5 pb-10 dark:bg-surface-dark lg:p-8 pointer-events-auto">
      <motion.div 
        initial={{ opacity: 0, x: -18 }}
        animate={{ opacity: 1, x: 0 }}
        transition={{ duration: 0.45, ease: [0.16, 1, 0.3, 1] }}
        className="mx-auto max-w-4xl space-y-6"
      >
        <div className="mb-2">
          <h1 className="font-display text-3xl font-bold text-gray-900 dark:text-white tracking-tight">Ví điện tử</h1>
          <p className="text-gray-500 dark:text-gray-400 text-sm mt-1">Quản lý số dư, nạp và rút tiền</p>
        </div>

        {/* Balance card */}
        <div className="relative overflow-hidden rounded-3xl p-8 shadow-xl bg-gradient-to-br from-brand-600 to-brand-800 border border-white/10 text-white">
          <div className="absolute top-0 right-0 w-64 h-64 bg-white/10 rounded-full blur-3xl opacity-50 translate-x-1/4 -translate-y-1/4"></div>
          <div className="absolute bottom-0 left-0 w-64 h-64 bg-black/20 rounded-full blur-3xl opacity-50 -translate-x-1/4 translate-y-1/4"></div>
          
          <div className="relative z-10 flex flex-col md:flex-row md:items-end justify-between gap-6">
            <div className="space-y-6">
              <div className="flex items-center gap-3">
                <div className="w-12 h-12 rounded-2xl bg-white/10 backdrop-blur-md border border-white/10 flex items-center justify-center">
                  <RiWalletLine size={24} className="text-brand-100" />
                </div>
                <div>
                  <p className="text-xs font-bold uppercase tracking-wider text-brand-200">Số dư khả dụng</p>
                  <p className="font-display text-4xl font-bold text-white mt-1 tracking-tight drop-shadow-sm">
                    {formatCurrency(wallet?.balance || 0)}
                  </p>
                </div>
              </div>
            </div>

            <div className="flex flex-col sm:flex-row gap-3 md:min-w-[300px]">
              <button
                onClick={() => { setAmount(''); setDepositOpen(true) }}
                className="flex-1 bg-white text-brand-900 hover:bg-brand-50 transition-colors py-3 px-4 rounded-xl shadow-lg flex items-center justify-center gap-2 font-bold text-sm"
              >
                <RiAddLine size={18} /> Nạp tiền
              </button>
              <button
                onClick={() => { setAmount(''); setWithdrawOpen(true) }}
                className="flex-1 bg-black/20 backdrop-blur-sm border border-white/20 text-white hover:bg-white/10 transition-colors py-3 px-4 rounded-xl flex items-center justify-center gap-2 font-bold text-sm"
              >
                <RiBankLine size={18} /> Rút tiền
              </button>
            </div>
          </div>
        </div>

        {/* Transaction history */}
        <div className="bg-white dark:bg-surface-card rounded-2xl p-6 border border-gray-100 dark:border-surface-border shadow-sm space-y-5">
          <h2 className="font-display text-xl font-bold text-gray-900 dark:text-white">Lịch sử giao dịch</h2>
          
          {transactions.length === 0 ? (
            <div className="py-12 text-center border border-dashed border-gray-200 dark:border-surface-border rounded-2xl">
              <div className="w-16 h-16 rounded-full bg-gray-50 dark:bg-surface-dark flex items-center justify-center mx-auto mb-3">
                <RiWalletLine size={24} className="text-gray-400" />
              </div>
              <p className="text-gray-500 font-medium">Chưa có giao dịch nào</p>
            </div>
          ) : (
            <div className="space-y-3">
              {transactions.map((tx) => {
                const Icon  = TX_ICON[tx.type]  || TX_ICON.default
                const color = TX_COLOR[tx.type] || TX_COLOR.default
                const isIn  = tx.type === TransactionType.DEPOSIT
                return (
                  <div key={tx.transactionId} className="flex items-center gap-4 p-4 rounded-2xl hover:bg-gray-50 dark:hover:bg-surface-border/50 border border-gray-100 dark:border-surface-border transition-colors group">
                    <div className={cn('w-12 h-12 rounded-2xl flex items-center justify-center shrink-0 shadow-sm border border-black/5 dark:border-white/5 transition-transform group-hover:scale-110', color)}>
                      <Icon size={22} />
                    </div>
                    <div className="flex-1 min-w-0">
                      <p className="text-[15px] font-bold text-gray-900 dark:text-white truncate mb-0.5">{tx.description || (isIn ? 'Nạp tiền' : 'Rút tiền')}</p>
                      <p className="text-xs font-medium text-gray-500">{formatDate(tx.createdAt)}</p>
                    </div>
                    <span className={cn('font-display text-lg font-bold shrink-0', isIn ? 'text-emerald-600 dark:text-emerald-400' : 'text-red-600 dark:text-red-400')}>
                      {isIn ? '+' : '-'}{formatCurrency(Math.abs(tx.amount))}
                    </span>
                  </div>
                )
              })}
            </div>
          )}
          
          {/* Pagination */}
          {totalPages > 1 && (
            <div className="flex flex-wrap items-center justify-center gap-2 mt-8 pt-4">
              <button
                onClick={() => setPage(p => Math.max(0, p - 1))}
                disabled={page === 0}
                className="px-4 py-2 rounded-xl border border-gray-200 dark:border-surface-border bg-white dark:bg-surface-card disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50 dark:hover:bg-surface-muted transition-colors text-sm font-semibold text-gray-700 dark:text-white"
              >
                Trước
              </button>
              <div className="flex flex-wrap items-center gap-1 mx-2">
                {Array.from({ length: totalPages }).map((_, i) => (
                  <button
                    key={i}
                    onClick={() => setPage(i)}
                    className={cn(
                      "w-9 h-9 rounded-xl flex items-center justify-center text-sm font-bold transition-colors",
                      page === i 
                        ? "bg-brand-500 text-white shadow-md shadow-brand-500/20" 
                        : "bg-white dark:bg-surface-card hover:bg-gray-50 dark:hover:bg-surface-muted text-gray-700 dark:text-white border border-transparent hover:border-gray-200 dark:hover:border-surface-border"
                    )}
                  >
                    {i + 1}
                  </button>
                ))}
              </div>
              <button
                onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
                disabled={page === totalPages - 1}
                className="px-4 py-2 rounded-xl border border-gray-200 dark:border-surface-border bg-white dark:bg-surface-card disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50 dark:hover:bg-surface-muted transition-colors text-sm font-semibold text-gray-700 dark:text-white"
              >
                Sau
              </button>
            </div>
          )}
        </div>

        {/* Deposit modal */}
        <Modal isOpen={depositOpen} onClose={() => setDepositOpen(false)} title="Nạp tiền vào ví" size="sm">
          <div className="space-y-6 pt-2">
            <div className="space-y-3">
              <p className="text-sm font-bold text-gray-700 dark:text-gray-300">Chọn mệnh giá</p>
              <div className="grid grid-cols-2 gap-3">
                {QUICK_AMOUNTS.map((a) => (
                  <button key={a} onClick={() => setAmount(a.toString())}
                    className={cn('py-3 px-4 rounded-xl text-sm font-bold border-2 transition-all', amount === a.toString()
                      ? 'bg-brand-50 dark:bg-brand-500/10 border-brand-500 text-brand-600 dark:text-brand-400 shadow-sm'
                      : 'bg-white dark:bg-surface-card border-gray-100 dark:border-surface-border text-gray-600 dark:text-gray-300 hover:border-brand-300 dark:hover:border-brand-500/50'
                    )}>
                    {formatCurrency(a, true)}
                  </button>
                ))}
              </div>
            </div>
            
            <div className="space-y-3">
              <p className="text-sm font-bold text-gray-700 dark:text-gray-300">Hoặc nhập số tiền khác</p>
              <Input placeholder="Nhập số tiền..." value={amount} onChange={(e) => setAmount(e.target.value)} className="font-bold text-lg" />
            </div>

            <div className="space-y-3">
              <p className="text-sm font-bold text-gray-700 dark:text-gray-300">Phương thức thanh toán</p>
              <div className="grid grid-cols-2 gap-3">
                {['VNPAY', 'MOMO'].map((provider) => (
                  <button
                    key={provider}
                    onClick={() => setPaymentProvider(provider)}
                    className={cn(
                      'p-3.5 rounded-xl border-2 flex items-center justify-center gap-2 transition-all duration-200',
                      paymentProvider === provider
                        ? 'border-brand-500 bg-brand-500/10 text-brand-600 dark:text-brand-400 font-bold shadow-sm'
                        : 'border-gray-100 dark:border-surface-border bg-gray-50 dark:bg-surface-dark text-gray-500 hover:border-brand-300'
                    )}
                  >
                    {provider}
                  </button>
                ))}
              </div>
            </div>

            <Button fullWidth onClick={handleDeposit} loading={actionLoading} className="py-4 text-base font-bold shadow-lg shadow-brand-500/20">
              Xác nhận nạp {amount ? formatCurrency(parseInt(amount.replace(/\D/g, '')) || 0) : ''}
            </Button>
          </div>
        </Modal>

        {/* Withdraw modal */}
        <Modal isOpen={withdrawOpen} onClose={() => setWithdrawOpen(false)} title="Rút tiền" size="sm">
          <div className="space-y-6 pt-2">
            <div className="bg-gray-50 dark:bg-surface-dark p-4 rounded-xl border border-gray-100 dark:border-surface-border">
              <p className="text-xs font-bold text-gray-500 dark:text-gray-400 uppercase tracking-wider mb-1">Số dư hiện tại</p>
              <p className="font-display text-2xl font-bold text-brand-600 dark:text-brand-400">
                {formatCurrency(wallet?.balance || 0)}
              </p>
            </div>
            
            <div className="space-y-3">
              <p className="text-sm font-bold text-gray-700 dark:text-gray-300">Nhập số tiền cần rút</p>
              <Input
                placeholder="0 đ"
                value={amount}
                onChange={(e) => setAmount(e.target.value)}
                className="font-bold text-lg"
              />
              <p className="text-xs font-medium text-gray-500 flex items-center gap-1.5">
                <RiBankLine /> Tiền sẽ được chuyển vào tài khoản ngân hàng đã liên kết
              </p>
            </div>
            
            <Button fullWidth onClick={handleWithdraw} loading={actionLoading} className="py-4 text-base font-bold shadow-lg shadow-brand-500/20">
              Yêu cầu rút tiền
            </Button>
          </div>
        </Modal>
      </motion.div>
    </div>
  )
}

export default DriverWalletPage
