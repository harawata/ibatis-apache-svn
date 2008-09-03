package org.apache.ibatis.jdbc;

public class DataSourceException extends RuntimeException {

  public DataSourceException() {
    super();
  }

  public DataSourceException(String message) {
    super(message);
  }

  public DataSourceException(String message, Throwable cause) {
    super(message, cause);
  }

  public DataSourceException(Throwable cause) {
    super(cause);
  }
}
