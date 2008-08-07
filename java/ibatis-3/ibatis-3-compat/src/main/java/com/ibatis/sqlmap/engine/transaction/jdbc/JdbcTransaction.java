package com.ibatis.sqlmap.engine.transaction.jdbc;

import com.ibatis.sqlmap.engine.transaction.*;
import org.apache.ibatis.mapping.Configuration;
import org.apache.ibatis.executor.Executor;

import javax.sql.DataSource;
import java.sql.*;

public class JdbcTransaction extends BaseTransaction {

  private DataSource dataSource;
  private IsolationLevel isolationLevel = new IsolationLevel();
  private Configuration configuration;
  private Executor executor;

  public JdbcTransaction(Configuration configuration, DataSource ds, int isolationLevel) throws TransactionException {
    // Check Parameters
    this.configuration = configuration;
    dataSource = ds;
    if (dataSource == null) {
      throw new TransactionException("JdbcTransaction initialization failed.  DataSource was null.");
    }
    this.isolationLevel.setIsolationLevel(isolationLevel);
  }

  private void init() throws SQLException, TransactionException {
    // Open JDBC Transaction
    Connection connection = dataSource.getConnection();
    if (connection == null) {
      throw new TransactionException("JdbcTransaction could not start transaction.  Cause: The DataSource returned a null connection.");
    }
    // Isolation Level
    isolationLevel.applyIsolationLevel(connection);
    // AutoCommit
    if (connection.getAutoCommit()) {
      connection.setAutoCommit(false);
    }
    executor = configuration.newExecutor(connection);
  }

  public void commit(boolean required) throws SQLException, TransactionException {
    if (executor != null) {
      executor.commit(required);
    }
  }

  public void rollback(boolean required) throws SQLException, TransactionException {
    if (executor != null) {
      executor.rollback(required);
    }
  }

  public void close() throws SQLException, TransactionException {
    if (executor != null) {
      try {
        isolationLevel.restoreIsolationLevel(executor.getConnection());
      } finally {
        executor.close();
        executor = null;
      }
    }
  }

  public Executor getExecutor() throws SQLException, TransactionException {
    if (executor == null) {
      init();
    }
    return executor;
  }

}
