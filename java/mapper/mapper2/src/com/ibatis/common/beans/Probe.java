package com.ibatis.common.beans;

/**
 * <p/>
 * Date: Apr 23, 2004 9:40:19 PM
 * 
 */
public interface Probe {

  /**
   * Gets an Object property from another object
   *
   * @param object - the object
   * @param name - the property name
   * @return The property value (as an Object)
   */
  public Object getObject(Object object, String name);

  /**
   * Sets the value of a property on an object
   *
   * @param object - the object to change
   * @param name - the name of the property to set
   * @param value - the new value to set
   */
  public void setObject(Object object, String name, Object value);

  /**
   * Returns an array of the readable properties exposed by an object
   *
   * @param object - the object
   * @return The array of property names
   */
  public String[] getReadablePropertyNames(Object object);

  /**
   * Returns an array of the writeable properties exposed by an object
   *
   * @param object - the object
   * @return The array of property names
   */
  public String[] getWriteablePropertyNames(Object object);

  /**
   * Returns the class that the setter expects when setting a property
   *
   * @param object - the object to check
   * @param name - the name of the property
   * @return The type of the property
   */
  public Class getPropertyTypeForSetter(Object object, String name);

  /**
   * Returns the class that the getter will return when reading a property
   *
   * @param object - the object to check
   * @param name - the name of the property
   * @return The type of the property
   */
  public Class getPropertyTypeForGetter(Object object, String name);

  /**
   * Checks to see if an object has a writable property by a given name
   *
   * @param object - the object to check
   * @param propertyName - the property to check for
   * @return True if the property exists and is writable
   */
  public boolean hasWritableProperty(Object object, String propertyName);

  /**
   * Checks to see if an object has a readable property by a given name
   *
   * @param object - the object to check
   * @param propertyName - the property to check for
   * @return True if the property exists and is readable
   */
  public boolean hasReadableProperty(Object object, String propertyName);

}
