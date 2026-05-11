package me.myproject.BUSINESSLOGIC;

import java.io.IOException;
import java.util.Map;

import me.myproject.Utilities.APIHelper;
import me.myproject.Utilities.AppConfig;

public class DriverChuyenDiBSL {
    public Map<String, Object> layThongTinChuyen(String bookingId) throws IOException {
        return APIHelper.getForMap(AppConfig.BASE_URL + "/bookings/" + bookingId);
    }

    public Map<String, Object> nhanChuyen(String bookingId, String driverId) throws IOException {
        return APIHelper.putForMap(AppConfig.BASE_URL + "/bookings/" + bookingId + "/assign-driver?driverId=" + driverId, "");
    }
    public Map<String, Object> capNhatTrangThaiChuyen(String bookingId, String status) {
        try {
            return APIHelper.putForMap(AppConfig.BASE_URL + "/bookings/" + bookingId + "/status?status=" + status, "");
        } catch (IOException e) {
            e.printStackTrace();
            return Map.of("status", 500, "message", "Lỗi khi cập nhật trạng thái chuyến đi!");
        }
    }
}
