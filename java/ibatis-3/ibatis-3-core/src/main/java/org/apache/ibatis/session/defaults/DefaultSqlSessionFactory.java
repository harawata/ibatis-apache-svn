package org.apache.ibatis.session.defaults;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.exceptions.ExceptionFactory;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.logging.jdbc.ConnectionLogger;
import org.apache.ibatis.mapping.Configuration;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.mapping.ExecutorType;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.transaction.TransactionFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DefaultSqlSessionFactory implements SqlSessionFactory {

  private static final Log log = LogFactory.getLog(Connection.class);

  private final Configuration configuration;
  private Environment environment;
  private DataSource dataSource;
  private TransactionFactory transactionFactory;

  public DefaultSqlSessionFactory(Configuration configuration) {
    this.configuration = configuration;
    this.environment = configuration.getEnvironment();
    this.dataSource = environment.getDataSource();
    this.transactionFactory = environment.getTransactionFactory();
  }

  public SqlSession openSession() {
    return openSession(configuration.getDefaultExecutorType(), false);
  }

  public SqlSession openSession(boolean autoCommit) {
    return openSession(configuration.getDefaultExecutorType(), autoCommit);
  }

  public SqlSession openSession(ExecutorType execType) {
    return openSession(execType, false);
  }

  public SqlSession openSession(ExecutorType execType, boolean autoCommit) {
    try {
      Connection connection = dataSource.getConnection();
      connection = wrapConnection(connection);
      Transaction tx = transactionFactory.newTransaction(connection, autoCommit);
      Executor executor = configuration.newExecutor(tx, execType);
      return new DefaultSqlSession(configuration, executor, autoCommit);
    } catch (SQLException e) {
      throw ExceptionFactory.wrapException("Error opening session.  Cause: " + e, e);
    }
  }

  public SqlSession openSession(Connection connection) {
    return openSession(configuration.getDefaultExecutorType(), connection);
  }

  public SqlSession openSession(ExecutorType execType, Connection connection) {
    try {
      boolean autoCommit;
      try {
        autoCommit = connection.getAutoCommit();
      } catch (SQLException e) {
        // Failover to true, as most poor drivers
        // or databases won't support transactions
        autoCommit = true;
      }
      connection = wrapConnection(connection);
      Transaction tx = transactionFactory.newTransaction(connection, autoCommit);
      Executor executor = configuration.newExecutor(tx, execType);
      return new DefaultSqlSession(configuration, executor, autoCommit);
    } catch (Exception e) {
      throw ExceptionFactory.wrapException("Error opening session.  Cause: " + e, e);
    }
  }

  public Configuration getConfiguration() {
    return configuration;
  }

  private Connection wrapConnection(Connection connection) {
    if (log.isDebugEnabled()) {
      return ConnectionLogger.newInstance(connection);
    } else {
      return connection;
    }
  }

}

