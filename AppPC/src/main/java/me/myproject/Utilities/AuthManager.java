package me.myproject.Utilities;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

import me.myproject.MODEL.TaiKhoan;
import me.myproject.MODEL.VaiTro;

public class AuthManager {
    private AuthManager() {
    }

    public static TaiKhoan tryRestoreSession() {
        try {
            Optional<String> accessToken = TokenStore.loadAccessToken();
            Optional<String> refreshToken = TokenStore.loadRefreshToken();
            if (accessToken.isEmpty() || refreshToken.isEmpty()) {
                return null;
            }
            APIHelper.setAuthToken(accessToken.get());

            boolean tokenValid = introspectToken();
            if (!tokenValid) {
                String refreshed = refreshAccessToken(refreshToken.get());
                if (refreshed == null || refreshed.isBlank()) {
                    return null;
                }
                APIHelper.setAuthToken(refreshed);
            }

            String role = TokenStore.loadRole().orElse(null);
            String profileId = TokenStore.loadProfileId().orElse(null);
            String userName = TokenStore.loadUserName().orElse(null);

            if (role == null || profileId == null) {
                Map<String, Object> userInfo = DangNhapBSLAdapter.fetchProfileInfo();
                if (userInfo != null) {
                    role = (String) userInfo.get("role");
                    profileId = (String) userInfo.get("profileId");
                    userName = userName != null ? userName : (String) userInfo.get("userName");
                }
            }

            if (role != null && profileId != null) {
                APIHelper.saveAuthState(APIHelper.getAuthToken().orElse(null), refreshToken.get(), role, profileId, userName);
            }

            if (role == null || profileId == null) {
                return null;
            }

            VaiTro vaiTro = new VaiTro();
            vaiTro.setRoleId(role);
            if(role.equalsIgnoreCase("CUSTOMER")) {
                vaiTro.setRoleName("Khách hàng");
            } else if(role.equalsIgnoreCase("DRIVER")) {
                vaiTro.setRoleName("Tài xế");
            } else {
                vaiTro.setRoleName("Người dùng");
            }

            TaiKhoan taiKhoan = new TaiKhoan(userName, null, vaiTro, profileId);
            taiKhoan.setToken(APIHelper.getAuthToken().orElse(null));
            taiKhoan.setRefreshToken(refreshToken.get());
            return taiKhoan;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static boolean introspectToken() throws IOException {
        Map<String, Object> response = APIHelper.postForMap(AppConfig.BASE_URL + "/auth/introspect", Map.of());
        Number status = (Number) response.get("status");
        if (status == null || status.intValue() < 200 || status.intValue() >= 300) {
            return false;
        }
        Object result = response.get("result");
        return result instanceof Boolean && (Boolean) result;
    }

    private static String refreshAccessToken(String refreshToken) throws IOException {
        String encoded = URLEncoder.encode(refreshToken, StandardCharsets.UTF_8);
        Map<String, Object> response = APIHelper.postForMap(AppConfig.BASE_URL + "/auth/refresh-token?refreshToken=" + encoded, Map.of());
        Number status = (Number) response.get("status");
        if (status == null || status.intValue() < 200 || status.intValue() >= 300) {
            return null;
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) response.get("result");
        if (result == null) {
            return null;
        }
        String token = (String) result.get("token");
        String newRefresh = (String) result.get("refreshToken");
        String savedRole = TokenStore.loadRole().orElse(null);
        String savedProfileId = TokenStore.loadProfileId().orElse(null);
        String savedUserName = TokenStore.loadUserName().orElse(null);
        if (newRefresh != null && !newRefresh.isBlank()) {
            TokenStore.saveAuthState(token, newRefresh, savedRole, savedProfileId, savedUserName);
        } else {
            TokenStore.saveAuthState(token, refreshToken, savedRole, savedProfileId, savedUserName);
        }
        return token;
    }

    private static class DangNhapBSLAdapter {
        static Map<String, Object> fetchProfileInfo() {
            try {
                Map<String, Object> userResponse = APIHelper.getForMap(AppConfig.BASE_URL + "/customer/myInfo");
                Number status = (Number) userResponse.get("status");
                if (status != null && status.intValue() >= 200 && status.intValue() < 300) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> result = (Map<String, Object>) userResponse.get("result");
                    if (result != null) {
                        return Map.of(
                                "role", "CUSTOMER",
                                "profileId", result.get("customerId"),
                                "userName", result.get("phoneNumber")
                        );
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            try {
                Map<String, Object> driverResponse = APIHelper.getForMap(AppConfig.BASE_URL + "/drivers/my-info");
                Number status = (Number) driverResponse.get("status");
                if (status != null && status.intValue() >= 200 && status.intValue() < 300) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> result = (Map<String, Object>) driverResponse.get("result");
                    if (result != null) {
                        return Map.of(
                                "role", "DRIVER",
                                "profileId", result.get("driverId"),
                                "userName", result.get("phoneNumber")
                        );
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return null;
        }
    }
}
