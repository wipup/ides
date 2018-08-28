package ports.soc.ides.controller.fragment;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import ports.soc.ides.controller.AbstractIdesController;
import ports.soc.ides.model.Organisation;
import ports.soc.ides.util.IdesUtils;

@Named("orgForm")
@ViewScoped
public class OrganisationFormController extends AbstractIdesController {

	private static final long serialVersionUID = 5740661417882090507L;

	private Organisation organisation;

	private boolean requireOrganisationName;
	private boolean requirePostalAddress;
	
	private boolean renderContactName;
	private boolean renderEmailAddress;
	private boolean renderTelephoneNumber;
	
	@PostConstruct
	public void initDefault() {
		initDefault(new Organisation());
	}
	
	public void initDefault(Organisation o) {
		organisation = o;
		
		requireOrganisationName = true;
		requirePostalAddress = true;
		renderContactName = true;
		renderEmailAddress = true;
		renderTelephoneNumber = true;
	}
	
	public Organisation getOrganisation() {
		return organisation;
	}

	public void trimAllFields() {
		if (organisation == null) {
			return;
		}
		organisation.setName(IdesUtils.trim(organisation.getName()));
		organisation.setAddress(IdesUtils.trim(organisation.getAddress()));
		organisation.setPostcode(IdesUtils.trim(organisation.getPostcode()));
		organisation.setTypeOfWork(IdesUtils.trim(organisation.getTypeOfWork()));
		organisation.setContact(IdesUtils.trim(organisation.getContact()));
		organisation.setTelephone(IdesUtils.trim(organisation.getTelephone()));
		organisation.setEmail(IdesUtils.trim(organisation.getEmail()));
	}

	public boolean isRequireOrganisationName() {
		return requireOrganisationName;
	}

	public boolean isRequirePostalAddress() {
		return requirePostalAddress;
	}

	public boolean isRenderContactName() {
		return renderContactName;
	}

	public boolean isRenderEmailAddress() {
		return renderEmailAddress;
	}

	public void setOrganisation(Organisation org) {
		if (org == null) {
			org = new Organisation();
		}
		this.organisation = org;
	}

	public void setRequireOrganisationName(boolean requireOrganisationName) {
		this.requireOrganisationName = requireOrganisationName;
	}

	public void setRequirePostalAddress(boolean requirePostalAddress) {
		this.requirePostalAddress = requirePostalAddress;
	}

	public void setRenderContactName(boolean renderContactName) {
		this.renderContactName = renderContactName;
	}

	public void setRenderEmailAddress(boolean renderEmailAddress) {
		this.renderEmailAddress = renderEmailAddress;
	}

	public boolean isRenderTelephoneNumber() {
		return renderTelephoneNumber;
	}

	public void setRenderTelephoneNumber(boolean renderTelephoneNumber) {
		this.renderTelephoneNumber = renderTelephoneNumber;
	}

}
