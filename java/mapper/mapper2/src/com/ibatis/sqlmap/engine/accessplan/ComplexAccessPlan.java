package com.ibatis.sqlmap.engine.accessplan;

import com.ibatis.common.beans.Probe;
import com.ibatis.common.beans.ProbeFactory;

/**
 * User: Clinton Begin
 * Date: Nov 27, 2003
 * Time: 9:36:06 PM
 */
public class ComplexAccessPlan extends BaseAccessPlan {

  private static final Probe PROBE = ProbeFactory.getProbe();

  ComplexAccessPlan(Class clazz, String[] propertyNames) {
    super(clazz, propertyNames);
  }

  public void setProperties(Object object, Object[] values) {
    for (int i = 0; i < propertyNames.length; i++) {
      PROBE.setObject(object, propertyNames[i], values[i]);
    }
  }

  public Object[] getProperties(Object object) {
    Object[] values = new Object[propertyNames.length];
    for (int i = 0; i < propertyNames.length; i++) {
      values[i] = PROBE.getObject(object, propertyNames[i]);
    }
    return values;
  }

}
