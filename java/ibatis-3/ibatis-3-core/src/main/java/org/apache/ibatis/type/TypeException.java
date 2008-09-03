package org.apache.ibatis.type;

import org.apache.ibatis.mapping.SqlMapperException;

public class TypeException extends SqlMapperException {

  public TypeException() {
    super();
  }

  public TypeException(String message) {
    super(message);
  }

  public TypeException(String message, Throwable cause) {
    super(message, cause);
  }

  public TypeException(Throwable cause) {
    super(cause);
  }
  
}
