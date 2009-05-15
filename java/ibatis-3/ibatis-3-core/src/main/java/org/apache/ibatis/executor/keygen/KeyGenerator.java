package org.apache.ibatis.executor.keygen;

import org.apache.ibatis.mapping.MappedStatement;

import java.sql.*;

public interface KeyGenerator {

  void processGeneratedKeys(MappedStatement ms, Statement stmt, Object parameter);


}
