package org.apache.ibatis.api.exceptions;

import java.sql.SQLException;

public class ExceptionFactory {

  public static RuntimeException wrapSQLException(String message, SQLException e) {
    return new SqlRuntimeException(message, e);
  }

}
