package utils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import model.TaiKhoan;

public class TokenManager {
	// Lưu token và thông tin token tương ứng (kể cả thời gian tạo token)
    private static final Map<String, TokenInfo> tokenStore = new HashMap<>();
    private static final long EXPIRE_DURATION = 30 * 60 * 1000; // 30 phút

    // Sinh token mới và lưu thông tin token
    public static String generateToken(TaiKhoan admin) {
        String token = UUID.randomUUID().toString();
        tokenStore.put(token, new TokenInfo( System.currentTimeMillis(),admin));
        return token;
    }

    // Kiểm tra token có hợp lệ (chưa hết hạn) hay không
    public static boolean isTokenValid(String token) {
        TokenInfo info = tokenStore.get(token);
        if (info == null) {
            return false;
        }
        long now = System.currentTimeMillis();
        return (now - info.getCreateTime()) <= EXPIRE_DURATION;
    }

    // Lấy thông tin tài khoản từ token (nếu token hợp lệ)
    public static TaiKhoan getAccountByToken(String token) {
        TokenInfo info = tokenStore.get(token);
        if (info != null && isTokenValid(token)) {
            return info.getTaiKhoan();
        }
        return null; // token hết hạn hoặc không tồn tại
    }

    // Xóa token khi logout
    public static void invalidateToken(String token) {
        tokenStore.remove(token);
    }
}
