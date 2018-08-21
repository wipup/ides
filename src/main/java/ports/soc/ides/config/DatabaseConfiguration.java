package ports.soc.ides.config;

import java.util.Objects;

import ports.soc.ides.config.util.PropertyLoader;
import ports.soc.util.IdesUtils;

public class DatabaseConfiguration extends AbstractConfiguration {

	private static final long serialVersionUID = 686421499244008738L;

	private DatasourceType datasourceConnectionType;

	private String driverClassName;
	private String url;
	private String username;
	private String password;

	private String initialContext;
	private String jndi;

	public DatabaseConfiguration clone() {		
		
		DatabaseConfiguration newConf = new DatabaseConfiguration();
		newConf.datasourceConnectionType = this.datasourceConnectionType;
		newConf.driverClassName = this.driverClassName;
		newConf.url = this.url;
		newConf.username = this.username;
		newConf.password = this.password;
		newConf.initialContext = this.initialContext;
		newConf.jndi = this.jndi;
		
		return newConf;
	}
	
	@Override
	public void load(PropertyLoader propReader) {
		String conType = propReader.getOneStringProperty(InitialConfigurationPropertyName.DATABASE_CONNECTION_TYPE);
		DatasourceType type = DatasourceType.getType(conType);
		if (type == null) {
			final String error = "Database connection type is invalid: " + conType;
			log.fatal(conType);
			throw new IdesConfigurationException(error);
		}
		
		datasourceConnectionType = type;
		driverClassName = propReader.getOneStringProperty(InitialConfigurationPropertyName.DATABASE_JDBC_DRIVER);
		url = propReader.getOneStringProperty(InitialConfigurationPropertyName.DATABASE_JDBC_URL);
		username = propReader.getOneStringProperty(InitialConfigurationPropertyName.DATABASE_JDBC_USERNAME);
		password = propReader.getOneStringProperty(InitialConfigurationPropertyName.DATABASE_JDBC_PASSWORD);
		jndi = propReader.getOneStringProperty(InitialConfigurationPropertyName.DATABASE_JNDI_DATASOURCE);
		initialContext = propReader.getOneStringProperty(InitialConfigurationPropertyName.DATABASE_JNDI_INIT_CONTEXT);		
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof DatabaseConfiguration)) {
			return false;
		}
		
		DatabaseConfiguration dbConfig = (DatabaseConfiguration) obj;
		if (dbConfig.datasourceConnectionType != this.datasourceConnectionType) {
			return false;
		}
		if (Objects.equals(dbConfig.datasourceConnectionType, DatasourceType.JDBC)) {
			if (!Objects.equals(dbConfig.password, this.password)
					|| !Objects.equals(dbConfig.username, this.username)
					|| !Objects.equals(dbConfig.url, this.url)
					|| !Objects.equals(dbConfig.driverClassName, this.driverClassName)) {
				return false;
			}
		} else if (dbConfig.datasourceConnectionType == DatasourceType.JNDI) {
			if (!Objects.equals(dbConfig.jndi, this.jndi)
					|| !Objects.equals(dbConfig.initialContext, this.initialContext)) {
				return false;
			}
		}		
		
		return true;
	}

	
	public String getUrl() {
		return url;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getInitialContext() {
		return initialContext;
	}

	public String getJndi() {
		return jndi;
	}

	public void setUrl(String url) {
		this.url = IdesUtils.trim(url);
	}

	public void setUsername(String username) {
		this.username = IdesUtils.trim(username);
	}

	public void setPassword(String password) {
		this.password = IdesUtils.trim(password);
	}

	public void setInitialContext(String initialContext) {
		this.initialContext = IdesUtils.trim(initialContext);
	}

	public void setJndi(String jndi) {
		this.jndi = IdesUtils.trim(jndi);
	}

	public String getDriverClassName() {
		return driverClassName;
	}

	public void setDriverClassName(String driverClassName) {
		this.driverClassName = IdesUtils.trim(driverClassName);
	}

	public DatasourceType getDatasourceConnectionType() {
		return datasourceConnectionType;
	}

	public void setDatasourceConnectionType(DatasourceType datasourceConnectionType) {
		this.datasourceConnectionType = datasourceConnectionType;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("DatabaseConfiguration[datasourceConnectionType=").append( datasourceConnectionType )
			.append(", driverClassName=").append( driverClassName )
			.append(", url=").append( url )
			.append(", username=").append( username )
			.append(", password=isEmpty()=").append( IdesUtils.isEmpty(password) )
			.append(", initialContext=").append( initialContext )
			.append(", jndi=").append( jndi ).append("]");
		return sb.toString();
	}
}
