/**
 * User: Clinton Begin
 * Date: Mar 3, 2003
 * Time: 7:30:48 PM
 */
package com.ibatis.sqlmap.engine.mapping.sql.dynamic.elements;

import com.ibatis.common.beans.BeanProbe;

public class IsNullTagHandler extends ConditionalTagHandler {

  public boolean isCondition(SqlTagContext ctx, SqlTag tag, Object parameterObject) {
    if (parameterObject == null) {
      return true;
    } else {
      String prop = tag.getPropertyAttr();
      Object value;
      if (prop != null) {
        value = BeanProbe.getObject(parameterObject, prop);
      } else {
        value = parameterObject;
      }
      return value == null;
    }
  }


}
