package com.ibatis.common.beans;

import com.ibatis.common.exception.*;

/**
 * BeansException for use for by BeanProbe and StaticBeanProbe.
 *
 * @author  clinton_begin
 */
public class BeanException extends NestedRuntimeException {

  /** Default constructor */
  public BeanException() {
  }

  /** Constructor to set the message for the exception
   * @param s The message for the exception
   */
  public BeanException(String s) {
    super(s);
  }

  public BeanException(Throwable cause) {
    super(cause);
  }

  public BeanException(String msg, Throwable cause) {
    super(msg, cause);
  }

}
