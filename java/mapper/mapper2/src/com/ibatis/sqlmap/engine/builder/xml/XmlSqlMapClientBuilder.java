package com.ibatis.sqlmap.engine.builder.xml;

import com.ibatis.sqlmap.client.*;
import com.ibatis.sqlmap.engine.impl.*;
import com.ibatis.sqlmap.engine.type.*;
import com.ibatis.sqlmap.engine.mapping.parameter.*;
import com.ibatis.sqlmap.engine.mapping.result.*;
import com.ibatis.sqlmap.engine.mapping.statement.*;
import com.ibatis.sqlmap.engine.mapping.sql.stat.*;
import com.ibatis.sqlmap.engine.mapping.sql.*;
import com.ibatis.sqlmap.engine.mapping.sql.dynamic.*;
import com.ibatis.sqlmap.engine.mapping.sql.dynamic.elements.*;
import com.ibatis.sqlmap.engine.mapping.sql.simple.*;
import com.ibatis.sqlmap.engine.cache.*;
import com.ibatis.sqlmap.engine.cache.memory.MemoryCacheController;
import com.ibatis.sqlmap.engine.cache.fifo.FifoCacheController;
import com.ibatis.sqlmap.engine.cache.lru.LruCacheController;
import com.ibatis.sqlmap.engine.datasource.*;
import com.ibatis.sqlmap.engine.transaction.*;
import com.ibatis.sqlmap.engine.transaction.external.ExternalTransactionConfig;
import com.ibatis.sqlmap.engine.transaction.jta.JtaTransactionConfig;
import com.ibatis.sqlmap.engine.transaction.jdbc.JdbcTransactionConfig;
import com.ibatis.sqlmap.engine.scope.*;
import com.ibatis.common.resources.*;
import com.ibatis.common.beans.*;
import com.ibatis.sqlmap.engine.accessplan.*;
import com.ibatis.common.io.*;

import javax.xml.parsers.*;
import javax.sql.*;
import java.util.*;
import java.io.*;
import java.math.BigDecimal;

import org.w3c.dom.*;
import org.xml.sax.*;
import org.apache.commons.logging.*;

/**
 * User: Clinton Begin
 * Date: Nov 8, 2003
 * Time: 7:12:11 PM
 */
public class XmlSqlMapClientBuilder {

  private static final Log log = LogFactory.getLog(XmlSqlMapClientBuilder.class);

//  private static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
//  private static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
//  private static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
//  private static final String OUTPUT_ENCODING = "UTF-8";

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

  private static final String KEY_CURRENT_NAMESPACE = "NAMESPACE";
  private static final String KEY_SQL_MAP_CLIENT = "SqlMapClient";
  private static final String KEY_GLOBAL_PROPS = "GlobalProperties";
  private static final String KEY_DATA_SOURCE = "DataSource";
  private static final String KEY_GLOBAL_SQL_MAP_CONV = "SqlMapConverter";
  private static final String KEY_GLOBAL_CONFIG_CONV = "SqlMapConfigConverter";

  private static final String KEY_ERROR_CONTEXT = "ErrorContext";
  private static final String KEY_STATEMENT_NAMESPACES = "StatementNamespaces";

  private static final String GROUP_CACHE_MODEL = "CacheModel";
  private static final String GROUP_RESULT_MAP = "ResultMap";
  private static final String GROUP_PARAMETER_MAP = "ParameterMap";
  private static final String GROUP_SELECT_KEY = "SelectKey";

  private static final String PARAMETER_TOKEN = "#";

  private HashMap typeAliases = new HashMap();
  private Map contextPropertiesMap = new HashMap();

  private boolean validationEnabled = true;


  public XmlSqlMapClientBuilder() {
    // TRANSACTION ALIASES
    putTypeAlias("JDBC", JdbcTransactionConfig.class.getName());
    putTypeAlias("JTA", JtaTransactionConfig.class.getName());
    putTypeAlias("EXTERNAL", ExternalTransactionConfig.class.getName());

    // DATA SOURCE ALIASES
    putTypeAlias("SIMPLE", SimpleDataSourceFactory.class.getName());
    putTypeAlias("DBCP", DbcpDataSourceFactory.class.getName());
    putTypeAlias("JNDI", JndiDataSourceFactory.class.getName());

    // CACHE ALIASES
    putTypeAlias("FIFO", FifoCacheController.class.getName());
    putTypeAlias("LRU", LruCacheController.class.getName());
    putTypeAlias("MEMORY", MemoryCacheController.class.getName());
    // -- use a string for OSCache to avoid uneccessary loading of properties upon init
    putTypeAlias("OSCACHE", "com.ibatis.sqlmap.engine.cache.oscache.OSCacheController");

    // TYPE ALIASEs
    putTypeAlias("xml", XmlTypeMarker.class.getName());
    putTypeAlias("xmlCollection", XmlCollectionTypeMarker.class.getName());
    putTypeAlias("string", String.class.getName());
    putTypeAlias("byte", Byte.class.getName());
    putTypeAlias("long", Long.class.getName());
    putTypeAlias("short", Short.class.getName());
    putTypeAlias("int", Integer.class.getName());
    putTypeAlias("integer", Integer.class.getName());
    putTypeAlias("double", Double.class.getName());
    putTypeAlias("float", Float.class.getName());
    putTypeAlias("boolean", Boolean.class.getName());
    putTypeAlias("date", Date.class.getName());
    putTypeAlias("decimal", BigDecimal.class.getName());
    putTypeAlias("object", Object.class.getName());
    putTypeAlias("map", Map.class.getName());
    putTypeAlias("hashmap", HashMap.class.getName());
    putTypeAlias("list", List.class.getName());
    putTypeAlias("arraylist", ArrayList.class.getName());
    putTypeAlias("collection", Collection.class.getName());
    putTypeAlias("iterator", Iterator.class.getName());
  }

  public boolean isValidationEnabled() {
    return validationEnabled;
  }

  public void setValidationEnabled(boolean validationEnabled) {
    this.validationEnabled = validationEnabled;
  }

  public SqlMapClient buildSqlMap(Reader reader, Properties props, XmlConverter sqlMapConfigConverter, XmlConverter sqlMapConverter) {
    setContextObject(KEY_GLOBAL_PROPS, props);
    setContextObject(KEY_GLOBAL_SQL_MAP_CONV, sqlMapConverter);
    setContextObject(KEY_GLOBAL_CONFIG_CONV, sqlMapConfigConverter);
    return buildSqlMap(reader);
  }

  public SqlMapClient buildSqlMap(Reader reader, XmlConverter sqlMapConfigConverter, XmlConverter sqlMapConverter) {
    setContextObject(KEY_GLOBAL_SQL_MAP_CONV, sqlMapConverter);
    setContextObject(KEY_GLOBAL_CONFIG_CONV, sqlMapConfigConverter);
    return buildSqlMap(reader);
  }

  public SqlMapClient buildSqlMap(Reader reader, Properties props) {
    setContextObject(KEY_GLOBAL_PROPS, props);
    return buildSqlMap(reader);
  }

  public SqlMapClient buildSqlMap(Reader reader) {
    ErrorContext errorCtx = new ErrorContext();
    setContextObject(KEY_ERROR_CONTEXT, errorCtx);

    errorCtx.setResource("the SQL Map Configuration file");

    // Parse input file
    if (reader == null) {
      throw new SqlMapException ("The reader passed to SqlMapClientBuilder was null.");
    }

    try {

      XmlConverter converter = (XmlConverter) getContextObject(KEY_GLOBAL_CONFIG_CONV);
      if (converter != null) {
        reader = converter.convertXml(reader);
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
    ErrorContext errorCtx = (ErrorContext) getContextObject(KEY_ERROR_CONTEXT);
    errorCtx.setActivity("creating the SqlMapClient instance");

    SqlMapExecutorDelegate clientImpl = new SqlMapExecutorDelegate();
    setContextObject(KEY_SQL_MAP_CLIENT, new SqlMapClientImpl(clientImpl));

    NodeList children = n.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node child = children.item(i);
      if (child.getNodeType() == Node.ELEMENT_NODE) {
        if (NODE_PROPERTIES.equals(child.getNodeName())) {
          parseGlobalProperties(child);
        } else if (NODE_TYPE_ALIAS.equals(child.getNodeName())) {
          parseTypeAliasNode(child);
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

    return new SqlMapClientImpl(clientImpl);
  }

  private void parseGlobalProperties(Node n) {
      ErrorContext errorCtx = (ErrorContext) getContextObject(KEY_ERROR_CONTEXT);
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
          throw new SqlMapException ("The " + NODE_PROPERTIES + " element requires either a resource or a url attribute.");
        }

        Properties existingProperties = (Properties)getContextObject(KEY_GLOBAL_PROPS);
        if (existingProperties == null) {
          setContextObject(KEY_GLOBAL_PROPS, props);
        } else {
          props.putAll(existingProperties);
          setContextObject(KEY_GLOBAL_PROPS, props);
        }
      } catch (IOException e) {
        throw new SqlMapException("Error loading properties.  Cause: " + e);
      }
  }

  private void parseSettings(Node n) {
    ErrorContext errorCtx = (ErrorContext) getContextObject(KEY_ERROR_CONTEXT);
    errorCtx.setActivity("loading settings properties");

    ExtendedSqlMapClient client = (ExtendedSqlMapClient) getContextObject(KEY_SQL_MAP_CLIENT);
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
    boolean useStatementNamespaces = ("true".equals(useStatementNamespacesAttr));
    setContextObject(KEY_STATEMENT_NAMESPACES, new Boolean(useStatementNamespaces));

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
    ErrorContext errorCtx = (ErrorContext) getContextObject(KEY_ERROR_CONTEXT);
    errorCtx.setActivity("configuring the transaction manager");

    ExtendedSqlMapClient client = (ExtendedSqlMapClient) getContextObject(KEY_SQL_MAP_CLIENT);

    Properties attributes = parseAttributes(n);

    Properties initProperties = new Properties();
    String type = attributes.getProperty("type");
    type = resolveAlias(type);

    NodeList children = n.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node child = children.item(i);
      if (child.getNodeType() == Node.ELEMENT_NODE) {
        if (NODE_PROPERTY.equals(child.getNodeName())) {
          addNameValuePairProperty(child, initProperties);
        } else if (NODE_DATA_SOURCE.equals(child.getNodeName())) {
          parseDataSource(child);
        }
      }
    }

    TransactionManager txManager = null;
    try {
      DataSource dataSource = (DataSource) getContextObject(KEY_DATA_SOURCE);
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

  private void parseDataSource(Node n) {
    ErrorContext errorCtx = (ErrorContext) getContextObject(KEY_ERROR_CONTEXT);
    errorCtx.setActivity("configuring the data source");

    Properties attributes = parseAttributes(n);

    Properties initProperties = new Properties();
    String type = attributes.getProperty("type");
    type = resolveAlias(type);

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

    setContextObject(KEY_DATA_SOURCE, dataSource);
  }

  private void parseSqlMapRef(Node n) throws IOException {
    ErrorContext errorCtx = (ErrorContext) getContextObject(KEY_ERROR_CONTEXT);
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
      throw new SqlMapException ("The " + NODE_SQL_MAP + " element requires either a resource or a url attribute.");
    }

    XmlConverter converter = (XmlConverter) getContextObject(KEY_GLOBAL_SQL_MAP_CONV);
    if (converter != null) {
      reader = converter.convertXml(reader);
    }

    Document doc = getDoc(reader);
    parseSqlMap(doc.getLastChild());

    reader.close();
  }

  private void parseSqlMap(Node n) {
    ErrorContext errorCtx = (ErrorContext) getContextObject(KEY_ERROR_CONTEXT);
    errorCtx.setActivity("building an SQL Map instance");

    Properties attributes = parseAttributes (n);
    String namespace = attributes.getProperty("namespace");
    setContextObject(KEY_CURRENT_NAMESPACE, namespace);

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
        }
      }
    }
  }

  private void parseProcedure(Node n) {
    ExtendedSqlMapClient client = (ExtendedSqlMapClient) getContextObject(KEY_SQL_MAP_CLIENT);
    MappedStatement statement = parseGeneralStatement(n, new ProcedureStatement());
    client.getDelegate().addMappedStatement(statement);
  }

  private void parseSelect(Node n) {
    ExtendedSqlMapClient client = (ExtendedSqlMapClient) getContextObject(KEY_SQL_MAP_CLIENT);
    MappedStatement statement = parseGeneralStatement(n, new SelectStatement());
    client.getDelegate().addMappedStatement(statement);
  }

  private void parseInsert(Node n) {
    ExtendedSqlMapClient client = (ExtendedSqlMapClient) getContextObject(KEY_SQL_MAP_CLIENT);
    MappedStatement statement = parseGeneralStatement(n, new InsertStatement());
    client.getDelegate().addMappedStatement(statement);
  }

  private void parseUpdate(Node n) {
    ExtendedSqlMapClient client = (ExtendedSqlMapClient) getContextObject(KEY_SQL_MAP_CLIENT);
    MappedStatement statement = parseGeneralStatement(n, new UpdateStatement());
    client.getDelegate().addMappedStatement(statement);
  }

  private void parseDelete(Node n) {
    ExtendedSqlMapClient client = (ExtendedSqlMapClient) getContextObject(KEY_SQL_MAP_CLIENT);
    MappedStatement statement = parseGeneralStatement(n, new DeleteStatement());
    client.getDelegate().addMappedStatement(statement);
  }

  private void parseStatement(Node n) {
    ExtendedSqlMapClient client = (ExtendedSqlMapClient) getContextObject(KEY_SQL_MAP_CLIENT);
    MappedStatement statement = parseGeneralStatement(n, new GeneralStatement());
    client.getDelegate().addMappedStatement(statement);
  }

  private MappedStatement parseGeneralStatement(Node n, GeneralStatement statement) {
    ErrorContext errorCtx = (ErrorContext) getContextObject(KEY_ERROR_CONTEXT);
    errorCtx.setActivity("parsing a mapped statement");

    // get attributes
    Properties attributes = parseAttributes(n);
    String id = attributes.getProperty("id");

    if (isStatementNamespacesEnabled()) {
      id = applyNamespace(id);
    }

    String parameterMapName = applyNamespace(attributes.getProperty("parameterMap"));
    String parameterClassName = attributes.getProperty("parameterClass");
    String resultMapName = applyNamespace(attributes.getProperty("resultMap"));
    String resultClassName = attributes.getProperty("resultClass");
    String cacheModelName = applyNamespace(attributes.getProperty("cacheModel"));
    String xmlResultName = attributes.getProperty("xmlResultName");

    errorCtx.setObjectId(id + " statement");

    parameterClassName = resolveAlias(parameterClassName);
    resultClassName = resolveAlias(resultClassName);

    Class parameterClass = null;
    Class resultClass = null;

    // get parameter and result maps

    errorCtx.setMoreInfo("Check the result map name.");
    BasicResultMap resultMap = (BasicResultMap) getSubContextObject(GROUP_RESULT_MAP, resultMapName);
    if (resultMap == null && resultMapName != null) {
      throw new SqlMapException("Could not find ResultMap named " + resultMapName);
    }

    errorCtx.setMoreInfo("Check the parameter map name.");
    BasicParameterMap parameterMap = (BasicParameterMap) getSubContextObject(GROUP_PARAMETER_MAP, parameterMapName);
    if (parameterMap == null && parameterMapName != null) {
      throw new SqlMapException("Could not find ParameterMap named " + parameterMapName);
    }

    statement.setId(id);
    statement.setParameterMap(parameterMap);
    statement.setResultMap(resultMap);
    statement.setResource(errorCtx.getResource());

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
      resultMap = new AutoResultMap();
      resultMap.setId(statement.getId() + "-AutoResultMap");
      resultMap.setResultClass(resultClass);
      resultMap.setXmlName(xmlResultName);
      resultMap.setResource(statement.getResource());
      statement.setResultMap(resultMap);

    }

    errorCtx.setMoreInfo(null);
    errorCtx.setObjectId(null);

    ExtendedSqlMapClient client = (ExtendedSqlMapClient) getContextObject(KEY_SQL_MAP_CLIENT);
    statement.setSqlMapClient(client);
    if (cacheModelName != null && cacheModelName.length() > 0 && client.getDelegate().isCacheModelsEnabled()) {
      CacheModel cacheModel = (CacheModel) getSubContextObject(GROUP_CACHE_MODEL, cacheModelName);
      return new CachingStatement(statement, cacheModel);
    } else {
      return statement;
    }

  }

  private SelectKeyStatement parseSelectKey(Node n, GeneralStatement insertStatement) {
    ErrorContext errorCtx = (ErrorContext) getContextObject(KEY_ERROR_CONTEXT);
    errorCtx.setActivity("parsing a select key");

    // get attributes
    Properties attributes = parseAttributes(n);
    String keyPropName = attributes.getProperty("keyProperty");
    String resultClassName = attributes.getProperty("resultClass");
    resultClassName = resolveAlias(resultClassName);
    Class resultClass = null;

    // get parameter and result maps
    SelectKeyStatement selectKeyStatement = new SelectKeyStatement();
    ExtendedSqlMapClient client = (ExtendedSqlMapClient) getContextObject(KEY_SQL_MAP_CLIENT);
    selectKeyStatement.setSqlMapClient(client);

    selectKeyStatement.setId(insertStatement.getId() + GROUP_SELECT_KEY);
    selectKeyStatement.setResource(errorCtx.getResource());
    selectKeyStatement.setKeyProperty(keyPropName);

    try {
      if (resultClassName != null) {
        errorCtx.setMoreInfo("Check the select key result class.");
        resultClass = Resources.classForName(resultClassName);
      } else {
        Class parameterClass = insertStatement.getParameterClass();
        if (keyPropName != null && parameterClass != null) {
          resultClass = BeanProbe.getPropertyTypeForSetter(parameterClass, selectKeyStatement.getKeyProperty());
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
    resultMap = new AutoResultMap();
    resultMap.setId(selectKeyStatement.getId() + "-AutoResultMap");
    resultMap.setResultClass(resultClass);
    resultMap.setResource(selectKeyStatement.getResource());
    selectKeyStatement.setResultMap(resultMap);

    errorCtx.setMoreInfo(null);
    return selectKeyStatement;

  }

  private void processSqlStatement(Node n, GeneralStatement statement) {
    ErrorContext errorCtx = (ErrorContext) getContextObject(KEY_ERROR_CONTEXT);
    errorCtx.setActivity("processing an SQL statement");

    boolean isDynamic = false;
    DynamicSql dynamic = new DynamicSql();
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
    ErrorContext errorCtx = (ErrorContext) getContextObject(KEY_ERROR_CONTEXT);
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
          sqlText = parseInlineParameterMap(data.toString());
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
    ErrorContext errorCtx = (ErrorContext) getContextObject(KEY_ERROR_CONTEXT);
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

    ErrorContext errorCtx = (ErrorContext) getContextObject(KEY_ERROR_CONTEXT);
    errorCtx.setActivity("building an inline parameter map");

    ParameterMap parameterMap = statement.getParameterMap();

    errorCtx.setMoreInfo("Check the inline parameters.");
    if (parameterMap == null) {

      BasicParameterMap map;
      map = new BasicParameterMap();

      map.setId(statement.getId() + "-InlineParameterMap");
      map.setParameterClass(statement.getParameterClass());
      map.setResource(statement.getResource());
      statement.setParameterMap(map);

      SqlText sqlText = parseInlineParameterMap(statement.getParameterClass(), newSql);
      newSql = sqlText.getText();
      List mappingList = Arrays.asList(sqlText.getParameterMappings());

      map.setParameterMappingList(mappingList);

    }

    Sql sql = null;
    if (SimpleDynamicSql.isSimpleDynamicSql(newSql)) {
      sql = new SimpleDynamicSql(newSql);
    } else {
      sql = new StaticSql(newSql);
    }
    statement.setSql(sql);
  }

  public static SqlText parseInlineParameterMap(String sqlStatement) {
    return parseInlineParameterMap(null, sqlStatement);
  }

  private static SqlText parseInlineParameterMap(Class parameterClass, String sqlStatement) {

    String newSql = sqlStatement;

    List mappingList = new ArrayList();

    StringTokenizer parser = new StringTokenizer(sqlStatement, PARAMETER_TOKEN, true);
    StringBuffer newSqlBuffer = new StringBuffer();

    String token = null;
    String lastToken = null;
    while (parser.hasMoreTokens()) {
      token = parser.nextToken();
      if (PARAMETER_TOKEN.equals(lastToken)) {
        if (PARAMETER_TOKEN.equals(token)) {
          newSqlBuffer.append(PARAMETER_TOKEN);
          token = null;
        } else {
          if (token.indexOf(':') > -1) {
            StringTokenizer paramParser = new StringTokenizer(token, ":", true);
            int n1 = paramParser.countTokens();
            if (n1 == 3) {
              String name = paramParser.nextToken();
              paramParser.nextToken(); //ignore ":"
              String type = paramParser.nextToken();
              BasicParameterMapping mapping = new BasicParameterMapping();
              mapping.setPropertyName(name);
              mapping.setJdbcTypeName(type);
              TypeHandler handler;
              if (parameterClass == null) {
                handler = TypeHandlerFactory.getUnkownTypeHandler();
              } else {
                handler = resolveTypeHandler(parameterClass, name, null, null);
              }
              mapping.setTypeHandler(handler);
              mappingList.add(mapping);
            } else if (n1 >= 5) {
              String name = paramParser.nextToken();
              paramParser.nextToken(); //ignore ":"
              String type = paramParser.nextToken();
              paramParser.nextToken(); //ignore ":"
              String nullValue = paramParser.nextToken();
              while (paramParser.hasMoreTokens()) {
                nullValue = nullValue + paramParser.nextToken();
              }
              BasicParameterMapping mapping = new BasicParameterMapping();
              mapping.setPropertyName(name);
              mapping.setJdbcTypeName(type);
              mapping.setNullValue(nullValue);
              TypeHandler handler;
              if (parameterClass == null) {
                handler = TypeHandlerFactory.getUnkownTypeHandler();
              } else {
                handler = resolveTypeHandler(parameterClass, name, null, null);
              }
              mapping.setTypeHandler(handler);
              mappingList.add(mapping);
            } else {
              throw new SqlMapException("Incorrect inline parameter map format: " + token);
            }
          } else {
            BasicParameterMapping mapping = new BasicParameterMapping();
            mapping.setPropertyName(token);
            TypeHandler handler;
            if (parameterClass == null) {
              handler = TypeHandlerFactory.getUnkownTypeHandler();
            } else {
              handler = resolveTypeHandler(parameterClass, token, null, null);
            }
            mapping.setTypeHandler(handler);
            mappingList.add(mapping);
          }
          newSqlBuffer.append("?");
          token = parser.nextToken();
          if (!PARAMETER_TOKEN.equals(token)) {
            throw new SqlMapException("Unterminated inline parameter in mapped statement (" + "statement.getId()" + ").");
          }
          token = null;
        }
      } else {
        if (!PARAMETER_TOKEN.equals(token)) {
          newSqlBuffer.append(token);
        }
      }

      lastToken = token;
    }

    newSql = newSqlBuffer.toString();

    ParameterMapping[] mappingArray = (ParameterMapping[]) mappingList.toArray(new ParameterMapping[mappingList.size()]);

    SqlText sqlText = new SqlText();
    sqlText.setText(newSql);
    sqlText.setParameterMappings(mappingArray);
    return sqlText;
  }

  private void parseResultMap(Node n) {
    ErrorContext errorCtx = (ErrorContext) getContextObject(KEY_ERROR_CONTEXT);
    errorCtx.setActivity("building a result map");

    BasicResultMap map;
    map = new BasicResultMap();

    Properties attributes = parseAttributes(n);
    String id = applyNamespace(attributes.getProperty("id"));
    String resultClassName = attributes.getProperty("class");
    String extended = applyNamespace(attributes.getProperty("extends"));
    String xmlName = attributes.getProperty("xmlName");
    resultClassName = resolveAlias(resultClassName);

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
      BasicResultMap extendedResultMap = (BasicResultMap) getSubContextObject(GROUP_RESULT_MAP, extended);
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

        javaType = resolveAlias(javaType);

        errorCtx.setObjectId(propertyName + " mapping of the " + id + " result map");

        errorCtx.setMoreInfo("Check the result mapping property type or name.");
        TypeHandler handler = resolveTypeHandler(resultClass, propertyName, javaType, jdbcType);

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
          throw new RuntimeException("Error setting javaType on result mapping.  Cause: " + e);
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

    setSubContextObject(GROUP_RESULT_MAP, map.getId(), map);

    errorCtx.setMoreInfo(null);

    errorCtx.setObjectId(null);
  }

  private void parseParameterMap(Node n) {
    ErrorContext errorCtx = (ErrorContext) getContextObject(KEY_ERROR_CONTEXT);
    errorCtx.setActivity("building a parameter map");

    BasicParameterMap map;
    map = new BasicParameterMap();

    Properties attributes = parseAttributes(n);
    String id = applyNamespace(attributes.getProperty("id"));
    String parameterClassName = attributes.getProperty("class");
    parameterClassName = resolveAlias(parameterClassName);

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

        javaType = resolveAlias(javaType);

        errorCtx.setObjectId(propertyName + " mapping of the " + id + " parameter map");

        errorCtx.setMoreInfo("Check the parameter mapping property type or name.");
        TypeHandler handler = resolveTypeHandler(parameterClass, propertyName, javaType, jdbcType);

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
          throw new RuntimeException("Error setting javaType on parameter mapping.  Cause: " + e);
        }


        parameterMappingList.add(mapping);
      }
    }
    map.setParameterMappingList(parameterMappingList);

    setSubContextObject(GROUP_PARAMETER_MAP, map.getId(), map);

    errorCtx.setMoreInfo(null);
    errorCtx.setObjectId(null);
  }

  private void parseCacheModel(Node n) {
    ErrorContext errorCtx = (ErrorContext) getContextObject(KEY_ERROR_CONTEXT);
    errorCtx.setActivity("building a cache model");

    CacheModel model = new CacheModel();

    Properties attributes = parseAttributes(n);
    String id = applyNamespace (attributes.getProperty("id"));
    String type = attributes.getProperty("type");
    type = resolveAlias(type);

    String readOnly = attributes.getProperty("readOnly");
    if (readOnly != null && readOnly.length() > 0) {
      model.setReadOnly("true".equals(readOnly));
    } else {
      model.setReadOnly(true);
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

    ExtendedSqlMapClient client = (ExtendedSqlMapClient) getContextObject(KEY_SQL_MAP_CLIENT);
    if (client.getDelegate().isCacheModelsEnabled()) {
      setSubContextObject(GROUP_CACHE_MODEL, model.getId(), model);
    }

    errorCtx.setMoreInfo(null);
    errorCtx.setObjectId(null);
  }

  private String applyNamespace(String id) {
    String newId = id;
    String namespace = (String)getContextObject(KEY_CURRENT_NAMESPACE);
    if (namespace != null && namespace.length() > 0 && id != null && id.indexOf(".") < 0) {
      newId = namespace + "." + id;
    }
    return newId;

  }

  private void wireUpCacheModelListeners() {
    ExtendedSqlMapClient client = (ExtendedSqlMapClient) getContextObject(KEY_SQL_MAP_CLIENT);

    Map cacheModelMap = (Map) getContextObject(GROUP_CACHE_MODEL);

    if (cacheModelMap != null) {
      Iterator cacheNames = cacheModelMap.keySet().iterator();
      while (cacheNames.hasNext()) {
        String cacheName = (String) cacheNames.next();
        CacheModel cacheModel = (CacheModel) cacheModelMap.get(cacheName);
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
      dbf.setIgnoringElementContentWhitespace(true);
      dbf.setCoalescing(false);
      dbf.setExpandEntityReferences(false);

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

  private static TypeHandler resolveTypeHandler(Class clazz, String propertyName, String propertyType, String jdbcType) {
    TypeHandler handler = null;
    if (clazz == null) {
      handler = TypeHandlerFactory.getUnkownTypeHandler();
    } else if (XmlTypeMarker.class.isAssignableFrom(clazz)) {
      handler = TypeHandlerFactory.getTypeHandler(String.class, jdbcType);
    } else if (java.util.Map.class.isAssignableFrom(clazz)) {
      if (propertyType == null) {
        handler = TypeHandlerFactory.getTypeHandler(java.lang.Object.class, jdbcType);
      } else {
        try {
          Class javaClass = Resources.classForName(propertyType);
          handler = TypeHandlerFactory.getTypeHandler(javaClass, jdbcType);
        } catch (Exception e) {
          throw new SqlMapException("Error.  Could not set TypeHandler.  Cause: " + e, e);
        }
      }
    } else if (TypeHandlerFactory.getTypeHandler(clazz, jdbcType) != null) {
      handler = TypeHandlerFactory.getTypeHandler(clazz, jdbcType);
    } else {
      Class type = BeanProbe.getPropertyTypeForGetter(clazz, propertyName);
      handler = TypeHandlerFactory.getTypeHandler(type, jdbcType);
    }
    return handler;
  }

  private boolean isStatementNamespacesEnabled() {
    Boolean useStatementNamespaces = (Boolean) getContextObject(KEY_STATEMENT_NAMESPACES);
    return useStatementNamespaces != null && useStatementNamespaces.booleanValue();
  }

  private String resolveAlias(String string) {
    String newString = null;
    if (typeAliases.containsKey(string)) {
      newString = (String) typeAliases.get(string);
    }
    if (newString != null) {
      string = newString;
    }
    return string;
  }

  private void parseTypeAliasNode (Node child) {
    Properties prop = parseAttributes(child);
    String alias = prop.getProperty("alias");
    String type = prop.getProperty("type");
    putTypeAlias(alias, type);
  }

  private void putTypeAlias (String alias, String value) {
    if (typeAliases.containsKey(alias)) {
      throw new SqlMapException ("Error in XmlSqlMapClientBuilder.  Alias name conflict occurred.  The alias '" + alias + "' is already mapped to the value '"+typeAliases.get(alias)+"'.");
    }
    typeAliases.put(alias, value);
  }

  private void addNameValuePairProperty(Node child, Properties modelProperties) {
    Properties prop = parseAttributes(child);
    String name = prop.getProperty("name");
    String value = prop.getProperty("value");
    modelProperties.setProperty(name, value);
  }

  private void setContextObject(Object key, Object value) {
    contextPropertiesMap.put(key, value);
  }

  private Object getContextObject(Object key) {
    return contextPropertiesMap.get(key);
  }

  private void setSubContextObject(Object group, Object key, Object value) {
    Map map = (Map) contextPropertiesMap.get(group);
    if (map == null) {
      map = new HashMap();
      contextPropertiesMap.put(group, map);
    }
    map.put(key, value);
  }

  private Object getSubContextObject(Object group, Object key) {
    Map map = (Map) contextPropertiesMap.get(group);
    Object result = null;
    if (map != null) {
      result = map.get(key);
    }
    return result;
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

    Properties props = (Properties) contextPropertiesMap.get(KEY_GLOBAL_PROPS);

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
    /** Error handler output goes here */
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

