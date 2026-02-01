package controller.User;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.DatXe;
import service.ChuyenXeService;

@WebServlet(urlPatterns = {"/api/user/dsChuyenXe"})
public class API_LichSuChuyenXe extends HttpServlet{

	private static final long serialVersionUID = 1L;
	private final ChuyenXeService chuyenXeService=new ChuyenXeService();
	private final ObjectMapper mapper=new ObjectMapper();
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("application/json");
		
		
		try {
//			String idKH=req.getParameter("idKH");
			List<DatXe> list=chuyenXeService.getAllRides();
			
			mapper.writeValue(resp.getWriter(), Map.of(
					"message","Thành công!",
					"status","success",
					"data",list
					));
			
		} catch (Exception e) {
			mapper.writeValue(resp.getWriter(), Map.of(
					"message","Lỗi.Vui lòng thử lại!",
					"status","error"
					
					));
		}
	}

}
