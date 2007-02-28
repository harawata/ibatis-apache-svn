package com.ibatis.sqlmap.engine.mapping.sql.raw;

import com.ibatis.sqlmap.engine.mapping.sql.Sql;
import com.ibatis.sqlmap.engine.mapping.parameter.ParameterMap;
import com.ibatis.sqlmap.engine.mapping.result.ResultMap;
import com.ibatis.sqlmap.engine.scope.RequestScope;

/**
 * A non-executable SQL container simply for
 * communicating raw SQL around the framework.
 */
public class RawSql implements Sql {

  private String sql;

  public RawSql(String sql) {
    this.sql = sql;
  }

  public String getSql(RequestScope request, Object parameterObject) {
    return sql;
  }

  public ParameterMap getParameterMap(RequestScope request, Object parameterObject) {
    throw new RuntimeException ("Method not implemented on RawSql.");
  }

  public ResultMap getResultMap(RequestScope request, Object parameterObject) {
    throw new RuntimeException ("Method not implemented on RawSql.");
  }

  public void cleanup(RequestScope request) {
    throw new RuntimeException ("Method not implemented on RawSql.");
  }
}
