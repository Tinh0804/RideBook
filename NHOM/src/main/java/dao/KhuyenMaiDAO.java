package dao;

import java.sql.ResultSet;
import java.util.List;

import model.KhuyenMai;

public class KhuyenMaiDAO extends AbstractDAO<KhuyenMai> implements InterfaceDAO<KhuyenMai> {

	@Override
	public int insert(KhuyenMai obj) {
		String sql="INSERT INTO KHUYENMAI (ID_KHUYENMAI, TENKM, HANMUC, TGBATDAU, TGKETTHUC, DIEUKIENAPDUNG, SOLUONG) VALUES\n"
				+ "(?,?,?,?,?,?,?)";
		Object[] params = {obj.getID_KhuyenMai(),obj.getTenKM(),obj.getHanMuc(),obj.getTGBatDau(),obj.getTGKetThuc(),obj.getDieuKienApDung(),obj.getSoLuong()};
		return executeUpdate(sql, params);
	}

	@Override
	public int update(KhuyenMai obj) {
		String sql="UPDATE KHUYENMAI SET TENKM=?, HANMUC=?, TGBATDAU=?, TGKETTHUC=?, DIEUKIENAPDUNG=?, SOLUONG=? WHERE ID_KHUYENMAI=?"
				+ "(?,?,?,?,?,?,?)";
		Object[] params = {obj.getTenKM(),obj.getHanMuc(),obj.getTGBatDau(),obj.getTGKetThuc(),obj.getDieuKienApDung(),obj.getSoLuong(),obj.getID_KhuyenMai()};
		return executeUpdate(sql, params);
	}

	@Override
	public int delete(String id) {
		String sql="DELETE KHUYENMAI WHERE ID_KHUYENMAI=?"
				+ "(?,?,?,?,?,?,?)";
		Object[] params = {id};
		return executeUpdate(sql, params);
	}

	@Override
	public KhuyenMai selectById(String id) {
		String sql="SELECT * FROM KHUYENMAI WHERE ID_KHUYENMAI=?";
		Object[] params= {id};
		return executeQuery(sql, params, khuyenMaiMapper);
	}

	@Override
	public List<KhuyenMai> selectAll() {
		String sql="SELECT * FROM KHUYENMAI ";
		return executeQueryList(sql, null, khuyenMaiMapper);
	}

	@Override
	public List<KhuyenMai> selectByCondition(String sql,Object[] params) {
		// TODO Auto-generated method stub
		return null;
	}
	 private final RowMapper<KhuyenMai> khuyenMaiMapper = (ResultSet rs) -> {
             KhuyenMai obj = new KhuyenMai();
             obj.setID_KhuyenMai(rs.getString("ID_KHUYENMAI"));
             obj.setTenKM(rs.getString("TENKM"));
             obj.setHanMuc(rs.getDouble("HANMUC"));
             obj.setTGBatDau(rs.getTimestamp("TGBATDAU"));
             obj.setTGKetThuc(rs.getTimestamp("TGKETTHUC"));
             obj.setDieuKienApDung(rs.getString("DIEUKIENAPDUNG"));
             obj.setSoLuong(rs.getInt("SOLUONG"));
             return obj;
        };

}
