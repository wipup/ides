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
		long start = System.currentTimeMillis();
		try(SqlSession sql = sqlSessionProvider.getSqlSession()){
			log.trace("Selecting organisation where id=" + id);
			return getOrganisationMapper(sql).selectOrgById(id);
		} finally {
			long end = System.currentTimeMillis() - start;
			log.info("selectOrgById took " + end + "ms");
		}
	}

	
	public List<Organisation> selectOrganisationsForListing() {
		long start = System.currentTimeMillis();
		try(SqlSession sql = sqlSessionProvider.getSqlSession()){
			log.trace("Selecting all organisations");
			return getOrganisationMapper(sql).selectOrganisationsForListing();
		} finally {
			long end = System.currentTimeMillis() - start;
			log.info("selectOrganisationsForListing took " + end + "ms");
		}
	}
	
	public long insertOrganisation(Organisation org) {
		long start = System.currentTimeMillis();
		try(SqlSession sql = sqlSessionProvider.getSqlSession()){
			OrganisationMapper mapper = getOrganisationMapper(sql);
			log.debug("Selecting new id for inserting organisation");
			long nextId = mapper.selectNextId();
			org.setId(nextId);
			log.info("Inserting organisation org=" + org.printDetail());
			return mapper.insertOrganisation(org);
		} finally {
			long end = System.currentTimeMillis() - start;
			log.info("insertOrganisation took " + end + "ms");
		}
	}

	@Deprecated
	public long selectNextId() {
		long start = System.currentTimeMillis();
		try(SqlSession sql = sqlSessionProvider.getSqlSession()){
			long nextId = getOrganisationMapper(sql).selectNextId();
			log.debug("Next organisation id=" + nextId);
			return nextId;
		} finally {
			long end = System.currentTimeMillis() - start;
			log.info("selectNextId_deprecated took " + end + "ms");
		}
	}

	public long deleteOrganisation(Organisation org) {
		long start = System.currentTimeMillis();
		try(SqlSession sql = sqlSessionProvider.getSqlSession()){
			log.info("Deleting organisation=" + org.printDetail());
			return getOrganisationMapper(sql).deleteOrganisationById(org.getId());
		} finally {
			long end = System.currentTimeMillis() - start;
			log.info("deleteOrganisation took " + end + "ms");
		}
	}
	
	@Deprecated
	public long deleteOrganisationById(long id) {
		long start = System.currentTimeMillis();
		try(SqlSession sql = sqlSessionProvider.getSqlSession()){
			log.info("Deleting organisation where id=" + id);
			return getOrganisationMapper(sql).deleteOrganisationById(id);
		} finally {
			long end = System.currentTimeMillis() - start;
			log.info("deleteOrganisationById_deprecated took " + end + "ms");
		}
	}

}
