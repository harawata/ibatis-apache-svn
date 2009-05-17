package org.apache.ibatis.parsing;

import org.apache.ibatis.exceptions.IbatisException;

public class NodeletException extends IbatisException {
  public NodeletException() {
    super();
  }

  public NodeletException(String message) {
    super(message);
  }

  public NodeletException(String message, Throwable cause) {
    super(message, cause);
  }

  public NodeletException(Throwable cause) {
    super(cause);
  }
}
