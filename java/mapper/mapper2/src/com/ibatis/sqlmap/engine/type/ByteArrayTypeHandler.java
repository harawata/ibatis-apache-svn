package com.ibatis.sqlmap.engine.type;

import java.sql.*;

/**
 * User: Clinton Begin
 * Date: Sep 12, 2003
 * Time: 8:24:33 PM
 */
public class ByteArrayTypeHandler implements TypeHandler {

  public void setParameter(PreparedStatement ps, int i, Object parameter, String jdbcType)
      throws SQLException {
    ps.setBytes(i, (byte[]) parameter);
  }

  public Object getResult(ResultSet rs, String columnName)
      throws SQLException {
    Object bytes = rs.getBytes(columnName);
    if (rs.wasNull()) {
      return null;
    } else {
      return bytes;
    }
  }

  public Object getResult(ResultSet rs, int columnIndex)
      throws SQLException {
    Object bytes = rs.getBytes(columnIndex);
    if (rs.wasNull()) {
      return null;
    } else {
      return bytes;
    }
  }

  public Object getResult(CallableStatement cs, int columnIndex)
      throws SQLException {
    Object bytes = cs.getBytes(columnIndex);
    if (cs.wasNull()) {
      return null;
    } else {
      return bytes;
    }
  }

  public Object valueOf(String s) {
    return s.getBytes();
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
