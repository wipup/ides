package ports.soc.ides.controller.event;

import ports.soc.ides.model.User;

public class UserEvent extends IdesEvent {
	
	protected User user;
	
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("UserEvent [user=").append( user ).append(", toString()=").append( super.toString() ).append("]");
		return sb.toString();
	}
	
	
}
