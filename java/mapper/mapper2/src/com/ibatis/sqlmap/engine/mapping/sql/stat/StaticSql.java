package com.ibatis.sqlmap.engine.mapping.sql.stat;

import com.ibatis.sqlmap.engine.mapping.parameter.ParameterMap;
import com.ibatis.sqlmap.engine.mapping.result.ResultMap;
import com.ibatis.sqlmap.engine.mapping.sql.Sql;
import com.ibatis.sqlmap.engine.scope.RequestScope;

/**
 * User: Clinton Begin
 * Date: Sep 13, 2003
 * Time: 6:49:56 PM
 */
public class StaticSql implements Sql {

  private String sqlStatement;

  public StaticSql(String sqlStatement) {
    this.sqlStatement = sqlStatement.replace('\r', ' ').replace('\n', ' ');
  }

  public String getSql(RequestScope request, Object parameterObject) {
    return sqlStatement;
  }

  public ParameterMap getParameterMap(RequestScope request, Object parameterObject) {
    return request.getParameterMap();
  }

  public ResultMap getResultMap(RequestScope request, Object parameterObject) {
    return request.getResultMap();
  }

  public void cleanup(RequestScope request) {
  }

}
