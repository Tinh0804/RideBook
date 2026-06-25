import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { masterDataApi } from '@/features/booking/api/masterDataApi';
import { formatCurrency } from '@/utils/currency';
import { formatDate } from '@/utils/formatDate';
import { toast } from 'react-hot-toast';
import { cn } from '@/utils/cn';
import { useAuthStore } from '@/store/rootStore';

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
  const [activeTab, setActiveTab] = useState('EXPLORE'); // 'EXPLORE' | 'WALLET'
  
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
        return;
    }
    try {
        await masterDataApi.savePromotion(customerId, code);
        toast.success(`Đã lưu mã ${code} vào Ví Voucher!`);
        // Tải lại danh sách voucher ngầm (silently)
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

  // Helper render card
  const renderPromoCard = (promo, idx, isWallet = false) => {
    const isSaved = myPromotions.some(p => p.promotionCode === promo.promotionCode);
    const isExpired = isWallet && (promo.isExpired || !promo.isActive);

    return (
      <div
        key={promo.promotionId || promo.promotionCode || idx}
        className={cn(
          "group bg-surface/60 backdrop-blur-sm rounded-2xl border overflow-hidden flex flex-col transition-all duration-300",
          isExpired 
            ? "border-red-500/30 opacity-70 grayscale-[0.5]" 
            : "border-white/10 hover:border-brand-500/40 hover:shadow-glow"
        )}
      >
        {/* Ảnh với overlay mờ */}
        <div className="relative h-44 overflow-hidden bg-surface-dark">
          <img
            src={promo.promotionImage || PROMO_IMAGES[idx % PROMO_IMAGES.length]}
            alt={promo.promotionName || "Khuyến mãi"}
            className="w-full h-full object-cover transition-transform duration-500 group-hover:scale-105"
            onError={(e) => { e.target.src = PROMO_IMAGES[0] }}
          />
          <div className="absolute inset-0 bg-gradient-to-t from-surface/90 via-surface/30 to-transparent" />
          
          {/* Labels góc phải */}
          {isExpired ? (
            <div className="absolute top-3 right-3 bg-red-500/90 text-white text-xs font-semibold px-2.5 py-1 rounded-full shadow-lg">
              Đã hết hạn
            </div>
          ) : idx === 0 && !isWallet ? (
            <div className="absolute top-3 right-3 bg-brand-500 text-white text-xs font-semibold px-2.5 py-1 rounded-full shadow-lg">
              Mới nhất
            </div>
          ) : isSaved && !isWallet ? (
            <div className="absolute top-3 right-3 bg-green-500 text-white text-xs font-semibold px-2.5 py-1 rounded-full shadow-lg">
              Đã lưu
            </div>
          ) : null}

          {/* Code */}
          <div className="absolute bottom-3 left-3 right-3">
            <div className="text-content-main font-mono font-bold text-lg bg-black/60 backdrop-blur-md inline-block px-3 py-1 rounded-xl border border-white/10">
              Mã: {promo.promotionCode}
            </div>
          </div>
        </div>

        {/* Nội dung */}
        <div className="p-5 flex flex-col flex-1">
          <h3 className="text-lg font-bold text-content-main mb-1 line-clamp-1">
            {promo.promotionName || `Ưu đãi giảm giá`}
          </h3>
          <p className="text-content-muted text-sm mb-4 line-clamp-2 min-h-[40px]">
            {promo.applicationCondition || `Giảm ${formatCurrency(promo.discountLimit)} cho chuyến đi tiếp theo.`}
          </p>

          <div className="mt-auto space-y-4">
            <div className="flex items-center justify-between text-sm">
              <span className="text-brand-400 font-bold text-xl">
                -{promo.discountType === 'PERCENTAGE' && promo.discountValue 
                    ? `${promo.discountValue}%` 
                    : formatCurrency(promo.discountLimit)}
              </span>
              <div className="text-right">
                <span className={cn(
                  "text-xs px-2 py-0.5 rounded-full",
                  isExpired ? "bg-red-500/10 text-red-400" : "bg-white/5 text-content-muted/70"
                )}>
                  {promo.endTime ? `HSD: ${formatDate(promo.endTime)}` : 'Không giới hạn'}
                </span>
                {promo.minTripValue > 0 && (
                  <p className="text-[10px] text-content-muted mt-1">
                    Đơn tối thiểu {formatCurrency(promo.minTripValue)}
                  </p>
                )}
              </div>
            </div>

            <div className="flex gap-2">
              {!isWallet ? (
                <>
                  <button
                    onClick={() => handleSavePromotion(promo.promotionCode)}
                    disabled={isSaved}
                    className={cn(
                      "flex-1 font-semibold py-2.5 rounded-xl transition-all duration-200 border",
                      isSaved 
                        ? "bg-surface-disabled text-content-muted border-transparent cursor-not-allowed"
                        : "bg-surface-border hover:bg-white/10 text-content-main border-white/10"
                    )}
                  >
                    {isSaved ? 'Đã lưu' : 'Lưu mã'}
                  </button>
                  <button
                    onClick={() => handleUsePromotion(promo.promotionCode)}
                    className="flex-1 bg-brand-500/90 hover:bg-brand-500 text-white font-semibold py-2.5 rounded-xl transition-all duration-200 shadow-md shadow-brand-500/20"
                  >
                    Dùng ngay
                  </button>
                </>
              ) : (
                <button
                  onClick={() => handleUsePromotion(promo.promotionCode)}
                  disabled={isExpired}
                  className={cn(
                    "w-full font-semibold py-2.5 rounded-xl transition-all duration-200 shadow-md",
                    isExpired
                      ? "bg-surface-disabled text-content-muted cursor-not-allowed"
                      : "bg-brand-500/90 hover:bg-brand-500 text-white shadow-brand-500/20"
                  )}
                >
                  {isExpired ? 'Đã hết hạn' : 'Dùng ngay'}
                </button>
              )}
            </div>
          </div>
        </div>
      </div>
    );
  };

  return (
    <div className="min-h-screen bg-gradient-to-b from-surface-dark to-surface pb-12">
      {/* Hero Section */}
      <div className="relative overflow-hidden bg-brand-500 rounded-b-3xl border-b border-white/10">
        <div className="absolute inset-0 bg-[url('https://www.transparenttextures.com/patterns/cubes.png')] opacity-5" />
        <div className="max-w-5xl mx-auto px-4 py-12 text-center">
          <h1 className="text-3xl md:text-4xl font-bold text-white drop-shadow-sm">
            Ưu đãi độc quyền
          </h1>
          <p className="text-white/80 mt-2 max-w-lg mx-auto text-sm md:text-base">
            Lưu mã vào Ví Voucher để không bỏ lỡ các đặc quyền giảm giá!
          </p>
        </div>
      </div>

      <div className="max-w-5xl mx-auto px-4 -mt-6">
        {/* Tabs Điều hướng */}
        <div className="bg-surface/80 backdrop-blur-xl p-1.5 rounded-2xl border border-white/10 flex shadow-xl max-w-sm mx-auto mb-10 relative z-10">
          <button
            className={cn(
              "flex-1 py-2.5 text-sm font-semibold rounded-xl transition-all duration-300",
              activeTab === 'EXPLORE' 
                ? "bg-brand-500 text-white shadow-md" 
                : "text-content-muted hover:text-content-main hover:bg-white/5"
            )}
            onClick={() => setActiveTab('EXPLORE')}
          >
            Khám phá ưu đãi
          </button>
          <button
            className={cn(
              "flex-1 py-2.5 text-sm font-semibold rounded-xl transition-all duration-300 relative",
              activeTab === 'WALLET' 
                ? "bg-brand-500 text-white shadow-md" 
                : "text-content-muted hover:text-content-main hover:bg-white/5"
            )}
            onClick={() => setActiveTab('WALLET')}
          >
            Ví Voucher
            {myPromotions.length > 0 && (
              <span className="absolute top-1.5 right-2 flex h-4 w-4 items-center justify-center rounded-full bg-red-500 text-[9px] text-white">
                {myPromotions.length}
              </span>
            )}
          </button>
        </div>

        {/* Content */}
        {loading ? (
          <div className="flex justify-center items-center py-20">
            <div className="animate-spin rounded-full h-10 w-10 border-t-2 border-b-2 border-brand-500" />
          </div>
        ) : activeTab === 'EXPLORE' ? (
          /* Tab Khám phá */
          promotions.length === 0 ? (
            <div className="text-center py-16 bg-surface/50 backdrop-blur-sm rounded-2xl border border-white/10">
              <p className="text-content-muted text-lg">Hiện chưa có chương trình khuyến mãi nào.</p>
              <p className="text-content-muted/70 text-sm mt-1">Vui lòng quay lại sau nhé!</p>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {promotions.map((promo, idx) => renderPromoCard(promo, idx, false))}
            </div>
          )
        ) : (
          /* Tab Ví Voucher */
          !isAuth ? (
            <div className="text-center py-16 bg-surface/50 backdrop-blur-sm rounded-2xl border border-white/10">
              <p className="text-content-main font-semibold mb-3">Bạn chưa đăng nhập</p>
              <button 
                onClick={() => navigate('/login/customer')}
                className="bg-brand-500 hover:bg-brand-600 text-white px-6 py-2 rounded-xl transition-colors font-medium"
              >
                Đăng nhập ngay
              </button>
            </div>
          ) : (
            <div className="space-y-6">
              {/* Nhập mã thủ công */}
              <div className="bg-surface/50 backdrop-blur-sm p-4 rounded-2xl border border-white/10 flex items-center justify-center max-w-xl mx-auto">
                <form onSubmit={handleManualSave} className="flex flex-col sm:flex-row gap-3 w-full">
                  <input 
                    type="text" 
                    placeholder="Nhập mã khuyến mãi (VD: VIP100)"
                    value={manualCode}
                    onChange={(e) => setManualCode(e.target.value)}
                    className="flex-1 bg-surface-dark border border-white/10 rounded-xl px-4 py-2.5 text-content-main focus:outline-none focus:border-brand-500 uppercase font-mono"
                  />
                  <button 
                    type="submit"
                    disabled={!manualCode.trim()}
                    className="bg-brand-500 hover:bg-brand-600 disabled:opacity-50 disabled:cursor-not-allowed text-white font-semibold px-6 py-2.5 rounded-xl transition-all shadow-md shadow-brand-500/20"
                  >
                    Lưu mã
                  </button>
                </form>
              </div>

              {myPromotions.length === 0 ? (
                <div className="text-center py-16 bg-surface/50 backdrop-blur-sm rounded-2xl border border-white/10">
                  <div className="w-16 h-16 bg-white/5 rounded-full flex items-center justify-center mx-auto mb-4">
                    <span className="text-2xl">🎫</span>
                  </div>
                  <p className="text-content-muted text-lg font-medium">Ví Voucher của bạn đang trống.</p>
                  <p className="text-content-muted/70 text-sm mt-1 mb-6">Hãy sang tab Khám phá để lưu mã nhé!</p>
                  <button 
                    onClick={() => setActiveTab('EXPLORE')}
                    className="bg-brand-500 hover:bg-brand-600 text-white px-6 py-2 rounded-xl transition-colors font-medium shadow-lg shadow-brand-500/20"
                  >
                    Khám phá ngay
                  </button>
                </div>
              ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                  {myPromotions.map((promo, idx) => renderPromoCard(promo, idx, true))}
                </div>
              )}
            </div>
          )
        )}
      </div>
    </div>
  );
};

export default CustomerPromotionsPage;