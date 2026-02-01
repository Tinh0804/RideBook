package controller.User;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.LoaiXe;
import model.TaiXe;
import service.ChuyenXeService;
import service.KhachHangService;
import service.LoaiXeService;
import service.TaiXeService;

@WebServlet(urlPatterns = {"/api/user/trangChu"})
public class API_TrangChu extends HttpServlet{

	private static final long serialVersionUID = 1L;
	private final KhachHangService khachHangService=new KhachHangService();
	private final TaiXeService taiXeService=new TaiXeService();
	private final LoaiXeService loaiXeService=new LoaiXeService();
	private ChuyenXeService chuyenXeService=new ChuyenXeService();
	private ObjectMapper mapper=new ObjectMapper();
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("application/json");
		resp.setCharacterEncoding("UTF-8");
		
		try {
			HashMap<String, Object> map=new HashMap<>();
			map.put("tongKH",khachHangService.selectAllUser().size());
			map.put("tongTX", taiXeService.selectAllTaiXe().size());
			map.put("tongTP", 3);
			map.put("dsLoaiXe", loaiXeService.selectAll());
			map.put("chuyenXeThangNay",chuyenXeService.getAllRides().size());
			map.put("danhGiaTB", khachHangService.danhGiaTrungBinh());
			map.put("dsTaiXeTieuBieu",taiXeService.dsTaiXeTieuBieu());
			
			
			mapper.writeValue(resp.getWriter(), Map.of(
					"status",200,
					"message","Lấy dữ liệu thành công",
					"data",map
					));
			
		} catch (Exception e) {
			mapper.writeValue(resp.getWriter(), Map.of(
					"status",400,
					"message","Lỗi khi lấy dữ liệu:"+e.getMessage(),
					"data",null
					));
		}
	}
	
}