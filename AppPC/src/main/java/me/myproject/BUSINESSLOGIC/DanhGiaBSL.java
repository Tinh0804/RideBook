package me.myproject.BUSINESSLOGIC;

import me.myproject.Utilities.APIHelper;
import me.myproject.Utilities.AppConfig;

import java.util.HashMap;
import java.util.Map;

public class DanhGiaBSL {

    public Map<String, Object> guiDanhGia(String bookingId, double score, String review) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("bookingId", bookingId);
            data.put("score", score);
            data.put("review", review);

            return APIHelper.postForMap(AppConfig.BASE_URL + "/ratings", data);
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("status", 500, "message", "Không thể gửi đánh giá!");
        }
    }

    public Map<String, Object> layDanhGiaTaiXe(String driverId) {
        try {
            return APIHelper.getForMap(AppConfig.BASE_URL + "/ratings/driver/" + driverId);
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("status", 500, "message", "Không thể lấy danh sách đánh giá!");
        }
    }
}
