package org.apache.ibatis.binding;

import org.apache.ibatis.mapping.SqlMapperException;

public class BindingException extends SqlMapperException {

  public BindingException() {
    super();
  }

  public BindingException(String message) {
    super(message);
  }

  public BindingException(String message, Throwable cause) {
    super(message, cause);
  }

  public BindingException(Throwable cause) {
    super(cause);
  }
}
