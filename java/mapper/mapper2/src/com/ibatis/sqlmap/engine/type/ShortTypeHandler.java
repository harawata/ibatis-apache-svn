package com.ibatis.sqlmap.engine.type;

import java.sql.*;

/**
 * User: Clinton Begin
 * Date: Sep 12, 2003
 * Time: 8:11:06 PM
 */
public class ShortTypeHandler implements TypeHandler {

  public void setParameter(PreparedStatement ps, int i, Object parameter, String jdbcType)
      throws SQLException {
    ps.setShort(i, ((Short) parameter).shortValue());
  }

  public Object getResult(ResultSet rs, String columnName)
      throws SQLException {
    short s = rs.getShort(columnName);
    if (rs.wasNull()) {
      return null;
    } else {
      return new Short(s);
    }
  }

  public Object getResult(ResultSet rs, int columnIndex)
      throws SQLException {
    short s = rs.getShort(columnIndex);
    if (rs.wasNull()) {
      return null;
    } else {
      return new Short(s);
    }
  }

  public Object getResult(CallableStatement cs, int columnIndex)
      throws SQLException {
    short s = cs.getShort(columnIndex);
    if (cs.wasNull()) {
      return null;
    } else {
      return new Short(s);
    }
  }

  public Object valueOf(String s) {
    return Short.valueOf(s);
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
