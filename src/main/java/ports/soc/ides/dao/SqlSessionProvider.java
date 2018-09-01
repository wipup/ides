package ports.soc.ides.dao;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.datasource.jndi.JndiDataSourceFactory;
import org.apache.ibatis.datasource.pooled.PooledDataSourceFactory;
import org.apache.ibatis.logging.log4j2.Log4j2Impl;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ports.soc.ides.config.DatabaseConfiguration;
import ports.soc.ides.config.DatasourceType;
import ports.soc.ides.config.InitialConfigurationPropertyName;
import ports.soc.ides.config.util.JVMPropertyReader;
import ports.soc.ides.config.util.PropertyFileReader;
import ports.soc.ides.config.util.PropertyLoader;
import ports.soc.ides.config.util.QueuedPropertyReader;
import ports.soc.ides.exception.IdesException;
import ports.soc.ides.util.IdesUtils;


@ApplicationScoped
public class SqlSessionProvider {

	public static final String PROPERTY_DRIVER_CLASS = "driver";
	public static final String PROPERTY_URL = "url";
	public static final String PROPERTY_USERNAME = "username";
	public static final String PROPERTY_PASSWORD = "password";

	private static final Logger log = LogManager.getRootLogger();
	
	private static final String[] xmlMappers = new String[] { 
			"/resources/mybatis/mapper/organisation-mapper.xml", 
			"/resources/mybatis/mapper/idea-mapper.xml", 
			"/resources/mybatis/mapper/profile-mapper.xml",
			"/resources/mybatis/mapper/extended-organisation-mapper.xml"
		};

	private SqlSessionFactory factory;

	private String databaseConfigFilePath;
	private DatabaseConfiguration config;

	@PostConstruct
	private void init() {
		log.info("Initialise database configuration");

		JVMPropertyReader jvmPropReader = new JVMPropertyReader();
		databaseConfigFilePath = jvmPropReader.getOneStringProperty(InitialConfigurationPropertyName.APPLICATION_DATABASE_CONFIGURATION_FILE_LOCATION);
		if (IdesUtils.isEmpty(databaseConfigFilePath)) {
			final String error = "Initial database configuration file has not been set: " + InitialConfigurationPropertyName.APPLICATION_DATABASE_CONFIGURATION_FILE_LOCATION;
			log.error(error);
			throw new IdesException(error);
		}

		log.info("Read database configuration file: " + databaseConfigFilePath);
		File f = new File(databaseConfigFilePath);
		if (!f.isFile() || !f.canRead()) {
			final String error = "Failed to load database configuration settings at " + f.getAbsolutePath();
			log.fatal(error);
			throw new IdesException(error);
		}

		PropertyLoader pReader = new PropertyFileReader(f);
		QueuedPropertyReader propReader = new QueuedPropertyReader();
		propReader.getPropertyReaders().add(pReader);
		propReader.getPropertyReaders().add(jvmPropReader);

		config = new DatabaseConfiguration();
		config.load(pReader);

		try {
			initSqlSessionFactory(config);
		} catch (Exception e) {
			log.error("Failed to initialised database connection configuration", e);
		}
	}

	/**
	 * Not thread safe
	 * @param dbConfig
	 * @throws IOException
	 */
	public void initSqlSessionFactory(DatabaseConfiguration dbConfig) throws IOException {
		log.debug("Load database configuration: " + dbConfig);
		DataSource dsc = null;
		if (DatasourceType.JNDI.equals(dbConfig.getDatasourceConnectionType())) {
			Properties prop = new Properties();
			prop.setProperty(JndiDataSourceFactory.INITIAL_CONTEXT, dbConfig.getInitialContext());
			prop.setProperty(JndiDataSourceFactory.DATA_SOURCE, dbConfig.getJndi());

			JndiDataSourceFactory factory = new JndiDataSourceFactory();
			factory.setProperties(prop);
			dsc = factory.getDataSource();

		} else if (DatasourceType.JDBC.equals(dbConfig.getDatasourceConnectionType())) {
			Properties prop = new Properties();
			prop.setProperty(PROPERTY_DRIVER_CLASS, dbConfig.getDriverClassName());
			prop.setProperty(PROPERTY_URL, dbConfig.getUrl());
			prop.setProperty(PROPERTY_USERNAME, dbConfig.getUsername());
			prop.setProperty(PROPERTY_PASSWORD, dbConfig.getPassword());

			PooledDataSourceFactory pf = new PooledDataSourceFactory();
			pf.setProperties(prop);
			dsc = pf.getDataSource();

		} else {
			throw new IllegalArgumentException("Invalid database settings");
		}

		Environment.Builder envBuilder = new Environment.Builder("production");
		TransactionFactory txfactory = new JdbcTransactionFactory();
		Environment env = envBuilder.transactionFactory(txfactory).dataSource(dsc).build();
		Configuration conf = new Configuration(env);
		conf.setLogImpl(Log4j2Impl.class);

		for (String mapper : xmlMappers) {
			log.debug("XML mapper: " + mapper);
			XMLMapperBuilder builder = null;
			try (InputStream ideaMapperStream = this.getClass().getResourceAsStream(mapper)) {
				builder = new XMLMapperBuilder(ideaMapperStream, conf, mapper, conf.getSqlFragments());
				builder.parse();
			}
		}

		SqlSessionFactory ss = new SqlSessionFactoryBuilder().build(conf);
		if (ss != null) {
			log.debug("produce SqlSessionFactory successfully");
		} else {
			log.error("produce SqlSessionFactory fail");
		}

		flushAllCache();
		this.factory = ss;
		config = dbConfig;
		log.info("SqlSessionFactory initialisation completed");
	}
	
	public void flushAllCache() {
		if (factory == null) {
			return;
		}
		try(SqlSession sql = factory.openSession()){
			sql.clearCache();
		}
	}
	
	public boolean testConnection(DatabaseConfiguration db) throws ClassNotFoundException, SQLException, NamingException {
		return testConnection(db, 0);
	}
	
	public boolean testConnection(DatabaseConfiguration db, int timeout) throws ClassNotFoundException, SQLException, NamingException{
		if (db == null) {
			return false;
		}
		log.info("Test database connection using: " + String.valueOf(db));
		if (db.getDatasourceConnectionType() == DatasourceType.JDBC) {
			String driverName = db.getDriverClassName();
			Class.forName(driverName);
			try (Connection con = DriverManager.getConnection(db.getUrl(), db.getUsername(), db.getPassword())) {
				return con.isValid(timeout);
			}
		} else if (db.getDatasourceConnectionType() == DatasourceType.JNDI) {
			String initialContext = db.getInitialContext();
			Context initCtx = new InitialContext();
			if (!IdesUtils.isEmpty(initialContext)) {
				initCtx = (Context) initCtx.lookup(initialContext);
			}
			DataSource ds = (DataSource) initCtx.lookup(db.getJndi());
			try (Connection con = ds.getConnection()){
				return con.isValid(timeout);
			}
		}
		
		return false;
	}

	/**
	 * MyBatis CDI module causes error in Glassfish.
	 * 
	 * @return
	 */
	public SqlSession getSqlSession() {
		return getSqlSession(true);
	}

	public SqlSession getSqlSession(boolean autoCommit) {
		if (factory == null) {
			throw new IdesException("Database Connection has not been initialised");
		}
		return factory.openSession(autoCommit);
	}

	public DatabaseConfiguration getConfig() {
		return config;
	}

	public String getDatabaseConfigFilePath() {
		return databaseConfigFilePath;
	}

	public void setDatabaseConfigFilePath(String databaseConfigFilePath) {
		this.databaseConfigFilePath = databaseConfigFilePath;
	}

}
