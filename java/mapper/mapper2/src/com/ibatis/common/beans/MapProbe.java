package com.ibatis.common.beans;

import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * StaticBeanProbe provides methods that allow simple, reflective access to
 * JavaBeans style properties.  Methods are provided for all simple types as
 * well as object types.
 * <p/>
 * Examples:
 * <p/>
 * StaticBeanProbe.setObject(object, propertyName, value);
 * <P>
 * Object value = StaticBeanProbe.getObject(object, propertyName);
 *
 * @author clinton_begin
 */
public class MapProbe extends BaseProbe implements Probe {

  private static final BaseProbe PROBE = new GenericProbe();

  protected MapProbe() {
  }

  /**
   * Returns an array of the readable properties exposed by a bean
   *
   * @param object The bean
   * @return The properties
   */
  public String[] getReadablePropertyNames(Object object) {
    Set keySet = ((Map) object).keySet();
    return (String[]) keySet.toArray(new String[keySet.size()]);
  }

  /**
   * Returns an array of the writeable properties exposed by a bean
   *
   * @param object The bean
   * @return The properties
   */
  public String[] getWriteablePropertyNames(Object object) {
    Set keySet = ((Map) object).keySet();
    return (String[]) keySet.toArray(new String[keySet.size()]);
  }

  /**
   * Returns the class that the setter expects to receive as a parameter when
   * setting a property value.
   *
   * @param object The bean to check
   * @param name   The name of the property
   * @return The type of the property
   */
  public Class getPropertyTypeForSetter(Object object, String name) {
    Class type = object.getClass();

    Map map = (Map) object;
    Object value = map.get(name);
    if (value == null) {
      type = Object.class;
    } else {
      type = value.getClass();
    }

    return type;
  }

  /**
   * Returns the class that the getter will return when reading a property value.
   *
   * @param object The bean to check
   * @param name   The name of the property
   * @return The type of the property
   */
  public Class getPropertyTypeForGetter(Object object, String name) {
    Class type = object.getClass();

    Map map = (Map) object;
    Object value = map.get(name);
    if (value == null) {
      type = Object.class;
    } else {
      type = value.getClass();
    }

    return type;
  }

  /**
   * Checks to see if a bean has a writable property be a given name
   *
   * @param object       The bean to check
   * @param propertyName The property to check for
   * @return True if the property exists and is writable
   */
  public boolean hasWritableProperty(Object object, String propertyName) {
    return ((Map) object).containsKey(propertyName);
  }

  /**
   * Checks to see if a bean has a readable property be a given name
   *
   * @param object       The bean to check
   * @param propertyName The property to check for
   * @return True if the property exists and is readable
   */
  public boolean hasReadableProperty(Object object, String propertyName) {
    return ((Map) object).containsKey(propertyName);
  }


  protected Object getProperty(Object object, String name) {
    return ((Map) object).get(name);
  }

  protected void setProperty(Object object, String name, Object value) {
    ((Map) object).put(name, value);
  }

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
        value = PROBE.getProperty(value, parser.nextToken());
        if (value == null) {
          break;
        }
      }
      return value;
    } else if (name.indexOf('[') > -1) {
      return getIndexedProperty(object, name);
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
            PROBE.setObject(parent, property, child);
          } catch (Exception e) {
            throw new ProbeException("Cannot set value of property '" + name + "' because '" + property + "' is null and cannot be instantiated on instance of " + type.getName() + ". Cause:" + e.toString(), e);
          }
        }
        property = parser.nextToken();
      }
      setProperty(child, property, value);
    } else if (name.indexOf('.') > -1) {
      setIndexedProperty(object, name, value);
    } else {
      setProperty(object, name, value);
    }
  }

}


