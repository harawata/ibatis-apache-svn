package org.apache.ibatis.monarch.builder;

import org.apache.ibatis.xml.*;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.type.*;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.reflection.*;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.monarch.environment.Environment;
import org.apache.ibatis.transaction.TransactionManagerFactory;
import org.apache.ibatis.datasource.DataSourceFactory;

import java.io.Reader;
import java.util.*;

public class MapperConfigParser extends BaseParser {

  protected Reader reader;
  protected NodeletParser parser;

  private Environment.Builder environmentBuilder;

  public MapperConfigParser(Reader reader, Properties props) {
    this.reader = reader;

    this.configuration = new MonarchConfiguration();
    this.configuration.setVariables(props);
    this.typeAliasRegistry = this.configuration.getTypeAliasRegistry();
    this.typeHandlerRegistry = this.configuration.getTypeHandlerRegistry();

    this.parser = new NodeletParser();
    this.parser.addNodeletHandler(this);
    this.parser.setVariables(props);
    this.parser.setEntityResolver(new MapperEntityResolver());
  }

  public void parse() {
    assert reader != null;
    assert parser != null;
    assert configuration != null;
    assert typeAliasRegistry != null;
    assert typeHandlerRegistry != null;
    parser.parse(reader);
  }

  public MonarchConfiguration getConfiguration() {
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
    ObjectFactory factory = (ObjectFactory)resolveClass(type).newInstance();
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
      throw new BuilderException("The properties element cannot specify both a URL and a resource based property file reference.  Please specify one or the other.");
    }
    if (resource != null) {
      defaults.putAll(Resources.getResourceAsProperties(resource));
    } else if (url != null){
      defaults.putAll(Resources.getUrlAsProperties(url));
    }
    Properties vars = configuration.getVariables();
    if (vars != null) {
      defaults.putAll(vars);
    }
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
      if (!metaConfig.hasSetter((String)entry.getKey())) {
        throw new BuilderException("The setting " + entry.getKey() + " is not known.  Make sure you spelled it correctly (case sensitive).");
      }
    }
    configuration.setCacheEnabled(booleanValueOf(props.getProperty("cacheEnabled"),true));
    configuration.setLazyLoadingEnabled(booleanValueOf(props.getProperty("lazyLoadingEnabled"),true));
    configuration.setMultipleResultSetsEnabled(booleanValueOf(props.getProperty("multipleResultSetsEnabled"),true));
    configuration.setUseColumnLabel(booleanValueOf(props.getProperty("useColumnLabel"),true));
    configuration.setEnhancementEnabled(booleanValueOf(props.getProperty("enhancementEnabled"),false));
    configuration.setGeneratedKeysEnabled(booleanValueOf(props.getProperty("generatedKeysEnabled"),false));
    configuration.setDefaultExecutorType(ExecutorType.valueOf(stringValueOf(props.getProperty("defaultExecutorType"),"SIMPLE")));
    configuration.setDefaultStatementTimeout(integerValueOf(props.getProperty("defaultStatementTimeout"),null));
  }

  //  <environments default="development">
  @Nodelet("/configuration/environments")
  public void environmentsElement(NodeletContext context) throws Exception {
    String defaultEnv = context.getStringAttribute("default","default");
    configuration.setDefaultEnvironment(defaultEnv);
  }

  //  <environment id="development">
  @Nodelet("/configuration/environments/environment")
  public void environmentElement(NodeletContext context) throws Exception {
    String id = context.getStringAttribute("id","default");
    environmentBuilder = new Environment.Builder(id);
  }

  //  <transactionManager type="JDBC|JTA|EXTERNAL">
  //    <property name="" value=""/>
  @Nodelet("/configuration/environments/environment/transactionManager")
  public void transactionManagerElement(NodeletContext context) throws Exception {
    String type = context.getStringAttribute("type");
    Properties props = context.getChildrenAsProperties();

    TransactionManagerFactory factory = (TransactionManagerFactory) resolveClass(type).newInstance();
    factory.setProperties(props);

    environmentBuilder.transactionManager(factory.getTransactionManager());
  }

  //  <dataSource type="POOLED|UNPOOLED|JNDI">
  //    <property name="" value=""/>
  @Nodelet("/configuration/environments/environment/dataSource")
  public void dataSourceElement(NodeletContext context) throws Exception {
    String type = context.getStringAttribute("type");
    Properties props = context.getChildrenAsProperties();

    DataSourceFactory factory = (DataSourceFactory) resolveClass(type).newInstance();
    factory.setProperties(props);

    environmentBuilder.dataSource(factory.getDataSource());
  }

  //  </environment>
  @Nodelet("/configuration/environments/environment/end()")
  public void environmentClosingElement(NodeletContext context) throws Exception {
    configuration.addEnvironment(environmentBuilder.build());
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
      reader = Resources.getResourceAsReader(resource);
    } else if (url != null && resource == null) {
      reader = Resources.getUrlAsReader(url);
    } else {
      throw new BuilderException("A mapper element may only specify a url or resource, but not both.");
    }
    MapperParser mapperParser = new MapperParser(reader,configuration);
    mapperParser.parse();
  }

}
