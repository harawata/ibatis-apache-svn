/**
 * User: Clinton Begin
 * Date: Mar 11, 2003
 * Time: 9:52:33 PM
 */
package com.ibatis.sqlmap.engine.mapping.statement;

import com.ibatis.sqlmap.engine.mapping.statement.*;

public interface ExecuteListener {

  public void onExecuteStatement(MappedStatement statement);

}
