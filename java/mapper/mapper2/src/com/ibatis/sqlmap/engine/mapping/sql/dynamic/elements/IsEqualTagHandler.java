/**
 * User: Clinton Begin
 * Date: Mar 3, 2003
 * Time: 7:31:11 PM
 */
package com.ibatis.sqlmap.engine.mapping.sql.dynamic.elements;

public class IsEqualTagHandler extends ConditionalTagHandler {

  public boolean isCondition(SqlTagContext ctx, SqlTag tag, Object parameterObject) {
    return compare(ctx, tag, parameterObject) == 0;
  }

}
