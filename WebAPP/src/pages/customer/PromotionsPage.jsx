import { useState, useEffect } from 'react';
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
  const { userProfile } = useAuthStore();
  const [promotions, setPromotions] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    masterDataApi.getActivePromotions()
      .then((data) => setPromotions(data || []))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  const handleSavePromotion = async (code) => {
    if (!userProfile?.id) {
        toast.error('Vui lòng đăng nhập để lưu khuyến mãi');
        return;
    }
    try {
        await masterDataApi.savePromotion(userProfile.id, code);
        toast.success(`Đã lưu mã ${code} vào Ví Voucher!`);
    } catch (error) {
        toast.error(error.response?.data?.message || 'Không thể lưu mã khuyến mãi này');
    }
  };

  const handleUsePromotion = (code) => {
    navigator.clipboard.writeText(code);
    toast.success(`Đã copy mã ${code} vào bộ nhớ tạm!`);
    navigate('/customer/booking');
  };

  return (
    <div className="min-h-screen bg-gradient-to-b from-surface-dark to-surface">
      {/* Hero Section – đơn giản, không quá rực rỡ, thích ứng nền */}
      <div className="relative overflow-hidden bg-brand-500 rounded-xl border-r border-b border-white/10">
        <div className="absolute inset-0 bg-[url('https://www.transparenttextures.com/patterns/cubes.png')] opacity-5" />
        <div className="max-w-7xl mx-auto px-4 py-12 md:py-16 text-center">
          <h1 className="text-3xl md:text-4xl font-bold text-content-main drop-shadow-sm">
            Ưu đãi độc quyền
          </h1>
          <p className="text-content-muted mt-2 max-w-lg mx-auto text-white">
            Nhập mã ngay để nhận giảm giá cho chuyến đi tiếp theo
          </p>
        </div>
      </div>

      {/* Promotions Grid */}
      <div className="max-w-7xl mx-auto px-4 py-12">
        {loading ? (
          <div className="flex justify-center items-center py-20">
            <div className="animate-spin rounded-full h-10 w-10 border-t-2 border-b-2 border-brand-500" />
          </div>
        ) : promotions.length === 0 ? (
          <div className="text-center py-16 bg-surface/50 backdrop-blur-sm rounded-2xl border border-white/10">
            <p className="text-content-muted text-lg">Hiện chưa có chương trình khuyến mãi nào.</p>
            <p className="text-content-muted/70 text-sm mt-1">Vui lòng quay lại sau nhé!</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {promotions.map((promo, idx) => (
              <div
                key={promo.promotionId || idx}
                className="group bg-surface/60 backdrop-blur-sm rounded-2xl border border-white/10 overflow-hidden hover:border-brand-500/40 hover:shadow-glow transition-all duration-300 flex flex-col"
              >
                {/* Ảnh với overlay mờ */}
                <div className="relative h-44 overflow-hidden">
                  <img
                    src={PROMO_IMAGES[idx % PROMO_IMAGES.length]}
                    alt={promo.promotionName || "Khuyến mãi"}
                    className="w-full h-full object-cover transition-transform duration-500 group-hover:scale-105"
                  />
                  <div className="absolute inset-0 bg-gradient-to-t from-surface/80 to-transparent" />
                  {idx === 0 && (
                    <div className="absolute top-3 right-3 bg-brand-500 text-white text-xs font-semibold px-2.5 py-1 rounded-full shadow-lg">
                      Mới nhất
                    </div>
                  )}
                  <div className="absolute bottom-3 left-3 right-3">
                    <div className="text-content-main font-mono font-bold text-lg bg-black/40 backdrop-blur-sm inline-block px-3 py-1 rounded-lg">
                      Mã: {promo.promotionCode}
                    </div>
                  </div>
                </div>

                {/* Nội dung */}
                <div className="p-5 flex flex-col flex-1">
                  <h3 className="text-lg font-bold text-content-main mb-1">
                    {promo.promotionName || `Ưu đãi giảm giá`}
                  </h3>
                  <p className="text-content-muted text-sm mb-4 line-clamp-2">
                    {promo.description || `Giảm ngay ${formatCurrency(promo.discountLimit)} cho chuyến đi tiếp theo. Số lượng có hạn!`}
                  </p>

                  <div className="mt-auto space-y-3">
                    <div className="flex items-center justify-between text-sm">
                      <span className="text-brand-400 font-bold text-xl">
                        -{formatCurrency(promo.discountLimit)}
                      </span>
                      <span className="text-content-muted/70 text-xs bg-white/5 px-2 py-0.5 rounded-full">
                        {promo.endTime ? `HSD: ${formatDate(promo.endTime)}` : 'Không giới hạn'}
                      </span>
                    </div>
                    <div className="flex gap-2">
                      <button
                        onClick={() => handleSavePromotion(promo.promotionCode)}
                        className="flex-1 bg-surface-border hover:bg-white/10 text-content-main font-semibold py-2.5 rounded-xl transition-all duration-200 border border-white/10"
                      >
                        Lưu mã
                      </button>
                      <button
                        onClick={() => handleUsePromotion(promo.promotionCode)}
                        className="flex-1 bg-brand-500/90 hover:bg-brand-500 text-white font-semibold py-2.5 rounded-xl transition-all duration-200 shadow-md shadow-brand-500/20"
                      >
                        Sử dụng
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default CustomerPromotionsPage;