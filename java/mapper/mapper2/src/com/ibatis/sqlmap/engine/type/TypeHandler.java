package com.ibatis.sqlmap.engine.type;

import java.sql.*;

/**
 * User: Clinton Begin
 * Date: Sep 5, 2003
 * Time: 9:37:13 AM
 */
public interface TypeHandler {

  public void setParameter(PreparedStatement ps, int i, Object parameter)
      throws SQLException;

  public Object getResult(ResultSet rs, String columnName)
      throws SQLException;

  public Object getResult(ResultSet rs, int columnIndex)
      throws SQLException;

  public Object getResult(CallableStatement cs, int columnIndex)
      throws SQLException;

  public Object valueOf(String s);

}
