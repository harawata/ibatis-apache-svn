package com.ibatis.sqlmap.engine.type;

import java.sql.*;

/**
 * User: Clinton Begin
 * Date: Sep 12, 2003
 * Time: 8:19:37 PM
 */
public class StringTypeHandler implements TypeHandler {

  public void setParameter(PreparedStatement ps, int i, Object parameter, String jdbcType)
      throws SQLException {
    ps.setString(i, ((String) parameter));
  }

  public Object getResult(ResultSet rs, String columnName)
      throws SQLException {
    Object s = rs.getString(columnName);
    if (rs.wasNull()) {
      return null;
    } else {
      return s;
    }
  }

  public Object getResult(ResultSet rs, int columnIndex)
      throws SQLException {
    Object s = rs.getString(columnIndex);
    if (rs.wasNull()) {
      return null;
    } else {
      return s;
    }
  }

  public Object getResult(CallableStatement cs, int columnIndex)
      throws SQLException {
    Object s = cs.getString(columnIndex);
    if (cs.wasNull()) {
      return null;
    } else {
      return s;
    }
  }

  public Object valueOf(String s) {
    return s;
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
