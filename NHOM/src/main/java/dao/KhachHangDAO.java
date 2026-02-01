package dao;

import java.sql.*;
import java.util.List;

import model.DatXe;
import model.KhachHang;
public class KhachHangDAO extends AbstractDAO<KhachHang> implements InterfaceDAO<KhachHang>{

  

    // Ví dụ phương thức chèn mới khách hàng
    @Override
    public int insert(KhachHang khachHang) {
        String sql = "INSERT INTO KHACHHANG (ID_KH, TENKH, SDT, DIACHI) VALUES (?, ?, ?, ?, ?)";
        Object[] params = {khachHang.getID_KhachHang(), khachHang.getTenKhachHang(), khachHang.getSDT(), khachHang.getDiaChi()};
        return executeUpdate(sql, params);
    }

    // Ví dụ phương thức cập nhật khách hàng
    @Override
    public int update(KhachHang khachHang) {
        String sql = "UPDATE KHACHHANG SET TENKH=?, SDT=?, DIACHI=? WHERE ID_KH=?";
        Object[] params = {khachHang.getTenKhachHang(), khachHang.getSDT(), khachHang.getDiaChi(), khachHang.getID_KhachHang()};
        return executeUpdate(sql, params);
    }
	@Override
	public int delete(String id) {
		  String sql = "DELETE FROM KHACHHANG WHERE ID_KH=?";
		    Object[] params = {id};
		    return executeUpdate(sql, params);
	}

    // Ví dụ phương thức chọn một khách hàng theo ID
    @Override
    public KhachHang selectById(String id) {
        String sql = "SELECT * FROM KHACHHANG WHERE ID_KH=?";
        Object[] params = {id};
        return executeQuery(sql, params, khachHangMapper);
    }


	@Override
	public List<KhachHang> selectAll() {
		String sql = "SELECT * FROM KHACHHANG";
	    return executeQueryList(sql, null,khachHangMapper);
	}

	@Override
	public List<KhachHang> selectByCondition(String sql,Object[] params) {
		// TODO Auto-generated method stub
		return null;
	}	
	public int khachHangMoiThangNay() {
		String sql="Fn_KhachHangMoiThangNay";
		return (int) callFunction(sql, null, Types.INTEGER);
	}
	public double danhGiaTB() {
		String sql="Fn_TinhDanhGiaTrungBinh";
		return (double) callFunction(sql, null, Types.DOUBLE);
	}
	public double tyLeHaiLong() {
		String sql="Fn_TyLeHaiLongCuaKhachHang";
		return (double) callFunction(sql, null, Types.DOUBLE);
	}
	public ResultSet capNhatKhachHang(KhachHang KH,String pass) throws SQLException {
		String sql="Pr_CapNhatKhachHang";
		Object[] params= {KH.getID_KhachHang(),KH.getTenKhachHang(),KH.getSDT(),KH.getDiaChi(),pass};
		return callProcedureResultSet(sql, params);
	}
	public ResultSet themKhachHang(KhachHang KH,String pass) throws SQLException{
		String sql="Pr_ThemKhachHang";
		Object[] params= {KH.getTenKhachHang(),KH.getSDT(),KH.getDiaChi(),pass};
		return callProcedureResultSet(sql, params);
	}
	private final RowMapper<KhachHang> khachHangMapper = new RowMapper<KhachHang>() {
	    @Override
	    public KhachHang mapRow(ResultSet rs) throws SQLException {
	    	KhachHang obj = new KhachHang();
            obj.setID_KhachHang(rs.getString("ID_KH"));
            obj.setTenKhachHang(rs.getString("TENKH"));
            obj.setDiaChi(rs.getString("DIACHI"));
            obj.setSDT(rs.getString("SDT"));
            return obj;
	       
	    }
	};


    // Các phương thức khác tiếp tục như vậy...
}
