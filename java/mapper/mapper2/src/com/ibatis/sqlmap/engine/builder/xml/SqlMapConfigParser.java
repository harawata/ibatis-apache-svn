package com.ibatis.sqlmap.engine.builder.xml;

import com.ibatis.common.resources.Resources;
import com.ibatis.common.xml.Nodelet;
import com.ibatis.common.xml.NodeletParser;
import com.ibatis.common.xml.NodeletUtils;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapException;
import org.w3c.dom.Node;

import java.io.InputStream;
import java.io.Reader;
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
    if (props != null) state.globalProps = props;
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
    if (props != null) state.globalProps = props;
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
        state.wireupCacheModels();
      }
    });
  }

  private void addGlobalPropNodelets() {
    parser.addNodelet("/sqlMapConfig/properties", new Nodelet() {
      public void process(Node node) throws Exception {
        Properties attributes = NodeletUtils.parseAttributes(node, state.globalProps);
        String resource = attributes.getProperty("resource");
        String url = attributes.getProperty("url");
        state.setGlobalProperties(resource, url);
      }
    });
  }

  private void addSettingsNodelets() {
    parser.addNodelet("/sqlMapConfig/settings", new Nodelet() {
      public void process(Node node) throws Exception {
        Properties attributes = NodeletUtils.parseAttributes(node, state.globalProps);

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

        state.setSettings(classInfoCacheEnabled, lazyLoadingEnabled, statementCachingEnabled, cacheModelsEnabled, enhancementEnabled, useStatementNamespaces, maxTransactions, maxRequests, maxSessions, defaultTimeout);
      }
    });
  }

  private void addTypeAliasNodelets() {
    parser.addNodelet("/sqlMapConfig/typeAlias", new Nodelet() {
      public void process(Node node) throws Exception {
        Properties prop = NodeletUtils.parseAttributes(node, state.globalProps);
        String alias = prop.getProperty("alias");
        String type = prop.getProperty("type");
        state.addTypeAlias(alias, type);
      }
    });
  }

  private void addTypeHandlerNodelets() {
    parser.addNodelet("/sqlMapConfig/typeHandler", new Nodelet() {
      public void process(Node node) throws Exception {
        Properties prop = NodeletUtils.parseAttributes(node, state.globalProps);
        String jdbcType = prop.getProperty("jdbcType");
        String javaType = prop.getProperty("javaType");
        String callback = prop.getProperty("callback");
        state.addGlobalTypeHandler(javaType, jdbcType, callback);
      }
    });
  }

  private void addTransactionManagerNodelets() {
    parser.addNodelet("/sqlMapConfig/transactionManager/property", new Nodelet() {
      public void process(Node node) throws Exception {
        Properties attributes = NodeletUtils.parseAttributes(node, state.globalProps);
        String name = attributes.getProperty("name");
        String value = NodeletUtils.parsePropertyTokens(attributes.getProperty("value"), state.globalProps);
        state.txProps.setProperty(name, value);
      }
    });
    parser.addNodelet("/sqlMapConfig/transactionManager/end()", new Nodelet() {
      public void process(Node node) throws Exception {
        Properties attributes = NodeletUtils.parseAttributes(node, state.globalProps);
        String type = attributes.getProperty("type");
        boolean commitRequired = "true".equals(attributes.getProperty("commitRequired"));
        state.setTransactionManager(type, commitRequired, state.txProps);
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
    parser.addNodelet("/sqlMapConfig/transactionManager/dataSource/end()", new Nodelet() {
      public void process(Node node) throws Exception {
        state.errorContext.setActivity("configuring the data source");

        Properties attributes = NodeletUtils.parseAttributes(node, state.globalProps);

        String type = attributes.getProperty("type");
        Properties props = state.dsProps;

        state.setDataSource(type, props);
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
        Properties attributes = NodeletUtils.parseAttributes(node, state.globalProps);
        String type = attributes.getProperty("type");

        state.setResultObjectFactory(type);
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
