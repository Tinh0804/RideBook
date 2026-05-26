package me.myproject.BUSINESSLOGIC;

import me.myproject.Utilities.APIHelper;
import me.myproject.Utilities.AppConfig;

import java.util.Map;

public class KhuyenMaiBSL {

    // ── Khách hàng ───────────────────────────────────────────────────

    public Map<String, Object> layDanhSachKhuyenMaiKhaDung() {
        try {
            return APIHelper.getForMap(AppConfig.BASE_URL + "/promotions/active");
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("status", 500, "message", "Lỗi khi lấy danh sách khuyến mãi!");
        }
    }

    public Map<String, Object> kiemTraKhuyenMai(String promoCode) {
        try {
            return APIHelper.getForMap(AppConfig.BASE_URL + "/promotions/" + promoCode);
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("status", 500, "message", "Mã khuyến mãi không hợp lệ hoặc đã hết hạn!");
        }
    }

    // ── Admin ────────────────────────────────────────────────────────

    /** Lấy toàn bộ khuyến mãi (kể cả đã tắt) */
    public Map<String, Object> layTatCaKhuyenMai() {
        try {
            return APIHelper.getForMap(AppConfig.BASE_URL + "/promotions");
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("status", 500, "message", "Lỗi khi lấy danh sách khuyến mãi!");
        }
    }

    /** Tạo mới */
    public Map<String, Object> taoKhuyenMai(Map<String, Object> data) {
        try {
            return APIHelper.postForMap(AppConfig.BASE_URL + "/promotions", data);
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("status", 500, "message", "Không thể tạo khuyến mãi!");
        }
    }

    /** Cập nhật */
    public Map<String, Object> capNhatKhuyenMai(String promotionId, Map<String, Object> data) {
        try {
            return APIHelper.putForMap(AppConfig.BASE_URL + "/promotions/" + promotionId, data);
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("status", 500, "message", "Không thể cập nhật khuyến mãi!");
        }
    }

    /** Bật/tắt */
    public Map<String, Object> toggleKhuyenMai(String promotionId) {
        try {
            return APIHelper.patchForMap(AppConfig.BASE_URL + "/promotions/" + promotionId + "/toggle");
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("status", 500, "message", "Không thể thay đổi trạng thái khuyến mãi!");
        }
    }

    /** Xóa */
    public Map<String, Object> xoaKhuyenMai(String promotionId) {
        try {
            return APIHelper.deleteForMap(AppConfig.BASE_URL + "/promotions/" + promotionId);
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("status", 500, "message", "Không thể xóa khuyến mãi!");
        }
    }
}
