package com.ibatis.common.beans;

import com.ibatis.common.exception.*;

/** BeansException for use for by BeanProbe and StaticBeanProbe.
 */
public class BeanException extends NestedRuntimeException {

  /** Default constructor */
  public BeanException() {
  }

  /** Constructor to set the message for the exception
   * @param s - the message for the exception
   */
  public BeanException(String s) {
    super(s);
  }

  /** Constructor for creating a simple nested exception
   * @param cause - the reason for the exception
   */
  public BeanException(Throwable cause) {
    super(cause);
  }

  /** Constructor for creating a nested exception with a message
   * @param msg - the message to pass along
   * @param cause - the reason for the exception
   */
  public BeanException(String msg, Throwable cause) {
    super(msg, cause);
  }

}
