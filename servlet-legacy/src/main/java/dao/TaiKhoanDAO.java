package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import model.TaiKhoan;

public class TaiKhoanDAO extends AbstractDAO<TaiKhoan> implements InterfaceDAO<TaiKhoan>{

	@Override
	public int insert(TaiKhoan obj) {
		String sql = "INSERT INTO TAIKHOAN (SDT, MATKHAU,ID_VAITRONO,ID_NGUOIDUNG,TRANGTHAITK) VALUES\n"
				+ "(?,?,?,?,?)";
		Object[] params= {obj.getUserName(),obj.getMatKhau(),obj.getID_VaiTro(),obj.getID_NguoiDung(),true};
		return executeUpdate(sql, params);
	}

	@Override
	public int update(TaiKhoan obj) {
		String sql = "UPDATE TAIKHOAN SET  MATKHAU=?,ID_NGUOIDUNG=?,TRANGTHAITK=? WHERE SDT=? AND ID_VAITRONO=? "
				+ "(?,?,?,?)";
		Object[] params= {obj.getMatKhau(),obj.getID_NguoiDung(),obj.getTrangThaiTK(),obj.getUserName(),obj.getID_VaiTro()};
		return executeUpdate(sql, params);
	}

	@Override
	public int delete(String id) {
		String sql = "DELETE TAIKHOAN WHERE SDT=?";
		Object[] params= {id};
		return executeUpdate(sql, params);
	}

	@Override
	public TaiKhoan selectById(String id) {
		String sql = "SELECT * FROM TAIKHOAN WHERE SDT=?";
		Object[] params= {id};
		return executeQuery(sql, params, taiKhoanMapper);
	}
	
	@Override
	public List<TaiKhoan> selectAll() {
		String sql = "SELECT * FROM TAIKHOAN ";
		return executeQueryList(sql, null, taiKhoanMapper);
	}

	@Override
	public List<TaiKhoan> selectByCondition(String sql,Object[] params) {
		// TODO Auto-generated method stub
		return null;
	}
	public String layMatKhau(String idNguoiDung,String idVaiTro) {
		String sql="Fn_LayMatKhau";
		Object[] params= {idVaiTro,idNguoiDung};
		return (String) callFunction(sql, params, Types.VARCHAR);
	}
	public ResultSet mo_KhoaTaiKhoan(String userName,boolean trangThai) throws SQLException{
		String sql="Pr_MoKhoaTaiKhoan";
		Object [] params= {userName,trangThai};
		
		return callProcedureResultSet(sql, params);
	}
	public Boolean kiemTraTTTK(String userName,String idVaiTro) {
		String sql="Fn_KiemTraTTTK";
		Object[] params= {userName,idVaiTro};
		
		return (Boolean) callFunction(sql, params, Types.BOOLEAN);
	}
	public TaiKhoan signIn(String sdt,String pass,String vaitro) {
		String sql = "SELECT * FROM TAIKHOAN WHERE SDT=? AND ID_VAITRONO=? AND MATKHAU=?";
		Object[] params= {sdt,vaitro,pass};
		return executeQuery(sql, params, taiKhoanMapper);
	}
	 private final RowMapper<TaiKhoan> taiKhoanMapper = (ResultSet rs) -> {
             TaiKhoan obj = new TaiKhoan();
             obj.setUserName(rs.getString("SDT"));
             obj.setMatKhau(rs.getString("MATKHAU"));
             obj.setID_VaiTro(rs.getString("ID_VAITRONO"));
             obj.setID_NguoiDung(rs.getString("ID_NGUOIDUNG"));
             obj.setTrangThaiTK(rs.getBoolean("TRANGTHAITK"));
             return obj;
        };

}
