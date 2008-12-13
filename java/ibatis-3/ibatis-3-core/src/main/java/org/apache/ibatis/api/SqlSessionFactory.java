package org.apache.ibatis.api;

import org.apache.ibatis.mapping.*;

import java.sql.Connection;

public interface SqlSessionFactory {

  SqlSession openSession();
  SqlSession openSession(boolean autoCommit);
  SqlSession openSession(ExecutorType execType);
  SqlSession openSession(Connection connection);
  SqlSession openSession(boolean autoCommit, ExecutorType execType);
  SqlSession openSession(Connection connection, ExecutorType execType);
  Configuration getConfiguration();

}
