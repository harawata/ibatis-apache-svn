package com.ibatis.sqlmap.engine.mapping.statement;

import com.ibatis.sqlmap.client.event.RowHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Clinton Begin
 * Date: Dec 13, 2003
 * Time: 10:23:23 AM
 */
public class DefaultRowHandler implements RowHandler {

  private List list = new ArrayList();

  public void handleRow(Object valueObject) {
    list.add(valueObject);
  }

  public List getList() {
    return list;
  }

  /**
   * @deprecated REMOVE THIS
   */
  public void handleRow(Object valueObject, List list) {
    throw new UnsupportedOperationException("DEPRECATED: This should never be called internally.");
  }


}
