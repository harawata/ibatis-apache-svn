/**
 * User: Clinton Begin
 * Date: Jul 11, 2003
 * Time: 9:06:46 AM
 */
package com.ibatis.common.exception;

/**
 * Nexted exception implementation.  Thanks Claus.
 */

public class NestedException extends Exception {

  private static final String CAUSED_BY = "\nCaused by: ";

  private Throwable cause = null;

  /**
   * Constructor
   */
  public NestedException() {
  }

  /**
   * Constructor
   * @param msg error message
   */
  public NestedException(String msg) {
    super(msg);
  }

  /**
   * Constructor
   * @param cause the nested exception (caused by)
   */
  public NestedException(Throwable cause) {
    super();
    this.cause = cause;
  }

  /**
   * Constructor
   * @param msg error message
   * @param cause the nested exception (caused by)
   */
  public NestedException(String msg, Throwable cause) {
    super(msg);
    this.cause = cause;
  }

  /**
   * Gets the causing exception, if any.
   * 
   * @return The cause of the exception
   */
  public Throwable getCause() {
    return cause;
  }

  /**
   * Converts the exception to a string representation
   * 
   * @return The string representation of the exception
   */
  public String toString() {
    if (cause == null) {
      return super.toString();
    } else {
      return super.toString() + CAUSED_BY + cause.toString();
    }
  }

  /**
   * Sends a stack trace to System.err (including the root cause, if any)
   */
  public void printStackTrace() {
    super.printStackTrace();
    if (cause != null) {
      System.err.println(CAUSED_BY);
      cause.printStackTrace();
    }
  }

  /**
   * Sends a stack trace to the PrintStream passed in (including the root cause, if any)
   * 
   * @param ps - the PrintStream to send the output to
   */
  public void printStackTrace(java.io.PrintStream ps) {
    super.printStackTrace(ps);
    if (cause != null) {
      ps.println(CAUSED_BY);
      cause.printStackTrace(ps);
    }
  }

  /**
   * Sends a stack trace to the PrintWriter passed in (including the root cause, if any)
   * 
   * @param pw - the PrintWriter to send the output to
   */
  public void printStackTrace(java.io.PrintWriter pw) {
    super.printStackTrace(pw);
    if (cause != null) {
      pw.println(CAUSED_BY);
      cause.printStackTrace(pw);
    }
  }

}
