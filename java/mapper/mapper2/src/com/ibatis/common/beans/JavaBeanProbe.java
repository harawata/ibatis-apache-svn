package com.ibatis.common.beans;

import java.lang.reflect.Method;
import java.util.List;
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
public class JavaBeanProbe extends BaseProbe {

  private static final Object[] NO_ARGUMENTS = new Object[0];

  protected JavaBeanProbe() {
  }

  /**
   * Returns an array of the readable properties exposed by a bean
   *
   * @param object The bean
   * @return The properties
   */
  public String[] getReadablePropertyNames(Object object) {
    return ClassInfo.getInstance(object.getClass()).getReadablePropertyNames();
  }

  /**
   * Returns an array of the writeable properties exposed by a bean
   *
   * @param object The bean
   * @return The properties
   */
  public String[] getWriteablePropertyNames(Object object) {
    return ClassInfo.getInstance(object.getClass()).getReadablePropertyNames();
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

  /**
   * Returns the class that the setter expects to receive as a parameter when
   * setting a property value.
   *
   * @param type The class to check
   * @param name The name of the property
   * @return The type of the property
   */
  public static Class getPropertyTypeForSetter(Class type, String name) {

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

  /**
   * Returns the class that the getter will return when reading a property value.
   *
   * @param object The bean to check
   * @param name   The name of the property
   * @return The type of the property
   */
  public Class getPropertyTypeForGetter(Object object, String name) {
    Class type = object.getClass();

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

  /**
   * Returns the class that the getter will return when reading a property value.
   *
   * @param type The class to check
   * @param name The name of the property
   * @return The type of the property
   */
  public static Class getPropertyTypeForGetter(Class type, String name) {

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

  /**
   * Checks to see if a bean has a writable property be a given name
   *
   * @param object       The bean to check
   * @param propertyName The property to check for
   * @return True if the property exists and is writable
   */
  public boolean hasWritableProperty(Object object, String propertyName) {
    boolean hasProperty = false;
    if (propertyName.indexOf('.') > -1) {
      StringTokenizer parser = new StringTokenizer(propertyName, ".");
      Class type = object.getClass();
      while (parser.hasMoreTokens()) {
        propertyName = parser.nextToken();
        type = ClassInfo.getInstance(type).getGetterType(propertyName);
        hasProperty = ClassInfo.getInstance(type).hasWritableProperty(propertyName);
      }
    } else {
      hasProperty = ClassInfo.getInstance(object.getClass()).hasWritableProperty(propertyName);
    }
    return hasProperty;
  }

  /**
   * Checks to see if a bean has a readable property be a given name
   *
   * @param object       The bean to check
   * @param propertyName The property to check for
   * @return True if the property exists and is readable
   */
  public boolean hasReadableProperty(Object object, String propertyName) {
    boolean hasProperty = false;
    if (propertyName.indexOf('.') > -1) {
      StringTokenizer parser = new StringTokenizer(propertyName, ".");
      Class type = object.getClass();
      while (parser.hasMoreTokens()) {
        propertyName = parser.nextToken();
        type = ClassInfo.getInstance(type).getGetterType(propertyName);
        hasProperty = ClassInfo.getInstance(type).hasReadableProperty(propertyName);
      }
    } else {
      hasProperty = ClassInfo.getInstance(object.getClass()).hasReadableProperty(propertyName);
    }
    return hasProperty;
  }

  protected Object getProperty(Object object, String name) {
    ClassInfo classCache = ClassInfo.getInstance(object.getClass());
    try {
      Object value = null;
      if (name.indexOf("[") > -1) {
        value = getArrayProperty(object, name);
      } else {
        Method method = classCache.getGetter(name);
        if (method == null) {
          throw new NoSuchMethodException("No GET method for property " + name + " on instance of " + object.getClass().getName());
        }
        try {
          value = method.invoke(object, NO_ARGUMENTS);
        } catch (Throwable t) {
          throw ClassInfo.unwrapThrowable(t);
        }
      }
      return value;
    } catch (ProbeException e) {
      throw e;
    } catch (Throwable t) {
      if (object == null) {
        throw new ProbeException("Could not get property '" + name + "' from null reference.  Cause: " + t.toString(), t);
      } else {
        throw new ProbeException("Could not get property '" + name + "' from " + object.getClass().getName() + ".  Cause: " + t.toString(), t);
      }
    }
  }

  protected void setProperty(Object object, String name, Object value) {
    ClassInfo classCache = ClassInfo.getInstance(object.getClass());
    try {
      if (name.indexOf("[") > -1) {
        setArrayProperty(object, name, value);
      } else {
        Method method = classCache.getSetter(name);
        if (method == null) {
          throw new NoSuchMethodException("No SET method for property " + name + " on instance of " + object.getClass().getName());
        }
        Object[] params = new Object[1];
        params[0] = value;
        try {
          method.invoke(object, params);
        } catch (Throwable t) {
          throw ClassInfo.unwrapThrowable(t);
        }
      }
    } catch (ProbeException e) {
      throw e;
    } catch (Throwable t) {
      if (object == null) {
        throw new ProbeException("Could not set property '" + name + "' for null reference.  Cause: " + t.toString(), t);
      } else {
        throw new ProbeException("Could not set property '" + name + "' for " + object.getClass().getName() + ".  Cause: " + t.toString(), t);
      }
    }
  }

  private Object getArrayProperty(Object object, String indexedName) {

    Object value = null;

    try {
      String name = indexedName.substring(0, indexedName.indexOf("["));
      int i = Integer.parseInt(indexedName.substring(indexedName.indexOf("[") + 1, indexedName.indexOf("]")));
      value = getProperty(object, name);
      if (value instanceof List) {
        value = ((List) value).get(i);
      } else if (value instanceof Object[]) {
        value = ((Object[]) value)[i];
      } else if (value instanceof char[]) {
        value = new Character(((char[]) value)[i]);
      } else if (value instanceof boolean[]) {
        value = new Boolean(((boolean[]) value)[i]);
      } else if (value instanceof byte[]) {
        value = new Byte(((byte[]) value)[i]);
      } else if (value instanceof double[]) {
        value = new Double(((double[]) value)[i]);
      } else if (value instanceof float[]) {
        value = new Float(((float[]) value)[i]);
      } else if (value instanceof int[]) {
        value = new Integer(((int[]) value)[i]);
      } else if (value instanceof long[]) {
        value = new Long(((long[]) value)[i]);
      } else if (value instanceof short[]) {
        value = new Short(((short[]) value)[i]);
      } else {
        throw new ProbeException("The '" + name + "' property of the " + object.getClass().getName() + " class is not a List or Array.");
      }

    } catch (ProbeException e) {
      throw e;
    } catch (Exception e) {
      throw new ProbeException("Error getting ordinal value from JavaBean. Cause " + e, e);
    }

    return value;
  }

  private void setArrayProperty(Object object, String indexedName, Object value) {

    try {
      String name = indexedName.substring(0, indexedName.indexOf("["));
      int i = Integer.parseInt(indexedName.substring(indexedName.indexOf("[") + 1, indexedName.indexOf("]")));
      Object list = getProperty(object, name);
      if (list instanceof List) {
        ((List) list).set(i, value);
      } else if (value instanceof Object[]) {
        value = ((Object[]) value)[i];
      } else if (value instanceof char[]) {
        ((char[]) value)[i] = ((Character) value).charValue();
      } else if (value instanceof boolean[]) {
        ((boolean[]) value)[i] = ((Boolean) value).booleanValue();
      } else if (value instanceof byte[]) {
        ((byte[]) value)[i] = ((Byte) value).byteValue();
      } else if (value instanceof double[]) {
        ((double[]) value)[i] = ((Double) value).doubleValue();
      } else if (value instanceof float[]) {
        ((float[]) value)[i] = ((Float) value).floatValue();
      } else if (value instanceof int[]) {
        ((int[]) value)[i] = ((Integer) value).intValue();
      } else if (value instanceof long[]) {
        ((long[]) value)[i] = ((Long) value).longValue();
      } else if (value instanceof short[]) {
        ((short[]) value)[i] = ((Short) value).shortValue();
      } else {
        throw new ProbeException("The '" + name + "' property of the " + object.getClass().getName() + " class is not a List or Array.");
      }
    } catch (ProbeException e) {
      throw e;
    } catch (Exception e) {
      throw new ProbeException("Error getting ordinal value from JavaBean. Cause " + e, e);
    }

  }

}


