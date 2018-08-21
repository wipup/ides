package ports.soc.ides.dao.mybatis;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import ports.soc.ides.model.constant.IdeaStatus;

public class IdeaStatusTypeHandler implements TypeHandler<IdeaStatus>  {

	@Override
	public void setParameter(PreparedStatement ps, int i, IdeaStatus ideaStatus, JdbcType jdbcType) throws SQLException {
		String statusValue = null;
		if (ideaStatus != null) {
			statusValue = ideaStatus.getValue();
		}
		ps.setString(i, statusValue);
	}

	@Override
	public IdeaStatus getResult(ResultSet rs, String columnName) throws SQLException {
		return IdeaStatus.getIdeaStatus(rs.getString(columnName));
	}

	@Override
	public IdeaStatus getResult(ResultSet rs, int columnIndex) throws SQLException {
		return IdeaStatus.getIdeaStatus(rs.getString(columnIndex));
	}

	@Override
	public IdeaStatus getResult(CallableStatement cs, int columnIndex) throws SQLException {		
		return IdeaStatus.getIdeaStatus(cs.getString(columnIndex));
	}

		
}
