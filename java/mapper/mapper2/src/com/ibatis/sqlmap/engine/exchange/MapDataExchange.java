package com.ibatis.sqlmap.engine.exchange;

import com.ibatis.sqlmap.engine.mapping.parameter.*;
import com.ibatis.sqlmap.engine.mapping.result.*;

import com.ibatis.sqlmap.engine.scope.*;
import com.ibatis.common.exception.*;

import java.util.*;

/**
 * User: Clinton Begin
 * Date: Sep 6, 2003
 * Time: 8:31:37 AM
 */
public class MapDataExchange extends BaseDataExchange implements DataExchange {

  public void initialize(Map properties) {
  }

  public Object[] getData(RequestScope request, ParameterMap parameterMap, Object parameterObject) {
    if (!(parameterObject instanceof Map)) {
      throw new NestedRuntimeException("Error.  Object passed into MapDataExchange was not an instance of Map.");
    }

    Object[] data = new Object[parameterMap.getParameterMappings().length];
    Map map = (Map) parameterObject;
    ParameterMapping[] mappings = parameterMap.getParameterMappings();
    for (int i = 0; i < mappings.length; i++) {
      data[i] = map.get(mappings[i].getPropertyName());
    }
    return data;
  }

  public Object setData(RequestScope request, ResultMap resultMap, Object resultObject, Object[] values) {
    if (!(resultObject == null || resultObject instanceof Map)) {
      throw new NestedRuntimeException("Error.  Object passed into MapDataExchange was not an instance of Map.");
    }

    Map map = (Map) resultObject;
    if (map == null) {
      map = new HashMap();
    }

    ResultMapping[] mappings = resultMap.getResultMappings();
    for (int i = 0; i < mappings.length; i++) {
      map.put(mappings[i].getPropertyName(), values[i]);
    }

    return map;
  }

  public Object setData(RequestScope request, ParameterMap parameterMap, Object parameterObject, Object[] values) {
    if (!(parameterObject == null || parameterObject instanceof Map)) {
      throw new NestedRuntimeException("Error.  Object passed into MapDataExchange was not an instance of Map.");
    }

    Map map = (Map) parameterObject;
    if (map == null) {
      map = new HashMap();
    }

    ParameterMapping[] mappings = parameterMap.getParameterMappings();
    for (int i = 0; i < mappings.length; i++) {
      map.put(mappings[i].getPropertyName(), values[i]);
    }

    return map;
  }

}


