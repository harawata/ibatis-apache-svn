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
package com.ibatis.sqlmap.engine.mapping.statement;

import com.ibatis.sqlmap.client.event.RowHandler;
import com.ibatis.sqlmap.engine.cache.CacheKey;
import com.ibatis.sqlmap.engine.cache.CacheModel;
import com.ibatis.sqlmap.engine.mapping.parameter.ParameterMap;
import com.ibatis.sqlmap.engine.mapping.result.ResultMap;
import com.ibatis.sqlmap.engine.mapping.sql.Sql;
import com.ibatis.sqlmap.engine.scope.RequestScope;
import com.ibatis.sqlmap.engine.transaction.Transaction;

import java.sql.SQLException;
import java.util.List;

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

  public Integer getResultSetType() {
    return statement.getResultSetType();
  }

  public Integer getFetchSize() {
    return statement.getFetchSize();
  }

  public ParameterMap getParameterMap() {
    return statement.getParameterMap();
  }

  public ResultMap getResultMap() {
    return statement.getResultMap();
  }

  public int executeUpdate(RequestScope request, Transaction trans, Object parameterObject)
      throws SQLException {
    int n = statement.executeUpdate(request, trans, parameterObject);
    return n;
  }

  public Object executeQueryForObject(RequestScope request, Transaction trans, Object parameterObject, Object resultObject)
      throws SQLException {
    CacheKey cacheKey = getCacheKey(request, parameterObject);
    cacheKey.update("executeQueryForObject");
    Object object = cacheModel.getObject(cacheKey);
    if (object == null) {
      synchronized (cacheModel.getLock(cacheKey)) {
        object = statement.executeQueryForObject(request, trans, parameterObject, resultObject);
        cacheModel.putObject(cacheKey, object);
      }
    }
    return object;
  }

  public List executeQueryForList(RequestScope request, Transaction trans, Object parameterObject, int skipResults, int maxResults)
      throws SQLException {
    CacheKey cacheKey = getCacheKey(request, parameterObject);
    cacheKey.update("executeQueryForList");
    cacheKey.update(skipResults);
    cacheKey.update(maxResults);
    List list = (List) cacheModel.getObject(cacheKey);
    if (list == null) {
      synchronized (cacheModel.getLock(cacheKey)) {
        list = statement.executeQueryForList(request, trans, parameterObject, skipResults, maxResults);
        cacheModel.putObject(cacheKey, list);
      }
    }
    return list;
  }

  public void executeQueryWithRowHandler(RequestScope request, Transaction trans, Object parameterObject, RowHandler rowHandler)
      throws SQLException {
    statement.executeQueryWithRowHandler(request, trans, parameterObject, rowHandler);
  }

  public CacheKey getCacheKey(RequestScope request, Object parameterObject) {
    CacheKey key = statement.getCacheKey(request, parameterObject);
    if (!cacheModel.isReadOnly() && !cacheModel.isSerialize()) {
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

}
