package dao;

import java.sql.ResultSet;
import java.util.List;

import model.LoaiXe;

public class LoaiXeDAO extends AbstractDAO<LoaiXe> implements InterfaceDAO<LoaiXe>{

	@Override
	public int insert(LoaiXe obj) {
		String sql="INSERT INTO LOAIXE (ID_LOAIXE, TENLOAIXE, GIA1KM) VALUES\n"
				+ "(?,?, ?)";
		Object[] params= {obj.getID_LoaiXe(),obj.getTenLoaiXe(),obj.getGia1KM()};
		return executeUpdate(sql, params);
	}

	@Override
	public int update(LoaiXe obj) {
		String sql="UPDATE LOAIXE SET ID_LOAIXE=?, TENLOAIXE=?, GIA1KM=?";
		Object[] params= {obj.getID_LoaiXe(),obj.getTenLoaiXe(),obj.getGia1KM()};
		return executeUpdate(sql, params);
	}

	@Override
	public int delete(String id) {
		String sql="DELETE LOAIXE SET ID_LOAIXE=?";
		Object[] params= {id};
		return executeUpdate(sql, params);
	}

	@Override
	public LoaiXe selectById(String id) {
		String sql="SELECT * FROM LOAIXE WHERE ID_LOAIXE=?";
		Object[] params= {id};
		return executeQuery(sql, params, loaiXeMapper);
	}

	@Override
	public List<LoaiXe> selectAll() {
		String sql="SELECT * FROM LOAIXE ";

		return executeQueryList(sql, null, loaiXeMapper);
	}

	@Override
	public List<LoaiXe> selectByCondition(String sql,Object[] params) {
		// TODO Auto-generated method stub
		return null;
	}
	 private final RowMapper<LoaiXe> loaiXeMapper = (ResultSet rs) -> {
             LoaiXe obj = new LoaiXe();
             obj.setID_LoaiXe(rs.getString("ID_LOAIXE"));
             obj.setTenLoaiXe(rs.getString("TENLOAIXE"));
             obj.setGia1KM(rs.getDouble("GIA1KM"));
             return obj;
        };
	
}
