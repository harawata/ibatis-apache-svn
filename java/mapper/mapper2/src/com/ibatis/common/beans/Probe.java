package com.ibatis.common.beans;

/**
 * <p/>
 * Date: Apr 23, 2004 9:40:19 PM
 * 
 * @author Clinton Begin
 */
public interface Probe {

  public Object getObject(Object object, String name);

  public void setObject(Object object, String name, Object value);

  public String[] getReadablePropertyNames(Object object);

  public String[] getWriteablePropertyNames(Object object);

  public Class getPropertyTypeForSetter(Object object, String name);

  public Class getPropertyTypeForGetter(Object object, String name);

  public boolean hasWritableProperty(Object object, String propertyName);

  public boolean hasReadableProperty(Object object, String propertyName);

}
