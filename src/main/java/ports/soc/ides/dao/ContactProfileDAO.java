package ports.soc.ides.dao;

import java.util.List;

import org.apache.ibatis.session.SqlSession;

import ports.soc.ides.dao.mybatis.ContactProfileMapper;
import ports.soc.ides.model.ContactProfile;
import ports.soc.ides.model.constant.OwnerType;

public class ContactProfileDAO extends AbstractDAO {

	public ContactProfileDAO(SqlSessionProvider provider) {
		super(provider);
	}

	protected ContactProfileMapper getContactProfileMapper(SqlSession sql) {
		return sql.getMapper(ContactProfileMapper.class);
	}

	public List<ContactProfile> selectProfile(String owner, OwnerType type) {
		try (SqlSession sql = sqlSessionProvider.getSqlSession()) {
			log.trace("Selecting contact profile where owner=" + owner + " and type=" + type);
			return getContactProfileMapper(sql).selectProfile(owner, type);
		}
	}

	public long countProfile(String owner, OwnerType type) {
		try (SqlSession sql = sqlSessionProvider.getSqlSession()) {
			log.trace("Counting contact profile where owner=" + owner + " and type=" + type);
			return getContactProfileMapper(sql).countProfile(owner, type);
		}
	}

	public long insertProfile(ContactProfile prof) {
		try (SqlSession sql = sqlSessionProvider.getSqlSession()) {
			ContactProfileMapper mapper = getContactProfileMapper(sql);
			log.debug("Selecting new id for contact profile");
			long nextId = mapper.selectNextId();
			prof.setId(nextId);
			log.info("Inserting contact profile prof=" + prof.printDetail());
			return mapper.insertProfile(prof);
		}
	}

	public long updateProfile(ContactProfile prof) {
		try (SqlSession sql = sqlSessionProvider.getSqlSession()) {
			log.info("Updating contact profile prof=" + prof.printDetail());
			return getContactProfileMapper(sql).updateProfile(prof);
		}
	}

	@Deprecated
	public long selectNextId() {
		try (SqlSession sql = sqlSessionProvider.getSqlSession()) {
			long nextId = getContactProfileMapper(sql).selectNextId();
			log.debug("Next contact profile id=" + nextId);
			return nextId;
		}
	}

	public long deleteProfile(ContactProfile prof) {
		try (SqlSession sql = sqlSessionProvider.getSqlSession()) {
			log.info("Deleting contact profile prof=" + prof.printDetail());
			return getContactProfileMapper(sql).deleteProfile(prof.getId());
		}
	}

	@Deprecated
	public long deleteProfile(long id) {
		try (SqlSession sql = sqlSessionProvider.getSqlSession()) {
			log.info("Deleting contact profile id=" + id);
			return getContactProfileMapper(sql).deleteProfile(id);
		}
	}

}
