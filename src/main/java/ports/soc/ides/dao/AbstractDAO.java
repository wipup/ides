package ports.soc.ides.dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AbstractDAO {

	protected static final Logger log = LogManager.getRootLogger();
	
	protected SqlSessionProvider sqlSessionProvider;
	
	public AbstractDAO(SqlSessionProvider provider) {
		if (provider == null) {
			throw new NullPointerException("Sql Session Provider must not be null");
		}
		this.sqlSessionProvider = provider;
	}
	
	public void setProvider(SqlSessionProvider provider) {
		this.sqlSessionProvider = provider;
	}
	
	public SqlSessionProvider getProvider() {
		return sqlSessionProvider;
	}
}
