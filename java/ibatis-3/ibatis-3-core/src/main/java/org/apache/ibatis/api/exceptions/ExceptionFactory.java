package org.apache.ibatis.api.exceptions;

import java.sql.SQLException;

public class ExceptionFactory {

  public static RuntimeException wrapException(String message, Exception e) {
    return new RuntimeSqlException(message, e);
  }

}
