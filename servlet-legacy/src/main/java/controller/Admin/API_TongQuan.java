package controller.Admin;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dao.DatXeDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.ChuyenXeService;
@WebServlet(urlPatterns = {"/api/admin/tong-quan"})
public class API_TongQuan extends HttpServlet{
	private static final long serialVersionUID = 1L;
	private final ChuyenXeService service=new ChuyenXeService();
	private final ObjectMapper mapper=new ObjectMapper();
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("application/json");
		resp.setCharacterEncoding("UTF-8");


		int soDong = 5; // Giá trị mặc định
		LocalDate thoiGian = LocalDate.now(); // Giá trị mặc định

		try {
		    String soDongParam = req.getParameter("soDong");
		    String thoiGianParam = req.getParameter("thoiGian");
			System.out.println(soDongParam);
			System.out.println(thoiGianParam);
		    if (soDongParam != null && !soDongParam.isEmpty()) {
		        soDong = Integer.parseInt(soDongParam);
		    }

		    if (thoiGianParam != null && !thoiGianParam.isEmpty()) {
		        thoiGian = LocalDate.parse(thoiGianParam, DateTimeFormatter.ISO_LOCAL_DATE);
		    }

		    // Tiếp tục xử lý dữ liệu với soDong và thoiGian

		} catch (NumberFormatException | DateTimeParseException e) {
		    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		    resp.getWriter().write("{\"error\": \"Invalid parameter format: " + e.getMessage() + "\"}");
		    return;
		}

		Map<String, Object> map=new HashMap<>();
		try {
			map.put("tongKhachHang",service.tongKhachHang());
			map.put("tongTaiXe",service.tongTaiXe());
			map.put("tongChuyenXe",service.tongChuyenXe());
			map.put("tongSoChuyenTrongNgay", service.tongChuyenXeTrongNgay(thoiGian));
			map.put("tongSoChuyenTrongThang", service.tongChuyenXeTrongThang(thoiGian));
			map.put("tongDoanhThuTrongNgay",service.tongDoanhThuTrongNgay(thoiGian));
			map.put("tongDoanhThuTrongThang", service.tongDoanhThuTrongThang(thoiGian));
			map.put("chuyenXeGanDay", service.chuyenXeGanDay(soDong));
			resp.setStatus(200);
			mapper.writeValue(resp.getOutputStream(), Map.of(
					"status",200,
					"data",map
					
					));
			
		} catch (SQLException e) {
			resp.setStatus(500);
			e.printStackTrace();
			mapper.writeValue(resp.getOutputStream(), "Dữ liệu lỗi!");
		}
		
		
	}
}
