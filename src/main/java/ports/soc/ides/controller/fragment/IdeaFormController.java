package ports.soc.ides.controller.fragment;

import javax.annotation.PostConstruct;
import javax.faces.event.ActionEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import ports.soc.ides.controller.AbstractIdesController;
import ports.soc.ides.model.Idea;
import ports.soc.ides.util.IdesUtils;

@Named("ideaForm")
@ViewScoped
public class IdeaFormController extends AbstractIdesController {

	private static final long serialVersionUID = 6824528599356833103L;
	
	private Idea idea;

	private boolean renderNotSpecificTypeRadio;
	private boolean renderIdeaStatusRadio;
	private boolean renderReferenceNumber;

	private boolean enableIdeaStatusRadio;
	private boolean renderIdeaStatus;
	
	private boolean renderProjectTypeHelp;

	private boolean keepSessionAlive;
	
	@PostConstruct
	public void initDefault() {
		initDefault(new Idea());
	}

	public void initDefault(Idea i) {
		idea = i;
		fixNewlineIssue();
		
		renderNotSpecificTypeRadio = false;
		renderIdeaStatusRadio = false;

		enableIdeaStatusRadio = false;
		renderReferenceNumber = false;
		renderIdeaStatus = false;
		renderProjectTypeHelp = true;
		
		keepSessionAlive = false;
	}
	
	private void fixNewlineIssue() {
		if (idea != null) {
			idea.setAim(IdesUtils.replaceNewlineWithBr(idea.getAim()));
			idea.setQuestion(IdesUtils.replaceNewlineWithBr(idea.getQuestion()));
			idea.setDeliverables(IdesUtils.replaceNewlineWithBr(idea.getDeliverables()));
		}
	}

	public void trimAllFields() {
		if (idea == null) {
			return;
		}
		idea.setTitle(IdesUtils.trim(idea.getTitle()));
		idea.setAim(IdesUtils.trim(idea.getAim()));
		idea.setQuestion(IdesUtils.trim(idea.getQuestion()));
		idea.setDeliverables(IdesUtils.trim(idea.getDeliverables()));
		idea.setStudent(IdesUtils.trim(idea.getStudent()));
	}
	
	public void keepAlive(ActionEvent event) {
		//do nothing, just keep session alive
	}

	public Idea getIdea() {
		return idea;
	}

	public void setIdea(Idea idea) {
		this.idea = idea;
		fixNewlineIssue();
	}

	public boolean isRenderNotSpecificTypeRadio() {
		return renderNotSpecificTypeRadio;
	}

	public boolean isRenderIdeaStatusRadio() {
		return renderIdeaStatusRadio;
	}

	public void setRenderIdeaStatusRadio(boolean renderIdeaStatusRadio) {
		this.renderIdeaStatusRadio = renderIdeaStatusRadio;
	}

	public boolean isEnableIdeaStatusRadio() {
		return enableIdeaStatusRadio;
	}

	public void setEnableIdeaStatusRadio(boolean enableIdeaStatusRadio) {
		this.enableIdeaStatusRadio = enableIdeaStatusRadio;
	}

	public boolean isRenderReferenceNumber() {
		return renderReferenceNumber;
	}

	public void setRenderReferenceNumber(boolean renderReferenceNumber) {
		this.renderReferenceNumber = renderReferenceNumber;
	}

	public boolean isRenderIdeaStatus() {
		return renderIdeaStatus;
	}

	public void setRenderNotSpecificTypeRadio(boolean renderNotSpecificTypeRadio) {
		this.renderNotSpecificTypeRadio = renderNotSpecificTypeRadio;
	}

	public void setRenderIdeaStatus(boolean renderIdeaStatus) {
		this.renderIdeaStatus = renderIdeaStatus;
	}

	public boolean isKeepSessionAlive() {
		return keepSessionAlive;
	}

	public void setKeepSessionAlive(boolean keepSessionAlive) {
		this.keepSessionAlive = keepSessionAlive;
	}

	public boolean isRenderProjectTypeHelp() {
		return renderProjectTypeHelp;
	}

	public void setRenderProjectTypeHelp(boolean renderProjectTypeHelp) {
		this.renderProjectTypeHelp = renderProjectTypeHelp;
	}

	
	
}
