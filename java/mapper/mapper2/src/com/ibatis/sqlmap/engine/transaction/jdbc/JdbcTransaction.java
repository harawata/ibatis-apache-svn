package com.ibatis.sqlmap.engine.transaction.jdbc;

import com.ibatis.sqlmap.engine.transaction.*;

import javax.sql.*;
import java.sql.*;

/**
 * User: Clinton Begin
 * Date: Sep 12, 2003
 * Time: 10:14:24 PM
 */
public class JdbcTransaction implements Transaction {

  private DataSource dataSource;
  private Connection connection;

  public JdbcTransaction(DataSource ds) throws SQLException, TransactionException {
    // Check Parameters
    dataSource = ds;
    if (dataSource == null) {
      throw new TransactionException("JdbcTransaction initialization failed.  DataSource was null.");
    }

    // Open JDBC Transaction
    connection = dataSource.getConnection();
    if (connection == null) {
      throw new TransactionException("JdbcTransaction could not start transaction.  Cause: The DataSource returned a null connection.");
    }
    if (connection.getAutoCommit()) {
      connection.setAutoCommit(false);
    }
  }

  public void commit() throws SQLException, TransactionException {
    connection.commit();
  }

  public void rollback() throws SQLException, TransactionException {
    connection.rollback();
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
