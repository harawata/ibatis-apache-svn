/**
 * User: Clinton Begin
 * Date: Mar 5, 2003
 * Time: 8:49:16 PM
 */
package com.ibatis.sqlmap.engine.mapping.sql.dynamic.elements;

public class DynamicTagHandler extends BaseTagHandler {

  public int doStartFragment(SqlTagContext ctx, SqlTag tag, Object parameterObject) {
    ctx.setFirstNonDynamicTagWithPrepend(null);
    if (tag.isPrependAvailable()) {
      ctx.setOverridePrepend(true);
    }
    return BaseTagHandler.INCLUDE_BODY;
  }

}


