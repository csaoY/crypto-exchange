package com.itranswarp.crypto.store;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

public class EntityInfo<T> {

	static final Map<Class<?>, String> typeMapping = initTypeMapping();

	static Map<Class<?>, String> initTypeMapping() {
		Map<Class<?>, String> map = new HashMap<>();
		map.put(boolean.class, "BOOL");
		map.put(Boolean.class, "BOOL");
		map.put(byte.class, "INT");
		map.put(Byte.class, "INT");
		map.put(short.class, "INT");
		map.put(Short.class, "INT");
		map.put(int.class, "INT");
		map.put(Integer.class, "INT");
		map.put(long.class, "BIGINT");
		map.put(Long.class, "BIGINT");
		map.put(float.class, "FLOAT");
		map.put(Float.class, "FLOAT");
		map.put(double.class, "DOUBLE");
		map.put(Double.class, "DOUBLE");
		map.put(BigInteger.class, "DECIMAL(20,0)");
		map.put(BigDecimal.class, "DECIMAL(20,6)");
		return map;
	}

	public final Class<T> entityClass;
	public final String tableName;
	public final Constructor<T> constructor;

	public final Field[] allFields;
	public final String[] allFieldNames;

	public final Field idField;
	public final String idFieldName;

	public final Field[] insertableFields;
	public final String[] insertableFieldNames;

	public final Field[] updatableFields;
	public final String[] updatableFieldNames;

	public final Map<String, Field> fieldsMap;

	public final String selectSql;
	public final String insertSql;
	public final String updateSql;
	public final String deleteSql;

	public EntityInfo(final Class<T> entityClass) {
		this.entityClass = entityClass;
		this.tableName = getTableName(this.entityClass);
		this.constructor = findDefaultConstructor(this.entityClass);

		this.allFields = filter(entityClass.getFields(), EntityInfo::isPersistableField);
		this.allFieldNames = map(this.allFields, EntityInfo::getColumnName);

		// id:
		final Field[] idFields = filter(this.allFields, EntityInfo::isIdField);
		if (idFields.length != 1) {
			throw new IllegalArgumentException("Expected only 1 primary key but " + idFields.length + " found.");
		}
		this.idField = idFields[0];
		this.idFieldName = getColumnName(this.idField);

		this.insertableFields = filter(this.allFields, EntityInfo::isInsertableField);
		this.insertableFieldNames = map(this.insertableFields, EntityInfo::getColumnName);

		this.updatableFields = filter(this.allFields, EntityInfo::isUpdatableField);
		this.updatableFieldNames = map(this.updatableFields, EntityInfo::getColumnName);

		this.fieldsMap = toMap(this.allFields);

		this.selectSql = buildSelect(this.tableName, this.idFieldName, this.allFieldNames);
		this.insertSql = buildInsert(this.tableName, this.insertableFieldNames);
		this.updateSql = buildUpdate(this.tableName, this.idFieldName, this.updatableFieldNames);
		this.deleteSql = buildDelete(this.tableName, this.idFieldName);
	}

	static Field[] filter(Field[] fields, Predicate<? super Field> predicate) {
		return Arrays.stream(fields).filter(predicate).toArray(Field[]::new);
	}

	static String[] map(Field[] fields, Function<? super Field, ? extends String> mapper) {
		return Arrays.stream(fields).map(mapper).toArray(String[]::new);
	}

	static String[] map(String[] array, Function<? super String, ? extends String> mapper) {
		return Arrays.stream(array).map(mapper).toArray(String[]::new);
	}

	static String buildSelect(String tableName, String idName, String[] names) {
		return String.format("SELECT %s FROM `%s` WHERE `%s` = ?", String.join(", ", map(names, (name) -> {
			return String.format("`%s`", name);
		})), tableName, idName);
	}

	static String buildInsert(String tableName, String[] names) {
		return String.format("INSERT INTO `%s` (%s) VALUES (%s)", tableName, String.join(", ", map(names, (name) -> {
			return String.format("`%s`", name);
		})), String.join(", ", map(names, (name) -> {
			return "?";
		})));
	}

	static String buildUpdate(String tableName, String idName, String[] names) {
		return String.format("UPDATE `%s` SET %s WHERE `%s` = ?", tableName, String.join(", ", map(names, (name) -> {
			return String.format("`%s` = ?", name);
		})), idName);
	}

	static String buildDelete(String tableName, String idName) {
		return String.format("DELETE FROM `%s` WHERE `%s` = ?", tableName, idName);
	}

	static String getTableName(Class<?> clazz) {
		Table table = clazz.getAnnotation(Table.class);
		if (table == null) {
			return clazz.getSimpleName();
		}
		return table.name().isEmpty() ? clazz.getSimpleName() : table.name();
	}

	T newInstance() {
		try {
			return this.constructor.newInstance();
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	Constructor<T> findDefaultConstructor(Class<T> clazz) {
		try {
			return clazz.getConstructor();
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Is this field persistable.
	 */
	static boolean isPersistableField(Field f) {
		if (f.isAnnotationPresent(Transient.class)) {
			return false;
		}
		int mod = f.getModifiers();
		if (Modifier.isFinal(mod) || Modifier.isTransient(mod) || Modifier.isStatic(mod)) {
			return false;
		}
		getSqlType(f);
		return true;
	}

	/**
	 * Is this field marked as primary key.
	 */
	static boolean isIdField(Field f) {
		return f.isAnnotationPresent(Id.class);
	}

	/**
	 * Is this field insertable.
	 */
	static boolean isInsertableField(Field f) {
		Column c = f.getAnnotation(Column.class);
		if (c == null) {
			return true;
		}
		return c.insertable();
	}

	/**
	 * Is this field updatable.
	 */
	static boolean isUpdatableField(Field f) {
		if (isIdField(f)) {
			return false;
		}
		Column c = f.getAnnotation(Column.class);
		if (c == null) {
			return true;
		}
		return c.updatable();
	}

	Map<String, Field> toMap(Field[] fs) {
		Map<String, Field> map = new HashMap<>();
		Arrays.stream(fs).forEach((f) -> {
			map.put(getColumnName(f), f);
		});
		return map;
	}

	static String getColumnName(Field f) {
		Column c = f.getAnnotation(Column.class);
		if (c == null) {
			return f.getName();
		}
		return c.name().isEmpty() ? f.getName() : c.name();
	}

	public void setField(T bean, String name, Object value) {
		Field f = this.fieldsMap.get(name);
		try {
			f.set(bean, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public String toCreateTable() {
		StringBuilder sb = new StringBuilder(128);
		sb.append(String.format("CREATE TABLE `%s` (\n", this.tableName));
		for (Map.Entry<String, Field> entry : this.fieldsMap.entrySet()) {
			String name = entry.getKey();
			Field field = entry.getValue();
			boolean isNull = nullable(field);
			sb.append(String.format("  `%s` %s %s,\n", name, getSqlType(field), (isNull ? "NULL" : "NOT NULL")));
		}
		sb.append(String.format("  PRIMARY KEY (`%s`)\n", this.idFieldName));
		sb.append(") ENGINE=INNODB, DEFAULT CHARSET=UTF8");
		return sb.toString();
	}

	static String columnDefinition(Field f) {
		Column c = f.getAnnotation(Column.class);
		if (c == null || c.columnDefinition().isEmpty()) {
			return getSqlType(f);
		}
		return c.columnDefinition();
	}

	static boolean nullable(Field f) {
		Column c = f.getAnnotation(Column.class);
		if (c == null) {
			return true;
		}
		return c.nullable();
	}

	static String getSqlType(Field f) {
		String t = typeMapping.get(f.getType());
		if (t != null) {
			return t;
		}
		if (f.getType().equals(String.class)) {
			Column c = f.getAnnotation(Column.class);
			int length = c == null ? 255 : c.length();
			return "VARCHAR(" + length + ")";
		}
		throw new IllegalArgumentException("Unsupported type: " + f.getType().getName() + " of field: " + f.getName());
	}

	public Object[] getInsertableFieldValues(Object entity) {
		return getFieldValues(entity, this.insertableFields);
	}

	public Object[] getUpdatableFieldValues(Object entity) {
		return getFieldValues(entity, this.updatableFields);
	}

	Object getIdFieldValue(Object entity) {
		try {
			return this.idField.get(entity);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private static Object[] getFieldValues(Object entity, Field[] fields) {
		return Arrays.stream(fields).map((f) -> {
			try {
				return f.get(entity);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}).toArray(Object[]::new);
	}

}
