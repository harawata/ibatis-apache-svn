package com.ibatis.sqlmap.engine.mapping.statement;

import com.ibatis.sqlmap.engine.scope.*;
import com.ibatis.sqlmap.client.event.*;

import java.sql.*;
import java.util.*;

/**
 * User: Clinton Begin
 * Date: Sep 14, 2003
 * Time: 8:18:34 AM
 */
public class UpdateStatement extends GeneralStatement {

  public Object executeQueryForObject(RequestScope request, Connection conn, Object parameterObject, Object resultObject)
      throws SQLException {
    throw new SQLException("Update statements cannot be executed as a query.");
  }

  public List executeQueryForList(RequestScope request, Connection conn, Object parameterObject, int skipResults, int maxResults)
      throws SQLException {
    throw new SQLException("Update statements cannot be executed as a query.");
  }

  public List executeQueryForList(RequestScope request, Connection conn, Object parameterObject, RowHandler rowHandler)
      throws SQLException {
    throw new SQLException("Update statements cannot be executed as a query.");
  }

}

