package org.apache.ibatis.api.exceptions;

public class SqlRuntimeException extends RuntimeException {

  public SqlRuntimeException() {
    super();
  }

  public SqlRuntimeException(String message) {
    super(message);
  }

  public SqlRuntimeException(String message, Throwable cause) {
    super(message, cause);
  }

  public SqlRuntimeException(Throwable cause) {
    super(cause);
  }

}
