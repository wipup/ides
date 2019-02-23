package ports.soc.ides.controller;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import ports.soc.ides.controller.fragment.IdeaDisplayController;
import ports.soc.ides.controller.fragment.OrganisationDisplayController;
import ports.soc.ides.dao.IdeaDAO;
import ports.soc.ides.dao.SqlSessionProvider;
import ports.soc.ides.model.Idea;
import ports.soc.ides.model.socialmedia.BasicOpenGraphModel;
import ports.soc.ides.util.FacesUtils;
import ports.soc.ides.util.IdesUtils;

@Named("share")
@RequestScoped
public class IdeaSharingController extends AbstractIdesController implements BasicOpenGraphModel {

	private static final long serialVersionUID = 3350207201789855790L;
	
	@Inject
	private IdeaDisplayController showIdea;
	
	@Inject
	private OrganisationDisplayController showOrg;
	
	@Inject
	private SqlSessionProvider sqlProvider;
	
	@Inject
	private ApplicationController app;

	private boolean allowed;
	
	@PostConstruct
	private void init() {
		FacesUtils.startLoggingSessionId();
		allowed = false;
		
		Idea i = getIdeaFromParameters();
		if (i == null) {
			return;
		}

		if (i.isApproved() || i.isAllocated()) {
			showIdea.initDefault(i);
			if (IdesUtils.isEmpty(showIdea.getShareableLink())) {
				log.error("shareable link of idea ref=" + i.getId() + " is empty, sharing halt");
				return;
			}
			if (i.getOrganisation() == null) {
				log.error("organisation of idea ref=" + i.getId() + " is null");
				return;
			}
			
			allowed = true;
			showOrg.initDefault(i.getOrganisation());
			
		} else {
			log.warn("idea ref=" + i.getId() + " with status=" + i.getStatus() + " is not shareable. No idea rendered");
		}
	}
	
	private Idea getIdeaFromParameters() {
		try {
			int ideaRef = getIntParameter(IdeaDisplayController.SHAREABLE_URL_PARAMETER_IDEA);			
			log.info("Fetching idea for sharing, idea reference=" + ideaRef);
			
			IdeaDAO dao = new IdeaDAO(sqlProvider);
			Idea requestedIdea = dao.selectIdeaById(ideaRef); 
			
			log.info("Fetched idea for sharing result=" + requestedIdea);
			
			return requestedIdea; 
		} catch (Exception e) {
			log.error("error retrieving shareable idea by parameter, " 
						+ IdesUtils.replaceNewline(e.getMessage(), "") 
						+ ", idea reference no=" 
						+ IdesUtils.replaceNewline(getParameter(IdeaDisplayController.SHAREABLE_URL_PARAMETER_IDEA), ""));
			return null;
		}
	}
	

	@Override
	public String getOgUrl() {
		if (!allowed || IdesUtils.isEmpty(showIdea.getShareableLink())) {
			return FacesUtils.getBaseUrl();
		}
		return showIdea.getShareableLink();
	}

	@Override
	public String getOgTitle() {
		String ideaTitle = showIdea.getIdea().getTitle();
		if (IdesUtils.isEmpty(ideaTitle)) {
			return app.getBundledMessage("og.title");
		}
		if (ideaTitle.length() > 55) {
			ideaTitle = ideaTitle.substring(0, 50) + "...";
		}
		return ideaTitle;
	}

	@Override
	public String getOgDescription() {
		String ideaAim = showIdea.getIdea().getAim();
		if (IdesUtils.isEmpty(ideaAim)) {
			return app.getBundledMessage("og.description");
		}
		ideaAim = IdesUtils.removeAllHtmlTags(ideaAim);
		
		if (ideaAim.length() > 200) {
			ideaAim = ideaAim.substring(0, 195) + "...";
		}
		return ideaAim;
	}
	
	public String getBaseUrl() {
		return FacesUtils.getBaseUrl();
	}
	
	public IdeaDisplayController getShowIdea() {
		return showIdea;
	}
	
	public boolean isAllowed() {
		return allowed;
	}

}
