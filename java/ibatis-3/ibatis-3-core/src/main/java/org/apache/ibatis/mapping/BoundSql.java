package org.apache.ibatis.mapping;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class BoundSql {

  private String sql;
  private List<ParameterMapping> parameterMappings;
  private Object parameterObject;
  private Map<String,Object> additionalParameters;

  public BoundSql(String sql, List<ParameterMapping> parameterMappings, Object parameterObject) {
    this.sql = sql;
    this.parameterMappings = parameterMappings;
    this.parameterObject = parameterObject;
    this.additionalParameters = new HashMap<String,Object>();
  }

  public String getSql() {
    return sql;
  }

  public List<ParameterMapping> getParameterMappings() {
    return parameterMappings;
  }

  public Object getParameterObject() {
    return parameterObject;
  }

  public boolean hasAdditionalParameter(String name) {
    return additionalParameters.containsKey(name);
  }

  public void setAdditionalParameter(String name, Object value) {
    additionalParameters.put(name, value);
  }

  public Object getAdditionalParameter(String name) {
    return additionalParameters.get(name);
  }
}
