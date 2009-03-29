package org.apache.ibatis.mapping;

import java.util.List;

public class BoundSql {

  private String sql;
  private List<ParameterMapping> parameterMappings;

  public BoundSql(String sql, List<ParameterMapping> parameterMappings) {
    this.sql = sql;
    this.parameterMappings = parameterMappings;
  }

  public String getSql() {
    return sql;
  }

  public List<ParameterMapping> getParameterMappings() {
    return parameterMappings;
  }

}
