/**
 * User: Clinton Begin
 * Date: Mar 3, 2003
 * Time: 7:39:41 PM
 */
package com.ibatis.sqlmap.engine.mapping.sql.dynamic.elements;

public abstract class BaseTagHandler implements SqlTagHandler {

  public int doStartFragment(SqlTagContext ctx, SqlTag tag, Object parameterObject) {
    return SqlTagHandler.INCLUDE_BODY;
  }

  public int doEndFragment(SqlTagContext ctx, SqlTag tag, Object parameterObject, StringBuffer bodyContent) {
    return SqlTagHandler.INCLUDE_BODY;
  }

  public void doPrepend(SqlTagContext ctx, SqlTag tag, Object parameterObject, StringBuffer bodyContent) {
    if (tag.isPrependAvailable()) {
      if (bodyContent.toString().trim().length() > 0) {
        if (ctx.isOverridePrepend()
            && tag == ctx.getFirstNonDynamicTagWithPrepend()) {
          ctx.setOverridePrepend(false);
        } else {
          bodyContent.insert(0, tag.getPrependAttr());
        }
      } else {
        if (ctx.getFirstNonDynamicTagWithPrepend() != null) {
          ctx.setFirstNonDynamicTagWithPrepend(null);
          ctx.setOverridePrepend(true);
        }
      }
    }

  }

  public boolean isPostParseRequired() {
    return false;
  }

}
