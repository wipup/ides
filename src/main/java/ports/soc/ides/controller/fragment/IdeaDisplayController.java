package ports.soc.ides.controller.fragment;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import ports.soc.ides.controller.AbstractIdesController;
import ports.soc.ides.controller.helper.IdesPage;
import ports.soc.ides.model.Idea;
import ports.soc.ides.util.IdesUtils;

@Named("showIdea")
@ViewScoped
public class IdeaDisplayController extends AbstractIdesController {

	private static final long serialVersionUID = -1867147741120211276L;

	public static final String SHAREABLE_URL_PARAMETER_IDEA = "ref"; 
	
	private static final Idea EMPTY_IDEA = new Idea();
	
	private Idea idea;

	private boolean renderId;
	private boolean renderTitle;
	private boolean renderAim;
	private boolean renderDeliverables;
	private boolean renderStatus;
	private boolean renderQuestion;
	private boolean renderStudent;
	private boolean renderType;
	
	private String shareableLink;
	
	@PostConstruct
	public void initDefault() {
		initDefault(EMPTY_IDEA);
	}
	
	public void initDefault(Idea i) {
		idea = i;
		fixNewlineIssue();
		
		renderId = true;
		renderTitle = true;
		renderAim = true;
		renderDeliverables = true;
		renderStatus = true;
		renderQuestion = true;
		renderStudent = true;
		renderType = true;
		
		generateShareableLink();
	}
	
	private void generateShareableLink() {
		if (idea.getId() <= 0) {
			shareableLink = null;
			return;
		}
		
		HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
		StringBuilder sb = new StringBuilder();
		sb.append(req.getRequestURL().substring(0, req.getRequestURL().length() - req.getRequestURI().length()))
			.append(req.getContextPath())
			.append(IdesPage.VIEW_IDEA.getPath())
			.append("?")
			.append(SHAREABLE_URL_PARAMETER_IDEA)
			.append("=")
			.append(idea.getId());
		shareableLink =  sb.toString();
	}
	
	private void fixNewlineIssue() {
		//replace newline with <br/> tag
		if (idea != null) {
			idea.setAim(IdesUtils.replaceNewlineToBr(idea.getAim()));
			idea.setQuestion(IdesUtils.replaceNewlineToBr(idea.getQuestion()));
			idea.setDeliverables(IdesUtils.replaceNewlineToBr(idea.getDeliverables()));
		}
	}
	
	public void setIdea(Idea idea) {
		this.idea = idea;
		fixNewlineIssue();
		generateShareableLink();
	}

	public Idea getIdea() {
		if (idea == EMPTY_IDEA) {
			idea = new Idea();
		}
		return idea;
	}


	public boolean isRenderId() {
		return renderId;
	}

	public boolean isRenderTitle() {
		return renderTitle;
	}

	public boolean isRenderAim() {
		return renderAim;
	}

	public boolean isRenderStatus() {
		return renderStatus;
	}

	public boolean isRenderQuestion() {
		return renderQuestion;
	}

	public boolean isRenderStudent() {
		return renderStudent;
	}

	public boolean isRenderType() {
		return renderType;
	}

	public void setRenderId(boolean renderId) {
		this.renderId = renderId;
	}

	public void setRenderTitle(boolean renderTitle) {
		this.renderTitle = renderTitle;
	}

	public void setRenderAim(boolean renderAim) {
		this.renderAim = renderAim;
	}

	public void setRenderStatus(boolean renderStatus) {
		this.renderStatus = renderStatus;
	}

	public void setRenderQuestion(boolean renderQuestion) {
		this.renderQuestion = renderQuestion;
	}

	public void setRenderStudent(boolean renderStudent) {
		this.renderStudent = renderStudent;
	}

	public void setRenderType(boolean renderType) {
		this.renderType = renderType;
	}

	public boolean isRenderDeliverables() {
		return renderDeliverables;
	}

	public void setRenderDeliverables(boolean renderDeliverables) {
		this.renderDeliverables = renderDeliverables;
	}
	
	public String getShareableLink() {
		return shareableLink;
	}
}
