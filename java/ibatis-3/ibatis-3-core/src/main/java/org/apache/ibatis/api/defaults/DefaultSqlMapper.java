package org.apache.ibatis.api.defaults;

import org.apache.ibatis.api.SqlMapper;
import org.apache.ibatis.api.SqlSession;
import org.apache.ibatis.api.exceptions.ExceptionFactory;
import org.apache.ibatis.mapping.Configuration;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.executor.Executor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DefaultSqlMapper implements SqlMapper {

  private final Configuration configuration;
  private Environment environment;
  private DataSource dataSource;
  private TransactionFactory transactionFactory;

  public DefaultSqlMapper(Configuration configuration) {
    this.configuration = configuration;
    this.environment = configuration.getEnvironment();
    this.dataSource = environment.getDataSource();
    this.transactionFactory = environment.getTransactionFactory();
  }

  public SqlSession openSession() {
    try {
      Connection connection = dataSource.getConnection();
      Transaction tx = transactionFactory.newTransaction(connection);
      Executor executor = configuration.newExecutor(tx);
      return new DefaultSqlSession(configuration, executor);
    } catch (SQLException e) {
      throw ExceptionFactory.wrapSQLException("Error opening session.  Cause: " + e, e);
    }
  }

  public SqlSession openSession(Connection connection) {
    Transaction tx = transactionFactory.newTransaction(connection);
    Executor executor = configuration.newExecutor(tx);
    return new DefaultSqlSession(configuration, executor);
  }

}
