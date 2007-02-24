package com.ibatis.sqlmap.engine.builder.xml;

import com.ibatis.common.beans.Probe;
import com.ibatis.common.beans.ProbeFactory;
import com.ibatis.common.resources.Resources;
import com.ibatis.sqlmap.engine.cache.CacheModel;
import com.ibatis.sqlmap.engine.cache.fifo.FifoCacheController;
import com.ibatis.sqlmap.engine.cache.lru.LruCacheController;
import com.ibatis.sqlmap.engine.cache.memory.MemoryCacheController;
import com.ibatis.sqlmap.engine.datasource.DbcpDataSourceFactory;
import com.ibatis.sqlmap.engine.datasource.JndiDataSourceFactory;
import com.ibatis.sqlmap.engine.datasource.SimpleDataSourceFactory;
import com.ibatis.sqlmap.engine.impl.ExtendedSqlMapClient;
import com.ibatis.sqlmap.engine.impl.SqlMapExecutorDelegate;
import com.ibatis.sqlmap.engine.impl.SqlMapClientImpl;
import com.ibatis.sqlmap.engine.mapping.parameter.BasicParameterMap;
import com.ibatis.sqlmap.engine.mapping.result.BasicResultMap;
import com.ibatis.sqlmap.engine.mapping.result.Discriminator;
import com.ibatis.sqlmap.engine.mapping.statement.MappedStatement;
import com.ibatis.sqlmap.engine.scope.ErrorContext;
import com.ibatis.sqlmap.engine.transaction.external.ExternalTransactionConfig;
import com.ibatis.sqlmap.engine.transaction.jdbc.JdbcTransactionConfig;
import com.ibatis.sqlmap.engine.transaction.jta.JtaTransactionConfig;
import com.ibatis.sqlmap.engine.type.*;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Variables the parser uses.  This "struct" like class is necessary because
 * that's what it is.  It's package scope to help protect the public fields from abuse.
 *
 * I'm favouring pragmatism in this case rather than OO dogma.
 *
 * The public methods are general utility methods that don't modify the state, but may
 * use the state.  They are tolerant of any and all values.
 */
class ParserState {

  private static final Probe PROBE = ProbeFactory.getProbe();

  public ErrorContext errorContext = new ErrorContext();
  public String resource = "SQL Map Config XML File";

  public ExtendedSqlMapClient client;
  public SqlMapExecutorDelegate delegate;
  public TypeHandlerFactory typeHandlerFactory;

  //
  // SQL Map Config
  //
  
  public Properties globalProps;
  public Properties txProps = new Properties();
  public Properties dsProps = new Properties();
  public boolean useStatementNamespaces;
  public Integer defaultStatementTimeout;
  public DataSource dataSource;

  //
  // SQL Map State
  //

  public String namespace;

  public CacheModel cacheModel;
  public Properties cacheProps;

  public BasicResultMap resultMap;
  public List resultMappingList;
  public int resultMappingIndex;
  public Discriminator discriminator;

  public BasicParameterMap parameterMap;
  public List parameterMappingList;

  public Map sqlIncludes = new HashMap();
  public MappedStatement currentStatement;

  public ParserState() {
    delegate = new SqlMapExecutorDelegate();
    typeHandlerFactory = delegate.getTypeHandlerFactory();
    client = new SqlMapClientImpl(delegate);
    useStatementNamespaces = false; 
    registerDefaultTypeAliases();
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

  public String applyNamespace(String id) {
    String newId = id;
    if (namespace != null && namespace.length() > 0 && id != null && id.indexOf('.') < 0) {
      newId = namespace + "." + id;
    }
    return newId;
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
