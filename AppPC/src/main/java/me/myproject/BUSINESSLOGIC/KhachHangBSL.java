package me.myproject.BUSINESSLOGIC;

import java.io.IOException;
import java.util.Map;

import me.myproject.Utilities.APIHelper;
import me.myproject.Utilities.AppConfig;

public class KhachHangBSL {
      public Map<String, Object> layThongTinKhachHang() throws IOException {
        return APIHelper.getForMap(AppConfig.BASE_URL + "/customers/" + "my-info");
    }
}
