package com.ibatis.db.sqlmap;

import java.util.List;

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

  public void handleRow(Object valueObject) {
    rowHandler.handleRow(valueObject);
  }

  /**
   * @deprecated REMOVE THIS
   */
  public void handleRow(Object valueObject, List list) {
    throw new UnsupportedOperationException("DEPRECATED: This should never be called internally.");
  }

}
