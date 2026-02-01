package controller.Driver;

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

@WebServlet(urlPatterns = {"/api/driver/hoanThanhChuyenXe"})
public class API_HoanThanhChuyenXe extends HttpServlet {
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
                while ((line = reader.readLine()) != null) 
                    bodyBuilder.append(line);
            }
            String bodyString = bodyBuilder.toString();
            JsonObject body = JsonUtil.fromJson(bodyString, JsonObject.class);
            String id = body.get("id_chuyenXe").getAsString() ;
            String message = chuyenXeService.doiTrangThai(id.trim(), "Hoàn thành");
            resp.setStatus(HttpServletResponse.SC_OK);
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", message);
            mapper.writeValue(resp.getWriter(), response);
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Không thể hoàn thành chuyến xe: " + e.getMessage());
            mapper.writeValue(resp.getWriter(), error);
        }
        resp.getWriter().flush();
    }
}