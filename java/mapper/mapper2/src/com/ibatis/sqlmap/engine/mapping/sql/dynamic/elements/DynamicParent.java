package com.ibatis.sqlmap.engine.mapping.sql.dynamic.elements;

import com.ibatis.sqlmap.engine.mapping.sql.*;

/**
 * User: Clinton Begin
 * Date: Nov 19, 2003
 * Time: 11:21:29 PM
 */
public interface DynamicParent {

  public void addChild(SqlChild child);

}
