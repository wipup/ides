package ports.soc.ides.controller.helper;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public enum IdesPage {

	AGREEMENT( "agreement", "/view/agreement.xhtml"),
	AGREEMENT_CONTENT( "agreement content", "/view/fragments/agreement_content.xhtml"),
	VIEW_IDEA("view_idea", "/view/idea.xhtml"),
	 
	ADMIN_TOOLS( "Administrator Tools", "/view/body/admin_tools.xhtml"), 
	CREATE_IDEA_WIZARD("Submit a new idea", "/view/body/ideaWizard/create_idea_wizard.xhtml"),
	CREATE_IDEA_WIZARD_NAV_BAR("createIdeaWizardNavBar", "/view/body/ideaWizard/idea_wizard_navigation_bar.xhtml"),
	CREATE_IDEA_WIZARD_SUBMIT_COMPLETE("idea submit complete", "/view/body/ideaWizard/idea_submit_success.xhtml"),
	HOW_TO_USE( "How to use", "/view/body/how_to_use.xhtml"), 
	IDEA_LIST( "Idea List", "/view/body/idea_list.xhtml"),
	ORG_LIST( "Organisation List", "/view/body/org_list.xhtml"),
	
	CREATE_ORG_FORM( "createOrgForm", "/view/fragments/edit_org.xhtml"), 
	CREATE_IDEA_FORM( "createIdeaForm", "/view/fragments/edit_idea.xhtml"),
	CREATE_IDEA_FORM_EDITOR_OPTION( "createIdeaFormEditorOption", "/view/fragments/edit_idea_editor_option.xhtml"),
	SEARCH_IDEA( "searchIdea", "/view/fragments/search_idea.xhtml"),
	SHOW_IDEA("showIdea", "/view/fragments/show_idea.xhtml"),
	SHOW_ORG("showOrg", "/view/fragments/show_org.xhtml"),

	MENU_BAR( "menuBar", "/view/fragments/menu_bar.xhtml"),
	ERROR( "error", "/view/error.xhtml"),
	INDEX( "homePage", "/view/index.xhtml");
	
	public static final Map<String, IdesPage> ALL_PAGES;
	
	static {
		Map<String, IdesPage> pages = new LinkedHashMap<>();
		for(IdesPage p : IdesPage.values()) {
			pages.put(p.id, p);
		}
		ALL_PAGES = Collections.unmodifiableMap(pages);
	}
	
	private final String path;
	private final String id;

	private IdesPage(String id, String path) {
		this.path = path;
		this.id = id;
	}

	public String getPageId() {
		return id;
	}

	public String getPath() {
		return path;
	}
	
	public static final IdesPage getPage(String pageId) {
		return ALL_PAGES.get(pageId);
	}
}
