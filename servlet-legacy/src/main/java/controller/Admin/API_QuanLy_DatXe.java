package controller.Admin;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.ChuyenXeService;

@WebServlet(urlPatterns = {"/api/admin/quanly/chuyenXe"})
public class API_QuanLy_DatXe extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	private final ObjectMapper mapper=new ObjectMapper();
	private final ChuyenXeService chuyenXeService=new ChuyenXeService();
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("application/json");
		resp.setCharacterEncoding("UTF-8");
		HashMap<String, Object> map=new HashMap<>();
		try {
			map.put("chuyenDangCho", chuyenXeService.soChuyenXeTheoTrangThai("Chờ tài xế nhận"));
			map.put("chuyenDangChay", chuyenXeService.soChuyenXeTheoTrangThai("Đang thực hiện"));
			map.put("chuyenDaHoanThanh", chuyenXeService.soChuyenXeTheoTrangThai("Hoàn thành"));
			map.put("chuyenDaHuy", chuyenXeService.soChuyenXeTheoTrangThai("Đã huỷ"));
			map.put("danhSachChuyenXe", chuyenXeService.chuyenXeChiTiet());
			
			mapper.writeValue(resp.getOutputStream(), Map.of(
					"status",200,
					"data",map,
					"message","Load dữ liệu thành công!"
					));
		} catch (Exception e) {
			mapper.writeValue(resp.getOutputStream(), Map.of(
					"status",400,
					"message","Lỗi dữ liệu.Vui lòng thử lại!"
					));
		}
	}
	
}