/**
 * User: Clinton Begin
 * Date: Mar 26, 2003
 * Time: 9:08:45 PM
 */
package com.ibatis.sqlmap.engine.mapping.sql.dynamic.elements;

public class IsNotEmptyTagHandler extends IsEmptyTagHandler {

  public boolean isCondition(SqlTagContext ctx, SqlTag tag, Object parameterObject) {
    return !super.isCondition(ctx, tag, parameterObject);
  }

}
