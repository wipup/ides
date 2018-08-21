package ports.soc.ides.controller.fragment;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import ports.soc.ides.controller.AbstractIdesController;
import ports.soc.ides.model.Organisation;

@Named("showOrg")
@ViewScoped
public class OrganisationDisplayController extends AbstractIdesController {

	private static final long serialVersionUID = 6105320770004360993L;

	private static final Organisation EMPTY_ORGANISATION = new Organisation();
	
	private Organisation organisation;

	private boolean renderId;
	private boolean renderName;
	private boolean renderTypeOfWork;
	private boolean renderAddress;
	private boolean renderPostcode;
	private boolean renderContact;
	private boolean renderEmail;
	private boolean renderTelephone;

	@PostConstruct
	public void initDefault() {
		initDefault(EMPTY_ORGANISATION);
	}
	
	public void initDefault(Organisation o) {
		this.organisation = o;
		
		renderId = true;
		renderName = true;
		renderTypeOfWork = true;
		renderAddress = true;
		renderPostcode = true;
		renderContact = true;
		renderEmail = true;
		renderTelephone = true;
	}
	

	public Organisation getOrganisation() {
		if (organisation == EMPTY_ORGANISATION) {
			organisation = new Organisation();
		}
		return organisation;
	}

	public boolean isRenderId() {
		return renderId;
	}

	public boolean isRenderName() {
		return renderName;
	}

	public boolean isRenderTypeOfWork() {
		return renderTypeOfWork;
	}

	public boolean isRenderAddress() {
		return renderAddress;
	}

	public boolean isRenderPostcode() {
		return renderPostcode;
	}

	public boolean isRenderContact() {
		return renderContact;
	}

	public boolean isRenderEmail() {
		return renderEmail;
	}

	public boolean isRenderTelephone() {
		return renderTelephone;
	}

	public void setOrganisation(Organisation organisation) {
		this.organisation = organisation;
	}

	public void setRenderId(boolean renderId) {
		this.renderId = renderId;
	}

	public void setRenderName(boolean renderName) {
		this.renderName = renderName;
	}

	public void setRenderTypeOfWork(boolean renderTypeOfWork) {
		this.renderTypeOfWork = renderTypeOfWork;
	}

	public void setRenderAddress(boolean renderAddress) {
		this.renderAddress = renderAddress;
	}

	public void setRenderPostcode(boolean renderPostcode) {
		this.renderPostcode = renderPostcode;
	}

	public void setRenderContact(boolean renderContact) {
		this.renderContact = renderContact;
	}

	public void setRenderEmail(boolean renderEmail) {
		this.renderEmail = renderEmail;
	}

	public void setRenderTelephone(boolean renderTelephone) {
		this.renderTelephone = renderTelephone;
	}

}
