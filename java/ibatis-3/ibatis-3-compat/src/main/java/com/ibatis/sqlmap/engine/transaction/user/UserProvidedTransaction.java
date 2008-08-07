package com.ibatis.sqlmap.engine.transaction.user;

import com.ibatis.sqlmap.engine.transaction.*;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.Configuration;

import java.sql.*;

public class UserProvidedTransaction extends BaseTransaction {

  private Executor executor;

  public UserProvidedTransaction(Configuration configuration, Connection connection) {
    this.executor = configuration.newExecutor(connection);
  }

  public void commit(boolean required) throws SQLException, TransactionException {
    executor.commit(required);
  }

  public void rollback(boolean required) throws SQLException, TransactionException {
    executor.rollback(required);
  }

  public void close() throws SQLException, TransactionException {
  }

  public Executor getExecutor() throws SQLException, TransactionException {
    return executor;
  }

}
