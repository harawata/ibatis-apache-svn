package org.apache.ibatis.builder;

import org.apache.ibatis.exceptions.IbatisException;

public class ParserException extends IbatisException {

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
