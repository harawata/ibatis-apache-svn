package org.apache.ibatis.executor;

import org.apache.ibatis.cache.*;
import org.apache.ibatis.executor.result.ResultHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;

import java.sql.*;
import java.util.*;

public class CachingExecutor implements Executor {

  private Executor delegate;

  private Set<Cache> clearCaches;
  private List<CacheEntry> cacheEntries;

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
      if (clearCaches == null) {
        clearCaches = new HashSet<Cache>();
      }
      clearCaches.add(cache);
    }
    return delegate.update(ms, parameterObject);
  }

  public List query(MappedStatement ms, Object parameterObject, int offset, int limit, ResultHandler resultHandler) throws SQLException {
    Cache cache = ms.getCache();
    if (cache != null) {
      synchronized (cache) {
        CacheKey key = createCacheKey(ms, parameterObject, offset, limit);
        if (cache.hasKey(key)) {
          return (List) cache.getObject(key);
        } else {
          List list = delegate.query(ms, parameterObject, offset, limit, resultHandler);
          if (cacheEntries == null) {
            cacheEntries = new ArrayList<CacheEntry>();
          }
          cacheEntries.add(new CacheEntry(cache, key, list));
          return list;
        }
      }
    } else {
      return delegate.query(ms, parameterObject, offset, limit, resultHandler);
    }
  }

  public List flushStatements() throws SQLException {
    return delegate.flushStatements();
  }

  public void commit(boolean required) throws SQLException {
    if (clearCaches != null) {
      for (Cache cache : clearCaches) {
        cache.clear();
      }
      clearCaches.clear();
    }
    if (cacheEntries != null) {
      for (CacheEntry entry : cacheEntries) {
        entry.commit();
      }
      cacheEntries.clear();
    }
    delegate.commit(required);
  }

  public void rollback(boolean required) throws SQLException {
    if (clearCaches != null) clearCaches.clear();
    if (cacheEntries != null) cacheEntries.clear();
    delegate.rollback(required);
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

  private static class CacheEntry {
    private Cache cache;
    private CacheKey key;
    private Object value;

    public CacheEntry(Cache cache, CacheKey key, Object value) {
      this.cache = cache;
      this.key = key;
      this.value = value;
    }

    public void commit() {
      cache.putObject(key, value);
    }
  }

}
