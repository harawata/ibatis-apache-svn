package com.ibatis.sqlmap.engine.exchange;

import com.ibatis.sqlmap.engine.mapping.parameter.*;
import com.ibatis.sqlmap.engine.mapping.result.*;

import com.ibatis.sqlmap.engine.scope.*;

import java.util.*;

/**
 * User: Clinton Begin
 * Date: Sep 6, 2003
 * Time: 8:31:18 AM
 */
public class PrimitiveDataExchange extends BaseDataExchange implements DataExchange {

  public void initialize(Map properties) {
  }

  public Object[] getData(RequestScope request, ParameterMap parameterMap, Object parameterObject) {
    ParameterMapping[] mappings = parameterMap.getParameterMappings();
    Object[] data = new Object[mappings.length];
    for (int i = 0; i < mappings.length; i++) {
      data[i] = parameterObject;
    }
    return data;
  }

  public Object setData(RequestScope request, ResultMap resultMap, Object resultObject, Object[] values) {
    return values[0];
  }

  public Object setData(RequestScope request, ParameterMap parameterMap, Object parameterObject, Object[] values) {
    return values[0];
  }

}
