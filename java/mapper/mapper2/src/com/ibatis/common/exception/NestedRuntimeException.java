/**
 * User: Clinton Begin
 * Date: Jul 11, 2003
 * Time: 9:06:53 AM
 */
package com.ibatis.common.exception;

/**
 * Nexted exception implementation.  Thanks Claus.
 */

public class NestedRuntimeException extends RuntimeException {

  private static final String CAUSED_BY = "\nCaused by: ";

  private Throwable cause = null;

  /**
   * Constructor
   */
  public NestedRuntimeException() {
  }

  /**
   * Constructor
   * @param msg error message
   */
  public NestedRuntimeException(String msg) {
    super(msg);
  }

  /**
   * Constructor
   * @param cause the nested exception (caused by)
   */
  public NestedRuntimeException(Throwable cause) {
    super();
    this.cause = cause;
  }

  /**
   * Constructor
   * @param msg error message
   * @param cause the nested exception (caused by)
   */
  public NestedRuntimeException(String msg, Throwable cause) {
    super(msg);
    this.cause = cause;
  }

  /**
   * Gets the causing exception, if any.
   */
  public Throwable getCause() {
    return cause;
  }

  public String toString() {
    if (cause == null) {
      return super.toString();
    } else {
      return super.toString() + CAUSED_BY + cause.toString();
    }
  }

  public void printStackTrace() {
    super.printStackTrace();
    if (cause != null) {
      System.err.println(CAUSED_BY);
      cause.printStackTrace();
    }
  }

  public void printStackTrace(java.io.PrintStream ps) {
    super.printStackTrace(ps);
    if (cause != null) {
      ps.println(CAUSED_BY);
      cause.printStackTrace(ps);
    }
  }

  public void printStackTrace(java.io.PrintWriter pw) {
    super.printStackTrace(pw);
    if (cause != null) {
      pw.println(CAUSED_BY);
      cause.printStackTrace(pw);
    }
  }

}
