package com.ibatis.sqlmap.engine.transaction.jta;

import com.ibatis.sqlmap.engine.transaction.*;
import com.ibatis.sqlmap.engine.transaction.Transaction;

import javax.transaction.UserTransaction;
import javax.transaction.Status;
import javax.sql.*;
import java.sql.*;

/**
 * User: Clinton Begin
 * Date: Sep 12, 2003
 * Time: 10:14:37 PM
 */
public class JtaTransaction implements Transaction {

  private UserTransaction userTransaction;
  private DataSource dataSource;
  private Connection connection;

  private boolean commmitted = false;
  private boolean newTransaction = false;

  public JtaTransaction(UserTransaction utx, DataSource ds) throws SQLException, TransactionException {
    // Check parameters
    userTransaction = utx;
    dataSource = ds;
    if (userTransaction == null) {
      throw new TransactionException("JtaTransaction initialization failed.  UserTransaction was null.");
    }
    if (dataSource == null) {
      throw new TransactionException("JtaTransaction initialization failed.  DataSource was null.");
    }

    // Start JTA Transaction
    try {
      newTransaction = userTransaction.getStatus() == Status.STATUS_NO_TRANSACTION;
      if (newTransaction) {
        userTransaction.begin();
      }
    } catch (Exception e) {
      throw new TransactionException("JtaTransaction could not start transaction.  Cause: ", e);
    }

    // Open JDBC Connection
    connection = dataSource.getConnection();
    if (connection == null) {
      throw new TransactionException("JtaTransaction could not start transaction.  Cause: The DataSource returned a null connection.");
    }
    if (connection.getAutoCommit()) {
      connection.setAutoCommit(false);
    }
  }

  public void commit() throws SQLException, TransactionException {
    if (commmitted) {
      throw new TransactionException("JtaTransaction could not commit because this transaction has already been committed.");
    }
    try {
      if (newTransaction) {
        userTransaction.commit();
      }
    } catch (Exception e) {
      throw new TransactionException("JtaTransaction could not commit.  Cause: ", e);
    }
    commmitted = true;
  }

  public void rollback() throws SQLException, TransactionException {
    if (!commmitted) {
      try {
        if (userTransaction != null) {
          if (newTransaction) {
            userTransaction.rollback();
          } else {
            userTransaction.setRollbackOnly();
          }
        }
      } catch (Exception e) {
        throw new TransactionException("JtaTransaction could not rollback.  Cause: ", e);
      }
    }
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
