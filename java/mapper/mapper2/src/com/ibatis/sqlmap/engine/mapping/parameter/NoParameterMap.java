package com.ibatis.sqlmap.engine.mapping.parameter;


import com.ibatis.sqlmap.engine.scope.*;

/**
 * User: Clinton Begin
 * Date: Nov 16, 2003
 * Time: 6:15:10 PM
 */
public class NoParameterMap extends BasicParameterMap {

  private static final ParameterMapping[] NO_PARAMETERS = new ParameterMapping[0];
  private static final Object[] NO_DATA = new Object[0];

  public ParameterMapping[] getParameterMappings() {
    return NO_PARAMETERS;
  }

  public Object[] getParameterObjectValues(RequestScope request, Object parameterObject) {
    return NO_DATA;
  }


}
