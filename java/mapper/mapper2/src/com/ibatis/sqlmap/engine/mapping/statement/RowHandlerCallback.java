package com.ibatis.sqlmap.engine.mapping.statement;

import com.ibatis.sqlmap.client.event.*;
import com.ibatis.sqlmap.engine.mapping.result.*;

import com.ibatis.sqlmap.engine.scope.*;

import java.sql.*;
import java.util.*;

/**
 * User: Clinton Begin
 * Date: Nov 29, 2003
 * Time: 11:34:24 AM
 */
public class RowHandlerCallback {

  private RowHandler rowHandler;
  private ResultMap resultMap;
  private Object resultObject;
  private List resultList;

  public RowHandlerCallback(ResultMap resultMap, List resultList, Object resultObject, RowHandler rowHandler) {
    this.rowHandler = rowHandler;
    this.resultList = resultList;
    this.resultMap = resultMap;
    this.resultObject = resultObject;
  }

  public void handleResultObject(RequestScope request, Object[] results)
      throws SQLException {
    Object object;
    object = resultMap.setResultObjectValues(request, resultObject, results);
    rowHandler.handleRow(object, resultList);
  }

}
