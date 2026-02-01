package controller.Admin;

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
import service.KhachHangService;
import service.TaiKhoanService;
import utils.JsonUtil;

@WebServlet(urlPatterns = {"/api/admin/manager/user/status"})
public class API_User_Status extends HttpServlet{

	private static final long serialVersionUID = 1L;
	private final KhachHangService khachHangService=new KhachHangService();
	private final TaiKhoanService taiKhoanService=new TaiKhoanService();
	private ObjectMapper mapper=new ObjectMapper();
	
	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("application/json");
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
		    String idKH = body.get("id_khachHang").getAsString(); 
		    boolean trangThai=body.get("trangThai").getAsBoolean();
		    
		    HashMap<String, String> map=taiKhoanService.mo_KhoaTaiKhoan(idKH, trangThai);
		    
		    mapper.writeValue(resp.getWriter(), map);
		    
		    
		} catch (Exception e) {
			 mapper.writeValue(resp.getWriter(), Map.of(
					 "status",400,
					 "message","Cập nhật trạng thái tài khoản khách hàng không thành công!"
					 ));
		}
	}
	
}