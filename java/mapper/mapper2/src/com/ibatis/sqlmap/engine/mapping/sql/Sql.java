package com.ibatis.sqlmap.engine.mapping.sql;

import com.ibatis.sqlmap.engine.mapping.parameter.*;
import com.ibatis.sqlmap.engine.mapping.result.*;

import com.ibatis.sqlmap.engine.scope.*;

/**
 * User: Clinton Begin
 * Date: Sep 12, 2003
 * Time: 7:37:44 PM
 */
public interface Sql {

  public String getSql(RequestScope request, Object parameterObject);

  public ParameterMap getParameterMap(RequestScope request, Object parameterObject);

  public ResultMap getResultMap(RequestScope request, Object parameterObject);

  public void cleanup(RequestScope request);

}
