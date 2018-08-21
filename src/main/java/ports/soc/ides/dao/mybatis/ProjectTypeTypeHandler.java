package ports.soc.ides.dao.mybatis;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import ports.soc.ides.model.constant.ProjectType;


public class ProjectTypeTypeHandler implements TypeHandler<ProjectType> {

	@Override
	public void setParameter(PreparedStatement ps, int i, ProjectType projectType, JdbcType jdbcType) throws SQLException {
		String projTypeValue = null;
		if (projectType != null) {
			projTypeValue = projectType.getValue();
		}
		ps.setString(i, projTypeValue);
	}

	@Override
	public ProjectType getResult(ResultSet rs, String columnName) throws SQLException {
		return ProjectType.getProjectType(rs.getString(columnName));
	}

	@Override
	public ProjectType getResult(ResultSet rs, int columnIndex) throws SQLException {
		return ProjectType.getProjectType(rs.getString(columnIndex));
	}

	@Override
	public ProjectType getResult(CallableStatement cs, int columnIndex) throws SQLException {
		return ProjectType.getProjectType(cs.getString(columnIndex));
	}


}
