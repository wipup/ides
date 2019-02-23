package ports.soc.ides.controller;

import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.enterprise.event.Reception;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.PrimeFaces;
import org.primefaces.component.wizard.Wizard;
import org.primefaces.event.FlowEvent;

import ports.soc.ides.controller.event.IdesEvent;
import ports.soc.ides.controller.event.PageChangeEvent;
import ports.soc.ides.controller.event.UserSignInEvent;
import ports.soc.ides.controller.fragment.IdeaDisplayController;
import ports.soc.ides.controller.fragment.IdeaFormController;
import ports.soc.ides.controller.fragment.OrganisationDisplayController;
import ports.soc.ides.controller.fragment.OrganisationFormController;
import ports.soc.ides.controller.helper.CreateIdeaWizardStep;
import ports.soc.ides.controller.helper.IdesPage;
import ports.soc.ides.dao.ContactProfileDAO;
import ports.soc.ides.dao.IdeaDAO;
import ports.soc.ides.dao.OrganisationDAO;
import ports.soc.ides.dao.SqlSessionProvider;
import ports.soc.ides.interceptor.annotation.LogPerformance;
import ports.soc.ides.model.ContactProfile;
import ports.soc.ides.model.DataModel;
import ports.soc.ides.model.Idea;
import ports.soc.ides.model.Organisation;
import ports.soc.ides.model.User;
import ports.soc.ides.model.constant.IdeaStatus;
import ports.soc.ides.model.constant.OwnerType;
import ports.soc.ides.model.constant.ProjectType;
import ports.soc.ides.model.constant.Role;
import ports.soc.ides.util.CaptchaUtil;
import ports.soc.ides.util.FacesUtils;
import ports.soc.ides.util.IdesUtils;

@Named("ideaWizard")
@ViewScoped
public class CreateIdeaWizardController extends AbstractIdesController {

	private static final long serialVersionUID = 8020078231931423011L;

	public static final String CREATE_IDEA_FORM_WIZARD_ID = "createIdeaWizardForm";
	public static final String CREATE_IDEA_WIZARD_ID = "wiz";

	public static final String PARAM_RESET_WIZARD = "resetWizard";
	public static final String PARAM_IGNORE_FORM_VALIDATION = "true";

	public static final String PARAM_CALLBACK_SUBMIT_ORG_RESULT = "submitOrgResult";
	public static final String PARAM_CALLBACK_SUBMIT_ORG_MSG = "submitOrgMessage";

	@Inject
	private SqlSessionProvider sqlProvider;
	@Inject
	private IdeaDisplayController showIdea;
	@Inject
	private OrganisationDisplayController showOrg;
	@Inject
	private IdeaFormController ideaForm;
	@Inject
	private OrganisationFormController orgForm;
	@Inject
	private UserController auth;
	@Inject
	private ApplicationController app;

	private List<Organisation> orgs;
	private Organisation selectedOrg;
	private CreateIdeaWizardStep currentStep;
	
	private boolean createNewOrganisationPage;
	private boolean submitOrganisationSuccess;
	private boolean submitIdeaSuccess;

	private boolean saveProfile;
	private ContactProfile profileTemplate;
	
	@PostConstruct
	public void init() {
		try {
			log.trace("CreateIdeaWizardController Init");
			initWizard(true);
		} catch (Exception e) {
			log.fatal("Error initialising wizard", e);
		}
	}

	@LogPerformance
	public void onStartOver(ActionEvent event) {
		// reset wizard
		initWizard(false);
	}

	private void initWizard(boolean queryOrgList) {
		log.debug("InitWizard query Organisation List=" + queryOrgList);

		if (queryOrgList) {
			try {
				OrganisationDAO dao = new OrganisationDAO(sqlProvider);
				orgs = dao.selectOrganisationsForListing();
				log.debug("total organisation found= " + orgs.size());
			} catch (Exception e) {
				orgs = new ArrayList<Organisation>();
				log.error("Error query organisation list", e);
				addMessageError("Error", "Unable to load clients list");
			}

		}

		UIComponent wizardInstance = FacesContext.getCurrentInstance().getViewRoot().findComponent(CREATE_IDEA_FORM_WIZARD_ID + ":" + CREATE_IDEA_WIZARD_ID);
		if (wizardInstance != null) {
			Wizard wiz = (Wizard) wizardInstance;
			wiz.setStep(CreateIdeaWizardStep.CREATE_ORGANISATION.toString());
		}

		currentStep = CreateIdeaWizardStep.CREATE_ORGANISATION;
		
		createNewOrganisationPage = false;
		submitOrganisationSuccess = false;
		submitIdeaSuccess = false;
		saveProfile = false;
		profileTemplate = null;
		

		selectedOrg = null;
		ideaForm.initDefault();
		ideaForm.setRenderIdeaStatus(false);

		showOrganisationDetail(selectedOrg);
	}

	@LogPerformance(logParameters = false)
	public String handleWizardFlow(FlowEvent event) {
		String nextStep = event.getNewStep();
		String oldStep = event.getOldStep();
		log.info("handle wizard flow, from step=" + oldStep + ", to=" + nextStep);

		createNewOrganisationPage = false;
		submitOrganisationSuccess = false;

		executeJavaScript("animateScroll('scrollTopBar');");

		if (PARAM_RESET_WIZARD.equals(nextStep)) {
			initWizard(false);
			currentStep = CreateIdeaWizardStep.CREATE_ORGANISATION;
			PrimeFaces.current().ajax().update(CREATE_IDEA_FORM_WIZARD_ID);
			return currentStep.toString();
		}

		currentStep = CreateIdeaWizardStep.getWizardStep(nextStep);
		if (currentStep == CreateIdeaWizardStep.CREATE_ORGANISATION) {
			// First step
			showOrganisationDetail(selectedOrg);
			
		} else if (currentStep == CreateIdeaWizardStep.CREATE_IDEA) {
			log.info("selected client and organisation=" + selectedOrg);
			if (selectedOrg == null ||  !orgs.contains(selectedOrg)) {
				currentStep = CreateIdeaWizardStep.CREATE_ORGANISATION;
				addMessageError("Error", "Please select your client and organisation");
			} else {
				log.info("Render idea creation form");
				
				// Second step
				Idea idea = ideaForm.getIdea();
				if (idea == null) {
					ideaForm.initDefault();
					idea = ideaForm.getIdea();
				}
				ideaForm.setKeepSessionAlive(true);

				idea.setOrganisation(selectedOrg);
				idea.setType(ProjectType.Study);
				idea.setStatus(IdeaStatus.Provisional);

				fillIdeaAttribute(idea);
			}
		} else if (currentStep == CreateIdeaWizardStep.REVIEW) {
			log.info("Rendering idea reviewing form before submission");
			
			ideaForm.trimAllFields();
			Idea i = ideaForm.getIdea();
			boolean valid = validateIdeaBeforeInsertion(i);
			if (!valid) {
				currentStep = CreateIdeaWizardStep.CREATE_IDEA;
				PrimeFaces.current().ajax().update(CREATE_IDEA_FORM_WIZARD_ID);
				return currentStep.toString();
			}

			// Last step
			showIdea.initDefault(ideaForm.getIdea());
			showIdea.setRenderId(false);
			showIdea.setRenderStatus(false);

			showOrganisationDetail(selectedOrg);

		}

		PrimeFaces.current().ajax().update(CREATE_IDEA_FORM_WIZARD_ID);
		return currentStep.toString();
	}

	private void fillIdeaAttribute(Idea idea) {
		User u = auth.getUser();
		if (!u.hasRole(Role.Student)) {
			return;
		}
		if (IdesUtils.isEmpty(idea.getStudent())) {
			idea.setStudent(u.getName());
		}
	}

	protected boolean validateIdeaBeforeInsertion(Idea i) {
		boolean valid = true;
		if (i == null) {
			addMessageError("Validation Error", "Idea data is empty!");
			valid = false;
		}
		if (IdesUtils.isEmpty(i.getTitle())) {
			addMessageError("Validation Error", "Idea title cannot be empty");
			valid = false;
		}
		if (IdesUtils.isEmpty(i.getAim())) {
			addMessageError("Validation Error", "Idea aim and objective cannot be empty");
			valid = false;
		}

		Organisation o = i.getOrganisation();
		if (o == null) {
			addMessageError("Validation Error", "Organisation cannot be empty");
			valid = false;
		}
		if (o.getId() <= 0) {
			addMessageError("Validation Error", "Invalid organisation reference number");
			valid = false;
		}
		if (!orgs.contains(o)) {
			addMessageError("Validation Error", "Invalid organisation");
			valid = false;
		}
		return valid;
	}

	@LogPerformance
	public void onSubmitNewIdea(ActionEvent event) {
		setAutoUpdateOverlayNotification(false);
		submitIdeaSuccess = false;

		boolean passCheck = validateCaptcha();
		if (!passCheck) {
			setAutoUpdateOverlayNotification(true);
			reInitReCaptcha();
			return;
		}

		log.debug("Validating new idea before insertion: " + DataModel.printDetail(ideaForm.getIdea()));

		ideaForm.trimAllFields();
		Idea i = ideaForm.getIdea();
		if (!validateIdeaBeforeInsertion(i)) {
			setAutoUpdateOverlayNotification(true);
			reInitReCaptcha();
			return;
		}

		try {
			IdeaDAO dao = new IdeaDAO(sqlProvider);

			LocalDateTime now = LocalDateTime.now();
			i.setTimestamp(now);
			i.setCreateTime(now);

			long result = dao.insertIdea(i);

			log.debug("insert new idea result: " + result);

			addMessageInfo("Success", "Your idea has been submitted successfully.");
			addMessageInfo("", "Please note that all submitted ideas have to be reviewed and approved by the administrator before being shown.");

			selectedOrg = null;
			submitIdeaSuccess = true;
		} catch (Exception e) {
			log.error("error onNewIdeaSubmit: " + i, e);
			addMessageError("Error", "Failed to insert new idea");
			reInitReCaptcha();
		} finally {
			setAutoUpdateOverlayNotification(true);
		}
		showRequestedModal();
	}
	
	@LogPerformance
	public void onAcceptCompleteIdeaSubmission(ActionEvent event) {
		try {
			if (!submitIdeaSuccess) {
				return;
			}
			
			addMessageInfo("Success", "Your idea has been submitted successfully.");
			addMessageInfo("", "Please note that all submitted ideas have to be reviewed and approved by the administrator before being shown.");
			
			NavigationController nav = FacesUtils.getNamedController(NavigationController.class);
			nav.navigateTo(IdesPage.CREATE_IDEA_WIZARD_SUBMIT_COMPLETE);
			
			showIdea.setRenderId(true);
		} catch (Exception e) {
			log.error(e);
		}
	}

	public void onIdesEvent(@Observes(notifyObserver = Reception.IF_EXISTS) IdesEvent e) {
		if (e instanceof PageChangeEvent && e.isEventSuccess()) {
			PageChangeEvent pe = (PageChangeEvent) e;
			if (pe.pageChangeTo(IdesPage.CREATE_IDEA_WIZARD)) {
				initWizard(true);
			}
		} else if (e instanceof UserSignInEvent && e.isEventSuccess()) {
			if (createNewOrganisationPage) {
				fillOrganisationAttribute(orgForm.getOrganisation());
			} else if (currentStep == CreateIdeaWizardStep.CREATE_IDEA) {
				fillIdeaAttribute(ideaForm.getIdea());
			}
		}
	}

	private void showOrganisationDetail(Organisation org) {
		showOrg.initDefault(org);
		showOrg.setRenderId(false);
		log.trace("Showing organisation: " + IdesUtils.printDetail(org));
	}

	@LogPerformance(level = "TRACE")
	public void onSelectOrganisation(ActionEvent event) {
		try {
			showOrganisationDetail(selectedOrg);
		} catch (Exception e) {
			addMessageError("Error", "Unable to query requested organisation's detail");
			log.error("Error onOrganisationSelected ", e);
		}
	}

	@LogPerformance(level = "TRACE")
	public void onClickCreateOrganisationButton(ActionEvent event) {
		log.info("Render client creation form");
		createNewOrganisationPage = true;
		submitOrganisationSuccess = false;
		Organisation org = new Organisation();
		fillOrganisationAttribute(org);
		
		orgForm.initDefault(org);
	}

	private void fillOrganisationAttribute(Organisation o) {
		saveProfile = false;
		profileTemplate = null;

		User user = auth.getUser();
		if (!user.hasRole(Role.Staff)) {
			return;
		}

		if (IdesUtils.isEmpty(o.getContact())) {
			o.setContact(user.getName());
		}
		if (IdesUtils.isEmpty(o.getEmail())) {
			o.setEmail(user.getEmail());
		}

		try {
			
			ContactProfileDAO dao = new ContactProfileDAO(sqlProvider);

			// select user owned first, expect one
			log.info("Finding a contact profile of " + user.getEmail());
			List<ContactProfile> profiles = dao.selectProfile(user.getEmail(), OwnerType.EMAIL); 
			if (profiles.isEmpty()) {
				// select domain, expect one
				log.info("Finding a contact profile of domain " + user.getEmailDomain());
				profiles = dao.selectProfile(user.getEmailDomain(), OwnerType.DOMAIN);
			}
			if (profiles.isEmpty()) {
				log.info("No contact profile found");
				profileTemplate = null;
				return;
			}
			profileTemplate = profiles.get(profiles.size() - 1); //get last profile
			log.info("Found contact profile: count=" + profiles.size() + ", profile=" + profileTemplate.printDetail());
		} catch (Exception e) {
			log.error("Error retrieving contact profile of email=" + user.getEmail() + " and domain=" + user.getEmailDomain(), e);
		}
		
		saveProfile = true;

		o.setName(profileTemplate.getName());
		o.setAddress(profileTemplate.getAddress());
		o.setPostcode(profileTemplate.getPostcode());
		o.setTypeOfWork(profileTemplate.getTypeOfWork());
		o.setTelephone(profileTemplate.getTelephone());
		o.setContact(profileTemplate.getContact());
		o.setEmail(profileTemplate.getEmail());

		if (IdesUtils.isEmpty(o.getContact())) {
			o.setContact(user.getName());
		}
		if (IdesUtils.isEmpty(o.getEmail())) {
			o.setEmail(user.getEmail());
		}
	}

	protected boolean validateOrganisationBeforeInsertion(Organisation o) {
		boolean valid = true;
		if (o == null) {
			addMessageError("Validation Error", "Invalid organisation");
			valid = false;
		}
		if (IdesUtils.isEmpty(o.getName())) {
			addMessageError("Validation Error", "Organisation name cannot be empty");
			valid = false;
		}
		if (IdesUtils.isEmpty(o.getContact())) {
			addMessageError("Validation Error", "Organisation contact cannot be empty");
			valid = false;
		}
		if (!IdesUtils.isEmpty(o.getEmail())) {
			if (!IdesUtils.isValidEmail(o.getEmail())) {
				valid = false;
			}
		}
		return valid;
	}

	@LogPerformance
	public void onSubmitNewOrganisation(ActionEvent event) {
		setAutoUpdateOverlayNotification(false);
		
		submitOrganisationSuccess = false;
		
		try {
			boolean passCheck = validateCaptcha();
			if (!passCheck) {
				reInitReCaptcha();
				return;
			}

			orgForm.trimAllFields();
			Organisation newOrg = orgForm.getOrganisation();
			if (!validateOrganisationBeforeInsertion(newOrg)) {
				reInitReCaptcha();
				return;
			}

			OrganisationDAO dao = new OrganisationDAO(sqlProvider);
			dao.insertOrganisation(newOrg);
			addMessageInfo("Success", "New client created successfully");

			String email = auth.getUser().getEmail();
			if (!IdesUtils.isEmpty(email) && IdesUtils.isValidEmail(email) && auth.isLoggedIn() && auth.getUser().hasRole(Role.Staff)) {
				if (saveProfile ) {
					ContactProfileDAO profDao = new ContactProfileDAO(sqlProvider);
					if (profileTemplate == null) {
						// insert
						profileTemplate = new ContactProfile(newOrg);
						profileTemplate.setOwnerType(OwnerType.EMAIL);
						profileTemplate.setOwner(auth.getUser().getEmail());
	
						profDao.insertProfile(profileTemplate);
						addMessageInfo("Success", "Your new profile has been saved for next use");
	
					} else {
						// update existing
						profileTemplate.createProfileFrom(newOrg);
						profDao.updateProfile(profileTemplate);
						addMessageInfo("Success", "Your contact profile has been updated");
					}
				} else if (!saveProfile && profileTemplate != null) {
					ContactProfileDAO profDao = new ContactProfileDAO(sqlProvider);
					profDao.deleteProfile(profileTemplate);
				}
				
			}

			saveProfile = false;
			profileTemplate = null;

			// refresh organisation list
			orgs = dao.selectOrganisationsForListing();
			
			for (Organisation o : orgs) {
				if (o.getId() == newOrg.getId()) {
					selectedOrg = o;
					break;
				}
			}

			showOrganisationDetail(selectedOrg);

			log.info("new client has been submitted successfully: " + newOrg.printDetail());
			submitOrganisationSuccess = true;
		} catch (Exception e) {
			log.error("Error onSubmitNewOrganisation: ", e);
			addMessageError("Error", "Failed to create new client");
			addMessageError("", e.getMessage());
			reInitReCaptcha();
		} finally {
			setAutoUpdateOverlayNotification(true);
		}
		showRequestedModal();
	}

	private boolean validateCaptcha() {
		if (auth.isLoggedIn()) {
			return true;
		}
		if (!app.isEnableCaptcha()) {
			return true;
		}
		
		String privateKey = app.getCaptchaPrivateKey();
		if (IdesUtils.isEmpty(privateKey) || IdesUtils.isEmpty(privateKey)) {
			log.error("Unable to verify reCAPTCHA. Captcha private key or public key is empty");
			addMessageError("Internal Server Error. Unable to verify reCAPTCHA response. Please contact the administrator.", "");
			return true;
		}

		String token = getParameter("g-recaptcha-response");

		if (IdesUtils.isEmpty(token)) {
			log.error("reCaptcha token is empty");
			addMessageError("Error Invalid reCAPTCHA response. Please try again.", "");
			return false;
		}

		boolean success = false;
		try {
			success = CaptchaUtil.validateCaptcha(token, privateKey);
		} catch (SocketTimeoutException e) {
			log.error("Error verifying captcha", e);
			addMessageError("Failed to verify reCAPTCHA. Unable to connect to reCAPTCHA verification service", "");
		} catch (Exception e) {
			log.error("Error verifying captcha", e);
			addMessageError("Internal Server Error. Failed to verify reCAPTCHA response.", "");
		}

		if (!success) {
			addMessageError("reCAPTCHA Validation Failed. Please re-challenge the reCAPTCHA", "");
		}

		return success;
	}

	private void reInitReCaptcha() {
		if (!app.isEnableCaptcha()) {
			return;
		}
		if (IdesUtils.isEmpty(app.getCaptchaPrivateKey()) || IdesUtils.isEmpty(app.getCaptchaPublicKey())) {
			return;
		}
		if (auth.isLoggedIn()) {
			return;
		}
		CaptchaUtil.reinitCaptcha(PrimeFaces.current());
	}

	public void onCancelCreateOrg(ActionEvent event) {
		initWizard(false);
	}

	public boolean isEnableBackBtn() {
		return currentStep != CreateIdeaWizardStep.CREATE_ORGANISATION;
	}

	public boolean isEnableNextBtn() {
		if (currentStep == CreateIdeaWizardStep.CREATE_ORGANISATION) {
			return selectedOrg != null;
		}
		return currentStep != CreateIdeaWizardStep.REVIEW;
	}

	public List<Organisation> getOrgs() {
		return orgs;
	}

	public void setSelectedOrg(Organisation selectedOrg) {
		this.selectedOrg = selectedOrg;
	}

	public Organisation getSelectedOrg() {
		return selectedOrg;
	}


	public CreateIdeaWizardStep getCurrentStep() {
		return currentStep;
	}

	public boolean isSubmitOrganisationSuccess() {
		return submitOrganisationSuccess;
	}

	public boolean isSubmitIdeaSuccess() {
		return submitIdeaSuccess;
	}

	public boolean isShowSaveProfileBtn() {
		User user = auth.getUser();
		return user.hasRole(Role.Staff);
	}

	public boolean isSaveProfile() {
		return saveProfile;
	}

	public void setSaveProfile(boolean saveProfile) {
		this.saveProfile = saveProfile;
	}

	public boolean isCreateNewOrganisationPage() {
		return createNewOrganisationPage;
	}

	public void setCreateNewOrganisationPage(boolean createNewOrganisationPage) {
		this.createNewOrganisationPage = createNewOrganisationPage;
	}
	
}
