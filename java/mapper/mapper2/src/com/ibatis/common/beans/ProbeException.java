package com.ibatis.common.beans;

import com.ibatis.common.exception.NestedRuntimeException;

/**
 * BeansException for use for by BeanProbe and StaticBeanProbe.
 *
 * @author clinton_begin
 */
public class ProbeException extends NestedRuntimeException {

  /**
   * Default constructor
   */
  public ProbeException() {
  }

  /**
   * Constructor to set the message for the exception
   *
   * @param s The message for the exception
   */
  public ProbeException(String s) {
    super(s);
  }

  public ProbeException(Throwable cause) {
    super(cause);
  }

  public ProbeException(String msg, Throwable cause) {
    super(msg, cause);
  }

}
