package com.ibatis.sqlmap.engine.mapping.statement;

import com.ibatis.sqlmap.engine.mapping.sql.*;
import com.ibatis.sqlmap.engine.mapping.result.*;
import com.ibatis.sqlmap.engine.mapping.parameter.*;
import com.ibatis.sqlmap.engine.execution.*;
import com.ibatis.sqlmap.engine.impl.*;

import com.ibatis.sqlmap.engine.scope.*;
import com.ibatis.sqlmap.engine.cache.*;
import com.ibatis.sqlmap.client.*;

import java.util.*;

/**
 * User: Clinton Begin
 * Date: Nov 9, 2003
 * Time: 10:02:41 PM
 */
public abstract class BaseStatement implements MappedStatement {

  private String id;
  private Integer resultSetType;
  private Integer fetchSize;
  private ResultMap resultMap;
  private ParameterMap parameterMap;
  private Class parameterClass;
  private Sql sql;
  private int baseCacheKey;
  private ExtendedSqlMapClient sqlMapClient;

  private List executeListeners = new ArrayList();

  private String resource;

  public String getId() {
    return id;
  }

  public Integer getResultSetType() {
    return resultSetType;
  }

  public void setResultSetType(Integer resultSetType) {
    this.resultSetType = resultSetType;
  }

  public Integer getFetchSize() {
    return fetchSize;
  }

  public void setFetchSize(Integer fetchSize) {
    this.fetchSize = fetchSize;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Sql getSql() {
    return sql;
  }

  public void setSql(Sql sql) {
    this.sql = sql;
  }

  public ResultMap getResultMap() {
    return resultMap;
  }

  public void setResultMap(ResultMap resultMap) {
    this.resultMap = resultMap;
  }

  public ParameterMap getParameterMap() {
    return parameterMap;
  }

  public void setParameterMap(ParameterMap parameterMap) {
    this.parameterMap = parameterMap;
  }

  public Class getParameterClass() {
    return parameterClass;
  }

  public void setParameterClass(Class parameterClass) {
    this.parameterClass = parameterClass;
  }

  public String getResource() {
    return resource;
  }

  public void setResource(String resource) {
    this.resource = resource;
  }

  public CacheKey getCacheKey(RequestScope request, Object parameterObject) {
    Sql sql = request.getSql();
    ParameterMap pmap = sql.getParameterMap(request, parameterObject);
    CacheKey cacheKey = pmap.getCacheKey(request, parameterObject);
    cacheKey.update(id);
    cacheKey.update(baseCacheKey);
    cacheKey.update(sql.getSql(request, parameterObject)); //Fixes bug 953001
    return cacheKey;
  }

  public void setBaseCacheKey(int base) {
    this.baseCacheKey = base;
  }

  public void addExecuteListener(ExecuteListener listener) {
    executeListeners.add(listener);
  }

  public void notifyListeners() {
    for (int i = 0, n = executeListeners.size(); i < n; i++) {
      ((ExecuteListener) executeListeners.get(i)).onExecuteStatement(this);
    }
  }

  public SqlExecutor getSqlExecutor() {
    return sqlMapClient.getSqlExecutor();
  }

  public SqlMapClient getSqlMapClient() {
    return sqlMapClient;
  }

  public void setSqlMapClient(SqlMapClient sqlMapClient) {
    this.sqlMapClient = (ExtendedSqlMapClient) sqlMapClient;
  }

  public void initRequest(RequestScope request) {
    request.setStatement(this);
    request.setParameterMap(parameterMap);
    request.setResultMap(resultMap);
    request.setSql(sql);
  }

}
