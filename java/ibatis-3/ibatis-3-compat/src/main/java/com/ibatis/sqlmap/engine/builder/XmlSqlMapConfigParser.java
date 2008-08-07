package com.ibatis.sqlmap.engine.builder;

import com.ibatis.sqlmap.client.SqlMapException;
import com.ibatis.sqlmap.client.extensions.TypeHandlerCallback;
import com.ibatis.sqlmap.engine.datasource.DataSourceFactory;
import com.ibatis.sqlmap.engine.transaction.*;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.reflection.*;
import org.apache.ibatis.type.*;
import org.apache.ibatis.xml.*;

import java.io.Reader;
import java.util.*;

public class XmlSqlMapConfigParser {

  private Reader reader;
  private NodeletParser parser = new NodeletParser();

  private Ibatis2Configuration config = new Ibatis2Configuration();
  private Properties dataSourceProps = new Properties();
  private Properties transactionManagerProps = new Properties();
  private boolean useStatementNamespaces;

  private Map<String, NodeletContext> sqlFragments = new HashMap<String, NodeletContext>();

  public XmlSqlMapConfigParser(Reader reader) {
    this.reader = reader;
    this.parser.addNodeletHandler(this);
    this.useStatementNamespaces = false;
  }

  public XmlSqlMapConfigParser(Reader reader, Properties props) {
    this(reader);
    this.config.setVariables(props);
    this.parser.setVariables(props);
    this.parser.setEntityResolver(new SqlMapEntityResolver());
  }

  public void parse() {
    parser.parse(reader);
  }

  public boolean hasSqlFragment(String id) {
    return sqlFragments.containsKey(id);
  }

  public NodeletContext getSqlFragment(String id) {
    return sqlFragments.get(id);
  }

  public void addSqlFragment(String id, NodeletContext context) {
    sqlFragments.put(id, context);
  }

  public Ibatis2Configuration getConfiguration() {
    return config;
  }

  public boolean isUseStatementNamespaces() {
    return useStatementNamespaces;
  }

  @Nodelet("/sqlMapConfig/properties")
  public void sqlMapConfigproperties(NodeletContext context) throws Exception {
    String resource = context.getStringAttribute("resource");
    String url = context.getStringAttribute("url");
    Properties fileVariables;
    if (resource != null) {
      fileVariables = Resources.getResourceAsProperties(resource);
    } else if (url != null) {
      fileVariables = Resources.getUrlAsProperties(url);
    } else {
      throw new RuntimeException("The properties element requires either a resource or a url attribute.");
    }
    // Override file variables with those passed in programmatically
    Properties passedVariables = config.getVariables();
    if (passedVariables != null) {
      fileVariables.putAll(passedVariables);
    }
    config.setVariables(fileVariables);
    parser.setVariables(fileVariables);
  }

  @Nodelet("/sqlMapConfig/settings")
  public void sqlMapConfigsettings(NodeletContext context) throws Exception {
    boolean classInfoCacheEnabled = context.getBooleanAttribute("classInfoCacheEnabled", true);
    MetaClass.setClassCacheEnabled(classInfoCacheEnabled);

    boolean lazyLoadingEnabled = context.getBooleanAttribute("lazyLoadingEnabled", true);
    config.setLazyLoadingEnabled(lazyLoadingEnabled);

    boolean enhancementEnabled = context.getBooleanAttribute("enhancementEnabled", true);
    config.setEnhancementEnabled(enhancementEnabled);

    boolean statementCachingEnabled = context.getBooleanAttribute("statementCachingEnabled", true);
    config.setStatementCachingEnabled(statementCachingEnabled);

    boolean batchUpdatesEnabled = context.getBooleanAttribute("batchUpdatesEnabled", true);
    config.setBatchUpdatesEnabled(batchUpdatesEnabled);

    boolean cacheModelsEnabled = context.getBooleanAttribute("cacheModelsEnabled", true);
    config.setCacheEnabled(cacheModelsEnabled);

    boolean useColumnLabel = context.getBooleanAttribute("useColumnLabel", true);
    config.setUseColumnLabel(useColumnLabel);

    boolean forceMultipleResultSetSupport = context.getBooleanAttribute("forceMultipleResultSetSupport", true);
    config.setMultipleResultSetsEnabled(forceMultipleResultSetSupport);

    useStatementNamespaces = context.getBooleanAttribute("useStatementNamespaces", false);

    Integer defaultTimeout = context.getIntAttribute("defaultStatementTimeout");
    config.setDefaultStatementTimeout(defaultTimeout);
  }

  @Nodelet("/sqlMapConfig/typeAlias")
  public void sqlMapConfigtypeAlias(NodeletContext context) throws Exception {
    String alias = context.getStringAttribute("alias");
    String type = context.getStringAttribute("type");
    config.getTypeAliasRegistry().registerAlias(alias, type);
  }

  @Nodelet("/sqlMapConfig/typeHandler")
  public void sqlMapConfigtypeHandler(NodeletContext context) throws Exception {
    String jdbcType = context.getStringAttribute("jdbcType");
    String javaType = context.getStringAttribute("javaType");
    String callback = context.getStringAttribute("callback");

    javaType = config.getTypeAliasRegistry().resolveAlias(javaType);
    callback = config.getTypeAliasRegistry().resolveAlias(callback);

    if (javaType != null && callback != null) {
      JdbcType jdbcTypeEnum = JdbcType.valueOf(jdbcType);
      Class javaTypeClass = Resources.classForName(javaType);
      Class callbackClass = Resources.classForName(callback);
      Object o = callbackClass.newInstance();
      if (o instanceof TypeHandlerCallback) {
        TypeHandler typeHandler = new TypeHandlerCallbackAdapter((TypeHandlerCallback) o);
        config.getTypeHandlerRegistry().register(javaTypeClass, jdbcTypeEnum, typeHandler);
      }
    }
  }

  @Nodelet("/sqlMapConfig/transactionManager/end()")
  public void sqlMapConfigtransactionManagerend(NodeletContext context) throws Exception {
    String type = context.getStringAttribute("type");
    type = config.getTypeAliasRegistry().resolveAlias(type);
    Class txClass = Class.forName(type);
    boolean commitRequired = context.getBooleanAttribute("commitRequired", false);

    TransactionConfig txConfig = (TransactionConfig) txClass.newInstance();
    txConfig.setDataSource(config.getDataSource());
    txConfig.setProperties(transactionManagerProps);
    txConfig.setForceCommit(commitRequired);
    config.setTransactionManager(new TransactionManager(config, txConfig));
  }

  @Nodelet("/sqlMapConfig/transactionManager/property")
  public void sqlMapConfigtransactionManagerproperty(NodeletContext context) throws Exception {
    String name = context.getStringAttribute("name");
    String value = context.getStringAttribute("value");
    transactionManagerProps.setProperty(name, value);
  }

  @Nodelet("/sqlMapConfig/transactionManager/dataSource/property")
  public void sqlMapConfigtransactionManagerdataSourceproperty(NodeletContext context) throws Exception {
    String name = context.getStringAttribute("name");
    String value = context.getStringAttribute("value");
    dataSourceProps.setProperty(name, value);
  }

  @Nodelet("/sqlMapConfig/transactionManager/dataSource/end()")
  public void sqlMapConfigtransactionManagerdataSourceend(NodeletContext context) throws Exception {
    String type = context.getStringAttribute("type");
    type = config.getTypeAliasRegistry().resolveAlias(type);
    Class dataSourceClass = Class.forName(type);
    DataSourceFactory dsFactory = (DataSourceFactory) dataSourceClass.newInstance();
    dsFactory.initialize(dataSourceProps);
    config.setDataSource(dsFactory.getDataSource());
  }

  @Nodelet("/sqlMapConfig/resultObjectFactory")
  public void sqlMapConfigresultObjectFactory(NodeletContext context) throws Exception {
    String type = context.getStringAttribute("type");
    Class factoryClass = Class.forName(type);
    ObjectFactory factory = (ObjectFactory) factoryClass.newInstance();
    config.setObjectFactory(factory);
  }

  @Nodelet("/sqlMapConfig/resultObjectFactory/property")
  public void sqlMapConfigresultObjectFactoryproperty(NodeletContext context) throws Exception {
    String name = context.getStringAttribute("name");
    String value = context.getStringAttribute("value");
    config.getObjectFactory().setProperty(name, value);
  }

  @Nodelet("/sqlMapConfig/sqlMap")
  public void sqlMapConfigsqlMap(NodeletContext context) throws Exception {
    String resource = context.getStringAttribute("resource");
    String url = context.getStringAttribute("url");

    Reader reader = null;
    if (resource != null) {
      reader = Resources.getResourceAsReader(resource);
    } else if (url != null) {
      reader = Resources.getUrlAsReader(url);
    } else {
      throw new SqlMapException("The sqlMap element requires either a resource or a url attribute.");
    }
    new XmlSqlMapParser(this, reader).parse();
  }

}
