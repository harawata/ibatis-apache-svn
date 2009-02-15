package org.apache.ibatis.binding;

import org.apache.ibatis.api.SqlSession;

public class MapperFactory {

  public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
    try {
      return MapperProxy.newMapperProxy(type, sqlSession);
    } catch (Exception e) {
      throw new RuntimeException("Error getting mapper instance. Cause: " + e, e);
    }
  }

}
