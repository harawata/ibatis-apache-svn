package org.apache.ibatis.monarch.environment;

import org.apache.ibatis.monarch.environment.transaction.TransactionManager;

import javax.sql.DataSource;

public class Environment {
  private String id;
  private TransactionManager transactionManager;
  private DataSource dataSource;

  private Environment() {}

  public static class Builder {
    private Environment environment = new Environment();
    public Builder(String id) {
      environment.id = id;
    }
    public Builder transactionManager(TransactionManager transactionManager) {
      environment.transactionManager = transactionManager;
      return this;
    }
    public Builder dataSource(DataSource dataSource) {
      environment.dataSource = dataSource;
      return this;
    }
    public Environment build() {
      return environment;
    }
  }

  public String getId() {
    return id;
  }

  public TransactionManager getTransactionManager() {
    return transactionManager;
  }

  public DataSource getDataSource() {
    return dataSource;
  }

}
