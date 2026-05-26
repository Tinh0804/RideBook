package me.myproject.BUSINESSLOGIC;

import me.myproject.Utilities.APIHelper;
import me.myproject.Utilities.AppConfig;

import java.util.Map;

public class ThongBaoBSL {

    public Map<String, Object> layDanhSachThongBao() {
        try {
            return APIHelper.getForMap(AppConfig.BASE_URL + "/notifications");
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("status", 500, "message", "Lỗi khi lấy thông báo!");
        }
    }

    public Map<String, Object> danhDauDaDoc(String idThongBao) {
        try {
            return APIHelper.putForMap(AppConfig.BASE_URL + "/notifications/" + idThongBao + "/read", null);
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("status", 500, "message", "Không thể cập nhật trạng thái thông báo!");
        }
    }
}
