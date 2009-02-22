package org.apache.ibatis.parser;

import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.reflection.MetaClass;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.xml.Nodelet;
import org.apache.ibatis.xml.NodeletContext;
import org.apache.ibatis.xml.NodeletParser;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

public class MapperParser extends BaseParser {

  protected String namespace;
  protected Reader reader;
  protected NodeletParser parser;

  private String resource;

  private ParameterMap.Builder parameterMapBuilder;
  private List<ParameterMapping> parameterMappings;

  private ResultMap.Builder resultMapBuilder;
  private List<ResultMapping> resultMappings;

  private Discriminator.Builder discriminatorBuilder;
  private HashMap<String, String> discriminatorMap;

  private Cache cache;

  public MapperParser(Reader reader, Configuration configuration, String resource) {
    ErrorContext.instance().resource(resource);
    this.resource = resource;
    this.reader = reader;

    this.configuration = configuration;
    this.typeAliasRegistry = configuration.getTypeAliasRegistry();
    this.typeHandlerRegistry = configuration.getTypeHandlerRegistry();

    this.parser = new NodeletParser();
    this.parser.addNodeletHandler(this);
    this.parser.setValidation(true);
    this.parser.setVariables(configuration.getVariables());
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

  //  <configuration namespace="com.domain.MapperClass" />
  @Nodelet("/mapper")
  public void configurationElement(NodeletContext context) throws Exception {
    namespace = context.getStringAttribute("namespace");
    if (namespace == null) {
      throw new ParserException("The mapper element requires a namespace attribute to be specified.");
    }
  }

  //  <cache type="LRU" flushInterval="3600000" size="1000" readOnly="false" />
  @Nodelet("/mapper/cache-ref")
  public void cacheRefElement(NodeletContext context) throws Exception {
    String ns = context.getStringAttribute("namespace");
    if (ns == null) {
      throw new ParserException("cache-ref element requires a namespace attribute.");
    }
    cache = configuration.getCache(namespaceCacheId(ns));
    if (cache == null) {
      throw new ParserException("No cache for namespace '" + ns + "' could be found.");
    }
  }

  //  <cache type="LRU" flushInterval="3600000" size="1000" readOnly="false" />
  @Nodelet("/mapper/cache")
  public void cacheElement(NodeletContext context) throws Exception {
    String type = context.getStringAttribute("perpetual", "PERPETUAL");
    type = typeAliasRegistry.resolveAlias(type);
    Class typeClass = Class.forName(type);

    String eviction = context.getStringAttribute("eviction", "LRU");
    eviction = typeAliasRegistry.resolveAlias(eviction);
    Class evictionClass = Class.forName(eviction);

    Long flushInterval = context.getLongAttribute("flushInterval", null);
    Integer size = context.getIntAttribute("size", null);
    boolean readOnly = context.getBooleanAttribute("readOnly", false);

    Properties props = context.getChildrenAsProperties();

    cache = new CacheBuilder(namespaceCacheId(namespace))
        .implementation(typeClass)
        .addDecorator(evictionClass)
        .clearInterval(flushInterval)
        .size(size)
        .readWrite(!readOnly)
        .properties(props)
        .build();

    configuration.addCache(cache);
  }

  //  <parameterMap id="" type="">
  @Nodelet("/mapper/parameterMap")
  public void parameterMapElement(NodeletContext context) throws Exception {
    String id = context.getStringAttribute("id");
    id = applyNamespace(id);
    String type = context.getStringAttribute("type");
    Class parameterClass = resolveClass(type);
    parameterMappings = new ArrayList<ParameterMapping>();
    parameterMapBuilder = new ParameterMap.Builder(configuration, id, parameterClass, parameterMappings);
  }

  //  <parameterMap id="" type="">
  //    <param property="id" javaType="" jdbcType="" typeHandler="" mode="" scale="" resultMap=""/>
  @Nodelet("/mapper/parameterMap/parameter")
  public void parameterMapParameterElement(NodeletContext context) throws Exception {
    ParameterMapping.Builder builder = buildParameterMappingFromContext(context);
    ParameterMapping parameterMapping = builder.build();
    parameterMappings.add(parameterMapping);
  }

  //  </parameterMap>
  @Nodelet("/mapper/parameterMap/end()")
  public void parameterMapClosingElement(NodeletContext context) throws Exception {
    configuration.addParameterMap(parameterMapBuilder.build());
  }

  //  <resultMap id="" type="" extends="">
  @Nodelet("/mapper/resultMap")
  public void resultMapElement(NodeletContext context) throws Exception {
    String id = context.getStringAttribute("id");
    id = applyNamespace(id);

    String type = context.getStringAttribute("type");
    String extend = context.getStringAttribute("extends");
    extend = applyNamespace(extend);

    Class typeClass = resolveClass(type);

    resultMappings = new ArrayList<ResultMapping>();
    resultMapBuilder = new ResultMap.Builder(configuration, id, typeClass, resultMappings);

    if (extend != null) {
      ResultMap resultMap = configuration.getResultMap(extend);
      resultMappings.addAll(resultMap.getResultMappings());
    }
  }

  //  <constructor>
  //    <id column="" javaType="" jdbcType="" typeHandler=""/>
  @Nodelet("/mapper/resultMap/constructor/id")
  public void resultMapConstructorIdElement(NodeletContext context) throws Exception {
    ResultMapping.Builder builder = buildResultMappingFromContext(context);
    builder.flags(new ArrayList<ResultFlag>() {
      {
        add(ResultFlag.CONSTRUCTOR);
        add(ResultFlag.ID);
      }
    });
    resultMappings.add(builder.build());
  }

  //  <constructor>
  //    <result column="" javaType="" jdbcType="" typeHandler=""/>
  @Nodelet("/mapper/resultMap/constructor/result")
  public void resultMapConstructorResultElement(NodeletContext context) throws Exception {
    ResultMapping.Builder builder = buildResultMappingFromContext(context);
    builder.flags(new ArrayList<ResultFlag>() {
      {
        add(ResultFlag.CONSTRUCTOR);
      }
    });
    resultMappings.add(builder.build());
  }

  //  <id property="" column="" javaType="" jdbcType="" typeHandler=""/>
  @Nodelet("/mapper/resultMap/id")
  public void resultMapIdElement(NodeletContext context) throws Exception {
    ResultMapping.Builder builder = buildResultMappingFromContext(context);
    builder.flags(new ArrayList<ResultFlag>() {
      {
        add(ResultFlag.ID);
      }
    });
    resultMappings.add(builder.build());
  }

  //  <result property="" column="" javaType="" jdbcType="" typeHandler=""/>
  @Nodelet("/mapper/resultMap/result")
  public void resultMapResultElement(NodeletContext context) throws Exception {
    ResultMapping.Builder builder = buildResultMappingFromContext(context);
    resultMappings.add(builder.build());
  }

  //  <collection property="" column="" javaType="" select="" resultMap=""/>
  @Nodelet("/mapper/resultMap/collection")
  public void resultMapCollectionElement(NodeletContext context) throws Exception {
    ResultMapping.Builder builder = buildResultMappingFromContext(context);
    resultMappings.add(builder.build());
  }

  //  <association property="" column="" javaType="" select="" resultMap=""/>
  @Nodelet("/mapper/resultMap/association")
  public void resultMapAssociationElement(NodeletContext context) throws Exception {
    ResultMapping.Builder builder = buildResultMappingFromContext(context);
    resultMappings.add(builder.build());
  }

  //  <discriminator column="" javaType="" jdbcType="">
  @Nodelet("/mapper/resultMap/discriminator")
  public void resultMapDiscriminatorElement(NodeletContext context) throws Exception {
    ResultMapping.Builder resultMappingBuilder = buildResultMappingFromContext(context);
    discriminatorMap = new HashMap<String, String>();
    discriminatorBuilder = new Discriminator.Builder(configuration, resultMappingBuilder.build(), discriminatorMap);
  }

  //  <discriminator column="" javaType="" jdbcType="">
  //    <case value="" resultMap=""/>
  @Nodelet("/mapper/resultMap/discriminator/case")
  public void resultMapDiscriminatorCaseElement(NodeletContext context) throws Exception {
    String value = context.getStringAttribute("value");
    String resultMap = context.getStringAttribute("resultMap");
    resultMap = applyNamespace(resultMap);
    discriminatorMap.put(value, resultMap);
  }

  //  </discriminator>
  @Nodelet("/mapper/resultMap/discriminator/end()")
  public void resultMapDiscriminatorClosingElement(NodeletContext context) throws Exception {
    resultMapBuilder.discriminator(discriminatorBuilder.build());
  }

  //  </resultMap>
  @Nodelet("/mapper/resultMap/end()")
  public void resultMapClosingElement(NodeletContext context) throws Exception {
    configuration.addResultMap(resultMapBuilder.build());
  }

  //  <select ...>
  @Nodelet("/mapper/select")
  public void selectElement(NodeletContext context) throws Exception {
    buildStatement(context, StatementType.PREPARED);
  }

  //  <insert ...>
  @Nodelet("/mapper/insert")
  public void insertElement(NodeletContext context) throws Exception {
    buildStatement(context, StatementType.PREPARED);
  }

  //  <update ...>
  @Nodelet("/mapper/update")
  public void updateElement(NodeletContext context) throws Exception {
    buildStatement(context, StatementType.PREPARED);
  }

  //  <delete ...>
  @Nodelet("/mapper/delete")
  public void deleteElement(NodeletContext context) throws Exception {
    buildStatement(context, StatementType.PREPARED);
  }

  //  <procedure ...>
  @Nodelet("/mapper/procedure")
  public void procedureElement(NodeletContext context) throws Exception {
    buildStatement(context, StatementType.CALLABLE);
  }

  //  <procedure ...>
  @Nodelet("/mapper/statement")
  public void statementElement(NodeletContext context) throws Exception {
    buildStatement(context, StatementType.STATEMENT);
  }

  private String applyNamespace(String base) {
    if (base == null) return null;
    if (base.contains(".")) return base;
    return namespace + "." + base;
  }

  private String namespaceCacheId(String ns) {
    return ns + ".Cache";
  }

  private void buildStatement(NodeletContext context, StatementType statementType) {
    String id = context.getStringAttribute("id");
    id = applyNamespace(id);

    //String sql = context.getStringBody();
    SqlSource sqlSource = new SqlSourceParser(configuration).parse(context);

    MappedStatement.Builder statementBuilder = new MappedStatement.Builder(configuration, id, sqlSource);
    statementBuilder.resource(resource);
    Integer fetchSize = context.getIntAttribute("fetchSize", null);
    statementBuilder.fetchSize(fetchSize);
    statementBuilder.statementType(statementType);
    setStatementTimeout(context, statementBuilder);

    setStatementParameterMap(context, statementBuilder);
    setStatementResultMap(context, statementBuilder);
    setStatementCache(context, statementBuilder);

    MappedStatement statement = statementBuilder.build();
    configuration.addMappedStatement(statement);
  }

  private void setStatementCache(NodeletContext context, MappedStatement.Builder statementBuilder) {
    boolean isSelect = "select".equals(context.getNode().getNodeName());

    boolean flushCache = context.getBooleanAttribute("flushCache", !isSelect);
    statementBuilder.flushCacheRequired(flushCache);

    boolean useCache = context.getBooleanAttribute("useCache", isSelect);
    statementBuilder.useCache(useCache);

    statementBuilder.cache(cache);
  }

  private void setStatementParameterMap(NodeletContext context, MappedStatement.Builder statementBuilder) {
    String parameterMap = context.getStringAttribute("parameterMap");
    parameterMap = applyNamespace(parameterMap);

    String parameterType = context.getStringAttribute("parameterType");
    if (parameterMap != null) {
      statementBuilder.parameterMap(configuration.getParameterMap(parameterMap));
    } else if (parameterType != null) {
      List<ParameterMapping> parameterMappings = new ArrayList<ParameterMapping>();
      Class parameterTypeClass = resolveClass(parameterType);
      ParameterMap.Builder inlineParameterMapBuilder = new ParameterMap.Builder(
          configuration,
          statementBuilder.id() + "-inline-parameter-map",
          parameterTypeClass,
          parameterMappings);
      statementBuilder.parameterMap(inlineParameterMapBuilder.build());
    }
  }

  private void setStatementResultMap(NodeletContext context, MappedStatement.Builder statementBuilder) {
    String resultMap = context.getStringAttribute("resultMap");
    resultMap = applyNamespace(resultMap);

    String resultType = context.getStringAttribute("resultType");
    List<ResultMap> resultMaps = new ArrayList<ResultMap>();
    if (resultMap != null) {
      String[] resultMapNames = resultMap.split(",");
      for (String resultMapName : resultMapNames) {
        resultMaps.add(configuration.getResultMap(resultMapName.trim()));
      }
    } else if (resultType != null) {
      Class resultTypeClass = resolveClass(resultType);
      ResultMap.Builder inlineResultMapBuilder = new ResultMap.Builder(
          configuration,
          statementBuilder.id() + "-inline-result-map",
          resultTypeClass,
          new ArrayList<ResultMapping>());
      resultMaps.add(inlineResultMapBuilder.build());
    }
    statementBuilder.resultMaps(resultMaps);

    String resultSetType = context.getStringAttribute("resultSetType");
    ResultSetType resultSetTypeEnum = resolveResultSetType(resultSetType);
    statementBuilder.resultSetType(resultSetTypeEnum);
  }

  private void setStatementTimeout(NodeletContext context, MappedStatement.Builder statementBuilder) {
    Integer timeout = context.getIntAttribute("timeout", null);
    if (timeout != null) {
      timeout = configuration.getDefaultStatementTimeout();
    }
    statementBuilder.timeout(timeout);
  }

  private ResultMapping.Builder buildResultMappingFromContext(NodeletContext context) {
    String property = context.getStringAttribute("property");
    String column = context.getStringAttribute("column");
    String javaType = context.getStringAttribute("javaType");
    String jdbcType = context.getStringAttribute("jdbcType");
    String nestedSelect = context.getStringAttribute("select");
    String nestedResultMap = context.getStringAttribute("resultMap");
    nestedResultMap = applyNamespace(nestedResultMap);

    String typeHandler = context.getStringAttribute("typeHandler");

    Class resultType = resultMapBuilder.type();
    Class javaTypeClass = resolveResultJavaType(resultType, property, javaType);
    TypeHandler typeHandlerInstance = (TypeHandler) resolveInstance(typeHandler);
    JdbcType jdbcTypeEnum = resolveJdbcType(jdbcType);

    ResultMapping.Builder builder = new ResultMapping.Builder(configuration, property, column, javaTypeClass);
    builder.jdbcType(jdbcTypeEnum);
    builder.nestedQueryId(applyNamespace(nestedSelect));
    builder.nestedResultMapId(applyNamespace(nestedResultMap));
    builder.typeHandler(typeHandlerInstance);

    return builder;
  }

  private Class resolveResultJavaType(Class resultType, String property, String javaType) {
    Class javaTypeClass = resolveClass(javaType);
    if (javaTypeClass == null) {
      MetaClass metaResultType = MetaClass.forClass(resultType);
      javaTypeClass = metaResultType.getSetterType(property);
    }
    if (javaTypeClass == null) {
      throw new ParserException("Could not determine javaType for result.  Specify property or javaType attribute.");
    }
    return javaTypeClass;
  }

  private ParameterMapping.Builder buildParameterMappingFromContext(NodeletContext context) {
    String property = context.getStringAttribute("property");
    String javaType = context.getStringAttribute("javaType");
    String jdbcType = context.getStringAttribute("jdbcType");
    String resultMap = context.getStringAttribute("resultMap");
    resultMap = applyNamespace(resultMap);
    String mode = context.getStringAttribute("mode");
    String typeHandler = context.getStringAttribute("typeHandler");
    Integer numericScale = context.getIntAttribute("numericScale", null);

    ParameterMode modeEnum = resolveParameterMode(mode);
    Class resultType = parameterMapBuilder.type();
    Class javaTypeClass = resolveParameterJavaType(resultType, property, javaType);
    JdbcType jdbcTypeEnum = resolveJdbcType(jdbcType);
    TypeHandler typeHandlerInstance = (TypeHandler) resolveInstance(typeHandler);

    ParameterMapping.Builder builder = new ParameterMapping.Builder(configuration, property, javaTypeClass);
    builder.jdbcType(jdbcTypeEnum);
    builder.resultMapId(resultMap);
    builder.mode(modeEnum);
    builder.numericScale(numericScale);
    builder.typeHandler(typeHandlerInstance);

    return builder;
  }

  private Class resolveParameterJavaType(Class resultType, String property, String javaType) {
    Class javaTypeClass = resolveClass(javaType);
    if (javaTypeClass == null) {
      MetaClass metaResultType = MetaClass.forClass(resultType);
      javaTypeClass = metaResultType.getGetterType(property);
    }
    if (javaTypeClass == null) {
      throw new ParserException("Could not determine javaType for result.  Specify property or javaType attribute.");
    }
    return javaTypeClass;
  }


}