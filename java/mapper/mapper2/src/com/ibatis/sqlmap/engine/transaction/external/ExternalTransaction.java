package com.ibatis.sqlmap.engine.transaction.external;

import com.ibatis.sqlmap.engine.transaction.*;

import javax.sql.*;
import java.sql.*;

/**
 * User: Clinton Begin
 * Date: Sep 13, 2003
 * Time: 6:33:22 AM
 */
public class ExternalTransaction implements Transaction {

  private DataSource dataSource;
  private Connection connection;

  public ExternalTransaction(DataSource ds) throws SQLException, TransactionException {
    // Check Parameters
    dataSource = ds;
    if (dataSource == null) {
      throw new TransactionException("ExternalTransaction initialization failed.  DataSource was null.");
    }

    // Open JDBC Transaction
    connection = dataSource.getConnection();
    if (connection == null) {
      throw new TransactionException("ExternalTransaction could not start transaction.  Cause: The DataSource returned a null connection.");
    }
    if (connection.getAutoCommit()) {
      connection.setAutoCommit(false);
    }
  }

  public void commit() throws SQLException, TransactionException {
  }

  public void rollback() throws SQLException, TransactionException {
  }

  public void close() throws SQLException, TransactionException {
    if (connection != null) {
      connection.close();
      connection = null;
    }
  }

  public Connection getConnection() throws SQLException, TransactionException {
    return connection;
  }

}
