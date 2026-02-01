package controller.Driver;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.ChuyenXeService;

@WebServlet(urlPatterns = {"/api/driver/kiemTraNhanChuyen"})
public class API_KiemTraNhanChuyen extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private ObjectMapper mapper=new ObjectMapper();
	private ChuyenXeService chuyenXeService=new ChuyenXeService();
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("application/json");
		
		try {
			String idTX=req.getParameter("idTX");
			
			mapper.writeValue(resp.getWriter(), Map.of(
					"status",200,
					"data",chuyenXeService.kiemTraNhanChuyen(idTX)
					));
		} catch (Exception e) {
			mapper.writeValue(resp.getWriter(), Map.of(
					"status",400,
					"data",null
					));
		}
	}
}
