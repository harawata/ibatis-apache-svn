package com.ibatis.sqlmap.engine.mapping.statement;

import com.ibatis.sqlmap.engine.cache.*;
import com.ibatis.sqlmap.engine.mapping.parameter.*;
import com.ibatis.sqlmap.engine.mapping.result.*;
import com.ibatis.sqlmap.engine.mapping.sql.Sql;

import com.ibatis.sqlmap.engine.scope.*;
import com.ibatis.sqlmap.client.event.*;

import java.sql.*;
import java.util.*;

/**
 * User: Clinton Begin
 * Date: Dec 7, 2003
 * Time: 11:38:59 AM
 */
public class CachingStatement implements MappedStatement {

  private MappedStatement statement;
  private CacheModel cacheModel;

  public CachingStatement(MappedStatement statement, CacheModel cacheModel) {
    this.statement = statement;
    this.cacheModel = cacheModel;
  }

  public String getId() {
    return statement.getId();
  }

  public ParameterMap getParameterMap() {
    return statement.getParameterMap();
  }

  public ResultMap getResultMap() {
    return statement.getResultMap();
  }

  public int executeUpdate(RequestScope request, Connection conn, Object parameterObject)
      throws SQLException {
    int n = statement.executeUpdate(request, conn, parameterObject);
    return n;
  }

  public Object executeQueryForObject(RequestScope request, Connection conn, Object parameterObject, Object resultObject)
      throws SQLException {
    CacheKey cacheKey = getCacheKey(request, parameterObject);
    cacheKey.update("executeQueryForObject");
    Object object = cacheModel.getObject(cacheKey);
    if (object == null) {
      synchronized (cacheModel) {
        object = statement.executeQueryForObject(request, conn, parameterObject, resultObject);
        cacheModel.putObject(cacheKey, object);
      }
    }
    return object;
  }

  public List executeQueryForList(RequestScope request, Connection conn, Object parameterObject, int skipResults, int maxResults)
      throws SQLException {
    CacheKey cacheKey = getCacheKey(request, parameterObject);
    cacheKey.update("executeQueryForList");
    List list = (List) cacheModel.getObject(cacheKey);
    if (list == null) {
      synchronized (cacheModel) {
        list = statement.executeQueryForList(request, conn, parameterObject, skipResults, maxResults);
        cacheModel.putObject(cacheKey, list);
      }
    }
    return list;
  }

  public List executeQueryForList(RequestScope request, Connection conn, Object parameterObject, RowHandler rowHandler)
      throws SQLException {
    List list = statement.executeQueryForList(request, conn, parameterObject, rowHandler);
    return list;
  }

  public CacheKey getCacheKey(RequestScope request, Object parameterObject) {
    CacheKey key = statement.getCacheKey(request, parameterObject);
    if (!cacheModel.isReadOnly()) {
      key.update(request.getSession());
    }
    return key;
  }

  public void setBaseCacheKey(int base) {
    statement.setBaseCacheKey(base);
  }

  public void addExecuteListener(ExecuteListener listener) {
    statement.addExecuteListener(listener);
  }

  public void notifyListeners() {
    statement.notifyListeners();
  }

  public void initRequest(RequestScope request) {
    statement.initRequest(request);
  }

  public Sql getSql() {
    return statement.getSql();
  }

  public Class getParameterClass() {
    return statement.getParameterClass();
  }

  public void flushDataCache() {
    cacheModel.flush();
  }

}
