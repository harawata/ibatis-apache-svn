package org.apache.ibatis.parser;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlSource;

import java.util.List;

public class StaticSqlSource implements SqlSource {


  private BoundSql boundSql;

  public StaticSqlSource(String sql) {
    this.boundSql = new BoundSql(sql, null);
  }

  public StaticSqlSource(String sql, List<ParameterMapping> parameterMappings) {
    this.boundSql = new BoundSql(sql, parameterMappings);
  }

  public BoundSql getBoundSql(Object parameterObject) {
    return boundSql;
  }

}
