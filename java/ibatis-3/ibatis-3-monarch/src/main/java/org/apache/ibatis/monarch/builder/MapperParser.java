package org.apache.ibatis.monarch.builder;

import org.apache.ibatis.mapping.*;
import org.apache.ibatis.reflection.MetaClass;
import org.apache.ibatis.type.*;
import org.apache.ibatis.xml.*;

import java.io.Reader;
import java.util.*;

public class MapperParser extends BaseParser {

  private ParameterMap.Builder parameterMapBuilder;
  private List<ParameterMapping> parameterMappings;

  private ResultMap.Builder resultMapBuilder;
  private List<ResultMapping> resultMappings;

  private Discriminator.Builder discriminatorBuilder;
  private HashMap<String, String> discriminatorMap;

  public MapperParser(Reader reader, MonarchConfiguration configuration) {
    this.reader = reader;

    this.configuration = configuration;
    this.typeAliasRegistry = configuration.getTypeAliasRegistry();
    this.typeHandlerRegistry = configuration.getTypeHandlerRegistry();

    this.parser = new NodeletParser();
    this.parser.addNodeletHandler(this);
    this.parser.setVariables(configuration.getVariables());
    this.parser.setEntityResolver(new MapperEntityResolver());
  }

  //  <parameterMap id="" type="">
  @Nodelet("/mapper/parameterMap")
  public void parameterMapElement(NodeletContext context) throws Exception {
    String id = context.getStringAttribute("id");
    String type = context.getStringAttribute("type");
    Class parameterClass = resolveClass(type);
    parameterMappings = new ArrayList<ParameterMapping>();
    parameterMapBuilder = new ParameterMap.Builder(id, parameterClass, parameterMappings);
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
    String type = context.getStringAttribute("type");
    String extend = context.getStringAttribute("extends");

    Class typeClass = resolveClass(type);

    resultMappings = new ArrayList<ResultMapping>();
    resultMapBuilder = new ResultMap.Builder(id, typeClass, resultMappings);

    if (extend != null) {
      ResultMap resultMap = configuration.getResultMap(extend);
      if (resultMap == null) {
        throw new RuntimeException("ResultMap named in extends attribute of " + id + "does not exist or is not defined yet.");
      }
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

  //  <discriminator column="" javaType="" jdbcType="">
  @Nodelet("/mapper/resultMap/discriminator")
  public void resultMapDiscriminatorElement(NodeletContext context) throws Exception {
    ResultMapping.Builder resultMappingBuilder = buildResultMappingFromContext(context);
    discriminatorMap = new HashMap<String, String>();
    discriminatorBuilder = new Discriminator.Builder(resultMappingBuilder.build(), discriminatorMap);
  }

  //  <discriminator column="" javaType="" jdbcType="">
  //    <case value="" resultMap=""/>
  @Nodelet("/mapper/resultMap/discriminator/case")
  public void resultMapDiscriminatorCaseElement(NodeletContext context) throws Exception {
    String value = context.getStringAttribute("value");
    String resultMap = context.getStringAttribute("resultMap");
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

  //  <select id="selectAllPeople" cacheType="" cacheDomain=""
  //          flushCache="" parameterType="" resultType="" resultMap="">
  @Nodelet("/mapper/select")
  public void selectElement(NodeletContext context) throws Exception {
    String id = context.getStringAttribute("id");
    String sql = context.getStringBody();
    SqlSource sqlSource = new BasicSqlSource(sql);

    MappedStatement.Builder statementBuilder = new MappedStatement.Builder(configuration, id, sqlSource);
    Integer fetchSize = context.getIntAttribute("fetchSize", null);
    statementBuilder.fetchSize(fetchSize);
    statementBuilder.statementType(StatementType.PREPARED);
    setStatementTimeout(context, statementBuilder);

    setStatementParameterMap(context, statementBuilder);
    setStatementResultMap(context, statementBuilder);
    setStatementCache(context, statementBuilder);
  }

  private void setStatementCache(NodeletContext context, MappedStatement.Builder statementBuilder) {
    //TODO: Implement cache templates
    String cacheType = context.getStringAttribute("cacheType");
    String flushCache = context.getStringAttribute("flushCache");
    statementBuilder.cache(null);
  }

  private void setStatementParameterMap(NodeletContext context, MappedStatement.Builder statementBuilder) {
    String parameterMap = context.getStringAttribute("parameterMap");
    String parameterType = context.getStringAttribute("parameterType");
    if (parameterMap != null) {
      statementBuilder.parameterMap(configuration.getParameterMap(parameterMap));
    } else if (parameterType != null) {
      //TODO: Parse Paremeter Mappings from SQL
      List<ParameterMapping> parameterMappings = new ArrayList<ParameterMapping>();
      Class parameterTypeClass = resolveClass(parameterType);
      ParameterMap.Builder inlineParameterMapBuilder = new ParameterMap.Builder(
          context.getStringAttribute("id") + "-inline-parameter-map",
          parameterTypeClass,
          parameterMappings);
      statementBuilder.parameterMap(inlineParameterMapBuilder.build());
    }
  }

  private void setStatementResultMap(NodeletContext context, MappedStatement.Builder statementBuilder) {
    String resultMap = context.getStringAttribute("resultMap");
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
          context.getStringAttribute("id")+ "-inline-result-map",
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

    Class resultType = resultMapBuilder.type();
    Class javaTypeClass = resolveResultJavaType(resultType, property, javaType);
    TypeHandler typeHandlerInstance = resolveResultTypeHandler(context, resultType);
    JdbcType jdbcTypeEnum = resolveJdbcType(jdbcType);

    ResultMapping.Builder builder = new ResultMapping.Builder(property, column, typeHandlerInstance);
    builder.javaType(javaTypeClass);
    builder.jdbcType(jdbcTypeEnum);
    builder.nestedQueryId(nestedSelect);
    builder.nestedResultMapId(nestedResultMap);

    return builder;
  }

  private Class resolveResultJavaType(Class resultType, String property, String javaType) {
    Class javaTypeClass = resolveClass(javaType);
    if (javaTypeClass == null) {
      MetaClass metaResultType = MetaClass.forClass(resultType);
      javaTypeClass = metaResultType.getSetterType(property);
    }
    if (javaTypeClass == null) {
      throw new RuntimeException("Could not determine javaType for result.  Specify property or javaType attribute.");
    }
    return javaTypeClass;
  }

  private TypeHandler resolveResultTypeHandler(NodeletContext context, Class resultType) {
    String property = context.getStringAttribute("property");
    String javaType = context.getStringAttribute("javaType");
    String jdbcType = context.getStringAttribute("jdbcType");
    String typeHandler = context.getStringAttribute("typeHandler");
    JdbcType jdbcTypeEnum = resolveJdbcType(jdbcType);
    Class javaTypeClass = resolveClass(javaType);
    TypeHandler typeHandlerInstance = (TypeHandler) resolveInstance(typeHandler);
    if (typeHandler == null) {
      if (javaTypeClass == null) {
        if (property != null) {
          Class propertyType = resolveResultJavaType(resultType, property, javaType);
          typeHandlerInstance = typeHandlerRegistry.getTypeHandler(propertyType, jdbcTypeEnum);
        }
      } else {
        typeHandlerInstance = typeHandlerRegistry.getTypeHandler(javaTypeClass, jdbcTypeEnum);
      }
    }
    if (typeHandlerInstance == null) {
      throw new RuntimeException("Could not determine typehandler for result.  Specify property, javaType or typeHandler attribute.");
    }
    return typeHandlerInstance;
  }

  private ParameterMapping.Builder buildParameterMappingFromContext(NodeletContext context) {
    String property = context.getStringAttribute("property");
    String javaType = context.getStringAttribute("javaType");
    String jdbcType = context.getStringAttribute("jdbcType");
    String resultMap = context.getStringAttribute("resultMap");
    String mode = context.getStringAttribute("mode");
    Integer numericScale = context.getIntAttribute("numericScale",null);

    ParameterMode modeEnum = resolveParameterMode(mode);

    Class resultType = resultMapBuilder.type();
    Class javaTypeClass = resolveParameterJavaType(resultType, property, javaType);
    TypeHandler typeHandlerInstance = resolveParameterTypeHandler(context, resultType);
    JdbcType jdbcTypeEnum = resolveJdbcType(jdbcType);

    ParameterMapping.Builder builder = new ParameterMapping.Builder(property, typeHandlerInstance);
    builder.javaType(javaTypeClass);
    builder.jdbcType(jdbcTypeEnum);
    builder.resultMapId(resultMap);
    builder.mode(modeEnum);
    builder.numericScale(numericScale);

    return builder;
  }

  private Class resolveParameterJavaType(Class resultType, String property, String javaType) {
    Class javaTypeClass = resolveClass(javaType);
    if (javaTypeClass == null) {
      MetaClass metaResultType = MetaClass.forClass(resultType);
      javaTypeClass = metaResultType.getGetterType(property);
    }
    if (javaTypeClass == null) {
      throw new RuntimeException("Could not determine javaType for result.  Specify property or javaType attribute.");
    }
    return javaTypeClass;
  }

  private TypeHandler resolveParameterTypeHandler(NodeletContext context, Class resultType) {
    String property = context.getStringAttribute("property");
    String javaType = context.getStringAttribute("javaType");
    String jdbcType = context.getStringAttribute("jdbcType");
    String typeHandler = context.getStringAttribute("typeHandler");
    JdbcType jdbcTypeEnum = resolveJdbcType(jdbcType);
    Class javaTypeClass = resolveClass(javaType);
    TypeHandler typeHandlerInstance = (TypeHandler) resolveInstance(typeHandler);
    if (typeHandler == null) {
      if (javaTypeClass == null) {
        if (property != null) {
          Class propertyType = resolveParameterJavaType(resultType, property, javaType);
          typeHandlerInstance = typeHandlerRegistry.getTypeHandler(propertyType, jdbcTypeEnum);
        }
      } else {
        typeHandlerInstance = typeHandlerRegistry.getTypeHandler(javaTypeClass, jdbcTypeEnum);
      }
    }
    if (typeHandlerInstance == null) {
      throw new RuntimeException("Could not determine typehandler for result.  Specify property, javaType or typeHandler attribute.");
    }
    return typeHandlerInstance;
  }

}
