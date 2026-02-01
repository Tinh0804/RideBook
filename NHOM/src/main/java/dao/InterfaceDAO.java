package dao;

import java.util.List;

public interface InterfaceDAO<T> {
	public int insert(T obj);
	public int update(T obj);
	public int delete(String id);
	public T selectById(String id);
	public List<T> selectAll(); 
	public List<T> selectByCondition(String condition,Object[] params);
	
}
