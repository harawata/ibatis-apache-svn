package com.ibatis.sqlmap.engine.mapping.statement;

import com.ibatis.sqlmap.client.event.RowHandler;
import com.ibatis.sqlmap.engine.scope.RequestScope;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * User: Clinton Begin
 * Date: Jan 7, 2004
 * Time: 9:16:00 PM
 */
public class SelectKeyStatement extends SelectStatement {

  private String keyProperty;
  private boolean after;

  public String getKeyProperty() {
    return keyProperty;
  }

  public void setKeyProperty(String keyProperty) {
    this.keyProperty = keyProperty;
  }

  public boolean isAfter() {
    return after;
  }

  public void setAfter(boolean after) {
    this.after = after;
  }

  public List executeQueryForList(RequestScope request, Connection conn, Object parameterObject, int skipResults, int maxResults)
      throws SQLException {
    throw new SQLException("Select statements cannot be executed for a list.");
  }

  public void executeQueryWithRowHandler(RequestScope request, Connection conn, Object parameterObject, RowHandler rowHandler)
      throws SQLException {
    throw new SQLException("Select Key statements cannot be executed with a row handler.");
  }

}
