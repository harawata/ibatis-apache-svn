package com.ibatis.sqlmap.engine.type;

import java.sql.*;
import java.math.*;

/**
 * User: Clinton Begin
 * Date: Sep 12, 2003
 * Time: 8:22:44 PM
 */
public class BigDecimalTypeHandler implements TypeHandler {

  public void setParameter(PreparedStatement ps, int i, Object parameter)
      throws SQLException {
    ps.setBigDecimal(i, ((BigDecimal) parameter));
  }

  public Object getResult(ResultSet rs, String columnName)
      throws SQLException {
    Object bigdec = rs.getBigDecimal(columnName);
    if (rs.wasNull()) {
      return null;
    } else {
      return bigdec;
    }
  }

  public Object getResult(ResultSet rs, int columnIndex)
      throws SQLException {
    Object bigdec = rs.getBigDecimal(columnIndex);
    if (rs.wasNull()) {
      return null;
    } else {
      return bigdec;
    }
  }

  public Object getResult(CallableStatement cs, int columnIndex)
      throws SQLException {
    Object bigdec = cs.getBigDecimal(columnIndex);
    if (cs.wasNull()) {
      return null;
    } else {
      return bigdec;
    }
  }

  public Object valueOf(String s) {
    return java.math.BigDecimal.valueOf(Long.valueOf(s).longValue());
  }

}
