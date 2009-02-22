package org.apache.ibatis.session;

public class SessionException extends RuntimeException {

  public SessionException() {
    super();
  }

  public SessionException(String message) {
    super(message);
  }

  public SessionException(String message, Throwable cause) {
    super(message, cause);
  }

  public SessionException(Throwable cause) {
    super(cause);
  }
}
