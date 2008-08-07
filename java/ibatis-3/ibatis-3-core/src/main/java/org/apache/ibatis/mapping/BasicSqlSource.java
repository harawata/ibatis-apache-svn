package org.apache.ibatis.mapping;

import java.util.List;

public class BasicSqlSource implements SqlSource {

  private String sql;

  public BasicSqlSource(String sql) {
    this.sql = sql;
  }

  public String getSql(Object parameterObject) {
    return sql;
  }

  public List<ParameterMapping> getParameterMappings(Object parameterObject) {
    return null;
  }
}
