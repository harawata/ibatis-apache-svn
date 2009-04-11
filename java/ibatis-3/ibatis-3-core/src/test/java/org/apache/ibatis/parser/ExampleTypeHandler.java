package org.apache.ibatis.parser;

import org.apache.ibatis.type.*;

import java.sql.*;

public class ExampleTypeHandler implements TypeHandler {

  public void setParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
    ps.setString(i, (String) parameter);
  }

  public Object getResult(ResultSet rs, String columnName) throws SQLException {
    return rs.getString(columnName);
  }

  public Object getResult(CallableStatement cs, int columnIndex) throws SQLException {
    return cs.getString(columnIndex);
  }

}
