package controller.Admin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.KhachHangService;

@WebServlet(urlPatterns = {"/api/admin/thongTin/khachHang"})
public class API_QuanLy_TTKhachHang extends HttpServlet{

	private static final long serialVersionUID = 1L;
	private final KhachHangService khachHangService=new KhachHangService();
	private final ObjectMapper mapper=new ObjectMapper();
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("application/json");
		
		try {
			int tongKH=khachHangService.selectAllUser().size();
			int khMoi=khachHangService.khachHangMoi();
			double danhGiaTB=khachHangService.danhGiaTrungBinh();
			double tyLeHaiLong=khachHangService.tyLeHaiLongChuyen();
			HashMap<String, Object> map=new HashMap<>();
			map.put("tongKH",tongKH);
			map.put("khMoi", khMoi);
			map.put("danhGiaTB", danhGiaTB);
			map.put("tyLeHaiLong", tyLeHaiLong);
			
			mapper.writeValue(resp.getWriter(), Map.of(
					
					"status",200,
					"message","Lấy dữ liệu thành công!",
					"data",map
					));
		} catch (Exception e) {
					mapper.writeValue(resp.getWriter(), Map.of(
										
										"status",400,
										"message","Lỗi.Vui lòng thử lại!",
										"data",null
										));
		}
	}
	
}
