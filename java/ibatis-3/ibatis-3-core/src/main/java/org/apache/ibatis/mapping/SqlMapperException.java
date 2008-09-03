package org.apache.ibatis.mapping;

public class SqlMapperException extends RuntimeException {

  public SqlMapperException() {
    super();
  }

  public SqlMapperException(String message) {
    super(message);
  }

  public SqlMapperException(String message, Throwable cause) {
    super(message, cause);
  }

  public SqlMapperException(Throwable cause) {
    super(cause);
  }

}
