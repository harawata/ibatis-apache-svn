package org.apache.ibatis.builder;

import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.CallableStatement;

public class ExampleTypeHandler implements TypeHandler {

  public void setParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
    ps.setString(i,(String)parameter);
  }

  public Object getResult(ResultSet rs, String columnName) throws SQLException {
    return rs.getString(columnName);
  }

  public Object getResult(CallableStatement cs, int columnIndex) throws SQLException {
    return cs.getString(columnIndex);
  }

}
