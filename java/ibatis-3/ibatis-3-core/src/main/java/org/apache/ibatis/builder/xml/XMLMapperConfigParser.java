package org.apache.ibatis.builder.xml;

import org.apache.ibatis.datasource.DataSourceFactory;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.builder.*;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.reflection.*;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.parsing.*;

import java.io.Reader;
import java.util.*;

public class XMLMapperConfigParser extends BaseParser {

  private boolean parsed;

  private Reader reader;
  private NodeletParser parser;

  private String environment;
  private Environment.Builder environmentBuilder;

  public XMLMapperConfigParser(Reader reader) {
    this(reader, null, null);
  }

  public XMLMapperConfigParser(Reader reader, String environment) {
    this(reader, environment, null);
  }

  public XMLMapperConfigParser(Reader reader, String environment, Properties props) {
    super(new Configuration());

    ErrorContext.instance().resource("SQL Mapper Configuration");

    this.configuration.setVariables(props);

    this.parsed = false;
    this.reader = reader;
    this.environment = environment;

    this.parser = new NodeletParser();
    this.parser.addNodeletHandler(this);
    this.parser.setValidation(true);
    this.parser.setVariables(props);
    this.parser.setEntityResolver(new XMLMapperEntityResolver());
  }

  public Configuration parse() {
    assert reader != null;
    assert parser != null;
    assert configuration != null;
    assert typeAliasRegistry != null;
    assert typeHandlerRegistry != null;
    if (parsed) {
      throw new ParserException("Each MapperConfigParser can only be used once.");
    }
    parsed = true;
    parser.parse(reader);
    return configuration;
  }

  //  <typeAlias alias="" type=""/>
  @Nodelet("/configuration/typeAliases/typeAlias")
  public void typeAliasElement(NodeletContext context) throws Exception {
    String alias = context.getStringAttribute("alias");
    String type = context.getStringAttribute("type");
    typeAliasRegistry.registerAlias(alias, type);
  }

  //  <plugin interceptor="">
  //    <property name="" value=""/>
  @Nodelet("/configuration/plugins/plugin")
  public void pluginElement(NodeletContext context) throws Exception {
    String interceptor = context.getStringAttribute("interceptor");
    Properties properties = context.getChildrenAsProperties();
    Interceptor interceptorInstance = (Interceptor) resolveClass(interceptor).newInstance();
    interceptorInstance.setProperties(properties);
    configuration.addInterceptor(interceptorInstance);
  }

  //  <objectFactory type="">
  //    <property name="" value=""/>
  @Nodelet("/configuration/objectFactory")
  public void objectFactoryElement(NodeletContext context) throws Exception {
    String type = context.getStringAttribute("type");
    Properties properties = context.getChildrenAsProperties();
    ObjectFactory factory = (ObjectFactory) resolveClass(type).newInstance();
    factory.setProperties(properties);
    configuration.setObjectFactory(factory);
  }

  //  <settings url="" resource="">
  //    <setting name="" value=""/>
  @Nodelet("/configuration/properties")
  public void propertiesElement(NodeletContext context) throws Exception {
    Properties defaults = context.getChildrenAsProperties();
    String resource = context.getStringAttribute("resource");
    String url = context.getStringAttribute("url");
    if (resource != null && url != null) {
      throw new ParserException("The properties element cannot specify both a URL and a resource based property file reference.  Please specify one or the other.");
    }
    if (resource != null) {
      defaults.putAll(Resources.getResourceAsProperties(resource));
    } else if (url != null) {
      defaults.putAll(Resources.getUrlAsProperties(url));
    }
    Properties vars = configuration.getVariables();
    if (vars != null) {
      defaults.putAll(vars);
    }
    parser.setVariables(defaults);
    configuration.setVariables(defaults);
  }

  //  <settings>
  //    <setting name="" value=""/>
  @Nodelet("/configuration/settings")
  public void settingsElement(NodeletContext context) throws Exception {
    Properties props = context.getChildrenAsProperties();
    // Check that all settings are known to the configuration class
    for (Map.Entry entry : props.entrySet()) {
      MetaClass metaConfig = MetaClass.forClass(Configuration.class);
      if (!metaConfig.hasSetter((String) entry.getKey())) {
        throw new ParserException("The setting " + entry.getKey() + " is not known.  Make sure you spelled it correctly (case sensitive).");
      }
    }
    configuration.setCacheEnabled(booleanValueOf(props.getProperty("cacheEnabled"), true));
    configuration.setLazyLoadingEnabled(booleanValueOf(props.getProperty("lazyLoadingEnabled"), true));
    configuration.setMultipleResultSetsEnabled(booleanValueOf(props.getProperty("multipleResultSetsEnabled"), true));
    configuration.setUseColumnLabel(booleanValueOf(props.getProperty("useColumnLabel"), true));
    configuration.setEnhancementEnabled(booleanValueOf(props.getProperty("enhancementEnabled"), false));
    configuration.setUseGeneratedKeys(booleanValueOf(props.getProperty("useGeneratedKeys"), false));
    configuration.setDefaultExecutorType(ExecutorType.valueOf(stringValueOf(props.getProperty("defaultExecutorType"), "SIMPLE")));
    configuration.setDefaultStatementTimeout(integerValueOf(props.getProperty("defaultStatementTimeout"), null));
  }

  //  <environments default="development">
  @Nodelet("/configuration/environments")
  public void environmentsElement(NodeletContext context) throws Exception {
    if (environment == null) {
      environment = context.getStringAttribute("default");
    }
  }

  //  <environment id="development">
  @Nodelet("/configuration/environments/environment")
  public void environmentElement(NodeletContext context) throws Exception {
    String id = context.getStringAttribute("id");
    environmentBuilder = new Environment.Builder(id, null, null);
  }

  //  <transactionManager type="JDBC|JTA|EXTERNAL">
  //    <property name="" value=""/>
  @Nodelet("/configuration/environments/environment/transactionManager")
  public void transactionManagerElement(NodeletContext context) throws Exception {
    if (isSpecifiedEnvironment()) {
      String type = context.getStringAttribute("type");
      Properties props = context.getChildrenAsProperties();

      TransactionFactory factory = (TransactionFactory) resolveClass(type).newInstance();
      factory.setProperties(props);

      environmentBuilder.transactionFactory(factory);
    }
  }

  //  <dataSource type="POOLED|UNPOOLED|JNDI">
  //    <property name="" value=""/>
  @Nodelet("/configuration/environments/environment/dataSource")
  public void dataSourceElement(NodeletContext context) throws Exception {
    if (isSpecifiedEnvironment()) {
      String type = context.getStringAttribute("type");
      Properties props = context.getChildrenAsProperties();

      DataSourceFactory factory = (DataSourceFactory) resolveClass(type).newInstance();
      factory.setProperties(props);

      environmentBuilder.dataSource(factory.getDataSource());
    }
  }

  //  </environment>
  @Nodelet("/configuration/environments/environment/end()")
  public void environmentElementEnd(NodeletContext context) throws Exception {
    if (isSpecifiedEnvironment()) {
      configuration.setEnvironment(environmentBuilder.build());
    }
  }

  //  <typeHandler javaType="" jdbcType="" handler=""/>
  @Nodelet("/configuration/typeHandlers/typeHandler")
  public void typeHandlerElement(NodeletContext context) throws Exception {
    String javaType = context.getStringAttribute("javaType");
    String jdbcType = context.getStringAttribute("jdbcType");
    String handler = context.getStringAttribute("handler");

    Class javaTypeClass = resolveClass(javaType);
    TypeHandler typeHandlerInstance = (TypeHandler) resolveClass(handler).newInstance();

    if (jdbcType == null) {
      typeHandlerRegistry.register(javaTypeClass, typeHandlerInstance);
    } else {
      typeHandlerRegistry.register(javaTypeClass, resolveJdbcType(jdbcType), typeHandlerInstance);
    }
  }

  //  <mapper url="" resource="resources/AnotherMapper.xml"/>
  @Nodelet("/configuration/mappers/mapper")
  public void mapperElement(NodeletContext context) throws Exception {
    String resource = context.getStringAttribute("resource");
    String url = context.getStringAttribute("url");
    Reader reader;
    if (resource != null && url == null) {
      ErrorContext.instance().resource(resource);
      reader = Resources.getResourceAsReader(resource);
      XMLMapperParser mapperParser = new XMLMapperParser(reader, configuration, resource);
      mapperParser.parse();
    } else if (url != null && resource == null) {
      ErrorContext.instance().resource(url);
      reader = Resources.getUrlAsReader(url);
      XMLMapperParser mapperParser = new XMLMapperParser(reader, configuration, url);
      mapperParser.parse();
    } else {
      throw new ParserException("A mapper element may only specify a url or resource, but not both.");
    }
  }

  private boolean isSpecifiedEnvironment() {
    if (environment == null) {
      throw new ParserException("No environment specified.");
    } else if (environmentBuilder.id() == null) {
      throw new ParserException("Environment requires an id attribute.");
    } else if (environment.equals(environmentBuilder.id())) {
      return true;
    }
    return false;
  }
}
