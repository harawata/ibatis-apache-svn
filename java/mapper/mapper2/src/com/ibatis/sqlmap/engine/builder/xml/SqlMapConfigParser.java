package com.ibatis.sqlmap.engine.builder.xml;

import com.ibatis.common.resources.Resources;
import com.ibatis.common.xml.Nodelet;
import com.ibatis.common.xml.NodeletParser;
import com.ibatis.common.xml.NodeletUtils;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapException;
import com.ibatis.sqlmap.engine.conifg.SqlMapConfiguration;
import org.w3c.dom.Node;

import java.io.InputStream;
import java.io.Reader;
import java.util.Properties;

public class SqlMapConfigParser {

  protected final NodeletParser parser = new NodeletParser();
  private SqlMapConfiguration config = new SqlMapConfiguration();

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
    if (props != null) config.globalProps = props;
    return parse(reader);
  }

  public SqlMapClient parse(Reader reader) {
    try {
      usingStreams = false;

      parser.parse(reader);
      return config.getClient();
    } catch (Exception e) {
      throw new RuntimeException("Error occurred.  Cause: " + e, e);
    }
  }

  public SqlMapClient parse(InputStream inputStream, Properties props) {
    if (props != null) config.globalProps = props;
    return parse(inputStream);
  }

  public SqlMapClient parse(InputStream inputStream) {
    try {
      usingStreams = true;

      parser.parse(inputStream);
      return config.getClient();
    } catch (Exception e) {
      throw new RuntimeException("Error occurred.  Cause: " + e, e);
    }
  }

  private void addSqlMapConfigNodelets() {
    parser.addNodelet("/sqlMapConfig/end()", new Nodelet() {
      public void process(Node node) throws Exception {
        config.wireupCacheModels();
      }
    });
  }

  private void addGlobalPropNodelets() {
    parser.addNodelet("/sqlMapConfig/properties", new Nodelet() {
      public void process(Node node) throws Exception {
        Properties attributes = NodeletUtils.parseAttributes(node, config.globalProps);
        String resource = attributes.getProperty("resource");
        String url = attributes.getProperty("url");
        config.setGlobalProperties(resource, url);
      }
    });
  }

  private void addSettingsNodelets() {
    parser.addNodelet("/sqlMapConfig/settings", new Nodelet() {
      public void process(Node node) throws Exception {
        Properties attributes = NodeletUtils.parseAttributes(node, config.globalProps);

        String classInfoCacheEnabledAttr = attributes.getProperty("classInfoCacheEnabled");
        boolean classInfoCacheEnabled = (classInfoCacheEnabledAttr == null || "true".equals(classInfoCacheEnabledAttr));

        String lazyLoadingEnabledAttr = attributes.getProperty("lazyLoadingEnabled");
        boolean lazyLoadingEnabled = (lazyLoadingEnabledAttr == null || "true".equals(lazyLoadingEnabledAttr));

        String statementCachingEnabledAttr = attributes.getProperty("statementCachingEnabled");
        boolean statementCachingEnabled = (statementCachingEnabledAttr == null || "true".equals(statementCachingEnabledAttr));

        String cacheModelsEnabledAttr = attributes.getProperty("cacheModelsEnabled");
        boolean cacheModelsEnabled = (cacheModelsEnabledAttr == null || "true".equals(cacheModelsEnabledAttr));

        String enhancementEnabledAttr = attributes.getProperty("enhancementEnabled");
        boolean enhancementEnabled = (enhancementEnabledAttr == null || "true".equals(enhancementEnabledAttr));


        String useStatementNamespacesAttr = attributes.getProperty("useStatementNamespaces");
        boolean useStatementNamespaces = "true".equals(useStatementNamespacesAttr);

        String maxTransactionsAttr = attributes.getProperty("maxTransactions");
        Integer maxTransactions = maxTransactionsAttr == null ? null : new Integer((maxTransactionsAttr));


        String maxRequestsAttr = attributes.getProperty("maxRequests");
        Integer maxRequests = maxRequestsAttr == null ? null : new Integer(maxRequestsAttr);

        String maxSessionsAttr = attributes.getProperty("maxSessions");
        Integer maxSessions = maxSessionsAttr == null ? null : new Integer(maxSessionsAttr);

        String defaultTimeoutAttr = attributes.getProperty("defaultStatementTimeout");
        Integer defaultTimeout = defaultTimeoutAttr == null ? null : Integer.valueOf(defaultTimeoutAttr);

        config.setSettings(classInfoCacheEnabled, lazyLoadingEnabled, statementCachingEnabled, cacheModelsEnabled, enhancementEnabled, useStatementNamespaces, maxTransactions, maxRequests, maxSessions, defaultTimeout);
      }
    });
  }

  private void addTypeAliasNodelets() {
    parser.addNodelet("/sqlMapConfig/typeAlias", new Nodelet() {
      public void process(Node node) throws Exception {
        Properties prop = NodeletUtils.parseAttributes(node, config.globalProps);
        String alias = prop.getProperty("alias");
        String type = prop.getProperty("type");
        config.addTypeAlias(alias, type);
      }
    });
  }

  private void addTypeHandlerNodelets() {
    parser.addNodelet("/sqlMapConfig/typeHandler", new Nodelet() {
      public void process(Node node) throws Exception {
        Properties prop = NodeletUtils.parseAttributes(node, config.globalProps);
        String jdbcType = prop.getProperty("jdbcType");
        String javaType = prop.getProperty("javaType");
        String callback = prop.getProperty("callback");
        config.addGlobalTypeHandler(javaType, jdbcType, callback);
      }
    });
  }

  private void addTransactionManagerNodelets() {
    parser.addNodelet("/sqlMapConfig/transactionManager/property", new Nodelet() {
      public void process(Node node) throws Exception {
        Properties attributes = NodeletUtils.parseAttributes(node, config.globalProps);
        String name = attributes.getProperty("name");
        String value = NodeletUtils.parsePropertyTokens(attributes.getProperty("value"), config.globalProps);
        config.txProps.setProperty(name, value);
      }
    });
    parser.addNodelet("/sqlMapConfig/transactionManager/end()", new Nodelet() {
      public void process(Node node) throws Exception {
        Properties attributes = NodeletUtils.parseAttributes(node, config.globalProps);
        String type = attributes.getProperty("type");
        boolean commitRequired = "true".equals(attributes.getProperty("commitRequired"));
        config.setTransactionManager(type, commitRequired, config.txProps);
      }
    });
    parser.addNodelet("/sqlMapConfig/transactionManager/dataSource/property", new Nodelet() {
      public void process(Node node) throws Exception {
        Properties attributes = NodeletUtils.parseAttributes(node, config.globalProps);
        String name = attributes.getProperty("name");
        String value = NodeletUtils.parsePropertyTokens(attributes.getProperty("value"), config.globalProps);
        config.dsProps.setProperty(name, value);
      }
    });
    parser.addNodelet("/sqlMapConfig/transactionManager/dataSource/end()", new Nodelet() {
      public void process(Node node) throws Exception {
        config.getErrorContext().setActivity("configuring the data source");

        Properties attributes = NodeletUtils.parseAttributes(node, config.globalProps);

        String type = attributes.getProperty("type");
        Properties props = config.dsProps;

        config.setDataSource(type, props);
      }
    });
  }


  protected void addSqlMapNodelets() {
    parser.addNodelet("/sqlMapConfig/sqlMap", new Nodelet() {
      public void process(Node node) throws Exception {
        config.getErrorContext().setActivity("loading the SQL Map resource");

        Properties attributes = NodeletUtils.parseAttributes(node, config.globalProps);

        String resource = attributes.getProperty("resource");
        String url = attributes.getProperty("url");

        if (usingStreams) {
          InputStream inputStream = null;
          if (resource != null) {
            config.getErrorContext().setResource(resource);
            inputStream = Resources.getResourceAsStream(resource);
          } else if (url != null) {
            config.getErrorContext().setResource(url);
            inputStream = Resources.getUrlAsStream(url);
          } else {
            throw new SqlMapException("The <sqlMap> element requires either a resource or a url attribute.");
          }

          new SqlMapParser(config).parse(inputStream);
        } else {
          Reader reader = null;
          if (resource != null) {
            config.getErrorContext().setResource(resource);
            reader = Resources.getResourceAsReader(resource);
          } else if (url != null) {
            config.getErrorContext().setResource(url);
            reader = Resources.getUrlAsReader(url);
          } else {
            throw new SqlMapException("The <sqlMap> element requires either a resource or a url attribute.");
          }

          new SqlMapParser(config).parse(reader);
        }
      }
    });
  }

  private void addResultObjectFactoryNodelets() {
    parser.addNodelet("/sqlMapConfig/resultObjectFactory", new Nodelet() {
      public void process(Node node) throws Exception {
        Properties attributes = NodeletUtils.parseAttributes(node, config.globalProps);
        String type = attributes.getProperty("type");

        config.setResultObjectFactory(type);
      }
    });
    parser.addNodelet("/sqlMapConfig/resultObjectFactory/property", new Nodelet() {
      public void process(Node node) throws Exception {
        Properties attributes = NodeletUtils.parseAttributes(node, config.globalProps);
        String name = attributes.getProperty("name");
        String value = NodeletUtils.parsePropertyTokens(attributes.getProperty("value"), config.globalProps);
        config.getDelegate().getResultObjectFactory().setProperty(name, value);
      }
    });
  }

}
