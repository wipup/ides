package ports.soc.ides.model;

import java.util.Set;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;

import ports.soc.ides.model.constant.Role;

/**
 * - A class represents G-mail user (Google mail) Currently, only supports
 * Google account user - Currently not being stored in database
 * 
 * @author WIPU
 *
 */
public class GmailUser extends User {

	private static final long serialVersionUID = 7606259494918259119L;

	private String googleIdToken;

	private String familyName;
	private String givenName;

	private boolean emailVerified;
	private String locale;

	public GmailUser() {
		super();
		googleIdToken = null;
		familyName = null;
		givenName = null;
		imageUrl = null;
		emailVerified = false;
		locale = null;
	}

	public GmailUser(String name, String email, String domain, Payload payload, Set<Role> roles) {
		super(name, email, domain, roles);
		if (payload != null) {
			setEmailVerified(Boolean.valueOf(payload.getEmailVerified()));
			setImageUrl((String) payload.get("picture"));
			setLocale((String) payload.get("locale"));
			setFamilyName((String) payload.get("family_name"));
			setGivenName((String) payload.get("given_name"));
		}
	}

	public String getUserIdToken() {
		return googleIdToken;
	}

	public String getFamilyName() {
		return familyName;
	}

	public String getGivenName() {
		return givenName;
	}

	public boolean isEmailVerified() {
		return emailVerified;
	}

	public String getLocale() {
		return locale;
	}

	public void setUserIdToken(String userIdToken) {
		this.googleIdToken = userIdToken;
	}

	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}

	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}

	public void setEmailVerified(boolean emailVerified) {
		this.emailVerified = emailVerified;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public String getGoogleIdToken() {
		return googleIdToken;
	}

	public void setGoogleIdToken(String googleIdToken) {
		this.googleIdToken = googleIdToken;
	}

}
