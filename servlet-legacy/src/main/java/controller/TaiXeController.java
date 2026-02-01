package controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import model.TaiXe;
import service.TaiKhoanService;
import service.TaiXeService;
import utils.JsonUtil;

@WebServlet(urlPatterns = {"/api-driver"})
@MultipartConfig(
	    fileSizeThreshold = 1024 * 1024,  // 1MB
	    maxFileSize = 1024 * 1024 * 10,   // 10MB
	    maxRequestSize = 1024 * 1024 * 50 // 50MB
	)
public class TaiXeController extends HttpServlet{
	
	private static final long serialVersionUID = -5468554703941755526L;
	private final TaiXeService taiXeService=new TaiXeService();
	private final TaiKhoanService taiKhoanService=new TaiKhoanService();
	private final ObjectMapper mappper=new ObjectMapper();
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		    
		resp.setContentType("application/json");
		resp.setCharacterEncoding("UTF-8");
		
		try {
			List<TaiXe> list=taiXeService.selectAllTaiXe();
	        List<Map<String, Object>> ds = list.stream().map(tx -> {
	            Map<String, Object> map = mappper.convertValue(tx, new TypeReference<Map<String, Object>>() {});
	            try {
					map.put("trangThaiTK",taiKhoanService.kiemTraTrangThai(tx.getSDT(), "DRIVER"));
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} // Thêm cột dữ liệu mới
	            return map;
	        }).collect(Collectors.toList());
			mappper.writeValue(resp.getOutputStream(), Map.of(
					"status",200,
					"data",ds
					));
		} catch (Exception e) {
			mappper.writeValue(resp.getOutputStream(), Map.of(
					"status",400,
					"data","Lỗi.Không load được dữ liệu!"
					));
		}
	}
	@Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String action=req.getParameter("action");
		try {
		    Part jsonPart = req.getPart("data");
		    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(jsonPart.getInputStream()));
		    String jsonString = bufferedReader.lines().collect(Collectors.joining("\n"));
		    String matKhau=req.getParameter("passWord");
		    
		    TaiXe taiXe = JsonUtil.fromJson(jsonString, TaiXe.class);
		    System.out.println(jsonString + jsonPart);
		    HashMap<String, String> result  =new HashMap<>();
		    if("create".equals(action)) {
		    	result= taiXeService.themTaiXe(req, taiXe,matKhau);
		    	
		    }
		    else {
		    	System.out.println(taiXe.getID_TaiXe());
				result=taiXeService.updateTaiXe(req, taiXe);
			}
		    System.out.println(result.get("Message"));
		    resp.setContentType("application/json");
		    resp.setCharacterEncoding("UTF-8");
		    mappper.writeValue(resp.getOutputStream(), result);

		} catch (Exception e) {
		    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		    resp.setContentType("application/json");
		    resp.setCharacterEncoding("UTF-8");

		    HashMap<String, String> errorMap = new HashMap<>();
		    errorMap.put("Status", "error");
		    errorMap.put("Message", e.getMessage());

		    mappper.writeValue(resp.getOutputStream(), errorMap);
		}

    }
	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		try {
		    Part jsonPart = req.getPart("data");
		    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(jsonPart.getInputStream()));
		    String jsonString = bufferedReader.lines().collect(Collectors.joining("\n"));
		    
		    TaiXe taiXe = JsonUtil.fromJson(jsonString, TaiXe.class);
		    System.out.println(jsonString + jsonPart);
		    HashMap<String, String> result = taiXeService.updateTaiXe(req, taiXe);
		    System.out.println(result.get("Message"));
		    resp.setContentType("application/json");
		    resp.setCharacterEncoding("UTF-8");
		    mappper.writeValue(resp.getOutputStream(), result);

		} catch (Exception e) {
		    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		    resp.setContentType("application/json");
		    resp.setCharacterEncoding("UTF-8");

		    HashMap<String, String> errorMap = new HashMap<>();
		    errorMap.put("Status", "error");
		    errorMap.put("Message", e.getMessage());

		    mappper.writeValue(resp.getOutputStream(), errorMap);
		}
	
	}
	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("application/json");
		resp.setCharacterEncoding("UTF-8");
		
		String jsonInput = req.getReader().lines().collect(Collectors.joining());
    	String idTX=req.getParameter("id");
    	    taiXeService.deleteTaiXe(idTX);
    	 
    	mappper.writeValue(resp.getOutputStream(), Map.of(
    			"ok",true,
    			"message","Đã xoá thành công"
    			));
	}

    
	
}
