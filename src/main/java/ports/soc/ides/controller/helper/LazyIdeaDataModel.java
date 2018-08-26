package ports.soc.ides.controller.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.apache.ibatis.session.SqlSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import ports.soc.ides.dao.SqlSessionProvider;
import ports.soc.ides.dao.mybatis.IdeaMapper;
import ports.soc.ides.interceptor.annotation.LogPerformance;
import ports.soc.ides.model.Idea;
import ports.soc.ides.model.constant.IdeaStatus;
import ports.soc.ides.model.constant.ProjectType;
import ports.soc.ides.util.IdesUtils;

/**
 * Enable lazy-loading behavior for Idea data table
 * 
 * @author WIPU
 *
 */
@Dependent
public class LazyIdeaDataModel extends LazyDataModel<Idea>  {

	@Inject
	private SqlSessionProvider sqlProvier;
	
	private static final long serialVersionUID = -340739539400680311L;
	
	private static final Logger log = LogManager.getRootLogger();

	private List<ProjectType> filterType;
	private List<IdeaStatus> filterStatus;
	
	private List<Idea> dataSource;
	
	private String searchKeyword;
	
	@Override
	public Idea getRowData(String rowKey) {
		try {
			int index = Integer.parseInt(rowKey);
			return dataSource.get(index);
		} catch (Exception e) {
			log.warn("getRowData: rowKey=" + rowKey, e);
		}
		return null;
	}

	@Override
	public Object getRowKey(Idea idea) {
		return String.valueOf(idea.getId());
	}
	
	@LogPerformance(note = "load idea", logParameters = false)
	@Override
	public List<Idea> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {	

		StringBuilder sb = new StringBuilder();
		sb.append("query ideas: first=").append(first).append(", pageSize=").append(pageSize).append(", sortField=").append(sortField).append(", sortOrder=").append(sortOrder)
				.append(", status=").append(IdesUtils.deepPrint(filterStatus)).append(", type=")
				.append(IdesUtils.deepPrint(filterType)).append(", searchText=").append(searchKeyword);
		log.info(sb.toString());
		
		List<Idea> result = new ArrayList<>();
		try (SqlSession sql = sqlProvier.getSqlSession()){
			
			int lastRecord = first + pageSize;
			
			IdeaMapper ideaMapper = sql.getMapper(IdeaMapper.class);
			long totalRowCount = ideaMapper.countIdeasForListing(filterStatus, filterType, searchKeyword);			
			result = ideaMapper.selectIdeasForListing(filterStatus, filterType, sortField, sortOrder, first, lastRecord, searchKeyword);
			
			log.debug("loaded resultSet size=" + result.size());
			
			dataSource = result;
			setWrappedData(dataSource);
			setRowCount((int) totalRowCount);
			
		} catch (Exception e) {
			log.error(e);
		}
		
		log.debug("load result: totalRowCount=" + getRowCount() + " and resultSet: " + result.size());
		return result;
	}
	
	public Idea getIdeaByIndex(int index) {
		if (IdesUtils.isEmpty(dataSource)) {
			return null;
		}
		return dataSource.get(index % dataSource.size());
	}
	
	public Idea getIdeaByIndex(String index) {
		return getIdeaByIndex(Integer.parseInt(index));
	}

	public void setFilterType(List<ProjectType> filterType) {
		this.filterType = filterType;
	}

	public void setFilterStatus(List<IdeaStatus> filterStatus) {
		this.filterStatus = filterStatus;
	}

	public void setSearchKeyword(String searchKeyword) {
		this.searchKeyword = searchKeyword;
	}

	public int getDatasourceSize() {
		return dataSource.size();
	}
}
