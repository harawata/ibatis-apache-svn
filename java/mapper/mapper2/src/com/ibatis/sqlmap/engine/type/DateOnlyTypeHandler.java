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
 * Time: 8:36:15 PM
 */
public class DateOnlyTypeHandler implements TypeHandler {

  private static final String DATE_FORMAT = "yyyy/MM/dd";
  private static final DateFormat format = new SimpleDateFormat(DATE_FORMAT);

  public void setParameter(PreparedStatement ps, int i, Object parameter, String jdbcType)
      throws SQLException {
    ps.setDate(i, new java.sql.Date(((Date) parameter).getTime()));
  }

  public Object getResult(ResultSet rs, String columnName)
      throws SQLException {
    java.sql.Date sqlDate = rs.getDate(columnName);
    if (rs.wasNull()) {
      return null;
    } else {
      return new java.util.Date(sqlDate.getTime());
    }
  }

  public Object getResult(ResultSet rs, int columnIndex)
      throws SQLException {
    java.sql.Date sqlDate = rs.getDate(columnIndex);
    if (rs.wasNull()) {
      return null;
    } else {
      return new java.util.Date(sqlDate.getTime());
    }
  }

  public Object getResult(CallableStatement cs, int columnIndex)
      throws SQLException {
    java.sql.Date sqlDate = cs.getDate(columnIndex);
    if (cs.wasNull()) {
      return null;
    } else {
      return new java.util.Date(sqlDate.getTime());
    }
  }

  public Object valueOf(String s) {
    try {
      return format.parse(s);
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
