package com.ibatis.db.sqlmap;

import java.util.*;

/**
 * User: Clinton Begin
 * Date: Nov 29, 2003
 * Time: 12:46:44 PM
 */
public class RowHandlerAdapter implements com.ibatis.sqlmap.client.event.RowHandler {

  private RowHandler rowHandler;

  RowHandlerAdapter(RowHandler rowHandler) {
    this.rowHandler = rowHandler;
  }

  public void handleRow(Object valueObject, List list) {
    rowHandler.handleRow(valueObject);
  }

}
