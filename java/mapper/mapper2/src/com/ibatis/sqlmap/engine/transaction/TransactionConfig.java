package com.ibatis.sqlmap.engine.transaction;

import javax.sql.*;
import java.util.*;
import java.sql.*;

/**
 * User: Clinton Begin
 * Date: Sep 13, 2003
 * Time: 7:31:18 AM
 */
public interface TransactionConfig {

  public DataSource getDataSource();

  public void setDataSource(DataSource ds);

  public void initialize(Map props) throws SQLException, TransactionException;

  public Transaction newTransaction() throws SQLException, TransactionException;

  public int getMaximumConcurrentTransactions();

  public void setMaximumConcurrentTransactions(int maximumConcurrentTransactions);

}
