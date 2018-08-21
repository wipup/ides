package ports.soc.ides.dao.mybatis;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import ports.soc.ides.model.constant.OwnerType;


public class OwnerTypeTypeHandler implements TypeHandler<OwnerType> {

	@Override
	public void setParameter(PreparedStatement ps, int i, OwnerType ownerType, JdbcType jdbcType) throws SQLException {
		String ownerTypeValue = null;
		if (ownerType != null) {
			ownerTypeValue = ownerType.getValue();
		}
		ps.setString(i, ownerTypeValue);
	}

	@Override
	public OwnerType getResult(ResultSet rs, String columnName) throws SQLException {
		return OwnerType.getOwnerType(rs.getString(columnName));
	}

	@Override
	public OwnerType getResult(ResultSet rs, int columnIndex) throws SQLException {
		return OwnerType.getOwnerType(rs.getString(columnIndex));
	}

	@Override
	public OwnerType getResult(CallableStatement cs, int columnIndex) throws SQLException {
		return OwnerType.getOwnerType(cs.getString(columnIndex));
	}


}
