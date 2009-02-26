package org.apache.ibatis.binding;

import static org.apache.ibatis.annotations.Annotations.*;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.parser.MapperConfigurator;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
      parseResults(method);
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

  private void parseResults(Method method) {
    Results results = method.getAnnotation(Results.class);
    if (results != null) {
      String resultMapId = type.getName() + "." + method.getName();
      configurator.resultMapStart(resultMapId,getReturnType(method),null);
      for (Result result : results.value()) {
        configurator.resultMapping(
            result.property(),
            result.column(),
            result.javaType() == void.class ? null : result.javaType(),
            result.jdbcType() == JdbcType.UNDEFINED ? null : result.jdbcType(),
            null,
            null,
            result.typeHandler() == void.class ? null : result.typeHandler(),
            null);
      }
      configurator.resultMapEnd();
      hasResults = true;
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
    if (returnType.isAssignableFrom(Collection.class)) {
      TypeVariable<? extends Class<?>>[] returnTypeVariables = returnType.getTypeParameters();
      if (returnTypeVariables.length == 1) {
        returnType = returnTypeVariables[0].getGenericDeclaration();
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


}
