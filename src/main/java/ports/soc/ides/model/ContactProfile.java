package ports.soc.ides.model;

import ports.soc.ides.model.constant.OwnerType;

public class ContactProfile extends Organisation {

	private static final long serialVersionUID = 4102706893900033351L;

	protected String owner;
	protected OwnerType ownerType;

	public ContactProfile() {
		super();
	}

	public ContactProfile(Organisation o) {
		createProfileFrom(o);
	}

	public void createProfileFrom(Organisation o) {
		name = o.getName();
		typeOfWork = o.getTypeOfWork();
		address = o.getAddress();
		postcode = o.getPostcode();
		telephone = o.getTelephone();
		contact = o.getContact();
		email = o.getEmail();
	}

	@Override
	public String toString() {
		return super.toString();
	}

	@Override
	public String printDetail() {
		StringBuilder sb = new StringBuilder();
		sb.append("ContactProfile[owner=").append(owner).append(", ownerType=").append(ownerType).append(", detail=").append(super.printDetail()).append("]");
		return sb.toString();
	}
 
	public String getOwner() {
		return owner;
	}

	public OwnerType getOwnerType() {
		return ownerType;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public void setOwnerType(OwnerType ownerType) {
		this.ownerType = ownerType;
	}
}
