package controller.Admin;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.KhachHang;
import service.KhachHangService;
import utils.JsonUtil;

@WebServlet(urlPatterns = {"/api/admin/quanLy/khachHang"})
public class API_QuanLy_KhachHang extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private final ObjectMapper mapper=new ObjectMapper();
	private final KhachHangService khachHangService=new KhachHangService();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("application/json");
		
		try {
			List<Map<String,Object>> list=khachHangService.dsChiTietKhachHang();
			
			mapper.writeValue(resp.getWriter(), Map.of(
					"status",200,
					"message","Lấy dữ liệu thành công!",
					"data",list
					));
		} catch (Exception e) {
			mapper.writeValue(resp.getWriter(), Map.of(
					"status",400,
					"message","Lỗi.Vui lòng thử lại!"
				
					));
		}
	}
	
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
		    
		    String idKH = body.get("id_khachHang").getAsString(); 
		    String tenKH=body.get("tenKhachHang").getAsString();
		    String sdt=body.get("sdt").getAsString();
		    String diaChi=body.get("diaChi").getAsString();
		    String pass=body.get("matKhau").getAsString();
		    KhachHang KH=new KhachHang(idKH, tenKH, sdt, diaChi);
		    HashMap<String,Object> map=khachHangService.capNhatKhachHang(KH,pass);
		    
		    mapper.writeValue(resp.getWriter(), map);
		} catch (Exception e) {
			mapper.writeValue(resp.getWriter(), Map.of(
					"status",400,
					"message","Lỗi.Vui lòng thử lại!"
				
					));
		}
	}
	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("application/json");
		
		try {
			String idKh=req.getParameter("idKH");
			
			if (idKh!=null) {
				khachHangService.deleteUser(idKh);
				mapper.writeValue(resp.getWriter(), Map.of(
						"status",200,
						"message","Đã xoá thành công khách hàng "+idKh+"!"
						));
			}
			else {
				mapper.writeValue(resp.getWriter(), Map.of(
						"status",400,
						"message","Mã khách hàng không hợp lệ!"
						));
			}
			
			
		} catch (Exception e) {
			mapper.writeValue(resp.getWriter(), Map.of(
					"status",500,
					"message","Lỗi.Vui lòng thử lại!",
					"data",null
					));
		}
	}
	
}
