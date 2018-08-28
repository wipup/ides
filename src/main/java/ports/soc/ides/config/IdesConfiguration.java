package ports.soc.ides.config;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ports.soc.ides.config.util.PropertyLoader;
import ports.soc.ides.model.User;
import ports.soc.ides.model.constant.Role;
import ports.soc.ides.util.IdesUtils;

public class IdesConfiguration extends AbstractConfiguration {

	private static final long serialVersionUID = -1160763844263519830L;

	public static final String PARAM_DONT_REDIRECT_IF_SESSION_EXPIRED = "ignoreRedirect";
	
	/**
	 * SESSION_TIMEOUT must equal to the value in web.xml
	 */
	public static final long DEFAULT_SESSION_TIMEOUT = 60; //60 minute
	public static final long DEFAULT_SESSION_TIMEOUT_IN_MILLISEC = DEFAULT_SESSION_TIMEOUT * 60 * 1000;
	
	private String googleSignInclientId;

	private Map<String, Set<Role>> userRoleMap; // key=email
	private Map<String, Set<Role>> domainRoleMap; // key=email domain (without @)

	private String announcement;
	
	private boolean autoDisplayAnnouncement;
	
	private boolean enableIdeaDeletion;
	private boolean enableOrgDeletion;
	private boolean enableProfileDeletion;

	private boolean enableCaptcha;
	private String captchaPrivateKey;
	private String captchaPublicKey;
	
	private String outputLogLocation;
	
	public IdesConfiguration() {
		announcement = "";
		autoDisplayAnnouncement = false;
		
		userRoleMap = new LinkedHashMap<>();
		domainRoleMap = new LinkedHashMap<>();
	}
	
	@Override
	public void load(PropertyLoader loader) {
		googleSignInclientId = loader.getOneStringProperty(InitialConfigurationPropertyName.GOOGLE_SIGN_IN_CLIENT_ID);
		if (IdesUtils.isEmpty(googleSignInclientId)) {
			final String error = "Google Sign-in Client ID has not been set: " + InitialConfigurationPropertyName.GOOGLE_SIGN_IN_CLIENT_ID;  
			log.fatal(error);
			throw new IdesConfigurationException(error);
		}
		
		mapEmailsToRole(userRoleMap, loader.getManyStringProperties(InitialConfigurationPropertyName.EMAIL_ADMINISTRATORS), Role.Administrator);
		mapEmailsToRole(userRoleMap, loader.getManyStringProperties(InitialConfigurationPropertyName.EMAIL_STAFF), Role.Staff);
		mapEmailsToRole(userRoleMap, loader.getManyStringProperties(InitialConfigurationPropertyName.EMAIL_STUDENTS), Role.Student);

		//mapEmailsToRole(domainRoleMap, loader.getManyStringProperties(InitialConfigurationPropertyName.EMAIL_DOMAIN_ADMINSTRATOR), Role.Administrator);
		mapEmailsToRole(domainRoleMap, loader.getManyStringProperties(InitialConfigurationPropertyName.EMAIL_DOMAIN_STAFF), Role.Staff);
		mapEmailsToRole(domainRoleMap, loader.getManyStringProperties(InitialConfigurationPropertyName.EMAIL_DOMAIN_STUDENTS), Role.Student);
		
		captchaPrivateKey = loader.getOneStringProperty(InitialConfigurationPropertyName.CAPTCHA_PRIVATE_KEY);
		captchaPublicKey = loader.getOneStringProperty(InitialConfigurationPropertyName.CAPTCHA_PUBLIC_KEY);
		
		if (!IdesUtils.isEmpty(captchaPublicKey) && !IdesUtils.isEmpty(captchaPrivateKey)) {
			enableCaptcha = true;
		}
		
		outputLogLocation = loader.getOneStringProperty(InitialConfigurationPropertyName.LOGGER_OUTPUT_LOCATION);
	}
	
	private void mapEmailsToRole(Map<String, Set<Role>> emailRoleMap, List<String> emails, Role role) {
		if (IdesUtils.isEmpty(emails)) {
			log.info("No value for mapping role=" + role);
			return;
		}
		for (String e : emails) {
			if (IdesUtils.isEmpty(e)) {
				continue;
			}
			Set<Role> roles = emailRoleMap.get(e);
			if (roles == null) {
				roles = new HashSet<>();
				emailRoleMap.put(e, roles);
			}
			log.info("Map Role=" + role.toString() + ", to value=" + e);
			roles.add(role);
		}
	}
	
	public IdesConfiguration clone() {
		IdesConfiguration newConfig = new IdesConfiguration();
		
		for(String key : this.userRoleMap.keySet()) {
			Set<Role> value = this.userRoleMap.get(key);
			Set<Role> newValue = new HashSet<Role>(value);
			newConfig.userRoleMap.put(key, newValue);
		}
		
		for(String key : this.domainRoleMap.keySet()) {
			Set<Role> value = this.domainRoleMap.get(key);
			Set<Role> newValue = new HashSet<Role>(value);
			newConfig.domainRoleMap.put(key, newValue);
		}
		
		newConfig.announcement = this.announcement;
		newConfig.autoDisplayAnnouncement = this.autoDisplayAnnouncement;
		newConfig.googleSignInclientId = this.googleSignInclientId;
		
		newConfig.enableIdeaDeletion = this.enableIdeaDeletion;
		newConfig.enableOrgDeletion = this.enableOrgDeletion;
		
		newConfig.enableCaptcha = this.enableCaptcha;
		newConfig.captchaPrivateKey = this.captchaPrivateKey;
		newConfig.captchaPublicKey = this.captchaPublicKey;
		
		newConfig.outputLogLocation = this.outputLogLocation;
		newConfig.enableProfileDeletion = this.enableProfileDeletion;
		
		return newConfig;
	}

	public void printAllProperties() {
		log.info("Announcement: display=" + autoDisplayAnnouncement + ", text=" + announcement);
		for (String key : userRoleMap.keySet()) {
			log.info("Email=" + key + " has role=" + userRoleMap.get(key).toString());
		}
		for (String key : domainRoleMap.keySet()) {
			log.info("Email Domain=" + key + " has role=" + domainRoleMap.get(key).toString());
		}
		log.info("Google Sign In API Client ID=" + googleSignInclientId);
		log.info("Enable captcha=" + enableCaptcha);
		log.info("Public captcha key=" + captchaPublicKey);
		log.info("Private captcha key=isEmpty()=" + IdesUtils.isEmpty(captchaPrivateKey) + "");
		log.info("Enable idea deletion=" + enableIdeaDeletion);
		log.info("Enable organisation deletion=" + enableOrgDeletion);
	}
	
	public Set<Role> getRolesOfUser(String email, String domain) {
		if (IdesUtils.isEmpty(email)) {
			return User.VISITOR_ROLES;
		}
		
		Set<Role> roles = new HashSet<>();
		
		email = email.toLowerCase();
		Set<Role> userRoles = userRoleMap.get(email);
		if (!IdesUtils.isEmpty(userRoles)) {
			roles.addAll(userRoles);
		}
		
		Set<Role> domainRoles = domainRoleMap.get(domain);
		if (!IdesUtils.isEmpty(domainRoles)) {
			roles.addAll(domainRoles);
		}
		
		if (roles.isEmpty()) {
			return User.VISITOR_ROLES;
		}
		
		return Collections.unmodifiableSet(roles);
	}
	
	public boolean isAutoDisplayAnnouncement() {
		return autoDisplayAnnouncement;
	}

	public void setAutoDisplayAnnouncement(boolean autoDisplayAnnouncement) {
		this.autoDisplayAnnouncement = autoDisplayAnnouncement;
	}

	public String getAnnouncement() {
		return announcement;
	}

	public void setAnnouncement(String announcement) {
		this.announcement = announcement;
	}

	public String getGoogleSignInclientId() {
		return googleSignInclientId;
	}

	public void setGoogleSignInclientId(String googleSignInclientId) {
		this.googleSignInclientId = googleSignInclientId;
	}

	public boolean isEnableIdeaDeletion() {
		return enableIdeaDeletion;
	}
	
	public void setEnableIdeaDeletion(boolean enableIdeaDeletion) {
		this.enableIdeaDeletion = enableIdeaDeletion;
	}

	public boolean isEnableOrgDeletion() {
		return enableOrgDeletion;
	}

	public void setEnableOrgDeletion(boolean enableOrgDeletion) {
		this.enableOrgDeletion = enableOrgDeletion;
	}

	public Map<String, Set<Role>> getUserRoleMap() {
		return userRoleMap;
	}

	public Map<String, Set<Role>> getDomainRoleMap() {
		return domainRoleMap;
	}

	public boolean isEnableCaptcha() {
		return enableCaptcha;
	}

	public void setEnableCaptcha(boolean enableCaptcha) {
		this.enableCaptcha = enableCaptcha;
	}

	public String getOutputLogLocation() {
		return outputLogLocation;
	}

	public void setOutputLogLocation(String outputLogLocation) {
		this.outputLogLocation = outputLogLocation;
	}

	public String getCaptchaPrivateKey() {
		return captchaPrivateKey;
	}

	public String getCaptchaPublicKey() {
		return captchaPublicKey;
	}

	public void setCaptchaPrivateKey(String captchaPrivateKey) {
		this.captchaPrivateKey = IdesUtils.trim(captchaPrivateKey);
	}

	public void setCaptchaPublicKey(String captchaPublicKey) {
		this.captchaPublicKey = IdesUtils.trim(captchaPublicKey);
	}

	public boolean isEnableProfileDeletion() {
		return enableProfileDeletion;
	}

	public void setEnableProfileDeletion(boolean enableProfileDeletion) {
		this.enableProfileDeletion = enableProfileDeletion;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("IdesConfiguration[googleSignInclientId=").append( googleSignInclientId )
		.append(", userRoleMap=").append( userRoleMap )
		.append(", domainRoleMap=").append( domainRoleMap )
		.append(", announcement=").append( announcement )
		.append(", autoDisplayAnnouncement=").append( autoDisplayAnnouncement )
		.append(", enableIdeaDeletion=").append( enableIdeaDeletion )
		.append(", enableOrgDeletion=").append( enableOrgDeletion )
		.append(", enableCaptcha=").append( enableCaptcha )
		.append(", captchaPrivateKey=isEmpty()=").append( IdesUtils.isEmpty(captchaPrivateKey) )
		.append(", captchaPublicKey=").append( captchaPublicKey )
		.append(", outputLogLocation=").append( outputLogLocation );
		return sb.toString();
	}

}
