package com.ibatis.sqlmap.engine.mapping.result;


import com.ibatis.sqlmap.engine.scope.RequestScope;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * User: Clinton Begin
 * Date: Nov 16, 2003
 * Time: 9:49:48 PM
 */
public interface ResultMap {

  public String getId();

  public Object[] getResults(RequestScope request, ResultSet rs)
      throws SQLException;

  public Object setResultObjectValues(RequestScope request, Object resultObject, Object[] values);

  public ResultMapping[] getResultMappings();

  public Class getResultClass();

}
