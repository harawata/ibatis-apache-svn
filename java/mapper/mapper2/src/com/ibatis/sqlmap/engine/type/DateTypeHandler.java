package com.ibatis.sqlmap.engine.type;

import com.ibatis.sqlmap.client.SqlMapException;

import java.util.Date;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;

/**
 * User: Clinton Begin
 * Date: Sep 12, 2003
 * Time: 8:21:05 PM
 */
public class DateTypeHandler implements TypeHandler {

  private static final String DATE_FORMAT = "yyyy/MM/dd hh:mm:ss";
  private static final DateFormat format = new SimpleDateFormat(DATE_FORMAT);

  public void setParameter(PreparedStatement ps, int i, Object parameter)
      throws SQLException {
    ps.setTimestamp(i, new java.sql.Timestamp(((Date) parameter).getTime()));
  }

  public Object getResult(ResultSet rs, String columnName)
      throws SQLException {
    java.sql.Timestamp sqlTimestamp = rs.getTimestamp(columnName);
    if (rs.wasNull()) {
      return null;
    } else {
      return new java.util.Date(sqlTimestamp.getTime());
    }
  }

  public Object getResult(ResultSet rs, int columnIndex)
      throws SQLException {
    java.sql.Timestamp sqlTimestamp = rs.getTimestamp(columnIndex);
    if (rs.wasNull()) {
      return null;
    } else {
      return new java.util.Date(sqlTimestamp.getTime());
    }
  }

  public Object getResult(CallableStatement cs, int columnIndex)
      throws SQLException {
    java.sql.Timestamp sqlTimestamp = cs.getTimestamp(columnIndex);
    if (cs.wasNull()) {
      return null;
    } else {
      return new java.util.Date(sqlTimestamp.getTime());
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
