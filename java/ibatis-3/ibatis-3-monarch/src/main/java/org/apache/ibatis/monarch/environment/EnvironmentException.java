package org.apache.ibatis.monarch.environment;

import org.apache.ibatis.mapping.SqlMapperException;

public class EnvironmentException extends SqlMapperException {

  public EnvironmentException() {
    super();
  }

  public EnvironmentException(String message) {
    super(message);
  }

  public EnvironmentException(String message, Throwable cause) {
    super(message, cause);
  }

  public EnvironmentException(Throwable cause) {
    super(cause);
  }
  
}
