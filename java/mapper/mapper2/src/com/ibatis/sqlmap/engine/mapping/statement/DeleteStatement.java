package com.ibatis.sqlmap.engine.mapping.statement;

import com.ibatis.sqlmap.client.event.RowHandler;
import com.ibatis.sqlmap.engine.scope.RequestScope;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * User: Clinton Begin
 * Date: Sep 14, 2003
 * Time: 8:18:41 AM
 */
public class DeleteStatement extends GeneralStatement {

  public Object executeQueryForObject(RequestScope request, Connection conn, Object parameterObject, Object resultObject)
      throws SQLException {
    throw new SQLException("Delete statements cannot be executed as a query.");
  }

  public List executeQueryForList(RequestScope request, Connection conn, Object parameterObject, int skipResults, int maxResults)
      throws SQLException {
    throw new SQLException("Delete statements cannot be executed as a query.");
  }

  public void executeQueryWithRowHandler(RequestScope request, Connection conn, Object parameterObject, RowHandler rowHandler)
      throws SQLException {
    throw new SQLException("Delete statements cannot be executed as a query.");
  }

}
