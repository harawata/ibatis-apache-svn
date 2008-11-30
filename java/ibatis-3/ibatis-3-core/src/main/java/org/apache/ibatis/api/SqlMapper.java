package org.apache.ibatis.api;

import org.apache.ibatis.mapping.Configuration;

import java.sql.Connection;

public interface SqlMapper {

  SqlSession openSession();
  
  SqlSession openSession(boolean autoCommit);

  SqlSession openSession(Connection connection);

  Configuration getConfiguration();

}
