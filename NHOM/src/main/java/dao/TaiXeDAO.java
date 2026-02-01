package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.TaiXe;

public class TaiXeDAO extends AbstractDAO<TaiXe> implements InterfaceDAO<TaiXe> {
	
	@Override
	public int insert(TaiXe taiXe) {
		String newID = (String) callFunction("Fn_TangTuDongID_TaiXe()",null,Types.VARCHAR);
	        if (newID == null) return 0;

	    if(taiXe.getID_TaiXe()==null) {
	    	taiXe.setID_TaiXe(newID);
	    }
		String sql = "INSERT INTO TAIXE (ID_TX, ID_LOAIXENO, TENTX, NGSINH, CCCD, GPLX,LLTP, SDT, EMAIL, BIENSOXE, TENXE,GIOITINH,DIACHI,KHUVUC) VALUES (?, ?, ?, ?, ?,?,?,?,?,?,?,?,?,?)";
        Object[] params = {taiXe.getID_TaiXe(),taiXe.getID_LoaiXe(),taiXe.getTenTaiXe(),taiXe.getNgaySinh(),taiXe.getCCCD(),taiXe.getGPLX(),taiXe.getLLTP(),taiXe.getSDT(),taiXe.getEmail(),taiXe.getBienSoXe(),taiXe.getTenXe(),taiXe.getGioiTinh(),taiXe.getDiaChi(),taiXe.getKhuVuc()};
        return executeUpdate(sql, params);
	}
	@Override
	public int update(TaiXe taiXe) {
		String sql = "UPDATE TAIXE SET ID_LOAIXE=?, TENTX=?, NGSINH=?, CCCD=?, GPLX=?,LLTP=?, SDT=?, EMAIL=?, BIENSOXE=?, TENXE=?,GIOITINH=?,DIACHI=?,KHUVUC=? WHERE ID_TX=?";
        Object[] params = {taiXe.getID_LoaiXe(),taiXe.getTenTaiXe(),taiXe.getNgaySinh(),taiXe.getCCCD(),taiXe.getGPLX(),taiXe.getLLTP(),taiXe.getSDT(),taiXe.getEmail(),taiXe.getBienSoXe(),taiXe.getTenXe(),taiXe.getID_TaiXe(),taiXe.getGioiTinh(),taiXe.getDiaChi(),taiXe.getKhuVuc()};
        return executeUpdate(sql, params);
	}
	
	@Override
	public int delete(String id) {
		 String sql = "DELETE FROM TAIXE WHERE ID_TX=?";
		    Object[] params = {id};
		    return executeUpdate(sql, params);
	}

	@Override
	public TaiXe selectById(String id) {
		 String sql = "SELECT * FROM TAIXE WHERE ID_TX=?";
	        Object[] params = {id};
	        return executeQuery(sql, params,taiXeMapper);
	}

	@Override
	public List<TaiXe> selectAll() {
		String sql = "SELECT * FROM TAIXE";
	    return executeQueryList(sql, null, taiXeMapper);
	}
	
	@Override
	public List<TaiXe> selectByCondition(String sql,Object[] params) {
		// TODO Auto-generated method stub
		return null;
	}
    public List<TaiXe> findAllDriverActive() {
        String sql = "SELECT * FROM TAIXE WHERE TRANGTHAIHD = 1";
        return executeQueryList(sql, null, taiXeMapper);
    }

    public int huyChuyenXe(String id) {
   	 String sql = "UPDATE DATXE SET TRANGTHAI='Đã huỷ' WHERE ID_DATXE=?";
        Object[] params = { id };
        return executeUpdate(sql, params);
    }
//    public int nhanChuyenXe(String id) {
//   	 String sql = "INSERT INTO DATXE (ID_DATXE, ID_KH, ID_TX, ID_THANHTOAN, ID_KHUYENMAI, DIEMDON, DIEMTRA, THOIGIANDAT, THOIGIANDON, THOIGIANDEN, TRANGTHAI, KHOANGCACH, DIEMSO, DANHGIA) VALUES\n"
//   	 		+ "(?,?, ?, ?, ?,?, ?, ?, ?, ?, ?, ?, ?,?'),";
//        Object[] params = { };
//        return executeUpdate(sql, params);
//   }

    public boolean existsByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM TAIXE WHERE EMAIL=?";
        Object[] params = {email};
        return executeQuery(sql, params, taiXeMapper) != null;
    }

    public boolean existsBySDT(String sdt) throws SQLException {
        String sql = "SELECT * FROM TAIXE WHERE SDT=?";
        Object[] params = {sdt};
        return executeQuery(sql, params, taiXeMapper) != null;
    }

    public boolean existsByCCCD(String cccd) throws SQLException {
        String sql = "SELECT * FROM TAIXE WHERE CCCD = ?";
        Object[] params = {cccd};
        return executeQuery(sql, params, taiXeMapper) != null;
    }
    public boolean existsByBSX(String bsx) throws SQLException {
        String sql = "SELECT * FROM TAIXE WHERE BIENSOXE = ?";
        Object[] params = {bsx};
        return executeQuery(sql, params, taiXeMapper) != null;
    }
    
    public String idTuDongtangTaiXe() throws SQLException {
    	return (String) callFunction("Fn_TangTuDongID_TaiXe",null,Types.VARCHAR);
    }
    public ResultSet themTaiXe_Pr(TaiXe taixe,String matKhau) throws SQLException {
    	Object[] params= {taixe.getID_LoaiXe(),taixe.getTenTaiXe(),taixe.getNgaySinh(),taixe.getCCCD(),taixe.getGPLX(),taixe.getSDT(),taixe.getEmail(),matKhau,taixe.getBienSoXe(),taixe.getTenXe(),taixe.getAnhDaiDien(),taixe.getGioiTinh(),taixe.getDiaChi(),taixe.getKhuVuc(),taixe.getLLTP()};
    	ResultSet rs= callProcedureResultSet("Pr_ThemTaiXe",params );
		return rs;
    	
    	
    }
    public ResultSet capNhatTaiXe_Pr(TaiXe taixe) throws SQLException {
    	Object[] params= {taixe.getID_TaiXe(),"LX001",taixe.getTenTaiXe(),taixe.getNgaySinh(),taixe.getCCCD(),taixe.getGPLX(),taixe.getSDT(),taixe.getEmail(),taixe.getBienSoXe(),taixe.getTenXe(),taixe.getTrangThaiHD(),taixe.getAnhDaiDien(),taixe.getGioiTinh(),taixe.getDiaChi(),taixe.getKhuVuc(),taixe.getLLTP()};
    	ResultSet rs= callProcedureResultSet("Pr_CapNhatTaiXe",params );
		return rs;
    	
    	
    }
    
    public List<Map<String, Object>> lichSuChuyenXe() throws SQLException {
    	String sql="Pr_LichSuChuyenXe";
    	return executeProcedure(sql, null, rideMapper);
    	
    }
    public List<TaiXe> dsTaiXeCoTheNhanChuyen(){
    	String sql="Pr_DSTaiXeCoTheNhanChuyen";
    	return executeQueryList(sql, null, taiXeMapper);
    }
    public ResultSet dsTaiXeTieuBieu() throws SQLException{
		String sql="Pr_DSTaiXeTieuBieu";
		return callProcedureResultSet(sql, null); 
    }
    	
    private final RowMapper<TaiXe> taiXeMapper = (ResultSet rs) -> {
        TaiXe obj = new TaiXe();
        obj.setID_TaiXe(rs.getString("ID_TX"));         // sửa lại tên cho rõ nghĩa
        obj.setID_LoaiXe(rs.getString("ID_LOAIXENO"));
        obj.setTenTaiXe(rs.getString("TENTX"));
        obj.setNgaySinh(rs.getDate("NGSINH"));
        obj.setCCCD(rs.getString("CCCD"));
        obj.setGPLX(rs.getString("GPLX"));
        obj.setLLTP(rs.getString("LLTP"));
        obj.setSDT(rs.getString("SDT"));
        obj.setEmail(rs.getString("EMAIL"));
        obj.setBienSoXe(rs.getString("BIENSOXE"));
        obj.setTenXe(rs.getString("TENXE"));
        obj.setAnhDaiDien(rs.getString("ANHDAIDIEN"));
        obj.setTrangThaiHD(rs.getBoolean("TRANGTHAIHD"));
        obj.setGioiTinh(rs.getString("GIOITINH"));
        obj.setDiaChi(rs.getString("DIACHI"));
        obj.setKhuVuc(rs.getString("KHUVUC"));
        
        return obj;
        };
    private final ResultSetMapper rideMapper = rs -> {
        Map<String, Object> ride = new HashMap<>();
        ride.put("ngay", rs.getString("ngay"));
        ride.put("maChuyenXe", rs.getString("maChuyenXe"));
        ride.put("khachHang", rs.getString("khachHang"));
        ride.put("giaTien", rs.getDouble("giaTien"));
        ride.put("trangThai", rs.getString("trangThai"));
        ride.put("diemDon", rs.getString("diemDon"));
        ride.put("diemTra", rs.getString("diemTra"));
        ride.put("khoangCach", rs.getDouble("khoangCach"));
        ride.put("thoiGianDon", rs.getString("thoiGianDon"));
        ride.put("thoiGianDen", rs.getString("thoiGianDen"));
        ride.put("danhGia", rs.getString("danhGia") != null ? rs.getString("danhGia") : "Chưa có");
        ride.put("diemSo", rs.getInt("diemSo"));
        ride.put("tenTaiXe", rs.getString("tenTaiXe"));
        ride.put("hinhThucThanhToan", rs.getString("hinhThucThanhToan") != null ? rs.getString("hinhThucThanhToan") : "Không xác định");
        ride.put("khuyenMai", rs.getString("khuyenMai"));
        return ride;
    };


}
