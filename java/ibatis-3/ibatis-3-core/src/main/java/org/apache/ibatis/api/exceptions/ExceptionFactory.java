package org.apache.ibatis.api.exceptions;

import org.apache.ibatis.executor.ErrorContext;

public class ExceptionFactory {

  public static RuntimeException wrapException(String message, Exception e) {
    return new RuntimeSqlException(ErrorContext.instance().message(message).cause(e).toString(), e);
  }

}
