package com.ibatis.sqlmap.engine.accessplan;

/**
 * User: Clinton Begin
 * Date: Nov 22, 2003
 * Time: 7:03:25 PM
 */
public interface AccessPlan {

  /**
   * Sets all of the properties of a bean
   * @param object - the bean
   * @param values - the property values
   */
  public void setProperties(Object object, Object[] values);

  /**
   * Gets all of the properties of a bean
   * @param object - the bean
   * @return the properties
   */
  public Object[] getProperties(Object object);

}
