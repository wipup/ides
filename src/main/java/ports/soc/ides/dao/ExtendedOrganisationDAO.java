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
		try(SqlSession sql = sqlSessionProvider.getSqlSession()){
			log.trace("Selecting all organisations with associating ideas");
			return getExtendedOrganisationMapper(sql).selectOrganisationWithIdeaCountForListing();
		}
	}
	
	public long countIdeaAssociatingWithOrganisation(long id) {
		try(SqlSession sql = sqlSessionProvider.getSqlSession()){
			log.trace("Counting idea associating with organisation id=" + id);
			return getExtendedOrganisationMapper(sql).countIdeaAssociatingWithOrganisation(id);
		}
	}

	public List<ExtendedOrganisation> selectOrganisationWithIdeaCountForListingAndSearch(String searchText) {
		try(SqlSession sql = sqlSessionProvider.getSqlSession()){
			log.debug("Selecting all organisations with associating ideas and searchText=" + searchText);
			return getExtendedOrganisationMapper(sql).selectOrganisationWithIdeaCountForListingAndSearch(searchText);
		}
	}

}
