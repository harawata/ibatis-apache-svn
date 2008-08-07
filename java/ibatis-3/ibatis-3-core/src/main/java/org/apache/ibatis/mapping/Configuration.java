package org.apache.ibatis.mapping;

import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.executor.*;
import org.apache.ibatis.executor.parameter.*;
import org.apache.ibatis.executor.result.ResultHandler;
import org.apache.ibatis.executor.resultset.*;
import org.apache.ibatis.executor.statement.*;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.*;
import org.apache.ibatis.type.*;

import java.sql.Connection;
import java.util.*;

public class Configuration {

  private boolean lazyLoadingEnabled = true;
  private boolean enhancementEnabled = false;
  private boolean multipleResultSetsEnabled = true;
  private boolean generatedKeysEnabled = true;
  private boolean useColumnLabel = true;
  private boolean cacheEnabled;

  //TODO:  Make enum for reuse/batch/simple executors
  private boolean statementCachingEnabled;
  private boolean batchUpdatesEnabled;

  private Properties variables = new Properties();
  private ObjectFactory objectFactory = new DefaultObjectFactory();

  private final InterceptorChain interceptorChain = new InterceptorChain();
  private final TypeHandlerRegistry typeHandlerRegistry = new TypeHandlerRegistry();
  private final TypeAliasRegistry typeAliasRegistry = new TypeAliasRegistry();
  private final Map<String, MappedStatement> mappedStatements = new HashMap<String, MappedStatement>();
  private final Map<String, Cache> caches = new HashMap<String, Cache>();
  private final Map<String, ResultMap> resultMaps = new HashMap<String, ResultMap>();
  private final Map<String, ParameterMap> parameterMaps = new HashMap<String, ParameterMap>();
  private Integer defaultStatementTimeout;

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

  public boolean isStatementCachingEnabled() {
    return statementCachingEnabled;
  }

  public void setStatementCachingEnabled(boolean statementCachingEnabled) {
    this.statementCachingEnabled = statementCachingEnabled;
  }

  public boolean isBatchUpdatesEnabled() {
    return batchUpdatesEnabled;
  }

  public void setBatchUpdatesEnabled(boolean batchUpdatesEnabled) {
    this.batchUpdatesEnabled = batchUpdatesEnabled;
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

  public ParameterHandler newParameterHandler(MappedStatement mappedStatement, Object parameterObject) {
    ParameterHandler parameterHandler = new DefaultParameterHandler(mappedStatement, parameterObject);
    parameterHandler = (ParameterHandler) interceptorChain.pluginAll(parameterHandler);
    return parameterHandler;
  }

  public ResultSetHandler newResultSetHandler(Executor executor, MappedStatement mappedStatement, int rowOffset, int rowLimit, ParameterHandler parameterHandler, ResultHandler resultHandler) {
    ResultSetHandler resultSetHandler = new DefaultResultSetHandler(executor, mappedStatement, parameterHandler, rowOffset, rowLimit, resultHandler);
    resultSetHandler = (ResultSetHandler) interceptorChain.pluginAll(resultSetHandler);
    return resultSetHandler;
  }

  public StatementHandler newStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameterObject, int rowOffset, int rowLimit, ResultHandler resultHandler) {
    StatementHandler statementHandler = new RoutingStatementHandler(executor, mappedStatement, parameterObject, rowOffset, rowLimit, resultHandler);
    statementHandler = (StatementHandler) interceptorChain.pluginAll(statementHandler);
    return statementHandler;
  }

  public Executor newExecutor(Connection conn) {
    Executor executor;
    if (batchUpdatesEnabled) {
      executor = new BatchExecutor(conn);
    } else if (statementCachingEnabled) {
      executor = new ReuseExecutor(conn);
    } else {
      executor = new SimpleExecutor(conn);
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

  public Collection<Cache> getCaches() {
    return caches.values();
  }

  public Cache getCache(String id) {
    return caches.get(id);
  }

  public void addResultMap(ResultMap rm) {
    resultMaps.put(rm.getId(), rm);
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

  public Collection<ParameterMap> getParameterMaps() {
    return parameterMaps.values();
  }

  public ParameterMap getParameterMap(String id) {
    return parameterMaps.get(id);
  }

  public void addMappedStatement(MappedStatement ms) {
    mappedStatements.put(ms.getId(), ms);
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

}
