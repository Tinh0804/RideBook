import { useState, useEffect } from 'react'
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

const QUICK_AMOUNTS = [100_000, 200_000, 500_000, 1_000_000]

const DriverWalletPage = () => {
  const [wallet,       setWallet]       = useState(null)
  const [transactions, setTransactions] = useState([])
  const [loading,      setLoading]      = useState(true)
  const [depositOpen,  setDepositOpen]  = useState(false)
  const [withdrawOpen, setWithdrawOpen] = useState(false)
  const [amount,       setAmount]       = useState('')
  const [actionLoading,setActionLoading]= useState(false)

  useEffect(() => {
      walletApi.getMyWallet()
      .then((w) => {
        setWallet(w)
        if (w?.walletId) {
          walletApi.getTransactionHistory(w.walletId)
            .then((h) => setTransactions(h))
            .catch(() => {})
        }
      })
      .catch(() => {})
      .finally(() => setLoading(false))
  }, [])

  const handleDeposit = async () => {
    const num = parseInt(amount.replace(/\D/g, ''))
    if (!num || num < 10000) { toast.error('Nhập số tiền tối thiểu 10,000đ'); return }
    setActionLoading(true)
    try {
      const data = await walletApi.deposit({ 
        amount: num, walletId: wallet?.id 
      })
      const url = data?.paymentUrl
      if (url) window.open(url, '_blank')
      toast.success('Đang chuyển đến cổng thanh toán...')
      setDepositOpen(false)
    } catch (err) {
      toast.error(err?.response?.data?.message || 'Nạp tiền thất bại')
    } finally {
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
    } catch (err) {
      toast.error(err?.response?.data?.message || 'Rút tiền thất bại')
    } finally {
      setActionLoading(false)
    }
  }

  const TX_ICON  = { DEPOSIT: RiArrowDownLine, WITHDRAW: RiArrowUpLine, default: RiWalletLine }
  const TX_COLOR = { DEPOSIT: 'text-brand-400 bg-brand-400/10', WITHDRAW: 'text-red-400 bg-red-400/10', default: 'text-content-muted bg-gray-400/10' }

  if (loading) return <div className="flex justify-center py-16"><Spinner size="xl" /></div>

  return (
    <div className="space-y-6">
      <div>
        <h1 className="section-title">Ví tiền</h1>
        <p className="text-content-muted text-sm mt-1">Quản lý số dư và giao dịch</p>
      </div>

      {/* Balance card */}
      <div className="relative overflow-hidden card p-8 bg-gradient-to-br from-brand-900/60 to-surface-card border-brand-500/20">
        <div className="absolute top-0 right-0 w-48 h-48 rounded-full bg-brand-500/8 blur-3xl -translate-y-1/2 translate-x-1/2" />
        <div className="relative z-10 space-y-6">
          <div className="flex items-center gap-3">
            <div className="w-11 h-11 rounded-xl bg-brand-500/20 border border-brand-500/30 flex items-center justify-center">
              <RiWalletLine size={22} className="text-brand-400" />
            </div>
            <div>
              <p className="text-xs text-content-muted uppercase tracking-wider">Số dư khả dụng</p>
              <p className="font-display text-4xl font-bold text-content-main mt-0.5">
                {formatCurrency(wallet?.balance || 0)}
              </p>
            </div>
          </div>

          <div className="flex gap-3">
            <Button
              size="sm" variant="outline"
              onClick={() => { setAmount(''); setDepositOpen(true) }}
              className="flex-1"
            >
              <RiAddLine size={14} /> Nạp tiền
            </Button>
            <Button
              size="sm" variant="ghost"
              onClick={() => { setAmount(''); setWithdrawOpen(true) }}
              className="flex-1"
            >
              <RiBankLine size={14} /> Rút tiền
            </Button>
          </div>
        </div>
      </div>

      {/* Transaction history */}
      <div className="space-y-3">
        <h2 className="font-display text-lg font-bold text-content-main">Lịch sử giao dịch</h2>
        {transactions.length === 0 ? (
          <div className="card p-8 text-center text-content-muted">Chưa có giao dịch nào</div>
        ) : (
          <div className="space-y-2">
            {transactions.map((tx) => {
              const Icon  = TX_ICON[tx.type]  || TX_ICON.default
              const color = TX_COLOR[tx.type] || TX_COLOR.default
              const isIn  = tx.type === 'DEPOSIT'
              return (
                <div key={tx.id} className="card p-4 flex items-center gap-4">
                  <div className={cn('w-10 h-10 rounded-xl flex items-center justify-center shrink-0', color)}>
                    <Icon size={18} />
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium text-content-main">{tx.description || (isIn ? 'Nạp tiền' : 'Rút tiền')}</p>
                    <p className="text-xs text-content-muted">{formatDate(tx.createdAt)}</p>
                  </div>
                  <span className={cn('font-display font-bold shrink-0', isIn ? 'text-brand-400' : 'text-red-400')}>
                    {isIn ? '+' : '-'}{formatCurrency(Math.abs(tx.amount))}
                  </span>
                </div>
              )
            })}
          </div>
        )}
      </div>

      {/* Deposit modal */}
      <Modal isOpen={depositOpen} onClose={() => setDepositOpen(false)} title="Nạp tiền vào ví" size="sm">
        <div className="space-y-4">
          <p className="text-sm text-content-muted">Chọn số tiền nạp hoặc nhập số tiền khác</p>
          <div className="grid grid-cols-2 gap-2">
            {QUICK_AMOUNTS.map((a) => (
              <button key={a} onClick={() => setAmount(a.toString())}
                className={cn('py-2.5 px-3 rounded-xl text-sm font-semibold border transition-all', amount === a.toString()
                  ? 'bg-brand-500/15 border-brand-500 text-brand-400'
                  : 'border-surface-border text-content-muted hover:border-brand-500/40'
                )}>
                {formatCurrency(a, true)}
              </button>
            ))}
          </div>
          <Input placeholder="Hoặc nhập số tiền..." value={amount} onChange={(e) => setAmount(e.target.value)} />
          <Button fullWidth onClick={handleDeposit} loading={actionLoading}>Nạp tiền qua VNPay</Button>
        </div>
      </Modal>

      {/* Withdraw modal */}
      <Modal isOpen={withdrawOpen} onClose={() => setWithdrawOpen(false)} title="Rút tiền" size="sm">
        <div className="space-y-4">
          <p className="text-sm text-content-muted">Số dư: <span className="text-brand-400 font-semibold">{formatCurrency(wallet?.balance || 0)}</span></p>
          <Input
            placeholder="Nhập số tiền muốn rút"
            value={amount}
            onChange={(e) => setAmount(e.target.value)}
          />
          <p className="text-xs text-gray-600">Tiền sẽ được chuyển vào tài khoản ngân hàng đã liên kết</p>
          <Button fullWidth onClick={handleWithdraw} loading={actionLoading}>Xác nhận rút tiền</Button>
        </div>
      </Modal>
    </div>
  )
}

export default DriverWalletPage
