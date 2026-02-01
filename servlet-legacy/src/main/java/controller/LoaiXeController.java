package controller;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.LoaiXe;
import service.LoaiXeService;

@WebServlet(urlPatterns = {"/api-loaiXe"})
public class LoaiXeController extends HttpServlet{
	private static final long serialVersionUID = 1L;
	private ObjectMapper mapper=new ObjectMapper();
	private LoaiXeService loaiXeService=new LoaiXeService();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setCharacterEncoding("UTF-8");
		resp.setContentType("application/json");
		
		List<LoaiXe> list = loaiXeService.selectAll();
		mapper.writeValue(resp.getOutputStream(), list);
	}
}
