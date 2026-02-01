package dao;

import java.sql.ResultSet;
import java.util.List;

import model.PhuongThucThanhToan;

public class PhuongThucThanhToanDAO extends AbstractDAO<PhuongThucThanhToan> implements InterfaceDAO<PhuongThucThanhToan>{

	@Override
	public int insert(PhuongThucThanhToan obj) {
		String sql="INSERT INTO PHUONGTHUCTHANHTOAN (ID_THANHTOAN, LOAIHINHTHANHTOAN, TRANGTHAITT) VALUES\n"
				+ "(?,?,?)";
		Object [] params= {obj.getID_ThanhToan(),obj.getLoaiHinhThanhToan(),obj.isTrangThaiTT()};
		return executeUpdate(sql, params);
	}

	@Override
	public int update(PhuongThucThanhToan obj) {
		String sql="UPDATE PHUONGTHUCTHANHTOAN SET LOAIHINHTHANHTOAN=?, TRANGTHAITT=? WHERE ID_THANHTOAN=?";
		Object [] params= {obj.getLoaiHinhThanhToan(),obj.isTrangThaiTT(),obj.getID_ThanhToan()};
		return executeUpdate(sql, params);
	}

	@Override
	public int delete(String id) {
		String sql="DELETE PHUONGTHUCTHANHTOAN   WHERE ID_THANHTOAN=?";
		Object [] params= {id};
		return executeUpdate(sql, params);
	}

	@Override
	public PhuongThucThanhToan selectById(String id) {
		String sql="SELECT * FROM PHUONGTHUCTHANHTOAN WHERE ID_THANHTOAN=?";
		Object [] params= {id};
		return executeQuery(sql, params, ptttMapper);
	}

	@Override
	public List<PhuongThucThanhToan> selectAll() {
		String sql="SELECT * FROM PHUONGTHUCTHANHTOAN ";
		return executeQueryList(sql, null, ptttMapper);
	}

	@Override
	public List<PhuongThucThanhToan> selectByCondition(String sql,Object[] params) {
		// TODO Auto-generated method stub
		return null;
	}
	 private final RowMapper<PhuongThucThanhToan> ptttMapper = (ResultSet rs) -> {
             PhuongThucThanhToan obj = new PhuongThucThanhToan();
             obj.setID_ThanhToan(rs.getString("ID_THANHTOAN"));
             obj.setLoaiHinhThanhToan(rs.getString("LOAIHINHTHANHTOAN"));
             obj.setTrangThaiTT(rs.getBoolean("TRANGTHAITT"));
             return obj;
        };
}
