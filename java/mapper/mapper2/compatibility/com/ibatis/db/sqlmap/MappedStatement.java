package com.ibatis.db.sqlmap;

import com.ibatis.common.util.*;
import com.ibatis.sqlmap.client.*;

import java.sql.*;
import java.util.*;

/**
 * User: Clinton Begin
 * Date: Nov 18, 2003
 * Time: 9:24:26 PM
 */
public class MappedStatement {

  private SqlMapClient sqlMapClient;
  private String statementName;

  public MappedStatement(SqlMapClient sqlMapClient, String statementName) {
    this.sqlMapClient = sqlMapClient;
    this.statementName = statementName;
  }

  public int executeUpdate(Connection conn, Object parameterObject)
      throws SQLException {
    sqlMapClient.setUserConnection(conn);
    int n = sqlMapClient.update(statementName, parameterObject);
    sqlMapClient.setUserConnection(null);
    return n;
  }

  public void executeQueryWithRowHandler(Connection conn, Object parameterObject, RowHandler rowHandler)
      throws SQLException {
    sqlMapClient.setUserConnection(conn);
    sqlMapClient.queryForList(statementName, parameterObject, new RowHandlerAdapter(rowHandler));
    sqlMapClient.setUserConnection(null);
  }

  public Map executeQueryForMap(Connection conn, Object parameterObject, String keyProperty)
      throws SQLException {
    sqlMapClient.setUserConnection(conn);
    Map map = sqlMapClient.queryForMap(statementName, parameterObject, keyProperty);
    sqlMapClient.setUserConnection(null);
    return map;
  }

  public Map executeQueryForMap(Connection conn, Object parameterObject, String keyProperty, String valueProperty)
      throws SQLException {
    sqlMapClient.setUserConnection(conn);
    Map map = sqlMapClient.queryForMap(statementName, parameterObject, keyProperty, valueProperty);
    sqlMapClient.setUserConnection(null);
    return map;
  }

  public PaginatedList executeQueryForPaginatedList(Object parameterObject, int pageSize)
      throws SQLException {
    PaginatedList list = sqlMapClient.queryForPaginatedList(statementName, parameterObject, pageSize);
    return list;
  }

  public List executeQueryForList(Connection conn, Object parameterObject)
      throws SQLException {
    sqlMapClient.setUserConnection(conn);
    List list = sqlMapClient.queryForList(statementName, parameterObject);
    sqlMapClient.setUserConnection(null);
    return list;
  }

  public List executeQueryForList(Connection conn, Object parameterObject, int skipResults, int maxResults)
      throws SQLException {
    sqlMapClient.setUserConnection(conn);
    List list = sqlMapClient.queryForList(statementName, parameterObject, skipResults, maxResults);
    sqlMapClient.setUserConnection(null);
    return list;
  }

  public Object executeQueryForObject(Connection conn, Object parameterObject)
      throws SQLException {
    sqlMapClient.setUserConnection(conn);
    Object o = sqlMapClient.queryForObject(statementName, parameterObject);
    sqlMapClient.setUserConnection(null);
    return o;
  }

  public Object executeQueryForObject(Connection conn, Object parameterObject, Object resultObject)
      throws SQLException {
    sqlMapClient.setUserConnection(conn);
    Object o = sqlMapClient.queryForObject(statementName, parameterObject, resultObject);
    sqlMapClient.setUserConnection(null);
    return o;
  }


}
