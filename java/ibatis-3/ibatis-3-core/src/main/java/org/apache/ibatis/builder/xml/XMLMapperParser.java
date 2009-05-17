package org.apache.ibatis.builder.xml;

import org.apache.ibatis.builder.*;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.parsing.*;
import org.apache.ibatis.type.JdbcType;

import java.io.Reader;
import java.util.*;

public class XMLMapperParser extends BaseParser {

  private Reader reader;
  private NodeletParser parser;
  private SequentialMapperBuilder sequentialBuilder;

  private Map<String,NodeletContext> sqlFragments = new HashMap<String,NodeletContext>();

  public XMLMapperParser(Reader reader, Configuration configuration, String resource, String namespace) {
    this(reader, configuration, resource);
    this.sequentialBuilder.namespace(namespace);
  }

  public XMLMapperParser(Reader reader, Configuration configuration, String resource) {
    super(configuration);
    this.sequentialBuilder = new SequentialMapperBuilder(configuration, resource);
    this.reader = reader;
    this.parser = new NodeletParser();
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
  @Nodelet("/mapper")
  public void configurationElement(NodeletContext context) throws Exception {
    String namespace = context.getStringAttribute("namespace");
    sequentialBuilder.namespace(namespace);
  }

  //  <cache type="LRU" flushInterval="3600000" size="1000" readOnly="false" />
  @Nodelet("/mapper/cache-ref")
  public void cacheRefElement(NodeletContext context) throws Exception {
    sequentialBuilder.cacheRef(context.getStringAttribute("namespace"));
  }

  //  <cache type="LRU" flushInterval="3600000" size="1000" readOnly="false" />
  @Nodelet("/mapper/cache")
  public void cacheElement(NodeletContext context) throws Exception {
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
  @Nodelet("/mapper/parameterMap")
  public void parameterMapElement(NodeletContext context) throws Exception {
    String id = context.getStringAttribute("id");
    String type = context.getStringAttribute("type");
    Class parameterClass = resolveClass(type);
    sequentialBuilder.parameterMapStart(id, parameterClass);
  }

  //  <parameterMap id="" type="">
  //    <param property="id" javaType="" jdbcType="" typeHandler="" mode="" scale="" resultMap=""/>
  @Nodelet("/mapper/parameterMap/parameter")
  public void parameterMapParameterElement(NodeletContext context) throws Exception {
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
  @Nodelet("/mapper/parameterMap/end()")
  public void parameterMapClosingElement(NodeletContext context) throws Exception {
    sequentialBuilder.parameterMapEnd();
  }

  //  <resultMap id="" type="" extends="">
  @Nodelet("/mapper/resultMap")
  public void resultMapElement(NodeletContext context) throws Exception {
    String id = context.getStringAttribute("id");
    String type = context.getStringAttribute("type");
    String extend = context.getStringAttribute("extends");
    Class typeClass = resolveClass(type);
    sequentialBuilder.resultMapStart(id, typeClass, extend);
  }

  //  <constructor>
  //    <id column="" javaType="" jdbcType="" typeHandler=""/>
  @Nodelet("/mapper/resultMap/constructor/idArg")
  public void resultMapConstructorIdElement(NodeletContext context) throws Exception {
    buildResultMappingFromContext(context,
        new ArrayList<ResultFlag>() {
          {
            add(ResultFlag.CONSTRUCTOR);
            add(ResultFlag.ID);
          }
        });
  }

  //  <constructor>
  //    <result column="" javaType="" jdbcType="" typeHandler=""/>
  @Nodelet("/mapper/resultMap/constructor/arg")
  public void resultMapConstructorResultElement(NodeletContext context) throws Exception {
    buildResultMappingFromContext(context, new ArrayList<ResultFlag>() {
      {
        add(ResultFlag.CONSTRUCTOR);
      }
    });
  }

  //  <id property="" column="" javaType="" jdbcType="" typeHandler=""/>
  @Nodelet("/mapper/resultMap/id")
  public void resultMapIdElement(NodeletContext context) throws Exception {
    buildResultMappingFromContext(context, new ArrayList<ResultFlag>() {
      {
        add(ResultFlag.ID);
      }
    });
  }

  //  <result property="" column="" javaType="" jdbcType="" typeHandler=""/>
  @Nodelet("/mapper/resultMap/result")
  public void resultMapResultElement(NodeletContext context) throws Exception {
    buildResultMappingFromContext(context, new ArrayList<ResultFlag>());
  }

  //  <collection property="" column="" javaType="" select="" resultMap=""/>
  @Nodelet("/mapper/resultMap/collection")
  public void resultMapCollectionElement(NodeletContext context) throws Exception {
    buildResultMappingFromContext(context, new ArrayList<ResultFlag>());
  }

  //  <association property="" column="" javaType="" select="" resultMap=""/>
  @Nodelet("/mapper/resultMap/association")
  public void resultMapAssociationElement(NodeletContext context) throws Exception {
    buildResultMappingFromContext(context, new ArrayList<ResultFlag>());
  }

  //  <discriminator column="" javaType="" jdbcType="">
  @Nodelet("/mapper/resultMap/discriminator")
  public void resultMapDiscriminatorElement(NodeletContext context) throws Exception {
    String column = context.getStringAttribute("column");
    String javaType = context.getStringAttribute("javaType");
    String jdbcType = context.getStringAttribute("jdbcType");
    String typeHandler = context.getStringAttribute("typeHandler");
    Class javaTypeClass = resolveClass(javaType);
    Class typeHandlerClass = resolveClass(typeHandler);
    JdbcType jdbcTypeEnum = resolveJdbcType(jdbcType);
    sequentialBuilder.resultMapDiscriminatorStart(column, javaTypeClass, jdbcTypeEnum, typeHandlerClass);
  }

  //  <discriminator column="" javaType="" jdbcType="">
  //    <case value="" resultMap=""/>
  @Nodelet("/mapper/resultMap/discriminator/case")
  public void resultMapDiscriminatorCaseElement(NodeletContext context) throws Exception {
    String value = context.getStringAttribute("value");
    String resultMap = context.getStringAttribute("resultMap");
    sequentialBuilder.resultMapDiscriminatorCase(value, resultMap);
  }

  //  </discriminator>
  @Nodelet("/mapper/resultMap/discriminator/end()")
  public void resultMapDiscriminatorClosingElement(NodeletContext context) throws Exception {
    sequentialBuilder.resultMapDiscriminatorEnd();
  }

  //  </resultMap>
  @Nodelet("/mapper/resultMap/end()")
  public void resultMapClosingElement(NodeletContext context) throws Exception {
    sequentialBuilder.resultMapEnd();
  }

  //  <sql id="">
  @Nodelet("/mapper/sql")
  public void sqlElement(NodeletContext context) throws Exception {
    String id = context.getStringAttribute("id");

    sqlFragments.put(id, context);
  }

  //  <select ...>
  @Nodelet("/mapper/select")
  public void selectElement(NodeletContext context) throws Exception {
    buildStatementFromContext(context);
  }

  //  <insert ...>
  @Nodelet("/mapper/insert")
  public void insertElement(NodeletContext context) throws Exception {
    buildStatementFromContext(context);
  }

  //  <update ...>
  @Nodelet("/mapper/update")
  public void updateElement(NodeletContext context) throws Exception {
    buildStatementFromContext(context);
  }

  //  <delete ...>
  @Nodelet("/mapper/delete")
  public void deleteElement(NodeletContext context) throws Exception {
    buildStatementFromContext(context);
  }

  public NodeletContext getSqlFragment(String refid) {
    return sqlFragments.get(refid);
  }

  private void buildStatementFromContext(NodeletContext context) {
    final XMLStatementParser statementParser = new XMLStatementParser(configuration, sequentialBuilder, this);
    statementParser.parseStatementNode(context);
  }

  private void buildResultMappingFromContext(NodeletContext context, ArrayList<ResultFlag> flags) {
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
