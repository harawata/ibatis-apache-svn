package com.ibatis.dao.engine.transaction.jta;

import com.ibatis.dao.client.DaoTransaction;
import com.ibatis.dao.client.DaoException;
import com.ibatis.common.jdbc.logging.ConnectionLogProxy;

import javax.sql.DataSource;
import javax.transaction.UserTransaction;
import javax.transaction.Status;
import java.sql.SQLException;
import java.sql.Connection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 *
 * <p>
 * Date: Jan 27, 2004 10:48:31 PM
 * @author Clinton Begin
 */
public class JtaDaoTransaction implements DaoTransaction {

  private static final Log connectionLog = LogFactory.getLog(Connection.class);

  private UserTransaction userTransaction;
  private DataSource dataSource;
  private Connection connection;

  private boolean commmitted = false;
  private boolean newTransaction = false;

  public JtaDaoTransaction(UserTransaction utx, DataSource ds) {
    // Check parameters
    userTransaction = utx;
    dataSource = ds;
    if (userTransaction == null) {
      throw new DaoException("JtaTransaction initialization failed.  UserTransaction was null.");
    }
    if (dataSource == null) {
      throw new DaoException("JtaTransaction initialization failed.  DataSource was null.");
    }

    // Start JTA Transaction
    try {
      newTransaction = userTransaction.getStatus() == Status.STATUS_NO_TRANSACTION;
      if (newTransaction) {
        userTransaction.begin();
      }
    } catch (Exception e) {
      throw new DaoException("JtaTransaction could not start transaction.  Cause: ", e);
    }

    try {
      // Open JDBC Connection
      connection = dataSource.getConnection();
      if (connection == null) {
        throw new DaoException("Could not start transaction.  Cause: The DataSource returned a null connection.");
      }
      if (connection.getAutoCommit()) {
        connection.setAutoCommit(false);
      }
      if (connectionLog.isDebugEnabled()) {
        connection = ConnectionLogProxy.newInstance(connection);
      }
    } catch (SQLException e) {
      throw new DaoException("Error opening JDBC connection.  Cause: " + e, e);
    }
  }

  public void commit() {
    if (commmitted) {
      throw new DaoException("JtaTransaction could not commit because this transaction has already been committed.");
    }
    try {
      if (newTransaction) {
        userTransaction.commit();
      }
    } catch (Exception e) {
      throw new DaoException("JtaTransaction could not commit.  Cause: ", e);
    }
    commmitted = true;
  }

  public void rollback() {
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
        throw new DaoException("JTA transaction could not rollback.  Cause: ", e);
      }
    }

  }


  public void close() throws SQLException {
    if (connection != null) {
      connection.close();
      connection = null;
    }
  }

  public Connection getConnection() {
    return connection;
  }


}
