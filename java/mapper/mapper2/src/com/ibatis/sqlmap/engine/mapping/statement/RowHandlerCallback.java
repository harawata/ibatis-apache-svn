package com.ibatis.sqlmap.engine.mapping.statement;

import com.ibatis.sqlmap.client.event.RowHandler;
import com.ibatis.sqlmap.engine.mapping.result.ResultMap;
import com.ibatis.sqlmap.engine.scope.RequestScope;

import java.sql.SQLException;

/**
 * User: Clinton Begin
 * Date: Nov 29, 2003
 * Time: 11:34:24 AM
 */
public class RowHandlerCallback {

  private RowHandler rowHandler;
  private ResultMap resultMap;
  private Object resultObject;

  public RowHandlerCallback(ResultMap resultMap, Object resultObject, RowHandler rowHandler) {
    this.rowHandler = rowHandler;
    this.resultMap = resultMap;
    this.resultObject = resultObject;
  }

  public void handleResultObject(RequestScope request, Object[] results)
      throws SQLException {
    Object object;
    object = resultMap.setResultObjectValues(request, resultObject, results);
    rowHandler.handleRow(object);
  }

}
