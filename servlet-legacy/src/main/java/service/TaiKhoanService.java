package service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import dao.TaiKhoanDAO;
import model.TaiKhoan;

public class TaiKhoanService {
	private TaiKhoanDAO taiKhoanDAO=new TaiKhoanDAO();
	
	public int signUp(TaiKhoan taiKhoan) {
		if(taiKhoanDAO.selectById(taiKhoan.getUserName())!=null) {
			return -1;
		}
		return taiKhoanDAO.insert(taiKhoan);
	}
	public int doiMatKhau(TaiKhoan taiKhoan) {
		return taiKhoanDAO.update(taiKhoan);
	}
	public int deleteAccount(String id) {
		return taiKhoanDAO.delete(id);
	}
	public List<TaiKhoan> selectAll(){
		return taiKhoanDAO.selectAll();
	}
	public TaiKhoan signIn(TaiKhoan taiKhoan) 
	{
		return taiKhoanDAO.signIn(taiKhoan.getUserName(),taiKhoan.getMatKhau(), taiKhoan.getID_VaiTro());
	}
	public Boolean kiemTraTrangThai(String userName,String idVaiTro) throws SQLException {
		return taiKhoanDAO.kiemTraTTTK(userName,idVaiTro);
	}
	public HashMap<String, String> mo_KhoaTaiKhoan(String id,boolean trangThai) throws SQLException {
		HashMap<String, String> map=new HashMap<>();
		ResultSet rs=taiKhoanDAO.mo_KhoaTaiKhoan(id, trangThai);
		while (rs.next()) {
			map.put("status",rs.getString("status"));
			map.put("message",rs.getString("message"));
		}
		return map;
	}
}
