package com.ibatis.sqlmap.engine.transaction;

import java.sql.*;

/**
 * User: Clinton Begin
 * Date: Sep 12, 2003
 * Time: 10:13:04 PM
 */
public interface Transaction {

  public void commit() throws SQLException, TransactionException;

  public void rollback() throws SQLException, TransactionException;

  public void close() throws SQLException, TransactionException;

  public Connection getConnection() throws SQLException, TransactionException;

}
