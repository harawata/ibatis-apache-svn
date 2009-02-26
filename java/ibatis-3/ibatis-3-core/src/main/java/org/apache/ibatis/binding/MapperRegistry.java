package org.apache.ibatis.binding;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.mapping.*;

import java.util.*;

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
    new MapperAnnotationParser(config,type).parse();
  }
}
