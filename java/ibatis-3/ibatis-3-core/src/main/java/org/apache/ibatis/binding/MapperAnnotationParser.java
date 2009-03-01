package org.apache.ibatis.binding;

import static org.apache.ibatis.annotations.Annotations.*;
import org.apache.ibatis.mapping.Configuration;
import org.apache.ibatis.mapping.ResultFlag;
import org.apache.ibatis.mapping.ResultSetType;
import org.apache.ibatis.mapping.StatementType;
import org.apache.ibatis.parser.MapperConfigurator;
import org.apache.ibatis.type.JdbcType;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Collection;

public class MapperAnnotationParser {

  private MapperConfigurator configurator;
  private Class type;
  private boolean hasResults;

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
    if (results != null || args != null) {
      String resultMapId = type.getName() + "." + method.getName();
      configurator.resultMapStart(resultMapId, getReturnType(method), null);
      applyConstructorArgs(args);
      applyResults(results);
      configurator.resultMapEnd();
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
          hasResults ? mappedStatementId : null,         // ResultMapID
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

  private void applyResults(Results results) {
    if (results != null) {
      for (Result result : results.value()) {
        ArrayList<ResultFlag> flags = new ArrayList<ResultFlag>();
        if (result.id()) flags.add(ResultFlag.ID);
        configurator.resultMapping(
            result.property(),
            result.column(),
            result.javaType() == void.class ? null : result.javaType(),
            result.jdbcType() == JdbcType.UNDEFINED ? null : result.jdbcType(),
            null,
            null,
            result.typeHandler() == void.class ? null : result.typeHandler(),
            flags);
      }
      hasResults = true;
    }
  }

  private void applyConstructorArgs(ConstructorArgs args) {
    if (args != null) {
      for (Arg arg : args.value()) {
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
      hasResults = true;
    }
  }

}
