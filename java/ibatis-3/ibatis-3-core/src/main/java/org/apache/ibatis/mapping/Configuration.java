package org.apache.ibatis.mapping;

import org.apache.ibatis.binding.MapperRegistry;
import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.decorators.FifoCache;
import org.apache.ibatis.cache.decorators.LruCache;
import org.apache.ibatis.cache.decorators.SoftCache;
import org.apache.ibatis.cache.decorators.WeakCache;
import org.apache.ibatis.cache.impl.PerpetualCache;
import org.apache.ibatis.datasource.jndi.JndiDataSourceFactory;
import org.apache.ibatis.datasource.pooled.PooledDataSourceFactory;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSourceFactory;
import org.apache.ibatis.executor.*;
import org.apache.ibatis.executor.parameter.DefaultParameterHandler;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.result.ResultHandler;
import org.apache.ibatis.executor.resultset.DefaultResultSetHandler;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.InterceptorChain;
import org.apache.ibatis.reflection.DefaultObjectFactory;
import org.apache.ibatis.reflection.ObjectFactory;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.apache.ibatis.transaction.managed.ManagedTransactionFactory;
import org.apache.ibatis.type.TypeAliasRegistry;
import org.apache.ibatis.type.TypeHandlerRegistry;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Configuration {

  private Environment environment;

  private boolean lazyLoadingEnabled = true;
  private boolean enhancementEnabled = false;
  private boolean multipleResultSetsEnabled = true;
  private boolean generatedKeysEnabled = false;
  private boolean useColumnLabel = true;
  private boolean cacheEnabled = true;
  private Integer defaultStatementTimeout;
  private ExecutorType defaultExecutorType = ExecutorType.SIMPLE;

  private Properties variables = new Properties();
  private ObjectFactory objectFactory = new DefaultObjectFactory();
  private MapperRegistry mapperRegistry = new MapperRegistry(this);

  private final InterceptorChain interceptorChain = new InterceptorChain();
  private final TypeHandlerRegistry typeHandlerRegistry = new TypeHandlerRegistry();
  private final TypeAliasRegistry typeAliasRegistry = new TypeAliasRegistry();
  private final Map<String, MappedStatement> mappedStatements = new StrictMap<String, MappedStatement>("Mapped Statements collection");
  private final Map<String, Cache> caches = new StrictMap<String, Cache>("Caches collection");
  private final Map<String, ResultMap> resultMaps = new StrictMap<String, ResultMap>("Result Maps collection");
  private final Map<String, ParameterMap> parameterMaps = new StrictMap<String, ParameterMap>("Parameter Maps collection");

  public Configuration(Environment environment) {
    this();
    this.environment = environment;
  }

  public Configuration() {
    typeAliasRegistry.registerAlias("JDBC", JdbcTransactionFactory.class.getName());
    typeAliasRegistry.registerAlias("MANAGED", ManagedTransactionFactory.class.getName());
    typeAliasRegistry.registerAlias("JNDI", JndiDataSourceFactory.class.getName());
    typeAliasRegistry.registerAlias("POOLED", PooledDataSourceFactory.class.getName());
    typeAliasRegistry.registerAlias("UNPOOLED", UnpooledDataSourceFactory.class.getName());

    typeAliasRegistry.registerAlias("PERPETUAL", PerpetualCache.class.getName());
    typeAliasRegistry.registerAlias("FIFO", FifoCache.class.getName());
    typeAliasRegistry.registerAlias("LRU", LruCache.class.getName());
    typeAliasRegistry.registerAlias("SOFT", SoftCache.class.getName());
    typeAliasRegistry.registerAlias("WEAK", WeakCache.class.getName());
  }

  public Environment getEnvironment() {
    return environment;
  }

  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }

  public boolean isLazyLoadingEnabled() {
    return lazyLoadingEnabled;
  }

  public void setLazyLoadingEnabled(boolean lazyLoadingEnabled) {
    this.lazyLoadingEnabled = lazyLoadingEnabled;
  }

  public boolean isEnhancementEnabled() {
    return enhancementEnabled;
  }

  public void setEnhancementEnabled(boolean enhancementEnabled) {
    this.enhancementEnabled = enhancementEnabled;
  }

  public boolean isMultipleResultSetsEnabled() {
    return multipleResultSetsEnabled;
  }

  public void setMultipleResultSetsEnabled(boolean multipleResultSetsEnabled) {
    this.multipleResultSetsEnabled = multipleResultSetsEnabled;
  }

  public boolean isGeneratedKeysEnabled() {
    return generatedKeysEnabled;
  }

  public void setGeneratedKeysEnabled(boolean generatedKeysEnabled) {
    this.generatedKeysEnabled = generatedKeysEnabled;
  }

  public ExecutorType getDefaultExecutorType() {
    return defaultExecutorType;
  }

  public void setDefaultExecutorType(ExecutorType defaultExecutorType) {
    this.defaultExecutorType = defaultExecutorType;
  }

  public boolean isCacheEnabled() {
    return cacheEnabled;
  }

  public void setCacheEnabled(boolean cacheEnabled) {
    this.cacheEnabled = cacheEnabled;
  }

  public Integer getDefaultStatementTimeout() {
    return defaultStatementTimeout;
  }

  public void setDefaultStatementTimeout(Integer defaultStatementTimeout) {
    this.defaultStatementTimeout = defaultStatementTimeout;
  }

  public boolean isUseColumnLabel() {
    return useColumnLabel;
  }

  public void setUseColumnLabel(boolean useColumnLabel) {
    this.useColumnLabel = useColumnLabel;
  }

  public Properties getVariables() {
    return variables;
  }

  public void setVariables(Properties variables) {
    this.variables = variables;
  }

  public TypeHandlerRegistry getTypeHandlerRegistry() {
    return typeHandlerRegistry;
  }

  public TypeAliasRegistry getTypeAliasRegistry() {
    return typeAliasRegistry;
  }

  public ObjectFactory getObjectFactory() {
    return objectFactory;
  }

  public void setObjectFactory(ObjectFactory objectFactory) {
    this.objectFactory = objectFactory;
  }

  public ParameterHandler newParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql) {
    ParameterHandler parameterHandler = new DefaultParameterHandler(mappedStatement, parameterObject, boundSql);
    parameterHandler = (ParameterHandler) interceptorChain.pluginAll(parameterHandler);
    return parameterHandler;
  }

  public ResultSetHandler newResultSetHandler(Executor executor, MappedStatement mappedStatement, int rowOffset, int rowLimit, ParameterHandler parameterHandler, ResultHandler resultHandler, BoundSql boundSql) {
    ResultSetHandler resultSetHandler = new DefaultResultSetHandler(this, executor, mappedStatement, parameterHandler, rowOffset, rowLimit, resultHandler, boundSql);
    return resultSetHandler;
  }

  public StatementHandler newStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameterObject, int rowOffset, int rowLimit, ResultHandler resultHandler) {
    StatementHandler statementHandler = new RoutingStatementHandler(executor, mappedStatement, parameterObject, rowOffset, rowLimit, resultHandler);
    statementHandler = (StatementHandler) interceptorChain.pluginAll(statementHandler);
    return statementHandler;
  }

  public Executor newExecutor(Transaction transaction) {
    return newExecutor(transaction, defaultExecutorType);
  }

  public Executor newExecutor(Transaction transaction, ExecutorType executorType) {
    executorType = executorType == null ? defaultExecutorType : executorType;
    executorType = executorType == null ? ExecutorType.SIMPLE : executorType;
    Executor executor;
    if (ExecutorType.BATCH == executorType) {
      executor = new BatchExecutor(transaction);
    } else if (ExecutorType.REUSE == executorType) {
      executor = new ReuseExecutor(transaction);
    } else {
      executor = new SimpleExecutor(transaction);
    }
    if (cacheEnabled) {
      executor = new CachingExecutor(executor);
    }
    executor = (Executor) interceptorChain.pluginAll(executor);
    return executor;
  }

  public void addCache(Cache cache) {
    caches.put(cache.getId(), cache);
  }

  public Collection<String> getCacheNames() {
    return caches.keySet();
  }

  public Collection<Cache> getCaches() {
    return caches.values();
  }

  public Cache getCache(String id) {
    return caches.get(id);
  }

  public void addResultMap(ResultMap rm) {
    resultMaps.put(rm.getId(), rm);
  }

  public Collection<String> getResultMapNames() {
    return resultMaps.keySet();
  }

  public Collection<ResultMap> getResultMaps() {
    return resultMaps.values();
  }

  public ResultMap getResultMap(String id) {
    return resultMaps.get(id);
  }

  public void addParameterMap(ParameterMap pm) {
    parameterMaps.put(pm.getId(), pm);
  }

  public Collection<String> getParameterMapNames() {
    return parameterMaps.keySet();
  }

  public Collection<ParameterMap> getParameterMaps() {
    return parameterMaps.values();
  }

  public ParameterMap getParameterMap(String id) {
    return parameterMaps.get(id);
  }

  public void addMappedStatement(MappedStatement ms) {
    mappedStatements.put(ms.getId(), ms);
  }

  public Collection<String> getMappedStatementNames() {
    return mappedStatements.keySet();
  }

  public Collection<MappedStatement> getMappedStatements() {
    return mappedStatements.values();
  }

  public MappedStatement getMappedStatement(String id) {
    return mappedStatements.get(id);
  }

  public void addInterceptor(Interceptor interceptor) {
    interceptorChain.addInterceptor(interceptor);
  }

  public <T> void addMapper(Class<T> type) {
    mapperRegistry.addMapper(type);
  }

  public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
    return mapperRegistry.getMapper(type, sqlSession);
  }

  public boolean hasMapper(Class type) {
    return mapperRegistry.hasMapper(type);
  }

  private static class StrictMap<J, K> extends HashMap<J, K> {

    private String name;

    public StrictMap(String name, int initialCapacity, float loadFactor) {
      super(initialCapacity, loadFactor);
      this.name = name;
    }

    public StrictMap(String name, int initialCapacity) {
      super(initialCapacity);
      this.name = name;
    }

    public StrictMap(String name) {
      super();
      this.name = name;
    }

    public StrictMap(String name, Map<? extends J, ? extends K> m) {
      super(m);
      this.name = name;
    }

    public K put(J key, K value) {
      if (containsKey(key)) throw new IllegalArgumentException(name + " already contains value for " + key);
      return super.put(key, value);
    }

    public K get(Object key) {
      K value = super.get(key);
      if (value == null) throw new IllegalArgumentException(name + " does not contain value for " + key);
      return value;
    }

  }

}
