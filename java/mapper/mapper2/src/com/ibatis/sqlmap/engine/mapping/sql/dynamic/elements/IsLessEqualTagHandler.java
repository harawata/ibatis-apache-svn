/**
 * User: Clinton Begin
 * Date: Mar 3, 2003
 * Time: 7:38:37 PM
 */
package com.ibatis.sqlmap.engine.mapping.sql.dynamic.elements;

public class IsLessEqualTagHandler extends ConditionalTagHandler {

  public boolean isCondition(SqlTagContext ctx, SqlTag tag, Object parameterObject) {
    long x = compare(ctx, tag, parameterObject);
    return x <= 0 && x != ConditionalTagHandler.NOT_COMPARABLE;
  }

}
