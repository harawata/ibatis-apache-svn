package com.ibatis.sqlmap.engine.mapping.sql.dynamic.elements;

import com.ibatis.sqlmap.engine.mapping.parameter.*;

import java.util.*;
import java.io.*;

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
