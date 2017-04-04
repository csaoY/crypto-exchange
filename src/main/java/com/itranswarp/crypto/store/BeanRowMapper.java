package com.itranswarp.crypto.store;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class BeanRowMapper<T> implements RowMapper<T> {

	EntityInfo<T> entityInfo;

	public BeanRowMapper(Class<T> entityClass) {
		this.entityInfo = new EntityInfo<>(entityClass);
	}

	@Override
	public T mapRow(ResultSet rs, int rowNum) throws SQLException {
		T bean = this.entityInfo.newInstance();
		ResultSetMetaData meta = rs.getMetaData();
		for (int i = 1; i <= meta.getColumnCount(); i++) {
			String name = meta.getColumnLabel(i);
			Object value = rs.getObject(i);
			this.entityInfo.setField(bean, name, value);
		}
		return bean;
	}

}
