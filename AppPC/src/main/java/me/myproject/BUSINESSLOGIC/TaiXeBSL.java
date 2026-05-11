package me.myproject.BUSINESSLOGIC;

import java.io.IOException;
import java.util.Map;

import me.myproject.Utilities.APIHelper;
import me.myproject.Utilities.AppConfig;

public class TaiXeBSL {
    public Map<String, Object> layThongTinTaiXe() throws IOException {
        return APIHelper.getForMap(AppConfig.BASE_URL + "/drivers/" + "my-info");
    }

    public Map<String, Object> layThongKeTaiXe() throws IOException {
        return APIHelper.getForMap(AppConfig.BASE_URL + "/drivers/my-dashboard");
    }

    public Map<String, Object> doiTrangThaiHoatDong() throws IOException {
        // Tạo đối tượng chứa thông tin mới
        return APIHelper.putForMap(AppConfig.BASE_URL + "/drivers/" + "status-activity", null);
    }

    public Map<String, Object> layBaoCaoDoanhThu() throws IOException {
       return APIHelper.getForMap(AppConfig.BASE_URL + "/drivers/my-revenue");
    }
}
