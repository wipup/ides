package ports.soc.ides.config;

import ports.soc.ides.util.IdesUtils;

public enum DatasourceType {
	JDBC, JNDI;

	public static DatasourceType getType(String name) {
		if (IdesUtils.isEmpty(name)) {
			return null;
		}
		name = name.toUpperCase();
		
		if (JDBC.toString().equals(name)) {
			return JDBC;
		} else if (JNDI.toString().equals(name)) {
			return JNDI;
		}
		return null;
	}
}
