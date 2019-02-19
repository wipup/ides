package ports.soc.ides.dao;

import java.util.List;

import org.apache.ibatis.session.SqlSession;

import ports.soc.ides.controller.helper.ExtendedOrganisation;
import ports.soc.ides.dao.mybatis.ExtendedOrganisationMapper;

public class ExtendedOrganisationDAO extends AbstractDAO {

	public ExtendedOrganisationDAO(SqlSessionProvider provider) {
		super(provider);
	}

	protected ExtendedOrganisationMapper getExtendedOrganisationMapper(SqlSession sql) {
		return sql.getMapper(ExtendedOrganisationMapper.class);
	}

	public List<ExtendedOrganisation> selectOrganisationWithIdeaCountForListing() {
		long start = System.currentTimeMillis();
		try(SqlSession sql = sqlSessionProvider.getSqlSession()){
			log.trace("Selecting all organisations with associating ideas");
			return getExtendedOrganisationMapper(sql).selectOrganisationWithIdeaCountForListing();
		} finally {
			long end = System.currentTimeMillis() - start;
			log.info("selectOrganisationWithIdeaCountForListing took " + end + "ms");
		}
	}
	
	public long countIdeaAssociatingWithOrganisation(long id) {
		long start = System.currentTimeMillis();
		try(SqlSession sql = sqlSessionProvider.getSqlSession()){
			log.trace("Counting idea associating with organisation id=" + id);
			return getExtendedOrganisationMapper(sql).countIdeaAssociatingWithOrganisation(id);
		} finally {
			long end = System.currentTimeMillis() - start;
			log.info("countIdeaAssociatingWithOrganisation took " + end + "ms");
		}
	}

	public List<ExtendedOrganisation> selectOrganisationWithIdeaCountForListingAndSearch(String searchText) {
		long start = System.currentTimeMillis();
		try(SqlSession sql = sqlSessionProvider.getSqlSession()){
			log.debug("Selecting all organisations with associating ideas and searchText=" + searchText);
			return getExtendedOrganisationMapper(sql).selectOrganisationWithIdeaCountForListingAndSearch(searchText);
		} finally {
			long end = System.currentTimeMillis() - start;
			log.info("selectOrganisationWithIdeaCountForListingAndSearch took " + end + "ms");
		}
	}

}
