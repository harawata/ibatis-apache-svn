package com.ibatis.sqlmap.engine.type;

import java.sql.*;

/**
 * User: Clinton Begin
 * Date: Sep 12, 2003
 * Time: 8:06:36 PM
 */
public class BooleanTypeHandler implements TypeHandler {

  public void setParameter(PreparedStatement ps, int i, Object parameter)
      throws SQLException {
    ps.setBoolean(i, ((Boolean) parameter).booleanValue());
  }

  public Object getResult(ResultSet rs, String columnName)
      throws SQLException {
    boolean b = rs.getBoolean(columnName);
    if (rs.wasNull()) {
      return null;
    } else {
      return new Boolean(b);
    }
  }

  public Object getResult(ResultSet rs, int columnIndex)
      throws SQLException {
    boolean b = rs.getBoolean(columnIndex);
    if (rs.wasNull()) {
      return null;
    } else {
      return new Boolean(b);
    }
  }

  public Object getResult(CallableStatement cs, int columnIndex)
      throws SQLException {
    boolean b = cs.getBoolean(columnIndex);
    if (cs.wasNull()) {
      return null;
    } else {
      return new Boolean(b);
    }
  }

  public Object valueOf(String s) {
    return Boolean.valueOf(s);
  }

}
