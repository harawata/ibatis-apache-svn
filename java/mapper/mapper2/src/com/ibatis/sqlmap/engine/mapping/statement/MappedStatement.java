package com.ibatis.sqlmap.engine.mapping.statement;

import com.ibatis.sqlmap.client.event.RowHandler;
import com.ibatis.sqlmap.engine.cache.CacheKey;
import com.ibatis.sqlmap.engine.mapping.parameter.ParameterMap;
import com.ibatis.sqlmap.engine.mapping.result.ResultMap;
import com.ibatis.sqlmap.engine.mapping.sql.Sql;
import com.ibatis.sqlmap.engine.scope.RequestScope;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * User: Clinton Begin
 * Date: Sep 14, 2003
 * Time: 8:37:28 AM
 */
public interface MappedStatement {

  public String getId();

  public int executeUpdate(RequestScope request, Connection conn, Object parameterObject)
      throws SQLException;

  public Object executeQueryForObject(RequestScope request, Connection conn, Object parameterObject, Object resultObject)
      throws SQLException;

  public List executeQueryForList(RequestScope request, Connection conn, Object parameterObject, int skipResults, int maxResults)
      throws SQLException;

  public void executeQueryWithRowHandler(RequestScope request, Connection conn, Object parameterObject, RowHandler rowHandler)
      throws SQLException;

  public CacheKey getCacheKey(RequestScope request, Object parameterObject);

  public ParameterMap getParameterMap();

  public ResultMap getResultMap();

  public void setBaseCacheKey(int base);

  public void addExecuteListener(ExecuteListener listener);

  public void notifyListeners();

  public void initRequest(RequestScope request);

  public Sql getSql();

  public Class getParameterClass();


}
