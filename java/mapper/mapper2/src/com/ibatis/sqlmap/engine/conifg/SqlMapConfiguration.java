package com.ibatis.sqlmap.engine.conifg;

import com.ibatis.common.beans.ClassInfo;
import com.ibatis.common.beans.Probe;
import com.ibatis.common.beans.ProbeFactory;
import com.ibatis.common.resources.Resources;
import com.ibatis.sqlmap.client.SqlMapException;
import com.ibatis.sqlmap.client.extensions.TypeHandlerCallback;
import com.ibatis.sqlmap.engine.accessplan.AccessPlanFactory;
import com.ibatis.sqlmap.engine.cache.CacheModel;
import com.ibatis.sqlmap.engine.cache.fifo.FifoCacheController;
import com.ibatis.sqlmap.engine.cache.lru.LruCacheController;
import com.ibatis.sqlmap.engine.cache.memory.MemoryCacheController;
import com.ibatis.sqlmap.engine.datasource.DataSourceFactory;
import com.ibatis.sqlmap.engine.datasource.DbcpDataSourceFactory;
import com.ibatis.sqlmap.engine.datasource.JndiDataSourceFactory;
import com.ibatis.sqlmap.engine.datasource.SimpleDataSourceFactory;
import com.ibatis.sqlmap.engine.impl.ExtendedSqlMapClient;
import com.ibatis.sqlmap.engine.impl.SqlMapClientImpl;
import com.ibatis.sqlmap.engine.impl.SqlMapExecutorDelegate;
import com.ibatis.sqlmap.engine.mapping.result.Discriminator;
import com.ibatis.sqlmap.engine.mapping.result.ResultMap;
import com.ibatis.sqlmap.engine.mapping.result.ResultObjectFactory;
import com.ibatis.sqlmap.engine.mapping.statement.GeneralStatement;
import com.ibatis.sqlmap.engine.mapping.statement.MappedStatement;
import com.ibatis.sqlmap.engine.scope.ErrorContext;
import com.ibatis.sqlmap.engine.transaction.TransactionConfig;
import com.ibatis.sqlmap.engine.transaction.TransactionManager;
import com.ibatis.sqlmap.engine.transaction.external.ExternalTransactionConfig;
import com.ibatis.sqlmap.engine.transaction.jdbc.JdbcTransactionConfig;
import com.ibatis.sqlmap.engine.transaction.jta.JtaTransactionConfig;
import com.ibatis.sqlmap.engine.type.*;

import javax.sql.DataSource;
import java.util.Iterator;
import java.util.Properties;

public class SqlMapConfiguration {
  private static final Probe PROBE = ProbeFactory.getProbe();
  private ErrorContext errorContext;
  private SqlMapExecutorDelegate delegate;
  private TypeHandlerFactory typeHandlerFactory;
  private ExtendedSqlMapClient client;
  private Integer defaultStatementTimeout;
  private DataSource dataSource;

  public SqlMapConfiguration() {
    errorContext = new ErrorContext();
    delegate = new SqlMapExecutorDelegate();
    typeHandlerFactory = delegate.getTypeHandlerFactory();
    client = new SqlMapClientImpl(delegate);
    registerDefaultTypeAliases();
  }

  public TypeHandlerFactory getTypeHandlerFactory() {
    return typeHandlerFactory;
  }

  public ErrorContext getErrorContext() {
    return errorContext;
  }

  public ExtendedSqlMapClient getClient() {
    return client;
  }

  public SqlMapExecutorDelegate getDelegate() {
    return delegate;
  }

  public Integer getDefaultStatementTimeout() {
    return defaultStatementTimeout;
  }

  //
  // Utility Methods
  //

  public TypeHandler resolveTypeHandler(TypeHandlerFactory typeHandlerFactory, Class clazz, String propertyName, String javaType, String jdbcType) {
    return resolveTypeHandler(typeHandlerFactory, clazz, propertyName, javaType, jdbcType, false);
  }

  public TypeHandler resolveTypeHandler(TypeHandlerFactory typeHandlerFactory, Class clazz, String propertyName, String javaType, String jdbcType, boolean useSetterToResolve) {
    TypeHandler handler;
    if (clazz == null) {
      // Unknown
      handler = typeHandlerFactory.getUnkownTypeHandler();
    } else if (DomTypeMarker.class.isAssignableFrom(clazz)) {
      // DOM
      handler = typeHandlerFactory.getTypeHandler(String.class, jdbcType);
    } else if (java.util.Map.class.isAssignableFrom(clazz)) {
      // Map
      if (javaType == null) {
        handler = typeHandlerFactory.getUnkownTypeHandler(); //BUG 1012591 - typeHandlerFactory.getTypeHandler(java.lang.Object.class, jdbcType);
      } else {
        try {
          Class javaClass = Resources.classForName(javaType);
          handler = typeHandlerFactory.getTypeHandler(javaClass, jdbcType);
        } catch (Exception e) {
          throw new RuntimeException("Error.  Could not set TypeHandler.  Cause: " + e, e);
        }
      }
    } else if (typeHandlerFactory.getTypeHandler(clazz, jdbcType) != null) {
      // Primitive
      handler = typeHandlerFactory.getTypeHandler(clazz, jdbcType);
    } else {
      // JavaBean
      if (javaType == null) {
        if (useSetterToResolve) {
          Class type = PROBE.getPropertyTypeForSetter(clazz, propertyName);
          handler = typeHandlerFactory.getTypeHandler(type, jdbcType);
        } else {
          Class type = PROBE.getPropertyTypeForGetter(clazz, propertyName);
          handler = typeHandlerFactory.getTypeHandler(type, jdbcType);
        }
      } else {
        try {
          Class javaClass = Resources.classForName(javaType);
          handler = typeHandlerFactory.getTypeHandler(javaClass, jdbcType);
        } catch (Exception e) {
          throw new RuntimeException("Error.  Could not set TypeHandler.  Cause: " + e, e);
        }
      }
    }
    return handler;
  }

  // TODO: Split into separate methods
  public void setSettings(boolean classInfoCacheEnabled, boolean lazyLoadingEnabled, boolean statementCachingEnabled, boolean cacheModelsEnabled, boolean enhancementEnabled, Integer maxTransactions, Integer maxRequests, Integer maxSessions, Integer defaultTimeout) {
    errorContext.setActivity("loading settings properties");
    ClassInfo.setCacheEnabled(classInfoCacheEnabled);
    client.getDelegate().setLazyLoadingEnabled(lazyLoadingEnabled);
    client.getDelegate().setStatementCacheEnabled(statementCachingEnabled);
    client.getDelegate().setCacheModelsEnabled(cacheModelsEnabled);
    try {
      enhancementEnabled = enhancementEnabled && Resources.classForName("net.sf.cglib.proxy.InvocationHandler") != null;
    } catch (ClassNotFoundException e) {
      enhancementEnabled = false;
    }
    client.getDelegate().setEnhancementEnabled(enhancementEnabled);
    if (maxTransactions != null && maxTransactions.intValue() > 0) {
      client.getDelegate().setMaxTransactions(maxTransactions.intValue());
    }
    if (maxRequests != null && maxRequests.intValue() > 0) {
      client.getDelegate().setMaxRequests(maxRequests.intValue());
    }
    if (maxSessions != null && maxSessions.intValue() > 0) {
      client.getDelegate().setMaxSessions(maxSessions.intValue());
    }
    AccessPlanFactory.setBytecodeEnhancementEnabled(client.getDelegate().isEnhancementEnabled());
    if (defaultTimeout != null) {
      try {
        defaultStatementTimeout = defaultTimeout;
      } catch (NumberFormatException e) {
        throw new SqlMapException("Specified defaultStatementTimeout is not a valid integer");
      }
    }
  }

  public void addTypeAlias(String alias, String type) {
    typeHandlerFactory.putTypeAlias(alias, type);
  }

  public void addGlobalTypeHandler(String javaType, String jdbcType, String callback) {
    try {
      errorContext.setActivity("building a building custom type handler");
      TypeHandlerFactory typeHandlerFactory = client.getDelegate().getTypeHandlerFactory();
      callback = typeHandlerFactory.resolveAlias(callback);
      javaType = typeHandlerFactory.resolveAlias(javaType);
      errorContext.setMoreInfo("Check the callback attribute '" + callback + "' (must be a classname).");
      TypeHandler typeHandler;
      Object impl = Resources.instantiate(callback);
      if (impl instanceof TypeHandlerCallback) {
        typeHandler = new CustomTypeHandler((TypeHandlerCallback) impl);
      } else if (impl instanceof TypeHandler) {
        typeHandler = (TypeHandler) impl;
      } else {
        throw new RuntimeException("The class '' is not a valid implementation of TypeHandler or TypeHandlerCallback");
      }
      errorContext.setMoreInfo("Check the javaType attribute '" + javaType + "' (must be a classname) or the jdbcType '" + jdbcType + "' (must be a JDBC type name).");
      if (jdbcType != null && jdbcType.length() > 0) {
        typeHandlerFactory.register(Resources.classForName(javaType), jdbcType, typeHandler);
      } else {
        typeHandlerFactory.register(Resources.classForName(javaType), typeHandler);
      }
    } catch (Exception e) {
      throw new SqlMapException("Error registering occurred.  Cause: " + e, e);
    }
    errorContext.setMoreInfo(null);
    errorContext.setObjectId(null);
  }

  //TODO: pass in datasource as a parameter to setTXMgr
  public void setDataSource(String type, Properties props) {
    type = typeHandlerFactory.resolveAlias(type);
    try {
      errorContext.setMoreInfo("Check the data source type or class.");
      DataSourceFactory dsFactory = (DataSourceFactory) Resources.instantiate(type);
      errorContext.setMoreInfo("Check the data source properties or configuration.");
      dsFactory.initialize(props);
      dataSource = dsFactory.getDataSource();
      errorContext.setMoreInfo(null);
    } catch (Exception e) {
      if (e instanceof SqlMapException) {
        throw (SqlMapException) e;
      } else {
        throw new SqlMapException("Error initializing DataSource.  Could not instantiate DataSourceFactory.  Cause: " + e, e);
      }
    }
  }

  public void setTransactionManager(String type, boolean commitRequired, Properties props) {
    errorContext.setActivity("configuring the transaction manager");
    type = typeHandlerFactory.resolveAlias(type);
    TransactionManager txManager;
    try {
      errorContext.setMoreInfo("Check the transaction manager type or class.");
      TransactionConfig config = (TransactionConfig) Resources.instantiate(type);
      config.setDataSource(dataSource);
      config.setMaximumConcurrentTransactions(client.getDelegate().getMaxTransactions());
      errorContext.setMoreInfo("Check the transactio nmanager properties or configuration.");
      config.initialize(props);
      errorContext.setMoreInfo(null);
      txManager = new TransactionManager(config);
      txManager.setForceCommit(commitRequired);
    } catch (Exception e) {
      if (e instanceof SqlMapException) {
        throw (SqlMapException) e;
      } else {
        throw new SqlMapException("Error initializing TransactionManager.  Could not instantiate TransactionConfig.  Cause: " + e, e);
      }
    }
    client.getDelegate().setTxManager(txManager);
  }

  public void setResultObjectFactory(String type) {
    errorContext.setActivity("configuring the Result Object Factory");
    ResultObjectFactory rof;
    try {
      rof = (ResultObjectFactory) Resources.instantiate(type);
      delegate.setResultObjectFactory(rof);
    } catch (Exception e) {
      throw new SqlMapException("Error instantiating resultObjectFactory: " + type, e);
    }
  }

  // TODO - post processing sql map config
  public void wireupCacheModels() {
    Iterator cacheNames = client.getDelegate().getCacheModelNames();
    while (cacheNames.hasNext()) {
      String cacheName = (String) cacheNames.next();
      CacheModel cacheModel = client.getDelegate().getCacheModel(cacheName);
      Iterator statementNames = cacheModel.getFlushTriggerStatementNames();
      while (statementNames.hasNext()) {
        String statementName = (String) statementNames.next();
        MappedStatement statement = client.getDelegate().getMappedStatement(statementName);
        if (statement != null) {
          statement.addExecuteListener(cacheModel);
        } else {
          throw new RuntimeException("Could not find statement named '" + statementName + "' for use as a flush trigger for the cache model named '" + cacheName + "'.");
        }
      }
    }
  }

  // TODO: post processing sql map
  public void bindDelegateSubMaps() {
    Iterator names = delegate.getResultMapNames();
    while (names.hasNext()) {
      String name = (String) names.next();
      ResultMap rm = delegate.getResultMap(name);
      Discriminator disc = rm.getDiscriminator();
      if (disc != null) {
        disc.bindSubMaps();
      }
    }
  }

  public ParameterMapConfig newParameterMapConfig(String id, String parameterClassName) {
    return new ParameterMapConfig(this, id, parameterClassName);
  }

  public ResultMapConfig newResultMapConfig(String id, String resultClassName, String groupBy, String extended, String xmlName) {
    return new ResultMapConfig(this, id, resultClassName, groupBy, extended, xmlName);
  }

  public CacheModelConfig newCacheModelConfig(String id, String type, Boolean readOnly, Boolean serialize) {
    return new CacheModelConfig(this, id, type, readOnly, serialize);
  }

  public MappedStatementConfig newMappedStatementConfig(String id, GeneralStatement statement, SqlSource processor, String parameterMapName, String parameterClassName, String resultMapName, String[] additionalResultMapNames, String resultClassName, String[] additionalResultClasses, String resultSetType, String fetchSize, String allowRemapping, String timeout, String cacheModelName, String xmlResultName) {
    return new MappedStatementConfig(this, id, statement, processor, parameterMapName, parameterClassName, resultMapName, additionalResultMapNames, resultClassName, additionalResultClasses, cacheModelName, resultSetType, fetchSize, allowRemapping, timeout, xmlResultName);
  }

  private void registerDefaultTypeAliases() {
    // TRANSACTION ALIASES
    typeHandlerFactory.putTypeAlias("JDBC", JdbcTransactionConfig.class.getName());
    typeHandlerFactory.putTypeAlias("JTA", JtaTransactionConfig.class.getName());
    typeHandlerFactory.putTypeAlias("EXTERNAL", ExternalTransactionConfig.class.getName());

    // DATA SOURCE ALIASES
    typeHandlerFactory.putTypeAlias("SIMPLE", SimpleDataSourceFactory.class.getName());
    typeHandlerFactory.putTypeAlias("DBCP", DbcpDataSourceFactory.class.getName());
    typeHandlerFactory.putTypeAlias("JNDI", JndiDataSourceFactory.class.getName());

    // CACHE ALIASES
    typeHandlerFactory.putTypeAlias("FIFO", FifoCacheController.class.getName());
    typeHandlerFactory.putTypeAlias("LRU", LruCacheController.class.getName());
    typeHandlerFactory.putTypeAlias("MEMORY", MemoryCacheController.class.getName());
    // use a string for OSCache to avoid unnecessary loading of properties upon init
    typeHandlerFactory.putTypeAlias("OSCACHE", "com.ibatis.sqlmap.engine.cache.oscache.OSCacheController");

    // TYPE ALIASEs
    typeHandlerFactory.putTypeAlias("dom", DomTypeMarker.class.getName());
    typeHandlerFactory.putTypeAlias("domCollection", DomCollectionTypeMarker.class.getName());
    typeHandlerFactory.putTypeAlias("xml", XmlTypeMarker.class.getName());
    typeHandlerFactory.putTypeAlias("xmlCollection", XmlCollectionTypeMarker.class.getName());
  }

}
