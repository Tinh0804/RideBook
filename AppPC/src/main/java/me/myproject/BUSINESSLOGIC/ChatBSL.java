package me.myproject.BUSINESSLOGIC;

import me.myproject.Utilities.APIHelper;
import me.myproject.Utilities.AppConfig;

import java.util.Map;

public class ChatBSL {

    public Map<String, Object> guiTinNhan(Map<String, Object> data) {
        try {
            return APIHelper.postForMap(AppConfig.BASE_URL + "/chats/send", data);
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("status", 500, "message", "Không thể gửi tin nhắn!");
        }
    }

    public Map<String, Object> layLichSuTinNhan(String bookingId) {
        try {
            return APIHelper.getForMap(AppConfig.BASE_URL + "/chats/" + bookingId);
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("status", 500, "message", "Lỗi khi tải dữ liệu trò chuyện!");
        }
    }
}
