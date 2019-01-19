package ports.soc.ides.dao.wrapper;

import java.util.ArrayList;
import java.util.List;

import ports.soc.ides.model.Idea;

public class LazyIdeaList {

	private long totalFoundIdea;
	
	private List<Idea> fetchedIdeas;

	public LazyIdeaList() {
		fetchedIdeas = new ArrayList<Idea>();
		totalFoundIdea = 0;
	}
	
	public long getTotalFoundIdea() {
		return totalFoundIdea;
	}

	public List<Idea> getFetchedIdeas() {
		return fetchedIdeas;
	}

	public void setTotalFoundIdea(long totalFoundIdea) {
		this.totalFoundIdea = totalFoundIdea;
	}

	public void setFetchedIdeas(List<Idea> fetchedIdeas) {
		this.fetchedIdeas = fetchedIdeas;
	}
	
	
}
