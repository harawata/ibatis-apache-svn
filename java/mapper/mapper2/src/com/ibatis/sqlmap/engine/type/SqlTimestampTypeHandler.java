package com.ibatis.sqlmap.engine.type;

import com.ibatis.sqlmap.client.SqlMapException;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;

/**
 * User: Clinton Begin
 * Date: Sep 12, 2003
 * Time: 8:34:30 PM
 */
public class SqlTimestampTypeHandler implements TypeHandler {

  private static final String DATE_FORMAT = "yyyy/MM/dd hh:mm:ss";
  private static final DateFormat format = new SimpleDateFormat(DATE_FORMAT);

  public void setParameter(PreparedStatement ps, int i, Object parameter, String jdbcType)
      throws SQLException {
    ps.setTimestamp(i, (java.sql.Timestamp) parameter);
  }

  public Object getResult(ResultSet rs, String columnName)
      throws SQLException {
    Object sqlTimestamp = rs.getTimestamp(columnName);
    if (rs.wasNull()) {
      return null;
    } else {
      return sqlTimestamp;
    }
  }

  public Object getResult(ResultSet rs, int columnIndex)
      throws SQLException {
    Object sqlTimestamp = rs.getTimestamp(columnIndex);
    if (rs.wasNull()) {
      return null;
    } else {
      return sqlTimestamp;
    }
  }

  public Object getResult(CallableStatement cs, int columnIndex)
      throws SQLException {
    Object sqlTimestamp = cs.getTimestamp(columnIndex);
    if (cs.wasNull()) {
      return null;
    } else {
      return sqlTimestamp;
    }
  }

  public Object valueOf(String s) {
    try {
      java.util.Date date = format.parse(s);
      return new Timestamp(date.getTime());
    } catch (ParseException e) {
      throw new SqlMapException("Error parsing default null value date.  Format must be '" + DATE_FORMAT + "'. Cause: " + e);
    }
  }

}
