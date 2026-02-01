package dao;

import java.sql.ResultSet;
import java.util.List;

import model.Gio;

public class GioDAO extends AbstractDAO<Gio> implements InterfaceDAO<Gio>{

	@Override
	public int insert(Gio obj) {
		String sql="INSERT INTO GIO (ID_GIO, THOIGIANBATDAU, THOIGIANKETTHUC) VALUES"
				+ "(?,?,?)";
		Object[] params= {obj.getID_Gio(),obj.getThoiGianBatDau(),obj.getThoiGianKetThuc()};
		return executeUpdate(sql, params);
	}

	@Override
	public int update(Gio obj) {
		String sql="UPDATE GIO SET THOIGIANBATDAU=?, THOIGIANKETTHUC=? WHERE ID_GIO=? ";
		Object[] params= {obj.getThoiGianBatDau(),obj.getThoiGianKetThuc(),obj.getID_Gio()};
		return executeUpdate(sql, params);
	}

	@Override
	public int delete(String id) {
		String sql="DELETE GIO  WHERE ID_GIO=? ";
		Object[] params= {id};
		return executeUpdate(sql, params);
	}

	@Override
	public Gio selectById(String id) {
		String sql="SELECT * FROM GIO  WHERE ID_GIO=? ";
		Object[] params= {id};
		return executeQuery(sql, params, gioMapper);
	}

	@Override
	public List<Gio> selectAll() {
		String sql="SELECT * FROM GIO ";
		Object[] params= {};
		return executeQueryList(sql, params, gioMapper);
	}

	@Override
	public List<Gio> selectByCondition(String sql,Object[] params) {
		// TODO Auto-generated method stub
		return null;
	}
	private final RowMapper<Gio> gioMapper = (ResultSet rs) -> {
            Gio obj = new Gio();
            obj.setID_Gio(rs.getString("ID_GIO"));
            obj.setThoiGianBatDau(rs.getTime("THOIGIANBATDAU"));
            obj.setThoiGianKetThuc(rs.getTime("THOIGIANKETTHUC"));
            return obj;
        };

}
