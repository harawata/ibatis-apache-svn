package com.ibatis.dao.engine.impl;

/**
 * Date: Jan 30, 2004 12:08:54 AM
 * @author Clinton Begin
 */
public class DaoTransactionState {

  public static final DaoTransactionState ACTIVE = new DaoTransactionState();
  public static final DaoTransactionState INACTIVE = new DaoTransactionState();
  public static final DaoTransactionState COMMITTED = new DaoTransactionState();
  public static final DaoTransactionState ROLLEDBACK = new DaoTransactionState();

  private DaoTransactionState() {
  }

}
