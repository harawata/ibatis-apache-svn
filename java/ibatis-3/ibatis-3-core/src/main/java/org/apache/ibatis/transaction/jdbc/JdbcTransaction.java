package org.apache.ibatis.transaction.jdbc;

import org.apache.ibatis.transaction.Transaction;

import java.sql.Connection;
import java.sql.SQLException;

public class JdbcTransaction implements Transaction {

  private Connection connection;

  public JdbcTransaction(Connection connection) {
    this.connection = connection;
  }

  public Connection getConnection() {
    return connection;
  }

  public void commit() throws SQLException {
    connection.commit();
  }

  public void rollback() throws SQLException {
    connection.rollback();
  }

  public void close() throws SQLException {
    connection.close();
  }
}
