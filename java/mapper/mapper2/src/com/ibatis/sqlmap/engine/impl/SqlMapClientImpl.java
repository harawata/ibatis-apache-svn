package com.ibatis.sqlmap.engine.impl;

import com.ibatis.sqlmap.client.*;
import com.ibatis.sqlmap.client.event.*;

import com.ibatis.sqlmap.engine.mapping.statement.*;
import com.ibatis.sqlmap.engine.execution.*;
import com.ibatis.common.util.*;

import javax.sql.*;
import java.sql.*;
import java.util.*;

/**
 * User: Clinton Begin
 * Date: Dec 28, 2003
 * Time: 4:10:15 PM
 */
public class SqlMapClientImpl implements ExtendedSqlMapClient {

  public SqlMapExecutorDelegate delegate;
  private ThreadLocal localSqlMapSession = new ThreadLocal();

  public SqlMapClientImpl(SqlMapExecutorDelegate delegate) {
    this.delegate = delegate;
  }

  public Object insert(String id, Object param) throws SQLException {
    return getLocalSqlMapSession().insert(id, param);
  }

  public int update(String id, Object param) throws SQLException {
    return getLocalSqlMapSession().update(id, param);
  }

  public int delete(String id, Object param) throws SQLException {
    return getLocalSqlMapSession().delete(id, param);
  }

  public Object queryForObject(String id, Object paramObject) throws SQLException {
    return getLocalSqlMapSession().queryForObject(id, paramObject);
  }

  public Object queryForObject(String id, Object paramObject, Object resultObject) throws SQLException {
    return getLocalSqlMapSession().queryForObject(id, paramObject, resultObject);
  }

  public List queryForList(String id, Object paramObject) throws SQLException {
    return getLocalSqlMapSession().queryForList(id, paramObject);
  }

  public List queryForList(String id, Object paramObject, int skip, int max) throws SQLException {
    return getLocalSqlMapSession().queryForList(id, paramObject, skip, max);
  }

  public PaginatedList queryForPaginatedList(String id, Object paramObject, int pageSize) throws SQLException {
    return getLocalSqlMapSession().queryForPaginatedList(id, paramObject, pageSize);
  }

  public Map queryForMap(String id, Object paramObject, String keyProp) throws SQLException {
    return getLocalSqlMapSession().queryForMap(id, paramObject, keyProp);
  }

  public Map queryForMap(String id, Object paramObject, String keyProp, String valueProp) throws SQLException {
    return getLocalSqlMapSession().queryForMap(id, paramObject, keyProp, valueProp);
  }

  public List queryForList(String id, Object paramObject, RowHandler rowHandler) throws SQLException {
    return getLocalSqlMapSession().queryForList(id, paramObject, rowHandler);
  }

  public void startTransaction() throws SQLException {
    getLocalSqlMapSession().startTransaction();
  }

  public void commitTransaction() throws SQLException {
    getLocalSqlMapSession().commitTransaction();
  }

  public void endTransaction() throws SQLException {
    try {
      getLocalSqlMapSession().endTransaction();
    } finally {
      getLocalSqlMapSession().close();
    }
  }

  public void startBatch() throws SQLException {
    getLocalSqlMapSession().startBatch();
  }

  public int executeBatch() throws SQLException {
    return getLocalSqlMapSession().executeBatch();
  }

  public void setUserConnection(Connection connection) throws SQLException {
    getLocalSqlMapSession().setUserConnection(connection);
  }

  public Connection getUserConnection() throws SQLException {
    return getLocalSqlMapSession().getUserConnection();
  }

  public DataSource getDataSource() {
    return getLocalSqlMapSession().getDataSource();
  }

  public MappedStatement getMappedStatement(String id) {
    return delegate.getMappedStatement(id);
  }

  public boolean isLazyLoadingEnabled() {
    return delegate.isLazyLoadingEnabled();
  }

  public boolean isEnhancementEnabled() {
    return delegate.isEnhancementEnabled();
  }

  public SqlExecutor getSqlExecutor() {
    return delegate.getSqlExecutor();
  }

  public SqlMapExecutorDelegate getDelegate() {
    return delegate;
  }

  public SqlMapSession openSession() {
    SqlMapSessionImpl sqlMapSession = getLocalSqlMapSession();
    sqlMapSession.open();
    return sqlMapSession;
  }

  /**
   * @deprecated
   * @return
   */
  public SqlMapSession getSession() {
    return openSession();
  }

  public void flushDataCache() {
    delegate.flushDataCache();
  }

  private SqlMapSessionImpl getLocalSqlMapSession() {
    SqlMapSessionImpl sqlMapSession = (SqlMapSessionImpl) localSqlMapSession.get();
    if (sqlMapSession == null || sqlMapSession.isClosed()) {
      sqlMapSession = new SqlMapSessionImpl(this);
      localSqlMapSession.set(sqlMapSession);
    }
    return sqlMapSession;
  }

}
