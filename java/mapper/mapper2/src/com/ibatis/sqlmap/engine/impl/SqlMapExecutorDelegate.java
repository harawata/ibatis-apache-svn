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
package com.ibatis.sqlmap.engine.impl;

import com.ibatis.common.beans.Probe;
import com.ibatis.common.beans.ProbeFactory;
import com.ibatis.common.jdbc.exception.NestedSQLException;
import com.ibatis.common.util.PaginatedList;
import com.ibatis.common.util.ThrottledPool;
import com.ibatis.sqlmap.client.SqlMapException;
import com.ibatis.sqlmap.client.event.RowHandler;
import com.ibatis.sqlmap.engine.cache.CacheKey;
import com.ibatis.sqlmap.engine.cache.CacheModel;
import com.ibatis.sqlmap.engine.exchange.DataExchangeFactory;
import com.ibatis.sqlmap.engine.execution.SqlExecutor;
import com.ibatis.sqlmap.engine.mapping.parameter.ParameterMap;
import com.ibatis.sqlmap.engine.mapping.result.ResultMap;
import com.ibatis.sqlmap.engine.mapping.statement.InsertStatement;
import com.ibatis.sqlmap.engine.mapping.statement.MappedStatement;
import com.ibatis.sqlmap.engine.mapping.statement.PaginatedDataList;
import com.ibatis.sqlmap.engine.mapping.statement.SelectKeyStatement;
import com.ibatis.sqlmap.engine.scope.RequestScope;
import com.ibatis.sqlmap.engine.scope.SessionScope;
import com.ibatis.sqlmap.engine.transaction.Transaction;
import com.ibatis.sqlmap.engine.transaction.TransactionException;
import com.ibatis.sqlmap.engine.transaction.TransactionManager;
import com.ibatis.sqlmap.engine.transaction.TransactionState;
import com.ibatis.sqlmap.engine.transaction.user.UserProvidedTransaction;
import com.ibatis.sqlmap.engine.type.TypeHandlerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * User: Clinton Begin
 * Date: Sep 13, 2003
 * Time: 7:04:24 AM
 */
public class SqlMapExecutorDelegate {

  private static final Probe PROBE = ProbeFactory.getProbe();

  public static final int DEFAULT_MAX_REQUESTS = 512;
  public static final int DEFAULT_MAX_SESSIONS = 128;
  public static final int DEFAULT_MAX_TRANSACTIONS = 32;

  private boolean lazyLoadingEnabled;
  private boolean cacheModelsEnabled;
  private boolean enhancementEnabled;

  private int maxRequests = DEFAULT_MAX_REQUESTS;
  private int maxSessions = DEFAULT_MAX_SESSIONS;
  private int maxTransactions = DEFAULT_MAX_TRANSACTIONS;

  private TransactionManager txManager;

  private HashMap mappedStatements;
  private HashMap cacheModels;
  private HashMap resultMaps;
  private HashMap parameterMaps;

  private ThrottledPool requestPool;
  private ThrottledPool sessionPool;

  private SqlExecutor sqlExecutor;
  private TypeHandlerFactory typeHandlerFactory;
  private DataExchangeFactory dataExchangeFactory;

  public SqlMapExecutorDelegate() {
    mappedStatements = new HashMap();
    cacheModels = new HashMap();
    resultMaps = new HashMap();
    parameterMaps = new HashMap();

    requestPool = new ThrottledPool(RequestScope.class, DEFAULT_MAX_REQUESTS);
    sessionPool = new ThrottledPool(SessionScope.class, DEFAULT_MAX_SESSIONS);

    sqlExecutor = new SqlExecutor();
    typeHandlerFactory = new TypeHandlerFactory();
    dataExchangeFactory = new DataExchangeFactory(typeHandlerFactory);
  }

  public DataExchangeFactory getDataExchangeFactory() {
    return dataExchangeFactory;
  }

  public TypeHandlerFactory getTypeHandlerFactory() {
    return typeHandlerFactory;
  }

  public boolean isLazyLoadingEnabled() {
    return lazyLoadingEnabled;
  }

  public void setLazyLoadingEnabled(boolean lazyLoadingEnabled) {
    this.lazyLoadingEnabled = lazyLoadingEnabled;
  }

  public boolean isCacheModelsEnabled() {
    return cacheModelsEnabled;
  }

  public void setCacheModelsEnabled(boolean cacheModelsEnabled) {
    this.cacheModelsEnabled = cacheModelsEnabled;
  }

  public boolean isEnhancementEnabled() {
    return enhancementEnabled;
  }

  public void setEnhancementEnabled(boolean enhancementEnabled) {
    this.enhancementEnabled = enhancementEnabled;
  }

  public int getMaxRequests() {
    return maxRequests;
  }

  public void setMaxRequests(int maxRequests) {
    this.maxRequests = maxRequests;
    requestPool = new ThrottledPool(RequestScope.class, maxRequests);
  }

  public int getMaxSessions() {
    return maxSessions;
  }

  public void setMaxSessions(int maxSessions) {
    this.maxSessions = maxSessions;
    this.sessionPool = new ThrottledPool(SessionScope.class, maxSessions);
  }

  public int getMaxTransactions() {
    return maxTransactions;
  }

  public void setMaxTransactions(int maxTransactions) {
    this.maxTransactions = maxTransactions;
  }

  public TransactionManager getTxManager() {
    return txManager;
  }

  public void setTxManager(TransactionManager txManager) {
    this.txManager = txManager;
  }

  public void addMappedStatement(MappedStatement ms) {
    ms.setBaseCacheKey(hashCode());
    mappedStatements.put(ms.getId(), ms);
  }

  public Iterator getMappedStatementNames() {
    return mappedStatements.keySet().iterator();
  }

  public MappedStatement getMappedStatement(String id) {
    MappedStatement ms = (MappedStatement) mappedStatements.get(id);
    if (ms == null) {
      throw new SqlMapException("There is no statement named " + id + " in this SqlMap.");
    }
    return ms;
  }

  public void addCacheModel(CacheModel model) {
    cacheModels.put(model.getId(), model);
  }

  public Iterator getCacheModelNames() {
    return cacheModels.keySet().iterator();
  }

  public CacheModel getCacheModel(String id) {
    CacheModel model = (CacheModel) cacheModels.get(id);
    if (model == null) {
      throw new SqlMapException("There is no cache model named " + id + " in this SqlMap.");
    }
    return model;
  }

  public void addResultMap(ResultMap map) {
    resultMaps.put(map.getId(), map);
  }

  public Iterator getResultMapNames() {
    return resultMaps.keySet().iterator();
  }

  public ResultMap getResultMap(String id) {
    ResultMap map = (ResultMap) resultMaps.get(id);
    if (map == null) {
      throw new SqlMapException("There is no result map named " + id + " in this SqlMap.");
    }
    return map;
  }

  public void addParameterMap(ParameterMap map) {
    parameterMaps.put(map.getId(), map);
  }

  public Iterator getParameterMapNames() {
    return parameterMaps.keySet().iterator();
  }

  public ParameterMap getParameterMap(String id) {
    ParameterMap map = (ParameterMap) parameterMaps.get(id);
    if (map == null) {
      throw new SqlMapException("There is no parameter map named " + id + " in this SqlMap.");
    }
    return map;
  }

  public void flushDataCache() {
    Iterator models = cacheModels.values().iterator();
    while (models.hasNext()) {
      ((CacheModel) models.next()).flush();
    }
  }

  public void flushDataCache(String id) {
    CacheModel model = getCacheModel(id);
    if (model != null) {
      model.flush();
    }
  }

  //-- Basic Methods
  public Object insert(SessionScope session, String id, Object param) throws SQLException {
    Object generatedKey = null;

    MappedStatement ms = getMappedStatement(id);
    Transaction trans = getTransaction(session);
    boolean autoStart = trans == null;

    try {
      trans = autoStartTransaction(session, autoStart, trans);

      SelectKeyStatement selectKeyStatement = null;
      if (ms instanceof InsertStatement) {
        selectKeyStatement = ((InsertStatement) ms).getSelectKeyStatement();
      }

      if (selectKeyStatement != null && !selectKeyStatement.isAfter()) {
        generatedKey = executeSelectKey(session, trans, ms, param);
      }

      RequestScope request = popRequest(session, ms);
      try {
        ms.executeUpdate(request, trans, param);
      } finally {
        pushRequest(request);
      }

      if (selectKeyStatement != null && selectKeyStatement.isAfter()) {
        generatedKey = executeSelectKey(session, trans, ms, param);
      }

      autoCommitTransaction(session, autoStart);
    } finally {
      autoEndTransaction(session, autoStart);
    }

    return generatedKey;
  }

  private Object executeSelectKey(SessionScope session, Transaction trans, MappedStatement ms, Object param) throws SQLException {
    Object generatedKey = null;
    RequestScope request;
    InsertStatement insert = (InsertStatement) ms;
    SelectKeyStatement selectKeyStatement = insert.getSelectKeyStatement();
    if (selectKeyStatement != null) {
      request = popRequest(session, selectKeyStatement);
      try {
        generatedKey = selectKeyStatement.executeQueryForObject(request, trans, param, null);
        String keyProp = selectKeyStatement.getKeyProperty();
        if (keyProp != null) {
          PROBE.setObject(param, keyProp, generatedKey);
        }
      } finally {
        pushRequest(request);
      }
    }
    return generatedKey;
  }

  public int update(SessionScope session, String id, Object param) throws SQLException {
    int rows = 0;

    MappedStatement ms = getMappedStatement(id);
    Transaction trans = getTransaction(session);
    boolean autoStart = trans == null;

    try {
      trans = autoStartTransaction(session, autoStart, trans);

      RequestScope request = popRequest(session, ms);
      try {
        rows = ms.executeUpdate(request, trans, param);
      } finally {
        pushRequest(request);
      }

      autoCommitTransaction(session, autoStart);
    } finally {
      autoEndTransaction(session, autoStart);
    }

    return rows;
  }

  public int delete(SessionScope session, String id, Object param) throws SQLException {
    return update(session, id, param);
  }

  public Object queryForObject(SessionScope session, String id, Object paramObject) throws SQLException {
    return queryForObject(session, id, paramObject, null);
  }

  public Object queryForObject(SessionScope session, String id, Object paramObject, Object resultObject) throws SQLException {
    Object object = null;

    MappedStatement ms = getMappedStatement(id);
    Transaction trans = getTransaction(session);
    boolean autoStart = trans == null;

    try {
      trans = autoStartTransaction(session, autoStart, trans);

      RequestScope request = popRequest(session, ms);
      try {
        object = ms.executeQueryForObject(request, trans, paramObject, resultObject);
      } finally {
        pushRequest(request);
      }

      autoCommitTransaction(session, autoStart);
    } finally {
      autoEndTransaction(session, autoStart);
    }

    return object;
  }

  public List queryForList(SessionScope session, String id, Object paramObject) throws SQLException {
    return queryForList(session, id, paramObject, SqlExecutor.NO_SKIPPED_RESULTS, SqlExecutor.NO_MAXIMUM_RESULTS);
  }

  public List queryForList(SessionScope session, String id, Object paramObject, int skip, int max) throws SQLException {
    List list = null;

    MappedStatement ms = getMappedStatement(id);
    Transaction trans = getTransaction(session);
    boolean autoStart = trans == null;

    try {
      trans = autoStartTransaction(session, autoStart, trans);

      RequestScope request = popRequest(session, ms);
      try {
        list = ms.executeQueryForList(request, trans, paramObject, skip, max);
      } finally {
        pushRequest(request);
      }

      autoCommitTransaction(session, autoStart);
    } finally {
      autoEndTransaction(session, autoStart);
    }

    return list;
  }

  public void queryWithRowHandler(SessionScope session, String id, Object paramObject, RowHandler rowHandler) throws SQLException {

    MappedStatement ms = getMappedStatement(id);
    Transaction trans = getTransaction(session);
    boolean autoStart = trans == null;

    try {
      trans = autoStartTransaction(session, autoStart, trans);

      RequestScope request = popRequest(session, ms);
      try {
        ms.executeQueryWithRowHandler(request, trans, paramObject, rowHandler);
      } finally {
        pushRequest(request);
      }

      autoCommitTransaction(session, autoStart);
    } finally {
      autoEndTransaction(session, autoStart);
    }

  }

  public PaginatedList queryForPaginatedList(SessionScope session, String id, Object paramObject, int pageSize) throws SQLException {
    return new PaginatedDataList(session.getSqlMapExecutor(), id, paramObject, pageSize);
  }

  public Map queryForMap(SessionScope session, String id, Object paramObject, String keyProp) throws SQLException {
    return queryForMap(session, id, paramObject, keyProp, null);
  }

  public Map queryForMap(SessionScope session, String id, Object paramObject, String keyProp, String valueProp) throws SQLException {
    Map map = new HashMap();

    List list = queryForList(session, id, paramObject);

    for (int i = 0, n = list.size(); i < n; i++) {
      Object object = list.get(i);
      Object key = PROBE.getObject(object, keyProp);
      Object value = null;
      if (valueProp == null) {
        value = object;
      } else {
        value = PROBE.getObject(object, valueProp);
      }
      map.put(key, value);
    }

    return map;
  }

  // -- Transaction Control Methods
  public void startTransaction(SessionScope session) throws SQLException {
    try {
      txManager.begin(session);
    } catch (TransactionException e) {
      throw new NestedSQLException("Could not start transaction.  Cause: " + e, e);
    }
  }

  public void commitTransaction(SessionScope session) throws SQLException {
    try {
      // Auto batch execution
      if (session.isInBatch()) {
        executeBatch(session);
      }
      sqlExecutor.cleanup(session);
      txManager.commit(session);
    } catch (TransactionException e) {
      throw new NestedSQLException("Could not commit transaction.  Cause: " + e, e);
    }
  }

  public void endTransaction(SessionScope session) throws SQLException {
    try {
      try {
        sqlExecutor.cleanup(session);
      } finally {
        txManager.end(session);
      }
    } catch (TransactionException e) {
      throw new NestedSQLException("Error while ending transaction.  Cause: " + e, e);
    }
  }

  public void startBatch(SessionScope session) throws SQLException {
    session.setInBatch(true);
  }

  public int executeBatch(SessionScope session) throws SQLException {
    session.setInBatch(false);
    return sqlExecutor.executeBatch(session);
  }

  public void setUserProvidedTransaction(SessionScope session, Connection userConnection) {
    if (session.getTransactionState() == TransactionState.STATE_USER_PROVIDED) {
      session.recallTransactionState();
    }
    if (userConnection != null) {
      Connection conn = userConnection;
      session.saveTransactionState();
      session.setTransaction(new UserProvidedTransaction(conn));
      session.setTransactionState(TransactionState.STATE_USER_PROVIDED);
    } else {
      session.setTransaction(null);
    }
  }

  public DataSource getDataSource() {
    DataSource ds = null;
    if (txManager != null) {
      ds = txManager.getDataSource();
    }
    return ds;
  }

  public SqlExecutor getSqlExecutor() {
    return sqlExecutor;
  }

  public Transaction getTransaction(SessionScope session) {
    return session.getTransaction();
  }

  // -- Private Methods

  private void autoEndTransaction(SessionScope session, boolean autoStart) throws SQLException {
    if (autoStart) {
      session.getSqlMapTxMgr().endTransaction();
    }
  }

  private void autoCommitTransaction(SessionScope session, boolean autoStart) throws SQLException {
    if (autoStart) {
      session.getSqlMapTxMgr().commitTransaction();
    }
  }

  private Transaction autoStartTransaction(SessionScope session, boolean autoStart, Transaction trans) throws SQLException {
    Transaction transaction = trans;
    if (autoStart) {
      session.getSqlMapTxMgr().startTransaction();
      transaction = getTransaction(session);
    }
    return transaction;
  }

  public boolean equals(Object obj) {
    return this == obj;
  }

  public int hashCode() {
    CacheKey key = new CacheKey();
    if (txManager != null) {
      key.update(txManager);
      if (txManager.getDataSource() != null) {
        key.update(txManager.getDataSource());
      }
    }
    key.update(System.identityHashCode(this));
    return key.hashCode();
  }

  protected RequestScope popRequest(SessionScope session, MappedStatement mappedStatement) {
    RequestScope request = (RequestScope) requestPool.pop();
    request.setSession(session);
    mappedStatement.initRequest(request);
    return request;
  }

  protected void pushRequest(RequestScope request) {
    request.reset();
    requestPool.push(request);
  }

  protected SessionScope popSession() {
    return (SessionScope) sessionPool.pop();
  }

  protected void pushSession(SessionScope session) {
    session.reset();
    sessionPool.push(session);
  }

}

