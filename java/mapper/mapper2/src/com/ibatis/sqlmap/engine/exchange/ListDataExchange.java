package com.ibatis.sqlmap.engine.exchange;

import com.ibatis.sqlmap.engine.mapping.parameter.*;
import com.ibatis.sqlmap.engine.mapping.result.*;

import com.ibatis.sqlmap.engine.scope.*;

import java.util.*;

/**
 * User: Clinton Begin
 * Date: Nov 22, 2003
 * Time: 6:25:30 PM
 */
public class ListDataExchange extends BaseDataExchange implements DataExchange {


  public void initialize(Map properties) {
  }

  public Object[] getData(RequestScope request, ParameterMap parameterMap, Object parameterObject) {
    ParameterMapping[] mappings = parameterMap.getParameterMappings();
    Object[] data = new Object[mappings.length];
    for (int i = 0; i < mappings.length; i++) {
      String propName = mappings[i].getPropertyName();
      int index = Integer.parseInt((propName.substring(1, propName.length() - 1)));
      data[i] = ((List) parameterObject).get(index);
    }
    return data;
  }

  public Object setData(RequestScope request, ResultMap resultMap, Object resultObject, Object[] values) {
    ResultMapping[] mappings = resultMap.getResultMappings();
    List data = new ArrayList();
    for (int i = 0; i < mappings.length; i++) {
      String propName = mappings[i].getPropertyName();
      int index = Integer.parseInt((propName.substring(1, propName.length() - 1)));
      data.set(index, values[i]);
    }
    return data;
  }

  public Object setData(RequestScope request, ParameterMap parameterMap, Object parameterObject, Object[] values) {
    ParameterMapping[] mappings = parameterMap.getParameterMappings();
    List data = new ArrayList();
    for (int i = 0; i < mappings.length; i++) {
      String propName = mappings[i].getPropertyName();
      int index = Integer.parseInt((propName.substring(1, propName.length() - 1)));
      data.set(index, values[i]);
    }
    return data;
  }

}
