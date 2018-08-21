package ports.soc.ides.dao.mybatis;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.primefaces.model.SortOrder;

import ports.soc.ides.model.Idea;
import ports.soc.ides.model.constant.IdeaStatus;
import ports.soc.ides.model.constant.ProjectType;

public interface IdeaMapper {
	
	public Idea selectIdeaById(long id);

	public long countIdeasForListing(@Param("statuses") List<IdeaStatus> statuses, @Param("types") List<ProjectType> types, @Param("searchText") String searchText);

	public List<Idea> selectIdeasForListing(@Param("statuses") List<IdeaStatus> statuses, @Param("types") List<ProjectType> types, @Param("sortBy") String sortColumn,
			@Param("order") SortOrder order, @Param("first") long first, @Param("last") long last, @Param("searchText") String searchText);
	
	public long insertIdea(@Param("idea") Idea idea);
	
	public long updateIdeaStatus(@Param("id") long id, @Param("status") IdeaStatus status, @Param("timestamp") LocalDateTime timestamp);
	
	public long updateIdea(@Param("idea") Idea idea);
	
	public long selectNextId();
	
	public long deleteIdea(@Param("id") long id);
}
