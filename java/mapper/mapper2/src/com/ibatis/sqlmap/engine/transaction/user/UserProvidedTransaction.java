package com.ibatis.sqlmap.engine.transaction.user;

import com.ibatis.sqlmap.engine.transaction.Transaction;
import com.ibatis.sqlmap.engine.transaction.TransactionException;

import java.sql.SQLException;
import java.sql.Connection;

/**
 * Created by IntelliJ IDEA.
 * User: Clinton
 * Date: 16-Jul-2004
 * Time: 9:38:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class UserProvidedTransaction implements Transaction {

  private Connection connection;

  public UserProvidedTransaction(Connection connection) {
    this.connection = connection;
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
