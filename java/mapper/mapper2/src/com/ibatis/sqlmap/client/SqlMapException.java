package com.ibatis.sqlmap.client;

import com.ibatis.common.exception.*;

/**
 * Thrown to indicate a problem with SQL Map configuration or state.  Generally
 * if an SqlMapException is thrown, something is critically wronge and cannot
 * be corrected until a change to the configuration or the environment is made.
 * <p>
 * Note: Generally this wouldn't be used to indicate that an SQL execution error
 * occurred (that's what SQLException is for).
 * <p>
 * Date: Sep 5, 2003 5:56:44 PM
 * @author Clinton Begin
 */
public class SqlMapException extends NestedRuntimeException {

  public SqlMapException() {
  }

  public SqlMapException(String msg) {
    super(msg);
  }

  public SqlMapException(Throwable cause) {
    super(cause);
  }

  public SqlMapException(String msg, Throwable cause) {
    super(msg, cause);
  }

}
