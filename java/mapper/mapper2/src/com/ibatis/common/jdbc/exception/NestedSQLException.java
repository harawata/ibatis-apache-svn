/*
 *  Copyright 2004 Clinton Begin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.ibatis.common.jdbc.exception;

import java.sql.SQLException;

/**
 * User: Clinton Begin
 * Date: Nov 30, 2003
 * Time: 6:47:38 PM
 */
public class NestedSQLException extends SQLException {

  private static final String CAUSED_BY = "\nCaused by: ";

  private Throwable cause = null;

  /**
   * Constructor
   */

  public NestedSQLException(String msg) {
    super(msg);
  }

  public NestedSQLException(String reason, String SQLState) {
    super(reason, SQLState);
  }

  public NestedSQLException(String reason, String SQLState, int vendorCode) {
    super(reason, SQLState, vendorCode);
  }

  public NestedSQLException(String msg, Throwable cause) {
    super(msg);
    this.cause = cause;
  }

  public NestedSQLException(String reason, String SQLState, Throwable cause) {
    super(reason, SQLState);
    this.cause = cause;
  }

  public NestedSQLException(String reason, String SQLState, int vendorCode, Throwable cause) {
    super(reason, SQLState, vendorCode);
    this.cause = cause;
  }

  public NestedSQLException(Throwable cause) {
    super();
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
