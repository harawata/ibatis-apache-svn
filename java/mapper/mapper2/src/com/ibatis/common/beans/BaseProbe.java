package com.ibatis.common.beans;

import java.util.StringTokenizer;

/**
 * <p/>
 * Date: Apr 23, 2004 9:34:47 PM
 * 
 * @author Clinton Begin
 */
public abstract class BaseProbe implements Probe {

  /**
   * Gets an Object property from a bean
   *
   * @param object The bean
   * @param name   The property name
   * @return The property value (as an Object)
   */
  public Object getObject(Object object, String name) {
    if (name.indexOf('.') > -1) {
      StringTokenizer parser = new StringTokenizer(name, ".");
      Object value = object;
      while (parser.hasMoreTokens()) {
        value = getProperty(value, parser.nextToken());
        if (value == null) {
          break;
        }
      }
      return value;
    } else {
      return getProperty(object, name);
    }
  }

  /**
   * Sets the value of a bean property to an Object
   *
   * @param object The bean to change
   * @param name   The name of the property to set
   * @param value  The new value to set
   */
  public void setObject(Object object, String name, Object value) {
    if (name.indexOf('.') > -1) {
      StringTokenizer parser = new StringTokenizer(name, ".");
      String property = parser.nextToken();
      Object child = object;
      while (parser.hasMoreTokens()) {
        Class type = getPropertyTypeForSetter(child, property);
        Object parent = child;
        child = getProperty(parent, property);
        if (child == null) {
          try {
            child = type.newInstance();
            setObject(parent, property, child);
          } catch (Exception e) {
            throw new ProbeException("Cannot set value of property '" + name + "' because '" + property + "' is null and cannot be instantiated on instance of " + type.getName() + ". Cause:" + e.toString(), e);
          }
        }
        property = parser.nextToken();
      }
      setProperty(child, property, value);
    } else {
      setProperty(object, name, value);
    }
  }

  protected abstract void setProperty(Object object, String property, Object value);

  protected abstract Object getProperty(Object object, String property);

}
