package service;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dao.DatXeDAO;
import dao.KhachHangDAO;
import dao.KhuyenMaiDAO;
import dao.LoaiXeDAO;
import dao.PhuongThucThanhToanDAO;
import dao.TaiXeDAO;
import model.DatXe;
import model.KhachHang;
import model.KhuyenMai;
import model.PhuongThucThanhToan;
import model.TaiXe;
import utils.DistanceCalculator;

public class ChuyenXeService {
    private DatXeDAO datXeDAO = new DatXeDAO();
    private LoaiXeDAO loaiXeDAO=new LoaiXeDAO();
    private KhachHangDAO khachHangDAO = new KhachHangDAO();
    private KhuyenMaiDAO khuyenMaiDAO=new KhuyenMaiDAO();
    private PhuongThucThanhToanDAO ptttDAO=new PhuongThucThanhToanDAO();
    private TaiXeDAO taiXeDAO=new TaiXeDAO();

     public void bookRide(DatXe datXe, String loaiXe) throws SQLException {
        System.out.print(datXe.getDiemDon());
        System.out.print(datXe.getDiemTra());
        datXe.getDiemTra();
        System.out.println(datXe);
        List<TaiXe> drivers = taiXeDAO.findAllDriverActive();
        if (!drivers.isEmpty()) {
            datXeDAO.themChuyenXe(datXe, loaiXe);
        } else {
            throw new RuntimeException("No available drivers");
        }
    }
    public boolean kiemTraNhanChuyen(String idTX) {
    	return datXeDAO.kiemTraNhanChuyen(idTX)!=null;
    }
    public int huyChuyenXe(String id) {
    	return datXeDAO.huyChuyenXe(id);
    }
    public int soChuyenXeTheoTrangThai(String trangThai) {
    	return datXeDAO.soChuyenXeTheoTrangThai(trangThai);
    }
    public int tongChuyenXeTrongNgay(LocalDate time) throws SQLException{
    	List<DatXe> list=datXeDAO.soChuyenXeTrongNgay(time);
    	return list.size();
    }
    public int tongChuyenXeTrongThang(LocalDate time) throws SQLException{
    	List<DatXe> list=datXeDAO.soChuyenXeTrongThang(time);
    	return list.size();
    }
    public double tongDoanhThuTrongNgay(LocalDate time)throws SQLException{
    	List<DatXe> list=datXeDAO.soChuyenXeTrongNgay(time);
    	double tong=0;
    	for (DatXe datXe : list) {
			tong+=datXeDAO.soTienChuyenXe(datXe.getID_DatXe());
		}
    	return tong;
    }
    public double tongDoanhThuTrongThang(LocalDate time)throws SQLException{
    	List<DatXe> list=datXeDAO.soChuyenXeTrongThang(time);
    	double tong=0;
    	for (DatXe datXe : list) {
			tong+=datXeDAO.soTienChuyenXe(datXe.getID_DatXe());
		}
    	return tong;
    }
    public double tongKhachHang()throws SQLException{
    	List<KhachHang> list=khachHangDAO.selectAll();
    	if(list!=null)
    		return list.size();
    	return 0;
    
    }
    public double tongTaiXe()throws SQLException{
    	List<TaiXe> list=taiXeDAO.selectAll();
    	if(list!=null)
    		return list.size();
    	return 0;
    
    }
    public double tongChuyenXe()throws SQLException{
    	List<DatXe> list=datXeDAO.selectAll();
    	if(list!=null)
    		return list.size();
    	return 0;
    
    }
    public List<Map<String,Object>> chuyenXeGanDay(int soDong) throws SQLException{
    	return datXeDAO.chuyenXeGanDay(soDong);
    }
   
    public List<DatXe> getAllRides() {
        return datXeDAO.selectAll();
    }
    public HashMap<String,Object> dsChuyenXeChiTiet(){
    	HashMap<String, Object> map=new HashMap<>();
    	
    	return map;
    }
    public int tongChuyenXeTrongNgay() {
    	return 0;
    }
    public String doiTrangThai(String id,String trangThai) throws SQLException {
    	ResultSet rs=datXeDAO.doiTrangThaiChuyenXe(id, trangThai);
    	String message="Lỗi.Vui lòng thử lại sau!";
    	while(rs.next()) {
    		message=rs.getString("Result");
    	}
    	return message;
    }
    public String ganTaiXeNhanDon(String idChuyenXe,String idTaiXe) throws SQLException {
    	
    	ResultSet rs=datXeDAO.ganTaiXeNhanDon(idChuyenXe, idTaiXe);
    	String message="Lỗi.Vui lòng thử lại sau!";
    	while(rs.next()) {
    		message=rs.getString("Result");
    	}
    	return message;
    }
    public HashMap<Integer, Integer> soChuyenXeTheoThang(String year) throws SQLException{
    	ResultSet rs=datXeDAO.soChuyenXeTheoThang(year);
    	HashMap<Integer, Integer> map=new HashMap<>();
    	while(rs.next()) {
    		map.put(rs.getInt("Thang"),rs.getInt("soChuyenXe"));
    	}
    	return map;
    }
    public HashMap<String,String> tongChuyenXeTheoKhuVuc() throws SQLException{
    	ResultSet rs=datXeDAO.tongChuyenXeTheoKhuVuc();
    	HashMap<String, String> map=new HashMap<>();
    	while(rs.next()) {
    		map.put("DaNang",rs.getString("DaNang"));
    		map.put("TP.HCM",rs.getString("TP.HCM"));
    		map.put("HaNoi",rs.getString("HaNoi"));
    	}
    	return map;
    }
    public HashMap<String,String> thongKeTyLeDungKM() throws SQLException{
    	ResultSet rs=datXeDAO.thongKeTyLeDungKM();
    	HashMap<String, String> map=new HashMap<>();
    	while(rs.next()) {
    		map.put("id_khuyenMai",rs.getString("id_khuyenMai"));
    		map.put("tenKM",rs.getString("tenKM"));
    		map.put("soLuong",rs.getString("soLuong"));
    		map.put("tyLe",rs.getString("tyLe"));
    	}
    	return map;
    }
    public HashMap<String,String> thongKeTrangThaiChuyen() throws SQLException{
    	ResultSet rs=datXeDAO.thongKeChuyenXeTheoTrangThai();
    	HashMap<String, String> map=new HashMap<>();
    	while(rs.next()) {
    		map.put(rs.getString("trangThai"),rs.getString("soLuong"));		
    	}
    	return map;
    }
    public HashMap<String,String> thongKeTyLeDungKhuyenMai() throws SQLException{
    	ResultSet rs=datXeDAO.thongKeTyLeDungKM();
    	HashMap<String, String> map=new HashMap<>();
    	while(rs.next()) {
    		map.put(rs.getString("tenKM"),rs.getString("tyLe"));		
    	}
    	return map;
    }
    public HashMap<String, Double> thongKePTTT(int year) throws SQLException{
    	ResultSet rs=datXeDAO.thongKePTTT(year);
    	HashMap<String, Double> map=new HashMap<>();
    	while(rs.next()) {
    		map.put(rs.getString("PhuongThucThanhToan"), rs.getDouble("TongDoanhThu"));
    	}
    	return map;
    }
    public HashMap<String,Double> thongKeDoanhThu() throws SQLException{
    	double nam2020=datXeDAO.tinhTongDoanhThuTheoNam(2020);
    	double nam2021=datXeDAO.tinhTongDoanhThuTheoNam(2021);
    	double nam2022=datXeDAO.tinhTongDoanhThuTheoNam(2022);
    	double nam2023=datXeDAO.tinhTongDoanhThuTheoNam(2023);
    	double nam2024=datXeDAO.tinhTongDoanhThuTheoNam(2024);
    	double nam2025=datXeDAO.tinhTongDoanhThuTheoNam(2025);
    	
    	HashMap<String, Double> map=new HashMap<>();
    	map.put("2020", nam2020);
    	map.put("2021", nam2021);
    	map.put("2022", nam2022);
    	map.put("2023", nam2023);
    	map.put("2024", nam2024);
    	map.put("2025", nam2025);
    	return map;
    }
   
    public List<Map<String, Object>> chuyenXeChiTiet() throws SQLException {
        List<DatXe> chuyenXeList = datXeDAO.selectAll();
        List<Map<String, Object>> formattedList = new ArrayList<>();

        for (DatXe chuyenXe : chuyenXeList) {
            // Xử lý khachHang
            String idKhachHang = chuyenXe.getID_KhachHang();
            KhachHang khachHang = idKhachHang != null ? khachHangDAO.selectById(idKhachHang) : null;

            // Xử lý taiXe
            String idTaiXe = chuyenXe.getID_TaiXe();
            TaiXe taiXe = null;
            if(idTaiXe!=null) {
            	taiXe=taiXeDAO.selectById(idTaiXe);
            	
            }

            // Xử lý khuyenMai
            String idKhuyenMai = chuyenXe.getID_KhuyenMai();
            KhuyenMai khuyenMai = idKhuyenMai != null ? khuyenMaiDAO.selectById(idKhuyenMai) : null;

            // Xử lý phuongThucThanhToan
            String idThanhToan = chuyenXe.getID_ThanhToan();
            PhuongThucThanhToan pttt = idThanhToan != null ? ptttDAO.selectById(idThanhToan) : null;

            // Tạo map mới với định dạng JSON mong muốn
            Map<String, Object> formattedChuyenXe = new HashMap<>();
            formattedChuyenXe.put("id_chuyenXe", chuyenXe.getID_DatXe());
            formattedChuyenXe.put("khachHang", khachHang);
            formattedChuyenXe.put("taiXe", taiXe);
            formattedChuyenXe.put("thanhToan", pttt);
            formattedChuyenXe.put("khuyenMai", khuyenMai);
            formattedChuyenXe.put("diemDon", chuyenXe.getDiemDon());
            formattedChuyenXe.put("diemDen", chuyenXe.getDiemTra());
            formattedChuyenXe.put("thoiGianDat", chuyenXe.getThoiGianDat());
            formattedChuyenXe.put("thoiGianDon", chuyenXe.getThoiGianDon());
            formattedChuyenXe.put("thoiGianDen", chuyenXe.getThoiGianDen());
            formattedChuyenXe.put("trangThai", chuyenXe.getTrangThai());
            formattedChuyenXe.put("khoangCach", chuyenXe.getKhoangCach());
            formattedChuyenXe.put("diemSo", chuyenXe.getDiemSo()); 
            formattedChuyenXe.put("danhGia", chuyenXe.getDanhGia());
            formattedChuyenXe.put("giaTien", chuyenXe.getGiaTien());
            

            formattedList.add(formattedChuyenXe);
        }

        return formattedList;
    }
    public int capNhatDanhGia(String id, String danhGia, int diem) throws SQLException {
        return datXeDAO.capNhatDGia(id, danhGia, diem);
 }
    public List<DatXe> lichSuChuyenXe(String idKH){
    	return datXeDAO.lichSuChuyenXeTheoKH(idKH);
    }
	   public List<DatXe> getAvailableRides(String diaChiTX) throws SQLException, IOException {
        List<DatXe> ds = datXeDAO.getAvailableRides();
        List<DatXe> danhSachGan = new ArrayList<>();
        
        for (DatXe datXe : ds) {
            double khoangCach = DistanceCalculator.calculateDistanceByAddress(diaChiTX, datXe.getDiemDon());
            System.out.println(datXe.getDiemDon());

            if (khoangCach <= 10.0) {
                danhSachGan.add(datXe);
            }
        }
        if(ds.isEmpty())
        	return null;
        return danhSachGan;
    }
    
}
