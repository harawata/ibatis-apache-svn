package org.apache.ibatis.api.defaults;

import org.apache.ibatis.api.SqlSessionFactory;
import org.apache.ibatis.api.SqlSession;
import org.apache.ibatis.api.exceptions.ExceptionFactory;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.executor.Executor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DefaultSqlSessionFactory implements SqlSessionFactory {

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
    return openSession(false, configuration.getDefaultExecutorType());
  }

  public SqlSession openSession(boolean autoCommit) {
    return openSession(autoCommit, configuration.getDefaultExecutorType());
  }

  public SqlSession openSession(ExecutorType execType) {
    return openSession(false, execType);
  }

  public SqlSession openSession(boolean autoCommit, ExecutorType execType) {
    try {
      Connection connection = dataSource.getConnection();
      Transaction tx = transactionFactory.newTransaction(connection, autoCommit);
      Executor executor = configuration.newExecutor(tx,execType);
      return new DefaultSqlSession(configuration, executor, autoCommit);
    } catch (SQLException e) {
      throw ExceptionFactory.wrapException("Error opening session.  Cause: " + e, e);
    }
  }

  public SqlSession openSession(Connection connection) {
    return openSession(connection, configuration.getDefaultExecutorType());
  }

  public SqlSession openSession(Connection connection, ExecutorType execType) {
    try {
      boolean autoCommit;
      try {
        autoCommit = connection.getAutoCommit();
      } catch (SQLException e) {
        // Failover to true, as most poor drivers
        // or databases won't support transactions
        autoCommit = true;
      }
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




}

