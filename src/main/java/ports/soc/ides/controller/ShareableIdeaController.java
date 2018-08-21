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
import ports.soc.ides.model.constant.IdeaStatus;

@Named("share")
@RequestScoped
public class ShareableIdeaController extends AbstractIdesController {

	private static final long serialVersionUID = 3350207201789855790L;
	
	@Inject
	private IdeaDisplayController showIdea;
	
	@Inject
	private OrganisationDisplayController showOrg;
	
	@Inject
	private SqlSessionProvider sqlProvider;

	private boolean allowed;
	
	@PostConstruct
	private void init() {
		allowed = false;
		
		Idea i = getIdeaFromParameters();
		if (i == null) {
			return;
		}
		
		IdeaStatus status = i.getStatus();
		if (status == IdeaStatus.Approved || status == IdeaStatus.Allocated) {
			allowed = true;
			showIdea.initDefault(i);
			showOrg.initDefault(i.getOrganisation());
		}
	}
	
	private Idea getIdeaFromParameters() {
		try {
			int ideaRef = getIntParameter(IdeaDisplayController.SHAREABLE_URL_PARAMETER_IDEA);
			if (ideaRef < 0) {
				return null; 
			}
			
			IdeaDAO dao = new IdeaDAO(sqlProvider);
			
			return dao.selectIdeaById(ideaRef);
		} catch (Exception e) {
			log.error("error getIdeaFromParameters idea ref=" + getParameter(IdeaDisplayController.SHAREABLE_URL_PARAMETER_IDEA));
			return null;
		}
	}
	
	
	public boolean isAllowed() {
		return allowed;
	}
}
