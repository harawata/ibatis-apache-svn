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
 *
 * @author clinton_begin
 */
public class GenericProbe extends BaseProbe {

  public static final BaseProbe MAP_PROBE = new MapProbe();
  public static final BaseProbe BEAN_PROBE = new JavaBeanProbe();

  protected GenericProbe() {
  }

  public Object getObject(Object object, String name) {
    if (object instanceof Map) {
      return MAP_PROBE.getObject(object, name);
    } else {
      return BEAN_PROBE.getObject(object, name);
    }
  }

  public void setObject(Object object, String name, Object value) {
    if (object instanceof Map) {
      MAP_PROBE.setObject(object, name, value);
    } else {
      BEAN_PROBE.setObject(object, name, value);
    }
  }

  public String[] getReadablePropertyNames(Object object) {
    if (object instanceof Map) {
      return MAP_PROBE.getReadablePropertyNames(object);
    } else {
      return BEAN_PROBE.getReadablePropertyNames(object);
    }
  }

  public String[] getWriteablePropertyNames(Object object) {
    if (object instanceof Map) {
      return MAP_PROBE.getWriteablePropertyNames(object);
    } else {
      return BEAN_PROBE.getWriteablePropertyNames(object);
    }
  }

  public Class getPropertyTypeForSetter(Object object, String name) {
    if (object instanceof Class) {
      return getClassPropertyTypeForSetter((Class) object, name);
    } else if (object instanceof Map) {
      return MAP_PROBE.getPropertyTypeForSetter(object, name);
    } else {
      return BEAN_PROBE.getPropertyTypeForSetter(object, name);
    }
  }

  public Class getPropertyTypeForGetter(Object object, String name) {
    if (object instanceof Class) {
      return getClassPropertyTypeForGetter((Class) object, name);
    } else if (object instanceof Map) {
      return MAP_PROBE.getPropertyTypeForGetter(object, name);
    } else {
      return BEAN_PROBE.getPropertyTypeForGetter(object, name);
    }
  }

  public boolean hasWritableProperty(Object object, String propertyName) {
    if (object instanceof Map) {
      return MAP_PROBE.hasWritableProperty(object, propertyName);
    } else {
      return BEAN_PROBE.hasWritableProperty(object, propertyName);
    }
  }

  public boolean hasReadableProperty(Object object, String propertyName) {
    if (object instanceof Map) {
      return MAP_PROBE.hasReadableProperty(object, propertyName);
    } else {
      return BEAN_PROBE.hasReadableProperty(object, propertyName);
    }
  }

  protected void setProperty(Object object, String property, Object value) {
    if (object instanceof Map) {
      MAP_PROBE.setProperty(object, property, value);
    } else {
      BEAN_PROBE.setProperty(object, property, value);
    }
  }

  protected Object getProperty(Object object, String property) {
    if (object instanceof Map) {
      return MAP_PROBE.getProperty(object, property);
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


