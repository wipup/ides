package ports.soc.ides.controller;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import ports.soc.ides.controller.event.IdesEvent;
import ports.soc.ides.controller.event.PageChangeEvent;
import ports.soc.ides.controller.helper.IdesPage;
import ports.soc.ides.interceptor.annotation.LogPerformance;

@Named("nav")
@ViewScoped
public class NavigationController extends AbstractIdesController {

	private static final long serialVersionUID = 4954507172720600153L;

	public static final String PARAM_NAVIGATION_PAGE = "page";

	public static final IdesPage DEFAULT_PAGE = IdesPage.IDEA_LIST;

	@Inject
	private UserController auth;
	
	@Inject
	private Event<IdesEvent> idesEvent;

	private IdesPage currentPage;

	@PostConstruct
	public void init() {
		log.trace("NavigationController init");
		currentPage = IdesPage.IDEA_LIST;
	}
	
	public void navigateTo(IdesPage requestedPage) {
		try {
			PageChangeEvent e = new PageChangeEvent();
			e.setPageBefore(currentPage);
			e.setRequestedPage(requestedPage);
			e.setEventSource(this);
			e.setEventSuccess(true);
			
			if (requestedPage == null) {
				log.error("requested page not set, redirect to default page: " + DEFAULT_PAGE);
				currentPage = DEFAULT_PAGE;
				e.setEventSuccess(false);
				log.trace("Firing event: " + e.toString());
				idesEvent.fire(e);
				return;
			}
			
			if (requestedPage == IdesPage.INDEX) {
				//a bug from the design phase
				requestedPage = DEFAULT_PAGE;
			}
			
			log.debug("Navigate to page: " + requestedPage.getPageId());

			if (auth.isAllowedToAccess(requestedPage)) {
				currentPage = requestedPage;
			} else {
				addMessageError("Error", "Permission Denied");
				e.setEventSuccess(false);
			}
			
			e.setPageAfter(currentPage);

			log.trace("Firing event: " + e.toString());
			idesEvent.fire(e);
		} catch (Exception e) {
			log.error("Error on navigateTo", e);
		}
	}


	@LogPerformance(level = "TRACE")
	public void navigateTo(ActionEvent event) {
		try {
			String param = getParameter(PARAM_NAVIGATION_PAGE);
			log.debug("Request navigation param: " + param);

			IdesPage requestedPage = IdesPage.getPage(param);
			if (requestedPage == null) {
				log.error("unable to find page of param: " + param);
				addMessageError("Error", "Unknown requested page");
			}
			navigateTo(requestedPage);

		} catch (Exception e) {
			log.error("Error on navigateTo", e);
			addMessageError("Error", "Unknown requested page");
		}
	}

	/**
	 * Handle page refresh by user (pressing F5, or browser's buttons) Retrieved
	 * from: https://www.beyondjava.net/how-to-detect-a-page-reload-in-jsf This
	 * method may be called when other backing-beans haven't been placed in the
	 * session.
	 */
	public String onPostBack() {
		try {
			log.trace("onPostBack");
			
			ExternalContext ex = FacesContext.getCurrentInstance().getExternalContext();
			String sessionId = ex.getSessionId(false);
			log.debug("Received a postback request with sessionId: " + sessionId);
			
			currentPage = IdesPage.IDEA_LIST;
		} catch (Exception e) {
			log.error("Error on postback", e);
		}
		return "/";
	}

	public IdesPage getCurrentPage() {
		return currentPage;
	}
}

