package org.apache.ibatis.builder;

import org.apache.ibatis.exceptions.IbatisException;

public class BulderException extends IbatisException {

  public BulderException() {
    super();
  }

  public BulderException(String message) {
    super(message);
  }

  public BulderException(String message, Throwable cause) {
    super(message, cause);
  }

  public BulderException(Throwable cause) {
    super(cause);
  }
}
