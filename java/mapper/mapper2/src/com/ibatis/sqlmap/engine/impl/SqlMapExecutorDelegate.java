package com.ibatis.sqlmap.engine.impl;

import com.ibatis.sqlmap.client.*;
import com.ibatis.sqlmap.client.event.*;
import com.ibatis.sqlmap.engine.transaction.*;
import com.ibatis.sqlmap.engine.mapping.statement.*;
import com.ibatis.sqlmap.engine.execution.*;

import com.ibatis.sqlmap.engine.scope.*;
import com.ibatis.sqlmap.engine.cache.*;
import com.ibatis.common.jdbc.logging.*;
import com.ibatis.common.jdbc.exception.*;
import com.ibatis.common.beans.*;
import com.ibatis.common.util.*;

import javax.sql.*;
import java.sql.*;
import java.util.*;

import org.apache.commons.logging.*;

/**
 * User: Clinton Begin
 * Date: Sep 13, 2003
 * Time: 7:04:24 AM
 */
public class SqlMapExecutorDelegate {

  public static final int DEFAULT_MAX_REQUESTS = 512;
  public static final int DEFAULT_MAX_SESSIONS = 128;
  public static final int DEFAULT_MAX_TRANSACTIONS = 32;

  private static final Log log = LogFactory.getLog(SqlMapExecutorDelegate.class);

  private boolean lazyLoadingEnabled;
  private boolean cacheModelsEnabled;
  private boolean enhancementEnabled;

  private int maxRequests = DEFAULT_MAX_REQUESTS;
  private int maxSessions = DEFAULT_MAX_SESSIONS;
  private int maxTransactions = DEFAULT_MAX_TRANSACTIONS;

  private TransactionManager txManager;
  private HashMap mappedStatements = new HashMap();

  private ThrottledPool requestPool = new ThrottledPool(RequestScope.class, DEFAULT_MAX_REQUESTS);
  private ThrottledPool sessionPool = new ThrottledPool(SessionScope.class, DEFAULT_MAX_SESSIONS);

  private SqlExecutor sqlExecutor = new SqlExecutor();

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

  public void flushDataCache () {
    Iterator statements = mappedStatements.values().iterator();
    while (statements.hasNext()) {
      MappedStatement statement = (MappedStatement) statements.next();
      if (statement instanceof CachingStatement) {
        ((CachingStatement)statement).flushDataCache();
      }
    }
  }

  //-- Basic Methods
  public Object insert(SessionScope session, String id, Object param) throws SQLException {
    Object generatedKey = null;

    MappedStatement ms = getMappedStatement(id);
    Connection conn = getConnection(session);
    boolean autoStart = conn == null;

    try {
      conn = autoStartTransaction(session, autoStart, conn);

      SelectKeyStatement selectKeyStatement = null;
      if (ms instanceof InsertStatement) {
        selectKeyStatement = ((InsertStatement) ms).getSelectKeyStatement();
      }

      if (selectKeyStatement != null && !selectKeyStatement.isAfter()) {
        generatedKey = executeSelectKey(session, conn, ms, param);
      }

      RequestScope request = popRequest(session, ms);
      try {
        ms.executeUpdate(request, conn, param);
      } finally {
        pushRequest(request);
      }

      if (selectKeyStatement != null && selectKeyStatement.isAfter()) {
        generatedKey = executeSelectKey(session, conn, ms, param);
      }

      autoCommitTransaction(session, autoStart);
    } finally {
      autoStopTransaction(session, autoStart);
    }

    return generatedKey;
  }

  private Object executeSelectKey(SessionScope session, Connection conn, MappedStatement ms, Object param) throws SQLException {
    Object generatedKey = null;
    RequestScope request;
    InsertStatement insert = (InsertStatement) ms;
    SelectKeyStatement selectKeyStatement = insert.getSelectKeyStatement();
    if (selectKeyStatement != null) {
      request = popRequest(session, selectKeyStatement);
      try {
        generatedKey = selectKeyStatement.executeQueryForObject(request, conn, param, null);
        String keyProp = selectKeyStatement.getKeyProperty();
        if (keyProp != null) {
          BeanProbe.setObject(param, keyProp, generatedKey);
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
    Connection conn = getConnection(session);
    boolean autoStart = conn == null;

    try {
      conn = autoStartTransaction(session, autoStart, conn);

      RequestScope request = popRequest(session, ms);
      try {
        rows = ms.executeUpdate(request, conn, param);
      } finally {
        pushRequest(request);
      }

      autoCommitTransaction(session, autoStart);
    } finally {
      autoStopTransaction(session, autoStart);
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
    Connection conn = getConnection(session);
    boolean autoStart = conn == null;

    try {
      conn = autoStartTransaction(session, autoStart, conn);

      RequestScope request = popRequest(session, ms);
      try {
        object = ms.executeQueryForObject(request, conn, paramObject, resultObject);
      } finally {
        pushRequest(request);
      }

      autoCommitTransaction(session, autoStart);
    } finally {
      autoStopTransaction(session, autoStart);
    }

    return object;
  }

  public List queryForList(SessionScope session, String id, Object paramObject) throws SQLException {
    return queryForList(session, id, paramObject, SqlExecutor.NO_SKIPPED_RESULTS, SqlExecutor.NO_MAXIMUM_RESULTS);
  }

  public List queryForList(SessionScope session, String id, Object paramObject, int skip, int max) throws SQLException {
    List list = null;

    MappedStatement ms = getMappedStatement(id);
    Connection conn = getConnection(session);
    boolean autoStart = conn == null;

    try {
      conn = autoStartTransaction(session, autoStart, conn);

      RequestScope request = popRequest(session, ms);
      try {
        list = ms.executeQueryForList(request, conn, paramObject, skip, max);
      } finally {
        pushRequest(request);
      }

      autoCommitTransaction(session, autoStart);
    } finally {
      autoStopTransaction(session, autoStart);
    }

    return list;
  }

  public List queryForList(SessionScope session, String id, Object paramObject, RowHandler rowHandler) throws SQLException {
    List list = null;

    MappedStatement ms = getMappedStatement(id);
    Connection conn = getConnection(session);
    boolean autoStart = conn == null;

    try {
      conn = autoStartTransaction(session, autoStart, conn);

      RequestScope request = popRequest(session, ms);
      try {
        list = ms.executeQueryForList(request, conn, paramObject, rowHandler);
      } finally {
        pushRequest(request);
      }

      autoCommitTransaction(session, autoStart);
    } finally {
      autoStopTransaction(session, autoStart);
    }

    return list;
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
      Object key = BeanProbe.getObject(object, keyProp);
      Object value = null;
      if (valueProp == null) {
        value = object;
      } else {
        value = BeanProbe.getObject(object, valueProp);
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

  public Connection getUserConnection(SessionScope session) {
    return session.getUserConnection();
  }

  public void setUserConnection(SessionScope session, Connection userConnection) {
    session.setUserConnection(userConnection);
  }

  public DataSource getDataSource() {
    return txManager.getDataSource();
  }

  public MappedStatement getMappedStatement(String id) {
    MappedStatement ms = (MappedStatement) mappedStatements.get(id);
    if (ms == null) {
      throw new SqlMapException("There is no statement named " + id + " in this SqlMap.");
    }
    return ms;
  }

  public SqlExecutor getSqlExecutor() {
    return sqlExecutor;
  }

  // -- Private Methods

  private Connection getConnection(SessionScope session) throws SQLException {
    Connection connection = session.getUserConnection();
    if (connection == null) {
      try {
        Transaction trans = session.getTransaction();
        if (trans != null) {
          connection = trans.getConnection();
        }
      } catch (TransactionException e) {
        throw new NestedSQLException("Could not get connection.  Cause: " + e, e);
      }
    }

    return connection;
  }

  private void autoStopTransaction(SessionScope session, boolean autoStart) throws SQLException {
    if (autoStart) {
      session.getSqlMapTxMgr().endTransaction();
    }
  }

  private void autoCommitTransaction(SessionScope session, boolean autoStart) throws SQLException {
    if (autoStart) {
      session.getSqlMapTxMgr().commitTransaction();
    }
  }

  private Connection autoStartTransaction(SessionScope session, boolean autoStart, Connection conn) throws SQLException {
    Connection connection = conn;
    if (autoStart) {
      session.getSqlMapTxMgr().startTransaction();
      connection = getConnection(session);
    }
    if (connection != null && log.isDebugEnabled()) {
      connection = ConnectionLogProxy.newInstance(connection);
    }
    return connection;
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

