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
import service.ChuyenXeService;
import utils.JsonUtil;

@WebServlet(urlPatterns = {"/api/admin/thongKe"})
public class API_ThongKe extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private final ChuyenXeService chuyenXeService=new ChuyenXeService();
	private ObjectMapper mapper=new ObjectMapper();
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("application/json");
		
		try {
			
		    String year = req.getParameter("year");
		   System.out.println(year);
			
		    HashMap<Integer, Integer> mapThongKeChuyenXe=new HashMap<>();
		    HashMap<String, Double> mapThongKeDoanhThu=new HashMap<>();
		    HashMap<String, String> mapThongKeChuyenTheoKhuVuc=new HashMap<>();
		    HashMap<String, Double> mapThongKeChuyenPTTT=new HashMap<>();
		    HashMap<String, String> tyLeDungKhuyenMai=new HashMap<>();
		    HashMap<String, String> mapThongKeTrangThaiChuyen=new HashMap<>();
			int status=400;
		    	try {
		    		mapThongKeChuyenXe=chuyenXeService.soChuyenXeTheoThang(year);
		    		mapThongKeChuyenTheoKhuVuc=chuyenXeService.tongChuyenXeTheoKhuVuc();
		    		mapThongKeDoanhThu=chuyenXeService.thongKeDoanhThu();
		    		tyLeDungKhuyenMai=chuyenXeService.thongKeTyLeDungKhuyenMai();
		    		mapThongKeTrangThaiChuyen=chuyenXeService.thongKeTrangThaiChuyen();
		    		mapThongKeChuyenPTTT=chuyenXeService.thongKePTTT(Integer.parseInt(year));
					status=200;
				} catch (Exception e) {
					status=400;
				}
		    	mapper.writeValue(resp.getOutputStream(), Map.of(
						"status",status,
						"thongKeChuyenXe",mapThongKeChuyenXe,
						"thongKeDoanhThu",mapThongKeDoanhThu,
						"thongKeChuyenTheoKhuVuc",mapThongKeChuyenTheoKhuVuc,
						"thongKeChuyenPTTT",mapThongKeChuyenPTTT,
						"thongKeTyLeDungKhuyenMai",tyLeDungKhuyenMai,
						"thongKeTrangThaiChuyen",mapThongKeTrangThaiChuyen
						
		    			));
			
		} catch (Exception e) {
		resp.getWriter().write("Lỗi.Vui lòng thử lại");
		}
	}

}
