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

import com.ibatis.common.exception.NestedRuntimeException;

import java.sql.SQLException;

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
      return ((SQLException) cause).getSQLState();
    } else {
      return null;
    }

  }

  public int getErrorCode() {
    Throwable cause = getCause();
    if (cause instanceof SQLException) {
      return ((SQLException) cause).getErrorCode();
    } else {
      return -1;
    }
  }

  public SQLException getNextException() {
    Throwable cause = getCause();
    if (cause instanceof SQLException) {
      return ((SQLException) cause).getNextException();
    } else {
      return null;
    }
  }

  public synchronized void setNextException(SQLException ex) {
    Throwable cause = getCause();
    if (cause instanceof SQLException) {
      ((SQLException) cause).setNextException(ex);
    }
  }

}
