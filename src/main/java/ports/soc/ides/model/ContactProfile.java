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
		String detail = super.printDetail();
		StringBuilder sb = new StringBuilder();
		sb.append("ContactProfile[owner=").append(owner).append(", ownerType=").append(ownerType).append(", detail=").append(detail).append("]");
		return sb.toString();
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getTypeOfWork() {
		return typeOfWork;
	}

	public String getAddress() {
		return address;
	}

	public String getPostcode() {
		return postcode;
	}

	public String getTelephone() {
		return telephone;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setTypeOfWork(String typeOfWork) {
		this.typeOfWork = typeOfWork;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public void setPostcode(String postcode) {
		this.postcode = postcode;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
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

	public void setOwnerType(OwnerType type) {
		this.ownerType = type;
	}
}
