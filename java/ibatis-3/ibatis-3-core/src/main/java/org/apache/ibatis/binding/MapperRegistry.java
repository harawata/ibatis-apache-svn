package org.apache.ibatis.binding;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.mapping.*;
import static org.apache.ibatis.annotations.Annotations.*;
import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.parser.SqlSourceParser;

import java.util.*;
import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;
import java.lang.annotation.Annotation;

public class MapperRegistry {

  private Configuration config;
  private Set<Class> knownMappers = new HashSet<Class>();

  public MapperRegistry(Configuration config) {
    this.config = config;
  }

  public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
    if (!knownMappers.contains(type))
      throw new BindingException("Type " + type + " is not known to the MapperRegistry.");
    try {
      return MapperProxy.newMapperProxy(type, sqlSession);
    } catch (Exception e) {
      throw new RuntimeException("Error getting mapper instance. Cause: " + e, e);
    }
  }

  public void addMapper(Class type) {
    if (!type.isInterface())
      throw new BindingException("Only interfaces can be configured by the MapperFactory.  Type " + type + " is not an interface.");
    if (knownMappers.contains(type))
      throw new BindingException("Type " + type + " is already known to the MapperRegistry.");
    knownMappers.add(type);
    parseAnnotations(type);
  }

  public void parseAnnotations(Class type) {
    Cache cache = parseCache(type);
    parseMethodAnnotations(cache, type);
  }

  private Cache parseCache(Class type) {
    CacheDomain cacheDomain = (CacheDomain) type.getAnnotation(CacheDomain.class);
    CacheDomainRef cacheDomainRef = (CacheDomainRef) type.getAnnotation(CacheDomainRef.class);
    if (cacheDomain != null) {
      String cacheId = type.getName() + "-BoundCache";
      CacheBuilder builder = new CacheBuilder(cacheId);
      builder.clearInterval(cacheDomain.flushInterval());
      builder.size(cacheDomain.size());
      builder.readWrite(cacheDomain.readWrite());
      builder.implementation(cacheDomain.implementation());
      builder.addDecorator(cacheDomain.eviction());
      return builder.build();
    } else if (cacheDomain != null) {
      String cacheRefId = cacheDomainRef.value().getName();
      Cache cache = config.getCache(cacheRefId);
      if (cache == null) {
        throw new BindingException("No cache exists in namespace "+ cacheRefId + ".  Be sure to register the referenced cache namespace first when building mappers.");
      }
      return cache;
    }
    return null;
  }

  private void parseMethodAnnotations(Cache cache, Class type) {
    Method[] methods = type.getMethods();
    for (Method method : methods) {
      MappedStatement statement = parseMappedStatement(method, cache);
      if (statement != null) {
        config.addMappedStatement(statement);
      }
    }
  }

  private MappedStatement parseMappedStatement(Method method, Cache cache) {
    Class annotationType = getSqlAnnotationType(method);
    if (annotationType != null) {
      final String sql = getSqlAnnotationValue(method, annotationType);
      final String mappedStatementId = method.getDeclaringClass().getName() + "." + method.getName();
      final SqlSource sqlSource = new SqlSourceParser(config).parse(sql);

      MappedStatement.Builder builder = new MappedStatement.Builder(config, mappedStatementId, sqlSource);
      builder.resource(method.getDeclaringClass().getName().replace('.', '/') + ".java (best guess)");
      builder.cache(cache);
      setOptions(method, builder);
      setParameterMap(method, mappedStatementId, builder);
      setResultMaps(method, mappedStatementId, builder);

      return builder.build();
    }
    return null;
  }

  private void setResultMaps(Method method, final String mappedStatementId, MappedStatement.Builder builder) {
    final Class returnType = getReturnType(method);

    Results results = method.getAnnotation(Results.class);
    

    builder.resultMaps(new ArrayList<ResultMap>() {{
      add(new ResultMap.Builder(
        config,
        mappedStatementId+"-BoundResultMap",
        returnType,
        new ArrayList<ResultMapping>()).build());
    }});
  }

  private void setParameterMap(Method method, String mappedStatementId, MappedStatement.Builder builder) {
    final Class parameterType = getParameterType(method);
    builder.parameterMap(new ParameterMap.Builder(
        config,
        mappedStatementId+"-BoundParameterMap",
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
    if (parameterTypes.length ==1 || parameterTypes.length == 3) {
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
