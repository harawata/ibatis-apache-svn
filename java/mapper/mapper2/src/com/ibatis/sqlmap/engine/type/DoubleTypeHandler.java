package com.ibatis.sqlmap.engine.type;

import java.sql.*;

/**
 * User: Clinton Begin
 * Date: Sep 12, 2003
 * Time: 8:14:50 PM
 */
public class DoubleTypeHandler implements TypeHandler {

  public void setParameter(PreparedStatement ps, int i, Object parameter)
      throws SQLException {
    ps.setDouble(i, ((Double) parameter).doubleValue());
  }

  public Object getResult(ResultSet rs, String columnName)
      throws SQLException {
    double d = rs.getDouble(columnName);
    if (rs.wasNull()) {
      return null;
    } else {
      return new Double(d);
    }
  }

  public Object getResult(ResultSet rs, int columnIndex)
      throws SQLException {
    double d = rs.getDouble(columnIndex);
    if (rs.wasNull()) {
      return null;
    } else {
      return new Double(d);
    }
  }

  public Object getResult(CallableStatement cs, int columnIndex)
      throws SQLException {
    double d = cs.getDouble(columnIndex);
    if (cs.wasNull()) {
      return null;
    } else {
      return new Double(d);
    }
  }

  public Object valueOf(String s) {
    return Double.valueOf(s);
  }

}
