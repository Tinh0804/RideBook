package service;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import dao.TaiXeDAO;
import dao.Exception.TaiXeException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import model.TaiXe;
import utils.DistanceCalculator;
import utils.FileUtils;

public class TaiXeService {
    private TaiXeDAO taiXeDAO = new TaiXeDAO();
    public static String layThanhPho(String diaChi) {
        if (diaChi == null || !diaChi.contains(",")) {
            return "";
        }

        String[] parts = diaChi.split(",");
        return parts[parts.length - 1].trim();
    }
    public HashMap<String, String> themTaiXe(HttpServletRequest request, TaiXe taiXe,String matKhau) throws SQLException, TaiXeException, IOException, ServletException {
        String id = taiXeDAO.idTuDongtangTaiXe();

        // Lấy Part từ request
        Part avatarPart = request.getPart("AVATAR");
        Part cccdPart = request.getPart("CCCD");
        Part gplxPart = request.getPart("GPLX");

        // Kiểm tra Part có dữ liệu không
        if (avatarPart == null || avatarPart.getSize() == 0) {
            throw new IOException("Chưa có ảnh đại diện");
        }
        if (cccdPart == null || cccdPart.getSize() == 0) {
            throw new IOException("Chưa có CCCD");
        }
        if (gplxPart == null || gplxPart.getSize() == 0) {
            throw new IOException("Chưa có GPLX");
        }

        // Lấy đuôi file
        String avatarExt = getFileExtension(avatarPart);
        String cccdExt = getFileExtension(cccdPart);
        String gplxExt = getFileExtension(gplxPart);

        String baseAvartar = "AVATAR_" + id; // AVATAR_TX004
        String baseCCCD = "CCCD_" + id;      // CCCD_TX004
        String baseGPLX = "GPLX_" + id;      // GPLX_TX004

        // Lưu file vào server
        FileUtils.saveFile(avatarPart, baseAvartar, request);
        FileUtils.saveFile(cccdPart, baseCCCD, request);
        FileUtils.saveFile(gplxPart, baseGPLX, request);

        // Tạo tên file đầy đủ (bao gồm đuôi) cho database
        String pathAvartar = baseAvartar + avatarExt; 
        String pathCCCD = baseCCCD + cccdExt;        
        String pathGPLX = baseGPLX + gplxExt;        

        // Lưu đường dẫn tương đối vào đối tượng TaiXe
        taiXe.setAnhDaiDien("/uploads/" + pathAvartar); // /uploads/AVATAR_TX004.png
        taiXe.setCCCD("/uploads/" + pathCCCD);          // /uploads/CCCD_TX004.png
        taiXe.setGPLX("/uploads/" + pathGPLX);          // /uploads/GPLX_TX004.png

        // Lưu vào database
        ResultSet result = taiXeDAO.themTaiXe_Pr(taiXe,matKhau);
        HashMap<String, String> map = new HashMap<>();
        while (result.next()) {
            map.put("Status", result.getString("Status"));
            map.put("Message", result.getString("Message"));
        }
        return map;
    }
 // Hàm trích xuất đuôi file từ Part
    String getFileExtension(Part part) {
        String fileName = part.getSubmittedFileName();
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
        }
        return "";
    }
    public HashMap<String, String> updateTaiXe(HttpServletRequest request, TaiXe taiXe) throws SQLException, TaiXeException, IOException, ServletException {
        String id = taiXe.getID_TaiXe();
        if (id == null || id.isEmpty()) {
            throw new TaiXeException("ID tài xế không hợp lệ.");
        }

        // Lấy Part từ request
        Part avatarPart = request.getPart("AVATAR");
        Part cccdPart = request.getPart("CCCD");
        Part gplxPart = request.getPart("GPLX");
        Part lltpPart=request.getPart("LLTP");

        // Xử lý file ảnh (nếu có)
        String pathAvartar = taiXe.getAnhDaiDien(); // Giữ đường dẫn cũ
        String pathCCCD = taiXe.getCCCD();         // Giữ đường dẫn cũ
        String pathGPLX = taiXe.getGPLX();         // Giữ đường dẫn cũ
        String pathLLTP=taiXe.getLLTP();

        // Xử lý ảnh đại diện
        if (avatarPart != null && avatarPart.getSize() > 0) {
            // Xóa file cũ nếu tồn tại
            if (pathAvartar != null && !pathAvartar.isEmpty()) {
                String oldAvatarPath = request.getServletContext().getRealPath("") + File.separator + "uploads" + File.separator + pathAvartar.replace("/uploads/", "");
                File oldAvatarFile = new File(oldAvatarPath);
                if (oldAvatarFile.exists()) {
                    oldAvatarFile.delete();
                }
            }

            String avatarExt = getFileExtension(avatarPart);
            String baseAvartar = "AVATAR_" + id;
            FileUtils.saveFile(avatarPart, baseAvartar, request);
            pathAvartar = baseAvartar + avatarExt;
            taiXe.setAnhDaiDien("/uploads/" + pathAvartar);
        }

        // Xử lý CCCD
        if (cccdPart != null && cccdPart.getSize() > 0) {
            // Xóa file cũ nếu tồn tại
            if (pathCCCD != null && !pathCCCD.isEmpty()) {
                String oldCCCDPath = request.getServletContext().getRealPath("") + File.separator + "uploads" + File.separator + pathCCCD.replace("/uploads/", "");
                File oldCCCDFile = new File(oldCCCDPath);
                if (oldCCCDFile.exists()) {
                    oldCCCDFile.delete();
                }
            }

            String cccdExt = getFileExtension(cccdPart);
            String baseCCCD = "CCCD_" + id; // CCCD_TX004
            FileUtils.saveFile(cccdPart, baseCCCD, request);
            pathCCCD = baseCCCD + cccdExt; // CCCD_TX004.png
            taiXe.setCCCD("/uploads/" + pathCCCD); // /uploads/CCCD_TX004.png
        }

        // Xử lý GPLX
        if (gplxPart != null && gplxPart.getSize() > 0) {
            // Xóa file cũ nếu tồn tại
            if (pathGPLX != null && !pathGPLX.isEmpty()) {
                String oldGPLXPath = request.getServletContext().getRealPath("") + File.separator + "uploads" + File.separator + pathGPLX.replace("/uploads/", "");
                File oldGPLXFile = new File(oldGPLXPath);
                if (oldGPLXFile.exists()) {
                    oldGPLXFile.delete();
                }
            }

            String gplxExt = getFileExtension(gplxPart);
            String baseGPLX = "GPLX_" + id; // GPLX_TX004
            FileUtils.saveFile(gplxPart, baseGPLX, request);
            pathGPLX = baseGPLX + gplxExt; // GPLX_TX004.png
            taiXe.setGPLX("/uploads/" + pathGPLX); // /uploads/GPLX_TX004.png
        }
        // Xử lý GPLX
        if (lltpPart != null && lltpPart.getSize() > 0) {
            // Xóa file cũ nếu tồn tại
            if (pathLLTP != null && !pathLLTP.isEmpty()) {
                String oldLLTPPath = request.getServletContext().getRealPath("") + File.separator + "uploads" + File.separator + pathGPLX.replace("/uploads/", "");
                File oldLLTPFile = new File(oldLLTPPath);
                if (oldLLTPFile.exists()) {
                    oldLLTPFile.delete();
                }
            }

            String lltpExt = getFileExtension(lltpPart);
            String baseLLTP = "LLTP_" + id; // GPLX_TX004
            FileUtils.saveFile(lltpPart, baseLLTP, request);
            pathLLTP = baseLLTP + lltpExt; // GPLX_TX004.png
            taiXe.setGPLX("/uploads/" + pathLLTP); // /uploads/GPLX_TX004.png
        }

        // Lưu vào database
        ResultSet result = taiXeDAO.capNhatTaiXe_Pr(taiXe);
        HashMap<String, String> map = new HashMap<>();
        while (result.next()) {
            map.put("Status", result.getString("Status"));
            map.put("Message", result.getString("Message"));
        }
        return map;
    }
    public List<TaiXe> selectAllTaiXe() {
    	return taiXeDAO.selectAll();
    }
    public void deleteTaiXe(String id) {
    	taiXeDAO.delete(id);
    }
    public void handleDriverUpload(HttpServletRequest request, TaiXe driver) throws Exception {
        String id = taiXeDAO.idTuDongtangTaiXe();
        driver.setID_TaiXe(id);

        // Lấy Part từ request
        Part avatarPart = request.getPart("AVATAR");
        Part cccdPart = request.getPart("CCCD");
        Part gplxPart = request.getPart("GPLX");
        Part lltpPart=request.getPart("LLTP");

        // Tạo tên file với phần mở rộng
        String pathAvartar = "AVATAR_" + driver.getID_TaiXe() ;
        String pathCCCD = "CCCD_" + driver.getID_TaiXe() ;
        String pathGPLX = "GPLX_" + driver.getID_TaiXe();
        String pathLLTP="LLTP_"+driver.getID_TaiXe();

        // Kiểm tra Part có dữ liệu không
        if (avatarPart == null || avatarPart.getSize() == 0) {
            throw new IOException("AVATAR file is missing or empty");
        }
        if (cccdPart == null || cccdPart.getSize() == 0) {
            throw new IOException("CCCD file is missing or empty");
        }
        if (gplxPart == null || gplxPart.getSize() == 0) {
            throw new IOException("GPLX file is missing or empty");
        }
        if (lltpPart == null || lltpPart.getSize() == 0) {
            throw new IOException("LLTP file is missing or empty");
        }

        // Lưu file
        FileUtils.saveFile(avatarPart, pathAvartar, request);
        FileUtils.saveFile(cccdPart, pathCCCD, request);
        FileUtils.saveFile(gplxPart, pathGPLX, request);
        FileUtils.saveFile(lltpPart, pathLLTP, request);

        // Lưu đường dẫn tương đối vào đối tượng TaiXe
        driver.setCCCD("/uploads/" + pathCCCD);
        driver.setGPLX("/uploads/" + pathGPLX);
        // Nếu AVATAR cũng cần lưu đường dẫn
        driver.setAnhDaiDien("/uploads/" + pathAvartar);
        driver.setLLTP("/uploads/"+pathLLTP);

        // Lưu vào database
        taiXeDAO.insert(driver);
        System.out.println("Thông tin tài xế: " + driver.toString());
    }
    public List<TaiXe> dsTaiXeGanKhachHang(double khoangCach,String diaChiKH) throws IOException{
    	List<TaiXe> ds=new ArrayList<>();
    	String thanhPhoKH = layThanhPho(diaChiKH); 

    	List<TaiXe> dsAllTaiXe = taiXeDAO.selectAll().stream()
    		    .filter(tx -> {
    		        String thanhPhoTX = layThanhPho(tx.getDiaChi());
    		        return thanhPhoTX.equalsIgnoreCase(thanhPhoKH)
    		            && (tx.getTrangThaiHD()==true);
    		    })
    		    .collect(Collectors.toList());


    	for (TaiXe taiXe : dsAllTaiXe) {
			double distance=DistanceCalculator.calculateDistanceByAddress(taiXe.getDiaChi(),diaChiKH);
			if(distance<=khoangCach) {
				ds.add(taiXe);
			}
		}
    	return ds;
    }
    public List<HashMap<String, Object>> dsTaiXeTieuBieu() throws SQLException{
    	ResultSet rs=taiXeDAO.dsTaiXeTieuBieu();
    	List<HashMap<String, Object>> result = new ArrayList<>();
        while (rs.next()) {
            HashMap<String, Object> item = new HashMap<>();
            item.put("id", rs.getString("ID_TX"));
            item.put("tenTX", rs.getString("TENTX"));
            item.put("anhDaiDien",rs.getString("ANHDAIDIEN"));
            item.put("diemTB", rs.getDouble("DIEM_TRUNGBINH"));
            
            result.add(item);
        }
        return result;
    }

}
