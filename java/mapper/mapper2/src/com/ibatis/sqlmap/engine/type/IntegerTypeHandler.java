package com.ibatis.sqlmap.engine.type;

import java.sql.*;

/**
 * User: Clinton Begin
 * Date: Sep 12, 2003
 * Time: 8:12:08 PM
 */
public class IntegerTypeHandler implements TypeHandler {

  public void setParameter(PreparedStatement ps, int i, Object parameter, String jdbcType)
      throws SQLException {
    ps.setInt(i, ((Integer) parameter).intValue());
  }

  public Object getResult(ResultSet rs, String columnName)
      throws SQLException {
    int i = rs.getInt(columnName);
    if (rs.wasNull()) {
      return null;
    } else {
      return new Integer(i);
    }
  }

  public Object getResult(ResultSet rs, int columnIndex)
      throws SQLException {
    int i = rs.getInt(columnIndex);
    if (rs.wasNull()) {
      return null;
    } else {
      return new Integer(i);
    }
  }

  public Object getResult(CallableStatement cs, int columnIndex)
      throws SQLException {
    int i = cs.getInt(columnIndex);
    if (cs.wasNull()) {
      return null;
    } else {
      return new Integer(i);
    }
  }

  public Object valueOf(String s) {
    return Integer.valueOf(s);
  }


  public boolean equals(Object object, String string) {
    if (object == null || string == null) {
      return object == string;
    } else {
      Object castedObject = valueOf(string);
      return object.equals(castedObject);
    }
  }

}
