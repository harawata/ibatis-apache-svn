package com.ibatis.common.beans;

/**
 * <p/>
 * Date: Apr 23, 2004 9:34:47 PM
 * 
 * @author Clinton Begin
 */
public abstract class BaseProbe implements Probe {

  protected abstract void setProperty(Object object, String property, Object value);

  protected abstract Object getProperty(Object object, String property);

  /**
   * Returns an array of the readable properties exposed by an object
   *
   * @param object - the object
   * @return The array of property names
   */
  public abstract String[] getReadablePropertyNames(Object object);

  /**
   * Returns an array of the writeable properties exposed by an object
   *
   * @param object - the object
   * @return The array of property names
   */
  public abstract String[] getWriteablePropertyNames(Object object);


}
