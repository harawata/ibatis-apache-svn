package com.ibatis.common.jdbc.exception;

import com.ibatis.common.exception.*;

import java.sql.*;

/**
 *
 *
 * <p>
 * Date: Jan 9, 2004 11:10:00 PM
 * @author Clinton Begin
 */
public class RuntimeSQLException extends NestedRuntimeException {

  public RuntimeSQLException() {
  }

  public RuntimeSQLException(String msg) {
    super(msg);
  }

  public RuntimeSQLException(SQLException sqlException) {
    super(sqlException);
  }

  public RuntimeSQLException(String msg, SQLException sqlException) {
    super(msg, sqlException);
  }

  public String getSQLState() {
    Throwable cause = getCause();
    if (cause instanceof SQLException) {
      return ((SQLException)cause).getSQLState();
    } else {
      return null;
    }

  }

  public int getErrorCode() {
    Throwable cause = getCause();
    if (cause instanceof SQLException) {
      return ((SQLException)cause).getErrorCode();
    } else {
      return -1;
    }
  }

  public SQLException getNextException() {
    Throwable cause = getCause();
    if (cause instanceof SQLException) {
      return ((SQLException)cause).getNextException();
    } else {
      return null;
    }
  }

  public synchronized void setNextException(SQLException ex) {
    Throwable cause = getCause();
    if (cause instanceof SQLException) {
      ((SQLException)cause).setNextException(ex);
    }
  }

}
