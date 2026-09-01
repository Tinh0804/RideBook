import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { masterDataApi } from '@/features/booking/api/masterDataApi';
import { formatCurrency } from '@/utils/currency';
import { formatDate } from '@/utils/formatDate';
import { toast } from 'react-hot-toast';
import { cn } from '@/utils/cn';
import { useAuthStore } from '@/store/rootStore';
import Button from '@/components/Elements/Button';
import Spinner from '@/components/Elements/Spinner';
import Input from '@/components/Elements/Input';
import { motion, AnimatePresence } from 'motion/react';
import { RiTicketLine, RiArrowRightLine, RiTimeLine, RiFileCopyLine } from 'react-icons/ri';

const PROMO_IMAGES = [
  "https://images.unsplash.com/photo-1542751371-adc2131af163?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80",
  "https://images.unsplash.com/photo-1504877492558-417e74b78840?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80",
  "https://images.unsplash.com/photo-1581090464777-f321c957e1bf?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80",
  "https://images.unsplash.com/photo-1525609004556-c46c7d6cf023?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80",
  "https://images.unsplash.com/photo-1519389950473-471779a99f5d?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80"
];

const CustomerPromotionsPage = () => {
  const navigate = useNavigate();
  const { userProfile, isAuth } = useAuthStore();
  const [activeTab, setActiveTab] = useState('EXPLORE'); 
  
  const [promotions, setPromotions] = useState([]);
  const [myPromotions, setMyPromotions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [manualCode, setManualCode] = useState('');

  const fetchPromotions = useCallback(async () => {
    setLoading(true);
    try {
      const activePromos = await masterDataApi.getActivePromotions();
      setPromotions(Array.isArray(activePromos) ? activePromos : []);

      if (isAuth && (userProfile?.id || userProfile?.customerId)) {
        const savedPromos = await masterDataApi.getMyPromotions(userProfile?.id || userProfile?.customerId);
        setMyPromotions(Array.isArray(savedPromos) ? savedPromos : []);
      }
    } catch (error) {
      console.error('Lỗi khi tải khuyến mãi:', error);
    } finally {
      setLoading(false);
    }
  }, [isAuth, userProfile]);

  useEffect(() => {
    fetchPromotions();
  }, [fetchPromotions]);

  const handleSavePromotion = async (code) => {
    const customerId = userProfile?.id || userProfile?.customerId;
    if (!customerId) {
        toast.error('Vui lòng đăng nhập để lưu khuyến mãi');
        navigate('/login/customer');
        return;
    }
    try {
        await masterDataApi.savePromotion(customerId, code);
        toast.success(`Đã lưu mã ${code} vào Ví Voucher!`);
        const savedPromos = await masterDataApi.getMyPromotions(customerId);
        setMyPromotions(Array.isArray(savedPromos) ? savedPromos : []);
    } catch (error) {
        toast.error(error.response?.data?.message || 'Không thể lưu mã khuyến mãi này');
    }
  };

  const handleManualSave = async (e) => {
    e.preventDefault();
    if (!manualCode.trim()) return;
    await handleSavePromotion(manualCode.trim().toUpperCase());
    setManualCode('');
  };

  const handleUsePromotion = (code) => {
    navigator.clipboard.writeText(code);
    toast.success(`Đã copy mã ${code} vào bộ nhớ tạm!`);
    navigate('/customer/booking');
  };

  const renderPromoCard = (promo, idx, isWallet = false) => {
    const isSaved = myPromotions.some(p => p.promotionCode === promo.promotionCode);
    const isExpired = isWallet && (promo.isExpired || !promo.isActive);

    return (
      <motion.div
        initial={{ opacity: 0, y: 15 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.4, delay: idx * 0.05, ease: [0.16, 1, 0.3, 1] }}
        key={promo.promotionId || promo.promotionCode || idx}
        className={cn(
          "group relative flex flex-col overflow-hidden rounded-2xl border bg-surface-card transition-all duration-300",
          isExpired 
            ? "border-surface-border/50 opacity-60 grayscale-[0.8]" 
            : "border-surface-border hover:border-slate-400 dark:hover:border-slate-500 shadow-sm hover:shadow-md"
        )}
      >
        <div className="relative h-40 overflow-hidden bg-slate-100 dark:bg-slate-900">
          <img
            src={promo.promotionImage || PROMO_IMAGES[idx % PROMO_IMAGES.length]}
            alt={promo.promotionName || "Khuyến mãi"}
            className="h-full w-full object-cover transition-transform duration-700 group-hover:scale-105"
            onError={(e) => { e.target.src = PROMO_IMAGES[0] }}
          />
          <div className="absolute inset-0 bg-gradient-to-t from-slate-950/80 via-slate-950/20 to-transparent" />
          
          <div className="absolute left-4 right-4 top-4 flex justify-between items-start">
            {isExpired ? (
              <span className="rounded-md bg-slate-900/90 px-2.5 py-1 text-[11px] font-bold uppercase tracking-wider text-white backdrop-blur">
                Đã hết hạn
              </span>
            ) : idx === 0 && !isWallet ? (
              <span className="rounded-md bg-lime-accent px-2.5 py-1 text-[11px] font-bold uppercase tracking-wider text-slate-950 shadow-sm">
                Mới nhất
              </span>
            ) : isSaved && !isWallet ? (
              <span className="rounded-md bg-emerald-500 px-2.5 py-1 text-[11px] font-bold uppercase tracking-wider text-white shadow-sm">
                Đã lưu
              </span>
            ) : (
              <span /> 
            )}
            
            <div className="flex items-center gap-1.5 rounded-lg bg-slate-950/80 px-2.5 py-1 text-white backdrop-blur">
              <RiTicketLine size={14} className="text-lime-accent" />
              <span className="font-mono text-xs font-bold tracking-widest">{promo.promotionCode}</span>
            </div>
          </div>
        </div>

        <div className="flex flex-1 flex-col p-5">
          <h3 className="mb-2 font-display text-lg font-bold leading-tight text-content-main line-clamp-2">
            {promo.promotionName || `Ưu đãi giảm giá đặc biệt`}
          </h3>
          <p className="mb-5 min-h-[40px] text-sm leading-relaxed text-content-muted line-clamp-2">
            {promo.applicationCondition || `Giảm ${formatCurrency(promo.discountLimit)} cho chuyến đi của bạn.`}
          </p>

          <div className="mb-5 mt-auto flex items-end justify-between border-t border-surface-border pt-4">
            <div>
              <p className="text-[11px] font-semibold uppercase tracking-wider text-content-muted mb-0.5">Giảm giá</p>
              <p className="font-display text-2xl font-bold text-slate-950 dark:text-white">
                {promo.discountType === 'PERCENTAGE' && promo.discountValue 
                    ? `${promo.discountValue}%` 
                    : formatCurrency(promo.discountLimit)}
              </p>
            </div>
            <div className="text-right">
              <span className="flex items-center justify-end gap-1 text-xs font-semibold text-content-muted">
                <RiTimeLine size={13} />
                {promo.endTime ? formatDate(promo.endTime) : 'Vô thời hạn'}
              </span>
              {promo.minTripValue > 0 && (
                <p className="mt-1 text-[10px] font-semibold text-amber-600 dark:text-amber-400">
                  Đơn tối thiểu {formatCurrency(promo.minTripValue)}
                </p>
              )}
            </div>
          </div>

          <div className="flex gap-2">
            {!isWallet ? (
              <>
                <Button
                  variant={isSaved ? "outline" : "primary"}
                  onClick={() => handleSavePromotion(promo.promotionCode)}
                  disabled={isSaved}
                  className={cn(
                    "flex-1 rounded-xl h-11 font-bold",
                    !isSaved && "bg-slate-950 text-white hover:bg-slate-800 dark:bg-white dark:text-slate-950 dark:hover:bg-slate-200"
                  )}
                >
                  {isSaved ? 'Đã lưu' : 'Lưu mã'}
                </Button>
                <Button
                  onClick={() => handleUsePromotion(promo.promotionCode)}
                  className="flex-1 rounded-xl h-11 bg-lime-accent text-slate-950 font-bold hover:bg-[#b8ff59]"
                >
                  Dùng ngay
                </Button>
              </>
            ) : (
              <Button
                fullWidth
                onClick={() => handleUsePromotion(promo.promotionCode)}
                disabled={isExpired}
                className={cn(
                  "rounded-xl h-11 font-bold",
                  isExpired 
                    ? "opacity-50"
                    : "bg-slate-950 text-white hover:bg-slate-800 dark:bg-white dark:text-slate-950 dark:hover:bg-slate-200"
                )}
              >
                {isExpired ? 'Đã hết hạn' : 'Sử dụng ngay'}
              </Button>
            )}
          </div>
        </div>
      </motion.div>
    );
  };

  return (
    <div className="h-full overflow-y-auto bg-[#e8ece3] dark:bg-surface-dark pointer-events-auto">
      <motion.div
        initial={{ opacity: 0, x: -18 }}
        animate={{ opacity: 1, x: 0 }}
        transition={{ duration: 0.45, ease: [0.16, 1, 0.3, 1] }}
        className="mx-auto w-full max-w-5xl space-y-6 p-5 pb-10 lg:p-8"
      >
        {/* Sleek Hero Section */}
        <section className="relative min-h-40 overflow-hidden rounded-2xl bg-slate-950 p-6 sm:p-8 text-white shadow-sm">
          <div className="relative z-10 max-w-[80%]">
            <p className="mb-3 text-sm font-semibold text-lime-accent uppercase tracking-wider">BookCar Rewards</p>
            <h1 className="font-display text-4xl font-bold leading-[1.05] tracking-[-0.04em]">
              Ưu đãi<br />Độc quyền
            </h1>
            <p className="mt-4 text-sm leading-relaxed text-white/55">
              Khám phá và lưu ngay các mã khuyến mãi tốt nhất dành riêng cho bạn.
            </p>
          </div>
          <span className="absolute -bottom-6 right-2 font-display text-[9rem] font-bold tracking-[-0.08em] text-white/[.04] select-none">
            %
          </span>
        </section>

        {/* Tab Navigation */}
        <div className="flex gap-2 border-b border-surface-border">
          <button
            onClick={() => setActiveTab('EXPLORE')}
            className={cn(
              'px-5 py-3 text-sm font-bold transition-all relative',
              activeTab === 'EXPLORE'
                ? 'text-slate-950 dark:text-white'
                : 'text-content-muted hover:text-content-main'
            )}
          >
            Khám phá
            {activeTab === 'EXPLORE' && (
              <motion.div layoutId="promoTab" className="absolute bottom-0 left-0 right-0 h-0.5 bg-slate-950 dark:bg-white" />
            )}
          </button>
          <button
            onClick={() => setActiveTab('WALLET')}
            className={cn(
              'px-5 py-3 text-sm font-bold transition-all relative flex items-center gap-2',
              activeTab === 'WALLET'
                ? 'text-slate-950 dark:text-white'
                : 'text-content-muted hover:text-content-main'
            )}
          >
            Ví Voucher
            {myPromotions.length > 0 && (
              <span className="flex h-5 w-5 items-center justify-center rounded-full bg-slate-950 dark:bg-white text-[10px] text-white dark:text-slate-950">
                {myPromotions.length}
              </span>
            )}
            {activeTab === 'WALLET' && (
              <motion.div layoutId="promoTab" className="absolute bottom-0 left-0 right-0 h-0.5 bg-slate-950 dark:bg-white" />
            )}
          </button>
        </div>

        {/* Content Area */}
        {loading ? (
          <div className="flex justify-center py-20">
            <Spinner size="lg" />
          </div>
        ) : (
          <AnimatePresence mode="wait">
            {activeTab === 'EXPLORE' && (
              <motion.div
                key="explore"
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: -10 }}
                transition={{ duration: 0.2 }}
              >
                {promotions.length === 0 ? (
                  <div className="rounded-2xl border border-surface-border bg-surface-card py-20 text-center shadow-sm">
                    <p className="font-display text-xl font-bold text-content-main">Chưa có khuyến mãi mới</p>
                    <p className="mt-2 text-sm text-content-muted">Vui lòng quay lại sau nhé.</p>
                  </div>
                ) : (
                  <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
                    {promotions.map((promo, idx) => renderPromoCard(promo, idx, false))}
                  </div>
                )}
              </motion.div>
            )}

            {activeTab === 'WALLET' && (
              <motion.div
                key="wallet"
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: -10 }}
                transition={{ duration: 0.2 }}
                className="space-y-6"
              >
                {!isAuth ? (
                  <div className="rounded-2xl border border-surface-border bg-surface-card py-20 text-center shadow-sm">
                    <p className="font-display text-xl font-bold text-content-main mb-4">Bạn chưa đăng nhập</p>
                    <Button 
                      onClick={() => navigate('/login/customer')}
                      className="rounded-xl bg-slate-950 font-bold text-white hover:bg-slate-800 dark:bg-white dark:text-slate-950"
                    >
                      Đăng nhập để xem Ví
                    </Button>
                  </div>
                ) : (
                  <>
                    {/* Manual Save Form */}
                    <div className="rounded-2xl border border-surface-border bg-surface-card p-4 shadow-sm flex flex-col sm:flex-row gap-3">
                      <div className="flex-1 relative">
                        <RiTicketLine className="absolute left-4 top-1/2 -translate-y-1/2 text-content-muted" />
                        <Input 
                          placeholder="Nhập mã khuyến mãi (VD: VIP100)"
                          value={manualCode}
                          onChange={(e) => setManualCode(e.target.value)}
                          className="w-full !pl-10 !rounded-xl !bg-surface-dark !border-surface-border font-mono uppercase"
                        />
                      </div>
                      <Button 
                        onClick={handleManualSave}
                        disabled={!manualCode.trim()}
                        className="h-11 rounded-xl bg-slate-950 font-bold text-white hover:bg-slate-800 dark:bg-white dark:text-slate-950 sm:w-auto w-full"
                      >
                        Thêm vào ví
                      </Button>
                    </div>

                    {myPromotions.length === 0 ? (
                      <div className="rounded-2xl border border-surface-border bg-surface-card py-20 text-center shadow-sm">
                        <div className="mx-auto mb-4 grid h-16 w-16 place-items-center rounded-full bg-surface-muted text-content-muted">
                          <RiTicketLine size={32} />
                        </div>
                        <p className="font-display text-xl font-bold text-content-main">Ví trống</p>
                        <p className="mt-2 text-sm text-content-muted">Hãy chuyển sang tab Khám phá để lưu mã nhé.</p>
                      </div>
                    ) : (
                      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
                        {myPromotions.map((promo, idx) => renderPromoCard(promo, idx, true))}
                      </div>
                    )}
                  </>
                )}
              </motion.div>
            )}
          </AnimatePresence>
        )}
      </motion.div>
    </div>
  );
};

export default CustomerPromotionsPage;