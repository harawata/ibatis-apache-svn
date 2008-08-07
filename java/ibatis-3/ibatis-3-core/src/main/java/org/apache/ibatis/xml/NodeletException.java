package org.apache.ibatis.xml;

public class NodeletException extends RuntimeException {
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
