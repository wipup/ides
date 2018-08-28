package ports.soc.ides.controller;

import java.io.File;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;

import ports.soc.ides.config.IdesConfiguration;
import ports.soc.ides.config.IdesConfigurationException;
import ports.soc.ides.config.InitialConfigurationPropertyName;
import ports.soc.ides.config.util.JVMPropertyReader;
import ports.soc.ides.config.util.PropertyFileReader;
import ports.soc.ides.config.util.PropertyLoader;
import ports.soc.ides.config.util.QueuedPropertyReader;
import ports.soc.ides.exception.IdesException;
import ports.soc.ides.interceptor.annotation.IdesRoleAllowed;
import ports.soc.ides.logger.LoggerConfigurationFactory;
import ports.soc.ides.model.constant.Role;
import ports.soc.ides.util.FacesUtils;
import ports.soc.ides.util.IdesUtils;

@Named(value = "app")
@ApplicationScoped
public class ApplicationController extends AbstractIdesController {

	private static final long serialVersionUID = 82211928976378818L;
	
	private IdesConfiguration config;
	private String configurationFilePath;
	
	private boolean allowedDatabaseConfig;
	private boolean allowedSecurityConfig;
	
	@PostConstruct
	private void init() {
		FacesUtils.startLoggingSessionId();
		
		log.info("ApplicationController init");
		allowedDatabaseConfig = false;
		allowedSecurityConfig = false;
		try {
			loadProperty();
		} catch (Exception e) {
			if (e instanceof IdesException) {
				log.fatal(e);
				throw e;
			}
			log.fatal("error initialise application" ,e);
		}
	}

	private void loadProperty() {		
		//init logger
		LoggerContext logCtx = (LoggerContext) LogManager.getContext(false);
		if (logCtx.getConfiguration() instanceof DefaultConfiguration) {
			log.fatal("IDES logger is not running. Attempt to start IDES logger");
			try {
				logCtx.updateLoggers(LoggerConfigurationFactory.createConfiguration("LoggerConfigurationFactory", ConfigurationBuilderFactory.newConfigurationBuilder()));
				log.info("IDES logger is updating");
			} catch (Exception e) {
				System.out.print("IDES logger initialisation failed");
				e.printStackTrace();
			}
		}
		
		QueuedPropertyReader propReader = new QueuedPropertyReader();
		PropertyLoader jvmConfigReader = new JVMPropertyReader();
		configurationFilePath = jvmConfigReader.getOneStringProperty(InitialConfigurationPropertyName.APPLICATION_INITIAL_CONFIGURATION_FILE_LOCATION);

		// Read properties from the configuration file first, if file is not found, read from JVM
		// property settings
		if (IdesUtils.isEmpty(configurationFilePath)) {
			final String error = "Initial configuration property has not been set: " + InitialConfigurationPropertyName.APPLICATION_INITIAL_CONFIGURATION_FILE_LOCATION; 
			log.fatal(error);
			throw new IdesConfigurationException(error);
		}
		
		log.info("Read configuration file: " + configurationFilePath);
		File f = new File(configurationFilePath);
		if (!f.isFile() || !f.canRead()) {
			final String error = "Failed to load configuration settings at: " + f.getAbsolutePath();  
			log.fatal(error);
			throw new IdesConfigurationException(error);
		}
		
		PropertyLoader configFileReader = new PropertyFileReader(f);
		propReader.getPropertyReaders().add(configFileReader);
		propReader.getPropertyReaders().add(jvmConfigReader);
		
		//If logLcation is not set in JVM system property, find config in a file instead
		String logFileOutputLocation = jvmConfigReader.getOneStringProperty(InitialConfigurationPropertyName.LOGGER_OUTPUT_LOCATION);
		if (IdesUtils.isEmpty(logFileOutputLocation)) {
			log.warn("No log file location specified in JVM system property. Attempt to fix by acquiring location from file: " + configurationFilePath );
			logFileOutputLocation = configFileReader.getOneStringProperty(InitialConfigurationPropertyName.LOGGER_OUTPUT_LOCATION);
			
			if (!IdesUtils.isEmpty(logFileOutputLocation)) {
				File logDir = new File(logFileOutputLocation);
				if (logDir.isDirectory()) {
					System.setProperty(InitialConfigurationPropertyName.LOGGER_OUTPUT_LOCATION, logFileOutputLocation);
					logCtx.updateLoggers(LoggerConfigurationFactory.createConfiguration("LoggerConfigurationFactory", ConfigurationBuilderFactory.newConfigurationBuilder()));
					log.info("Logger reconfigured");
				}	
			} else {
				log.error("No log file location specified. Use default location.");
			}
		}
		
		
		String logLevel = configFileReader.getOneStringProperty(InitialConfigurationPropertyName.LOGGER_DEFAULT_LEVEL);
		if (!IdesUtils.isEmpty(logLevel)) {
			Level desiredLevel = Level.getLevel(logLevel.toUpperCase());
			if (desiredLevel == null) {
				desiredLevel = LoggerConfigurationFactory.DEFAULT_LOG_LEVEL;
				log.info("Invalid log level " + logLevel + ", set to default=" + desiredLevel);
			}
			if (desiredLevel != logCtx.getRootLogger().getLevel()) {
				Configurator.setRootLevel(desiredLevel);
				logCtx.updateLoggers();
				log.info("Log level has been changed to " + desiredLevel.name());
			}
		} 

		String delimiter = ",";
		delimiter = propReader.getOneStringProperty(InitialConfigurationPropertyName.PROPERTY_DELIMITER, delimiter);
		propReader.setPropertyDelimiter(delimiter);

		allowedDatabaseConfig = propReader.getOneBooleanProperty(InitialConfigurationPropertyName.ALLOW_DATABASE_CONFIG_WEB_INTERFACE, false);
		allowedSecurityConfig = propReader.getOneBooleanProperty(InitialConfigurationPropertyName.ALLOW_IDES_CONFIG_WEB_INTERFACE, false);
		
		log.info("allowed Database Config via admin page=" + allowedDatabaseConfig);
		log.info("allowed Security Config via admin page=" + allowedSecurityConfig);
		
		config = new IdesConfiguration();
		
		config.setAnnouncement("");
		config.setAutoDisplayAnnouncement(false);
		
		//for further development
		config.setEnableProfileDeletion(false);
		
		config.load(propReader);
		
		log.info("Log file output location: " + logFileOutputLocation);
		log.info("Log file name: " + LoggerConfigurationFactory.ACTIVE_LOG_FILE_NAME);
		log.info("Logger default level: " + LoggerConfigurationFactory.DEFAULT_LOG_LEVEL);
		
		config.printAllProperties();
	}

	@IdesRoleAllowed({Role.Administrator})
	public IdesConfiguration getConfig() {
		return config;
	}

	@IdesRoleAllowed({Role.Administrator})
	public void setConfig(IdesConfiguration config) {
		this.config = config;
	}
	
	public Set<Role> getRolesOfEmailAndDomain(String email, String domain) {
		return config.getRolesOfUser(email, domain);
	}

	public String getGoogleSignInclientId() {
		return config.getGoogleSignInclientId();
	}

	public String getConfigurationFilePath() {
		return configurationFilePath;
	}

	public boolean isAllowedDatabaseConfig() {
		return allowedDatabaseConfig;
	}
	
	public boolean isAllowedSecurityConfig() {
		return allowedSecurityConfig;
	}

	public boolean isAutoDisplayAnnouncement() {
		return config.isAutoDisplayAnnouncement();
	}

	public String getAnnouncement() {
		return config.getAnnouncement();
	}

	public boolean isEnableIdeaDeletion() {
		return config.isEnableIdeaDeletion();
	}

	public boolean isEnableOrgDeletion() {
		return config.isEnableOrgDeletion();
	}

	public boolean isEnableCaptcha() {
		return config.isEnableCaptcha();
	}

	public String getCaptchaPrivateKey() {
		return config.getCaptchaPrivateKey();
	}

	public String getCaptchaPublicKey() {
		return config.getCaptchaPublicKey();
	}

	public boolean isEnableProfileDeletion() {
		return config.isEnableProfileDeletion();
	}
}
