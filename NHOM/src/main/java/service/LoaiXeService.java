package service;

import java.util.List;

import dao.LoaiXeDAO;
import model.LoaiXe;

public class LoaiXeService {
	private LoaiXeDAO loaiXeDAO=new LoaiXeDAO();
	
	public List<LoaiXe> selectAll(){
		return loaiXeDAO.selectAll();
	}
	
}
