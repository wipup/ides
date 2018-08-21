package ports.soc.ides.model.constant;

import java.util.Collection;


public enum Role {
	Visitor, //only anonymous 
	Student,
	Staff,
	Administrator //highest ordinal
	;
	
	/**
	 * Get role for displaying on the webpage
	 * @param roles
	 * @return
	 */
	public static Role getRoleForDisplaying(Collection<Role> roles) {
		Role result = null;
		for(Role r : roles) {
			if (result == null) {
				result = r;
				continue;
			}
			if (result.ordinal() < r.ordinal()) {
				result = r;
			}
		}
		return result;
	}
}
