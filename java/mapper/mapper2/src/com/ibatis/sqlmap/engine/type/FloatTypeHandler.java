package com.ibatis.sqlmap.engine.type;

import java.sql.*;

/**
 * User: Clinton Begin
 * Date: Sep 12, 2003
 * Time: 8:14:04 PM
 */
public class FloatTypeHandler implements TypeHandler {

  public void setParameter(PreparedStatement ps, int i, Object parameter, String jdbcType)
      throws SQLException {
    ps.setFloat(i, ((Float) parameter).floatValue());
  }

  public Object getResult(ResultSet rs, String columnName)
      throws SQLException {
    float f = rs.getFloat(columnName);
    if (rs.wasNull()) {
      return null;
    } else {
      return new Float(f);
    }
  }

  public Object getResult(ResultSet rs, int columnIndex)
      throws SQLException {
    float f = rs.getFloat(columnIndex);
    if (rs.wasNull()) {
      return null;
    } else {
      return new Float(f);
    }
  }

  public Object getResult(CallableStatement cs, int columnIndex)
      throws SQLException {
    float f = cs.getFloat(columnIndex);
    if (cs.wasNull()) {
      return null;
    } else {
      return new Float(f);
    }
  }

  public Object valueOf(String s) {
    return Float.valueOf(s);
  }

}
