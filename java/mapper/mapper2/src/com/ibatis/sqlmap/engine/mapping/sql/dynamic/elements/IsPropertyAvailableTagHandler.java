/**
 * User: Clinton Begin
 * Date: Mar 3, 2003
 * Time: 7:36:04 PM
 */
package com.ibatis.sqlmap.engine.mapping.sql.dynamic.elements;

import com.ibatis.common.beans.Probe;
import com.ibatis.common.beans.ProbeFactory;

public class IsPropertyAvailableTagHandler extends ConditionalTagHandler {

  private static final Probe PROBE = ProbeFactory.getProbe();

  public boolean isCondition(SqlTagContext ctx, SqlTag tag, Object parameterObject) {
    if (parameterObject == null) {
      return false;
    } else {
      return PROBE.hasReadableProperty(parameterObject, tag.getPropertyAttr());
    }
  }

}
