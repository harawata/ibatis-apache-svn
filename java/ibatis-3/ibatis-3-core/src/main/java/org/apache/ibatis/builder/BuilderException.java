package org.apache.ibatis.builder;

import org.apache.ibatis.mapping.SqlMapperException;

public class BuilderException extends SqlMapperException {

  public BuilderException() {
    super();
  }

  public BuilderException(String message) {
    super(message);
  }

  public BuilderException(String message, Throwable cause) {
    super(message, cause);
  }

  public BuilderException(Throwable cause) {
    super(cause);
  }
}
