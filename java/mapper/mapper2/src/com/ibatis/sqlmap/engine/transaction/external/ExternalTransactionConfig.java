package com.ibatis.sqlmap.engine.transaction.external;

import com.ibatis.sqlmap.engine.transaction.BaseTransactionConfig;
import com.ibatis.sqlmap.engine.transaction.Transaction;
import com.ibatis.sqlmap.engine.transaction.TransactionException;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Properties;

/**
 * User: Clinton Begin
 * Date: Sep 13, 2003
 * Time: 7:36:01 AM
 */
public class ExternalTransactionConfig extends BaseTransactionConfig {

  private DataSource dataSource;
  private boolean defaultAutoCommit = false;
  private boolean setAutoCommitAllowed = true;

  public DataSource getDataSource() {
    return dataSource;
  }

  public void setDataSource(DataSource ds) {
    this.dataSource = ds;
  }

  public void initialize(Properties props) throws SQLException, TransactionException {
    String dacProp = props.getProperty("DefaultAutoCommit");
    String sacaProp = props.getProperty("SetAutoCommitAllowed");
    defaultAutoCommit = "true".equals(dacProp);
    setAutoCommitAllowed = "true".equals(sacaProp) || sacaProp == null;
  }

  public Transaction newTransaction() throws SQLException, TransactionException {
    return new ExternalTransaction(dataSource, defaultAutoCommit, setAutoCommitAllowed);
  }
}
