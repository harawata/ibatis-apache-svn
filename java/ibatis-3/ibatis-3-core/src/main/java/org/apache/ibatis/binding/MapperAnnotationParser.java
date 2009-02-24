package org.apache.ibatis.binding;

import static org.apache.ibatis.annotations.Annotations.*;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.parser.MapperConfigurator;
import org.apache.ibatis.parser.SqlSourceParser;
import org.apache.ibatis.cache.Cache;

import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;

public class MapperAnnotationParser {

  private MapperConfigurator configurator;
  private Configuration config;
  private Class type;

  public MapperAnnotationParser(Configuration config, Class type) {
    this.configurator = new MapperConfigurator(config, type.getName() + ".java");
    this.config = config;
    this.type = type;
  }

  public void parse() {
    configurator.namespace(type.getName());
    parseCache(type, configurator);
    parseCacheRef(type, configurator);
    parseMethodAnnotations(type);
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

  private void parseMethodAnnotations(Class type) {
    Method[] methods = type.getMethods();
    for (Method method : methods) {
      MappedStatement statement = parseMappedStatement(method);
      if (statement != null) {
        config.addMappedStatement(statement);
      }
    }
  }

  private MappedStatement parseMappedStatement(Method method) {
    Class annotationType = getSqlAnnotationType(method);
    Options options = method.getAnnotation(Options.class);

    if (annotationType != null) {
      final String sql = getSqlAnnotationValue(method, annotationType);
      final String mappedStatementId = method.getDeclaringClass().getName() + "." + method.getName();
      final SqlSource sqlSource = new SqlSourceParser(config).parse(sql);

      MappedStatement.Builder builder = new MappedStatement.Builder(config, mappedStatementId, sqlSource);
      builder.resource(method.getDeclaringClass().getName().replace('.', '/') + ".java (best guess)");
      setOptions(method, builder);
      setParameterMap(method, mappedStatementId, builder);
      setResultMaps(method, mappedStatementId, builder);

      //configurator.statement(mappedStatementId,sql,options.fetchSize(),options.timeout(),options);

      return builder.build();
    }
    return null;
  }

  private void setResultMaps(Method method, final String mappedStatementId, MappedStatement.Builder builder) {
    final Class returnType = getReturnType(method);

    Results results = method.getAnnotation(Results.class);


    builder.resultMaps(new ArrayList<ResultMap>() {
      {
        add(new ResultMap.Builder(
            config,
            mappedStatementId + "-BoundResultMap",
            returnType,
            new ArrayList<ResultMapping>()).build());
      }
    });
  }

  private void setParameterMap(Method method, String mappedStatementId, MappedStatement.Builder builder) {
    final Class parameterType = getParameterType(method);
    builder.parameterMap(new ParameterMap.Builder(
        config,
        mappedStatementId + "-BoundParameterMap",
        parameterType,
        new ArrayList<ParameterMapping>()).build());
  }

  private void setOptions(Method method, MappedStatement.Builder builder) {
    Options options = method.getAnnotation(Options.class);
    if (options != null) {
      builder.useCache(options.useCache());
      builder.flushCacheRequired(options.flushCache());
      builder.resultSetType(options.resultSetType());
      builder.statementType(options.statementType());
      builder.fetchSize(options.fetchSize());
      builder.timeout(options.timeout());
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
    Class[] types = {Select.class, Insert.class, Update.class, Delete.class, Procedure.class, Statement.class};
    for (Class type : types) {
      Annotation annotation = method.getAnnotation(type);
      if (annotation != null) {
        return type;
      }
    }
    return null;
  }


}
