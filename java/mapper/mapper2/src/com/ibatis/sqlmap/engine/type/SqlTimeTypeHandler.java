package com.ibatis.sqlmap.engine.type;

import com.ibatis.sqlmap.client.SqlMapException;

import java.sql.*;
import java.text.ParseException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * User: Clinton Begin
 * Date: Sep 12, 2003
 * Time: 8:33:51 PM
 */
public class SqlTimeTypeHandler implements TypeHandler {

  private static final String DATE_FORMAT = "hh:mm:ss";
  private static final DateFormat format = new SimpleDateFormat(DATE_FORMAT);

  public void setParameter(PreparedStatement ps, int i, Object parameter, String jdbcType)
      throws SQLException {
    ps.setTime(i, (java.sql.Time) parameter);
  }

  public Object getResult(ResultSet rs, String columnName)
      throws SQLException {
    Object sqlTime = rs.getTime(columnName);
    if (rs.wasNull()) {
      return null;
    } else {
      return sqlTime;
    }
  }

  public Object getResult(ResultSet rs, int columnIndex)
      throws SQLException {
    Object sqlTime = rs.getTime(columnIndex);
    if (rs.wasNull()) {
      return null;
    } else {
      return sqlTime;
    }
  }

  public Object getResult(CallableStatement cs, int columnIndex)
      throws SQLException {
    Object sqlTime = cs.getTime(columnIndex);
    if (cs.wasNull()) {
      return null;
    } else {
      return sqlTime;
    }
  }

  public Object valueOf(String s) {
    try {
      java.util.Date date = format.parse(s);
      return new Time(date.getTime());
    } catch (ParseException e) {
      throw new SqlMapException("Error parsing default null value date.  Format must be '" + DATE_FORMAT + "'. Cause: " + e);
    }
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
