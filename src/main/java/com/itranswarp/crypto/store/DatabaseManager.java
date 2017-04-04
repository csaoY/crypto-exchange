package com.itranswarp.crypto.store;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.itranswarp.crypto.user.User;

@Component
public class DatabaseManager {

	final Log log = LogFactory.getLog(getClass());

	@Autowired
	JdbcTemplate jdbcTemplate;

	Map<Class<?>, RowMapper<?>> rowMappers = new ConcurrentHashMap<>();

	public DatabaseManager() {
	}

	public void insert(Object entity) {
		EntityInfo<?> ei = new EntityInfo<>(entity.getClass());
		this.jdbcTemplate.update(ei.insertSql, ei.getInsertableFieldValues(entity));
	}

	public <T> void batchInsert(@SuppressWarnings("unchecked") T... entities) {
		if (entities.length > 0) {
			EntityInfo<?> ei = new EntityInfo<>(entities[0].getClass());
			List<Object[]> batchArgs = Arrays.stream(entities).map((entity) -> {
				return ei.getInsertableFieldValues(entity);
			}).collect(Collectors.toList());
			this.jdbcTemplate.batchUpdate(ei.insertSql, batchArgs);
		}
	}

	public int delete(Object entity) {
		EntityInfo<?> ei = new EntityInfo<>(entity.getClass());
		return this.jdbcTemplate.update(ei.deleteSql, ei.getIdFieldValue(entity));
	}

	public int delete(Class<?> entityClass, Object idValue) {
		EntityInfo<?> ei = new EntityInfo<>(entityClass);
		return this.jdbcTemplate.update(ei.deleteSql, idValue);
	}

	public void update(Object entity) {
		EntityInfo<?> ei = new EntityInfo<>(entity.getClass());
		Object[] args = ei.getUpdatableFieldValues(entity);
		Object[] updateArgs = new Object[args.length + 1];
		updateArgs[args.length] = ei.getIdFieldValue(entity);
		this.jdbcTemplate.update(ei.updateSql, updateArgs);
	}

	public void execute(String sql) {
		this.jdbcTemplate.execute(sql);
	}

	public int update(String sql, Object... args) {
		return this.jdbcTemplate.update(sql, args);
	}

	public <T> T queryForObject(Class<T> type, String sql, Object... args) {
		return this.jdbcTemplate.queryForObject(sql, getRowMapper(type), args);
	}

	public <T> List<T> queryForList(Class<T> type, String sql, Object... args) {
		return this.jdbcTemplate.query(sql, args, getRowMapper(type));
	}

	public Optional<Long> queryForLong(String sql, Object... args) {
		Long val = this.jdbcTemplate.queryForObject(sql, args, new RowMapper<Long>() {
			@Override
			public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
				Number number = (Number) rs.getObject(1);
				if (number == null) {
					return null;
				}
				return number.longValue();
			}
		});
		return Optional.ofNullable(val);
	}

	public void createTable(Class<?> entityClass) {
		EntityInfo<?> ei = new EntityInfo<>(entityClass);
		String sql = ei.toCreateTable();
		log.info(sql);
		execute(sql);
	}

	public void dropTable(Class<User> entityClass) {
		EntityInfo<?> ei = new EntityInfo<>(entityClass);
		String sql = String.format("DROP TABLE IF EXISTS `%s`", ei.tableName);
		log.info(sql);
		execute(sql);
	}

	@SuppressWarnings("unchecked")
	<T> RowMapper<T> getRowMapper(Class<T> clazz) {
		RowMapper<?> rowMapper = this.rowMappers.get(clazz);
		if (rowMapper == null) {
			rowMapper = new BeanPropertyRowMapper<>(clazz);
			rowMappers.put(clazz, rowMapper);
		}
		return (RowMapper<T>) rowMapper;
	}

}
