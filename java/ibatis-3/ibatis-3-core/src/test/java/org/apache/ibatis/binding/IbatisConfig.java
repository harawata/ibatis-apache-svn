package org.apache.ibatis.binding;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.apache.ibatis.BaseDataTest;

import javax.sql.DataSource;

public class IbatisConfig {

  public static SqlSessionFactory getSqlSessionFactory() {
    try {
      DataSource dataSource = BaseDataTest.createBlogDataSource();
      TransactionFactory transactionFactory = new JdbcTransactionFactory();
      Environment environment = new Environment("Production", transactionFactory, dataSource);
      Configuration configuration = new Configuration(environment);
      configuration.addMapper(BoundBlogMapper.class);
      configuration.addMapper(BoundAuthorMapper.class);
      return new DefaultSqlSessionFactory(configuration);
    } catch (Exception e) {
      throw new RuntimeException("Error initializing SqlSessionFactory. Cause: " + e, e);
    }
  }

}
