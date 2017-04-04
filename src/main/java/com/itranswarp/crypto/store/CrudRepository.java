package com.itranswarp.crypto.store;

import java.util.List;

public interface CrudRepository<T> {

	List<T> selectAll();

	Page<T> selectByPage(int pageNumber);

	Page<T> selectByPage(int pageNumber, int pageSize);

	T selectById(Object id);

	void insert(@SuppressWarnings("unchecked") T... entities);

	void update(T entity);

	void delete(T entity);

	void delete(Class<T> clazz, Object id);

}
