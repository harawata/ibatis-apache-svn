package com.ibatis.sqlmap.engine.transaction.user;

import com.ibatis.sqlmap.engine.transaction.Transaction;
import com.ibatis.sqlmap.engine.transaction.TransactionException;
import com.ibatis.common.jdbc.logging.ConnectionLogProxy;

import java.sql.SQLException;
import java.sql.Connection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by IntelliJ IDEA.
 * User: Clinton
 * Date: 16-Jul-2004
 * Time: 9:38:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class UserProvidedTransaction implements Transaction {

  private static final Log connectionLog = LogFactory.getLog(Connection.class);

  private Connection connection;

  public UserProvidedTransaction(Connection connection) {
    if (connectionLog.isDebugEnabled()) {
      this.connection = ConnectionLogProxy.newInstance(connection);
    } else {
      this.connection = connection;
    }
  }

  public void commit() throws SQLException, TransactionException {
    connection.commit();
  }

  public void rollback() throws SQLException, TransactionException {
    connection.rollback();
  }

  public void close() throws SQLException, TransactionException {
    connection.close();
  }

  public Connection getConnection() throws SQLException, TransactionException {
    return connection;
  }

}
