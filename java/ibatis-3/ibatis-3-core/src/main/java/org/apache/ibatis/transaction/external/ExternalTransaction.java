package org.apache.ibatis.transaction.external;

import org.apache.ibatis.transaction.Transaction;

import java.sql.Connection;
import java.sql.SQLException;

public class ExternalTransaction implements Transaction {

  private Connection connection;

  public ExternalTransaction(Connection connection) {
    this.connection = connection;
  }

  public Connection getConnection() {
    return connection;
  }

  public void commit() throws SQLException {
    // Does nothing
  }

  public void rollback() throws SQLException {
    // Does nothing
  }
}
