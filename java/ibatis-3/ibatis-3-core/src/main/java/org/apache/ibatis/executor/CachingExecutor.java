package org.apache.ibatis.executor;

import org.apache.ibatis.cache.*;
import org.apache.ibatis.executor.result.ResultHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;

import java.sql.*;
import java.util.*;

public class CachingExecutor implements Executor {

  private Executor delegate;
  private TransactionalCacheManager tcm = new TransactionalCacheManager();

  public CachingExecutor(Executor delegate) {
    this.delegate = delegate;
  }

  public Connection getConnection() {
    return delegate.getConnection();
  }

  public void close() {
    delegate.close();
  }

  public int update(MappedStatement ms, Object parameterObject) throws SQLException {
    Cache cache = ms.getCache();
    if (cache != null) {
      tcm.clear(cache);
    }
    return delegate.update(ms, parameterObject);
  }


  public List query(MappedStatement ms, Object parameterObject, int offset, int limit, ResultHandler resultHandler) throws SQLException {
    if (ms != null) {
      Cache cache = ms.getCache();
      if (cache != null) {
        cache.getReadWriteLock().readLock().lock();
        try {
          CacheKey key = createCacheKey(ms, parameterObject, offset, limit);
          if (cache.hasKey(key)) {
            return (List) cache.getObject(key);
          } else {
            List list = delegate.query(ms, parameterObject, offset, limit, resultHandler);
            tcm.putObject(cache, key, list);
            return list;
          }
        } finally {
          cache.getReadWriteLock().readLock().unlock();
        }
      }
    }
    return delegate.query(ms, parameterObject, offset, limit, resultHandler);
  }

  public List flushStatements() throws SQLException {
    return delegate.flushStatements();
  }

  public void commit(boolean required) throws SQLException {
    delegate.commit(required);
    tcm.commit();
  }

  public void rollback(boolean required) throws SQLException {
    try {
      delegate.rollback(required);
    } finally {
      tcm.rollback();
    }
  }

  public CacheKey createCacheKey(MappedStatement ms, Object parameterObject, int offset, int limit) {
    return delegate.createCacheKey(ms, parameterObject, offset, limit);
  }

  public boolean isCached(MappedStatement ms, CacheKey key) {
    throw new UnsupportedOperationException("The CachingExecutor should not be used by result loaders and thus isCached() should never be called.");
  }

  public void deferLoad(MappedStatement ms, MetaObject resultObject, String property, CacheKey key) {
    throw new UnsupportedOperationException("The CachingExecutor should not be used by result loaders and thus deferLoad() should never be called.");
  }

}
