package org.apache.ibatis.api;

import org.apache.ibatis.executor.result.ResultHandler;

import java.util.List;

public interface SqlSession {

  Object selectOne(String statement);
  Object selectOne(String statement, Object parameter);

  List selectList(String statement);
  List selectList(String statement, Object parameter);
  List selectList(String statement, Object parameter, int offset, int limit);
  List selectList(String statement, Object parameter, int offset, int limit, ResultHandler handler);

  Object insert(String statement);
  Object insert(String statement, Object parameter);

  int update(String statement);
  int update(String statement, Object parameter);

  int delete(String statement);
  int delete(String statement, Object parameter);

  void commit();
  void commit(boolean force);
  void rollback();
  void rollback(boolean force);
  void close();

}
