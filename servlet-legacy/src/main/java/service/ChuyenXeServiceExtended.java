package service;

import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import model.DatXe;

public class ChuyenXeServiceExtended extends ChuyenXeService {
    private final ObjectMapper mapper = new ObjectMapper();

    public void bookRideWithNearestDriver(DatXe datXe, String loaiXe) throws Exception {
        // Gọi API_DSTaiXeGanKhachHang nội bộ
        URL url = new URL("http://localhost:8080/api/driver/dsGanKhachHang?diaChiKH=" + datXe.getDiemDon());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type", "application/json");

        if (conn.getResponseCode() != 200) {
            throw new Exception("Lỗi khi lấy danh sách tài xế!");
        }

        Map<String, Object> apiResponse = mapper.readValue(conn.getInputStream(), Map.class);
        List<Map<String, Object>> dsTaiXe = (List<Map<String, Object>>) apiResponse.get("data");

        if (dsTaiXe.isEmpty()) {
            throw new Exception("Không tìm thấy tài xế phù hợp gần khu vực: " + datXe.getDiemDon());
        }

        // Lấy tài xế đầu tiên (gần nhất)
        Map<String, Object> nearestDriver = dsTaiXe.get(0);
        String idTaiXe = (String) nearestDriver.get("idTaiXe");

        // Gán tài xế và trạng thái
        datXe.setID_TaiXe(idTaiXe);
        datXe.setTrangThai("Assigned");
        if (datXe.getThoiGianDat() == null) {
            datXe.setThoiGianDat(new Timestamp(System.currentTimeMillis()));
        }

        // Gọi bookRide gốc để lưu chuyến xe
        super.bookRide(datXe, loaiXe);
    }
}