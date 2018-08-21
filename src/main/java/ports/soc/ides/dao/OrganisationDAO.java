package ports.soc.ides.dao;

import java.util.List;

import org.apache.ibatis.session.SqlSession;

import ports.soc.ides.dao.mybatis.OrganisationMapper;
import ports.soc.ides.model.Organisation;

public class OrganisationDAO extends AbstractDAO  {

	public OrganisationDAO(SqlSessionProvider provider) {
		super(provider);
	}
	
	protected OrganisationMapper getOrganisationMapper(SqlSession sql) {
		return sql.getMapper(OrganisationMapper.class);
	}

	
	public Organisation selectOrgById(long id) {
		try(SqlSession sql = sqlSessionProvider.getSqlSession()){
			log.trace("Selecting organisation where id=" + id);
			return getOrganisationMapper(sql).selectOrgById(id);
		}
	}

	
	public List<Organisation> selectOrganisationsForListing() {
		try(SqlSession sql = sqlSessionProvider.getSqlSession()){
			log.trace("Selecting all organisations");
			return getOrganisationMapper(sql).selectOrganisationsForListing();
		}
	}
	
	public long insertOrganisation(Organisation org) {
		try(SqlSession sql = sqlSessionProvider.getSqlSession()){
			OrganisationMapper mapper = getOrganisationMapper(sql);
			log.debug("Selecting new id for inserting organisation");
			long nextId = mapper.selectNextId();
			org.setId(nextId);
			log.info("Inserting organisation org=" + org.printDetail());
			return mapper.insertOrganisation(org);
		}
	}

	@Deprecated
	public long selectNextId() {
		try(SqlSession sql = sqlSessionProvider.getSqlSession()){
			long nextId = getOrganisationMapper(sql).selectNextId();
			log.debug("Next organisation id=" + nextId);
			return nextId;
		}
	}

	public long deleteOrganisation(Organisation org) {
		try(SqlSession sql = sqlSessionProvider.getSqlSession()){
			log.info("Deleting organisation=" + org.printDetail());
			return getOrganisationMapper(sql).deleteOrganisationById(org.getId());
		}
	}
	
	@Deprecated
	public long deleteOrganisationById(long id) {
		try(SqlSession sql = sqlSessionProvider.getSqlSession()){
			log.info("Deleting organisation where id=" + id);
			return getOrganisationMapper(sql).deleteOrganisationById(id);
		}
	}

}
