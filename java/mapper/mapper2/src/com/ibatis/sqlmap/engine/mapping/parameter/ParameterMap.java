package com.ibatis.sqlmap.engine.mapping.parameter;


import com.ibatis.sqlmap.engine.cache.CacheKey;
import com.ibatis.sqlmap.engine.scope.RequestScope;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * User: Clinton Begin
 * Date: Nov 16, 2003
 * Time: 9:52:21 PM
 */
public interface ParameterMap {

  public String getId();

  public void setParameters(RequestScope request, PreparedStatement ps, Object[] parameters)
      throws SQLException;

  public Object[] getParameterObjectValues(RequestScope request, Object parameterObject);

  public CacheKey getCacheKey(RequestScope request, Object parameterObject);

  public void refreshParameterObjectValues(RequestScope request, Object parameterObject, Object[] values);

  public ParameterMapping[] getParameterMappings();

  public Class getParameterClass();

}
