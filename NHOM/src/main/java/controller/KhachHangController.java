package controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.JSONObject;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.KhachHang;
import service.KhachHangService;
import utils.JsonUtil;

@WebServlet("/api-users")
public class KhachHangController extends HttpServlet {
    private static final long serialVersionUID = 1L;
	private final KhachHangService khachHangService = new KhachHangService();
	ObjectMapper mapper=new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
	    
    	resp.setContentType("application/json");
        String jsonInput = req.getReader().lines().collect(Collectors.joining());

        JSONObject jsonObject = new JSONObject(jsonInput);

        // Lấy mật khẩu
        String matkhau = jsonObject.optString("matKhau");

        jsonObject.remove("matKhau");
        KhachHang khachHang = JsonUtil.fromJson(jsonObject.toString(), KhachHang.class);
        HashMap<String,Object> map=new HashMap<>();
       try {
    	    map= khachHangService.dangKyUser(khachHang, matkhau);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
        
        
        resp.setStatus(HttpServletResponse.SC_CREATED);
        mapper.writeValue(resp.getWriter(), map);
        
        
    }
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    	resp.setContentType("application/json");
    	String jsonInput = req.getReader().lines().collect(Collectors.joining());
    	 JSONObject jsonObject = new JSONObject(jsonInput);

         // Lấy mật khẩu
         String matkhau = jsonObject.optString("matkhau");

         jsonObject.remove("matkhau");
        KhachHang khachHang = JsonUtil.fromJson(jsonInput, KhachHang.class);
        HashMap<String, Object> map=new HashMap<>();
        try {
			map=khachHangService.capNhatKhachHang(khachHang,matkhau);
		} catch (SQLException e) {
			e.printStackTrace();
		}
        resp.setStatus(HttpServletResponse.SC_CREATED);
       
        mapper.writeValue(resp.getWriter(), map);
    }
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    	resp.setContentType("application/json");
    	ObjectMapper mapper=new ObjectMapper();
    	
    	String id = req.getParameter("ID_KH");
    	khachHangService.deleteUser(id);
    	mapper.writeValue(resp.getOutputStream(), Map.of(
    			"status",200,
    			"message","Đã xoá thành công"
    			));
    	
    }
}