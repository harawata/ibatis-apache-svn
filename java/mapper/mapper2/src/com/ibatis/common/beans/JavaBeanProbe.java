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

import java.lang.reflect.Method;
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

  protected Object getProperty(Object object, String name) {
    ClassInfo classCache = ClassInfo.getInstance(object.getClass());
    try {
      Object value = null;
      if (name.indexOf("[") > -1) {
        value = getIndexedProperty(object, name);
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
        setIndexedProperty(object, name, value);
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

}


