/*
 *  Copyright 2004 Clinton Begin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.ibatis.sqlmap.engine.exchange;

import com.ibatis.sqlmap.engine.mapping.parameter.ParameterMap;
import com.ibatis.sqlmap.engine.mapping.parameter.ParameterMapping;
import com.ibatis.sqlmap.engine.mapping.result.ResultMap;
import com.ibatis.sqlmap.engine.mapping.result.ResultMapping;
import com.ibatis.sqlmap.engine.scope.RequestScope;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * User: Clinton Begin
 * Date: Nov 22, 2003
 * Time: 6:25:30 PM
 */
public class ListDataExchange extends BaseDataExchange implements DataExchange {

  protected ListDataExchange(DataExchangeFactory dataExchangeFactory) {
    super(dataExchangeFactory);
  }

  public void initialize(Map properties) {
  }

  public Object[] getData(RequestScope request, ParameterMap parameterMap, Object parameterObject) {
    ParameterMapping[] mappings = parameterMap.getParameterMappings();
    Object[] data = new Object[mappings.length];
    for (int i = 0; i < mappings.length; i++) {
      String propName = mappings[i].getPropertyName();
      int index = Integer.parseInt((propName.substring(propName.indexOf('[') + 1, propName.length() - 1)));
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
