package com.ibatis.sqlmap.engine.transaction;

/**
 * User: Clinton Begin
 * Date: Nov 9, 2003
 * Time: 5:58:07 PM
 */
public class TransactionState {

  public static final TransactionState STATE_STARTED = new TransactionState();
  public static final TransactionState STATE_COMMITTED = new TransactionState();
  public static final TransactionState STATE_ENDED = new TransactionState();
  public static final TransactionState STATE_USER_PROVIDED = new TransactionState();

  private TransactionState() {
  }

}
