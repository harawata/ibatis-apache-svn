package com.ibatis.sqlmap.engine.builder.xml;

import com.ibatis.common.resources.Resources;
import com.ibatis.common.xml.*;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.extensions.TypeHandlerCallback;
import com.ibatis.sqlmap.engine.accessplan.AccessPlanFactory;
import com.ibatis.sqlmap.engine.cache.fifo.FifoCacheController;
import com.ibatis.sqlmap.engine.cache.lru.LruCacheController;
import com.ibatis.sqlmap.engine.cache.memory.MemoryCacheController;
import com.ibatis.sqlmap.engine.cache.CacheModel;
import com.ibatis.sqlmap.engine.datasource.*;
import com.ibatis.sqlmap.engine.impl.*;
import com.ibatis.sqlmap.engine.transaction.*;
import com.ibatis.sqlmap.engine.transaction.external.ExternalTransactionConfig;
import com.ibatis.sqlmap.engine.transaction.jdbc.JdbcTransactionConfig;
import com.ibatis.sqlmap.engine.transaction.jta.JtaTransactionConfig;
import com.ibatis.sqlmap.engine.type.*;
import com.ibatis.sqlmap.engine.mapping.statement.MappedStatement;
import org.w3c.dom.Node;

import java.io.Reader;
import java.util.*;

public class SqlMapConfigParser extends BaseParser {

  private final NodeletParser parser = new NodeletParser();

  public SqlMapConfigParser() {
    super (new Variables());
    parser.setValidation(true);
    parser.setEntityResolver(new SqlMapClasspathEntityResolver());

    vars.delegate = new SqlMapExecutorDelegate();
    vars.typeHandlerFactory = vars.delegate.getTypeHandlerFactory();
    vars.client = new SqlMapClientImpl(vars.delegate);

    addDefaultTypeAliases();

    addSqlMapConfigNodelets();
    addGlobalPropNodelets();
    addSettingsNodelets();
    addTypeAliasNodelets();
    addTypeHandlerNodelets();
    addTransactionManagerNodelets();
    addSqlMapNodelets();

  }

  public SqlMapClient parse(Reader reader, Properties props) {
    try {
      vars.properties = props;
      parser.parse(reader);
      return vars.client;
    } catch (Exception e) {
      throw new RuntimeException("Error occurred.  Cause: " + e, e);
    }
  }

  public SqlMapClient parse(Reader reader) {
    try {
      parser.parse(reader);
      return vars.client;
    } catch (Exception e) {
      throw new RuntimeException("Error occurred.  Cause: " + e, e);
    }
  }

  private void addSqlMapConfigNodelets() {
    parser.addNodelet("/sqlMapConfig/end()", new Nodelet() {
      public void process(Node node) throws Exception {
        Iterator cacheNames = vars.delegate.getCacheModelNames();

        while (cacheNames.hasNext()) {
          String cacheName = (String) cacheNames.next();
          CacheModel cacheModel = vars.delegate.getCacheModel(cacheName);
          Iterator statementNames = cacheModel.getFlushTriggerStatementNames();
          while (statementNames.hasNext()) {
            String statementName = (String) statementNames.next();
            MappedStatement statement = vars.delegate.getMappedStatement(statementName);
            if (statement != null) {
              statement.addExecuteListener(cacheModel);
            } else {
              throw new RuntimeException("Could not find statement named '" + statementName + "' for use as a flush trigger for the cache model named '" + cacheName + "'.");
            }
          }
        }
      }
    });
  }

  private void addGlobalPropNodelets() {
    parser.addNodelet("/sqlMapConfig/properties", new Nodelet() {
      public void process(Node node) throws Exception {
        Properties attributes = NodeletUtils.parseAttributes(node);
        String resource = attributes.getProperty("resource");
        String url = attributes.getProperty("url");

        Properties props = null;
        if (resource != null) {
          props = Resources.getResourceAsProperties(resource);
        } else if (url != null) {
          props = Resources.getUrlAsProperties(resource);
        } else {
          throw new RuntimeException("The properties element requires either a resource or a url attribute.");
        }

        if (vars.properties == null) {
          vars.properties = props;
        } else {
          props.putAll(vars.properties);
          vars.properties = props;
        }
      }
    });
  }

  private void addSettingsNodelets() {
    parser.addNodelet("/sqlMapConfig/settings", new Nodelet() {
      public void process(Node node) throws Exception {
        Properties attributes = NodeletUtils.parseAttributes(node, vars.properties);
        vars.useStatementNamespaces = NodeletUtils.getBooleanAttribute(attributes, "useStatementNamespaces", false);
        vars.delegate.setCacheModelsEnabled(NodeletUtils.getBooleanAttribute(attributes, "cacheModelsEnabled", false));
        vars.delegate.setEnhancementEnabled(NodeletUtils.getBooleanAttribute(attributes, "enhancementEnabled", false));
        vars.delegate.setLazyLoadingEnabled(NodeletUtils.getBooleanAttribute(attributes, "lazyLoadingEnabled", false));
        vars.delegate.setMaxRequests(NodeletUtils.getIntAttribute(attributes, "maxRequests", 1024));
        vars.delegate.setMaxSessions(NodeletUtils.getIntAttribute(attributes, "maxSessions", 256));
        vars.delegate.setMaxTransactions(NodeletUtils.getIntAttribute(attributes, "maxTransactions", 64));

        if (vars.delegate.isEnhancementEnabled()) {
          try {
            vars.delegate.setEnhancementEnabled(Class.forName("net.sf.cglib.proxy.InvocationHandler") != null);
          } catch (ClassNotFoundException e) {
            vars.delegate.setEnhancementEnabled(false);
          }
        }
        

        AccessPlanFactory.setBytecodeEnhancementEnabled(vars.delegate.isEnhancementEnabled());
      }
    });
  }

  private void addTypeAliasNodelets() {
    parser.addNodelet("/sqlMapConfig/typeAlias", new Nodelet() {
      public void process(Node node) throws Exception {
        Properties attributes = NodeletUtils.parseAttributes(node, vars.properties);
        String alias = attributes.getProperty("alias");
        String type = attributes.getProperty("type");
        vars.typeHandlerFactory.putTypeAlias(alias, type);
      }
    });
  }

  private void addTypeHandlerNodelets() {
    parser.addNodelet("/sqlMapConfig/typeHandler", new Nodelet() {
      public void process(Node node) throws Exception {
        Properties attributes = NodeletUtils.parseAttributes(node, vars.properties);
        String javaType = attributes.getProperty("javaType");
        String jdbcType = attributes.getProperty("jdbcType");
        String callback = attributes.getProperty("callback");
        callback = vars.typeHandlerFactory.resolveAlias(callback);
        javaType = vars.typeHandlerFactory.resolveAlias(javaType);

        TypeHandlerCallback typeHandlerCallback = (TypeHandlerCallback) Resources.classForName(callback).newInstance();
        TypeHandler typeHandler = new CustomTypeHandler(typeHandlerCallback);

        if (jdbcType != null && jdbcType.length() > 0) {
          vars.typeHandlerFactory.register(Resources.classForName(javaType), jdbcType, typeHandler);
        } else {
          vars.typeHandlerFactory.register(Resources.classForName(javaType), typeHandler);
        }
      }
    });
  }

  private void addTransactionManagerNodelets() {
    parser.addNodelet("/sqlMapConfig/transactionManager/end()", new Nodelet() {
      public void process(Node node) throws Exception {
        Properties attributes = NodeletUtils.parseAttributes(node, vars.properties);
        String type = attributes.getProperty("type");
        boolean commitRequired = NodeletUtils.getBooleanAttribute(attributes, "commitRequired", false);
        type = vars.typeHandlerFactory.resolveAlias(type);

        TransactionManager txManager = null;

        TransactionConfig config1 = (TransactionConfig) Resources.instantiate(type);
        config1.setDataSource(vars.dataSource);
        config1.setMaximumConcurrentTransactions(vars.delegate.getMaxTransactions());

        config1.initialize(vars.txProps);

        txManager = new TransactionManager(config1);
        txManager.setForceCommit(commitRequired);
        vars.delegate.setTxManager(txManager);
      }
    });
    parser.addNodelet("/sqlMapConfig/transactionManager/property", new Nodelet() {
      public void process(Node node) throws Exception {
        Properties attributes = NodeletUtils.parseAttributes(node, vars.properties);
        String name = attributes.getProperty("name");
        String value = NodeletUtils.parsePropertyTokens(attributes.getProperty("value"),vars.properties);
        vars.txProps.setProperty(name, value);
      }
    });
    parser.addNodelet("/sqlMapConfig/transactionManager/dataSource/end()", new Nodelet() {
      public void process(Node node) throws Exception {
        Properties attributes = NodeletUtils.parseAttributes(node, vars.properties);
        String type = attributes.getProperty("type");
        type = vars.typeHandlerFactory.resolveAlias(type);

        DataSourceFactory dsFactory = (DataSourceFactory) Resources.instantiate(type);
        dsFactory.initialize(vars.dsProps);
        vars.dataSource = dsFactory.getDataSource();
      }
    });
    parser.addNodelet("/sqlMapConfig/transactionManager/dataSource/property", new Nodelet() {
      public void process(Node node) throws Exception {
        Properties attributes = NodeletUtils.parseAttributes(node, vars.properties);
        String name = attributes.getProperty("name");
        String value = NodeletUtils.parsePropertyTokens(attributes.getProperty("value"),vars.properties);
        vars.dsProps.setProperty(name, value);
      }
    });
  }

  private void addSqlMapNodelets() {
    parser.addNodelet("/sqlMapConfig/sqlMap", new Nodelet() {
      public void process(Node node) throws Exception {
        Properties attributes = NodeletUtils.parseAttributes(node, vars.properties);
        String resource = attributes.getProperty("resource");
        String url = attributes.getProperty("url");

        Reader reader;
        if (resource != null) {
          vars.currentResource = resource;
          reader = Resources.getResourceAsReader(resource);
        } else if (url != null) {
          vars.currentResource = url;
          reader = Resources.getUrlAsReader(resource);
        } else {
          throw new RuntimeException("The sqlMap element requires either a resource or a url attribute.");
        }

        new SqlMapParser(vars).parse(reader);

      }
    });
  }

  private void addDefaultTypeAliases() {
    // TRANSACTION ALIASES
    vars.typeHandlerFactory.putTypeAlias("JDBC", JdbcTransactionConfig.class.getName());
    vars.typeHandlerFactory.putTypeAlias("JTA", JtaTransactionConfig.class.getName());
    vars.typeHandlerFactory.putTypeAlias("EXTERNAL", ExternalTransactionConfig.class.getName());

    // DATA SOURCE ALIASES
    vars.typeHandlerFactory.putTypeAlias("SIMPLE", SimpleDataSourceFactory.class.getName());
    vars.typeHandlerFactory.putTypeAlias("DBCP", DbcpDataSourceFactory.class.getName());
    vars.typeHandlerFactory.putTypeAlias("JNDI", JndiDataSourceFactory.class.getName());

    // CACHE ALIASES
    vars.typeHandlerFactory.putTypeAlias("FIFO", FifoCacheController.class.getName());
    vars.typeHandlerFactory.putTypeAlias("LRU", LruCacheController.class.getName());
    vars.typeHandlerFactory.putTypeAlias("MEMORY", MemoryCacheController.class.getName());
    // -- use a string for OSCache to avoid uneccessary loading of properties upon init
    vars.typeHandlerFactory.putTypeAlias("OSCACHE", "com.ibatis.sqlmap.engine.cache.oscache.OSCacheController");

    // TYPE ALIASEs
    vars.typeHandlerFactory.putTypeAlias("dom", DomTypeMarker.class.getName());
    vars.typeHandlerFactory.putTypeAlias("domCollection", DomCollectionTypeMarker.class.getName());
    vars.typeHandlerFactory.putTypeAlias("xml", XmlTypeMarker.class.getName());
    vars.typeHandlerFactory.putTypeAlias("xmlCollection", XmlCollectionTypeMarker.class.getName());
  }


}
