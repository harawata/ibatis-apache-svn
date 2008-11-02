package org.apache.ibatis.transaction.jdbc;

import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.Transaction;

import java.util.Properties;
import java.sql.Connection;

public class JdbcTransactionFactory implements TransactionFactory {

  public void setProperties(Properties props) {
  }

  public Transaction newTransaction(Connection conn) {
    return null;
  }

}
