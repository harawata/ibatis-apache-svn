package com.ibatis.sqlmap.engine.type;

import com.ibatis.sqlmap.client.SqlMapException;

import java.util.Date;
import java.sql.*;
import java.text.ParseException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * User: Clinton Begin
 * Date: Sep 12, 2003
 * Time: 8:36:06 PM
 */
public class TimeOnlyTypeHandler implements TypeHandler {

  private static final String DATE_FORMAT = "hh:mm:ss";
  private static final DateFormat format = new SimpleDateFormat(DATE_FORMAT);

  public void setParameter(PreparedStatement ps, int i, Object parameter)
      throws SQLException {
    ps.setTime(i, new java.sql.Time(((Date) parameter).getTime()));
  }

  public Object getResult(ResultSet rs, String columnName)
      throws SQLException {
    java.sql.Time sqlTime = rs.getTime(columnName);
    if (rs.wasNull()) {
      return null;
    } else {
      return new java.util.Date(sqlTime.getTime());
    }
  }

  public Object getResult(ResultSet rs, int columnIndex)
      throws SQLException {
    java.sql.Time sqlTime = rs.getTime(columnIndex);
    if (rs.wasNull()) {
      return null;
    } else {
      return new java.util.Date(sqlTime.getTime());
    }
  }

  public Object getResult(CallableStatement cs, int columnIndex)
      throws SQLException {
    java.sql.Time sqlTime = cs.getTime(columnIndex);
    if (cs.wasNull()) {
      return null;
    } else {
      return new java.util.Date(sqlTime.getTime());
    }
  }

  public Object valueOf(String s) {
    try {
      return format.parse(s);
    } catch (ParseException e) {
      throw new SqlMapException("Error parsing default null value date.  Format must be '" + DATE_FORMAT + "'. Cause: " + e);
    }
  }

}
