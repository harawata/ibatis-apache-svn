package org.apache.ibatis.binding;

import static org.apache.ibatis.annotations.Annotations.*;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.parser.MapperConfigurator;

import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;
import java.lang.annotation.Annotation;
import java.util.Collection;

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
    parseCache(type, configurator);
    parseCacheRef(type, configurator);
    Method[] methods = type.getMethods();
    for (Method method : methods) {
      parseStatement(method);
    }
  }
  private void parseCache(Class type, MapperConfigurator mapperConfigurator) {
    CacheDomain cacheDomain = (CacheDomain) type.getAnnotation(CacheDomain.class);
    if (cacheDomain != null) {
      mapperConfigurator.cache(cacheDomain.implementation(), cacheDomain.eviction(), cacheDomain.flushInterval(), cacheDomain.size(), !cacheDomain.readWrite(), null);
    }
  }

  private void parseCacheRef(Class type, MapperConfigurator mapperConfigurator) {
    CacheDomainRef cacheDomainRef = (CacheDomainRef) type.getAnnotation(CacheDomainRef.class);
    if (cacheDomainRef != null) {
      mapperConfigurator.cacheRef(cacheDomainRef.value().getName());
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
          null,         // ResultMapID
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
