/*
 *  Copyright 2004 Clinton Begin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.ibatis.sqlmap.engine.scope;

import com.ibatis.sqlmap.engine.mapping.parameter.ParameterMap;
import com.ibatis.sqlmap.engine.mapping.result.ResultMap;
import com.ibatis.sqlmap.engine.mapping.sql.Sql;
import com.ibatis.sqlmap.engine.mapping.statement.MappedStatement;

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
