package com.ibatis.sqlmap.engine.builder.xml;

import com.ibatis.common.beans.ClassInfo;
import com.ibatis.common.resources.Resources;
import com.ibatis.common.xml.Nodelet;
import com.ibatis.common.xml.NodeletParser;
import com.ibatis.common.xml.NodeletUtils;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapException;
import com.ibatis.sqlmap.client.extensions.TypeHandlerCallback;
import com.ibatis.sqlmap.engine.accessplan.AccessPlanFactory;
import com.ibatis.sqlmap.engine.cache.CacheModel;
import com.ibatis.sqlmap.engine.datasource.DataSourceFactory;
import com.ibatis.sqlmap.engine.mapping.result.ResultObjectFactory;
import com.ibatis.sqlmap.engine.mapping.statement.MappedStatement;
import com.ibatis.sqlmap.engine.transaction.TransactionConfig;
import com.ibatis.sqlmap.engine.transaction.TransactionManager;
import com.ibatis.sqlmap.engine.type.CustomTypeHandler;
import com.ibatis.sqlmap.engine.type.TypeHandler;
import com.ibatis.sqlmap.engine.type.TypeHandlerFactory;
import org.w3c.dom.Node;

import java.io.InputStream;
import java.io.Reader;
import java.util.Iterator;
import java.util.Properties;

public class SqlMapConfigParser {

  protected final NodeletParser parser = new NodeletParser();
  private ParserState state = new ParserState();

  private boolean usingStreams = false;

  public SqlMapConfigParser() {
    parser.setValidation(true);
    parser.setEntityResolver(new SqlMapClasspathEntityResolver());

    addSqlMapConfigNodelets();
    addGlobalPropNodelets();
    addSettingsNodelets();
    addTypeAliasNodelets();
    addTypeHandlerNodelets();
    addTransactionManagerNodelets();
    addSqlMapNodelets();
    addResultObjectFactoryNodelets();

  }

  public SqlMapClient parse(Reader reader, Properties props) {
    state.globalProps = props;
    return parse(reader);
  }

  public SqlMapClient parse(Reader reader) {
    try {
      usingStreams = false;
      
      parser.parse(reader);
      return state.client;
    } catch (Exception e) {
      throw new RuntimeException("Error occurred.  Cause: " + e, e);
    }
  }

  public SqlMapClient parse(InputStream inputStream, Properties props) {
    state.globalProps = props;
    return parse(inputStream);
  }

  public SqlMapClient parse(InputStream inputStream) {
    try {
      usingStreams = true;

      parser.parse(inputStream);
      return state.client;
    } catch (Exception e) {
      throw new RuntimeException("Error occurred.  Cause: " + e, e);
    }
  }

  private void addSqlMapConfigNodelets() {
    parser.addNodelet("/sqlMapConfig/end()", new Nodelet() {
      public void process(Node node) throws Exception {
        Iterator cacheNames = state.client.getDelegate().getCacheModelNames();

        while (cacheNames.hasNext()) {
          String cacheName = (String) cacheNames.next();
          CacheModel cacheModel = state.client.getDelegate().getCacheModel(cacheName);
          Iterator statementNames = cacheModel.getFlushTriggerStatementNames();
          while (statementNames.hasNext()) {
            String statementName = (String) statementNames.next();
            MappedStatement statement = state.client.getDelegate().getMappedStatement(statementName);
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
        state.errorContext.setActivity("loading global properties");

        Properties attributes = NodeletUtils.parseAttributes(node, state.globalProps);
        String resource = attributes.getProperty("resource");
        String url = attributes.getProperty("url");

        try {
          Properties props = null;
          if (resource != null) {
            state.errorContext.setResource(resource);
            props = Resources.getResourceAsProperties(resource);
          } else if (url != null) {
            state.errorContext.setResource(url);
            props = Resources.getUrlAsProperties(url);
          } else {
            throw new RuntimeException("The " + "properties" + " element requires either a resource or a url attribute.");
          }

          if (state.globalProps == null) {
            state.globalProps = props;
          } else {
            props.putAll(state.globalProps);
            state.globalProps = props;
          }
        } catch (Exception e) {
          throw new RuntimeException("Error loading properties.  Cause: " + e);
        }
      }
    });
  }

  private void addSettingsNodelets() {
    parser.addNodelet("/sqlMapConfig/settings", new Nodelet() {
      public void process(Node node) throws Exception {
        state.errorContext.setActivity("loading settings properties");

        Properties attributes = NodeletUtils.parseAttributes(node, state.globalProps);

        String classInfoCacheEnabledAttr = attributes.getProperty("classInfoCacheEnabled");
        boolean classInfoCacheEnabled = (classInfoCacheEnabledAttr == null || "true".equals(classInfoCacheEnabledAttr));
        ClassInfo.setCacheEnabled(classInfoCacheEnabled);

        String lazyLoadingEnabledAttr = attributes.getProperty("lazyLoadingEnabled");
        boolean lazyLoadingEnabled = (lazyLoadingEnabledAttr == null || "true".equals(lazyLoadingEnabledAttr));
        state.client.getDelegate().setLazyLoadingEnabled(lazyLoadingEnabled);

        String statementCachingEnabledAttr = attributes.getProperty("statementCachingEnabled");
        boolean statementCachingEnabled = (statementCachingEnabledAttr == null || "true".equals(statementCachingEnabledAttr));
        state.client.getDelegate().setStatementCacheEnabled(statementCachingEnabled);

        String cacheModelsEnabledAttr = attributes.getProperty("cacheModelsEnabled");
        boolean cacheModelsEnabled = (cacheModelsEnabledAttr == null || "true".equals(cacheModelsEnabledAttr));
        state.client.getDelegate().setCacheModelsEnabled(cacheModelsEnabled);

        String enhancementEnabledAttr = attributes.getProperty("enhancementEnabled");
        boolean enhancementEnabled = (enhancementEnabledAttr == null || "true".equals(enhancementEnabledAttr));
        try {
          enhancementEnabled = enhancementEnabled && Resources.classForName("net.sf.cglib.proxy.InvocationHandler") != null;
        } catch (ClassNotFoundException e) {
          enhancementEnabled = false;
        }
        state.client.getDelegate().setEnhancementEnabled(enhancementEnabled);

        String useStatementNamespacesAttr = attributes.getProperty("useStatementNamespaces");
        state.useStatementNamespaces = ("true".equals(useStatementNamespacesAttr));

        String maxTransactions = attributes.getProperty("maxTransactions");
        if (maxTransactions != null && Integer.parseInt(maxTransactions) > 0) {
          state.client.getDelegate().setMaxTransactions(Integer.parseInt(maxTransactions));
        }

        String maxRequests = attributes.getProperty("maxRequests");
        if (maxRequests != null && Integer.parseInt(maxRequests) > 0) {
          state.client.getDelegate().setMaxRequests(Integer.parseInt(maxRequests));
        }

        String maxSessions = attributes.getProperty("maxSessions");
        if (maxSessions != null && Integer.parseInt(maxSessions) > 0) {
          state.client.getDelegate().setMaxSessions(Integer.parseInt(maxSessions));
        }

        AccessPlanFactory.setBytecodeEnhancementEnabled(state.client.getDelegate().isEnhancementEnabled());
        
        String defaultStatementTimeout = attributes.getProperty("defaultStatementTimeout");
        if (defaultStatementTimeout != null) {
          try {
            Integer defaultTimeout = Integer.valueOf(defaultStatementTimeout);
            state.defaultStatementTimeout = defaultTimeout;
          } catch (NumberFormatException e) {
            throw new SqlMapException("Specified defaultStatementTimeout is not a valid integer");
          }
        }
      }
    });
  }

  private void addTypeAliasNodelets() {
    parser.addNodelet("/sqlMapConfig/typeAlias", new Nodelet() {
      public void process(Node node) throws Exception {
        Properties prop = NodeletUtils.parseAttributes(node, state.globalProps);
        String alias = prop.getProperty("alias");
        String type = prop.getProperty("type");
        state.typeHandlerFactory.putTypeAlias(alias, type);
      }
    });
  }

  private void addTypeHandlerNodelets() {
    parser.addNodelet("/sqlMapConfig/typeHandler", new Nodelet() {
      public void process(Node node) throws Exception {
        state.errorContext.setActivity("building a building custom type handler");
        try {
          TypeHandlerFactory typeHandlerFactory = state.client.getDelegate().getTypeHandlerFactory();

          Properties prop = NodeletUtils.parseAttributes(node, state.globalProps);

          String jdbcType = prop.getProperty("jdbcType");
          String javaType = prop.getProperty("javaType");
          String callback = prop.getProperty("callback");
          callback = typeHandlerFactory.resolveAlias(callback);
          javaType = typeHandlerFactory.resolveAlias(javaType);

          state.errorContext.setMoreInfo("Check the callback attribute '" + callback + "' (must be a classname).");

          TypeHandler typeHandler;
          Object impl = Resources.instantiate(callback);
          if (impl instanceof TypeHandlerCallback) {
            typeHandler = new CustomTypeHandler((TypeHandlerCallback) impl);
          } else if (impl instanceof TypeHandler) {
            typeHandler = (TypeHandler) impl;
          } else {
            throw new RuntimeException ("The class '' is not a valid implementation of TypeHandler or TypeHandlerCallback");
          }

          state.errorContext.setMoreInfo("Check the javaType attribute '" + javaType + "' (must be a classname) or the jdbcType '" + jdbcType + "' (must be a JDBC type name).");
          if (jdbcType != null && jdbcType.length() > 0) {
            typeHandlerFactory.register(Resources.classForName(javaType), jdbcType, typeHandler);
          } else {
            typeHandlerFactory.register(Resources.classForName(javaType), typeHandler);
          }
        } catch (Exception e) {
          throw new SqlMapException("Error registering occurred.  Cause: " + e, e);
        }
        state.errorContext.setMoreInfo(null);
        state.errorContext.setObjectId(null);
      }
    });
  }

  private void addTransactionManagerNodelets() {
    parser.addNodelet("/sqlMapConfig/transactionManager/end()", new Nodelet() {
      public void process(Node node) throws Exception {
        state.errorContext.setActivity("configuring the transaction manager");

        Properties attributes = NodeletUtils.parseAttributes(node, state.globalProps);


        String type = attributes.getProperty("type");
        type = state.typeHandlerFactory.resolveAlias(type);

        TransactionManager txManager = null;
        try {
          state.errorContext.setMoreInfo("Check the transaction manager type or class.");
          TransactionConfig config = (TransactionConfig) Resources.instantiate(type);
          config.setDataSource(state.dataSource);
          config.setMaximumConcurrentTransactions(state.client.getDelegate().getMaxTransactions());
          state.errorContext.setMoreInfo("Check the transactio nmanager properties or configuration.");
          config.initialize(state.txProps);
          state.errorContext.setMoreInfo(null);
          txManager = new TransactionManager(config);
          txManager.setForceCommit("true".equals(attributes.getProperty("commitRequired")));
        } catch (Exception e) {
          if (e instanceof SqlMapException) {
            throw (SqlMapException) e;
          } else {
            throw new SqlMapException("Error initializing TransactionManager.  Could not instantiate TransactionConfig.  Cause: " + e, e);
          }
        }

        state.client.getDelegate().setTxManager(txManager);
      }
    });
    parser.addNodelet("/sqlMapConfig/transactionManager/property", new Nodelet() {
      public void process(Node node) throws Exception {
        Properties attributes = NodeletUtils.parseAttributes(node, state.globalProps);
        String name = attributes.getProperty("name");
        String value = NodeletUtils.parsePropertyTokens(attributes.getProperty("value"), state.globalProps);
        state.txProps.setProperty(name, value);
      }
    });
    parser.addNodelet("/sqlMapConfig/transactionManager/dataSource", new Nodelet() {
      public void process(Node node) throws Exception {
        state.dsProps = new Properties();
      }
    });
    parser.addNodelet("/sqlMapConfig/transactionManager/dataSource/end()", new Nodelet() {
      public void process(Node node) throws Exception {
        state.errorContext.setActivity("configuring the data source");

        Properties attributes = NodeletUtils.parseAttributes(node, state.globalProps);

        String type = attributes.getProperty("type");
        type = state.typeHandlerFactory.resolveAlias(type);

        try {
          state.errorContext.setMoreInfo("Check the data source type or class.");
          DataSourceFactory dsFactory = (DataSourceFactory) Resources.instantiate(type);
          state.errorContext.setMoreInfo("Check the data source properties or configuration.");
          dsFactory.initialize(state.dsProps);
          state.dataSource = dsFactory.getDataSource();
          state.errorContext.setMoreInfo(null);
        } catch (Exception e) {
          if (e instanceof SqlMapException) {
            throw (SqlMapException) e;
          } else {
            throw new SqlMapException("Error initializing DataSource.  Could not instantiate DataSourceFactory.  Cause: " + e, e);
          }
        }
      }
    });
    parser.addNodelet("/sqlMapConfig/transactionManager/dataSource/property", new Nodelet() {
      public void process(Node node) throws Exception {
        Properties attributes = NodeletUtils.parseAttributes(node, state.globalProps);
        String name = attributes.getProperty("name");
        String value = NodeletUtils.parsePropertyTokens(attributes.getProperty("value"), state.globalProps);
        state.dsProps.setProperty(name, value);
      }
    });
  }

  protected void addSqlMapNodelets() {
    parser.addNodelet("/sqlMapConfig/sqlMap", new Nodelet() {
      public void process(Node node) throws Exception {
        state.errorContext.setActivity("loading the SQL Map resource");

        Properties attributes = NodeletUtils.parseAttributes(node, state.globalProps);

        String resource = attributes.getProperty("resource");
        String url = attributes.getProperty("url");

        if (usingStreams) {
          InputStream inputStream = null;
          if (resource != null) {
            state.errorContext.setResource(resource);
            inputStream = Resources.getResourceAsStream(resource);
          } else if (url != null) {
            state.errorContext.setResource(url);
            inputStream = Resources.getUrlAsStream(url);
          } else {
            throw new SqlMapException("The <sqlMap> element requires either a resource or a url attribute.");
          }

          new SqlMapParser(state).parse(inputStream);
        } else {
          Reader reader = null;
          if (resource != null) {
            state.errorContext.setResource(resource);
            reader = Resources.getResourceAsReader(resource);
          } else if (url != null) {
            state.errorContext.setResource(url);
            reader = Resources.getUrlAsReader(url);
          } else {
            throw new SqlMapException("The <sqlMap> element requires either a resource or a url attribute.");
          }

          new SqlMapParser(state).parse(reader);
        }
      }
    });
  }

  private void addResultObjectFactoryNodelets() {
    parser.addNodelet("/sqlMapConfig/resultObjectFactory", new Nodelet() {
      public void process(Node node) throws Exception {
        state.errorContext.setActivity("configuring the Result Object Factory");

        Properties attributes = NodeletUtils.parseAttributes(node, state.globalProps);

        String type = attributes.getProperty("type");

        ResultObjectFactory rof;
        try {
          rof = (ResultObjectFactory) Resources.instantiate(type);
          state.delegate.setResultObjectFactory(rof);
        } catch (Exception e) {
          throw new SqlMapException("Error instantiating resultObjectFactory: " + type, e);
        }
      }
    });
    parser.addNodelet("/sqlMapConfig/resultObjectFactory/property", new Nodelet() {
      public void process(Node node) throws Exception {
        Properties attributes = NodeletUtils.parseAttributes(node, state.globalProps);
        String name = attributes.getProperty("name");
        String value = NodeletUtils.parsePropertyTokens(attributes.getProperty("value"), state.globalProps);
        state.delegate.getResultObjectFactory().setProperty(name, value);
      }
    });
  }
  
}
