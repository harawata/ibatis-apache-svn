package org.apache.ibatis.transaction.managed;

import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.Transaction;

import java.util.Properties;
import java.sql.Connection;

public class ManagedTransactionFactory implements TransactionFactory {

  public void setProperties(Properties props) {

  }

  public Transaction newTransaction(Connection conn) {

    return new ManagedTransaction(conn);
  }

}
