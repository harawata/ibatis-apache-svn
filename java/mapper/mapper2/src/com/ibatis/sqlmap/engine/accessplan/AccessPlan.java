package com.ibatis.sqlmap.engine.accessplan;

/**
 * User: Clinton Begin
 * Date: Nov 22, 2003
 * Time: 7:03:25 PM
 */
public interface AccessPlan {

  public void setProperties(Object object, Object[] values);

  public Object[] getProperties(Object object);

}
