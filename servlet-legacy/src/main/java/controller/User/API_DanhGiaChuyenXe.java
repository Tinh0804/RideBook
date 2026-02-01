package controller.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.ChuyenXeService;
import utils.JsonUtil;

@WebServlet(urlPatterns = {"/api/user/danhGiaChuyenXe"})
public class API_DanhGiaChuyenXe extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final ChuyenXeService chuyenXeService = new ChuyenXeService();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            StringBuilder bodyBuilder = new StringBuilder();
            try (BufferedReader reader = req.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    bodyBuilder.append(line);
                }
            }
            String bodyString = bodyBuilder.toString();
            JsonObject body = JsonUtil.fromJson(bodyString, JsonObject.class);
            String id = body.get("id_chuyenXe").getAsString().trim();
            String danhGia = body.get("danhGia").getAsString().trim();
            int diem = body.get("diem").getAsInt();
            int rowsAffected = chuyenXeService.capNhatDanhGia(id, danhGia, diem);
            Map<String, Object> response = new HashMap<>();
            if (rowsAffected > 0) {
                resp.setStatus(HttpServletResponse.SC_OK);
                response.put("status", "success");
                response.put("message", "Đánh giá chuyến xe thành công.");
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.put("status", "error");
                response.put("message", "Không tìm thấy chuyến xe với ID: " + id);
            }
            mapper.writeValue(resp.getWriter(), response);

        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            mapper.writeValue(resp.getWriter(), error);
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Lỗi server: " + e.getMessage());
            mapper.writeValue(resp.getWriter(), error);
        }
    }
}