package io.tomahawkd.detect.database.mapper.util;

import io.tomahawkd.detect.TreeCode;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(TreeCode.class)
public class TreeCodeTypeHandler extends BaseTypeHandler<TreeCode> {

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, TreeCode parameter, JdbcType jdbcType)
			throws SQLException {
		ps.setLong(i, parameter.getRaw());
	}

	@Override
	public TreeCode getNullableResult(ResultSet rs, String columnName) throws SQLException {
		return new TreeCode(rs.getLong(columnName), Long.SIZE);
	}

	@Override
	public TreeCode getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		return new TreeCode(rs.getLong(columnIndex), Long.SIZE);
	}

	@Override
	public TreeCode getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		return new TreeCode(cs.getLong(columnIndex), Long.SIZE);
	}

}
