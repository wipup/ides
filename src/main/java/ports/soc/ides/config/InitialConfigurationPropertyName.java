package ports.soc.ides.config;

public class InitialConfigurationPropertyName {

	//ides.property.fileLocation is only for JVM system property and Environment variable
	public static final String APPLICATION_INITIAL_CONFIGURATION_FILE_LOCATION = "ides.configuration.initial.fileLocation";  //required, JVM only
	public static final String APPLICATION_DATABASE_CONFIGURATION_FILE_LOCATION = "ides.configuration.database.fileLocation";  //required, JVM only
	
	//-------- IDES setting
	public static final String ALLOW_DATABASE_CONFIG_WEB_INTERFACE = "ides.adminPanel.enableDatabaseTab";
	public static final String ALLOW_IDES_CONFIG_WEB_INTERFACE = "ides.adminPanel.enableSecurityTab";
	
	public static final String PROPERTY_DELIMITER = "ides.property.delimiter";
	
	public static final String EMAIL_ADMINISTRATORS = "ides.role.administrator.email";
	public static final String EMAIL_STAFF = "ides.role.staff.email";
	public static final String EMAIL_STUDENTS = "ides.role.student.email";
	
	@Deprecated
	public static final String EMAIL_DOMAIN_ADMINSTRATOR = "ides.role.administrator.emailDomain";
	
	public static final String EMAIL_DOMAIN_STAFF = "ides.role.staff.emailDomain";	
	public static final String EMAIL_DOMAIN_STUDENTS = "ides.role.student.emailDomain";
	
	public static final String CAPTCHA_PRIVATE_KEY = "ides.captcha.privateKey";
	public static final String CAPTCHA_PUBLIC_KEY = "ides.captcha.publicKey";
	
	public static final String GOOGLE_SIGN_IN_CLIENT_ID = "ides.externalApi.googleSignIn.clientId";
	
	//--database settings
	public static final String DATABASE_CONNECTION_TYPE = "ides.database.connectionType";
	
	public static final String DATABASE_JDBC_DRIVER = "ides.database.jdbc.driverClass";
	public static final String DATABASE_JDBC_URL = "ides.database.jdbc.url";
	public static final String DATABASE_JDBC_USERNAME = "ides.database.jdbc.username";
	public static final String DATABASE_JDBC_PASSWORD = "ides.database.jdbc.password";
	
	public static final String DATABASE_JNDI_INIT_CONTEXT = "ides.database.jndi.initialContext";
	public static final String DATABASE_JNDI_DATASOURCE = "ides.database.jndi.datasource";
	
	
	//--------- logger
	
	public static final String LOG4J2_CONFIGURATION_FACTORY = "log4j.configurationFactory"; //required
	public static final String LOGGER_DEFAULT_LEVEL = "ides.log.level"; //preferrably JVM
	public static final String LOGGER_OUTPUT_LOCATION = "ides.log.outputLocation"; //preferrable  JVM
	
	
	
}
