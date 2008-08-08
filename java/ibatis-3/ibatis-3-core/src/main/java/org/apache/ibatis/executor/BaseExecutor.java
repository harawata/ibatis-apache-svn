package org.apache.ibatis.executor;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.cache.impl.PerpetualCache;
import org.apache.ibatis.executor.result.ResultHandler;
import org.apache.ibatis.logging.*;
import org.apache.ibatis.logging.jdbc.ConnectionLogger;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.type.TypeHandlerRegistry;

import java.sql.*;
import java.util.*;

public abstract class BaseExecutor implements Executor {

  private static final Log log = LogFactory.getLog(BaseExecutor.class);
  private static final Object EXECUTION_PLACEHOLDER = new Object();

  protected final Connection connection;

  protected final List<DeferredLoad> deferredLoads;
  protected final PerpetualCache localCache;

  protected int queryStack = 0;

  protected List<BatchResult> batchResults = new ArrayList<BatchResult>();

  protected BaseExecutor(Connection connection) {
    if (log.isDebugEnabled()) {
      this.connection = ConnectionLogger.newInstance(connection);
    } else {
      this.connection = connection;
    }
    this.deferredLoads = new ArrayList<DeferredLoad>();
    this.localCache = new PerpetualCache();
  }

  public Connection getConnection() {
    return connection;
  }

  public void close() {
    try {
      connection.close();
    } catch (SQLException e) {
      // Ignore.  There's nothing that can be done at this point.
    }
  }

  public int update(MappedStatement ms, Object parameter) throws SQLException {
    localCache.clear();
    return doUpdate(ms, parameter);
  }

  public List flushStatements() throws SQLException {
    batchResults.addAll(doFlushStatements());
    return batchResults;
  }

  public List query(MappedStatement ms, Object parameter, int offset, int limit, ResultHandler resultHandler) throws SQLException {
    List list;
    try {
      queryStack++;
      CacheKey key = createCacheKey(ms, parameter, offset, limit);
      if (localCache.hasKey(key)) {
        list = (List) localCache.getObject(key);
      } else {
        localCache.putObject(key, EXECUTION_PLACEHOLDER);
        try {
          list = doQuery(ms, parameter, offset, limit, resultHandler);
        } finally {
          localCache.removeObject(key);
        }
        localCache.putObject(key, list);
      }
    } finally {
      queryStack--;
    }
    if (queryStack == 0) {
      for (DeferredLoad deferredLoad : deferredLoads) {
        deferredLoad.load();
      }
    }
    return list;
  }

  public void deferLoad(MappedStatement ms, MetaObject resultObject, String property, CacheKey key) {
    deferredLoads.add(new DeferredLoad(ms, resultObject, property, key));
  }

  public CacheKey createCacheKey(MappedStatement ms, Object parameterObject, int offset, int limit) {
    CacheKey cacheKey = new CacheKey();
    cacheKey.update(ms.getId());
    cacheKey.update(offset);
    cacheKey.update(limit);
    if (ms.getDynamicParameterMappings(parameterObject).size() > 0 && parameterObject != null) {
      TypeHandlerRegistry typeHandlerRegistry = ms.getConfiguration().getTypeHandlerRegistry();
      if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
        cacheKey.update(parameterObject);
      } else {
        MetaObject metaObject = MetaObject.forObject(parameterObject);
        List<ParameterMapping> parameterMappings = ms.getDynamicParameterMappings(parameterObject);
        for (ParameterMapping parameterMapping : parameterMappings) {
          cacheKey.update(metaObject.getValue(parameterMapping.getProperty()));
        }
      }
    }
    return cacheKey;
  }

  public boolean isCached(MappedStatement ms, CacheKey key) {
    return localCache.hasKey(key);
  }

  public void commit(boolean required) throws SQLException {
    flushStatements();
    if (required) {
      connection.commit();
    }
  }

  public void rollback(boolean required) throws SQLException {
    if (required) {
      connection.rollback();
    }
  }

  protected abstract int doUpdate(MappedStatement ms, Object parameter)
      throws SQLException;

  protected abstract List<BatchResult> doFlushStatements()
      throws SQLException;

  protected abstract List doQuery(MappedStatement ms, Object parameter, int offset, int limit, ResultHandler resultHandler)
      throws SQLException;

  protected void closeStatement(Statement statement) {
    if (statement != null) {
      try {
        statement.close();
      } catch (SQLException e) {
        // ignore
      }
    }
  }

  private class DeferredLoad {

    MappedStatement mappedStatement;
    private MetaObject resultObject;
    private String property;
    private CacheKey key;

    public DeferredLoad(MappedStatement mappedStatement, MetaObject resultObject, String property, CacheKey key) {
      this.mappedStatement = mappedStatement;
      this.resultObject = resultObject;
      this.property = property;
      this.key = key;
    }

    public void load() {
      Object value = null;
      List list = (List) localCache.getObject(key);
      Class targetType = resultObject.getSetterType(property);
      if (Set.class.isAssignableFrom(targetType)) {
        value = new HashSet(list);
      } else if (Collection.class.isAssignableFrom(targetType)) {
        value = list;
      } else if (targetType.isArray()) {
        Object array = java.lang.reflect.Array.newInstance(targetType.getComponentType(), list.size());
        value = list.toArray((Object[]) array);
      } else {
        if (list.size() > 1) {
          throw new RuntimeException("Statement returned more than one row, where no more than one was expected.");
        } else if (list.size() == 1) {
          value = list.get(0);
        }
      }
      resultObject.setValue(property, value);
    }

  }


}