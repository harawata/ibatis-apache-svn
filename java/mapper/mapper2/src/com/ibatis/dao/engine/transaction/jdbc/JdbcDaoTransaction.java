package com.ibatis.dao.engine.transaction.jdbc;

import com.ibatis.dao.client.DaoTransaction;
import com.ibatis.dao.client.DaoException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 *
 *
 * <p>
 * Date: Jan 27, 2004 10:48:48 PM
 * @author Clinton Begin
 */
public class JdbcDaoTransaction implements DaoTransaction {

  private Connection connection;

  public JdbcDaoTransaction(DataSource dataSource) {
    try {
      connection = dataSource.getConnection();
    } catch (SQLException e) {
      throw new DaoException("Error starting JDBC transaction.  Cause: " + e);
    }
  }


  public void commit() {
    try {
      try {
        connection.commit();
      } finally {
        connection.close();
      }
    } catch (SQLException e) {
      throw new DaoException("Error committing JDBC transaction.  Cause: " + e);
    }
  }

  public void rollback() {
    try {
      try {
        connection.rollback();
      } finally {
        connection.close();
      }
    } catch (SQLException e) {
      throw new DaoException("Error ending JDBC transaction.  Cause: " + e);
    }
  }

  public Connection getConnection() {
    return connection;
  }

}
