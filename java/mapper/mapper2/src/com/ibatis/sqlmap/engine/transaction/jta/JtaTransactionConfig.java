package com.ibatis.sqlmap.engine.transaction.jta;

import com.ibatis.sqlmap.client.SqlMapException;
import com.ibatis.sqlmap.engine.transaction.BaseTransactionConfig;
import com.ibatis.sqlmap.engine.transaction.Transaction;
import com.ibatis.sqlmap.engine.transaction.TransactionException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.transaction.UserTransaction;
import java.sql.SQLException;
import java.util.Properties;

/**
 * User: Clinton Begin
 * Date: Sep 13, 2003
 * Time: 7:28:58 AM
 */
public class JtaTransactionConfig extends BaseTransactionConfig {

  private DataSource dataSource;
  private UserTransaction userTransaction;

  public DataSource getDataSource() {
    return dataSource;
  }

  public void setDataSource(DataSource ds) {
    this.dataSource = ds;
  }

  public void initialize(Properties props) throws SQLException, TransactionException {
    String utxName = null;
    try {
      utxName = (String) props.get("UserTransaction");
      InitialContext initCtx = new InitialContext();
      userTransaction = (UserTransaction) initCtx.lookup(utxName);
    } catch (NamingException e) {
      throw new SqlMapException("Error initializing JtaTransactionConfig while looking up UserTransaction (" + utxName + ").  Cause: " + e);
    }
  }

  public Transaction newTransaction() throws SQLException, TransactionException {
    return new JtaTransaction(userTransaction, dataSource);
  }

}

