package com.ibatis.sqlmap.engine.transaction.jdbc;

import com.ibatis.sqlmap.engine.transaction.*;
import com.ibatis.common.jdbc.logging.ConnectionLogProxy;

import javax.sql.*;
import java.sql.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * User: Clinton Begin
 * Date: Sep 12, 2003
 * Time: 10:14:24 PM
 */
public class JdbcTransaction implements Transaction {

  private static final Log connectionLog = LogFactory.getLog(Connection.class);

  private DataSource dataSource;
  private Connection connection;

  public JdbcTransaction(DataSource ds) throws TransactionException {
    // Check Parameters
    dataSource = ds;
    if (dataSource == null) {
      throw new TransactionException("JdbcTransaction initialization failed.  DataSource was null.");
    }
  }

  private void init() throws SQLException, TransactionException {
    // Open JDBC Transaction
    connection = dataSource.getConnection();
    if (connection == null) {
      throw new TransactionException("JdbcTransaction could not start transaction.  Cause: The DataSource returned a null connection.");
    }
    if (connection.getAutoCommit()) {
      connection.setAutoCommit(false);
    }
    if (connectionLog.isDebugEnabled()) {
      connection = ConnectionLogProxy.newInstance(connection);
    }
  }

  public void commit() throws SQLException, TransactionException {
    if (connection != null) {
      connection.commit();
    }
  }

  public void rollback() throws SQLException, TransactionException {
    if (connection != null) {
      connection.rollback();
    }
  }

  public void close() throws SQLException, TransactionException {
    if (connection != null) {
      connection.close();
      connection = null;
    }
  }

  public Connection getConnection() throws SQLException, TransactionException {
    if (connection == null) {
      init();
    }
    return connection;
  }

}
