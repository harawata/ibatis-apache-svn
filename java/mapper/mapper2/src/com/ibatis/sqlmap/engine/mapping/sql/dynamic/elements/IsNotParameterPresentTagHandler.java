/**
 * User: Clinton Begin
 * Date: Mar 26, 2003
 * Time: 9:05:33 PM
 */
package com.ibatis.sqlmap.engine.mapping.sql.dynamic.elements;

public class IsNotParameterPresentTagHandler extends IsParameterPresentTagHandler {

  public boolean isCondition(SqlTagContext ctx, SqlTag tag, Object parameterObject) {
    return !super.isCondition(ctx, tag, parameterObject);
  }

}
