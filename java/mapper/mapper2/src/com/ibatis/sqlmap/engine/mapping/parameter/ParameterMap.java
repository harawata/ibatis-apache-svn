package com.ibatis.sqlmap.engine.mapping.parameter;


import com.ibatis.sqlmap.engine.scope.*;
import com.ibatis.sqlmap.engine.cache.*;

import java.sql.*;

/**
 * User: Clinton Begin
 * Date: Nov 16, 2003
 * Time: 9:52:21 PM
 */
public interface ParameterMap {

  public void setParameters(RequestScope request, PreparedStatement ps, Object[] parameters)
      throws SQLException;

  public Object[] getParameterObjectValues(RequestScope request, Object parameterObject);

  public CacheKey getCacheKey(RequestScope request, Object parameterObject);

  public void refreshParameterObjectValues(RequestScope request, Object parameterObject, Object[] values);

  public ParameterMapping[] getParameterMappings();

  public Class getParameterClass();

}
