package org.apache.ibatis.binding;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.mapping.*;
import static org.apache.ibatis.annotations.Annotations.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.parser.InlineSqlSource;
import org.apache.ibatis.parser.SqlSourceParser;
import org.apache.ibatis.xml.GenericTokenParser;

import java.util.*;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
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
    if (cacheDomain != null) {
      CacheBuilder builder = new CacheBuilder(type.getName() + "-Cache");
      builder.clearInterval(cacheDomain.flushInterval());
      builder.size(cacheDomain.size());
      builder.readWrite(cacheDomain.readWrite());
      builder.implementation(cacheDomain.implementation());
      builder.addDecorator(cacheDomain.eviction());
      return builder.build();
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
      String sql = getSqlAnnotationValue(method, annotationType);
      String mappedStatementId = method.getDeclaringClass().getName() + "." + method.getName();
      SqlSource sqlSource = new BasicSqlSource(sql);
      MappedStatement.Builder builder = new MappedStatement.Builder(config, mappedStatementId, sqlSource);
      builder.resource(method.getDeclaringClass().getName().replace('.', '/') + ".java (best guess)");

      Options options = method.getAnnotation(Options.class);
      if (options != null) {
        builder.useCache(options.useCache());
        builder.flushCacheRequired(options.flushCache());
        builder.resultSetType(options.resultSetType());
        builder.statementType(options.statementType());
        builder.fetchSize(options.fetchSize());
        builder.timeout(options.timeout());
      }

      builder.cache(cache);

      Class returnType = method.getReturnType();
      if (returnType.isAssignableFrom(Collection.class)) {
        TypeVariable<? extends Class<?>>[] returnTypeVariables = returnType.getTypeParameters();
        if (returnTypeVariables.length == 1) {
          returnType = returnTypeVariables[0].getGenericDeclaration();
        }
      }

      builder.parameterMap(new ParameterMap.Builder(config, "", Object.class, new ArrayList<ParameterMapping>()).build());
      final ResultMap resultMap = new ResultMap.Builder(config, "", returnType, new ArrayList<ResultMapping>()).build();
      builder.resultMaps(new ArrayList<ResultMap>() {
        {
          add(resultMap);
        }
      });

      return builder.build();
    }
    return null;
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
