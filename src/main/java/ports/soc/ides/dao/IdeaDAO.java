package ports.soc.ides.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.primefaces.model.SortOrder;

import ports.soc.ides.dao.mybatis.IdeaMapper;
import ports.soc.ides.dao.wrapper.LazyIdeaList;
import ports.soc.ides.model.DataModel;
import ports.soc.ides.model.Idea;
import ports.soc.ides.model.constant.IdeaStatus;
import ports.soc.ides.model.constant.ProjectType;
import ports.soc.ides.util.IdesUtils;

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
		long start = System.currentTimeMillis();
		try (SqlSession sql = sqlSessionProvider.getSqlSession()) {
			log.trace("Selecting idea where id=" + id);
			return getIdeaMapper(sql).selectIdeaById(id);
		} finally {
			long end = System.currentTimeMillis() - start;
			log.info("selectIdeaById took " + end + "ms");
		}
	}

	// TODO make it query once
	public LazyIdeaList selectIdeasForListingLazily(List<IdeaStatus> statuses, List<ProjectType> types, String sortColumn, SortOrder order, long first, long last, String searchText) {
		long start = System.currentTimeMillis();
		try (SqlSession sql = sqlSessionProvider.getSqlSession()) {
			StringBuilder sb = new StringBuilder();
			sb.append("Selecting idea where status=").append(IdesUtils.deepPrint(statuses)).append(", types=").append(IdesUtils.deepPrint(types)).append(", sortColumn=")
					.append(sortColumn).append(", first=").append(first).append(", last=").append(last).append(", searchText=").append(searchText);
			log.trace(sb.toString());
			
			IdeaMapper ideaMapper = getIdeaMapper(sql);
			List<Idea> ideas = ideaMapper.selectIdeasForListing(statuses, types, sortColumn, order, first, last, searchText);
			long totalFoundIdea = ideaMapper.countIdeasForListing(statuses, types, searchText);
			
			LazyIdeaList result  = new LazyIdeaList();
			result.setFetchedIdeas(ideas);
			result.setTotalFoundIdea(totalFoundIdea);
			
			return result;
		} finally {
			long end = System.currentTimeMillis() - start;
			log.info("selectIdeasForListingLazily took " + end + "ms");
		}
	}

	public long insertIdea(Idea idea) {
		long start = System.currentTimeMillis();
		try (SqlSession sql = sqlSessionProvider.getSqlSession()) {
			IdeaMapper mapper = getIdeaMapper(sql);
			log.debug("Selecting id for inserting new idea");
			
			long nextId = mapper.selectNextId();
			idea.setId(nextId);
			log.info("Inserting idea=" + IdesUtils.deepPrint(idea));
			
			return mapper.insertIdea(idea);
		} finally {
			long end = System.currentTimeMillis() - start;
			log.info("insertIdea took " + end + "ms");
		}
	}

	public long updateIdeaStatus(Idea id, IdeaStatus status, LocalDateTime timestamp) {
		long start = System.currentTimeMillis();
		try (SqlSession sql = sqlSessionProvider.getSqlSession()) {
			StringBuilder sb = new StringBuilder();
			sb.append("Updating status of idea=").append(id.printDetail()).append(" to status=").append(status).append(" with timestamp=").append(timestamp);
			log.info(sb.toString());
			return getIdeaMapper(sql).updateIdeaStatus(id.getId(), status, timestamp);
		} finally {
			long end = System.currentTimeMillis() - start;
			log.info("updateIdeaStatus took " + end + "ms");
		}
	}

	@Deprecated
	public long updateIdeaStatusById(long id, IdeaStatus status, LocalDateTime timestamp) {
		long start = System.currentTimeMillis();
		try (SqlSession sql = sqlSessionProvider.getSqlSession()) {
			StringBuilder sb = new StringBuilder();
			sb.append("Updating status of idea id=").append(id).append(" to status=").append(status).append(" with timestamp=").append(timestamp);
			log.info(sb.toString());
			return getIdeaMapper(sql).updateIdeaStatus(id, status, timestamp);
		} finally {
			long end = System.currentTimeMillis() - start;
			log.info("updateIdeaStatusById_deprecated took " + end + "ms");
		}
	}

	public void updateManyIdeasStatus(List<Idea> ideas, IdeaStatus status, LocalDateTime timestamp) {
		long start = System.currentTimeMillis();
		try (SqlSession sql = sqlSessionProvider.getSqlSession(false)) {
			IdeaMapper mapper = getIdeaMapper(sql);
			StringBuilder sb = new StringBuilder();
			for (Idea i : ideas) {
				sb.append("Updating status of idea=").append(IdesUtils.deepPrint(ideas)).append(" to status=").append(status).append(" with timestamp=").append(timestamp);
				sb.setLength(0);
				mapper.updateIdeaStatus(i.getId(), status, timestamp);
			}
			log.info(sb.toString());
			sql.commit();
		} finally {
			long end = System.currentTimeMillis() - start;
			log.info("updateManyIdeasStatus took " + end + "ms");
		}
	}

	public long updateIdea(Idea idea) {
		long start = System.currentTimeMillis();
		try (SqlSession sql = sqlSessionProvider.getSqlSession()) {
			log.info("Updating idea, id=" + idea.getId() + ", idea=" + DataModel.printDetail(idea));
			return getIdeaMapper(sql).updateIdea(idea);
		} finally {
			long end = System.currentTimeMillis() - start;
			log.info("updateIdea took " + end + "ms");
		}
	}

	public long selectNextIdeaId() {
		long start = System.currentTimeMillis();
		try (SqlSession sql = sqlSessionProvider.getSqlSession()) {
			long nextId = getIdeaMapper(sql).selectNextId();
			log.debug("Next idea id=" + nextId);
			return nextId;
		} finally {
			long end = System.currentTimeMillis() - start;
			log.info("selectNextIdeaId_deprecated took " + end + "ms");
		}
	}

	public long deleteIdea(Idea idea) {
		long start = System.currentTimeMillis();
		try (SqlSession sql = sqlSessionProvider.getSqlSession()) {
			log.info("Deleting idea=" + idea.printDetail());
			return getIdeaMapper(sql).deleteIdea(idea.getId());
		} finally {
			long end = System.currentTimeMillis() - start;
			log.info("deleteIdea took " + end + "ms");
		}
	}

	@Deprecated
	public long deleteIdeaById(long id) {
		long start = System.currentTimeMillis();
		try (SqlSession sql = sqlSessionProvider.getSqlSession()) {
			log.info("Deleting idea, id=" + id);
			return getIdeaMapper(sql).deleteIdea(id);
		} finally {
			long end = System.currentTimeMillis() - start;
			log.info("deleteIdeaById_deprecated took " + end + "ms");
		}
	}

	public void deleteIdeas(List<Idea> ideas) {
		long start = System.currentTimeMillis();
		try (SqlSession sql = sqlSessionProvider.getSqlSession(false)) {
			IdeaMapper mapper = getIdeaMapper(sql);
			for (Idea i : ideas) {
				log.info("Deleting idea=" + String.valueOf(i));
				mapper.deleteIdea(i.getId());
			}
			sql.commit();
		} finally {
			long end = System.currentTimeMillis() - start;
			log.info("deleteIdeas took " + end + "ms");
		}
	}
}
