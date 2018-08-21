package ports.soc.ides.controller;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Event;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.PrimeFaces;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;

import ports.soc.ides.controller.event.IdesEvent;
import ports.soc.ides.controller.event.UserSignInEvent;
import ports.soc.ides.controller.event.UserSignOutEvent;
import ports.soc.ides.controller.helper.IdesPage;
import ports.soc.ides.exception.IdesException;
import ports.soc.ides.interceptor.annotation.LogPerformance;
import ports.soc.ides.model.GmailUser;
import ports.soc.ides.model.User;
import ports.soc.ides.model.constant.Role;
import ports.soc.ides.util.FacesUtils;
import ports.soc.ides.util.IdesUtils;

@Named(value = "auth")
@SessionScoped
public class UserController extends AbstractIdesController {

	private static final long serialVersionUID = 8114413403902019534L;

	public static final String PARAM_CALLBACK_LOGGED_IN = "loggedIn";
	
	public static final String PARAM_ID_TOKEN = "idToken";
	public static final String PARAM_ID_EMAIL = "idEmail";

	private User user = User.ANONYMOUS;
	
	@Inject
	private ApplicationController app;
	
	@Inject
	private Event<IdesEvent> idesEvent;

	@PostConstruct
	public void init() {
		log.trace("UserController init");
	}
	
	@LogPerformance
	public void actionSignIn(ActionEvent event) throws Throwable {
		String idToken = null;
		String email = null;
		
		UserSignInEvent uso = new UserSignInEvent();
		uso.setEventSuccess(false);
		uso.setEventSource(this);
		
		try {
			log.info("User requested to sign in");
			
			email = getParameter(PARAM_ID_EMAIL);
			idToken = getParameter(PARAM_ID_TOKEN);
			
			log.trace("User requested to sign in with email: " + email);
			
			if (!IdesUtils.isEmpty(idToken)) {
				User prevUser = user;
				
				user = processGoogleIdToken(idToken);
				uso.setEventSuccess(true);
				uso.setUser(user);
				
				log.info("sign in completed successfully");
				if (!user.equals(prevUser) && isLoggedIn()) {
					addMessageInfo("Success", "Signed in as " + user.getName() + " (" + Role.getRoleForDisplaying(user.getRoles()) + ")");
				}
			} else {
				addMessageError("Unable to sign in", "Sent credential is empty");
			}
			
		} catch (IdesException e) {
			addMessageError(e.getMessageHeader(), e.getMessageContent());
			user = User.ANONYMOUS;
		} catch (GeneralSecurityException | IOException e) {
			user = User.ANONYMOUS;
			log.error("Error when signing in: " + idToken, e);
			addMessageError("Error", "Unable to verify your account");
		} catch (Exception e) {
			user = User.ANONYMOUS;
			log.error("Unknown error when signing in: " + idToken, e);
			addMessageError("Fail", "Unknown error occurred");
		} catch (Throwable e) {
			user = User.ANONYMOUS;
			// Glassfish 5 has a bug about Security method with Java version 1.8.0_171 and some above
			// This block is for logging errors from such bug
			log.fatal("Unexpected error during signing in", e);
			throw e;
		} finally {
			log.trace("Firing event: " + uso.toString());
			idesEvent.fire(uso);
			PrimeFaces.current().ajax().addCallbackParam(PARAM_CALLBACK_LOGGED_IN, isLoggedIn());
		}
	}

	@LogPerformance
	public void actionSignOut(ActionEvent event) {
		UserSignOutEvent uso = new UserSignOutEvent();
		uso.setUser(user);
		try {
			log.debug("actionSignOut, Current user: " + user.toString());

			if (user != User.ANONYMOUS && user != null) {
				log.trace("User signing out, user=" + user.printDetail());
			}
			user = User.ANONYMOUS;

			uso.setEventSuccess(true);
			uso.setEventSource(this);

			log.trace("Firing event: " + uso.toString());
			idesEvent.fire(uso);

			log.info("user signed out, invalidate session");
			FacesUtils.forceClearCache();
			FacesUtils.redirectToWelcomePage(false);
			FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
		} catch (Exception e) {
			log.error("Failed to sign out", e);
		}
	}

	private User processGoogleIdToken(String idTokenString) throws GeneralSecurityException, IOException {
		//ApplicationController app = IdesUtils.getNamedController(ApplicationController.class);
		String clientId = app.getGoogleSignInclientId();

		HttpTransport http = new NetHttpTransport();
		JsonFactory jsonFactory = new GsonFactory();
		GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(http, jsonFactory)
				.setAudience(Collections.singletonList(clientId)).build();
		// Glassfish 5 has a bug with Java version 1.8.0_171 and some above
		GoogleIdToken idToken = verifier.verify(idTokenString);

		if (idToken == null) {
			throw new IdesException("Error", "Invalid credential");
		}
		
		Payload payload = idToken.getPayload();
		String aud = (String) payload.getAudience();
		
		if (!clientId.equals(aud)) { // invalid google-client id
			throw new IdesException("Sign in failed", "Please contact administrator");
		}

		String email = payload.getEmail();
		String domainName = payload.getHostedDomain();
		log.trace("Google Account sign in API: User email=" + email);
		log.trace("Google Account sign in API: User domain=" + domainName);
		
		Set<Role> roles = getRole(email, domainName);
		
		String name = (String) payload.get("name");
		GmailUser gmailUser = new GmailUser(name, email, domainName, payload, roles);
		gmailUser.setUserIdToken(idTokenString);
		
		log.trace(gmailUser.getEmail() + " has role " + IdesUtils.deepPrint(roles));

		if (User.VISITOR_ROLES.equals(roles) || IdesUtils.isEmpty(roles)) {
			throw new IdesException("Unable to sign in", "Unauthorised email domain");
		}
		
		return gmailUser;
	}
	
	public boolean isAllowedToAccess(IdesPage page) {
		if (page == IdesPage.ADMIN_TOOLS || page == IdesPage.ORG_LIST) {
			return user.hasRole(Role.Administrator);
		}
		return true;
	}

	private Set<Role> getRole(String email, String domain) {
		return app.getRolesOfEmailAndDomain(email, domain);
	}

	public boolean isAdmin() {
		return user.hasRole(Role.Administrator) && isLoggedIn();
	}
	
	public boolean isLoggedIn() {
		return user != User.ANONYMOUS;
	}

	public User getUser() {
		return user;
	}
}
