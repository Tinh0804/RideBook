package controller.Admin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.TaiKhoanService;
import utils.JsonUtil;

@WebServlet(urlPatterns = {"/api/admin/driver/status"})
public class API_TaiXe_Status extends HttpServlet {
	private TaiKhoanService taiKhoanService=new TaiKhoanService();
	private static final long serialVersionUID = 1L;
	ObjectMapper mapper=new ObjectMapper();
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        
        
	}
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
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
	    String action = body.get("action").getAsString(); // "lock" or "unlock"
	    String id = body.get("id").getAsString();

	    Boolean trangThai = action.equals("lock") ? false : true;

	    try {
	        HashMap<String, String> map = taiKhoanService.mo_KhoaTaiKhoan(id, trangThai);
	        mapper.writeValue(resp.getOutputStream(), Map.of(
	            "status", map.get("status"),
	            "message", map.get("message")
	        ));
	    } catch (Exception e) {
	        e.printStackTrace();
	        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	        mapper.writeValue(resp.getOutputStream(), Map.of(
	            "status", "Error",
	            "message", "Lỗi hệ thống khi xử lý yêu cầu."
	        ));
	    }
	}

}
