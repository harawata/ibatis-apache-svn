/**
 * User: Clinton Begin
 * Date: Aug 23, 2003
 * Time: 7:38:47 AM
 */
package com.ibatis.common.beans;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.math.BigInteger;
import java.util.*;

/**
 * This class represents a cached set of class definition information that
 * allows for easy mapping between property names and getter/setter methods.
 */
public class ClassInfo {

  private static final String[] EMPTY_STRING_ARRAY = new String[0];
  private static final Set SIMPLE_TYPE_SET = new HashSet();
  private static final Map CLASS_INFO_MAP = Collections.synchronizedMap(new HashMap());

  private String className;
  private String[] readablePropertyNames = EMPTY_STRING_ARRAY;
  private String[] writeablePropertyNames = EMPTY_STRING_ARRAY;
  private HashMap setMethods = new HashMap();
  private HashMap getMethods = new HashMap();
  private HashMap setTypes = new HashMap();
  private HashMap getTypes = new HashMap();

  static {
    SIMPLE_TYPE_SET.add(String.class);
    SIMPLE_TYPE_SET.add(Byte.class);
    SIMPLE_TYPE_SET.add(Short.class);
    SIMPLE_TYPE_SET.add(Character.class);
    SIMPLE_TYPE_SET.add(Integer.class);
    SIMPLE_TYPE_SET.add(Long.class);
    SIMPLE_TYPE_SET.add(Float.class);
    SIMPLE_TYPE_SET.add(Double.class);
    SIMPLE_TYPE_SET.add(Boolean.class);
    SIMPLE_TYPE_SET.add(Date.class);
    SIMPLE_TYPE_SET.add(Class.class);
    SIMPLE_TYPE_SET.add(BigInteger.class);
    SIMPLE_TYPE_SET.add(Collection.class);
    SIMPLE_TYPE_SET.add(HashMap.class);
    SIMPLE_TYPE_SET.add(TreeMap.class);
    SIMPLE_TYPE_SET.add(ArrayList.class);
    SIMPLE_TYPE_SET.add(LinkedList.class);
    SIMPLE_TYPE_SET.add(HashSet.class);
    SIMPLE_TYPE_SET.add(TreeSet.class);
    SIMPLE_TYPE_SET.add(Vector.class);
    SIMPLE_TYPE_SET.add(Hashtable.class);
    SIMPLE_TYPE_SET.add(Enumeration.class);
  }

  private ClassInfo(Class clazz) {
    className = clazz.getName();
    addMethods(clazz);
    Class superClass = clazz.getSuperclass();
    while (superClass != null) {
      addMethods(superClass);
      superClass = superClass.getSuperclass();
    }
    readablePropertyNames = (String[]) getMethods.keySet().toArray(new String[getMethods.keySet().size()]);
    writeablePropertyNames = (String[]) setMethods.keySet().toArray(new String[setMethods.keySet().size()]);
  }

  private void addMethods(Class cls) {
    Method[] methods = cls.getMethods();
    for (int i = 0; i < methods.length; i++) {
      String name = methods[i].getName();
      if (name.indexOf("set") == 0 && name.length() > 3) {
        if (methods[i].getParameterTypes().length == 1) {
          name = name.substring(3, 4).toLowerCase() + name.substring(4);
          setMethods.put(name, methods[i]);
          setTypes.put(name, methods[i].getParameterTypes()[0]);
        }
      } else if (name.indexOf("get") == 0 && name.length() > 3) {
        if (methods[i].getParameterTypes().length == 0) {
          name = name.substring(3, 4).toLowerCase() + name.substring(4);
          getMethods.put(name, methods[i]);
          getTypes.put(name, methods[i].getReturnType());
        }
      } else if (name.indexOf("is") == 0 && name.length() > 2) {
        if (methods[i].getParameterTypes().length == 0) {
          name = name.substring(2, 3).toLowerCase() + name.substring(3);
          getMethods.put(name, methods[i]);
          getTypes.put(name, methods[i].getReturnType());
        }
      }
      name = null;
    }
  }

  public String getClassName() {
    return className;
  }

  public Method getSetter(String propertyName) {
    Method method = (Method) setMethods.get(propertyName);
    if (method == null) {
      throw new ProbeException("There is no WRITEABLE property named '" + propertyName + "' in class '" + className + "'");
    }
    return method;
  }

  public Method getGetter(String propertyName) {
    Method method = (Method) getMethods.get(propertyName);
    if (method == null) {
      throw new ProbeException("There is no READABLE property named '" + propertyName + "' in class '" + className + "'");
    }
    return method;
  }

  public Class getSetterType(String propertyName) {
    Class clazz = (Class) setTypes.get(propertyName);
    if (clazz == null) {
      throw new ProbeException("There is no WRITEABLE property named '" + propertyName + "' in class '" + className + "'");
    }
    return clazz;
  }

  public Class getGetterType(String propertyName) {
    Class clazz = (Class) getTypes.get(propertyName);
    if (clazz == null) {
      throw new ProbeException("There is no READABLE property named '" + propertyName + "' in class '" + className + "'");
    }
    return clazz;
  }

  public String[] getReadablePropertyNames() {
    return readablePropertyNames;
  }

  public String[] getWriteablePropertyNames() {
    return writeablePropertyNames;
  }

  public boolean hasWritableProperty(String propertyName) {
    return setMethods.keySet().contains(propertyName);
  }

  public boolean hasReadableProperty(String propertyName) {
    return getMethods.keySet().contains(propertyName);
  }

  public static boolean isKnownType(Class clazz) {
    if (SIMPLE_TYPE_SET.contains(clazz)) {
      return true;
    } else if (Collection.class.isAssignableFrom(clazz)) {
      return true;
    } else if (Map.class.isAssignableFrom(clazz)) {
      return true;
    } else if (List.class.isAssignableFrom(clazz)) {
      return true;
    } else if (Set.class.isAssignableFrom(clazz)) {
      return true;
    } else if (Iterator.class.isAssignableFrom(clazz)) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Gets an instance of ClassInfo for the specified class.
   *
   * @param clazz The class for which to lookup the method cache.
   * @return The method cache for the class
   */
  public static ClassInfo getInstance(Class clazz) {
    synchronized (clazz) {
      ClassInfo cache = (ClassInfo) CLASS_INFO_MAP.get(clazz);
      if (cache == null) {
        cache = new ClassInfo(clazz);
        CLASS_INFO_MAP.put(clazz, cache);
      }
      return cache;
    }
  }

  public static Throwable unwrapThrowable(Throwable t) {
    Throwable t2 = t;
    while (true) {
      if (t2 instanceof InvocationTargetException) {
        t2 = ((InvocationTargetException) t).getTargetException();
      } else if (t instanceof UndeclaredThrowableException) {
        t2 = ((UndeclaredThrowableException) t).getUndeclaredThrowable();
      } else {
        return t2;
      }
    }
  }


}



