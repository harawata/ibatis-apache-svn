package com.ibatis.sqlmap.engine.transaction.external;

import com.ibatis.sqlmap.engine.transaction.*;
import com.ibatis.common.jdbc.logging.ConnectionLogProxy;

import javax.sql.*;
import java.sql.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * User: Clinton Begin
 * Date: Sep 13, 2003
 * Time: 6:33:22 AM
 */
public class ExternalTransaction implements Transaction {

  private static final Log connectionLog = LogFactory.getLog(Connection.class);

  private DataSource dataSource;
  private boolean defaultAutoCommit;
  private boolean setAutoCommitAllowed;
  private Connection connection;

  public ExternalTransaction(DataSource ds, boolean defaultAutoCommit, boolean setAutoCommitAllowed) throws TransactionException {
    // Check Parameters
    dataSource = ds;
    if (dataSource == null) {
      throw new TransactionException("ExternalTransaction initialization failed.  DataSource was null.");
    }

    this.defaultAutoCommit = defaultAutoCommit;
    this.setAutoCommitAllowed = setAutoCommitAllowed;
  }

  private void init() throws SQLException, TransactionException {
    // Open JDBC Transaction
    connection = dataSource.getConnection();
    if (connection == null) {
      throw new TransactionException("ExternalTransaction could not start transaction.  Cause: The DataSource returned a null connection.");
    }
    if (setAutoCommitAllowed) {
      if (connection.getAutoCommit() != defaultAutoCommit) {
        connection.setAutoCommit(defaultAutoCommit);
      }
    }
    if (connectionLog.isDebugEnabled()) {
      connection = ConnectionLogProxy.newInstance(connection);
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
    if (connection == null) {
      init();
    }
    return connection;
  }

}
