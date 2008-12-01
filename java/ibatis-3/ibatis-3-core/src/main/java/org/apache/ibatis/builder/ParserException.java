package org.apache.ibatis.builder;

import org.apache.ibatis.mapping.SqlMapperException;

public class ParserException extends SqlMapperException {

  public ParserException() {
    super();
  }

  public ParserException(String message) {
    super(message);
  }

  public ParserException(String message, Throwable cause) {
    super(message, cause);
  }

  public ParserException(Throwable cause) {
    super(cause);
  }
}
