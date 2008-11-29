package org.apache.ibatis.api;

import org.apache.ibatis.executor.result.ResultHandler;

import java.util.List;

public interface SqlSession {

  List selectList(String statement);
  List selectList(String statement, Object parameter);
  List selectList(String statement, Object parameter, int offset, int limit);
  List selectList(String statement, Object parameter, int offset, int limit, ResultHandler handler);
  void close();
  
}
