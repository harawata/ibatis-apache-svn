package org.apache.ibatis.parser;

import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlSource;

import java.util.List;

public class InlineSqlSource implements SqlSource {

  private String sql;
  private List<ParameterMapping> parameterMappings;

  public InlineSqlSource(String sql, List<ParameterMapping> parameterMappings) {
    this.sql = sql;
    this.parameterMappings = parameterMappings;
  }

  public String getSql(Object parameterObject) {
    return sql;
  }

  public List<ParameterMapping> getParameterMappings(Object parameterObject) {
    return parameterMappings;
  }
}
