package com.ibatis.sqlmap.engine.scope;

import com.ibatis.sqlmap.engine.mapping.parameter.*;
import com.ibatis.sqlmap.engine.mapping.result.*;
import com.ibatis.sqlmap.engine.mapping.sql.*;
import com.ibatis.sqlmap.engine.mapping.statement.*;

/**
 * User: Clinton Begin
 * Date: Dec 28, 2003
 * Time: 2:39:12 PM
 */
public class RequestScope extends BaseScope {



  // Used by Any
  private SessionScope session;
  private ErrorContext errorContext;
  private MappedStatement statement;
  private ParameterMap parameterMap;
  private ResultMap resultMap;
  private Sql sql;

  // Used by DynamicSql
  private ParameterMap dynamicParameterMap;
  private String dynamicSql;

  public RequestScope() {
    errorContext = new ErrorContext();
  }

  public ErrorContext getErrorContext() {
    return errorContext;
  }

  public SessionScope getSession() {
    return session;
  }

  public void setSession(SessionScope session) {
    this.session = session;
  }

  public MappedStatement getStatement() {
    return statement;
  }

  public void setStatement(MappedStatement statement) {
    this.statement = statement;
  }

  public ParameterMap getParameterMap() {
    return parameterMap;
  }

  public void setParameterMap(ParameterMap parameterMap) {
    this.parameterMap = parameterMap;
  }

  public ResultMap getResultMap() {
    return resultMap;
  }

  public void setResultMap(ResultMap resultMap) {
    this.resultMap = resultMap;
  }

  public Sql getSql() {
    return sql;
  }

  public void setSql(Sql sql) {
    this.sql = sql;
  }

  public ParameterMap getDynamicParameterMap() {
    return dynamicParameterMap;
  }

  public void setDynamicParameterMap(ParameterMap dynamicParameterMap) {
    this.dynamicParameterMap = dynamicParameterMap;
  }

  public String getDynamicSql() {
    return dynamicSql;
  }

  public void setDynamicSql(String dynamicSql) {
    this.dynamicSql = dynamicSql;
  }

  public void reset() {
    super.reset();
    errorContext.reset();
    session = null;
    statement = null;
    parameterMap = null;
    resultMap = null;
    sql = null;
    dynamicParameterMap = null;
    dynamicSql = null;
  }

}
