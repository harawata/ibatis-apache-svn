package com.ibatis.sqlmap.engine.mapping.sql.stat;

import com.ibatis.sqlmap.engine.mapping.parameter.*;
import com.ibatis.sqlmap.engine.mapping.result.*;
import com.ibatis.sqlmap.engine.mapping.sql.*;

import com.ibatis.sqlmap.engine.scope.*;

/**
 * User: Clinton Begin
 * Date: Sep 13, 2003
 * Time: 6:49:56 PM
 */
public class StaticSql implements Sql {

  private String sqlStatement;

  public StaticSql(String sqlStatement) {
    this.sqlStatement = sqlStatement;
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
