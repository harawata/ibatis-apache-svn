package com.ibatis.sqlmap.client.extensions;

import java.sql.SQLException;

/**
 * User: Clinton
 * Date: 1-Aug-2004
 * Time: 7:42:59 AM
 */
public interface TypeHandlerCallback {

  public void setParameter(ParameterSetter setter, Object parameter)
      throws SQLException;

  public Object getResult(ResultGetter getter)
      throws SQLException;

  public Object valueOf(String s);

}
