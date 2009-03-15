package org.apache.ibatis.binding;

import static org.apache.ibatis.annotations.Annotations.*;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.parser.MapperConfigurator;
import org.apache.ibatis.reflection.MetaClass;
import org.apache.ibatis.type.JdbcType;

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
    configurator.namespace(type.getName());
    parseCache();
    parseCacheRef();
    Method[] methods = type.getMethods();
    for (Method method : methods) {
      parseResultsAndConstructorArgs(method);
      parseStatement(method);
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
    ConstructorArgs args = method.getAnnotation(ConstructorArgs.class);
    Results results = method.getAnnotation(Results.class);
    Class returnType = getReturnType(method);
    String resultMapId = type.getName() + "." + method.getName();
    if (hasResults(method)) {
      TypeDiscriminator typeDiscriminator = method.getAnnotation(TypeDiscriminator.class);
      applyResultMap(resultMapId, returnType, argsIf(args), resultsIf(results), typeDiscriminator);
    }
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

      }
    }
  }

  private void applyDiscriminator(String resultMapId, TypeDiscriminator discriminator) {
    if (discriminator != null) {
      String column = discriminator.column();
      Class javaType = discriminator.javaType();
      JdbcType jdbcType = discriminator.jdbcType();
      Class typeHandler = discriminator.typeHandler();
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
    Class annotationType = getSqlAnnotationType(method);
    Options options = method.getAnnotation(Options.class);
    if (annotationType != null) {
      final String sql = getSqlAnnotationValue(method, annotationType);
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
          sql,
          fetchSize,
          timeout,
          null,         // ParameterMapID
          getParameterType(method),
          hasResults(method) ? mappedStatementId : null,         // ResultMapID
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

  private String getSqlAnnotationValue(Method method, Class annotationType) {
    Annotation annotation = method.getAnnotation(annotationType);
    if (annotation != null) {
      try {
        String[] strings = (String[]) annotation.getClass().getMethod("value").invoke(annotation);
        StringBuilder sql = new StringBuilder();
        for (String fragment : strings) {
          sql.append(fragment);
          sql.append(" ");
        }
        return sql.toString();
      } catch (Exception e) {
        throw new RuntimeException("Could not find value method on SQL annotation.  Cause: " + e, e);
      }
    }
    throw new BindingException("Reuested value from annotation that does not exist: " + annotationType);
  }

  private Class getSqlAnnotationType(Method method) {
    Class[] types = {Select.class, Insert.class, Update.class, Delete.class};
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

  private boolean hasResults(Method method) {
    ConstructorArgs args = method.getAnnotation(ConstructorArgs.class);
    Results results = method.getAnnotation(Results.class);
    return results != null || args != null;
  }

  private Result[] resultsIf(Results results) {
    return results == null ? new Result[0] : results.value();
  }

  private Arg[] argsIf(ConstructorArgs args) {
    return args == null ? new Arg[0] : args.value();
  }

}
