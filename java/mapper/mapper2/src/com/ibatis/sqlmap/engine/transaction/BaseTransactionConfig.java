package com.ibatis.sqlmap.engine.transaction;

/**
 * User: Clinton Begin
 * Date: Jan 4, 2004
 * Time: 10:09:53 PM
 */
public abstract class BaseTransactionConfig implements TransactionConfig {

  private int maximumConcurrentTransactions;

  public int getMaximumConcurrentTransactions() {
    return maximumConcurrentTransactions;
  }

  public void setMaximumConcurrentTransactions(int maximumConcurrentTransactions) {
    this.maximumConcurrentTransactions = maximumConcurrentTransactions;
  }

}
