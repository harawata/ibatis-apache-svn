/**
 * User: Clinton Begin
 * Date: Mar 3, 2003
 * Time: 7:37:14 PM
 */
package com.ibatis.sqlmap.engine.mapping.sql.dynamic.elements;

public class IsGreaterThanTagHandler extends ConditionalTagHandler {

  public boolean isCondition(SqlTagContext ctx, SqlTag tag, Object parameterObject) {
    long x = compare(ctx, tag, parameterObject);
    return x > 0 && x != ConditionalTagHandler.NOT_COMPARABLE;
  }

}
