package ports.soc.ides.dao.mybatis;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import ports.soc.ides.model.Organisation;

public interface OrganisationMapper {

	public Organisation selectOrgById(long id);

	public List<Organisation> selectOrganisationsForListing();

//	public long insertOrganisation(@Param("id") long id, @Param("name") String name, @Param("typeOfWork") String typeOfWork, @Param("address") String address, @Param("postcode") String postcode,
//			@Param("contact") String contact, @Param("email") String email, @Param("telephone") String telephone);
	
	public long insertOrganisation(@Param("org") Organisation org);
	
	public long selectNextId();
	
	public long deleteOrganisationById(@Param("id") long id);
}
