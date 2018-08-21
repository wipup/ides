package ports.soc.ides.model;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import ports.soc.ides.model.constant.Role;
import ports.soc.util.DateTimeUtils;
import ports.soc.util.IdesUtils;

/**
 * Currently not being stored in database
 * 
 * @author WIPU
 *
 */
public class User extends DataModel {

	private static final long serialVersionUID = 2772503551950605915L;

	public static final Set<Role> VISITOR_ROLES;
	static {
		Set<Role> visitorRole = new HashSet<>();
		visitorRole.add(Role.Visitor);
		VISITOR_ROLES = Collections.unmodifiableSet(visitorRole);
	}

	public static final User ANONYMOUS = new User();

	protected final String name;
	protected final String email;
	protected final Set<Role> roles;
	protected final String emailDomain;

	protected String imageUrl;
	protected LocalDateTime loggedInTime = LocalDateTime.now();

	public User() {
		roles = VISITOR_ROLES;
		name = "Anonymous";
		email = "";
		imageUrl = null;
		emailDomain = "";
	}

	public User(String name, String email, String domain, Set<Role> role) {
		this.name = name;
		if (role == null) {
			this.roles = VISITOR_ROLES;
		} else {
			this.roles = Collections.unmodifiableSet(role);
		}

		this.email = email;
		if (email == null) {
			this.emailDomain = null;
		} else {
			if (email.indexOf('@') < 0) {
				throw new IllegalArgumentException("Invalid email format: " + email);
			}
			email = email.toLowerCase();
			this.emailDomain = email.split("@")[1].toLowerCase();
		}
	}
	
	@Override
	public int hashCode() {
		// eclipse generated
		final int prime = 31;
		int result = 1;
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		// eclipse generated
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (email == null) {
			if (other.email != null)
				return false;
		} else if (!email.equals(other.email))
			return false;
		return true;
	}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String getEmailDomain() {
		return emailDomain;
	}

	public LocalDateTime getLoggedInTime() {
		return loggedInTime;
	}

	public void setLoggedInTime(LocalDateTime loggedInTime) {
		this.loggedInTime = loggedInTime;
	}

	public boolean hasRole(Object o) {
		return roles.contains(o);
	}

	public boolean addRole(Role e) {
		return roles.add(e);
	}

	public Role getDisplayRole() {
		return Role.getRoleForDisplaying(roles);
	}

	public Set<Role> getRoles() {
		return roles;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("User[name=").append(name).append(", email=").append(email).append("]");
		return sb.toString();
	}

	@Override
	public String printDetail() {
		StringBuilder sb = new StringBuilder();
		sb.append("User[name=").append(name).append(", email=").append(email).append(", loggedInTime=").append(DateTimeUtils.formatDateTime(loggedInTime)).append(", roles=")
				.append(IdesUtils.deepPrint(roles)).append("]");
		return sb.toString();
	}

}
