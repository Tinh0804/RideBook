package me.myproject.BUSINESSLOGIC;

import java.util.HashMap;
import java.util.Map;

import me.myproject.Utilities.APIHelper;
import me.myproject.Utilities.AppConfig;
import me.myproject.Utilities.Enum.PaymentMethod;

public class WalletBSL {
    
    private final String WALLET_URL = AppConfig.BASE_URL + "/wallets";

    // 1. Lấy thông tin ví
    public Map<String, Object> getWalletInfo() {
        try {
            String url = WALLET_URL + "/my-wallet" ;
            return APIHelper.getForMap(url); // Giả định bạn có hàm getForMap trong APIHelper
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Map<String, Object> deposit(String walletId, Double amount, PaymentMethod method) {
        try {
            String url = WALLET_URL + "/deposit";
            
            // Tạo payload khớp hoàn toàn với các field trong PaymentRequest DTO
            Map<String, Object> payload = new HashMap<>();
            payload.put("referenceId", walletId); // Hoặc mã giao dịch tạm thời
            payload.put("amount", amount);
            payload.put("orderInfo", "Nap tien vao vi tai xe: " + walletId);
            payload.put("method", method); // "VNPay" hoặc "MoMo"
            
            // Truyền Token trong Header (Nếu APIHelper của bạn có hỗ trợ)
            return APIHelper.postForMap(url, payload);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 3. Rút tiền (Tự động)
    public Map<String, Object> withdraw(String driverId, Double amount) {
        try {
            String url = WALLET_URL + "/withdraw";
            Map<String, Object> payload = new HashMap<>();
            payload.put("driverId", driverId);
            payload.put("amount", amount);
            
            return APIHelper.postForMap(url, payload);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public Map<String, Object> getTransactionHistory(String walletId) {
        try {
            String url = WALLET_URL + "/history-transactions?walletId=" + walletId;
            return APIHelper.getForMap(url);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}