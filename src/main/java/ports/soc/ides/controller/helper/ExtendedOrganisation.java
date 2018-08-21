package ports.soc.ides.controller.helper;

import java.util.ArrayList;
import java.util.List;

import ports.soc.ides.model.Idea;
import ports.soc.ides.model.Organisation;
import ports.soc.ides.util.IdesUtils;

public class ExtendedOrganisation extends Organisation {

	private static final long serialVersionUID = 1802688380217946006L;

	protected List<Idea> ideas;
	
	public List<Idea> getIdeas() {
		return ideas;
	}

	public void setIdeas(List<Idea> ideas) {
		this.ideas = ideas;
	}

	public int getTotalIdeas() {
		if (ideas == null) {
			ideas = new ArrayList<>();
		}
		return ideas.size();
	}
	
	@Override
	public String printDetail() {
		String result = super.printDetail();
		return "ExtendedOrganisation[ideas=" +  IdesUtils.deepPrint(ideas) + "]:"  + result;
	}
}
