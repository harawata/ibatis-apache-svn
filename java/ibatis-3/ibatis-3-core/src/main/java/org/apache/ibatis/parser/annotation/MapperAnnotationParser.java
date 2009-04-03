package org.apache.ibatis.parser.annotation;

import static org.apache.ibatis.annotations.Annotations.*;
import org.apache.ibatis.binding.BindingException;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.reflection.MetaClass;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.parser.xml.XMLMapperParser;
import org.apache.ibatis.parser.MapperConfigurator;
import org.apache.ibatis.parser.SqlSourceParser;

import java.io.IOException;
import java.io.Reader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MapperAnnotationParser {

  private MapperConfigurator configurator;
  private Class type;

  public MapperAnnotationParser(Configuration config, Class type) {
    String resource = type.getName().replace('.', '/') + ".java (best guess)";
    this.configurator = new MapperConfigurator(config, resource);
    this.type = type;
  }

  public void parse() {
    loadXmlResource();
    configurator.namespace(type.getName());
    parseCache();
    parseCacheRef();
    Method[] methods = type.getMethods();
    for (Method method : methods) {
      parseResultsAndConstructorArgs(method);
      parseStatement(method);
    }
  }

  private void loadXmlResource() {
    String xmlResource = type.getName().replace('.', '/') + ".xml";
    Reader xmlReader = null;
    try {
      xmlReader = Resources.getResourceAsReader(type.getClassLoader(), xmlResource);
    } catch (IOException e) {
      // ignore, resource is not required
    }
    if (xmlReader != null) {
      XMLMapperParser xmlParser = new XMLMapperParser(xmlReader, configurator.getConfiguration(), xmlResource, type.getName());
      xmlParser.parse();
    }
  }

  private void parseCache() {
    CacheDomain cacheDomain = (CacheDomain) type.getAnnotation(CacheDomain.class);
    if (cacheDomain != null) {
      configurator.cache(cacheDomain.implementation(), cacheDomain.eviction(), cacheDomain.flushInterval(), cacheDomain.size(), !cacheDomain.readWrite(), null);
    }
  }

  private void parseCacheRef() {
    CacheDomainRef cacheDomainRef = (CacheDomainRef) type.getAnnotation(CacheDomainRef.class);
    if (cacheDomainRef != null) {
      configurator.cacheRef(cacheDomainRef.value().getName());
    }
  }

  private void parseResultsAndConstructorArgs(Method method) {
    Class returnType = getReturnType(method);
    if (returnType != null) {
      ConstructorArgs args = method.getAnnotation(ConstructorArgs.class);
      Results results = method.getAnnotation(Results.class);
      TypeDiscriminator typeDiscriminator = method.getAnnotation(TypeDiscriminator.class);
      String resultMapId = generateResultMapName(method);
      applyResultMap(resultMapId, returnType, argsIf(args), resultsIf(results), typeDiscriminator);
    }
  }

  private String generateResultMapName(Method method) {
    StringBuilder suffix = new StringBuilder();
    for (Class c : method.getParameterTypes()) {
      suffix.append("-");
      suffix.append(c.getSimpleName());
    }
    if (suffix.length() < 1) {
      suffix.append("-void");
    }
    return type.getName() + "." + method.getName() + suffix;
  }

  private void applyResultMap(String resultMapId, Class returnType, Arg[] args, Result[] results, TypeDiscriminator discriminator) {
    applyNestedResultMaps(resultMapId, returnType, results);
    configurator.resultMapStart(resultMapId, returnType, null);
    applyConstructorArgs(args);
    applyResults(resultMapId, results);
    applyDiscriminator(resultMapId, discriminator);
    configurator.resultMapEnd();
    createDiscriminatorResultMaps(resultMapId, discriminator);
  }

  private void createDiscriminatorResultMaps(String resultMapId, TypeDiscriminator discriminator) {
    if (discriminator != null) {
      for (Case c : discriminator.cases()) {
        String value = c.value();
        Class type = c.type();
        String caseResultMapId = resultMapId + "-" + value;
        configurator.resultMapStart(caseResultMapId, type, resultMapId);
        for (Result result : c.results()) {
          List<ResultFlag> flags = new ArrayList<ResultFlag>();
          if (result.id()) {
            flags.add(ResultFlag.ID);
          }
          configurator.resultMapping(
              result.property(),
              result.column(),
              result.javaType() == void.class ? null : result.javaType(),
              result.jdbcType() == JdbcType.UNDEFINED ? null : result.jdbcType(),
              hasNestedSelect(result) ? nestedSelectId(result) : null,
              hasCollectionOrAssociation(result) ? nestedResultMapId(resultMapId, result) : null,
              result.typeHandler() == void.class ? null : result.typeHandler(),
              flags);
        }
        configurator.resultMapEnd();
      }
    }
  }

  private void applyDiscriminator(String resultMapId, TypeDiscriminator discriminator) {
    if (discriminator != null) {
      String column = discriminator.column();
      Class javaType = discriminator.javaType() == void.class ? String.class : discriminator.javaType();
      JdbcType jdbcType = discriminator.jdbcType() == JdbcType.UNDEFINED ? null : discriminator.jdbcType();
      Class typeHandler = discriminator.typeHandler() == void.class ? null : discriminator.typeHandler();
      Case[] cases = discriminator.cases();

      configurator.resultMapDiscriminatorStart(column, javaType, jdbcType, typeHandler);
      for (Case c : cases) {
        String value = c.value();
        String caseResultMapId = resultMapId + "-" + value;
        configurator.resultMapDiscriminatorCase(value, caseResultMapId);
      }
      configurator.resultMapDiscriminatorEnd();
    }
  }

  private void applyNestedResultMaps(String resultMapId, Class returnType, Result[] results) {
    if (results != null) {
      for (Result result : results) {
        ensureHasOnlyCollectionOrResultNotBoth(result);
        if (hasCollection(result)) {
          Class propertyType = result.many().javaType();
          Arg[] nestedArgs = result.many().constructor().value();
          Result[] nestedResults = result.many().results().value();
          applyResultMap(nestedResultMapId(resultMapId, result), propertyType, nestedArgs, nestedResults, null);
        }
        if (hasAssociation(result)) {
          Class propertyType = MetaClass.forClass(returnType).getSetterType(result.property());
          Arg[] nestedArgs = result.one().constructor().value();
          Result[] nestedResults = result.one().results().value();
          applyResultMap(nestedResultMapId(resultMapId, result), propertyType, nestedArgs, nestedResults, null);
        }
      }
    }
  }

  private void parseStatement(Method method) {
    SqlSource sqlSource = getSqlSourceFromAnnotations(method);
    if (sqlSource != null) {
      Options options = method.getAnnotation(Options.class);
      final String mappedStatementId = method.getDeclaringClass().getName() + "." + method.getName();
      boolean isSelect = method.getAnnotation(Select.class) != null;
      boolean flushCache = false;
      boolean useCache = true;
      Integer fetchSize = null;
      Integer timeout = null;
      StatementType statementType = StatementType.PREPARED;
      ResultSetType resultSetType = ResultSetType.FORWARD_ONLY;
      if (options != null) {
        flushCache = options.flushCache();
        useCache = options.useCache();
        fetchSize = options.fetchSize() > -1 ? options.fetchSize() : null;
        timeout = options.timeout() > -1 ? options.timeout() : null;
        statementType = options.statementType();
        resultSetType = options.resultSetType();
      }
      configurator.statement(
          mappedStatementId,
          sqlSource,
          fetchSize,
          timeout,
          null,         // ParameterMapID
          getParameterType(method),
          generateResultMapName(method),         // ResultMapID
          getReturnType(method),
          resultSetType,
          isSelect,                  // IsSelectStatement
          flushCache,
          useCache,
          statementType);
    }
  }

  private Class getParameterType(Method method) {
    Class parameterType = null;
    Class[] parameterTypes = method.getParameterTypes();
    if (parameterTypes.length == 1 || parameterTypes.length == 3) {
      // Methods with 1 or 3 parameters have a value parameter
      // (the other two params are offset/limit parameters for multiple selects)
      parameterType = parameterTypes[0];
    }
    return parameterType;
  }

  private Class getReturnType(Method method) {
    Class returnType = method.getReturnType();
    if (Collection.class.isAssignableFrom(returnType)) {
      Type returnTypeParameter = method.getGenericReturnType();
      if (returnTypeParameter instanceof ParameterizedType) {
        Type[] actualTypeArguments = ((ParameterizedType) returnTypeParameter).getActualTypeArguments();
        if (actualTypeArguments != null && actualTypeArguments.length == 1) {
          returnTypeParameter = actualTypeArguments[0];
          if (returnTypeParameter instanceof Class) {
            returnType = (Class) returnTypeParameter;
          }
        }
      }
    }
    return returnType;
  }

  private SqlSource getSqlSourceFromAnnotations(Method method) {
    try {
      Class sqlAnnotationType = getSqlAnnotationType(method);
      Class sqlProviderAnnotationType = getSqlProviderAnnotationType(method);
      if (sqlAnnotationType != null) {
        if (sqlProviderAnnotationType != null) {
          throw new BindingException("You cannot supply both a static SQL and SqlProvider to method named " + method.getName());
        }
        Annotation sqlAnnotation = method.getAnnotation(sqlAnnotationType);
        final String[] strings = (String[]) sqlAnnotation.getClass().getMethod("value").invoke(sqlAnnotation);
        StringBuilder sql = new StringBuilder();
        for (String fragment : strings) {
          sql.append(fragment);
          sql.append(" ");
        }
        SqlSourceParser parser = new SqlSourceParser(configurator.getConfiguration());
        return parser.parse(sql.toString());
      } else if (sqlProviderAnnotationType != null) {
        Annotation sqlProviderAnnotation = method.getAnnotation(sqlProviderAnnotationType);
        return new ProviderSqlSource(configurator.getConfiguration(), sqlProviderAnnotation);
      }
      return null;
    } catch (Exception e) {
      throw new RuntimeException("Could not find value method on SQL annotation.  Cause: " + e, e);
    }
  }

  private Class getSqlAnnotationType(Method method) {
    Class[] types = {Select.class, Insert.class, Update.class, Delete.class};
    return chooseAnnotationType(method, types);
  }

  private Class getSqlProviderAnnotationType(Method method) {
    Class[] types = {SelectProvider.class, InsertProvider.class, UpdateProvider.class, DeleteProvider.class};
    return chooseAnnotationType(method, types);
  }

  private Class chooseAnnotationType(Method method, Class[] types) {
    for (Class type : types) {
      Annotation annotation = method.getAnnotation(type);
      if (annotation != null) {
        return type;
      }
    }
    return null;
  }

  private void applyResults(String resultMapId, Result[] results) {
    if (results.length > 0) {
      for (Result result : results) {
        ArrayList<ResultFlag> flags = new ArrayList<ResultFlag>();
        if (result.id()) flags.add(ResultFlag.ID);
        configurator.resultMapping(
            result.property(),
            result.column(),
            result.javaType() == void.class ? null : result.javaType(),
            result.jdbcType() == JdbcType.UNDEFINED ? null : result.jdbcType(),
            hasNestedSelect(result) ? nestedSelectId(result) : null,
            hasCollectionOrAssociation(result) ? nestedResultMapId(resultMapId, result) : null,
            result.typeHandler() == void.class ? null : result.typeHandler(),
            flags);
      }
    }
  }

  private String nestedSelectId(Result result) {
    String nestedSelect = result.one().select();
    if (nestedSelect.length() < 1) {
      nestedSelect = result.many().select();
    }
    if (!nestedSelect.contains(".")) {
      nestedSelect = type.getName() + "." + nestedSelect;
    }
    return nestedSelect;
  }

  private boolean hasNestedSelect(Result result) {
    return result.one().select().length() > 0
        || result.many().select().length() > 0;
  }

  private void applyConstructorArgs(Arg[] args) {
    if (args.length > 0) {
      for (Arg arg : args) {
        ArrayList<ResultFlag> flags = new ArrayList<ResultFlag>();
        flags.add(ResultFlag.CONSTRUCTOR);
        if (arg.id()) flags.add(ResultFlag.ID);
        configurator.resultMapping(
            null,
            arg.column(),
            arg.javaType() == void.class ? null : arg.javaType(),
            arg.jdbcType() == JdbcType.UNDEFINED ? null : arg.jdbcType(),
            null,
            null,
            arg.typeHandler() == void.class ? null : arg.typeHandler(),
            flags);
      }
    }
  }

  private String nestedResultMapId(String resultMapId, Result result) {
    return resultMapId + "." + result.property();
  }

  private void ensureHasOnlyCollectionOrResultNotBoth(Result result) {
    if (hasCollection(result) && hasAssociation(result)) {
      throw new BindingException("On each result you can only use an association or a collection, not both!");
    }
  }

  private boolean hasCollectionOrAssociation(Result result) {
    return hasCollection(result) || hasAssociation(result);
  }

  private boolean hasAssociation(Result result) {
    return result.one().constructor().value().length > 1
        || result.one().results().value().length > 1;
  }

  private boolean hasCollection(Result result) {
    return result.many().constructor().value().length > 1
        || result.many().results().value().length > 1;
  }

  private Result[] resultsIf(Results results) {
    return results == null ? new Result[0] : results.value();
  }

  private Arg[] argsIf(ConstructorArgs args) {
    return args == null ? new Arg[0] : args.value();
  }

}
