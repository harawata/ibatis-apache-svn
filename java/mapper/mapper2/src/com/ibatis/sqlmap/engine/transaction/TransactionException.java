package com.ibatis.sqlmap.engine.transaction;

import com.ibatis.common.exception.*;

/**
 * User: Clinton Begin
 * Date: Sep 12, 2003
 * Time: 10:16:35 PM
 */
public class TransactionException extends NestedException {

  public TransactionException() {
  }

  public TransactionException(String msg) {
    super(msg);
  }

  public TransactionException(Throwable cause) {
    super(cause);
  }

  public TransactionException(String msg, Throwable cause) {
    super(msg, cause);
  }

}
