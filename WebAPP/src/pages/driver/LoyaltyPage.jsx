import { useState, useEffect } from 'react'
import { motion } from 'motion/react'
import {
  RiVipCrownLine,
  RiHistoryLine,
  RiArrowRightLine,
  RiCheckLine,
  RiMoneyDollarCircleLine
} from 'react-icons/ri'
import { loyaltyApi } from '@/features/loyalty/api/loyaltyApi'
import Spinner from '@/components/Elements/Spinner'
import { formatDate } from '@/utils/formatDate'
import { cn } from '@/utils/cn'

const TIER_INFO = {
  BRONZE: { color: 'text-amber-700', bg: 'bg-amber-100', next: 'SILVER' },
  SILVER: { color: 'text-gray-400', bg: 'bg-gray-100', next: 'GOLD' },
  GOLD: { color: 'text-yellow-500', bg: 'bg-yellow-100', next: 'DIAMOND' },
  DIAMOND: { color: 'text-blue-500', bg: 'bg-blue-100', next: null }
}

const LoyaltyPage = () => {
  const [account, setAccount] = useState(null)
  const [transactions, setTransactions] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    Promise.all([
      loyaltyApi.getMyAccount(),
      loyaltyApi.getPointTransactions(0, 50)
    ]).then(([accData, txData]) => {
      setAccount(accData)
      setTransactions(txData.content || [])
    }).catch(console.error)
      .finally(() => setLoading(false))
  }, [])

  if (loading) {
    return (
      <div className="flex h-full items-center justify-center bg-[#e8ece3] dark:bg-surface-dark">
        <Spinner size="xl" />
      </div>
    )
  }

  if (!account) {
    return (
      <div className="flex h-full items-center justify-center bg-[#e8ece3] dark:bg-surface-dark">
        <p className="text-content-muted">Không tải được thông tin Loyalty</p>
      </div>
    )
  }

  const tierInfo = TIER_INFO[account.tier] || TIER_INFO.BRONZE

  return (
    <div className="h-full overflow-y-auto bg-[#e8ece3] dark:bg-surface-dark pointer-events-auto pb-12">
      <div className="mx-auto w-full max-w-3xl space-y-6 p-fluid">
        <div>
          <h1 className="font-display text-fluid-3xl font-bold text-gray-900 dark:text-white tracking-tight">Hạng thành viên & Quyền lợi</h1>
          <p className="text-sm text-gray-500 dark:text-gray-400 mt-1">Lịch sử tích lũy và đặc quyền dành cho Đối tác Tài xế</p>
        </div>

        {/* Overview Card */}
        <div className="rounded-3xl border border-gray-100 dark:border-surface-border bg-white dark:bg-surface-card p-fluid md:p-6 shadow-sm overflow-hidden relative">
          <div className="absolute top-0 right-0 w-32 h-32 bg-brand-500/10 rounded-full blur-3xl" />
          
          <div className="flex items-center gap-4 mb-6 relative z-10">
            <div className={cn("w-14 h-14 rounded-2xl flex items-center justify-center", tierInfo.bg, tierInfo.color)}>
              <RiVipCrownLine size={32} />
            </div>
            <div>
              <p className="text-sm font-bold text-gray-500 dark:text-gray-400 uppercase tracking-wider">Hạng đối tác</p>
              <h2 className={cn("text-2xl font-black", tierInfo.color)}>{account.tier}</h2>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4 relative z-10">
            <div className="p-fluid rounded-2xl bg-gray-50 dark:bg-surface-dark border border-gray-100 dark:border-surface-border">
              <div className="flex items-center gap-2 mb-1">
                <RiMoneyDollarCircleLine className="text-gray-500" size={20} />
                <p className="text-sm font-bold text-gray-600 dark:text-gray-300">Điểm hiện có</p>
              </div>
              <p className="text-fluid-3xl font-black text-brand-600 dark:text-brand-400 mt-2">
                {account.currentPoints.toLocaleString()}
              </p>
            </div>
            
            <div className="p-fluid rounded-2xl bg-gray-50 dark:bg-surface-dark border border-gray-100 dark:border-surface-border">
              <div className="flex items-center gap-2 mb-1">
                <RiCheckLine className="text-green-500" size={20} />
                <p className="text-sm font-bold text-gray-600 dark:text-gray-300">Giảm phí nền tảng</p>
              </div>
              <p className="text-sm font-medium text-gray-700 dark:text-white mt-2">
                Được giảm <span className="font-bold text-green-600 dark:text-green-400">{account.tierBenefits?.discountRate}%</span> phí nhận cuốc
              </p>
            </div>
          </div>

          {account.nextTierPoints != null && account.nextTierPoints > 0 && (
            <div className="mt-6 relative z-10">
              <div className="flex justify-between text-sm font-bold mb-2">
                <span className="text-gray-600 dark:text-gray-300">Điểm tích lũy</span>
                <span className="text-brand-600 dark:text-brand-400">
                  {account.lifetimePoints.toLocaleString()} / {account.nextTierPoints.toLocaleString()}
                </span>
              </div>
              <div className="h-2.5 w-full rounded-full overflow-hidden bg-gray-100 dark:bg-surface-dark">
                <motion.div 
                  initial={{ width: 0 }}
                  animate={{ width: `${Math.min((account.lifetimePoints / account.nextTierPoints) * 100, 100)}%` }}
                  transition={{ duration: 1, delay: 0.2 }}
                  className="h-full rounded-full bg-gradient-to-r from-brand-400 to-brand-600"
                />
              </div>
              <p className="text-xs text-gray-500 mt-2">
                Cần thêm {(account.nextTierPoints - account.lifetimePoints).toLocaleString()} điểm để lên hạng {tierInfo.next}
              </p>
            </div>
          )}
        </div>

        {/* Transaction History */}
        <div className="rounded-3xl border border-gray-100 dark:border-surface-border bg-white dark:bg-surface-card p-fluid md:p-6 shadow-sm">
          <div className="flex items-center gap-2 mb-6">
            <RiHistoryLine size={24} className="text-content-muted" />
            <h3 className="text-lg font-bold text-gray-900 dark:text-white">Lịch sử điểm thưởng</h3>
          </div>

          {transactions.length > 0 ? (
            <div className="space-y-4">
              {transactions.map((tx) => (
                <div key={tx.transactionId} className="flex items-center justify-between p-4 rounded-2xl bg-gray-50 dark:bg-surface-dark">
                  <div className="flex items-start gap-3">
                    <div className={cn(
                      "mt-0.5 p-2 rounded-xl",
                      tx.transactionType === 'EARN' ? 'bg-green-100 text-green-600' : 'bg-red-100 text-red-600'
                    )}>
                      {tx.transactionType === 'EARN' ? <RiCheckLine size={18} /> : <RiArrowRightLine size={18} />}
                    </div>
                    <div>
                      <p className="font-bold text-gray-900 dark:text-white">
                        {tx.transactionType === 'EARN' ? 'Nhận điểm' : 'Trừ điểm'}
                      </p>
                      <p className="text-xs text-gray-500 mt-1">{tx.description}</p>
                      <p className="text-xs text-gray-400 mt-0.5">{formatDate(tx.createdAt)}</p>
                    </div>
                  </div>
                  <div className={cn(
                    "font-black text-lg",
                    tx.transactionType === 'EARN' ? 'text-green-600' : 'text-red-600'
                  )}>
                    {tx.transactionType === 'EARN' ? '+' : '-'}{tx.points.toLocaleString()}
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="text-center py-8 text-gray-500">
              Chưa có lịch sử giao dịch nào
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

export default LoyaltyPage
