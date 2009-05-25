package org.apache.ibatis.builder.xml;

import org.apache.ibatis.builder.*;
import org.apache.ibatis.datasource.DataSourceFactory;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.parsing.*;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.reflection.*;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.type.TypeHandler;

import java.io.Reader;
import java.util.*;

public class XMLMapperConfigParser extends BaseParser {

  private boolean parsed;

  private XPathParser parser;

  private String environment;

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
    this.environment = environment;
    this.parser = new XPathParser(reader,true, new XMLMapperEntityResolver(),props);
  }

  public Configuration parse() {
    if (parsed) {
      throw new ParserException("Each MapperConfigParser can only be used once.");
    }
    parsed = true;
    try {
      typeAliasesElement(parser.evalNode("/configuration/typeAliases"));
      pluginElement(parser.evalNode("/configuration/plugins"));
      objectFactoryElement(parser.evalNode("/configuration/objectFactory"));
      propertiesElement(parser.evalNode("/configuration/properties"));
      settingsElement(parser.evalNode("/configuration/settings"));
      environmentsElement(parser.evalNode("/configuration/environments"));
      typeHandlerElement(parser.evalNode("/configuration/typeHandlers"));
      mapperElement(parser.evalNode("/configuration/mappers"));
    } catch (Exception e) {
      throw new RuntimeException("Description. Cause: " + e, e);
    }

    return configuration;
  }

  private void typeAliasesElement(XNode parent) {
    for (XNode child : parent.getChildren()) {
      String alias = child.getStringAttribute("alias");
      String type = child.getStringAttribute("type");
      typeAliasRegistry.registerAlias(alias, type);
    }
  }

  private void pluginElement(XNode parent) throws Exception {
    for (XNode child : parent.getChildren()) {
      String interceptor = child.getStringAttribute("interceptor");
      Properties properties = child.getChildrenAsProperties();
      Interceptor interceptorInstance = (Interceptor) resolveClass(interceptor).newInstance();
      interceptorInstance.setProperties(properties);
      configuration.addInterceptor(interceptorInstance);
    }
  }


  private void objectFactoryElement(XNode context) throws Exception {
    String type = context.getStringAttribute("type");
    Properties properties = context.getChildrenAsProperties();
    ObjectFactory factory = (ObjectFactory) resolveClass(type).newInstance();
    factory.setProperties(properties);
    configuration.setObjectFactory(factory);
  }

  private void propertiesElement(XNode context) throws Exception {
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

  private void settingsElement(XNode context) throws Exception {
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

  private void environmentsElement(XNode context) throws Exception {
    if (environment == null) {
      environment = context.getStringAttribute("default");
    }
    for (XNode child : context.getChildren()) {
      String id = child.getStringAttribute("id");
      if (isSpecifiedEnvironment(id)) {
        TransactionFactory txFactory = transactionManagerElement(child.evalNode("transactionManager"));
        DataSourceFactory dsFactory = dataSourceElement(child.evalNode("dataSource"));
        Environment.Builder environmentBuilder = new Environment.Builder(id, txFactory, dsFactory.getDataSource());
        configuration.setEnvironment(environmentBuilder.build());
      }
    }
  }

  private TransactionFactory transactionManagerElement(XNode context) throws Exception {
    String type = context.getStringAttribute("type");
    Properties props = context.getChildrenAsProperties();
    TransactionFactory factory = (TransactionFactory) resolveClass(type).newInstance();
    factory.setProperties(props);
    return factory;
  }

  private DataSourceFactory dataSourceElement(XNode context) throws Exception {
    String type = context.getStringAttribute("type");
    Properties props = context.getChildrenAsProperties();
    DataSourceFactory factory = (DataSourceFactory) resolveClass(type).newInstance();
    factory.setProperties(props);
    return factory;
  }


  private void typeHandlerElement(XNode parent) throws Exception {
    for (XNode child : parent.getChildren()) {
      String javaType = child.getStringAttribute("javaType");
      String jdbcType = child.getStringAttribute("jdbcType");
      String handler = child.getStringAttribute("handler");

      Class javaTypeClass = resolveClass(javaType);
      TypeHandler typeHandlerInstance = (TypeHandler) resolveClass(handler).newInstance();

      if (jdbcType == null) {
        typeHandlerRegistry.register(javaTypeClass, typeHandlerInstance);
      } else {
        typeHandlerRegistry.register(javaTypeClass, resolveJdbcType(jdbcType), typeHandlerInstance);
      }
    }
  }


  private void mapperElement(XNode parent) throws Exception {
    for (XNode child : parent.getChildren()) {
      String resource = child.getStringAttribute("resource");
      String url = child.getStringAttribute("url");
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
  }

  private boolean isSpecifiedEnvironment(String id) {
    if (environment == null) {
      throw new ParserException("No environment specified.");
    } else if (id == null) {
      throw new ParserException("Environment requires an id attribute.");
    } else if (environment.equals(id)) {
      return true;
    }
    return false;
  }
}
