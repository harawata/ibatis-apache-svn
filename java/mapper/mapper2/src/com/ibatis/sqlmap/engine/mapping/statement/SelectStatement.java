package com.ibatis.sqlmap.engine.mapping.statement;

import com.ibatis.sqlmap.engine.scope.*;
import com.ibatis.sqlmap.engine.transaction.Transaction;

import java.sql.*;

/**
 * User: Clinton Begin
 * Date: Sep 14, 2003
 * Time: 8:19:03 AM
 */
public class SelectStatement extends GeneralStatement {

  public int executeUpdate(RequestScope request, Transaction trans, Object parameterObject)
      throws SQLException {
    throw new SQLException("Select statements cannot be executed as an update.");
  }

}
