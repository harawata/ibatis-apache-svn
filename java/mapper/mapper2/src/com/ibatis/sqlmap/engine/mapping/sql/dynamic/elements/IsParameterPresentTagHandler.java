/**
 * User: Clinton Begin
 * Date: Mar 26, 2003
 * Time: 9:02:26 PM
 */
package com.ibatis.sqlmap.engine.mapping.sql.dynamic.elements;

public class IsParameterPresentTagHandler extends ConditionalTagHandler {

  public boolean isCondition(SqlTagContext ctx, SqlTag tag, Object parameterObject) {
    return parameterObject != null;
  }

}
