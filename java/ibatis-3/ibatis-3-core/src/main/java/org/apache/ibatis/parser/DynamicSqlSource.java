package org.apache.ibatis.parser;

import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.BoundSql;

import java.util.List;

public class DynamicSqlSource implements SqlSource {

  public BoundSql getBoundSql(Object parameterObject) {
    return null;
  }
}
