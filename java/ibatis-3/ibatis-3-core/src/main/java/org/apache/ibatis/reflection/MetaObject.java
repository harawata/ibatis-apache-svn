package org.apache.ibatis.reflection;

import org.apache.ibatis.reflection.wrapper.BeanWrapper;
import org.apache.ibatis.reflection.wrapper.MapWrapper;
import org.apache.ibatis.reflection.wrapper.ObjectWrapper;
import org.apache.ibatis.reflection.property.PropertyTokenizer;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;

import java.util.Map;

public class MetaObject {

  private static final ObjectFactory DEFAULT_OBJECT_FACTORY = new DefaultObjectFactory();
  public static final MetaObject NULL_META_OBJECT = new MetaObject(NullObject.class, DEFAULT_OBJECT_FACTORY);

  private ObjectWrapper dynamicObject;
  private ObjectFactory objectFactory;

  private MetaObject(Object object, ObjectFactory objectFactory) {
    this.objectFactory = objectFactory;
    if (object instanceof ObjectWrapper) {
      this.dynamicObject = (ObjectWrapper) object;
    } else if (object instanceof Map) {
      this.dynamicObject = new MapWrapper(this, (Map)object);
    } else {
      this.dynamicObject = new BeanWrapper(this, object);
    }
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

  public String findProperty(String propName) {
    return dynamicObject.findProperty(propName);
  }

  public String[] getGetterNames() {
    return dynamicObject.getGetterNames();
  }

  public String[] getSetterNames() {
    return dynamicObject.getSetterNames();
  }

  public Class getSetterType(String name) {
    return dynamicObject.getSetterType(name);
  }

  public Class getGetterType(String name) {
    return dynamicObject.getGetterType(name);
  }

  public boolean hasSetter(String name) {
    return dynamicObject.hasSetter(name);
  }

  public boolean hasGetter(String name) {
    return dynamicObject.hasGetter(name);
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
      return dynamicObject.get(prop);
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
            dynamicObject.set(prop, newObject);
          } catch (Exception e) {
            throw new ReflectionException("Cannot set value of property '" + name + "' because '" + name + "' is null and cannot be instantiated on instance of " + type.getName() + ". Cause:" + e.toString(), e);
          }
        }
      }
      metaValue.setValue(prop.getChildren(), value);
    } else {
      dynamicObject.set(prop, value);
    }
  }

  public MetaObject metaObjectForProperty(String name) {
    Object value = getValue(name);
    return MetaObject.forObject(value);
  }

  public ObjectWrapper getDynamicObject() {
    return dynamicObject;
  }

  private static class NullObject {
  }

}
