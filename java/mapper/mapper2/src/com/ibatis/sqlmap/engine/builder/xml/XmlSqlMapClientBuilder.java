/*
 *  Copyright 2004 Clinton Begin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.ibatis.sqlmap.engine.builder.xml;

import com.ibatis.common.beans.Probe;
import com.ibatis.common.beans.ProbeFactory;
import com.ibatis.common.io.ReaderInputStream;
import com.ibatis.common.resources.Resources;
import com.ibatis.sqlmap.client.SqlMapClient;
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
import com.ibatis.sqlmap.engine.mapping.parameter.BasicParameterMap;
import com.ibatis.sqlmap.engine.mapping.parameter.BasicParameterMapping;
import com.ibatis.sqlmap.engine.mapping.parameter.InlineParameterMapParser;
import com.ibatis.sqlmap.engine.mapping.parameter.ParameterMap;
import com.ibatis.sqlmap.engine.mapping.result.AutoResultMap;
import com.ibatis.sqlmap.engine.mapping.result.BasicResultMap;
import com.ibatis.sqlmap.engine.mapping.result.BasicResultMapping;
import com.ibatis.sqlmap.engine.mapping.result.ResultMapping;
import com.ibatis.sqlmap.engine.mapping.sql.Sql;
import com.ibatis.sqlmap.engine.mapping.sql.SqlText;
import com.ibatis.sqlmap.engine.mapping.sql.dynamic.DynamicSql;
import com.ibatis.sqlmap.engine.mapping.sql.dynamic.elements.DynamicParent;
import com.ibatis.sqlmap.engine.mapping.sql.dynamic.elements.SqlTag;
import com.ibatis.sqlmap.engine.mapping.sql.dynamic.elements.SqlTagHandler;
import com.ibatis.sqlmap.engine.mapping.sql.dynamic.elements.SqlTagHandlerFactory;
import com.ibatis.sqlmap.engine.mapping.sql.simple.SimpleDynamicSql;
import com.ibatis.sqlmap.engine.mapping.sql.stat.StaticSql;
import com.ibatis.sqlmap.engine.mapping.statement.*;
import com.ibatis.sqlmap.engine.scope.ErrorContext;
import com.ibatis.sqlmap.engine.transaction.TransactionConfig;
import com.ibatis.sqlmap.engine.transaction.TransactionManager;
import com.ibatis.sqlmap.engine.transaction.external.ExternalTransactionConfig;
import com.ibatis.sqlmap.engine.transaction.jdbc.JdbcTransactionConfig;
import com.ibatis.sqlmap.engine.transaction.jta.JtaTransactionConfig;
import com.ibatis.sqlmap.engine.type.*;
import org.w3c.dom.CharacterData;
import org.w3c.dom.*;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.sql.ResultSet;
import java.util.*;

/**
 * NOT THREAD SAFE.  USE SEPARATE INSTANCES PER THREAD.
 */
public class XmlSqlMapClientBuilder {

  private static final Probe PROBE = ProbeFactory.getProbe();
  private static final InlineParameterMapParser PARAM_PARSER = new InlineParameterMapParser();

  private static final String NODE_PROPERTIES = "properties";
  private static final String NODE_SETTINGS = "settings";
  private static final String NODE_TX_MANAGER = "transactionManager";
  private static final String NODE_DATA_SOURCE = "dataSource";
  private static final String NODE_PROPERTY = "property";
  private static final String NODE_SQL_MAP = "sqlMap";
  private static final String NODE_CACHE_MODEL = "cacheModel";
  private static final String NODE_FLUSH_INTERVAL = "flushInterval";
  private static final String NODE_FLUSH_ON_EXECUTE = "flushOnExecute";
  private static final String NODE_RESULT_MAP = "resultMap";
  private static final String NODE_PARAMETER_MAP = "parameterMap";
  private static final String NODE_SELECT = "select";
  private static final String NODE_INSERT = "insert";
  private static final String NODE_UPDATE = "update";
  private static final String NODE_DELETE = "delete";
  private static final String NODE_STATEMENT = "statement";
  private static final String NODE_PROCEDURE = "procedure";
  private static final String NODE_SELECT_KEY = "selectKey";
  private static final String NODE_TYPE_ALIAS = "typeAlias";
  private static final String NODE_TYPE_HANDLER = "typeHandler";

  private boolean validationEnabled = true;

  // State Variables
  // These variables maintain the state of the buld process.
  private ErrorContext errorCtx = new ErrorContext();
  private ExtendedSqlMapClient client;
  private XmlConverter sqlMapConv;
  private XmlConverter sqlMapConfigConv;
  private Properties globalProps;
  private boolean useStatementNamespaces;
  private String currentNamespace;
  private TypeHandlerFactory typeHandlerFactory;

  public XmlSqlMapClientBuilder() {
    SqlMapExecutorDelegate delegate = new SqlMapExecutorDelegate();
    typeHandlerFactory = delegate.getTypeHandlerFactory();
    client = new SqlMapClientImpl(delegate);

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
    // -- use a string for OSCache to avoid uneccessary loading of properties upon init
    typeHandlerFactory.putTypeAlias("OSCACHE", "com.ibatis.sqlmap.engine.cache.oscache.OSCacheController");

    // TYPE ALIASEs
    typeHandlerFactory.putTypeAlias("dom", DomTypeMarker.class.getName());
    typeHandlerFactory.putTypeAlias("domCollection", DomCollectionTypeMarker.class.getName());
    typeHandlerFactory.putTypeAlias("xml", XmlTypeMarker.class.getName());
    typeHandlerFactory.putTypeAlias("xmlCollection", XmlCollectionTypeMarker.class.getName());
  }

  public boolean isValidationEnabled() {
    return validationEnabled;
  }

  public void setValidationEnabled(boolean validationEnabled) {
    this.validationEnabled = validationEnabled;
  }

  public SqlMapClient buildSqlMap(Reader reader, Properties props, XmlConverter sqlMapConfigConverter, XmlConverter sqlMapConverter) {
    this.globalProps = props;
    this.sqlMapConv = sqlMapConverter;
    this.sqlMapConfigConv = sqlMapConfigConverter;
    return buildSqlMap(reader);
  }

  public SqlMapClient buildSqlMap(Reader reader, XmlConverter sqlMapConfigConverter, XmlConverter sqlMapConverter) {
    this.sqlMapConv = sqlMapConverter;
    this.sqlMapConfigConv = sqlMapConfigConverter;
    return buildSqlMap(reader);
  }

  public SqlMapClient buildSqlMap(Reader reader, Properties props) {
    this.globalProps = props;
    return buildSqlMap(reader);
  }

  public SqlMapClient buildSqlMap(Reader reader) {

    errorCtx.setResource("the SQL Map Configuration file");

    // Parse input file
    if (reader == null) {
      throw new SqlMapException("The reader passed to SqlMapClientBuilder was null.");
    }

    try {

      if (sqlMapConfigConv != null) {
        reader = sqlMapConfigConv.convertXml(reader);
      }

      Document doc = getDoc(reader);

      Element rootElement = (Element) doc.getLastChild();

      return parseSqlMapConfig(rootElement);

    } catch (Exception e) {
      errorCtx.setCause(e);
      throw new SqlMapException("There was an error while building the SqlMap instance." + errorCtx, e);
    }

  }

  private SqlMapClient parseSqlMapConfig(Node n) throws IOException {
    errorCtx.setActivity("creating the SqlMapClient instance");

    NodeList children = n.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node child = children.item(i);
      if (child.getNodeType() == Node.ELEMENT_NODE) {
        if (NODE_PROPERTIES.equals(child.getNodeName())) {
          parseGlobalProperties(child);
        } else if (NODE_TYPE_ALIAS.equals(child.getNodeName())) {
          parseTypeAliasNode(child);
        } else if (NODE_TYPE_HANDLER.equals(child.getNodeName())) {
          parseTypeHandlerNode(child);
        } else if (NODE_SETTINGS.equals(child.getNodeName())) {
          parseSettings(child);
        } else if (NODE_TX_MANAGER.equals(child.getNodeName())) {
          parseTransactionManager(child);
        } else if (NODE_SQL_MAP.equals(child.getNodeName())) {
          parseSqlMapRef(child);
        }
      }
    }

    wireUpCacheModelListeners();

    return client;
  }

  private void parseGlobalProperties(Node n) {
    errorCtx.setActivity("loading global properties");

    Properties attributes = parseAttributes(n);
    String resource = attributes.getProperty("resource");
    String url = attributes.getProperty("url");

    try {
      Properties props = null;
      if (resource != null) {
        errorCtx.setResource(resource);
        props = Resources.getResourceAsProperties(resource);
      } else if (url != null) {
        errorCtx.setResource(url);
        props = Resources.getUrlAsProperties(resource);
      } else {
        throw new SqlMapException("The " + NODE_PROPERTIES + " element requires either a resource or a url attribute.");
      }

      if (globalProps == null) {
        globalProps = props;
      } else {
        props.putAll(globalProps);
        globalProps = props;
      }
    } catch (IOException e) {
      throw new SqlMapException("Error loading properties.  Cause: " + e);
    }
  }

  private void parseSettings(Node n) {
    errorCtx.setActivity("loading settings properties");

    Properties attributes = parseAttributes(n);

    String lazyLoadingEnabledAttr = attributes.getProperty("lazyLoadingEnabled");
    boolean lazyLoadingEnabled = (lazyLoadingEnabledAttr == null || "true".equals(lazyLoadingEnabledAttr));
    client.getDelegate().setLazyLoadingEnabled(lazyLoadingEnabled);

    String cacheModelsEnabledAttr = attributes.getProperty("cacheModelsEnabled");
    boolean cacheModelsEnabled = (cacheModelsEnabledAttr == null || "true".equals(cacheModelsEnabledAttr));
    client.getDelegate().setCacheModelsEnabled(cacheModelsEnabled);

    String enhancementEnabledAttr = attributes.getProperty("enhancementEnabled");
    boolean enhancementEnabled = (enhancementEnabledAttr == null || "true".equals(enhancementEnabledAttr));
    try {
      enhancementEnabled = enhancementEnabled && Class.forName("net.sf.cglib.proxy.InvocationHandler") != null;
    } catch (ClassNotFoundException e) {
      enhancementEnabled = false;
    }
    client.getDelegate().setEnhancementEnabled(enhancementEnabled);

    String useStatementNamespacesAttr = attributes.getProperty("useStatementNamespaces");
    useStatementNamespaces = ("true".equals(useStatementNamespacesAttr));

    String maxTransactions = attributes.getProperty("maxTransactions");
    if (maxTransactions != null && Integer.parseInt(maxTransactions) > 0) {
      client.getDelegate().setMaxTransactions(Integer.parseInt(maxTransactions));
    }

    String maxRequests = attributes.getProperty("maxRequests");
    if (maxRequests != null && Integer.parseInt(maxRequests) > 0) {
      client.getDelegate().setMaxRequests(Integer.parseInt(maxRequests));
    }

    String maxSessions = attributes.getProperty("maxSessions");
    if (maxSessions != null && Integer.parseInt(maxSessions) > 0) {
      client.getDelegate().setMaxSessions(Integer.parseInt(maxSessions));
    }

    AccessPlanFactory.setBytecodeEnhancementEnabled(client.getDelegate().isEnhancementEnabled());
  }

  private void parseTransactionManager(Node n) {
    errorCtx.setActivity("configuring the transaction manager");

    Properties attributes = parseAttributes(n);

    Properties initProperties = new Properties();
    String type = attributes.getProperty("type");
    type = typeHandlerFactory.resolveAlias(type);

    DataSource dataSource = null;

    NodeList children = n.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node child = children.item(i);
      if (child.getNodeType() == Node.ELEMENT_NODE) {
        if (NODE_PROPERTY.equals(child.getNodeName())) {
          addNameValuePairProperty(child, initProperties);
        } else if (NODE_DATA_SOURCE.equals(child.getNodeName())) {
          dataSource = parseDataSource(child);
        }
      }
    }

    TransactionManager txManager = null;
    try {
      errorCtx.setMoreInfo("Check the transaction manager type or class.");
      TransactionConfig config = (TransactionConfig) Resources.instantiate(type);
      config.setDataSource(dataSource);
      config.setMaximumConcurrentTransactions(client.getDelegate().getMaxTransactions());
      errorCtx.setMoreInfo("Check the transactio nmanager properties or configuration.");
      config.initialize(initProperties);
      errorCtx.setMoreInfo(null);
      txManager = new TransactionManager(config);
    } catch (Exception e) {
      if (e instanceof SqlMapException) {
        throw (SqlMapException) e;
      } else {
        throw new SqlMapException("Error initializing TransactionManager.  Could not instantiate TransactionConfig.  Cause: " + e, e);
      }
    }

    client.getDelegate().setTxManager(txManager);

  }

  private DataSource parseDataSource(Node n) {
    errorCtx.setActivity("configuring the data source");

    Properties attributes = parseAttributes(n);

    Properties initProperties = new Properties();
    String type = attributes.getProperty("type");
    type = typeHandlerFactory.resolveAlias(type);

    NodeList children = n.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node child = children.item(i);
      if (child.getNodeType() == Node.ELEMENT_NODE) {
        if (NODE_PROPERTY.equals(child.getNodeName())) {
          addNameValuePairProperty(child, initProperties);
        }
      }
    }

    DataSource dataSource = null;
    try {
      errorCtx.setMoreInfo("Check the data source type or class.");
      DataSourceFactory dsFactory = (DataSourceFactory) Resources.instantiate(type);
      errorCtx.setMoreInfo("Check the data source properties or configuration.");
      dsFactory.initialize(initProperties);
      dataSource = dsFactory.getDataSource();
      errorCtx.setMoreInfo(null);
    } catch (Exception e) {
      if (e instanceof SqlMapException) {
        throw (SqlMapException) e;
      } else {
        throw new SqlMapException("Error initializing DataSource.  Could not instantiate DataSourceFactory.  Cause: " + e, e);
      }
    }

    return dataSource;
  }

  private void parseSqlMapRef(Node n) throws IOException {
    errorCtx.setActivity("loading the SQL Map resource");

    Properties attributes = parseAttributes(n);

    String resource = attributes.getProperty("resource");
    String url = attributes.getProperty("url");

    Reader reader = null;
    if (resource != null) {
      errorCtx.setResource(resource);
      reader = Resources.getResourceAsReader(resource);
    } else if (url != null) {
      errorCtx.setResource(url);
      reader = Resources.getUrlAsReader(url);
    } else {
      throw new SqlMapException("The " + NODE_SQL_MAP + " element requires either a resource or a url attribute.");
    }

    if (sqlMapConv != null) {
      reader = sqlMapConv.convertXml(reader);
    }

    Document doc = getDoc(reader);
    parseSqlMap(doc.getLastChild());

    reader.close();
  }

  private void parseSqlMap(Node n) {
    errorCtx.setActivity("building an SQL Map instance");

    Properties attributes = parseAttributes(n);
    currentNamespace = attributes.getProperty("namespace");


    NodeList children = n.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node child = children.item(i);
      if (child.getNodeType() == Node.ELEMENT_NODE) {
        if (NODE_SELECT.equals(child.getNodeName())) {
          parseSelect(child);
        } else if (NODE_INSERT.equals(child.getNodeName())) {
          parseInsert(child);
        } else if (NODE_UPDATE.equals(child.getNodeName())) {
          parseUpdate(child);
        } else if (NODE_DELETE.equals(child.getNodeName())) {
          parseDelete(child);
        } else if (NODE_STATEMENT.equals(child.getNodeName())) {
          parseStatement(child);
        } else if (NODE_PROCEDURE.equals(child.getNodeName())) {
          parseProcedure(child);
        } else if (NODE_RESULT_MAP.equals(child.getNodeName())) {
          parseResultMap(child);
        } else if (NODE_PARAMETER_MAP.equals(child.getNodeName())) {
          parseParameterMap(child);
        } else if (NODE_CACHE_MODEL.equals(child.getNodeName())) {
          parseCacheModel(child);
        } else if (NODE_TYPE_ALIAS.equals(child.getNodeName())) {
          parseTypeAliasNode(child);
        } else if (NODE_TYPE_HANDLER.equals(child.getNodeName())) {
          parseTypeHandlerNode(child);
        }
      }
    }
  }

  private void parseProcedure(Node n) {
    MappedStatement statement = parseGeneralStatement(n, new ProcedureStatement());
    client.getDelegate().addMappedStatement(statement);
  }

  private void parseSelect(Node n) {
    MappedStatement statement = parseGeneralStatement(n, new SelectStatement());
    client.getDelegate().addMappedStatement(statement);
  }

  private void parseInsert(Node n) {
    MappedStatement statement = parseGeneralStatement(n, new InsertStatement());
    client.getDelegate().addMappedStatement(statement);
  }

  private void parseUpdate(Node n) {
    MappedStatement statement = parseGeneralStatement(n, new UpdateStatement());
    client.getDelegate().addMappedStatement(statement);
  }

  private void parseDelete(Node n) {
    MappedStatement statement = parseGeneralStatement(n, new DeleteStatement());
    client.getDelegate().addMappedStatement(statement);
  }

  private void parseStatement(Node n) {
    MappedStatement statement = parseGeneralStatement(n, new GeneralStatement());
    client.getDelegate().addMappedStatement(statement);
  }

  private MappedStatement parseGeneralStatement(Node n, GeneralStatement statement) {
    errorCtx.setActivity("parsing a mapped statement");

    // get attributes
    Properties attributes = parseAttributes(n);
    String id = attributes.getProperty("id");

    if (useStatementNamespaces) {
      id = applyNamespace(id);
    }

    String parameterMapName = applyNamespace(attributes.getProperty("parameterMap"));
    String parameterClassName = attributes.getProperty("parameterClass");
    String resultMapName = applyNamespace(attributes.getProperty("resultMap"));
    String resultClassName = attributes.getProperty("resultClass");
    String cacheModelName = applyNamespace(attributes.getProperty("cacheModel"));
    String xmlResultName = attributes.getProperty("xmlResultName");
    String resultSetType = attributes.getProperty("resultSetType");
    String fetchSize = attributes.getProperty("fetchSize");

    errorCtx.setObjectId(id + " statement");

    parameterClassName = typeHandlerFactory.resolveAlias(parameterClassName);
    resultClassName = typeHandlerFactory.resolveAlias(resultClassName);

    Class parameterClass = null;
    Class resultClass = null;

    // get parameter and result maps

    errorCtx.setMoreInfo("Check the result map name.");
    BasicResultMap resultMap = null;
    if (resultMapName != null) {
      resultMap = (BasicResultMap) client.getDelegate().getResultMap(resultMapName);
    }

    errorCtx.setMoreInfo("Check the parameter map name.");
    BasicParameterMap parameterMap = null;
    if (parameterMapName != null) {
      parameterMap = (BasicParameterMap) client.getDelegate().getParameterMap(parameterMapName);
    }

    statement.setId(id);
    statement.setParameterMap(parameterMap);
    statement.setResultMap(resultMap);
    statement.setResource(errorCtx.getResource());

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
    if (parameterMap == null) {
      try {
        if (parameterClassName != null) {
          errorCtx.setMoreInfo("Check the parameter class.");
          parameterClass = Resources.classForName(parameterClassName);
          statement.setParameterClass(parameterClass);
        }
      } catch (ClassNotFoundException e) {
        throw new SqlMapException("Error.  Could not set parameter class.  Cause: " + e, e);
      }
    } else {
      statement.setParameterClass(parameterMap.getParameterClass());
    }

    try {
      if (resultClassName != null) {
        errorCtx.setMoreInfo("Check the result class.");
        resultClass = Resources.classForName(resultClassName);
      }
    } catch (ClassNotFoundException e) {
      throw new SqlMapException("Error.  Could not set result class.  Cause: " + e, e);
    }

    // process SQL statement, including inline parameter maps
    errorCtx.setMoreInfo("Check the SQL statement.");
    processSqlStatement(n, statement);

    // set up either null result map or automatic result mapping
    if (resultMap == null && resultClass == null) {
      statement.setResultMap(null);
    } else if (resultMap == null) {
      resultMap = new AutoResultMap(client.getDelegate());
      resultMap.setId(statement.getId() + "-AutoResultMap");
      resultMap.setResultClass(resultClass);
      resultMap.setXmlName(xmlResultName);
      resultMap.setResource(statement.getResource());
      statement.setResultMap(resultMap);

    }

    errorCtx.setMoreInfo(null);
    errorCtx.setObjectId(null);

    statement.setSqlMapClient(client);
    if (cacheModelName != null && cacheModelName.length() > 0 && client.getDelegate().isCacheModelsEnabled()) {
      CacheModel cacheModel = (CacheModel) client.getDelegate().getCacheModel(cacheModelName);
      return new CachingStatement(statement, cacheModel);
    } else {
      return statement;
    }

  }

  private SelectKeyStatement parseSelectKey(Node n, GeneralStatement insertStatement) {
    errorCtx.setActivity("parsing a select key");

    // get attributes
    Properties attributes = parseAttributes(n);
    String keyPropName = attributes.getProperty("keyProperty");
    String resultClassName = attributes.getProperty("resultClass");
    resultClassName = typeHandlerFactory.resolveAlias(resultClassName);
    Class resultClass = null;

    // get parameter and result maps
    SelectKeyStatement selectKeyStatement = new SelectKeyStatement();
    selectKeyStatement.setSqlMapClient(client);

    selectKeyStatement.setId(insertStatement.getId() + "-SelectKey");
    selectKeyStatement.setResource(errorCtx.getResource());
    selectKeyStatement.setKeyProperty(keyPropName);

    try {
      if (resultClassName != null) {
        errorCtx.setMoreInfo("Check the select key result class.");
        resultClass = Resources.classForName(resultClassName);
      } else {
        Class parameterClass = insertStatement.getParameterClass();
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
    errorCtx.setMoreInfo("Check the select key SQL statement.");
    processSqlStatement(n, selectKeyStatement);

    BasicResultMap resultMap;
    resultMap = new AutoResultMap(client.getDelegate());
    resultMap.setId(selectKeyStatement.getId() + "-AutoResultMap");
    resultMap.setResultClass(resultClass);
    resultMap.setResource(selectKeyStatement.getResource());
    selectKeyStatement.setResultMap(resultMap);

    errorCtx.setMoreInfo(null);
    return selectKeyStatement;

  }

  private void processSqlStatement(Node n, GeneralStatement statement) {
    errorCtx.setActivity("processing an SQL statement");

    boolean isDynamic = false;
    DynamicSql dynamic = new DynamicSql(client.getDelegate());
    StringBuffer sqlBuffer = new StringBuffer();

    isDynamic = parseDynamicTags(n, dynamic, sqlBuffer, isDynamic, false);
    if (statement instanceof InsertStatement) {
      InsertStatement insertStatement = ((InsertStatement) statement);
      SelectKeyStatement selectKeyStatement = findAndParseSelectKeyStatement(n, statement);
      insertStatement.setSelectKeyStatement(selectKeyStatement);
    }

    String sqlStatement = sqlBuffer.toString();
    if (isDynamic) {
      statement.setSql(dynamic);
    } else {
      applyInlineParameterMap(statement, sqlStatement);
    }

  }

  private boolean parseDynamicTags(Node n, DynamicParent dynamic, StringBuffer sqlBuffer, boolean isDynamic, boolean postParseRequired) {
    errorCtx.setActivity("parsing dynamic SQL tags");

    NodeList children = n.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node child = children.item(i);
      if (child.getNodeType() == Node.CDATA_SECTION_NODE
          || child.getNodeType() == Node.TEXT_NODE) {

        String data = ((CharacterData) child).getData();
        data = parsePropertyTokens(data);

        SqlText sqlText;
        if (postParseRequired) {
          sqlText = new SqlText();
          sqlText.setText(data.toString());
        } else {
          sqlText = PARAM_PARSER.parseInlineParameterMap(client.getDelegate().getTypeHandlerFactory(), data.toString(), null);
        }

        dynamic.addChild(sqlText);

        sqlBuffer.append(data);

      } else {
        errorCtx.setMoreInfo("Check the dynamic tags.");
        String nodeName = child.getNodeName();
        SqlTagHandler handler = SqlTagHandlerFactory.getSqlTagHandler(nodeName);
        if (handler != null) {
          isDynamic = true;

          SqlTag tag = new SqlTag();
          tag.setName(nodeName);
          tag.setHandler(handler);

          Properties attributes = parseAttributes(child);

          tag.setPrependAttr(attributes.getProperty("prepend"));
          tag.setPropertyAttr(attributes.getProperty("property"));

          tag.setOpenAttr(attributes.getProperty("open"));
          tag.setCloseAttr(attributes.getProperty("close"));

          tag.setComparePropertyAttr(attributes.getProperty("compareProperty"));
          tag.setCompareValueAttr(attributes.getProperty("compareValue"));
          tag.setConjunctionAttr(attributes.getProperty("conjunction"));

          dynamic.addChild(tag);

          if (child.hasChildNodes()) {
            isDynamic = parseDynamicTags(child, tag, sqlBuffer, isDynamic, handler.isPostParseRequired());
          }
        }
      }
    }
    errorCtx.setMoreInfo(null);
    return isDynamic;
  }

  private SelectKeyStatement findAndParseSelectKeyStatement(Node n, GeneralStatement insertStatement) {
    errorCtx.setActivity("parsing select key tags");

    SelectKeyStatement selectKeyStatement = null;

    boolean foundTextFirst = false;

    NodeList children = n.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node child = children.item(i);
      if (child.getNodeType() == Node.CDATA_SECTION_NODE
          || child.getNodeType() == Node.TEXT_NODE) {
        String data = ((CharacterData) child).getData();
        if (data.trim().length() > 0) {
          foundTextFirst = true;
        }
      } else if (child.getNodeType() == Node.ELEMENT_NODE
          && NODE_SELECT_KEY.equals(child.getNodeName())) {
        selectKeyStatement = parseSelectKey(child, insertStatement);
        break;
      }
    }
    if (selectKeyStatement != null) {
      selectKeyStatement.setAfter(foundTextFirst);
    }
    errorCtx.setMoreInfo(null);
    return selectKeyStatement;
  }

  private void applyInlineParameterMap(GeneralStatement statement, String sqlStatement) {
    String newSql = sqlStatement;

    errorCtx.setActivity("building an inline parameter map");

    ParameterMap parameterMap = statement.getParameterMap();

    errorCtx.setMoreInfo("Check the inline parameters.");
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

    Sql sql = null;
    if (SimpleDynamicSql.isSimpleDynamicSql(newSql)) {
      sql = new SimpleDynamicSql(client.getDelegate(), newSql);
    } else {
      sql = new StaticSql(newSql);
    }
    statement.setSql(sql);
  }

  private void parseResultMap(Node n) {
    errorCtx.setActivity("building a result map");

    BasicResultMap map;
    map = new BasicResultMap(client.getDelegate());

    Properties attributes = parseAttributes(n);
    String id = applyNamespace(attributes.getProperty("id"));
    String resultClassName = attributes.getProperty("class");
    String extended = applyNamespace(attributes.getProperty("extends"));
    String xmlName = attributes.getProperty("xmlName");
    resultClassName = typeHandlerFactory.resolveAlias(resultClassName);

    errorCtx.setObjectId(id + " result map");

    map.setId(id);
    map.setXmlName(xmlName);
    map.setResource(errorCtx.getResource());

    Class resultClass = null;
    try {
      errorCtx.setMoreInfo("Check the result class.");
      resultClass = Resources.classForName(resultClassName);
    } catch (Exception e) {
      if (e instanceof SqlMapException) {
        throw (SqlMapException) e;
      } else {
        throw new SqlMapException("Error configuring Result.  Could not set ResultClass.  Cause: " + e, e);
      }
    }

    map.setResultClass(resultClass);

    List resultMappingList = new ArrayList();

    errorCtx.setMoreInfo("Check the extended result map.");
    if (extended != null) {
      BasicResultMap extendedResultMap = (BasicResultMap) client.getDelegate().getResultMap(extended);
      ResultMapping[] resultMappings = extendedResultMap.getResultMappings();
      for (int i = 0; i < resultMappings.length; i++) {
        resultMappingList.add(resultMappings[i]);
      }
    }

    errorCtx.setMoreInfo("Check the result mappings.");
    NodeList children = n.getChildNodes();
    int index = resultMappingList.size();
    for (int i = 0; i < children.getLength(); i++) {
      Node child = children.item(i);
      if (child.getNodeType() == Node.ELEMENT_NODE) {
        Properties childAttributes = parseAttributes(child);
        String propertyName = childAttributes.getProperty(NODE_PROPERTY);
        String nullValue = childAttributes.getProperty("nullValue");
        String jdbcType = childAttributes.getProperty("jdbcType");
        String javaType = childAttributes.getProperty("javaType");
        String columnName = childAttributes.getProperty("column");
        String columnIndex = childAttributes.getProperty("columnIndex");
        String statementName = childAttributes.getProperty("select");
        String callback = childAttributes.getProperty("typeHandler");

        callback = typeHandlerFactory.resolveAlias(callback);
        javaType = typeHandlerFactory.resolveAlias(javaType);

        errorCtx.setObjectId(propertyName + " mapping of the " + id + " result map");

        TypeHandler handler = null;
        if (callback != null) {
          errorCtx.setMoreInfo("Check the result mapping typeHandler attribute '" + callback + "' (must be a TypeHandlerCallback implementation).");
          try {
            TypeHandlerCallback typeHandlerCallback = (TypeHandlerCallback) Resources.classForName(callback).newInstance();
            handler = new CustomTypeHandler(typeHandlerCallback);
          } catch (Exception e) {
            throw new SqlMapException("Error occurred during custom type handler configuration.  Cause: " + e, e);
          }
        } else {
          errorCtx.setMoreInfo("Check the result mapping property type or name.");
          handler = resolveTypeHandler(client.getDelegate().getTypeHandlerFactory(), resultClass, propertyName, javaType, jdbcType, true);
        }


        BasicResultMapping mapping = new BasicResultMapping();
        mapping.setPropertyName(propertyName);
        mapping.setColumnName(columnName);
        mapping.setJdbcTypeName(jdbcType);
        mapping.setTypeHandler(handler);
        mapping.setNullValue(nullValue);
        mapping.setStatementName(statementName);
        try {
          if (javaType != null && javaType.length() > 0) {
            mapping.setJavaType(Class.forName(javaType));
          }
        } catch (ClassNotFoundException e) {
          throw new SqlMapException("Error setting javaType on result mapping.  Cause: " + e);
        }

        if (columnIndex != null && columnIndex.length() > 0) {
          mapping.setColumnIndex(Integer.parseInt(columnIndex));
        } else {
          index++;
          mapping.setColumnIndex(index);
        }

        resultMappingList.add(mapping);
      }
    }
    map.setResultMappingList(resultMappingList);

    client.getDelegate().addResultMap(map);

    errorCtx.setMoreInfo(null);

    errorCtx.setObjectId(null);
  }

  private void parseParameterMap(Node n) {
    errorCtx.setActivity("building a parameter map");

    BasicParameterMap map;
    map = new BasicParameterMap(client.getDelegate());

    Properties attributes = parseAttributes(n);
    String id = applyNamespace(attributes.getProperty("id"));
    String parameterClassName = attributes.getProperty("class");
    parameterClassName = typeHandlerFactory.resolveAlias(parameterClassName);

    map.setId(id);
    map.setResource(errorCtx.getResource());

    errorCtx.setObjectId(id + " parameter map");

    Class parameterClass = null;
    try {
      errorCtx.setMoreInfo("Check the parameter class.");
      parameterClass = Resources.classForName(parameterClassName);
    } catch (Exception e) {
      //throw new SqlMapException("Error configuring ParameterMap.  Could not set ParameterClass.  Cause: " + e, e);
    }

    map.setParameterClass(parameterClass);

    List parameterMappingList = new ArrayList();

    errorCtx.setMoreInfo("Check the parameter mappings.");
    NodeList children = n.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node child = children.item(i);
      if (child.getNodeType() == Node.ELEMENT_NODE) {
        Properties childAttributes = parseAttributes(child);
        String propertyName = childAttributes.getProperty(NODE_PROPERTY);
        String jdbcType = childAttributes.getProperty("jdbcType");
        String javaType = childAttributes.getProperty("javaType");
        String nullValue = childAttributes.getProperty("nullValue");
        String mode = childAttributes.getProperty("mode");
        String callback = childAttributes.getProperty("typeHandler");

        callback = typeHandlerFactory.resolveAlias(callback);
        javaType = typeHandlerFactory.resolveAlias(javaType);

        errorCtx.setObjectId(propertyName + " mapping of the " + id + " parameter map");

        TypeHandler handler = null;
        if (callback != null) {
          errorCtx.setMoreInfo("Check the parameter mapping typeHandler attribute '" + callback + "' (must be a TypeHandlerCallback implementation).");
          try {
            TypeHandlerCallback typeHandlerCallback = (TypeHandlerCallback) Resources.classForName(callback).newInstance();
            handler = new CustomTypeHandler(typeHandlerCallback);
          } catch (Exception e) {
            throw new SqlMapException("Error occurred during custom type handler configuration.  Cause: " + e, e);
          }
        } else {
          errorCtx.setMoreInfo("Check the parameter mapping property type or name.");
          handler = resolveTypeHandler(client.getDelegate().getTypeHandlerFactory(), parameterClass, propertyName, javaType, jdbcType);
        }

        BasicParameterMapping mapping = new BasicParameterMapping();
        mapping.setPropertyName(propertyName);
        mapping.setJdbcTypeName(jdbcType);
        mapping.setNullValue(nullValue);
        if (mode != null && mode.length() > 0) {
          mapping.setMode(mode);
        }
        mapping.setTypeHandler(handler);
        try {
          if (javaType != null && javaType.length() > 0) {
            mapping.setJavaType(Class.forName(javaType));
          }
        } catch (ClassNotFoundException e) {
          throw new SqlMapException("Error setting javaType on parameter mapping.  Cause: " + e);
        }


        parameterMappingList.add(mapping);
      }
    }
    map.setParameterMappingList(parameterMappingList);

    client.getDelegate().addParameterMap(map);

    errorCtx.setMoreInfo(null);
    errorCtx.setObjectId(null);
  }

  private void parseTypeHandlerNode(Node child) {
    errorCtx.setActivity("building a building custom type handler");
    try {
      TypeHandlerFactory typeHandlerFactory = client.getDelegate().getTypeHandlerFactory();

      Properties prop = parseAttributes(child);

      String jdbcType = prop.getProperty("jdbcType");
      String javaType = prop.getProperty("javaType");
      String callback = prop.getProperty("callback");
      callback = typeHandlerFactory.resolveAlias(callback);
      javaType = typeHandlerFactory.resolveAlias(javaType);

      errorCtx.setMoreInfo("Check the callback attribute '" + callback + "' (must be a classname).");
      TypeHandlerCallback typeHandlerCallback = (TypeHandlerCallback) Resources.classForName(callback).newInstance();
      TypeHandler typeHandler = new CustomTypeHandler(typeHandlerCallback);

      errorCtx.setMoreInfo("Check the javaType attribute '" + javaType + "' (must be a classname) or the jdbcType '" + jdbcType + "' (must be a JDBC type name).");
      if (jdbcType != null && jdbcType.length() > 0) {
        typeHandlerFactory.register(Resources.classForName(javaType), jdbcType, typeHandler);
      } else {
        typeHandlerFactory.register(Resources.classForName(javaType), typeHandler);
      }
    } catch (Exception e) {
      throw new SqlMapException("Error registering occurred.  Cause: " + e, e);
    }
    errorCtx.setMoreInfo(null);
    errorCtx.setObjectId(null);
  }

  private void parseCacheModel(Node n) {
    errorCtx.setActivity("building a cache model");

    CacheModel model = new CacheModel();

    Properties attributes = parseAttributes(n);
    String id = applyNamespace(attributes.getProperty("id"));
    String type = attributes.getProperty("type");
    type = typeHandlerFactory.resolveAlias(type);

    String readOnly = attributes.getProperty("readOnly");
    if (readOnly != null && readOnly.length() > 0) {
      model.setReadOnly("true".equals(readOnly));
    } else {
      model.setReadOnly(true);
    }

    String serialize = attributes.getProperty("serialize");
    if (serialize != null && serialize.length() > 0) {
      model.setSerialize("true".equals(serialize));
    } else {
      model.setSerialize(false);
    }

    errorCtx.setObjectId(id + " cache model");

    errorCtx.setMoreInfo("Check the cache model type.");
    model.setId(id);
    model.setResource(errorCtx.getResource());

    try {
      model.setControllerClassName(type);
    } catch (Exception e) {
      throw new SqlMapException("Error setting Cache Controller Class.  Cause: " + e, e);
    }

    Properties modelProperties = new Properties();

    NodeList children = n.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node child = children.item(i);
      if (child.getNodeType() == Node.ELEMENT_NODE) {
        if (NODE_FLUSH_INTERVAL.equals(child.getNodeName())) {
          Properties childAttributes = parseAttributes(child);
          long t = 0;
          try {
            errorCtx.setMoreInfo("Check the cache model flush interval.");
            String milliseconds = childAttributes.getProperty("milliseconds");
            String seconds = childAttributes.getProperty("seconds");
            String minutes = childAttributes.getProperty("minutes");
            String hours = childAttributes.getProperty("hours");
            if (milliseconds != null) t += Integer.parseInt(milliseconds);
            if (seconds != null) t += Integer.parseInt(seconds) * 1000;
            if (minutes != null) t += Integer.parseInt(minutes) * 60 * 1000;
            if (hours != null) t += Integer.parseInt(hours) * 60 * 60 * 1000;
            if (t < 1) throw new SqlMapException("A flush interval must specify one or more of milliseconds, seconds, minutes or hours.");
            model.setFlushInterval(t);
          } catch (NumberFormatException e) {
            throw new SqlMapException("Error building cache '" + model.getId() + "' in '" + "resourceNAME" + "'.  Flush interval milliseconds must be a valid long integer value.  Cause: " + e, e);
          }
        } else if (NODE_FLUSH_ON_EXECUTE.equals(child.getNodeName())) {
          errorCtx.setMoreInfo("Check the cache model flush on statement elements.");
          Properties childAttributes = parseAttributes(child);
          model.addFlushTriggerStatement(childAttributes.getProperty("statement"));
        } else if (NODE_PROPERTY.equals(child.getNodeName())) {
          errorCtx.setMoreInfo("Check the cache model properties.");
          addNameValuePairProperty(child, modelProperties);
        }
      }
    }
    errorCtx.setMoreInfo("Check the cache model configuration.");
    model.configure(modelProperties);

    if (client.getDelegate().isCacheModelsEnabled()) {
      client.getDelegate().addCacheModel(model);
    }

    errorCtx.setMoreInfo(null);
    errorCtx.setObjectId(null);
  }

  private String applyNamespace(String id) {
    String newId = id;

    if (currentNamespace != null && currentNamespace.length() > 0 && id != null && id.indexOf(".") < 0) {
      newId = currentNamespace + "." + id;
    }
    return newId;

  }

  private void wireUpCacheModelListeners() {

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
          throw new SqlMapException("Could not find statement named '" + statementName + "' for use as a flush trigger for the cache model named '" + cacheName + "'.");
        }
      }
    }
  }


  //--------------------------------------------------------
  //--------------------------------------------------------
  //--------------------------------------------------------

  private Document getDoc(Reader reader) {
    try {
      // Configuration
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setNamespaceAware(false);
      dbf.setValidating(validationEnabled);
      dbf.setIgnoringComments(true);
      dbf.setIgnoringElementContentWhitespace(false);
      dbf.setCoalescing(false);
      dbf.setExpandEntityReferences(true);

      OutputStreamWriter errorWriter = new OutputStreamWriter(System.err);

      DocumentBuilder db = dbf.newDocumentBuilder();
      db.setErrorHandler(new SimpleErrorHandler(new PrintWriter(errorWriter, true)));
      db.setEntityResolver(new SqlMapClasspathEntityResolver());

      Document doc = db.parse(new ReaderInputStream(reader));
      return doc;
    } catch (Exception e) {
      throw new SqlMapException("XML Parser Error.  Cause: " + e, e);
    }
  }

  private static TypeHandler resolveTypeHandler(TypeHandlerFactory typeHandlerFactory, Class clazz, String propertyName, String javaType, String jdbcType) {
    return resolveTypeHandler(typeHandlerFactory, clazz, propertyName, javaType, jdbcType, false);
  }

  private static TypeHandler resolveTypeHandler(TypeHandlerFactory typeHandlerFactory, Class clazz, String propertyName, String javaType, String jdbcType, boolean useSetterToResolve) {
    TypeHandler handler = null;
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
          throw new SqlMapException("Error.  Could not set TypeHandler.  Cause: " + e, e);
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
          throw new SqlMapException("Error.  Could not set TypeHandler.  Cause: " + e, e);
        }
      }
    }
    return handler;
  }


  private void parseTypeAliasNode(Node child) {
    Properties prop = parseAttributes(child);
    String alias = prop.getProperty("alias");
    String type = prop.getProperty("type");
    typeHandlerFactory.putTypeAlias(alias, type);
  }


  private void addNameValuePairProperty(Node child, Properties modelProperties) {
    Properties prop = parseAttributes(child);
    String name = prop.getProperty("name");
    String value = prop.getProperty("value");
    modelProperties.setProperty(name, value);
  }

  private Properties parseAttributes(Node n) {
    Properties attributes = new Properties();
    NamedNodeMap attributeNodes = n.getAttributes();
    for (int i = 0; i < attributeNodes.getLength(); i++) {
      Node attribute = attributeNodes.item(i);
      String value = parsePropertyTokens(attribute.getNodeValue());
      attributes.put(attribute.getNodeName(), value);
    }
    return attributes;
  }

  private String parsePropertyTokens(String string) {
    final String OPEN = "${";
    final String CLOSE = "}";

    Properties props = globalProps;

    String newString = string;
    if (newString != null && props != null) {
      int start = newString.indexOf(OPEN);
      int end = newString.indexOf(CLOSE);

      while (start > -1 && end > start) {
        String prepend = newString.substring(0, start);
        String append = newString.substring(end + CLOSE.length());
        String propName = newString.substring(start + OPEN.length(), end);
        String propValue = props.getProperty(propName);
        if (propValue == null) {
          newString = prepend + propName + append;
        } else {
          newString = prepend + propValue + append;
        }
        start = newString.indexOf(OPEN);
        end = newString.indexOf(CLOSE);
      }
    }
    return newString;
  }

  /**
   * **********************************
   * ******* SimpleErrorHandler *******
   * **********************************
   */

  // Error handler to report errors and warnings
  private static class SimpleErrorHandler implements ErrorHandler {
    /**
     * Error handler output goes here
     */
    private PrintWriter out;

    SimpleErrorHandler(PrintWriter out) {
      this.out = out;
    }

    /**
     * Returns a string describing parse exception details
     */
    private String getParseExceptionInfo(SAXParseException spe) {
      String systemId = spe.getSystemId();
      if (systemId == null) {
        systemId = "null";
      }
      String info = "URI=" + systemId +
          " Line=" + spe.getLineNumber() +
          ": " + spe.getMessage();
      return info;
    }

    // The following methods are standard SAX ErrorHandler methods.
    // See SAX documentation for more info.

    public void warning(SAXParseException spe) throws SAXException {
      out.println("Warning: " + getParseExceptionInfo(spe));
    }

    public void error(SAXParseException spe) throws SAXException {
      String message = "Error: " + getParseExceptionInfo(spe);
      throw new SAXException(message);
    }

    public void fatalError(SAXParseException spe) throws SAXException {
      String message = "Fatal Error: " + getParseExceptionInfo(spe);
      throw new SAXException(message);
    }
  }

}

