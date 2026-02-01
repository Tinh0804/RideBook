package controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.JSONObject;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.DatXe;
import service.ChuyenXeService;
import utils.JsonUtil;

@WebServlet(urlPatterns = {"/api-rides", "/api-rides/available"}) // Cập nhật để bao gồm /api-rides/available
public class ChuyenXeController extends HttpServlet {
    private static final long serialVersionUID = -1137823512321937256L;
    private final ChuyenXeService chuyenXeService = new ChuyenXeService();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String pathInfo = req.getServletPath();
        try {
        	if (pathInfo != null && "/api-rides/available".equals(pathInfo)) {
        		String diaChiTX=req.getParameter("diaChiTX");
        	    List<DatXe> availableRides = chuyenXeService.getAvailableRides(diaChiTX);
        	    System.out.println(diaChiTX);
        	    List<Map<String, Object>> responseData = availableRides.stream().map(datXe -> {
        	        Map<String, Object> map = new HashMap<>();
        	        map.put("id", datXe.getID_DatXe());
        	        map.put("customerId", datXe.getID_KhachHang());
        	        map.put("pickup", datXe.getDiemDon());
        	        map.put("dropoff", datXe.getDiemTra());
        	        map.put("distance", datXe.getKhoangCach());
        	        map.put("status", datXe.getTrangThai());
        	        map.put("driverId", datXe.getID_TaiXe());
        	        map.put("price",datXe.getGiaTien());
        	        return map;
        	    }).collect(Collectors.toList());
        	    mapper.writeValue(resp.getOutputStream(), Map.of("status", 200, "data", responseData));
        	} else {
        		System.out.println("vo day 2");
                List<DatXe> list = chuyenXeService.getAllRides();
                List<Map<String, Object>> responseData = list.stream().map(datXe -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", datXe.getID_DatXe());
                    map.put("customerId", datXe.getID_KhachHang());
                    map.put("pickup", datXe.getDiemDon());
                    map.put("dropoff", datXe.getDiemTra());
                    map.put("distance", datXe.getKhoangCach());
                    map.put("status", datXe.getTrangThai());
                    map.put("driverId", datXe.getID_TaiXe());
                    map.put("price",datXe.getGiaTien());
                    return map;
                }).collect(Collectors.toList());
                mapper.writeValue(resp.getOutputStream(), Map.of("status", 200, "data", responseData));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            mapper.writeValue(resp.getOutputStream(), Map.of("status", 500, "error", "Lỗi khi lấy danh sách chuyến xe: " + e.getMessage()));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        String jsonInput = req.getReader().lines().collect(Collectors.joining());
        JSONObject jsonObject = new JSONObject(jsonInput);
        DatXe datXe = JsonUtil.fromJson(jsonInput, DatXe.class);
        String loaiXe = jsonObject.optString("loaiXe");
        System.out.print(datXe.getID_KhuyenMai());
        jsonObject.remove("loaiXe");
        try {
            chuyenXeService.bookRide(datXe, loaiXe);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            mapper.writeValue(resp.getOutputStream(), Map.of("message", "Đã đặt xe thành công!"));
        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            mapper.writeValue(resp.getOutputStream(), Map.of("error", "Đã xảy ra lỗi khi đặt xe: " + e.getMessage()));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        StringBuilder bodyBuilder = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                bodyBuilder.append(line);
            }
        }
        String bodyString = bodyBuilder.toString();

        JsonObject body = JsonUtil.fromJson(bodyString, JsonObject.class);
        String idChuyenXe = body.get("ID_ChuyenXe").getAsString();
        String idTaiXe = body.get("ID_TX").getAsString();
        String ghiChu = body.has("ghiChu") ? body.get("ghiChu").getAsString() : null;
        String message = "Lỗi. Vui lòng thử lại sau!";
        int status = 400;

        try {
            message = chuyenXeService.ganTaiXeNhanDon(idChuyenXe, idTaiXe);
            status = 200;
        } catch (SQLException e) {
            message = "Lỗi khi gán tài xế: " + e.getMessage();
            e.printStackTrace();
        } catch (Exception e) {
            message = "Lỗi không xác định: " + e.getMessage();
            e.printStackTrace();
        }

        Map<String, Object> response = Map.of(
                "status", status,
                "message", message
        );
        mapper.writeValue(resp.getOutputStream(), response);
        resp.setStatus(status);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        String jsonInput = req.getReader().lines().collect(Collectors.joining());
        Map<String, String> data = mapper.readValue(jsonInput, new TypeReference<Map<String, String>>() {});
        chuyenXeService.huyChuyenXe(data.get("ID_CHUYENXE"));

        mapper.writeValue(resp.getOutputStream(), Map.of("status", 200, "message", "Huỷ chuyến thành công"));
    }

}