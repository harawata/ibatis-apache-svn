package com.ibatis.sqlmap.engine.accessplan;

import java.util.*;

/**
 * User: Clinton Begin
 * Date: Nov 27, 2003
 * Time: 8:57:56 PM
 */
public class MapAccessPlan extends BaseAccessPlan {

  MapAccessPlan(Class clazz, String[] propertyNames) {
    super(clazz, propertyNames);
  }

  public void setProperties(Object object, Object[] values) {
    Map map = (Map) object;
    for (int i = 0; i < propertyNames.length; i++) {
      map.put(propertyNames[i], values[i]);
    }
  }

  public Object[] getProperties(Object object) {
    Object[] values = new Object[propertyNames.length];
    Map map = (Map) object;
    for (int i = 0; i < propertyNames.length; i++) {
      values[i] = map.get(propertyNames[i]);
    }
    return values;
  }

}
