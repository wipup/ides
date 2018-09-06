package ports.soc.ides.controller;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.enterprise.event.Reception;
import javax.faces.annotation.FacesConfig;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.PrimeFaces;
import org.primefaces.component.datatable.DataTable;

import ports.soc.ides.controller.event.IdesEvent;
import ports.soc.ides.controller.event.PageChangeEvent;
import ports.soc.ides.controller.event.UserEvent;
import ports.soc.ides.controller.fragment.IdeaDisplayController;
import ports.soc.ides.controller.fragment.IdeaFormController;
import ports.soc.ides.controller.fragment.OrganisationDisplayController;
import ports.soc.ides.controller.helper.IdesPage;
import ports.soc.ides.controller.helper.LazyIdeaDataModel;
import ports.soc.ides.dao.IdeaDAO;
import ports.soc.ides.dao.SqlSessionProvider;
import ports.soc.ides.interceptor.annotation.IdesRoleAllowed;
import ports.soc.ides.interceptor.annotation.LogPerformance;
import ports.soc.ides.model.DataModel;
import ports.soc.ides.model.Idea;
import ports.soc.ides.model.constant.IdeaStatus;
import ports.soc.ides.model.constant.ProjectType;
import ports.soc.ides.model.constant.Role;
import ports.soc.ides.util.FacesUtils;
import ports.soc.ides.util.IdesUtils;

@FacesConfig // Required to enable JSF 2.3
@ViewScoped
@Named(value = "ideaList")
public class IdeaListController extends AbstractIdesController implements Serializable {

	private static final long serialVersionUID = -7743233255207776937L;
	
	public static final String IDEA_TABLE_FORM_ID = "ideaTableForm";
	public static final String IDEA_TABLE_FORM_DATATABLE_ID = "ideaDataTable";
	
	public static final String PARAM_IDEA_INDEX = "ideaIndex";

	public static final String PARAM_CONFIRM_MODE = "actionMode";
	public static final String PARAM_CONFIRM_MODE_APPROVE = "Approval";
	public static final String PARAM_CONFIRM_MODE_WITHDRAW = "Withdrawal";
	public static final String PARAM_CONFIRM_MODE_ALLOCATE = "Allocation";
	public static final String PARAM_CONFIRM_MODE_DELETE = "Deletion";

	public static final String PARAM_CALLBACK = "actionResult";

	public static final int MAX_SEARCH_KEYWORD_LENGTH = 100;
	
	public static final List<Integer> ROWS_PER_PAGE_LIST;
	public static final String ROWS_PER_PAGE_TEMPLATE;
	public static final int DEFAULT_ROWS_PER_PAGE;
	static {
		DEFAULT_ROWS_PER_PAGE = 15;
		
		List<Integer> rpps = new ArrayList<>();
		rpps.add(5);
		rpps.add(DEFAULT_ROWS_PER_PAGE);
		rpps.add(25);
		rpps.add(50);
		rpps.add(100);
		Collections.sort(rpps);
		ROWS_PER_PAGE_LIST = Collections.unmodifiableList(rpps);
		
		StringBuilder sb = new StringBuilder();
		for(int index = 0; index < ROWS_PER_PAGE_LIST.size(); index++) {
			sb.append(ROWS_PER_PAGE_LIST.get(index));
			if (index + 1 < ROWS_PER_PAGE_LIST.size()) {
				sb.append(",");
			}
		}
		ROWS_PER_PAGE_TEMPLATE = sb.toString();
	}
	

	@Inject
	private SqlSessionProvider sqlProvider;
	@Inject
	private ApplicationController app;

	@Inject
	private UserController auth;

	@Inject
	private IdeaDisplayController showIdea;
	@Inject
	private OrganisationDisplayController showOrg;

	@Inject
	private IdeaFormController ideaForm;
	private boolean renderEditIdeaForm;

	@Inject
	private LazyIdeaDataModel lazyIdeaDataModel;

	private IdeaStatus filterIdeaStatus;
	private ProjectType filterProjectType;

	private String confirmModalMode;

	private List<Idea> selectedIdeas;

	private String searchKeyword;
	
	//for history 
	private int selectedIdeaIndex;

	@PostConstruct
	public void init() {		
		FacesUtils.startLoggingSessionId();
		
		log.trace("IdeaListController init");
		
		renderEditIdeaForm = false;

		searchKeyword = "";
		selectedIdeaIndex = -1;

		filterIdeaStatus = IdeaStatus.Approved;
		filterProjectType = null;
		
		processIdeaFilterOption();

		selectedIdeas = new ArrayList<>();
	}

	@LogPerformance(note = "select idea from data table", level = "DEBUG")
	public void onSelectIdea(ActionEvent event) {
		String ind = "";
		try {
			ind = getParameter(PARAM_IDEA_INDEX);

			log.debug("Receiving parameter: " + PARAM_IDEA_INDEX + " = " + ind);
			int index = Integer.parseInt(ind);
				
			Idea lazyIdea = lazyIdeaDataModel.getIdeaByIndex(index);
			if (lazyIdea == null) {
				log.error("Unable to get idea with index=" + ind + ", where ideaList size=" + lazyIdeaDataModel.getDatasourceSize());
				addMessageError("Error", "The requested idea doesn't exist or has been removed.");
				return;
			}
			
			long ideaId = lazyIdea.getId();
			IdeaDAO ideaDAO = new IdeaDAO(sqlProvider);
			Idea selectedIdea = ideaDAO.selectIdeaById(ideaId);
			if (selectedIdea == null) {
				log.error("Failed to query idea with reference=" + ideaId);
				addMessageError("Error", "The requested idea doesn't exist or has been removed.");
				return;
			}
			
			if (!auth.isAdmin()) {
				// not allow non-admin users to see provisional/withdrawn ideas
				if (selectedIdea.getStatus() == IdeaStatus.Provisional || selectedIdea.getStatus() == IdeaStatus.Withdrawn) {
					selectedIdea = new Idea(); // send empty idea instead
					log.error("Unable to select " + DataModel.printDetail(selectedIdea) + " idea without being an administrator");
					addMessageError("Permission Denied", "");
				}
			}
			showIdea.initDefault(selectedIdea);
			log.info("Showing detail of idea reference=" + selectedIdea.getId());

			if (selectedIdea != null) {
				log.debug("Selected idea=" + selectedIdea.printDetail());
				showOrg.initDefault(selectedIdea.getOrganisation());
				log.debug("Selected idea's org=" + selectedIdea.getOrganisation().printDetail());
				selectedIdeaIndex = index;
			}

			showRequestedModal();
		} catch (Exception e) {
			showIdea.initDefault();
			addMessageError("Error", "Unable to load idea detail");
			log.error("Error onSelectRow, requested index=" + ind, e);
		}
	}

	public void onIdesEvent(@Observes(notifyObserver = Reception.IF_EXISTS) IdesEvent e) {
		if (e instanceof UserEvent) {
			if (e.isEventSuccess()) {
				log.debug("Process user sign in/out event: " + e.toString());
				processIdeaFilterOption();
			}
		} else if (e instanceof PageChangeEvent) {
			PageChangeEvent pe = (PageChangeEvent) e;
			if (pe.getPageAfter() == IdesPage.IDEA_LIST && pe.isEventSuccess()) {
				log.debug("Process pageChange event: " + e.toString());
				processIdeaFilterOption();
			}
		}
	}

	@LogPerformance(level = "TRACE")
	public void onFilterIdea(ActionEvent event) {
		processIdeaFilterOption();
	}

	@IdesRoleAllowed({Role.Administrator})
	@LogPerformance
	public void onSubmitEditIdea(ActionEvent event) {
		LocalDateTime oldTimestamp = null;
		setAutoUpdateOverlayNotification(true);
		try {

			Idea i = ideaForm.getIdea();
			if (IdesUtils.isEmpty(i.getTitle())) {
				addMessageError("Validation Error", "Idea title cannot be empty");
				return;
			}
			if (IdesUtils.isEmpty(i.getAim())) {
				addMessageError("Validation Error", "Idea aim and objective cannot be empty");
				return;
			}

			oldTimestamp = i.getTimestamp();
			i.setTimestamp(LocalDateTime.now());

			IdeaDAO dao = new IdeaDAO(sqlProvider);
			dao.updateIdea(i);
			
			
			processIdeaFilterOption();
			addMessageInfo("Success", "Idea #" + i.getId() + " has been updated");
		} catch (Exception e) {
			Idea idea = ideaForm.getIdea();
			log.error("fail to submit editted idea: " + DataModel.printDetail(idea), e);
			addMessageError("Error", "Unable to update idea");
			if (oldTimestamp != null && idea != null) {
				idea.setTimestamp(oldTimestamp);
			}
		} finally {
			setAutoUpdateOverlayNotification(true);
		}
	}

	@IdesRoleAllowed({Role.Administrator})
	@LogPerformance
	public void onShowEditIdeaModal(ActionEvent event) {
		try {
			String indexParam = getParameter(PARAM_IDEA_INDEX);
			int index = Integer.parseInt(indexParam);

			log.debug("Receive requested idea edit for index: " + index);
			Idea lazyIdea = lazyIdeaDataModel.getIdeaByIndex(index);
			if (lazyIdea == null) {
				log.error("Unable to get idea with index=" + index + ", where ideaList size=" + lazyIdeaDataModel.getDatasourceSize());
				addMessageError("Error", "The requested idea doesn't exist or has been removed");
				return;
			}
			log.info("Rendering idea editing modal for idea reference=" + lazyIdea.getId());

			IdeaDAO dao = new IdeaDAO(sqlProvider);
			long ideaId = lazyIdea.getId();
			Idea selectedIdea = dao.selectIdeaById(ideaId);
			if (selectedIdea == null) {
				log.error("Failed to query idea with reference=" + ideaId);
				addMessageError("Error", "Fail to fetch idea data");
				renderEditIdeaForm = false;
				return;
			}

			log.debug("Get idea info from database: " + selectedIdea);

			ideaForm.initDefault(selectedIdea);
			ideaForm.setRenderNotSpecificTypeRadio(false);
			ideaForm.setRenderIdeaStatus(true);
			ideaForm.setRenderIdeaStatusRadio(false);
			ideaForm.setRenderReferenceNumber(true);
			ideaForm.setRenderProjectTypeHelp(false);

			selectedIdeaIndex = index;
			renderEditIdeaForm = true;
			showRequestedModal();
		} catch (Exception e) {
			log.error("fail to show idea editing modal", e);
			addMessageError("Error", "Unable to load idea");
			renderEditIdeaForm = false;
		}
	}

	@IdesRoleAllowed({Role.Administrator})
	@LogPerformance
	public void onProcessOneIdea(ActionEvent event) {
		try {
			String indexParam = getParameter(PARAM_IDEA_INDEX);
			int index = Integer.parseInt(indexParam);
			String mode = getParameter(PARAM_CONFIRM_MODE);

			log.debug("Receiving parameter for mode=" + mode + " on Idea at index[" + index + "] = " + lazyIdeaDataModel.getIdeaByIndex(index));
			
			Idea targetIdea = lazyIdeaDataModel.getIdeaByIndex(index);
			if (targetIdea == null) {
				log.error("Unable to get idea with index=" + index + ", where ideaList size=" + lazyIdeaDataModel.getDatasourceSize());
				addMessageError("Error", "The requested idea doesn't exist or has been removed");
				return;
			}
			
			IdeaDAO dao = new IdeaDAO(sqlProvider);

			if (PARAM_CONFIRM_MODE_DELETE.equals(mode) && app.isEnableIdeaDeletion()) {
				if (!app.isEnableIdeaDeletion()) {
					addMessageError("Error", "Idea deletion is not allowed");
					return;
				}

				if (targetIdea.getStatus() != IdeaStatus.Withdrawn) {
					addMessageError("Error", "The idea to be deleted must be withdrawn first");
					return;
				}
				
				dao.deleteIdea(targetIdea);
				addMessageInfo("Success", "Idea #" + targetIdea.getId() + " has been deleted");
			} else {
				
				IdeaStatus targetStatus = getResultIdeaStatusFromAction(mode);
				if (targetStatus == null) {
					addMessageError("Error", "Unrecognised action");
					return;
				}
				
				dao.updateIdeaStatus(targetIdea, targetStatus, LocalDateTime.now());
				addMessageInfo("Success", "Idea #" + targetIdea.getId() + " has been " + targetStatus.toString().toLowerCase());
			}
			
			processIdeaFilterOption();
		} catch (Exception e) {
			log.error("error on processing idea", e);
			addMessageError("Error", "Unable to update requested idea");
		}
	}

	private List<Integer> getHiddenIdeaIndexRequestParams() {
		List<Integer> values = new ArrayList<>();
		Map<String, String> params = getParameterMap();
		for (String key : params.keySet()) {
			if (key.endsWith(PARAM_IDEA_INDEX)) {
				String index = params.get(key);
				try {
					values.add(Integer.parseInt(index));
				} catch (Exception e) {
					log.error("Invalid index of idea format: " + index);
				}
			}
		}
		return values;
	}

	@IdesRoleAllowed({Role.Administrator})
	@LogPerformance
	public void onProcessSelectedIdeas(ActionEvent event) {
		try {
			String mode = getParameter(PARAM_CONFIRM_MODE);
			if (IdesUtils.isEmpty(mode) || IdesUtils.isEmpty(confirmModalMode)) {
				log.error("error on processing selected idea, invalid parameter=" + mode);
				addMessageError("Error", "Unrecognised requested action");
				PrimeFaces.current().ajax().addCallbackParam(PARAM_CALLBACK, false);
				return;
			}

			if (!mode.equals(confirmModalMode)) {
				addMessageError("Error", "The requested action has not been confirmed yet.");
				PrimeFaces.current().ajax().addCallbackParam(PARAM_CALLBACK, false);
				return;
			}
			
			IdeaDAO ideaDAO = new IdeaDAO(sqlProvider);

			if (!PARAM_CONFIRM_MODE_DELETE.equals(mode)) {
				IdeaStatus targetStatus = getResultIdeaStatusFromAction(confirmModalMode);
				if (targetStatus == null) {
					addMessageError("Error", "Invalid requested action");
					PrimeFaces.current().ajax().addCallbackParam(PARAM_CALLBACK, false);
					return;
				}

				LocalDateTime timestamp = LocalDateTime.now();
				ideaDAO.updateManyIdeasStatus(selectedIdeas, targetStatus, timestamp);
				addMessageInfo("Success", "Updating " + selectedIdeas.size() + " ideas completed");
			} else {
				if (!app.isEnableIdeaDeletion()) {
					addMessageError("Error", "Idea deletion function has not been enabled");
					return;
				}
				
				ideaDAO.deleteIdeas(selectedIdeas);
				addMessageInfo("Success", "Deleting " + selectedIdeas.size() + " ideas completed");
			}
			
			confirmModalMode = null;
			PrimeFaces.current().ajax().addCallbackParam(PARAM_CALLBACK, true);
			processIdeaFilterOption();
			
		} catch (Exception e) {
			log.error("error on process selected ideas", e);
			addMessageError("Error", "Unexpected error happened when processing selected ideas");
			PrimeFaces.current().ajax().addCallbackParam(PARAM_CALLBACK, false);
		}
	}

	private IdeaStatus getResultIdeaStatusFromAction(String action) {
		if (PARAM_CONFIRM_MODE_ALLOCATE.equals(action)) {
			return IdeaStatus.Allocated;
		} else if (PARAM_CONFIRM_MODE_APPROVE.equals(action)) {
			return IdeaStatus.Approved;
		} else if (PARAM_CONFIRM_MODE_WITHDRAW.equals(action)) {
			return IdeaStatus.Withdrawn;
		}
		return null;
	}

	@IdesRoleAllowed({Role.Administrator})
	@LogPerformance
	public void onConfirmSelectedIdeas(ActionEvent event) {
		try {
			String mode = getParameter(PARAM_CONFIRM_MODE);
			List<Integer> refIds = getHiddenIdeaIndexRequestParams();

			selectedIdeas = new ArrayList<>();
			for (int index : refIds) {
				Idea i = lazyIdeaDataModel.getIdeaByIndex(index);
				if (i == null) {
					addMessageError("Error", "Selected idea of row " + index + " does not exist");
					continue;
				}
				IdeaStatus targetStat = getEligibleStatusforAction(mode);

				if (targetStat == i.getStatus() && targetStat != null) {
					log.debug("Confirming action=" + mode + ", idea=" + i.printDetail());
					selectedIdeas.add(i);
				}
			}

			if (selectedIdeas.isEmpty()) {
				confirmModalMode = null;
				PrimeFaces.current().ajax().addCallbackParam(PARAM_CALLBACK, false);
				addMessageError("Error", "No selected ideas are eligible for " + mode);
			} else {
				confirmModalMode = mode;
				PrimeFaces.current().ajax().addCallbackParam(PARAM_CALLBACK, true);
			}

		} catch (Exception e) {
			log.error("error on confirming selected ideas", e);
		}
	}

	private IdeaStatus getEligibleStatusforAction(String action) {
		if (PARAM_CONFIRM_MODE_ALLOCATE.equals(action)) {
			return IdeaStatus.Approved;
		} else if (PARAM_CONFIRM_MODE_APPROVE.equals(action)) {
			return IdeaStatus.Provisional;
		} else if (PARAM_CONFIRM_MODE_WITHDRAW.equals(action)) {
			return IdeaStatus.Provisional;
		} else if (PARAM_CONFIRM_MODE_DELETE.equals(action)) {
			if (app.isEnableIdeaDeletion()) {
				return IdeaStatus.Withdrawn;
			}
		}
		return null;
	}

	private int processIdeaFilterOption() {
		try {
			if (lazyIdeaDataModel == null) {
				lazyIdeaDataModel = new LazyIdeaDataModel();
			}

			List<IdeaStatus> selectedStatus = new ArrayList<>();
			List<ProjectType> selectedProjType = new ArrayList<>();

			if (filterIdeaStatus == null) {
				selectedStatus.addAll(Arrays.asList(IdeaStatus.values()));
			} else {
				selectedStatus.add(filterIdeaStatus);
			}

			if (filterProjectType == null) {
				selectedProjType.addAll(Arrays.asList(ProjectType.values()));
			} else {
				selectedProjType.add(filterProjectType);
			}

			if (!auth.isAdmin() || !auth.isLoggedIn()) {
				selectedStatus.remove(IdeaStatus.Provisional);
				selectedStatus.remove(IdeaStatus.Withdrawn);
				if (selectedStatus.isEmpty()) {
					filterIdeaStatus = IdeaStatus.Approved;
					selectedStatus.add(filterIdeaStatus);
				}
			}

			log.debug("set idea filter option=[selectedStatus=" + IdesUtils.deepPrint(selectedStatus) + ", selectedProjType=" + IdesUtils.deepPrint(selectedProjType)
					+", searchKeyword=" + searchKeyword + "]");

			if (!IdesUtils.isEmpty(searchKeyword)) {
				searchKeyword = searchKeyword.trim();
				if (searchKeyword.length() > MAX_SEARCH_KEYWORD_LENGTH) {
					searchKeyword = searchKeyword.substring(0, MAX_SEARCH_KEYWORD_LENGTH);
				}
				final String doubleQuote = "\"";
				if (searchKeyword.startsWith(doubleQuote) && searchKeyword.endsWith(doubleQuote) && searchKeyword.length() > 1) {
					searchKeyword = searchKeyword.substring(1, searchKeyword.length() - 1);
				}
			}

			lazyIdeaDataModel.setSearchKeyword(searchKeyword);
			lazyIdeaDataModel.setFilterStatus(selectedStatus);
			lazyIdeaDataModel.setFilterType(selectedProjType);

			UIComponent comp = FacesContext.getCurrentInstance().getViewRoot().findComponent(IDEA_TABLE_FORM_ID + ":" + IDEA_TABLE_FORM_DATATABLE_ID);
			if (comp instanceof DataTable) {
				//fix datatable's multiple ajax requests issue
				DataTable dt = (DataTable) comp;
				dt.setFirst(0);
				int rowsPerPage = dt.getRows();
				if (!ROWS_PER_PAGE_LIST.contains(rowsPerPage)) {
					dt.setRows(DEFAULT_ROWS_PER_PAGE);
				}	
			}
			
			return (int) lazyIdeaDataModel.getRowCount();
		} catch (Exception e) {
			log.error("Failed to query idea", e);
			addMessageFatal("Error", "Failed to query ideas");
		}
		return -1;
	}
	
	@LogPerformance(level = "DEBUG")
	public void onShowIdeaFetchingStatus(ActionEvent event) {
		int count = lazyIdeaDataModel.getRowCount();
		if (count > 0) {
			addMessageInfo("Success", "Found " + count + " ideas");
		} else if (count <= 0) {
			addMessageInfo("Success", "No record found");
		}
	}

	public String getConfirmModalMode() {
		return confirmModalMode;
	}

	public IdeaStatus getFilterIdeaStatus() {
		return filterIdeaStatus;
	}

	public ProjectType getFilterProjectType() {
		return filterProjectType;
	}

	public List<Idea> getSelectedIdeas() {
		return selectedIdeas;
	}

	public void setFilterIdeaStatus(IdeaStatus filterIdeaStatus) {
		this.filterIdeaStatus = filterIdeaStatus;
	}

	public void setFilterProjectType(ProjectType filterProjectType) {
		this.filterProjectType = filterProjectType;
	}

	public void setSelectedIdeas(List<Idea> selectedIdeas) {
		this.selectedIdeas = selectedIdeas;
	}

	public int getSelectedIdeaSize() {
		return selectedIdeas.size();
	}

	public boolean isRenderEditIdeaForm() {
		return renderEditIdeaForm;
	}

	public String getSearchKeyword() {
		return searchKeyword;
	}

	public void setSearchKeyword(String searchKeyword) {
		this.searchKeyword = searchKeyword;
	}

	public LazyIdeaDataModel getLazyIdeaDataModel() {
		return lazyIdeaDataModel;
	}

	public void setLazyIdeaDataModel(LazyIdeaDataModel lazyIdeaDataModel) {
		this.lazyIdeaDataModel = lazyIdeaDataModel;
	}

	public int getSelectedIdeaIndex() {
		return selectedIdeaIndex;
	}
}
