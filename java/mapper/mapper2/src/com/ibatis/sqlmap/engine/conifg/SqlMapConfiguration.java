package com.ibatis.sqlmap.engine.conifg;

import com.ibatis.common.beans.*;
import com.ibatis.common.resources.Resources;
import com.ibatis.sqlmap.client.SqlMapException;
import com.ibatis.sqlmap.client.extensions.TypeHandlerCallback;
import com.ibatis.sqlmap.engine.accessplan.AccessPlanFactory;
import com.ibatis.sqlmap.engine.cache.CacheModel;
import com.ibatis.sqlmap.engine.cache.fifo.FifoCacheController;
import com.ibatis.sqlmap.engine.cache.lru.LruCacheController;
import com.ibatis.sqlmap.engine.cache.memory.MemoryCacheController;
import com.ibatis.sqlmap.engine.datasource.*;
import com.ibatis.sqlmap.engine.impl.*;
import com.ibatis.sqlmap.engine.mapping.parameter.*;
import com.ibatis.sqlmap.engine.mapping.result.*;
import com.ibatis.sqlmap.engine.mapping.statement.*;
import com.ibatis.sqlmap.engine.mapping.sql.*;
import com.ibatis.sqlmap.engine.mapping.sql.raw.RawSql;
import com.ibatis.sqlmap.engine.mapping.sql.dynamic.DynamicSql;
import com.ibatis.sqlmap.engine.mapping.sql.stat.StaticSql;
import com.ibatis.sqlmap.engine.mapping.sql.simple.SimpleDynamicSql;
import com.ibatis.sqlmap.engine.scope.ErrorContext;
import com.ibatis.sqlmap.engine.transaction.*;
import com.ibatis.sqlmap.engine.transaction.external.ExternalTransactionConfig;
import com.ibatis.sqlmap.engine.transaction.jdbc.JdbcTransactionConfig;
import com.ibatis.sqlmap.engine.transaction.jta.JtaTransactionConfig;
import com.ibatis.sqlmap.engine.type.*;

import javax.sql.DataSource;
import java.util.*;
import java.sql.ResultSet;

/**
 * A work in progress...it works, but there are many refactorings
 * still required to make it a usable API.  Most notably, the public
 * fields and the insane method signatures...all holdovers from the
 * old XML exclusive configuration.
 */
public class SqlMapConfiguration {

  private static final Probe PROBE = ProbeFactory.getProbe();
  private static final InlineParameterMapParser PARAM_PARSER = new InlineParameterMapParser();

  private ErrorContext errorContext = new ErrorContext();
  
  private ExtendedSqlMapClient client;
  private SqlMapExecutorDelegate delegate;
  private TypeHandlerFactory typeHandlerFactory;

  private Integer defaultStatementTimeout;
  private DataSource dataSource;

  // TODO: Move to ResultMapConfig
  private BasicResultMap resultMap;
  private List resultMappingList;
  private int resultMappingIndex;
  private Discriminator discriminator;

  // TODO: Move to ParameterMapConfig
  private BasicParameterMap parameterMap;
  private List parameterMappingList;

  // TODO: Move to CacheModelConfig
  public CacheModel cacheModel;
  public Properties cacheProps;

  public SqlMapConfiguration() {
    delegate = new SqlMapExecutorDelegate();
    typeHandlerFactory = delegate.getTypeHandlerFactory();
    client = new SqlMapClientImpl(delegate);
    registerDefaultTypeAliases();
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

  //
  // SQL Map Config methods
  //


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

  //
  // SQL Map methods
  //

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

  // TODO: pass into addCacheModel as parameter
  public void setFlushInterval(int hours, int minutes, int seconds, int milliseconds) {
    errorContext.setMoreInfo("Check the cache model flush interval.");
    long t = 0;
    t += milliseconds;
    t += seconds * 1000;
    t += minutes * 60 * 1000;
    t += hours * 60 * 60 * 1000;
    if (t < 1)
      throw new RuntimeException("A flush interval must specify one or more of milliseconds, seconds, minutes or hours.");
    cacheModel.setFlushInterval(t);
  }

  // TODO: remove in favour of working directly with cacheModel if possible
  public void addFlushTriggerStatement(String statement) {
    errorContext.setMoreInfo("Check the cache model flush on statement elements.");
    cacheModel.addFlushTriggerStatement(statement);
  }

  public void addCacheModel(String id, String type, Boolean readOnly, Boolean serialize, Properties props) {
    errorContext.setActivity("building a cache model");
    type = typeHandlerFactory.resolveAlias(type);
    if (readOnly != null) {
      cacheModel.setReadOnly(readOnly.booleanValue());
    } else {
      cacheModel.setReadOnly(true);
    }
    if (serialize != null) {
      cacheModel.setSerialize(serialize.booleanValue());
    } else {
      cacheModel.setSerialize(false);
    }

    errorContext.setObjectId(id + " cache model");

    errorContext.setMoreInfo("Check the cache model type.");
    cacheModel.setId(id);
    cacheModel.setResource(errorContext.getResource());

    try {
      cacheModel.setControllerClassName(type);
    } catch (Exception e) {
      throw new RuntimeException("Error setting Cache Controller Class.  Cause: " + e, e);
    }

    errorContext.setMoreInfo("Check the cache model configuration.");
    cacheModel.configure(props);

    if (client.getDelegate().isCacheModelsEnabled()) {
      client.getDelegate().addCacheModel(cacheModel);
    }

    errorContext.setMoreInfo(null);
    errorContext.setObjectId(null);
    cacheProps = null;
    cacheModel = null;
  }

  // TODO: merge with addPArameterMap
  public void finalizeParameterMap() {
    parameterMap.setParameterMappingList(parameterMappingList);

    client.getDelegate().addParameterMap(parameterMap);

    errorContext.setMoreInfo(null);
    errorContext.setObjectId(null);
  }

  // TODO: pass list into addParameterMap
  public void addParameterMapping(String callback, String javaType, String resultMap, String propertyName, String jdbcType, String type, String nullValue, String mode, String numericScale) {
    callback = typeHandlerFactory.resolveAlias(callback);
    javaType = typeHandlerFactory.resolveAlias(javaType);

    errorContext.setObjectId(propertyName + " mapping of the " + parameterMap.getId() + " parameter map");

    TypeHandler handler;
    if (callback != null) {
      errorContext.setMoreInfo("Check the parameter mapping typeHandler attribute '" + callback + "' (must be a TypeHandler or TypeHandlerCallback implementation).");
      try {
        Object impl = Resources.instantiate(callback);
        if (impl instanceof TypeHandlerCallback) {
          handler = new CustomTypeHandler((TypeHandlerCallback) impl);
        } else if (impl instanceof TypeHandler) {
          handler = (TypeHandler) impl;
        } else {
          throw new RuntimeException("The class '" + callback + "' is not a valid implementation of TypeHandler or TypeHandlerCallback");
        }
      } catch (Exception e) {
        throw new RuntimeException("Error occurred during custom type handler configuration.  Cause: " + e, e);
      }
    } else {
      errorContext.setMoreInfo("Check the parameter mapping property type or name.");
      handler = resolveTypeHandler(client.getDelegate().getTypeHandlerFactory(), parameterMap.getParameterClass(), propertyName, javaType, jdbcType);
    }

    BasicParameterMapping mapping = new BasicParameterMapping();
    mapping.setPropertyName(propertyName);
    mapping.setJdbcTypeName(jdbcType);
    mapping.setTypeName(type);
    mapping.setResultMapName(resultMap);
    mapping.setNullValue(nullValue);
    if (mode != null && mode.length() > 0) {
      mapping.setMode(mode);
    }
    mapping.setTypeHandler(handler);
    try {
      if (javaType != null && javaType.length() > 0) {
        mapping.setJavaType(Resources.classForName(javaType));
      }
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Error setting javaType on parameter mapping.  Cause: " + e);
    }

    if (numericScale != null) {
      try {
        Integer scale = Integer.valueOf(numericScale);
        if (scale.intValue() < 0) {
          throw new RuntimeException("Error setting numericScale on parameter mapping.  Cause: scale must be greater than or equal to zero");
        }

        mapping.setNumericScale(scale);
      } catch (NumberFormatException e) {
        throw new RuntimeException("Error setting numericScale on parameter mapping.  Cause: " + numericScale + " is not a valid integer");
      }
    }

    parameterMappingList.add(mapping);
  }

  public void addParameterMap(String id, String parameterClassName) {
    errorContext.setActivity("building a parameter map");
    parameterMap = new BasicParameterMap(client.getDelegate());

    parameterClassName = typeHandlerFactory.resolveAlias(parameterClassName);

    parameterMap.setId(id);
    parameterMap.setResource(errorContext.getResource());

    errorContext.setObjectId(id + " parameter map");

    Class parameterClass;
    try {
      errorContext.setMoreInfo("Check the parameter class.");
      parameterClass = Resources.classForName(parameterClassName);
    } catch (Exception e) {
      throw new SqlMapException("Error configuring ParameterMap.  Could not set ParameterClass.  Cause: " + e, e);
    }

    parameterMap.setParameterClass(parameterClass);

    parameterMappingList = new ArrayList();

    errorContext.setMoreInfo("Check the parameter mappings.");
  }

  // TODO: pass into addResultMap
  public void addDiscriminator(String callback, String javaType, String jdbcType, String columnName, String nullValue, String columnIndex) {
    callback = typeHandlerFactory.resolveAlias(callback);
    javaType = typeHandlerFactory.resolveAlias(javaType);

    TypeHandler handler;
    if (callback != null) {
      errorContext.setMoreInfo("Check the result mapping typeHandler attribute '" + callback + "' (must be a TypeHandlerCallback implementation).");
      try {
        Object impl = Resources.instantiate(callback);
        if (impl instanceof TypeHandlerCallback) {
          handler = new CustomTypeHandler((TypeHandlerCallback) impl);
        } else if (impl instanceof TypeHandler) {
          handler = (TypeHandler) impl;
        } else {
          throw new RuntimeException("The class '' is not a valid implementation of TypeHandler or TypeHandlerCallback");
        }
      } catch (Exception e) {
        throw new RuntimeException("Error occurred during custom type handler configuration.  Cause: " + e, e);
      }
    } else {
      errorContext.setMoreInfo("Check the result mapping property type or name.");
      handler = resolveTypeHandler(client.getDelegate().getTypeHandlerFactory(), resultMap.getResultClass(), "", javaType, jdbcType, true);
    }

    BasicResultMapping mapping = new BasicResultMapping();
    mapping.setColumnName(columnName);
    mapping.setJdbcTypeName(jdbcType);
    mapping.setTypeHandler(handler);
    mapping.setNullValue(nullValue);

    try {
      if (javaType != null && javaType.length() > 0) {
        mapping.setJavaType(Resources.classForName(javaType));
      }
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Error setting javaType on result mapping.  Cause: " + e);
    }

    if (columnIndex != null && columnIndex.length() > 0) {
      mapping.setColumnIndex(Integer.parseInt(columnIndex));
    }

    discriminator = new Discriminator(delegate, mapping);
  }

  // TODO: pass into addResultMap
  public void addSubMap(String value, String resultMap) {
    if (discriminator == null) {
      throw new RuntimeException("The discriminator is null, but somehow a subMap was reached.  This is a bug.");
    }
    discriminator.addSubMap(value, resultMap);
  }

  // TODO: pass into addResultMap
  public void addResultMapping(String callback, String javaType, String propertyName, String jdbcType, String columnName, String nullValue, String statementName, String resultMapName, String columnIndex) {
    callback = typeHandlerFactory.resolveAlias(callback);
    javaType = typeHandlerFactory.resolveAlias(javaType);

    errorContext.setObjectId(propertyName + " mapping of the " + resultMap.getId() + " result map");

    TypeHandler handler;
    if (callback != null) {
      errorContext.setMoreInfo("Check the result mapping typeHandler attribute '" + callback + "' (must be a TypeHandler or TypeHandlerCallback implementation).");
      try {
        Object impl = Resources.instantiate(callback);
        if (impl instanceof TypeHandlerCallback) {
          handler = new CustomTypeHandler((TypeHandlerCallback) impl);
        } else if (impl instanceof TypeHandler) {
          handler = (TypeHandler) impl;
        } else {
          throw new RuntimeException("The class '" + callback + "' is not a valid implementation of TypeHandler or TypeHandlerCallback");
        }
      } catch (Exception e) {
        throw new RuntimeException("Error occurred during custom type handler configuration.  Cause: " + e, e);
      }
    } else {
      errorContext.setMoreInfo("Check the result mapping property type or name.");
      handler = resolveTypeHandler(client.getDelegate().getTypeHandlerFactory(), resultMap.getResultClass(), propertyName, javaType, jdbcType, true);
    }


    BasicResultMapping mapping = new BasicResultMapping();
    mapping.setPropertyName(propertyName);
    mapping.setColumnName(columnName);
    mapping.setJdbcTypeName(jdbcType);
    mapping.setTypeHandler(handler);
    mapping.setNullValue(nullValue);
    mapping.setStatementName(statementName);
    mapping.setNestedResultMapName(resultMapName);

    if (resultMapName != null && resultMapName.length() > 0) {
      resultMap.addNestedResultMappings(mapping);
    }

    try {
      if (javaType != null && javaType.length() > 0) {
        mapping.setJavaType(Resources.classForName(javaType));
      }
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Error setting javaType on result mapping.  Cause: " + e);
    }

    if (columnIndex != null && columnIndex.length() > 0) {
      mapping.setColumnIndex(Integer.parseInt(columnIndex));
    } else {
      resultMappingIndex++;
      mapping.setColumnIndex(resultMappingIndex);
    }

    resultMappingList.add(mapping);
  }

  public void addResultMap(String id, String resultClassName, String xmlName, String groupBy, String extended) {
    errorContext.setActivity("building a result map");

    resultMap = new BasicResultMap(client.getDelegate());

    resultClassName = typeHandlerFactory.resolveAlias(resultClassName);

    errorContext.setObjectId(id + " result map");

    resultMap.setId(id);
    resultMap.setXmlName(xmlName);
    resultMap.setResource(errorContext.getResource());

    if (groupBy != null && groupBy.length() > 0) {
      StringTokenizer parser = new StringTokenizer(groupBy, ", ", false);
      while (parser.hasMoreTokens()) {
        resultMap.addGroupByProperty(parser.nextToken());
      }
    }

    Class resultClass;
    try {
      errorContext.setMoreInfo("Check the result class.");
      resultClass = Resources.classForName(resultClassName);
    } catch (Exception e) {
      throw new RuntimeException("Error configuring Result.  Could not set ResultClass.  Cause: " + e, e);

    }

    resultMap.setResultClass(resultClass);

    resultMappingList = new ArrayList();

    errorContext.setMoreInfo("Check the extended result map.");
    if (extended != null) {
      BasicResultMap extendedResultMap = (BasicResultMap) client.getDelegate().getResultMap(extended);
      ResultMapping[] resultMappings = extendedResultMap.getResultMappings();
      for (int i = 0; i < resultMappings.length; i++) {
        resultMappingList.add(resultMappings[i]);
      }

      List nestedResultMappings = extendedResultMap.getNestedResultMappings();
      if (nestedResultMappings != null) {
        Iterator iter = nestedResultMappings.iterator();
        while (iter.hasNext()) {
          resultMap.addNestedResultMappings((ResultMapping) iter.next());
        }
      }

      if (groupBy == null || groupBy.length() == 0) {
        if (extendedResultMap.hasGroupBy()) {
          Iterator i = extendedResultMap.groupByProps();
          while (i.hasNext()) {
            resultMap.addGroupByProperty((String) i.next());
          }
        }
      }
    }

    errorContext.setMoreInfo("Check the result mappings.");
    resultMappingIndex = resultMappingList.size();
  }

  // TODO: pass into addResultMap
  public void finalizeResultMap() {
    if (resultMappingList.size() == 0) {
      throw new RuntimeException("resultMap " + resultMap.getId() + " must have at least one result mapping");
    }

    resultMap.setResultMappingList(resultMappingList);

    resultMap.setDiscriminator(discriminator);
    discriminator = null;

    client.getDelegate().addResultMap(resultMap);

    errorContext.setMoreInfo(null);

    errorContext.setObjectId(null);
  }

  //
  // SQL Statement methods
  //

  // TODO:  Clean up method signature
  public MappedStatement prepareGeneralStatement(SqlSource processor, GeneralStatement statement, String id, String resultMapName, String[] additionalResultMapNames, String parameterMapName, String resultSetType, String fetchSize, String parameterClassName, String resultClassName, String[] additionalResultClasses, String allowRemapping, String xmlResultName, String timeout, String cacheModelName) {
    errorContext.setActivity("parsing a mapped statement");
    errorContext.setObjectId(id + " statement");
    errorContext.setMoreInfo("Check the result map name.");
    if (resultMapName != null) {
      statement.setResultMap((BasicResultMap) client.getDelegate().getResultMap(resultMapName));
      if (additionalResultMapNames != null) {
        for (int i = 0; i < additionalResultMapNames.length; i++) {
          statement.addResultMap((BasicResultMap) client.getDelegate().getResultMap(additionalResultMapNames[i]));
        }
      }
    }

    errorContext.setMoreInfo("Check the parameter map name.");

    if (parameterMapName != null) {
      statement.setParameterMap((BasicParameterMap) client.getDelegate().getParameterMap(parameterMapName));
    }

    statement.setId(id);
    statement.setResource(errorContext.getResource());

    if (resultSetType != null) {
      if ("FORWARD_ONLY".equals(resultSetType)) {
        statement.setResultSetType(new Integer(ResultSet.TYPE_FORWARD_ONLY));
      } else if ("SCROLL_INSENSITIVE".equals(resultSetType)) {
        statement.setResultSetType(new Integer(ResultSet.TYPE_SCROLL_INSENSITIVE));
      } else if ("SCROLL_SENSITIVE".equals(resultSetType)) {
        statement.setResultSetType(new Integer(ResultSet.TYPE_SCROLL_SENSITIVE));
      }
    }

    if (fetchSize != null) {
      statement.setFetchSize(new Integer(fetchSize));
    }

    // set parameter class either from attribute or from map (make sure to match)
    ParameterMap parameterMap = statement.getParameterMap();
    if (parameterMap == null) {
      try {
        if (parameterClassName != null) {
          errorContext.setMoreInfo("Check the parameter class.");
          parameterClassName = typeHandlerFactory.resolveAlias(parameterClassName);
          Class parameterClass = Resources.classForName(parameterClassName);
          statement.setParameterClass(parameterClass);
        }
      } catch (ClassNotFoundException e) {
        throw new SqlMapException("Error.  Could not set parameter class.  Cause: " + e, e);
      }
    } else {
      statement.setParameterClass(parameterMap.getParameterClass());
    }

    // process SQL statement, including inline parameter maps
    errorContext.setMoreInfo("Check the SQL statement.");
    Sql sql = processor.getSql();
    setSqlForStatement(statement, sql);

    // set up either null result map or automatic result mapping
    BasicResultMap resultMap = (BasicResultMap) statement.getResultMap();
    if (resultMap == null && resultClassName == null) {
      statement.setResultMap(null);
    } else if (resultMap == null) {
      resultMap = buildAutoResultMap(allowRemapping, statement, resultClassName, xmlResultName);
      statement.setResultMap(resultMap);
      if (additionalResultClasses != null) {
        for (int i = 0; i < additionalResultClasses.length; i++) {
          statement.addResultMap(buildAutoResultMap(allowRemapping, statement, additionalResultClasses[i], xmlResultName));
        }
      }

    }

    statement.setTimeout(defaultStatementTimeout);
    if (timeout != null) {
      try {
        statement.setTimeout(Integer.valueOf(timeout));
      } catch (NumberFormatException e) {
        throw new SqlMapException("Specified timeout value for statement "
            + statement.getId() + " is not a valid integer");
      }
    }

    errorContext.setMoreInfo(null);
    errorContext.setObjectId(null);

    statement.setSqlMapClient(client);
    if (cacheModelName != null && cacheModelName.length() > 0 && client.getDelegate().isCacheModelsEnabled()) {
      CacheModel cacheModel = client.getDelegate().getCacheModel(cacheModelName);
      return new CachingStatement(statement, cacheModel);
    } else {
      return statement;
    }
  }

  // TODO:  Clean up method signature!
  public SelectKeyStatement prepareSelectKeyStatement(SqlSource processor, String resultClassName, String statementId, String keyPropName, boolean runAfterSQL, String type, Class parameterClass) {
    errorContext.setActivity("parsing a select key");

    SelectKeyStatement selectKeyStatement = new SelectKeyStatement();

    resultClassName = typeHandlerFactory.resolveAlias(resultClassName);
    Class resultClass = null;

    // get parameter and result maps
    selectKeyStatement.setSqlMapClient(client);

    selectKeyStatement.setId(statementId + "-SelectKey");
    selectKeyStatement.setResource(errorContext.getResource());
    selectKeyStatement.setKeyProperty(keyPropName);

    selectKeyStatement.setRunAfterSQL(runAfterSQL);
    // process the type (pre or post) attribute
    if (type != null) {
      selectKeyStatement.setRunAfterSQL("post".equals(type));
    }

    try {
      if (resultClassName != null) {
        errorContext.setMoreInfo("Check the select key result class.");
        resultClass = Resources.classForName(resultClassName);
      } else {
        if (keyPropName != null && parameterClass != null) {
          resultClass = PROBE.getPropertyTypeForSetter(parameterClass, selectKeyStatement.getKeyProperty());
        }
      }
    } catch (ClassNotFoundException e) {
      throw new SqlMapException("Error.  Could not set result class.  Cause: " + e, e);
    }

    if (resultClass == null) {
      resultClass = Object.class;
    }

    // process SQL statement, including inline parameter maps
    errorContext.setMoreInfo("Check the select key SQL statement.");
    Sql sql = processor.getSql();
    setSqlForStatement(selectKeyStatement, sql);


    BasicResultMap resultMap;
    resultMap = new AutoResultMap(client.getDelegate(), false);
    resultMap.setId(selectKeyStatement.getId() + "-AutoResultMap");
    resultMap.setResultClass(resultClass);
    resultMap.setResource(selectKeyStatement.getResource());
    selectKeyStatement.setResultMap(resultMap);

    errorContext.setMoreInfo(null);
    return selectKeyStatement;
  }

  private void setSqlForStatement(GeneralStatement statement, Sql sql) {
    if (sql instanceof DynamicSql) {
      statement.setSql(sql);
    } else {
      applyInlineParameterMap(statement, sql.getSql(null, null));
    }
  }

  private void applyInlineParameterMap(GeneralStatement statement, String sqlStatement) {
    String newSql = sqlStatement;

    errorContext.setActivity("building an inline parameter map");

    ParameterMap parameterMap = statement.getParameterMap();

    errorContext.setMoreInfo("Check the inline parameters.");
    if (parameterMap == null) {

      BasicParameterMap map;
      map = new BasicParameterMap(client.getDelegate());

      map.setId(statement.getId() + "-InlineParameterMap");
      map.setParameterClass(statement.getParameterClass());
      map.setResource(statement.getResource());
      statement.setParameterMap(map);

      SqlText sqlText = PARAM_PARSER.parseInlineParameterMap(client.getDelegate().getTypeHandlerFactory(), newSql, statement.getParameterClass());
      newSql = sqlText.getText();
      List mappingList = Arrays.asList(sqlText.getParameterMappings());

      map.setParameterMappingList(mappingList);
    }

    Sql sql;
    if (SimpleDynamicSql.isSimpleDynamicSql(newSql)) {
      sql = new SimpleDynamicSql(client.getDelegate(), newSql);
    } else {
      sql = new StaticSql(newSql);
    }
    statement.setSql(sql);

  }

  private BasicResultMap buildAutoResultMap(String allowRemapping, GeneralStatement statement, String firstResultClass, String xmlResultName) {
    BasicResultMap resultMap;
    resultMap = new AutoResultMap(client.getDelegate(), "true".equals(allowRemapping));
    resultMap.setId(statement.getId() + "-AutoResultMap");
    resultMap.setResultClass(resolveClass(firstResultClass));
    resultMap.setXmlName(xmlResultName);
    resultMap.setResource(statement.getResource());
    return resultMap;
  }

  private Class resolveClass(String resultClassName) {
    try {
      if (resultClassName != null) {
        errorContext.setMoreInfo("Check the result class.");
        return Resources.classForName(typeHandlerFactory.resolveAlias(resultClassName));
      } else {
        return null;
      }
    } catch (ClassNotFoundException e) {
      throw new SqlMapException("Error.  Could not set result class.  Cause: " + e, e);
    }
  }


}
