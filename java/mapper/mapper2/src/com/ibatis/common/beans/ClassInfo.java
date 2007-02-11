/*
 *  Copyright 2004 Clinton Begin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.ibatis.common.beans;

import com.ibatis.common.logging.Log;
import com.ibatis.common.logging.LogFactory;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * This class represents a cached set of class definition information that
 * allows for easy mapping between property names and getter/setter methods.
 */
public class ClassInfo {
  
  private static final Log log = LogFactory.getLog(ClassInfo.class);

  private static boolean cacheEnabled = true;
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
  private Constructor defaultConstructor;

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
    SIMPLE_TYPE_SET.add(BigDecimal.class);

    SIMPLE_TYPE_SET.add(Collection.class);
    SIMPLE_TYPE_SET.add(Set.class);
    SIMPLE_TYPE_SET.add(Map.class);
    SIMPLE_TYPE_SET.add(List.class);
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
    addDefaultConstructor(clazz);
    addMethods(clazz);
    addFields (clazz);
    readablePropertyNames = (String[]) getMethods.keySet().toArray(new String[getMethods.keySet().size()]);
    writeablePropertyNames = (String[]) setMethods.keySet().toArray(new String[setMethods.keySet().size()]);
  }

  private void addDefaultConstructor(Class clazz) {
    Constructor[] consts = clazz.getDeclaredConstructors();
    for (int i = 0; i < consts.length; i++) {
      Constructor constructor = consts[i];
      if (constructor.getParameterTypes().length == 0) {
        if (canAccessPrivateMethods()) {
          try {
            constructor.setAccessible(true);
          } catch (Exception e) {
            // Ignored. This is only a final precaution, nothing we can do.
          }
        }
        if (constructor.isAccessible()) {
          this.defaultConstructor = constructor;
        }
      }
    }
  }

  private void addFields(Class clazz) {
    Field[] fields = clazz.getDeclaredFields();
    for (int i=0; i < fields.length; i++) {
      Field field = fields[i];
      if (canAccessPrivateMethods()) {
        try {
          field.setAccessible(true);
        } catch (Exception e) {
          // Ignored. This is only a final precaution, nothing we can do.
        }
      }
      if (field.isAccessible()) {
        setMethods.put("(" + field.getName() + ")", new SetFieldInvoker(field));
        setTypes.put("(" + field.getName() + ")", field.getType());
        getMethods.put("(" + field.getName() + ")", new GetFieldInvoker(field));
        getTypes.put("(" + field.getName() + ")", field.getType());
      }
    }
    if (clazz.getSuperclass() != null) {
      addFields(clazz.getSuperclass());
    }
  }

  private void addMethods(Class cls) {
    Method[] methods = getAllMethodsForClass(cls);
    for (int i = 0; i < methods.length; i++) {
      Method method = methods[i];
      String name = method.getName();
      if (name.startsWith("set") && name.length() > 3) {
        if (method.getParameterTypes().length == 1) {
          name = dropCase(name);
          if (setMethods.containsKey(name)) {
            // TODO(JGB) - this should probably be a RuntimeException at some point???
            log.error("Illegal overloaded setter method for property " + name + " in class " + cls.getName() +
                ".  This breaks the JavaBeans specification and can cause unpredicatble results.");
          }
          setMethods.put(name, new MethodInvoker(method));
          setTypes.put(name, method.getParameterTypes()[0]);
        }
      } else if (name.startsWith("get") && name.length() > 3) {
        if (method.getParameterTypes().length == 0) {
          name = dropCase(name);
          getMethods.put(name, new MethodInvoker(method));
          getTypes.put(name, method.getReturnType());
        }
      } else if (name.startsWith("is") && name.length() > 2) {
        if (method.getParameterTypes().length == 0) {
          name = dropCase(name);
          getMethods.put(name, new MethodInvoker(method));
          getTypes.put(name, method.getReturnType());
        }
      }
      name = null;
    }
  }

  private Method[] getAllMethodsForClass(Class cls) {
    if (cls.isInterface()) {
      // interfaces only have public methods - so the
      // simple call is all we need (this will also get superinterface methods)
      return cls.getMethods();
    } else {
      // need to get all the declared methods in this class
      // and any super-class - then need to set access appropriatly
      // for private methods
      return getClassMethods(cls);
    }
  }
  
  /**
   * This method returns an array containing all methods
   * declared in this class and any superclass.
   * We use this method, instead of the simpler Class.getMethods(),
   * because we want to look for private methods as well. 
   * 
   * @param cls The class
   * @return An array containing all methods in this class
   */
  private Method[] getClassMethods(Class cls) {
    HashMap uniqueMethods = new HashMap();
    Class currentClass = cls;
    while (currentClass != null) {
      addUniqueMethods(uniqueMethods, currentClass.getDeclaredMethods());
      
      // we also need to look for interface methods - 
      // because the class may be abstract
      Class[] interfaces = currentClass.getInterfaces();
      for (int i = 0; i < interfaces.length; i++) {
        addUniqueMethods(uniqueMethods, interfaces[i].getMethods());
      }
      
      currentClass = currentClass.getSuperclass();
    }
    
    Collection methods = uniqueMethods.values();
    
    return (Method[]) methods.toArray(new Method[methods.size()]);
  }

  private void addUniqueMethods(HashMap uniqueMethods, Method[] methods) {
    for (int i = 0; i < methods.length; i++) {
      Method currentMethod = methods[i];
      String signature = getSignature(currentMethod);
      // check to see if the method is already known
      // if it is known, then an extended class must have
      // overridden a method
      if (!uniqueMethods.containsKey(signature)) {
        if (canAccessPrivateMethods()) {
          try {
            currentMethod.setAccessible(true);
          } catch (Exception e) {
            // Ignored. This is only a final precaution, nothing we can do.
          }
        }
        
        uniqueMethods.put(signature, currentMethod);
      }
    }
  }
  
  private String getSignature(Method method) {
    StringBuffer sb = new StringBuffer();
    sb.append(method.getName());
    Class[] parameters = method.getParameterTypes();
    
    for (int i = 0; i < parameters.length; i++) {
      if (i == 0) {
        sb.append(':');
      } else {
        sb.append(',');
      }
      sb.append(parameters[i].getName());
    }
    
    return sb.toString();
  }

  private static String dropCase(String name) {
    if (name.startsWith("is")) {
      name = name.substring(2);
    } else if (name.startsWith("get") || name.startsWith("set")) {
      name = name.substring(3);
    } else {
      throw new ProbeException("Error parsing property name '" + name + "'.  Didn't start with 'is', 'get' or 'set'.");
    }

    if (name.length() == 1 || (name.length() > 1 && !Character.isUpperCase(name.charAt(1)))) {
      name = name.substring(0, 1).toLowerCase(Locale.US) + name.substring(1);
    }

    return name;
  }

  private static boolean canAccessPrivateMethods() {
    try {
      System.getSecurityManager().checkPermission(new ReflectPermission("suppressAccessChecks"));
      return true;
    } catch (SecurityException e) {
      return false;
    } catch (NullPointerException e) {
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Gets the name of the class the instance provides information for
   *
   * @return The class name
   */
  public String getClassName() {
    return className;
  }

  public Object instantiateClass() {
    if (defaultConstructor != null) {
      try {
        return defaultConstructor.newInstance(null);
      } catch (Exception e) {
        throw new RuntimeException("Error instantiating class. Cause: " + e, e);
      }
    } else {
      throw new RuntimeException ("Error instantiating class.  There is no default constructor for class " + className);
    }
  }

  /**
   * Gets the setter for a property as a Method object
   *
   * @param propertyName - the property
   * @return The Method
   */
  public Method getSetter(String propertyName) {
    Invoker method = (Invoker) setMethods.get(propertyName);
    if (method == null) {
      throw new ProbeException("There is no WRITEABLE property named '" + propertyName + "' in class '" + className + "'");
    }
    if (!(method instanceof MethodInvoker)) {
      throw new ProbeException("Can't get setter method because '" + propertyName + "' is a field in class '" + className + "'");    
    }
    return ((MethodInvoker)method).getMethod();
  }

  /**
   * Gets the getter for a property as a Method object
   *
   * @param propertyName - the property
   * @return The Method
   */
  public Method getGetter(String propertyName) {
    Invoker method = (Invoker) getMethods.get(propertyName);
    if (method == null) {
      throw new ProbeException("There is no READABLE property named '" + propertyName + "' in class '" + className + "'");
    }
    if (!(method instanceof MethodInvoker)) {
      throw new ProbeException("Can't get getter method because '" + propertyName + "' is a field in class '" + className + "'");
    }
    return ((MethodInvoker)method).getMethod();
  }

  public Invoker getSetInvoker(String propertyName) {
    Invoker method = (Invoker) setMethods.get(propertyName);
    if (method == null) {
      throw new ProbeException("There is no WRITEABLE property named '" + propertyName + "' in class '" + className + "'");
    }
    return method;
  }

  public Invoker getGetInvoker(String propertyName) {
    Invoker method = (Invoker) getMethods.get(propertyName);
    if (method == null) {
      throw new ProbeException("There is no READABLE property named '" + propertyName + "' in class '" + className + "'");
    }
    return method;
  }

  /**
   * Gets the type for a property setter
   *
   * @param propertyName - the name of the property
   * @return The Class of the propery setter
   */
  public Class getSetterType(String propertyName) {
    Class clazz = (Class) setTypes.get(propertyName);
    if (clazz == null) {
      throw new ProbeException("There is no WRITEABLE property named '" + propertyName + "' in class '" + className + "'");
    }
    return clazz;
  }

  /**
   * Gets the type for a property getter
   *
   * @param propertyName - the name of the property
   * @return The Class of the propery getter
   */
  public Class getGetterType(String propertyName) {
    Class clazz = (Class) getTypes.get(propertyName);
    if (clazz == null) {
      throw new ProbeException("There is no READABLE property named '" + propertyName + "' in class '" + className + "'");
    }
    return clazz;
  }

  /**
   * Gets an array of the readable properties for an object
   *
   * @return The array
   */
  public String[] getReadablePropertyNames() {
    return readablePropertyNames;
  }

  /**
   * Gets an array of the writeable properties for an object
   *
   * @return The array
   */
  public String[] getWriteablePropertyNames() {
    return writeablePropertyNames;
  }

  /**
   * Check to see if a class has a writeable property by name
   *
   * @param propertyName - the name of the property to check
   * @return True if the object has a writeable property by the name
   */
  public boolean hasWritableProperty(String propertyName) {
    return setMethods.keySet().contains(propertyName);
  }

  /**
   * Check to see if a class has a readable property by name
   *
   * @param propertyName - the name of the property to check
   * @return True if the object has a readable property by the name
   */
  public boolean hasReadableProperty(String propertyName) {
    return getMethods.keySet().contains(propertyName);
  }

  /**
   * Tells us if the class passed in is a knwon common type
   *
   * @param clazz The class to check
   * @return True if the class is known
   */
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
    if (cacheEnabled) {
      synchronized (clazz) {
        ClassInfo cached = (ClassInfo) CLASS_INFO_MAP.get(clazz);
        if (cached == null) {
          cached = new ClassInfo(clazz);
          CLASS_INFO_MAP.put(clazz, cached);
        }
        return cached;
      }
    } else {
      return new ClassInfo(clazz);
    }
  }

  public static void setCacheEnabled(boolean cacheEnabled) {
    ClassInfo.cacheEnabled = cacheEnabled;
  }

  /**
   * Examines a Throwable object and gets it's root cause
   *
   * @param t - the exception to examine
   * @return The root cause
   */
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



