package com.ibatis.common.beans;

/**
 * <p/>
 * Date: Apr 23, 2004 9:40:19 PM
 * 
 * @author Clinton Begin
 */
public interface Probe {

  Object getObject(Object object, String name);

  void setObject(Object object, String name, Object value);

  String[] getReadablePropertyNames(Object object);

  String[] getWriteablePropertyNames(Object object);

  Class getPropertyTypeForSetter(Object object, String name);

  Class getPropertyTypeForGetter(Object object, String name);

  boolean hasWritableProperty(Object object, String propertyName);

  boolean hasReadableProperty(Object object, String propertyName);

}
