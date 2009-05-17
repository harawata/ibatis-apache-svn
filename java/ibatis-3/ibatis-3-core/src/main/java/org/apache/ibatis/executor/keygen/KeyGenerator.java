package org.apache.ibatis.executor.keygen;

import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.executor.Executor;

import java.sql.*;

public interface KeyGenerator {

  boolean executeBefore();

  boolean executeAfter();

  void processGeneratedKeys(Executor executor, MappedStatement ms, Statement stmt, Object parameter);

}
