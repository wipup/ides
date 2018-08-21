package ports.soc.ides.dao.mybatis;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import ports.soc.ides.model.ContactProfile;
import ports.soc.ides.model.constant.OwnerType;

public interface ContactProfileMapper {
	
	public List<ContactProfile> selectProfile(@Param("owner") String owner, @Param("type") OwnerType type);
	
	public long countProfile(@Param("owner") String owner, @Param("type") OwnerType type);

	public long insertProfile(@Param("profile") ContactProfile prof);
	
	public long updateProfile(@Param("profile") ContactProfile prof);
	
	public long selectNextId();
	
	public long deleteProfile(@Param("id") long id);
}