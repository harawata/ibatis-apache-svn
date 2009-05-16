package org.apache.ibatis.executor.keygen;

import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.executor.Executor;

import java.sql.*;

public interface KeyGenerator {

  void processGeneratedKeys(Executor executor, MappedStatement ms, Statement stmt, Object parameter);


}
