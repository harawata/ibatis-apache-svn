package com.ibatis.common.beans;

import java.util.Map;
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
 */
public class GenericProbe extends BaseProbe {

  private static final BaseProbe MAP_PROBE = new MapProbe();
  private static final BaseProbe BEAN_PROBE = new JavaBeanProbe();
  private static final BaseProbe DOM_PROBE = new DomProbe();

  protected GenericProbe() {
  }

  /**
   * Gets an object from a Map or bean
   *
   * @param object - the object to probe
   * @param name   - the name of the property (or map entry)
   * @return The value of the property (or map entry)
   * @see com.ibatis.common.beans.BaseProbe#getObject(java.lang.Object, java.lang.String)
   */
  public Object getObject(Object object, String name) {
    if (object instanceof Map) {
      return MAP_PROBE.getObject(object, name);
    } else if (object instanceof org.w3c.dom.Document) {
      return DOM_PROBE.getObject(object, name);
    } else {
      return BEAN_PROBE.getObject(object, name);
    }
  }

  /**
   * Sets an object in a Map or bean
   *
   * @param object - the object to probe
   * @param name   - the name of the property (or map entry)
   * @param value  - the new value of the property (or map entry)
   * @see com.ibatis.common.beans.BaseProbe#setObject(java.lang.Object, java.lang.String, java.lang.Object)
   */
  public void setObject(Object object, String name, Object value) {
    if (object instanceof Map) {
      MAP_PROBE.setObject(object, name, value);
    } else if (object instanceof org.w3c.dom.Document) {
      DOM_PROBE.setObject(object, name, value);
    } else {
      BEAN_PROBE.setObject(object, name, value);
    }
  }

  /**
   * Gets an array of the readable properties in a Map or JavaBean
   *
   * @param object - the object to get properties for
   * @return The array of properties (or map entries)
   * @see com.ibatis.common.beans.BaseProbe#getReadablePropertyNames(java.lang.Object)
   */
  public String[] getReadablePropertyNames(Object object) {
    if (object instanceof Map) {
      return MAP_PROBE.getReadablePropertyNames(object);
    } else if (object instanceof org.w3c.dom.Document) {
      return DOM_PROBE.getReadablePropertyNames(object);
    } else {
      return BEAN_PROBE.getReadablePropertyNames(object);
    }
  }

  /**
   * Gets an array of the writeable properties in a Map or JavaBean
   *
   * @param object - the object to get properties for
   * @return The array of properties (or map entries)
   * @see com.ibatis.common.beans.BaseProbe#getWriteablePropertyNames(java.lang.Object)
   */
  public String[] getWriteablePropertyNames(Object object) {
    if (object instanceof Map) {
      return MAP_PROBE.getWriteablePropertyNames(object);
    } else if (object instanceof org.w3c.dom.Document) {
      return DOM_PROBE.getWriteablePropertyNames(object);
    } else {
      return BEAN_PROBE.getWriteablePropertyNames(object);
    }
  }

  /**
   * Returns the class that the setter expects to receive as a parameter when
   * setting a property value.
   *
   * @param object - The class to check
   * @param name   - the name of the property
   * @return The type of the property
   * @see com.ibatis.common.beans.Probe#getPropertyTypeForSetter(java.lang.Object, java.lang.String)
   */
  public Class getPropertyTypeForSetter(Object object, String name) {
    if (object instanceof Class) {
      return getClassPropertyTypeForSetter((Class) object, name);
    } else if (object instanceof Map) {
      return MAP_PROBE.getPropertyTypeForSetter(object, name);
    } else if (object instanceof org.w3c.dom.Document) {
      return DOM_PROBE.getPropertyTypeForSetter(object, name);
    } else {
      return BEAN_PROBE.getPropertyTypeForSetter(object, name);
    }
  }

  /**
   * Returns the class that the getter will return when reading a property value.
   *
   * @param object The bean to check
   * @param name   The name of the property
   * @return The type of the property
   * @see com.ibatis.common.beans.Probe#getPropertyTypeForGetter(java.lang.Object, java.lang.String)
   */
  public Class getPropertyTypeForGetter(Object object, String name) {
    if (object instanceof Class) {
      return getClassPropertyTypeForGetter((Class) object, name);
    } else if (object instanceof Map) {
      return MAP_PROBE.getPropertyTypeForGetter(object, name);
    } else if (object instanceof org.w3c.dom.Document) {
      return DOM_PROBE.getPropertyTypeForGetter(object, name);
    } else {
      return BEAN_PROBE.getPropertyTypeForGetter(object, name);
    }
  }

  /**
   * Checks to see if an object has a writable property by a given name
   *
   * @param object       The bean to check
   * @param propertyName The property to check for
   * @return True if the property exists and is writable
   * @see com.ibatis.common.beans.Probe#hasWritableProperty(java.lang.Object, java.lang.String)
   */
  public boolean hasWritableProperty(Object object, String propertyName) {
    if (object instanceof Map) {
      return MAP_PROBE.hasWritableProperty(object, propertyName);
    } else if (object instanceof org.w3c.dom.Document) {
      return DOM_PROBE.hasWritableProperty(object, propertyName);
    } else {
      return BEAN_PROBE.hasWritableProperty(object, propertyName);
    }
  }

  /**
   * Checks to see if a bean has a readable property by a given name
   *
   * @param object       The bean to check
   * @param propertyName The property to check for
   * @return True if the property exists and is readable
   * @see com.ibatis.common.beans.Probe#hasReadableProperty(java.lang.Object, java.lang.String)
   */
  public boolean hasReadableProperty(Object object, String propertyName) {
    if (object instanceof Map) {
      return MAP_PROBE.hasReadableProperty(object, propertyName);
    } else if (object instanceof org.w3c.dom.Document) {
      return DOM_PROBE.hasReadableProperty(object, propertyName);
    } else {
      return BEAN_PROBE.hasReadableProperty(object, propertyName);
    }
  }

  protected void setProperty(Object object, String property, Object value) {
    if (object instanceof Map) {
      MAP_PROBE.setProperty(object, property, value);
    } else if (object instanceof org.w3c.dom.Document) {
      DOM_PROBE.setProperty(object, property, value);
    } else {
      BEAN_PROBE.setProperty(object, property, value);
    }
  }

  protected Object getProperty(Object object, String property) {
    if (object instanceof Map) {
      return MAP_PROBE.getProperty(object, property);
    } else if (object instanceof org.w3c.dom.Document) {
      return DOM_PROBE.getProperty(object, property);
    } else {
      return BEAN_PROBE.getProperty(object, property);
    }
  }

  private Class getClassPropertyTypeForSetter(Class type, String name) {

    if (name.indexOf('.') > -1) {
      StringTokenizer parser = new StringTokenizer(name, ".");
      while (parser.hasMoreTokens()) {
        name = parser.nextToken();
        type = ClassInfo.getInstance(type).getSetterType(name);
      }
    } else {
      type = ClassInfo.getInstance(type).getSetterType(name);
    }

    return type;
  }

  private Class getClassPropertyTypeForGetter(Class type, String name) {

    if (name.indexOf('.') > -1) {
      StringTokenizer parser = new StringTokenizer(name, ".");
      while (parser.hasMoreTokens()) {
        name = parser.nextToken();
        type = ClassInfo.getInstance(type).getGetterType(name);
      }
    } else {
      type = ClassInfo.getInstance(type).getGetterType(name);
    }

    return type;
  }

}


