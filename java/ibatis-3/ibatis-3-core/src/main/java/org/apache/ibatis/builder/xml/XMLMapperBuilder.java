package org.apache.ibatis.builder.xml;

import org.apache.ibatis.builder.*;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.parsing.*;
import org.apache.ibatis.type.JdbcType;

import java.io.Reader;
import java.util.*;

public class XMLMapperBuilder extends BaseBuilder {

  private Reader reader;
  private NodeEventParser parser;
  private SequentialMapperBuilder sequentialBuilder;

  private Map<String, XNode> sqlFragments = new HashMap<String, XNode>();

  public XMLMapperBuilder(Reader reader, Configuration configuration, String resource, String namespace) {
    this(reader, configuration, resource);
    this.sequentialBuilder.namespace(namespace);
  }

  public XMLMapperBuilder(Reader reader, Configuration configuration, String resource) {
    super(configuration);
    this.sequentialBuilder = new SequentialMapperBuilder(configuration, resource);
    this.reader = reader;
    this.parser = new NodeEventParser();
    this.parser.addNodeletHandler(this);
    this.parser.setValidation(true);
    this.parser.setVariables(configuration.getVariables());
    this.parser.setEntityResolver(new XMLMapperEntityResolver());
  }

  public void parse() {
    assert reader != null;
    assert parser != null;
    assert configuration != null;
    assert typeAliasRegistry != null;
    assert typeHandlerRegistry != null;
    parser.parse(reader);
    bindMapperForNamespace();
  }

  //  <configuration namespace="com.domain.MapperClass" />
  @NodeEvent("/mapper")
  public void configurationElement(XNode context) throws Exception {
    String namespace = context.getStringAttribute("namespace");
    sequentialBuilder.namespace(namespace);
  }

  //  <cache type="LRU" flushInterval="3600000" size="1000" readOnly="false" />
  @NodeEvent("/mapper/cache-ref")
  public void cacheRefElement(XNode context) throws Exception {
    sequentialBuilder.cacheRef(context.getStringAttribute("namespace"));
  }

  //  <cache type="LRU" flushInterval="3600000" size="1000" readOnly="false" />
  @NodeEvent("/mapper/cache")
  public void cacheElement(XNode context) throws Exception {
    String type = context.getStringAttribute("type", "PERPETUAL");
    type = typeAliasRegistry.resolveAlias(type);
    Class typeClass = Class.forName(type);
    String eviction = context.getStringAttribute("eviction", "LRU");
    eviction = typeAliasRegistry.resolveAlias(eviction);
    Class evictionClass = Class.forName(eviction);
    Long flushInterval = context.getLongAttribute("flushInterval");
    Integer size = context.getIntAttribute("size");
    boolean readOnly = context.getBooleanAttribute("readOnly", false);
    Properties props = context.getChildrenAsProperties();
    sequentialBuilder.cache(typeClass, evictionClass, flushInterval, size, readOnly, props);
  }

  //  <parameterMap id="" type="">
  @NodeEvent("/mapper/parameterMap")
  public void parameterMapElement(XNode context) throws Exception {
    String id = context.getStringAttribute("id");
    String type = context.getStringAttribute("type");
    Class parameterClass = resolveClass(type);
    sequentialBuilder.parameterMapStart(id, parameterClass);
  }

  //  <parameterMap id="" type="">
  //    <param property="id" javaType="" jdbcType="" typeHandler="" mode="" scale="" resultMap=""/>
  @NodeEvent("/mapper/parameterMap/parameter")
  public void parameterMapParameterElement(XNode context) throws Exception {
    String property = context.getStringAttribute("property");
    String javaType = context.getStringAttribute("javaType");
    String jdbcType = context.getStringAttribute("jdbcType");
    String resultMap = context.getStringAttribute("resultMap");
    String mode = context.getStringAttribute("mode");
    String typeHandler = context.getStringAttribute("typeHandler");
    Integer numericScale = context.getIntAttribute("numericScale", null);
    ParameterMode modeEnum = resolveParameterMode(mode);
    Class javaTypeClass = resolveClass(javaType);
    JdbcType jdbcTypeEnum = resolveJdbcType(jdbcType);
    Class typeHandlerClass = resolveClass(typeHandler);
    sequentialBuilder.parameterMapping(property, javaTypeClass, jdbcTypeEnum, resultMap, modeEnum, typeHandlerClass, numericScale);
  }

  //  </parameterMap>
  @NodeEvent("/mapper/parameterMap/end()")
  public void parameterMapClosingElement(XNode context) throws Exception {
    sequentialBuilder.parameterMapEnd();
  }

  //  <resultMap id="" type="" extends="">
  @NodeEvent("/mapper/resultMap")
  public void resultMapElement(XNode context) throws Exception {
    ErrorContext.instance().activity("processing " + context.getValueBasedIdentifier());
    String id = context.getStringAttribute("id");
    String type = context.getStringAttribute("type");
    String extend = context.getStringAttribute("extends");
    if (id == null) {
      id = context.getValueBasedIdentifier();
    }
    Class typeClass = resolveClass(type);
    processChildrenAsResultMap(context);
    sequentialBuilder.resultMapStart(id, typeClass, extend);
    List<XNode> resultChildren = context.getChildren();
    for (XNode resultChild : resultChildren) {
      if ("constructor".equals(resultChild.getName())) {
        processConstructorElement(resultChild);
      } else if ("discriminator".equals(resultChild.getName())) {
        processDiscriminatorElement(resultChild);
      } else {
        ArrayList<ResultFlag> flags = new ArrayList<ResultFlag>();
        if ("id".equals(resultChild.getName())) {
          flags.add(ResultFlag.ID);
        }
        buildResultMappingFromContext(resultChild, flags);
      }
    }
    sequentialBuilder.resultMapEnd();
  }

  private void processChildrenAsResultMap(XNode resultChild) throws Exception {
    List<String> acceptedResultMapElements = Arrays.asList(new String[]{"association","collection","case"});
    for (XNode arg : resultChild.getChildren()) {
      if (acceptedResultMapElements.contains(resultChild.getName()) && arg.getChildren().size() > 0) {
        resultMapElement(arg);
      }
    }
  }

  private void processConstructorElement(XNode resultChild) throws Exception {
    List<XNode> argChildren = resultChild.getChildren();
    for (XNode argChild : argChildren) {
      ArrayList<ResultFlag> flags = new ArrayList<ResultFlag>();
      flags.add(ResultFlag.CONSTRUCTOR);
      if ("idArg".equals(argChild.getName())) {
        flags.add(ResultFlag.ID);
      }
      buildResultMappingFromContext(argChild, flags);
    }
  }

  public void processDiscriminatorElement(XNode context) throws Exception {
    String column = context.getStringAttribute("column");
    String javaType = context.getStringAttribute("javaType");
    String jdbcType = context.getStringAttribute("jdbcType");
    String typeHandler = context.getStringAttribute("typeHandler");
    Class javaTypeClass = resolveClass(javaType);
    Class typeHandlerClass = resolveClass(typeHandler);
    JdbcType jdbcTypeEnum = resolveJdbcType(jdbcType);
    sequentialBuilder.resultMapDiscriminatorStart(column, javaTypeClass, jdbcTypeEnum, typeHandlerClass);
    for (XNode caseChild : context.getChildren()) {
      processChildrenAsResultMap(caseChild);
      String value = caseChild.getStringAttribute("value");
      String resultMap = caseChild.getStringAttribute("resultMap");
      sequentialBuilder.resultMapDiscriminatorCase(value, resultMap);
    }
    sequentialBuilder.resultMapDiscriminatorEnd();
  }

  //  <sql id="">
  @NodeEvent("/mapper/sql")
  public void sqlElement(XNode context) throws Exception {
    String id = context.getStringAttribute("id");

    sqlFragments.put(id, context);
  }

  //  <select ...>
  @NodeEvent("/mapper/select")
  public void selectElement(XNode context) throws Exception {
    buildStatementFromContext(context);
  }

  //  <insert ...>
  @NodeEvent("/mapper/insert")
  public void insertElement(XNode context) throws Exception {
    buildStatementFromContext(context);
  }

  //  <update ...>
  @NodeEvent("/mapper/update")
  public void updateElement(XNode context) throws Exception {
    buildStatementFromContext(context);
  }

  //  <delete ...>
  @NodeEvent("/mapper/delete")
  public void deleteElement(XNode context) throws Exception {
    buildStatementFromContext(context);
  }

  public XNode getSqlFragment(String refid) {
    return sqlFragments.get(refid);
  }

  private void buildStatementFromContext(XNode context) {
    final XMLStatementBuilder statementParser = new XMLStatementBuilder(configuration, sequentialBuilder, this);
    statementParser.parseStatementNode(context);
  }

  private void buildResultMappingFromContext(XNode context, ArrayList<ResultFlag> flags) {
    String property = context.getStringAttribute("property");
    String column = context.getStringAttribute("column");
    String javaType = context.getStringAttribute("javaType");
    String jdbcType = context.getStringAttribute("jdbcType");
    String nestedSelect = context.getStringAttribute("select");
    String nestedResultMap = context.getStringAttribute("resultMap");
    String typeHandler = context.getStringAttribute("typeHandler");
    Class javaTypeClass = resolveClass(javaType);
    Class typeHandlerClass = resolveClass(typeHandler);
    JdbcType jdbcTypeEnum = resolveJdbcType(jdbcType);
    sequentialBuilder.resultMapping(property, column, javaTypeClass, jdbcTypeEnum, nestedSelect, nestedResultMap, typeHandlerClass, flags);
  }

  private void bindMapperForNamespace() {
    String namespace = sequentialBuilder.getNamespace();
    if (namespace != null) {
      Class boundType = null;
      try {
        boundType = Class.forName(namespace);
      } catch (ClassNotFoundException e) {
        //ignore, bound type is not required
      }
      if (boundType != null) {
        if (!configuration.hasMapper(boundType)) {
          configuration.addMapper(boundType);
        }
      }
    }
  }

}
