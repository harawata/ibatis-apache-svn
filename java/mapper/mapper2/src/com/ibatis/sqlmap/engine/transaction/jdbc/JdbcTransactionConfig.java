package com.ibatis.sqlmap.engine.transaction.jdbc;

import com.ibatis.sqlmap.engine.transaction.*;

import javax.sql.*;
import java.util.*;
import java.sql.*;

/**
 * User: Clinton Begin
 * Date: Sep 13, 2003
 * Time: 7:36:12 AM
 */
public class JdbcTransactionConfig extends BaseTransactionConfig {

  private DataSource dataSource;

  public DataSource getDataSource() {
    return dataSource;
  }

  public void setDataSource(DataSource ds) {
    this.dataSource = ds;
  }

  public void initialize(Map props) throws SQLException, TransactionException {
  }

  public Transaction newTransaction() throws SQLException, TransactionException {
    return new JdbcTransaction(dataSource);
  }
}
