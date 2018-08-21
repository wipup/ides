package ports.soc.ides.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.enterprise.event.Reception;
import javax.faces.event.ActionEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.primefaces.PrimeFaces;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import ports.soc.ides.config.DatabaseConfiguration;
import ports.soc.ides.config.DatasourceType;
import ports.soc.ides.config.IdesConfiguration;
import ports.soc.ides.controller.event.IdesEvent;
import ports.soc.ides.controller.event.PageChangeEvent;
import ports.soc.ides.controller.fragment.OrganisationDisplayController;
import ports.soc.ides.controller.fragment.OrganisationFormController;
import ports.soc.ides.controller.helper.FileWrapper;
import ports.soc.ides.controller.helper.IdesPage;
import ports.soc.ides.controller.helper.LogFileFilter;
import ports.soc.ides.dao.ContactProfileDAO;
import ports.soc.ides.dao.SqlSessionProvider;
import ports.soc.ides.interceptor.annotation.IdesRoleAllowed;
import ports.soc.ides.interceptor.annotation.LogPerformance;
import ports.soc.ides.logger.LoggerConfigurationFactory;
import ports.soc.ides.model.ContactProfile;
import ports.soc.ides.model.DataModel;
import ports.soc.ides.model.Organisation;
import ports.soc.ides.model.User;
import ports.soc.ides.model.constant.OwnerType;
import ports.soc.ides.model.constant.Role;
import ports.soc.util.FacesUtils;
import ports.soc.util.IdesUtils;

@Named("admin")
@ViewScoped
public class AdministratorPanelController extends AbstractIdesController {

	private static final long serialVersionUID = 7302993200848091435L;
	
	public static final String PARAM_CALLBACK_SUCCESS = "callbackSuccess";

	@Inject
	private ApplicationController app;

	@Inject
	private SqlSessionProvider sqlProvider;
	
	@Inject
	private OrganisationFormController orgForm;
	@Inject
	private OrganisationDisplayController showOrg;

	private IdesConfiguration config;
	private String idesConfigPath;

	private DatabaseConfiguration dbConfig;
	private String dbConfigPath;

	private List<FileWrapper> logFiles;
	private FileWrapper selectedLogFile;

	private Level logLevel;
	private boolean logIpAddress;
	private List<Driver> driverList;
	private Driver jdbcDriverClassName;
	
	private Set<String> profileOwners;
	private String selectedProfileDomain;
	private ContactProfile selectedContactProfile;
	
	private boolean editContactProfile;
	private boolean showDeleteProfileButton;
	
	@PostConstruct
	public void init() {
		log.trace("AdministratorPanelController init");
		
		UserController u = getNamedController(UserController.class);
		User user = u.getUser();
		if (user == null) {
			redirectOutOfPage();
			return;
		}
		if (!user.hasRole(Role.Administrator)) {
			redirectOutOfPage();
			return;
		}

		cloneSettings();
		onRefreshLogFolder(null);
	}

	@LogPerformance(note = "download a log file")
	@IdesRoleAllowed({Role.Administrator})
	public StreamedContent getDownloadableFile() {
		if (selectedLogFile == null) {
			return null;
		}
		File f = selectedLogFile.getFile();
		InputStream stream = null;
		try {
			stream = new FileInputStream(f);
			log.info("Sending log file: " + f.getName());
			return new DefaultStreamedContent(stream, "application/octet-stream", f.getName());
		} catch (FileNotFoundException e) {
			log.error(e);
			addMessageError("Error", "Unable to download file");
			addMessageError(e.getMessage(), "");
		}
		return null;
	}

	@LogPerformance(level = "DEBUG")
	@IdesRoleAllowed({Role.Administrator})
	public void onRefreshLogFolder(ActionEvent event) {
		try {
			selectedLogFile = null;
			logFiles = new ArrayList<>();
			String logFolder = config.getOutputLogLocation();
			if (!IdesUtils.isEmpty(logFolder)) {
				File outputFolder = new File(logFolder);
				if (outputFolder.isDirectory()) {
					File[] files = outputFolder.listFiles(new LogFileFilter());
					for (int i = files.length - 1; i >= 0; i--) {
						//descending order to make the most recent files come first
						File f = files[i];
						FileWrapper fw = new FileWrapper(f);
						logFiles.add(fw);
					}
				}
			}
		} catch (Exception e) {
			log.error("Error occured while refreshing list of log files", e);
			addAllExceptionCausesToMessage(e);
		}
	}

	@LogPerformance
	@IdesRoleAllowed({Role.Administrator})
	public void onApplyChange(ActionEvent event) {
		log.debug("Applying new setting");

		boolean updateSettingSuccess = true;

		log.info("Old Ides config=" + String.valueOf(app.getConfig()));
		log.info("New Ides config=" + String.valueOf(config));

		log.info("Old DB config=" + String.valueOf(sqlProvider.getConfig()));
		log.info("New DB config=" + String.valueOf(dbConfig));

		// General Tab
		if (config.isAutoDisplayAnnouncement()) {
			String content = config.getAnnouncement();
			if (IdesUtils.isEmpty(content)) {
				addMessageWarn("Announcement content is empty", "");
			}
		}

		// Security Tab
		if (config.isEnableCaptcha()) {
			String privateKey = config.getCaptchaPrivateKey();
			String publicKey = config.getCaptchaPublicKey();

			if (IdesUtils.isEmpty(publicKey) || IdesUtils.isEmpty(privateKey)) {
				updateSettingSuccess = false;
				addMessageError("Error", "Captcha private key and public key must not be empty if enable!");
			}
		}

		if (IdesUtils.isEmpty(config.getGoogleSignInclientId())) {
			updateSettingSuccess = false;
			addMessageError("Error", "Google Sign-In Client ID must not be empty");
		}

		// Datasource tab
		DatasourceType dsType = dbConfig.getDatasourceConnectionType();
		if (DatasourceType.JDBC == dsType) {
			// Driver class is selected based on protocol
			if (IdesUtils.isEmpty(dbConfig.getDriverClassName())) {
				addMessageWarn("Warning", "Driver class is not specified. The system will choose a driver automatically but the connection may be failed.");
			}
			if (IdesUtils.isEmpty(dbConfig.getUrl())) {
				updateSettingSuccess = false;
				addMessageError("Error", "Database URL must not be empty");
			}
			if (IdesUtils.isEmpty(dbConfig.getUsername())) {
				updateSettingSuccess = false;
				addMessageError("Error", "Database username must not be empty");
			}
			if (IdesUtils.isEmpty(dbConfig.getPassword())) {
				updateSettingSuccess = false;
				addMessageError("Error", "Database password must not be empty");
			}
		} else if (DatasourceType.JNDI == dsType) {
			if (IdesUtils.isEmpty(dbConfig.getJndi())) {
				updateSettingSuccess = false;
				addMessageError("Error", "JNDI name must not be empty");
			}
		} else {
			addMessageError("Error", "Unknown datasource connection method");
			updateSettingSuccess = false;
		}

		// Logging tab
		if (logLevel == null) {
			addMessageError("Error", "Log level must not be null");
			updateSettingSuccess = false;
		}

		// before perform update
		if (!updateSettingSuccess) {
			addMessageError("Failed", "Configuration not updated");
			return;
		}

		// update general & security
		app.setConfig(config);
		config = config.clone();

		// update database
		DatabaseConfiguration currentConfig = sqlProvider.getConfig();
		log.debug("Dbconfig Equal=" + (currentConfig.equals(dbConfig)));
		// only reload database config if change made
		if (!currentConfig.equals(dbConfig)) {
			try {
				sqlProvider.testConnection(dbConfig);
			} catch (Exception e) {
				addMessageWarn("Warning", "Unable to connect to the database with the new setting");
			}

			try {
				sqlProvider.initSqlSessionFactory(dbConfig); // expensive
				dbConfig = sqlProvider.getConfig().clone();
				addMessageInfo("Database setting has been updated successfully", "");
			} catch (IOException e) {
				log.error(e);
				updateSettingSuccess = false;
				addMessageError("Error", "Failed to update database setting");
				addMessageError(e.getMessage(), "");
			}
		}

		// update log
		try {
			LoggerContext logCtx = (LoggerContext) LogManager.getContext(false);
			Logger logger = logCtx.getRootLogger();
			log.debug("Current Log level: " + logger.getLevel());
			log.debug("Desired log level: " + logLevel);

			if (!logger.getLevel().equals(logLevel) || LoggerConfigurationFactory.LOG_IP_ADDRESS != logIpAddress) {
				
				Configurator.setRootLevel(logLevel);
				if (LoggerConfigurationFactory.LOG_IP_ADDRESS != logIpAddress) {
					LoggerConfigurationFactory.LOG_IP_ADDRESS = logIpAddress;
					logCtx.updateLoggers(LoggerConfigurationFactory.createConfiguration("LoggerConfigurationFactory", ConfigurationBuilderFactory.newConfigurationBuilder()));
					addMessageInfo("Success", "Logging IP Address has been set to " + logIpAddress);
				} else {
					logCtx.updateLoggers(LoggerConfigurationFactory.createConfiguration("LoggerConfigurationFactory", ConfigurationBuilderFactory.newConfigurationBuilder()));
				}
				
				addMessageInfo("Success", "Log level has been set to " + logLevel.toString());
				log.info("Log level changed to: " + logLevel);
				log.info("Log IP Address: " + logIpAddress);
			}
			if (logLevel == Level.OFF) {
				addMessageWarn("Warning", "Logging system has been turned off");
			}
		} catch (Exception e) {
			updateSettingSuccess = false;
			log.error("Error updating logger", e);
			addMessageError(e.getMessage(), "");
		}

		if (updateSettingSuccess) {
			log.info("All settings have been updated");
			addMessageInfo("Success", "All settings have been updated successfully");
		}
	}

	@LogPerformance
	@IdesRoleAllowed({Role.Administrator})
	public void onTestConnection(ActionEvent event) {
		boolean valid = false;
		Throwable configError = null;
		try {
			valid = sqlProvider.testConnection(dbConfig);
		} catch (Exception e) {
			configError = e;
			valid = false;
			log.error("Error testing connection of: " + dbConfig.toString(), e);
		}

		if (valid) {
			addMessageInfo("Success", "Test connection succeeded");
		} else {
			addMessageError("Failed", "Test connection failed");
			addAllExceptionCausesToMessage(configError);
		}
	}

	@LogPerformance
	@IdesRoleAllowed({Role.Administrator})
	public void onReloadSecurityConfig(ActionEvent event) {
		log.info("Reinitiating IDES initial configuration from file: " + idesConfigPath);
		try {
			File f = new File(idesConfigPath);
			config.load(f);
			addMessageInfo("Success", "Reinitiated IDES configuration successfully");
			addMessageInfo("", "Please review the setting and click 'Apply All' button to confirm");
			log.info("Reinitiating IDES initial configuration completed");
		} catch (Exception e) {
			log.error("Failed to reinitiate IDES configuration from file: " + idesConfigPath, e);
			addMessageError("Failed", "Error occured when reading file " + idesConfigPath);
			addAllExceptionCausesToMessage(e);
		}
	}

	@LogPerformance
	@IdesRoleAllowed({Role.Administrator})
	public void onReloadDatabaseConfig(ActionEvent event) {
		log.info("Reinitiating database configuration from file: " + dbConfigPath);
		try {
			File f = new File(dbConfigPath);
			dbConfig.load(f);
			addMessageInfo("Success", "Reinitiated database configuration successfully");
			addMessageInfo("", "Please test the database connection and click 'Apply All' button to confirm");
			log.info("Reinitiating database configuration completed");
		} catch (Exception e) {
			log.error("Failed to reinitiate database configuration from file: " + dbConfigPath, e);
			addMessageError("Failed", "Error occured when reading file " + dbConfigPath);
			addAllExceptionCausesToMessage(e);
		}
	}
	
	@LogPerformance
	@IdesRoleAllowed({Role.Administrator})
	public void onClickEditProfile(ActionEvent event) {
		//Edit and create new profile
		if (IdesUtils.isEmpty(profileOwners)) {
			editContactProfile = false;
			return;
		}
		
		if (selectedContactProfile == null) {
			ContactProfile profile = new ContactProfile();
			profile.setOwner(selectedProfileDomain);
			orgForm.initDefault(profile);
			showDeleteProfileButton = false;
		} else {
			showDeleteProfileButton = true;
			orgForm.initDefault(selectedContactProfile);
		}
		editContactProfile = true;
		
		orgForm.setRenderEmailAddress(false);
		orgForm.setRenderContactName(false);
		orgForm.setRequireOrganisationName(false);
		orgForm.setRequirePostalAddress(false);
		orgForm.setRenderTelephoneNumber(false);
	}
	
	@LogPerformance(level = "TRACE")
	@IdesRoleAllowed({Role.Administrator})
	public void onClickCancelCreatingProfile(ActionEvent event) {
		editContactProfile = false;
		PrimeFaces.current().ajax().addCallbackParam(PARAM_CALLBACK_SUCCESS, true);
	}
	
	@LogPerformance
	@IdesRoleAllowed({Role.Administrator})
	public void onClickDeleteProfile(ActionEvent event) {
		Organisation org = showOrg.getOrganisation();
		if (!(org instanceof ContactProfile)) {
			log.error("The profile to be deleted is invalid: " + DataModel.printDetail(org));
			addMessageError("Error", "Invalid profile. Please refresh the page");
			return;
		}
		
		ContactProfile prof = (ContactProfile) org;
		String owner = prof.getOwner();
		try {			
			ContactProfileDAO dao = new ContactProfileDAO(sqlProvider);
			dao.deleteProfile(prof);
			
			addMessageInfo("Success", "Profile has been deleted successfully");
			PrimeFaces.current().ajax().addCallbackParam(PARAM_CALLBACK_SUCCESS, true);
		} catch (Exception e) {
			log.error("Error deleting profile: " + DataModel.printDetail(prof), e);
			addMessageError("Error", "Failed to delete the requested profile of domain " + prof.getOwner() );
			addAllExceptionCausesToMessage(e);
			PrimeFaces.current().ajax().addCallbackParam(PARAM_CALLBACK_SUCCESS, false);
		} 
		
		showOrg.initDefault();
		orgForm.initDefault();
		setSelectedProfileDomain(owner);
	}

	
	@LogPerformance
	@IdesRoleAllowed({Role.Administrator})
	public void onClickSaveProfile(ActionEvent event) {
		Organisation org = orgForm.getOrganisation();
		if (!(org instanceof ContactProfile)) {
			log.error("The profile to be saved is invalid: " + DataModel.printDetail(org));
			addMessageError("Error", "Invalid profile. Please try recreating new profile.");
			return ;
		}
		orgForm.trimAllFields();
		ContactProfile profile = (ContactProfile) org;
		
		try {
			ContactProfileDAO dao = new ContactProfileDAO(sqlProvider);
			
			if (profile.getOwnerType() == null || profile.getId() <= 0) { 
				//new profile
				profile.setOwnerType(OwnerType.DOMAIN);
				dao.insertProfile(profile);
				addMessageInfo("Success", "Profile for " + profile.getOwner() + " has been created");
			} else {
				dao.updateProfile(profile);
				addMessageInfo("Success", "Profile for " + profile.getOwner() + " has been updated");
			}
			
			setSelectedProfileDomain(profile.getOwner());
			
			PrimeFaces.current().ajax().addCallbackParam(PARAM_CALLBACK_SUCCESS, true);
			editContactProfile = false;
		} catch (Exception e) {
			log.error("Error saving/editing profile: " + DataModel.printDetail(profile), e);
			addMessageError("Error", "Failed to process profile");
			addAllExceptionCausesToMessage(e);
			PrimeFaces.current().ajax().addCallbackParam(PARAM_CALLBACK_SUCCESS, false);
		}
		
	}

	@LogPerformance
	@IdesRoleAllowed({Role.Administrator})
	public void onClickRevert(ActionEvent event) {
		try {
			log.debug("Revert setting: retrieving current active configuration");
			cloneSettings();
			log.info("Revert setting completed");
			addMessageInfo("Success", "All input fields have been reset");
		} catch (Exception e) {
			log.error("Error on reverting configuration", e);
			addMessageError("Error occurred while reloading configuriation", "");
			addAllExceptionCausesToMessage(e);
		}
	}

	private void redirectOutOfPage() {
		try {
			FacesUtils.forceClearCache();
			FacesUtils.redirectToWelcomePage();
			return;
		} catch (IOException e) {
			log.error(e);
		}
	}

	private void cloneSettings() {
		orgForm.initDefault();
		showOrg.initDefault();
		editContactProfile = false;
		showDeleteProfileButton = false;
		
		this.config = app.getConfig().clone();
		this.idesConfigPath = app.getConfigurationFilePath();

		this.dbConfig = sqlProvider.getConfig().clone();
		this.dbConfigPath = sqlProvider.getDatabaseConfigFilePath();

		LoggerContext logCtx = (LoggerContext) LogManager.getContext(false);
		Logger logger = logCtx.getRootLogger();
		logLevel = logger.getLevel();
		log.debug("clone settings, log level=" + logLevel);
		logIpAddress = LoggerConfigurationFactory.LOG_IP_ADDRESS;

		loadDriverList();
		
		//list all staff email domains
		profileOwners = new LinkedHashSet<>();
		log.debug("List all email and domain that declared role staff");
		Set<String> domainEmails = config.getDomainRoleMap().keySet();
		for(String s : domainEmails) {
			Set<Role> roles = config.getDomainRoleMap().get(s);
			if (roles.contains(Role.Staff) && !profileOwners.contains(s)) {
				log.debug("Found declared staff domain: " + s);
				profileOwners.add(s);
				if (selectedProfileDomain == null) {
					selectedProfileDomain = s;
				}
			}
		}
		
		Set<String> emails = config.getUserRoleMap().keySet();
		for(String s : emails) {
			Set<Role> roles = config.getUserRoleMap().get(s);
			if (IdesUtils.isEmpty(roles)) {
				continue;
			}
			int indexOfA = s.indexOf('@');
			if (roles.contains(Role.Staff) && indexOfA >= 0 && indexOfA < s.length() && !profileOwners.contains(s)) {
				log.debug("Found declared staff email: " + s);
				String domain = s.substring(s.indexOf('@') + 1);
				profileOwners.add(domain);
				if (selectedProfileDomain == null) {
					selectedProfileDomain = domain;
				}
			}
		}
		
		if (selectedProfileDomain != null) {
			fetchContactProfile();
		}
		
		log.debug("Admin security tab enable=" + app.isAllowedSecurityConfig());
		log.debug("Admin database tab enable=" + app.isAllowedDatabaseConfig());
	}

	private void loadDriverList() {
		driverList = new ArrayList<>();
		Enumeration<Driver> drivers = DriverManager.getDrivers();
		while (drivers.hasMoreElements()) {
			Driver d = drivers.nextElement();
			String className = d.getClass().getName();
			if (className.equals(dbConfig.getDriverClassName())) {
				jdbcDriverClassName = d;
			}
			driverList.add(d);
		}
	}

	public void onIdesEvent(@Observes(notifyObserver = Reception.IF_EXISTS) IdesEvent e) {
		if (e instanceof PageChangeEvent) {
			PageChangeEvent pe = (PageChangeEvent) e;
			if (pe.isEventSuccess() && pe.getPageAfter() == IdesPage.ADMIN_TOOLS) {
				cloneSettings();
			}
		} 
//		else if (e instanceof UserSignOutEvent && e.isEventSuccess()) {
//			NavigationController nav = getNamedController(NavigationController.class);
//			if (nav.getCurrentPage() == IdesPage.ADMIN_TOOLS) {
//				nav.navigateTo(NavigationController.DEFAULT_PAGE);
//			}
//		}
	}
	
	private void fetchContactProfile() {
		log.debug("SelectedProfileDomain value=" + selectedProfileDomain);
		if (IdesUtils.isEmpty(selectedProfileDomain)) {
			return;
		}
		
		try {
			ContactProfileDAO dao = new ContactProfileDAO(sqlProvider);
			List<ContactProfile> prof =	dao.selectProfile(selectedProfileDomain, OwnerType.DOMAIN);
			if (!prof.isEmpty()) {
				selectedContactProfile = prof.get(prof.size() - 1);
			}
			log.trace("selectedContactProfile=" + DataModel.printDetail(selectedContactProfile));
		} catch (Exception e) {
			addMessageError("Error", "Failed to query profile of " + selectedProfileDomain);
			log.error("Error loading contact profile of " + selectedProfileDomain, e);
		} 
		
		if (selectedContactProfile != null) {
			showOrg.initDefault(selectedContactProfile);
		} else {
			showOrg.initDefault();
		}
		
		showOrg.setRenderContact(false);
		showOrg.setRenderEmail(false);
		showOrg.setRenderTelephone(false);
		showOrg.setRenderId(false);
	}

	@LogPerformance
	public void setSelectedProfileDomain(String selectedProfileDomain) {
		this.selectedProfileDomain = selectedProfileDomain;	
		this.selectedContactProfile = null;
		fetchContactProfile();
	}
	

	public Driver getJdbcDriverClassName() {
		return jdbcDriverClassName;
	}

	public void setJdbcDriverClassName(Driver jdbcDriverClassName) {
		this.jdbcDriverClassName = jdbcDriverClassName;
		dbConfig.setDriverClassName(jdbcDriverClassName.getClass().getName());
	}

	public IdesConfiguration getConfig() {
		return config;
	}

	public void setConfig(IdesConfiguration config) {
		this.config = config;
	}

	public DatabaseConfiguration getDbConfig() {
		return dbConfig;
	}

	public void setDbConfig(DatabaseConfiguration dbConfig) {
		this.dbConfig = dbConfig;
	}

	public String getIdesConfigPath() {
		return idesConfigPath;
	}

	public String getDbConfigPath() {
		return dbConfigPath;
	}

	public void setIdesConfigPath(String idesConfigPath) {
		this.idesConfigPath = idesConfigPath;
	}

	public void setDbConfigPath(String dbConfigPath) {
		this.dbConfigPath = dbConfigPath;
	}

	public List<FileWrapper> getLogFiles() {
		return logFiles;
	}

	public void setLogFiles(List<FileWrapper> logFiles) {
		this.logFiles = logFiles;
	}

	public FileWrapper getSelectedLogFile() {
		return selectedLogFile;
	}

	public void setSelectedLogFile(FileWrapper selectedLogFile) {
		this.selectedLogFile = selectedLogFile;
	}

	public Level getLogLevel() {
		return logLevel;
	}

	public void setLogLevel(Level logLevel) {
		this.logLevel = logLevel;
	}

	public List<Driver> getDriverList() {
		return driverList;
	}

	public Set<String> getProfileOwners() {
		return profileOwners;
	}

	public String getSelectedProfileDomain() {
		return selectedProfileDomain;
	}

	public void setProfileOwners(Set<String> profileOwners) {
		this.profileOwners = profileOwners;
	}	
	
	public ContactProfile getSelectedContactProfile() {
		return selectedContactProfile;
	}
	
	public boolean isEditContactProfile() {
		return editContactProfile;
	}
	
	public boolean isShowDeleteProfileButton() {
		return showDeleteProfileButton;
	}
	
	public boolean isLogIpAddress() {
		return logIpAddress;
	}
	
	public void setLogIpAddress(boolean logIpAddress) {
		this.logIpAddress = logIpAddress;
	}
}
