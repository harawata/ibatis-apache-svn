package com.ibatis.sqlmap.engine.mapping.statement;

import com.ibatis.sqlmap.client.event.*;

import java.util.*;

/**
 * User: Clinton Begin
 * Date: Dec 13, 2003
 * Time: 10:23:23 AM
 */
public class DefaultRowHandler implements RowHandler {

  public void handleRow(Object valueObject, List list) {
    list.add(valueObject);
  }

}
