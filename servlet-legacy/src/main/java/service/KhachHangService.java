package service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dao.DatXeDAO;
import dao.KhachHangDAO;
import dao.TaiKhoanDAO;
import model.DatXe;
import model.KhachHang;

public class KhachHangService {
    private KhachHangDAO khachHangDAO = new KhachHangDAO();
    private DatXeDAO datXeDAO=new DatXeDAO();
    private TaiKhoanDAO taiKhoanDAO=new TaiKhoanDAO();

    public void registerUser(KhachHang khachHang) {
        khachHangDAO.insert(khachHang);
    }
    public HashMap<String, Object> dangKyUser(KhachHang KH,String pass) throws SQLException {
    	HashMap<String, Object> map=new HashMap<>();
        ResultSet rs= khachHangDAO.themKhachHang(KH, pass);
        while(rs.next()) {
     	   map.put("ID_KH",rs.getString("ID_KH"));
     	   map.put("status", rs.getInt("Status"));
     	   map.put("message", rs.getString("Message"));
        }
        return map;
    }
    public HashMap<String, Object> capNhatKhachHang(KhachHang khachHangMoi,String pass) throws SQLException {
        
        KhachHang khachHangCu = khachHangDAO.selectById(khachHangMoi.getID_KhachHang());
        
      
        if (khachHangCu == null) 
            throw new IllegalArgumentException("Khách hàng không tồn tại!");
        
       HashMap<String, Object> map=new HashMap<>();
       ResultSet rs= khachHangDAO.capNhatKhachHang(khachHangMoi,pass);
       while(rs.next()) {

    	   map.put("status", rs.getInt("status"));
    	   map.put("message", rs.getString("message"));
       }
       return map;
    }

  
    public List<KhachHang> selectAllUser() {
    	return khachHangDAO.selectAll();
    }
    public void deleteUser(String id) {
    	khachHangDAO.delete(id);
    }
    public int khachHangMoi() {
    	return khachHangDAO.khachHangMoiThangNay();
    }
    public double danhGiaTrungBinh() {
    	return khachHangDAO.danhGiaTB();
    }
    public double tyLeHaiLongChuyen() {
    	return khachHangDAO.tyLeHaiLong();
    }
    public KhachHang selectUser(String id){
        return khachHangDAO.selectById(id);
    }
    public List<Map<String, Object>> dsChiTietKhachHang(){
    	List<KhachHang> khachHangList = khachHangDAO.selectAll();
        List<Map<String, Object>> formattedList = new ArrayList<>();

        for (KhachHang khachHang : khachHangList) {
            List<DatXe> ds=datXeDAO.lichSuChuyenXeTheoKH(khachHang.getID_KhachHang());
            boolean trangThai=taiKhoanDAO.kiemTraTTTK(khachHang.getSDT(), "USER");
            
            // Tạo map mới với định dạng JSON mong muốn
            Map<String, Object> formattedKhachHang = new HashMap<>();
            formattedKhachHang.put("id_khachHang", khachHang.getID_KhachHang());
            formattedKhachHang.put("tenKhachHang", khachHang.getTenKhachHang());
            formattedKhachHang.put("diaChi", khachHang.getDiaChi());
            formattedKhachHang.put("sdt", khachHang.getSDT());
            formattedKhachHang.put("matKhau", taiKhoanDAO.layMatKhau(khachHang.getID_KhachHang(), "USER"));
            formattedKhachHang.put("lichSuChuyen", ds);
            formattedKhachHang.put("trangThai",trangThai );
            formattedKhachHang.put("khieuNai",null );
            
            formattedList.add(formattedKhachHang);
        }

        return formattedList;
    }


}
