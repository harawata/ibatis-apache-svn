/**
 * User: Clinton Begin
 * Date: Mar 26, 2003
 * Time: 9:06:01 PM
 */
package com.ibatis.sqlmap.engine.mapping.sql.dynamic.elements;

import com.ibatis.common.beans.BeanProbe;

import java.util.Collection;
import java.lang.reflect.Array;

public class IsEmptyTagHandler extends ConditionalTagHandler {

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
      if (value instanceof Collection) {
        return value == null || ((Collection) value).size() < 1;
      } else if(value != null && value.getClass().isArray()) {
        return Array.getLength(value) == 0;
      } else {
        return value == null || String.valueOf(value).equals("");
      }
    }
  }

}
