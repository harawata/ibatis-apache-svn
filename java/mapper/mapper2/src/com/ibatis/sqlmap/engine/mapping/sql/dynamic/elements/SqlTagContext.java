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
package com.ibatis.sqlmap.engine.mapping.sql.dynamic.elements;

import com.ibatis.sqlmap.engine.mapping.parameter.ParameterMapping;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * User: Clinton Begin
 * Date: Nov 18, 2003
 * Time: 10:13:46 PM
 */
public class SqlTagContext {

  private StringWriter sw;
  private PrintWriter out;

  private HashMap attributes;

  private boolean overridePrepend;
  private SqlTag firstNonDynamicTagWithPrepend;

  private ArrayList parameterMappings = new ArrayList();


  public SqlTagContext() {
    sw = new StringWriter();
    out = new PrintWriter(sw);
    attributes = new HashMap();
    overridePrepend = false;
  }

  public PrintWriter getWriter() {
    return out;
  }

  public String getBodyText() {
    out.flush();
    return sw.getBuffer().toString();
  }

  public boolean isOverridePrepend() {
    return overridePrepend;
  }

  public void setOverridePrepend(boolean overridePrepend) {
    this.overridePrepend = overridePrepend;
  }

  public SqlTag getFirstNonDynamicTagWithPrepend() {
    return firstNonDynamicTagWithPrepend;
  }

  public void setFirstNonDynamicTagWithPrepend(SqlTag firstNonDynamicTagWithPrepend) {
    this.firstNonDynamicTagWithPrepend = firstNonDynamicTagWithPrepend;
  }

  public void setAttribute(Object key, Object value) {
    attributes.put(key, value);
  }

  public Object getAttribute(Object key) {
    return attributes.get(key);
  }

  public void addParameterMapping(ParameterMapping mapping) {
    parameterMappings.add(mapping);
  }

  public List getParameterMappings() {
    return parameterMappings;
  }

}
