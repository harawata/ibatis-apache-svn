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

import com.ibatis.common.minixml.MiniDom;
import com.ibatis.common.minixml.MiniParser;
import com.ibatis.sqlmap.engine.mapping.parameter.ParameterMap;
import com.ibatis.sqlmap.engine.mapping.parameter.ParameterMapping;
import com.ibatis.sqlmap.engine.mapping.result.BasicResultMap;
import com.ibatis.sqlmap.engine.mapping.result.ResultMap;
import com.ibatis.sqlmap.engine.mapping.result.ResultMapping;
import com.ibatis.sqlmap.engine.scope.RequestScope;

import java.util.Map;

/**
 * User: Clinton Begin
 * Date: Sep 6, 2003
 * Time: 8:33:56 AM
 */
public class XmlDataExchange extends BaseDataExchange implements DataExchange {

  protected XmlDataExchange(DataExchangeFactory dataExchangeFactory) {
    super(dataExchangeFactory);
  }

  public void initialize(Map properties) {
  }

  public Object[] getData(RequestScope request, ParameterMap parameterMap, Object parameterObject) {
    MiniDom dom = new MiniParser((String) parameterObject).getDom();

    ParameterMapping[] mappings = parameterMap.getParameterMappings();
    Object[] values = new Object[mappings.length];

    for (int i = 0; i < mappings.length; i++) {
      values[i] = dom.getValue(mappings[i].getPropertyName());
    }

    return values;
  }

  public Object setData(RequestScope request, ResultMap resultMap, Object resultObject, Object[] values) {
    String name = ((BasicResultMap) resultMap).getXmlName();
    if (name == null) {
      name = "result";
    }

    MiniDom dom = new MiniDom(name);

    ResultMapping[] mappings = resultMap.getResultMappings();

    for (int i = 0; i < mappings.length; i++) {
      if (values[i] != null) {
        dom.setValue(mappings[i].getPropertyName(), values[i].toString());
      }
    }

    dom.setIgnoreRoot(request.getSession().isIgnoreDomRoot());

    return dom.toString();
  }

  public Object setData(RequestScope request, ParameterMap parameterMap, Object parameterObject, Object[] values) {
    MiniDom dom = new MiniDom("outparam");

    ParameterMapping[] mappings = parameterMap.getParameterMappings();

    for (int i = 0; i < mappings.length; i++) {
      if (values[i] != null) {
        dom.setValue(mappings[i].getPropertyName(), values[i].toString());
      }
    }

    return dom.toString();
  }

}
