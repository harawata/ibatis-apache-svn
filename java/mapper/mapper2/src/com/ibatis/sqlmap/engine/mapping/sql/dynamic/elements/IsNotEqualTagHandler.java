/**
 * User: Clinton Begin
 * Date: Mar 3, 2003
 * Time: 7:32:00 PM
 */
package com.ibatis.sqlmap.engine.mapping.sql.dynamic.elements;

public class IsNotEqualTagHandler extends IsEqualTagHandler {

  public boolean isCondition(SqlTagContext ctx, SqlTag tag, Object parameterObject) {
    return !super.isCondition(ctx, tag, parameterObject);
  }

}
