package com.ibatis.sqlmap.engine.mapping.statement;

import com.ibatis.sqlmap.client.event.RowHandler;
import com.ibatis.sqlmap.engine.scope.RequestScope;
import com.ibatis.sqlmap.engine.transaction.Transaction;

import java.sql.SQLException;
import java.util.List;

/**
 * User: Clinton Begin
 * Date: Sep 14, 2003
 * Time: 8:18:34 AM
 */
public class UpdateStatement extends GeneralStatement {

  public Object executeQueryForObject(RequestScope request, Transaction trans, Object parameterObject, Object resultObject)
      throws SQLException {
    throw new SQLException("Update statements cannot be executed as a query.");
  }

  public List executeQueryForList(RequestScope request, Transaction trans, Object parameterObject, int skipResults, int maxResults)
      throws SQLException {
    throw new SQLException("Update statements cannot be executed as a query.");
  }

  public void executeQueryWithRowHandler(RequestScope request, Transaction trans, Object parameterObject, RowHandler rowHandler)
      throws SQLException {
    throw new SQLException("Update statements cannot be executed as a query.");
  }

}

