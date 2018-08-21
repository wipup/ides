package ports.soc.ides.dao.mybatis;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import ports.soc.ides.controller.helper.ExtendedOrganisation;

public interface ExtendedOrganisationMapper {
	
	public List<ExtendedOrganisation> selectOrganisationWithIdeaCountForListing();
	
	public long countIdeaAssociatingWithOrganisation(long id);
	
	public List<ExtendedOrganisation> selectOrganisationWithIdeaCountForListingAndSearch(@Param("searchText") String searchText);
}
