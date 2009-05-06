package org.apache.ibatis.parser;

import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.decorators.LruCache;
import org.apache.ibatis.cache.impl.PerpetualCache;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.reflection.MetaClass;
import org.apache.ibatis.type.*;

import java.util.*;

public class SequentialMapperBuilder extends BaseParser {

  private String namespace;
  private String resource;

  private ParameterMap.Builder parameterMapBuilder;
  private List<ParameterMapping> parameterMappings;

  private ResultMap.Builder resultMapBuilder;
  private List<ResultMapping> resultMappings;

  private Discriminator.Builder discriminatorBuilder;
  private HashMap<String, String> discriminatorMap;

  private Cache cache;

  public SequentialMapperBuilder(Configuration configuration, String resource) {
    super(configuration);
    ErrorContext.instance().resource(resource);
    this.resource = resource;
  }

  public String getNamespace() {
    return namespace;
  }

  public void namespace(String namespace) {
    if (namespace != null) {
      this.namespace = namespace;
    }
    if (this.namespace == null) {
      throw new ParserException("The mapper element requires a namespace attribute to be specified.");
    }
  }

  public void cacheRef(String namespace) {
    if (namespace == null) {
      throw new ParserException("cache-ref element requires a namespace attribute.");
    }
    cache = configuration.getCache(namespace);
    if (cache == null) {
      throw new ParserException("No cache for namespace '" + namespace + "' could be found.");
    }
  }

  //  <cache type="LRU" flushInterval="3600000" size="1000" readOnly="false" />
  public void cache(Class typeClass,
                    Class evictionClass,
                    Long flushInterval,
                    Integer size,
                    boolean readOnly,
                    Properties props) {
    typeClass = valueOrDefault(typeClass, PerpetualCache.class);
    evictionClass = valueOrDefault(evictionClass, LruCache.class);
    cache = new CacheBuilder(namespace)
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
  public void parameterMapStart(String id, Class parameterClass) {
    id = applyNamespace(id);
    parameterMappings = new ArrayList<ParameterMapping>();
    parameterMapBuilder = new ParameterMap.Builder(configuration, id, parameterClass, parameterMappings);
  }

  //  <parameterMap id="" type="">
  //    <param property="id" javaType="" jdbcType="" typeHandler="" mode="" scale="" resultMap=""/>
  public void parameterMapping(
      String property,
      Class javaType,
      JdbcType jdbcType,
      String resultMap,
      ParameterMode parameterMode,
      Class typeHandler,
      Integer numericScale) {
    resultMap = applyNamespace(resultMap);

    Class resultType = parameterMapBuilder.type();
    Class javaTypeClass = resolveParameterJavaType(resultType, property, javaType);
    TypeHandler typeHandlerInstance = (TypeHandler) resolveInstance(typeHandler);

    ParameterMapping.Builder builder = new ParameterMapping.Builder(configuration, property, javaTypeClass);
    builder.jdbcType(jdbcType);
    builder.resultMapId(resultMap);
    builder.mode(parameterMode);
    builder.numericScale(numericScale);
    builder.typeHandler(typeHandlerInstance);
    parameterMappings.add(builder.build());
  }

  //  </parameterMap>
  public void parameterMapEnd() {
    configuration.addParameterMap(parameterMapBuilder.build());
  }

  //  <resultMap id="" type="" extends="">
  public void resultMapStart(
      String id,
      Class type,
      String extend) {
    id = applyNamespace(id);
    extend = applyNamespace(extend);

    resultMappings = new ArrayList<ResultMapping>();
    resultMapBuilder = new ResultMap.Builder(configuration, id, type, resultMappings);

    if (extend != null) {
      ResultMap resultMap = configuration.getResultMap(extend);
      resultMappings.addAll(resultMap.getResultMappings());
    }
  }

  //  <constructor>
  //    <id column="" javaType="" jdbcType="" typeHandler=""/>
  //  <constructor>
  //    <result column="" javaType="" jdbcType="" typeHandler=""/>
  //  <id property="" column="" javaType="" jdbcType="" typeHandler=""/>
  //  <result property="" column="" javaType="" jdbcType="" typeHandler=""/>
  //  <collection property="" column="" javaType="" select="" resultMap=""/>
  //  <association property="" column="" javaType="" select="" resultMap=""/>
  public void resultMapping(
      String property,
      String column,
      Class javaType,
      JdbcType jdbcType,
      String nestedSelect,
      String nestedResultMap,
      Class typeHandler,
      List<ResultFlag> flags) {
    ResultMapping resultMapping = buildResultMapping(
        property,
        column,
        javaType,
        jdbcType,
        nestedSelect,
        nestedResultMap,
        typeHandler,
        flags);
    resultMappings.add(resultMapping);
  }


  //  <discriminator column="" javaType="" jdbcType="">
  public void resultMapDiscriminatorStart(
      String column,
      Class javaType,
      JdbcType jdbcType,
      Class typeHandler) {
    ResultMapping resultMapping = buildResultMapping(
        null,
        column,
        javaType,
        jdbcType,
        null,
        null,
        typeHandler,
        new ArrayList<ResultFlag>());
    discriminatorMap = new HashMap<String, String>();
    discriminatorBuilder = new Discriminator.Builder(configuration, resultMapping, discriminatorMap);
  }

  //  <discriminator column="" javaType="" jdbcType="">
  //    <case value="" resultMap=""/>
  public void resultMapDiscriminatorCase(
      String value,
      String resultMap) {
    resultMap = applyNamespace(resultMap);
    discriminatorMap.put(value, resultMap);
  }

  //  </discriminator>
  public void resultMapDiscriminatorEnd() {
    resultMapBuilder.discriminator(discriminatorBuilder.build());
  }

  //  </resultMap>
  public void resultMapEnd() {
    configuration.addResultMap(resultMapBuilder.build());
  }

  public void statement(
      String id,
      SqlSource sqlSource,
      Integer fetchSize,
      Integer timeout,
      String parameterMap,
      Class parameterType,
      String resultMap,
      Class resultType,
      ResultSetType resultSetType,
      boolean isSelect,
      boolean flushCache,
      boolean useCache,
      StatementType statementType) {
    id = applyNamespace(id);

    MappedStatement.Builder statementBuilder = new MappedStatement.Builder(configuration, id, sqlSource);
    statementBuilder.resource(resource);
    statementBuilder.fetchSize(fetchSize);
    statementBuilder.statementType(statementType);
    setStatementTimeout(timeout, statementBuilder);

    setStatementParameterMap(parameterMap, parameterType, statementBuilder);
    setStatementResultMap(resultMap, resultType, resultSetType, statementBuilder);
    setStatementCache(isSelect, flushCache, useCache, statementBuilder);

    MappedStatement statement = statementBuilder.build();
    configuration.addMappedStatement(statement);
  }

  private <T> T valueOrDefault(T value, T defaultValue) {
    return value == null ? defaultValue : value;
  }

  private String applyNamespace(String base) {
    if (base == null) return null;
    if (base.contains(".")) return base;
    return namespace + "." + base;
  }

  private void setStatementCache(
      boolean isSelect,
      boolean flushCache,
      boolean useCache,
      MappedStatement.Builder statementBuilder) {
    flushCache = valueOrDefault(flushCache, !isSelect);
    useCache = valueOrDefault(useCache, isSelect);
    statementBuilder.flushCacheRequired(flushCache);
    statementBuilder.useCache(useCache);
    statementBuilder.cache(cache);
  }

  private void setStatementParameterMap(
      String parameterMap,
      Class parameterTypeClass,
      MappedStatement.Builder statementBuilder) {
    parameterMap = applyNamespace(parameterMap);

    if (parameterMap != null) {
      statementBuilder.parameterMap(configuration.getParameterMap(parameterMap));
    } else if (parameterTypeClass != null) {
      List<ParameterMapping> parameterMappings = new ArrayList<ParameterMapping>();
      ParameterMap.Builder inlineParameterMapBuilder = new ParameterMap.Builder(
          configuration,
          statementBuilder.id() + "-Inline",
          parameterTypeClass,
          parameterMappings);
      statementBuilder.parameterMap(inlineParameterMapBuilder.build());
    }
  }

  private void setStatementResultMap(
      String resultMap,
      Class resultType,
      ResultSetType resultSetType,
      MappedStatement.Builder statementBuilder) {
    resultMap = applyNamespace(resultMap);

    List<ResultMap> resultMaps = new ArrayList<ResultMap>();
    if (resultMap != null) {
      String[] resultMapNames = resultMap.split(",");
      for (String resultMapName : resultMapNames) {
        resultMaps.add(configuration.getResultMap(resultMapName.trim()));
      }
    } else if (resultType != null) {
      ResultMap.Builder inlineResultMapBuilder = new ResultMap.Builder(
          configuration,
          statementBuilder.id() + "-Inline",
          resultType,
          new ArrayList<ResultMapping>());
      resultMaps.add(inlineResultMapBuilder.build());
    }
    statementBuilder.resultMaps(resultMaps);

    statementBuilder.resultSetType(resultSetType);
  }

  private void setStatementTimeout(Integer timeout, MappedStatement.Builder statementBuilder) {
    if (timeout != null) {
      timeout = configuration.getDefaultStatementTimeout();
    }
    statementBuilder.timeout(timeout);
  }

  private ResultMapping buildResultMapping(
      String property,
      String column,
      Class javaType,
      JdbcType jdbcType,
      String nestedSelect,
      String nestedResultMap,
      Class typeHandler,
      List<ResultFlag> flags) {

    nestedResultMap = applyNamespace(nestedResultMap);
    Class resultType = resultMapBuilder.type();
    Class javaTypeClass = resolveResultJavaType(resultType, property, javaType);
    TypeHandler typeHandlerInstance = (TypeHandler) resolveInstance(typeHandler);

    ResultMapping.Builder builder = new ResultMapping.Builder(configuration, property, column, javaTypeClass);
    builder.jdbcType(jdbcType);
    builder.nestedQueryId(applyNamespace(nestedSelect));
    builder.nestedResultMapId(applyNamespace(nestedResultMap));
    builder.typeHandler(typeHandlerInstance);
    builder.flags(flags == null ? new ArrayList<ResultFlag>() : flags);

    return builder.build();
  }

  private Class resolveResultJavaType(Class resultType, String property, Class javaType) {
    if (javaType == null && property != null) {
      MetaClass metaResultType = MetaClass.forClass(resultType);
      javaType = metaResultType.getSetterType(property);
    }
    if (javaType == null) {
      throw new ParserException("Could not determine javaType for result.  Specify property or javaType attribute.");
    }
    return javaType;
  }

  private Class resolveParameterJavaType(Class resultType, String property, Class javaType) {
    if (javaType == null) {
      MetaClass metaResultType = MetaClass.forClass(resultType);
      javaType = metaResultType.getGetterType(property);
    }
    if (javaType == null) {
      throw new ParserException("Could not determine javaType for result.  Specify property or javaType attribute.");
    }
    return javaType;
  }

}
