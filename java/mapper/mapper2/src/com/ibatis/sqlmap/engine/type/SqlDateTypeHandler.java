package com.ibatis.sqlmap.engine.type;

import com.ibatis.sqlmap.client.SqlMapException;

import java.sql.*;
import java.text.ParseException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * User: Clinton Begin
 * Date: Sep 12, 2003
 * Time: 8:32:05 PM
 */
public class SqlDateTypeHandler extends BaseTypeHandler implements TypeHandler {

  private static final String DATE_FORMAT = "yyyy/MM/dd";
  private static final DateFormat format = new SimpleDateFormat(DATE_FORMAT);

  public void setParameter(PreparedStatement ps, int i, Object parameter, String jdbcType)
      throws SQLException {
    ps.setDate(i, (java.sql.Date) parameter);
  }

  public Object getResult(ResultSet rs, String columnName)
      throws SQLException {
    Object sqlDate = rs.getDate(columnName);
    if (rs.wasNull()) {
      return null;
    } else {
      return sqlDate;
    }
  }

  public Object getResult(ResultSet rs, int columnIndex)
      throws SQLException {
    Object sqlDate = rs.getDate(columnIndex);
    if (rs.wasNull()) {
      return null;
    } else {
      return sqlDate;
    }
  }

  public Object getResult(CallableStatement cs, int columnIndex)
      throws SQLException {
    Object sqlDate = cs.getDate(columnIndex);
    if (cs.wasNull()) {
      return null;
    } else {
      return sqlDate;
    }
  }

  public Object valueOf(String s) {
    try {
      java.util.Date date = format.parse(s);
      return new Date(date.getTime());
    } catch (ParseException e) {
      throw new SqlMapException("Error parsing default null value date.  Format must be '" + DATE_FORMAT + "'. Cause: " + e);
    }
  }

}
