package com.ibatis.sqlmap.engine.accessplan;

import net.sf.cglib.beans.*;

/**
 * User: Clinton Begin
 * Date: Nov 27, 2003
 * Time: 9:05:13 PM
 */
public class EnhancedPropertyAccessPlan extends BaseAccessPlan {

  private BulkBean bulkBean;

  EnhancedPropertyAccessPlan(Class clazz, String[] propertyNames) {
    super(clazz, propertyNames);
    bulkBean = BulkBean.create(clazz, getGetterNames(propertyNames), getSetterNames(propertyNames), getTypes(propertyNames));
  }

  public void setProperties(Object object, Object[] values) {
    bulkBean.setPropertyValues(object, values);
  }

  public Object[] getProperties(Object object) {
    return bulkBean.getPropertyValues(object);
  }

}
