package ports.soc.ides.controller;

import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.enterprise.event.Reception;
import javax.enterprise.event.TransactionPhase;
import javax.faces.event.ActionEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import ports.soc.ides.controller.event.IdesEvent;
import ports.soc.ides.controller.event.PageChangeEvent;
import ports.soc.ides.controller.fragment.OrganisationDisplayController;
import ports.soc.ides.controller.helper.ExtendedOrganisation;
import ports.soc.ides.controller.helper.IdesPage;
import ports.soc.ides.dao.ExtendedOrganisationDAO;
import ports.soc.ides.dao.OrganisationDAO;
import ports.soc.ides.dao.SqlSessionProvider;
import ports.soc.ides.interceptor.annotation.IdesRoleAllowed;
import ports.soc.ides.interceptor.annotation.LogPerformance;
import ports.soc.ides.model.Organisation;
import ports.soc.ides.model.constant.Role;
import ports.soc.ides.util.IdesUtils;

/**
 * A backing bean of Manage-Clients page
 * @author WIPU
 *
 */
@Named("orgList")
@ViewScoped
public class OrganisationListController extends AbstractIdesController {

	private static final long serialVersionUID = -3281293855488158204L;
	
	public static final String PARAM_ORG_INDEX = "orgIndex";

	public static final String PARAM_IDEA_COUNT_EQUALS = "equals";
	public static final String PARAM_IDEA_COUNT_MORE_THAN = "more than";
	public static final String PARAM_IDEA_COUNT_LESS_THAN = "less than";
	
	@Inject
	private SqlSessionProvider sqlProvider;
	@Inject
	private OrganisationDisplayController showOrg;
	@Inject
	private ApplicationController app;

	private boolean DeleteConfirmed;
	private String searchText;
	private String savedSearchTxt;

	private ExtendedOrganisation selectedOrg;
	private List<ExtendedOrganisation> orgList;
	
	private Double associatingIdeaCount;
	private String ideaCountSearchCondition;

	@PostConstruct
	public void init() {
		log.trace("OrganisationListController init");
		
		selectedOrg = null;
		DeleteConfirmed = false;
		searchText = null;
		savedSearchTxt = null;
		associatingIdeaCount = null;
		ideaCountSearchCondition = null;
		fetchOrganisation();
		ideaCountSearchCondition = PARAM_IDEA_COUNT_EQUALS;
		
	}

	private void fetchOrganisation() {
		try {
			ExtendedOrganisationDAO dao = new ExtendedOrganisationDAO(sqlProvider);

			if (IdesUtils.isEmpty(savedSearchTxt) ){
				orgList = dao.selectOrganisationWithIdeaCountForListing();
			} else {
				orgList = dao.selectOrganisationWithIdeaCountForListingAndSearch(savedSearchTxt);
			}
			
			log.debug("associatingIdeaCount=" + associatingIdeaCount);
			log.debug("ideaCountSearchCondition=" + ideaCountSearchCondition);
			
			if (associatingIdeaCount != null && !IdesUtils.isEmpty(ideaCountSearchCondition)) {
				int preferredNumIdea = associatingIdeaCount.intValue();
				Iterator<ExtendedOrganisation> iter = orgList.iterator();
				while(iter.hasNext()) {
					ExtendedOrganisation exo = iter.next();
					int ideaCount = exo.getIdeas().size();
					boolean passCheck = validateNumberCondition(ideaCount, ideaCountSearchCondition, preferredNumIdea);
					if (!passCheck) {
						iter.remove();
					}
				}
			}
			
		} catch (Exception e) {
			log.error("Error when selecting organistaion with associating ideas ", e);
			addMessageError("Error", "Failed to fetch client list and associating ideas.");
		}
	}
	
	public String getIntAssociatingIdeaCount() {
		if (associatingIdeaCount == null) {
			return "";
		}
		return String.valueOf(associatingIdeaCount.intValue());
	}
	
	private boolean validateNumberCondition(int number, String condition, int threshold) {
		if (condition.equals(PARAM_IDEA_COUNT_EQUALS)) {
			return number == threshold;
		}
		if (condition.equals(PARAM_IDEA_COUNT_LESS_THAN)) {
			return number < threshold;
		}
		if (condition.equals(PARAM_IDEA_COUNT_MORE_THAN)) {
			return number > threshold;
		}
		return true;
	}

	@LogPerformance
	@IdesRoleAllowed({ Role.Administrator })
	public void onClearAllFilters(ActionEvent event) {
		init();
	}
	
	@LogPerformance
	@IdesRoleAllowed({ Role.Administrator })
	public void onClickClearSearch(ActionEvent event) {
		searchText = null;
		savedSearchTxt = null;
		fetchOrganisation();
	}
	
	@LogPerformance
	@IdesRoleAllowed({ Role.Administrator })
	public void onClickClearIdeaCount() {
		associatingIdeaCount = null;
		fetchOrganisation();
	}

	@LogPerformance
	@IdesRoleAllowed({ Role.Administrator })
	public void onClickSearch(ActionEvent event) {
		savedSearchTxt = searchText;
		fetchOrganisation();
	}

	@LogPerformance
	@IdesRoleAllowed({ Role.Administrator })
	public void onConfirmDeleteOrg(ActionEvent event) {
		try {
			int index = getIntParameter(PARAM_ORG_INDEX);
			selectedOrg = orgList.get(index);
			showRequestedModal();
			DeleteConfirmed = true;
		} catch (Exception e) {
			log.error(e);
			selectedOrg = null;
			DeleteConfirmed = false;
			addMessageError("Error", "Unable to process your request");
			addAllExceptionCausesToMessage(e);
		}
	}

	@LogPerformance
	@IdesRoleAllowed({ Role.Administrator })
	public void onDeleteOrg(ActionEvent event) {
		try {
			if (!DeleteConfirmed && selectedOrg != null) {
				addMessageError("Error", "This client hasn't been confirmed to delete");
				return;
			}

			ExtendedOrganisationDAO exDao = new ExtendedOrganisationDAO(sqlProvider);
			long count = exDao.countIdeaAssociatingWithOrganisation(selectedOrg.getId());
			if (count > 0) {
				addMessageError("Unable to delete", "This client seems to have associating ideas");
				return;
			}

			OrganisationDAO dao = new OrganisationDAO(sqlProvider);
			dao.deleteOrganisation(selectedOrg);

			addMessageInfo("Success", "Client #" + selectedOrg.getId() + " has been deleted");
			fetchOrganisation();
		} catch (Exception e) {
			log.error(e);
			addMessageError("Error", "Delete failed");
			addAllExceptionCausesToMessage(e);
		} finally {
			selectedOrg = null;
			DeleteConfirmed = false;
		}
	}

	@IdesRoleAllowed({ Role.Administrator })
	public void onRefreshOrgList(ActionEvent event) {
		searchText = "";
		savedSearchTxt = "";
		fetchOrganisation();
	}

	@LogPerformance
	public void onSelectOrg(ActionEvent event) {
		try {
			int index = getIntParameter(PARAM_ORG_INDEX);

			selectedOrg = orgList.get(index);
			log.debug("selected org=" + selectedOrg.printDetail());

			OrganisationDAO dao = new OrganisationDAO(sqlProvider);
			Organisation org = dao.selectOrgById(selectedOrg.getId());

			log.debug("Detail org=" + org.printDetail());

			showOrg.initDefault(org);

			showRequestedModal();
		} catch (Exception e) {
			selectedOrg = null;
			log.error("Error showing organisation", e);
			addMessageError("Error", "Unable to fetch detail of the organisation");
			addAllExceptionCausesToMessage(e);
		}
	}

	public void onIdesEvent(@Observes(notifyObserver = Reception.IF_EXISTS, during = TransactionPhase.AFTER_COMPLETION) IdesEvent event) {
		if (event instanceof PageChangeEvent && event.isEventSuccess()) {
			PageChangeEvent pe = (PageChangeEvent) event;
			if (app.isEnableOrgDeletion() && pe.getRequestedPage() == IdesPage.ORG_LIST) {
				init();
			}
		} 
//		else if (event instanceof UserSignOutEvent && event.isEventSuccess()) {
//			NavigationController nav = getNamedController(NavigationController.class);
//			if (nav.getCurrentPage() == IdesPage.ORG_LIST) {
//				nav.navigateTo(NavigationController.DEFAULT_PAGE);
//			}
//		}
	}

	public List<ExtendedOrganisation> getOrgList() {
		return orgList;
	}


	public void setOrgList(List<ExtendedOrganisation> orgList) {
		this.orgList = orgList;
	}

	public ExtendedOrganisation getSelectedOrg() {
		return selectedOrg;
	}

	public String getSearchText() {
		return searchText;
	}

	public void setSearchText(String searchText) {
		this.searchText = searchText;
	}

	public String getSavedSearchTxt() {
		return savedSearchTxt;
	}

	public void setSavedSearchTxt(String savedSearchTxt) {
		this.savedSearchTxt = savedSearchTxt;
	}

	public Double getAssociatingIdeaCount() {
		return associatingIdeaCount;
	}

	public void setAssociatingIdeaCount(Double associatingIdeaCount) {
		this.associatingIdeaCount = associatingIdeaCount;
	}

	public String getIdeaCountSearchCondition() {
		return ideaCountSearchCondition;
	}
	
	public void setIdeaCountSearchCondition(String ideaCountSearchCondition) {
		this.ideaCountSearchCondition = ideaCountSearchCondition;
	}
}
