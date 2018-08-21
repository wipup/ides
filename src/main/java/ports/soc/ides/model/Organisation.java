package ports.soc.ides.model;

import javax.validation.constraints.Size;

public class Organisation extends DataModel {

	private static final long serialVersionUID = -3695863190798188566L;

	protected long id;

	@Size(max = 50)
	protected String name;

	@Size(max = 50)
	protected String typeOfWork;

	@Size(max = 100)
	protected String address;

	@Size(max = 10)
	protected String postcode;

	@Size(max = 50)
	protected String contact;

	@Size(max = 50)
	protected String email;

	@Size(max = 20)
	protected String telephone;

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

	public String getContact() {
		return contact;
	}

	public String getEmail() {
		return email;
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

	public void setContact(String contact) {
		this.contact = contact;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	/**
	 * This value is used at Organisation Listbox in Create New Idea page
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(name).append(" ").append(contact).append(" #").append(id);
		return sb.toString();
	}

	@Override
	public String printDetail() {
		StringBuilder sb = new StringBuilder();
		sb.append("Organisation [id=").append(id).append(", name=").append(name).append(", typeOfWork=").append(typeOfWork).append(", address=").append(address)
				.append(", postcode=").append(postcode).append(", contact=").append(contact).append(", email=").append(email).append(", telephone=").append(telephone).append("]");
		return sb.toString();
	}

}
