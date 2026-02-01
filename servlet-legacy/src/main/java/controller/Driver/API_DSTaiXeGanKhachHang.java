package controller.Driver;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.TaiXe;
import service.TaiXeService;

@WebServlet(urlPatterns = {"/api/driver/dsGanKhachHang"})
public class API_DSTaiXeGanKhachHang extends HttpServlet{

	private static final long serialVersionUID = 1L;
	private final ObjectMapper mapper=new ObjectMapper();
	private final TaiXeService taiXeService=new TaiXeService();
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("application/json");
		resp.setCharacterEncoding("UTF-8");
		String diaChiKH=req.getParameter("diaChiKH");
		try {
			int khoangCach=10;
			List<TaiXe> ds=taiXeService.dsTaiXeGanKhachHang(khoangCach, diaChiKH);
			
			mapper.writeValue(resp.getOutputStream(), Map.of(
					"status",200,
					"message","Lấy dữ liệu thành công!",
					"data",ds
					));
		} catch (Exception e) {
			mapper.writeValue(resp.getOutputStream(), Map.of(
					"status",400,
					"message",e.getMessage()
				
					));
		}
	}
	
}