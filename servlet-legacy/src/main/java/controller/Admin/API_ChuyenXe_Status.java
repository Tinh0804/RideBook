package controller.Admin;

import java.io.BufferedReader;
import java.io.IOException;
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

@WebServlet(urlPatterns = {"/api/admin/datXe/status"})
public class API_ChuyenXe_Status extends HttpServlet{

	private static final long serialVersionUID = 1L;
	private final ChuyenXeService chuyenXeService=new ChuyenXeService();
//	private final ObjectMapper mapper=new ObjectMapper();
	
	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("application/json");
		resp.setCharacterEncoding("UTF-8");
		
		 StringBuilder bodyBuilder = new StringBuilder();
		    try (BufferedReader reader = req.getReader()) {
		        String line;
		        while ((line = reader.readLine()) != null) {
		            bodyBuilder.append(line);
		        }
		    }
		    String bodyString = bodyBuilder.toString();

	    JsonObject body = JsonUtil.fromJson(bodyString, JsonObject.class);
	    String id = body.get("id_chuyenXe").getAsString();
	    String trangThai = body.get("trangThai").getAsString(); 
	    String ghiChu = body.get("ghiChu").getAsString();
	  
		try {
			String message=chuyenXeService.doiTrangThai(id,trangThai);
			resp.getWriter().write(message);
		} catch (Exception e) {
			resp.getWriter().write(e.getMessage());
		}
		resp.getWriter().flush();
	}
	
}