package com.ibatis.common.beans;

import java.lang.reflect.*;
import java.util.*;

/**
 * StaticBeanProbe provides methods that allow simple, reflective access to
 * JavaBeans style properties.  Methods are provided for all simple types as
 * well as object types.
 * <p>
 * Examples:
 * <p>
 * StaticBeanProbe.setObject(object, propertyName, value);
 * <P>
 * Object value = StaticBeanProbe.getObject(object, propertyName);
 *
 * @author clinton_begin
 *
 */
public class BeanProbe extends Object {

  private static final Object[] NO_ARGUMENTS = new Object[0];

  private BeanProbe() {
  }

  /** Returns an array of the readable properties exposed by a bean
   * @param object The bean
   * @return The properties
   */
  public static String[] getReadablePropertyNames(Object object) {
    return ClassInfo.getInstance(object.getClass()).getReadablePropertyNames();
  }

  /** Returns an array of the writeable properties exposed by a bean
   * @param object The bean
   * @return The properties
   */
  public static String[] getWriteablePropertyNames(Object object) {
    return ClassInfo.getInstance(object.getClass()).getReadablePropertyNames();
  }

  /** Returns the class that the setter expects to receive as a parameter when
   * setting a property value.
   * @param object The bean to check
   * @param name The name of the property
   * @return The type of the property
   */
  public static Class getPropertyTypeForSetter(Object object, String name) {
    Class type = object.getClass();

    if (object instanceof Map) {
      Map map = (Map) object;
      Object value = map.get(name);
      if (value == null) {
        type = Object.class;
      } else {
        type = value.getClass();
      }
    } else {
      if (name.indexOf('.') > -1) {
        StringTokenizer parser = new StringTokenizer(name, ".");
        while (parser.hasMoreTokens()) {
          name = parser.nextToken();
          type = ClassInfo.getInstance(type).getSetterType(name);
        }
      } else {
        type = ClassInfo.getInstance(type).getSetterType(name);
      }
    }

    return type;
  }

  /** Returns the class that the setter expects to receive as a parameter when
   * setting a property value.
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

  /** Returns the class that the getter will return when reading a property value.
   * @param object The bean to check
   * @param name The name of the property
   * @return The type of the property
   */
  public static Class getPropertyTypeForGetter(Object object, String name) {
    Class type = object.getClass();

    if (object instanceof Map) {
      Map map = (Map) object;
      Object value = map.get(name);
      if (value == null) {
        type = Object.class;
      } else {
        type = value.getClass();
      }
    } else {
      if (name.indexOf('.') > -1) {
        StringTokenizer parser = new StringTokenizer(name, ".");
        while (parser.hasMoreTokens()) {
          name = parser.nextToken();
          type = ClassInfo.getInstance(type).getGetterType(name);
        }
      } else {
        type = ClassInfo.getInstance(type).getGetterType(name);
      }
    }

    return type;
  }

  /** Returns the class that the getter will return when reading a property value.
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


  private static Object getArrayProperty(Object object, String indexedName) {

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
        throw new BeanException("The '" + name + "' property of the " + object.getClass().getName() + " class is not a List or Array.");
      }

    } catch (BeanException e) {
      throw e;
    } catch (Exception e) {
      throw new BeanException("Error getting ordinal value from JavaBean. Cause " + e, e);
    }

    return value;
  }

  private static Object getProperty(Object object, String name) {
    ClassInfo classCache = ClassInfo.getInstance(object.getClass());
    try {
      Object value = null;
      if (name.indexOf("[") > -1) {
        value = getArrayProperty(object, name);
      } else {
        if (object instanceof Map) {
          value = ((Map) object).get(name);
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
      }
      return value;
    } catch (BeanException e) {
      throw e;
    } catch (Throwable t) {
      if (object == null) {
        throw new BeanException("Could not get property '" + name + "' from null reference.  Cause: " + t.toString(), t);
      } else {
        throw new BeanException("Could not get property '" + name + "' from " + object.getClass().getName() + ".  Cause: " + t.toString(), t);
      }
    }
  }

  private static void setArrayProperty(Object object, String indexedName, Object value) {

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
        throw new BeanException("The '" + name + "' property of the " + object.getClass().getName() + " class is not a List or Array.");
      }
    } catch (BeanException e) {
      throw e;
    } catch (Exception e) {
      throw new BeanException("Error getting ordinal value from JavaBean. Cause " + e, e);
    }

  }

  private static void setProperty(Object object, String name, Object value) {
    ClassInfo classCache = ClassInfo.getInstance(object.getClass());
    try {
      if (name.indexOf("[") > -1) {
        setArrayProperty(object, name, value);
      } else {
        if (object instanceof Map) {
          ((Map) object).put(name, value);
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
      }
    } catch (BeanException e) {
      throw e;
    } catch (Throwable t) {
      if (object == null) {
        throw new BeanException("Could not set property '" + name + "' for null reference.  Cause: " + t.toString(), t);
      } else {
        throw new BeanException("Could not set property '" + name + "' for " + object.getClass().getName() + ".  Cause: " + t.toString(), t);
      }
    }
  }

  /** Gets an Object property from a bean
   * @param object The bean
   * @param name The property name
   * @return The property value (as an Object)
   */
  public static Object getObject(Object object, String name) {
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

  /** Sets the value of a bean property to an Object
   * @param object The bean to change
   * @param name The name of the property to set
   * @param value The new value to set
   */
  public static void setObject(Object object, String name, Object value) {
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
            throw new BeanException("Cannot set value of property '" + name + "' because '" + property + "' is null and cannot be instantiated on instance of " + type.getName() + ". Cause:" + e.toString(), e);
          }
        }
        property = parser.nextToken();
      }
      setProperty(child, property, value);
    } else {
      setProperty(object, name, value);
    }
  }


  /** Checks to see if a bean has a writable property be a given name
   * @param object The bean to check
   * @param propertyName The property to check for
   * @return True if the property exists and is writable
   */
  public static boolean hasWritableProperty(Object object, String propertyName) {
    boolean hasProperty = false;
    if (object instanceof Map) {
      hasProperty = ((Map) object).containsKey(propertyName);
    } else {
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
    }
    return hasProperty;
  }

  /** Checks to see if a bean has a readable property be a given name
   * @param object The bean to check
   * @param propertyName The property to check for
   * @return True if the property exists and is readable
   */
  public static boolean hasReadableProperty(Object object, String propertyName) {
    boolean hasProperty = false;
    if (object instanceof Map) {
      hasProperty = ((Map) object).containsKey(propertyName);
    } else {
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
    }
    return hasProperty;
  }

  // ARRAY UTILITIES

  public static List arrayToList(Object array) {
    List list = null;
    if (array instanceof Object[]) {
      list = Arrays.asList((Object[]) array);
    } else {
      list = new ArrayList();
      for (int i = 0, n = Array.getLength(array); i < n; i++) {
        list.add(Array.get(array, i));
      }
    }
    return list;
  }

  public static Object[] listToArray(List list, Class type) {
    Object array = Array.newInstance(type, list.size());
    array = list.toArray((Object[]) array);
    return (Object[]) array;
  }


}


