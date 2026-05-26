package me.myproject.BUSINESSLOGIC;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import me.myproject.MODEL.TaiKhoan;
import me.myproject.MODEL.VaiTro;
import me.myproject.Utilities.APIHelper;
import me.myproject.Utilities.AppConfig;
import me.myproject.Utilities.GsonUtil;
import me.myproject.Utilities.TokenStore;

public class DangNhapBSL {
    private HashMap<String, String> listAccount;
    private HashMap<String, String> otpMap;

    public DangNhapBSL() {
        listAccount = new HashMap<>();
        otpMap = new HashMap<>();
    }

    public Map<String, Object> xuLyDangNhap(String phone, String password, String role, boolean rememberToken) {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, String> data = Map.of("userName", phone, "passWord", password, "roleName", role);

            Map<String, Object> response = APIHelper.postForMap(AppConfig.BASE_URL + "/auth/login", data);
            Number status = (Number) response.get("status");
            boolean success = status != null && status.intValue() == 200;

            if (success) {
                Map<String, Object> authResult = (Map<String, Object>) response.get("result");
                if (authResult == null) {
                    throw new IOException("Phản hồi đăng nhập không hợp lệ");
                }
                Map<String, Object> accountData = (Map<String, Object>) authResult.get("account");
                if (accountData == null) {
                    throw new IOException("Thiếu thông tin tài khoản trong phản hồi");
                }
                Map<String, Object> roleJson = (Map<String, Object>) accountData.get("role");
                if (roleJson == null) {
                    throw new IOException("Thiếu thông tin vai trò trong phản hồi");    
                }
                VaiTro roleData = GsonUtil.convert(roleJson, VaiTro.class);
                if (roleData == null) {
                    throw new IOException("Không thể giải mã vai trò từ phản hồi");
                }

               

                TaiKhoan taiKhoan = new TaiKhoan(
                    (String) accountData.get("userName"),
                    null,
                    roleData,
                    (String) accountData.get("accountId")
                );

                String token = (String) authResult.get("token");
                System.out.println("Token nhận được: " + token); // Debug token
                String refreshToken = (String) authResult.get("refreshToken");
                String profileId = null;

                if (token != null && !token.isBlank()) {
                    APIHelper.setAuthToken(token);
                }

                // Always use token to fetch profile info after login
                profileId = fetchProfileId(roleData.getRoleId());
                if (profileId != null) {
                    taiKhoan.setID_Ref(profileId);
                } else {
                    System.err.println("Không thể lấy profileId sau khi đăng nhập");
                }
                if (rememberToken) {
                    try {
                        TokenStore.saveAuthState(token, refreshToken, roleData.getRoleId(), profileId, (String) accountData.get("userName"));
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }

                taiKhoan.setToken(token);
                taiKhoan.setRefreshToken(refreshToken);
                if (rememberToken) {
                    taiKhoan.setUserName((String) accountData.get("userName"));
                }
                taiKhoan.setID_Ref(profileId);

                result.put("message", "Đăng nhập thành công");
                result.put("taiKhoan", taiKhoan);
            } else {
                String errorMessage = (String) response.getOrDefault("message", "Đăng nhập thất bại!");
                result.put("message", errorMessage);
                result.put("taiKhoan", null);
            }
        } catch (IOException e) {
            e.printStackTrace();
            result.put("message", "Không kết nối được tới server!");
            result.put("taiKhoan", null);
        }
        return result;
    }



    public boolean kiemTraTonTaiSDT(String phone) {
        try {
            Map<String, Object> response = APIHelper.getForMap(AppConfig.BASE_URL + "/auth/check-phone?phone=" + phone);
            Number status = (Number) response.get("status");
            return status != null && status.intValue() >= 200 && status.intValue() < 300;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String xuLyQuenMatKhau(String phone) {
        if (phone == null || phone.trim().isEmpty()) 
            return "Vui lòng nhập số điện thoại!";
        if (!kiemTraTonTaiSDT(phone)) 
            return "Số điện thoại không tồn tại trong hệ thống.";
        sendOTP(phone);
        return "Mã OTP đã được gửi đến số điện thoại của bạn.";
    }

    private void sendOTP(String phone) {
        otpMap.put(phone, "123456");
    }

    public String xacThucOTP(String phone, String otpInput) {
        if (otpInput == null || otpInput.trim().isEmpty()) 
            return "Bạn chưa nhập mã OTP!";
        String validOTP = otpMap.get(phone);
        if (validOTP != null && validOTP.equals(otpInput)) 
            return "Xác thực OTP thành công! Vui lòng nhập mật khẩu mới.";
        else 
            return "Mã OTP không hợp lệ!";
    }

    public String capNhatMatKhau(String phone, String newPass, String confirmPass) {
        if (newPass == null || newPass.trim().isEmpty() || confirmPass == null || confirmPass.trim().isEmpty()) 
            return "Mật khẩu không được để trống!";
        if (!newPass.equals(confirmPass)) 
            return "Mật khẩu không khớp!";
        try {
            Map<String, String> data = new HashMap<>();
            data.put("phone", phone);
            data.put("newPassword", newPass);
            Map<String, Object> response = APIHelper.putForMap(AppConfig.BASE_URL + "/auth/reset-password", data);
            Number status = (Number) response.get("status");
            if (status != null && status.intValue() >= 200 && status.intValue() < 300) {
                return "Đổi mật khẩu thành công!";
            } else {
                return (String) response.getOrDefault("message", "Lỗi khi cập nhật mật khẩu!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Lỗi khi kết nối tới server!";
        }
    }
    private String fetchProfileId(String roleId) {
        if (roleId == null) {
            return null;
        }
        try {
            String apiUrl;
            if ("CUSTOMER".equalsIgnoreCase(roleId)) {
                apiUrl = AppConfig.BASE_URL + "/customers/my-info";
                Map<String, Object> response = APIHelper.getForMap(apiUrl);
                Number status = (Number) response.get("status");
                if (status != null && status.intValue() >= 200 && status.intValue() < 300) {
                    Map<String, Object> result = (Map<String, Object>) response.get("result");
                    return result != null ? (String) result.get("customerId") : null;
                }
            }
            if ("DRIVER".equalsIgnoreCase(roleId)) {
                apiUrl = AppConfig.BASE_URL + "/drivers/my-info";
                Map<String, Object> response = APIHelper.getForMap(apiUrl);
                Number status = (Number) response.get("status");
                if (status != null && status.intValue() >= 200 && status.intValue() < 300) {
                    Map<String, Object> result = (Map<String, Object>) response.get("result");
                    return result != null ? (String) result.get("driverId") : null;
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    // public static void main(String[] args) {
    //     Map<String, Object> result = xuLyDangNhap("0355160346", String password, String role)
    // }
}
