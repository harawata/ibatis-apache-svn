package com.ibatis.db.sqlmap;

import com.ibatis.common.util.PaginatedList;
import com.ibatis.sqlmap.client.SqlMapClient;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * User: Clinton Begin
 * Date: Nov 18, 2003
 * Time: 9:23:34 PM
 */
public class SqlMap {

  private SqlMapClient client;

  public SqlMap(SqlMapClient client) {
    this.client = client;
  }

  public MappedStatement getMappedStatement(String name) {
    return new MappedStatement(client, name);
  }

  public void startTransaction()
      throws SQLException {
    client.startTransaction();
  }

  public void commitTransaction()
      throws SQLException {
    client.commitTransaction();
    client.endTransaction();
  }

  public void rollbackTransaction()
      throws SQLException {
    client.endTransaction();
  }

  public int executeUpdate(String statementName, Object parameterObject)
      throws SQLException {
    return client.update(statementName, parameterObject);
  }

  public Object executeQueryForObject(String statementName, Object parameterObject)
      throws SQLException {
    return client.queryForObject(statementName, parameterObject);
  }

  public Object executeQueryForObject(String statementName, Object parameterObject, Object resultObject)
      throws SQLException {
    return client.queryForObject(statementName, parameterObject, resultObject);
  }

  public Map executeQueryForMap(String statementName, Object parameterObject, String keyProperty)
      throws SQLException {
    return client.queryForMap(statementName, parameterObject, keyProperty);
  }

  public Map executeQueryForMap(String statementName, Object parameterObject, String keyProperty, String valueProperty)
      throws SQLException {
    return client.queryForMap(statementName, parameterObject, keyProperty, valueProperty);
  }

  public List executeQueryForList(String statementName, Object parameterObject)
      throws SQLException {
    return client.queryForList(statementName, parameterObject);
  }

  public List executeQueryForList(String statementName, Object parameterObject, int skipResults, int maxResults)
      throws SQLException {
    return client.queryForList(statementName, parameterObject, skipResults, maxResults);
  }

  public PaginatedList executeQueryForPaginatedList(String statementName, Object parameterObject, int pageSize)
      throws SQLException {
    return client.queryForPaginatedList(statementName, parameterObject, pageSize);
  }

  public void executeQueryWithRowHandler(String statementName, Object parameterObject, RowHandler rowHandler)
      throws SQLException {
    client.queryWithRowHandler(statementName, parameterObject, new RowHandlerAdapter(rowHandler));
  }

  public void startBatch()
      throws SQLException {
    client.startBatch();
  }

  public void endBatch()
      throws SQLException {
    client.executeBatch();
  }

  public DataSource getDataSource() {
    return client.getDataSource();
  }


}
