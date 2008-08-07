package org.apache.ibatis.reflection;

import java.util.*;

public class MetaObject {

  private Object object;
  private MetaClass metaClass;
  private ObjectFactory objectFactory;

  private static final Object[] NO_ARGUMENTS = new Object[0];
  private static final ObjectFactory DEFAULT_OBJECT_FACTORY = new DefaultObjectFactory();

  public static final MetaObject NULL_META_OBJECT = new MetaObject(NullObject.class, DEFAULT_OBJECT_FACTORY);

  private MetaObject(Object object, ObjectFactory objectFactory) {
    this.object = object;
    this.metaClass = MetaClass.forClass(object.getClass());
    this.objectFactory = objectFactory;
  }

  public static MetaObject forObject(Object object, ObjectFactory objectFactory) {
    if (object == null) {
      return NULL_META_OBJECT;
    } else {
      return new MetaObject(object, objectFactory);
    }
  }

  public static MetaObject forObject(Object object) {
    return forObject(object, DEFAULT_OBJECT_FACTORY);
  }

  public MetaObject metaObjectForProperty(String name) {
    Object value = getValue(name);
    return MetaObject.forObject(value);
  }

  public String findProperty(String propName) {
    if (object instanceof Map) return propName;
    return metaClass.findProperty(propName);
  }

  public String[] getGetterNames() {
    if (object instanceof Map) return (String[]) ((Map) object).keySet().toArray(new String[((Map) object).size()]);
    return metaClass.getGetterNames();
  }

  public String[] getSetterNames() {
    if (object instanceof Map) return (String[]) ((Map) object).keySet().toArray(new String[((Map) object).size()]);
    return metaClass.getSetterNames();
  }

  public Class getSetterType(String name) {
    if (object instanceof Map)
      return ((Map) object).get(name) == null ? Object.class : ((Map) object).get(name).getClass();
    PropertyTokenizer prop = new PropertyTokenizer(name);
    if (prop.hasNext()) {
      MetaObject metaValue = metaObjectForProperty(prop.getIndexedName());
      if (metaValue == MetaObject.NULL_META_OBJECT) {
        return metaClass.getSetterType(name);
      } else {
        return metaValue.getSetterType(prop.getChildren());
      }
    } else {
      return metaClass.getSetterType(name);
    }
  }

  public Class getGetterType(String name) {
    if (object instanceof Map)
      return ((Map) object).get(name) == null ? Object.class : ((Map) object).get(name).getClass();
    PropertyTokenizer prop = new PropertyTokenizer(name);
    if (prop.hasNext()) {
      MetaObject metaValue = metaObjectForProperty(prop.getIndexedName());
      if (metaValue == MetaObject.NULL_META_OBJECT) {
        return metaClass.getGetterType(name);
      } else {
        return metaValue.getGetterType(prop.getChildren());
      }
    } else {
      return metaClass.getGetterType(name);
    }
  }

  public boolean hasSetter(String name) {
    if (object instanceof Map) return true;
    PropertyTokenizer prop = new PropertyTokenizer(name);
    if (prop.hasNext()) {
      MetaObject metaValue = metaObjectForProperty(prop.getIndexedName());
      if (metaValue == MetaObject.NULL_META_OBJECT) {
        return metaClass.hasSetter(name);
      } else {
        return metaValue.hasSetter(prop.getChildren());
      }
    } else {
      return metaClass.hasSetter(name);
    }
  }

  public boolean hasGetter(String name) {
    if (object instanceof Map) return true;
    PropertyTokenizer prop = new PropertyTokenizer(name);
    if (prop.hasNext()) {
      MetaObject metaValue = metaObjectForProperty(prop.getIndexedName());
      if (metaValue == MetaObject.NULL_META_OBJECT) {
        return metaClass.hasGetter(name);
      } else {
        return metaValue.hasGetter(prop.getChildren());
      }
    } else {
      return metaClass.hasGetter(name);
    }
  }

  public Object getValue(String name) {
    PropertyTokenizer prop = new PropertyTokenizer(name);
    if (prop.hasNext()) {
      MetaObject metaValue = metaObjectForProperty(prop.getIndexedName());
      if (metaValue == MetaObject.NULL_META_OBJECT) {
        return null;
      } else {
        return metaValue.getValue(prop.getChildren());
      }
    } else {
      return getProperty(object, prop);
    }
  }

  public void setValue(String name, Object value) {
    PropertyTokenizer prop = new PropertyTokenizer(name);
    if (prop.hasNext()) {
      MetaObject metaValue = metaObjectForProperty(prop.getIndexedName());
      if (metaValue == MetaObject.NULL_META_OBJECT) {
        if (value == null && prop.getChildren() != null) {
          return; // don't instantiate child path if value is null
        } else {
          Class type = getSetterType(prop.getName());
          try {
            Object newObject = objectFactory.create(type);
            metaValue = MetaObject.forObject(newObject);
            setProperty(object, prop, newObject);
          } catch (Exception e) {
            throw new RuntimeException("Cannot set value of property '" + name + "' because '" + name + "' is null and cannot be instantiated on instance of " + type.getName() + ". Cause:" + e.toString(), e);
          }
        }
      }
      metaValue.setValue(prop.getChildren(), value);
    } else {
      setProperty(object, prop, value);
    }

  }

  private Object getProperty(Object object, PropertyTokenizer prop) {
    if (prop.getIndex() != null) {
      return getIndexedProperty(object, prop);
    } else {
      return getBeanOrMapProperty(object, prop);
    }
  }

  private Object getBeanOrMapProperty(Object object, PropertyTokenizer prop) {
    if (object instanceof Map) {
      return ((Map) object).get(prop.getName());
    } else {
      return getBeanProperty(object, prop);
    }
  }

  private void setProperty(Object object, PropertyTokenizer prop, Object value) {
    if (prop.getIndex() != null) {
      setIndexedProperty(object, prop, value);
    } else if (object instanceof Map) {
      ((Map) object).put(prop.getName(), value);
    } else {
      setBeanProperty(object, prop, value);
    }
  }

  private Object getBeanProperty(Object object, PropertyTokenizer prop) {
    try {
      Invoker method = metaClass.getGetInvoker(prop.getName());
      try {
        return method.invoke(object, NO_ARGUMENTS);
      } catch (Throwable t) {
        throw ExceptionUtil.unwrapThrowable(t);
      }
    } catch (RuntimeException e) {
      throw e;
    } catch (Throwable t) {
      throw new RuntimeException("Could not get property '" + prop.getName() + "' from " + object + ".  Cause: " + t.toString(), t);
    }
  }

  private void setBeanProperty(Object object, PropertyTokenizer prop, Object value) {
    try {
      Invoker method = metaClass.getSetInvoker(prop.getName());
      Object[] params = {value};
      try {
        method.invoke(object, params);
      } catch (Throwable t) {
        throw ExceptionUtil.unwrapThrowable(t);
      }
    } catch (Throwable t) {
      throw new RuntimeException("Could not set property '" + prop.getName() + "' for " + object + ".  Cause: " + t.toString(), t);
    }
  }

  private Object getIndexedProperty(Object object, PropertyTokenizer prop) {
    Object value;
    try {
      int i = Integer.parseInt(prop.getIndex());
      Object list;
      if ("".equals(prop.getName())) {
        list = object;
      } else {
        list = getBeanOrMapProperty(object, prop);
      }
      if (list instanceof List) {
        value = ((List) list).get(i);
      } else if (list instanceof Object[]) {
        value = ((Object[]) list)[i];
      } else if (list instanceof char[]) {
        value = ((char[]) list)[i];
      } else if (list instanceof boolean[]) {
        value = ((boolean[]) list)[i];
      } else if (list instanceof byte[]) {
        value = ((byte[]) list)[i];
      } else if (list instanceof double[]) {
        value = ((double[]) list)[i];
      } else if (list instanceof float[]) {
        value = ((float[]) list)[i];
      } else if (list instanceof int[]) {
        value = ((int[]) list)[i];
      } else if (list instanceof long[]) {
        value = ((long[]) list)[i];
      } else if (list instanceof short[]) {
        value = ((short[]) list)[i];
      } else {
        throw new RuntimeException("The '" + prop.getName() + "' property of " + object + " is not a List or Array.");
      }
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException("Error getting ordinal list from JavaBean. Cause " + e, e);
    }
    return value;
  }

  private void setIndexedProperty(Object object, PropertyTokenizer prop, Object value) {
    try {
      int i = Integer.parseInt(prop.getIndex());
      Object list;
      if ("".equals(prop.getName())) {
        list = object;
      } else {
        list = getBeanOrMapProperty(object, prop);
      }
      if (list instanceof List) {
        ((List) list).set(i, value);
      } else if (list instanceof Object[]) {
        ((Object[]) list)[i] = value;
      } else if (list instanceof char[]) {
        ((char[]) list)[i] = (Character) value;
      } else if (list instanceof boolean[]) {
        ((boolean[]) list)[i] = (Boolean) value;
      } else if (list instanceof byte[]) {
        ((byte[]) list)[i] = (Byte) value;
      } else if (list instanceof double[]) {
        ((double[]) list)[i] = (Double) value;
      } else if (list instanceof float[]) {
        ((float[]) list)[i] = (Float) value;
      } else if (list instanceof int[]) {
        ((int[]) list)[i] = (Integer) value;
      } else if (list instanceof long[]) {
        ((long[]) list)[i] = (Long) value;
      } else if (list instanceof short[]) {
        ((short[]) list)[i] = (Short) value;
      } else {
        throw new RuntimeException("The '" + prop.getName() + "' property of " + object + " is not a List or Array.");
      }
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException("Error getting ordinal value from JavaBean. Cause " + e, e);
    }
  }

  private static class NullObject {
  }

}
