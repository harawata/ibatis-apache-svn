package com.ibatis.dao.client;

import com.ibatis.common.exception.*;

/**
 * General runtime exception thrown by the DAO framework.
 *
 * <p>
 * Date: Jan 27, 2004 10:42:55 PM
 * @author Clinton Begin
 */
public class DaoException extends NestedRuntimeException {

  public DaoException() {
  }

  public DaoException(String msg) {
    super(msg);
  }

  public DaoException(Throwable cause) {
    super(cause);
  }

  public DaoException(String msg, Throwable cause) {
    super(msg, cause);
  }

}
