package ports.soc.ides.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.primefaces.model.SortOrder;

import ports.soc.ides.dao.mybatis.IdeaMapper;
import ports.soc.ides.model.DataModel;
import ports.soc.ides.model.Idea;
import ports.soc.ides.model.constant.IdeaStatus;
import ports.soc.ides.model.constant.ProjectType;
import ports.soc.util.IdesUtils;

public class IdeaDAO extends AbstractDAO {

	/**
	 * Column constant variables for sorting
	 */
	public static final String COLUMN_ID = "COLUMN_ID";
	public static final String COLUMN_TITLE = "COLUMN_TITLE";
	public static final String COLUMN_STATUS = "COLUMN_STATUS";
	public static final String COLUMN_TIMESTAMP = "COLUMN_TIMESTAMP";
	public static final String COLUMN_ORGANISATION_NAME = "COLUMN_ORGANISATION_NAME";
	public static final String COLUMN_ORGANISATION_CONTACT = "COLUMN_ORGANISATION_CONTACT";
	public static final String COLUMN_TYPE = "COLUMN_TYPE";
	public static final String COLUMN_CREATE = "COLUMN_CREATE";

	public IdeaDAO(SqlSessionProvider provider) {
		super(provider);
	}

	protected IdeaMapper getIdeaMapper(SqlSession sql) {
		return sql.getMapper(IdeaMapper.class);
	}

	public Idea selectIdeaById(long id) {
		try (SqlSession sql = sqlSessionProvider.getSqlSession()) {
			log.trace("Selecting idea where id=" + id);
			return getIdeaMapper(sql).selectIdeaById(id);
		}
	}

	public long countIdeasForListing(List<IdeaStatus> statuses, List<ProjectType> types, String searchText) {
		try (SqlSession sql = sqlSessionProvider.getSqlSession()) {
			StringBuilder sb = new StringBuilder();
			sb.append("Counting idea where status=").append(IdesUtils.deepPrint(statuses)).append(", types=").append(IdesUtils.deepPrint(types)).append(", searchText=")
					.append(searchText);
			log.trace(sb.toString());
			return getIdeaMapper(sql).countIdeasForListing(statuses, types, searchText);
		}
	}

	public List<Idea> selectIdeasForListing(List<IdeaStatus> statuses, List<ProjectType> types, String sortColumn, SortOrder order, long first, long last, String searchText) {
		try (SqlSession sql = sqlSessionProvider.getSqlSession()) {
			StringBuilder sb = new StringBuilder();
			sb.append("Selecting idea where status=").append(IdesUtils.deepPrint(statuses)).append(", types=").append(IdesUtils.deepPrint(types)).append(", sortColumn=")
					.append(sortColumn).append(", first=").append(first).append(", last=").append(last).append(", searchText=").append(searchText);
			log.trace(sb.toString());
			return getIdeaMapper(sql).selectIdeasForListing(statuses, types, sortColumn, order, first, last, searchText);
		}
	}

	public long insertIdea(Idea idea) {
		try (SqlSession sql = sqlSessionProvider.getSqlSession()) {
			IdeaMapper mapper = getIdeaMapper(sql);
			log.debug("Selecting id for inserting new idea");
			long nextId = mapper.selectNextId();
			idea.setId(nextId);
			log.info("Inserting idea=" + IdesUtils.deepPrint(idea));
			return mapper.insertIdea(idea);
		}
	}

	public long updateIdeaStatus(Idea id, IdeaStatus status, LocalDateTime timestamp) {
		try (SqlSession sql = sqlSessionProvider.getSqlSession()) {
			StringBuilder sb = new StringBuilder();
			sb.append("Updating status of idea=").append(id.printDetail()).append(" to status=").append(status).append(" with timestamp=").append(timestamp);
			log.info(sb.toString());
			return getIdeaMapper(sql).updateIdeaStatus(id.getId(), status, timestamp);
		}
	}

	@Deprecated
	public long updateIdeaStatusById(long id, IdeaStatus status, LocalDateTime timestamp) {
		try (SqlSession sql = sqlSessionProvider.getSqlSession()) {
			StringBuilder sb = new StringBuilder();
			sb.append("Updating status of idea id=").append(id).append(" to status=").append(status).append(" with timestamp=").append(timestamp);
			log.info(sb.toString());
			return getIdeaMapper(sql).updateIdeaStatus(id, status, timestamp);
		}
	}

	public void updateManyIdeasStatus(List<Idea> ideas, IdeaStatus status, LocalDateTime timestamp) {
		try (SqlSession sql = sqlSessionProvider.getSqlSession(false)) {
			IdeaMapper mapper = getIdeaMapper(sql);
			StringBuilder sb = new StringBuilder();
			for (Idea i : ideas) {
				sb.append("Updating status of idea=").append(IdesUtils.deepPrint(ideas)).append(" to status=").append(status).append(" with timestamp=").append(timestamp);
				log.info(sb.toString());
				sb.setLength(0);
				mapper.updateIdeaStatus(i.getId(), status, timestamp);
			}
			sql.commit();
		}
	}

	public long updateIdea(Idea idea) {
		try (SqlSession sql = sqlSessionProvider.getSqlSession()) {
			log.info("Updating idea, id=" + idea.getId() + ", idea=" + DataModel.printDetail(idea));
			return getIdeaMapper(sql).updateIdea(idea);
		}
	}

	@Deprecated
	public long selectNextIdeaId() {
		try (SqlSession sql = sqlSessionProvider.getSqlSession()) {
			long nextId = getIdeaMapper(sql).selectNextId();
			log.debug("Next idea id=" + nextId);
			return nextId;
		}
	}

	public long deleteIdea(Idea idea) {
		try (SqlSession sql = sqlSessionProvider.getSqlSession()) {
			log.info("Deleting idea=" + idea.printDetail());
			return getIdeaMapper(sql).deleteIdea(idea.getId());
		}
	}

	@Deprecated
	public long deleteIdeaById(long id) {
		try (SqlSession sql = sqlSessionProvider.getSqlSession()) {
			log.info("Deleting idea, id=" + id);
			return getIdeaMapper(sql).deleteIdea(id);
		}
	}

	public void deleteIdeas(List<Idea> ideas) {
		try (SqlSession sql = sqlSessionProvider.getSqlSession(false)) {
			IdeaMapper mapper = getIdeaMapper(sql);
			for (Idea i : ideas) {
				log.info("Deleting idea=" + String.valueOf(i));
				mapper.deleteIdea(i.getId());
			}
			sql.commit();
		}
	}

}
