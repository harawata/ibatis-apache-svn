package org.apache.ibatis.api;

import org.apache.ibatis.mapping.Configuration;
import org.apache.ibatis.mapping.ExecutorType;

import java.sql.Connection;

public interface SqlSessionFactory {

  SqlSession openSession();

  SqlSession openSession(boolean autoCommit);

  SqlSession openSession(Connection connection);

  SqlSession openSession(ExecutorType execType);

  SqlSession openSession(ExecutorType execType, boolean autoCommit);

  SqlSession openSession(ExecutorType execType, Connection connection);

  Configuration getConfiguration();

}
