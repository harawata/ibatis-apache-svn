package org.apache.ibatis.reflection;

import java.util.*;

public class HybridDynamicObject implements DynamicObject {

  private Object object;
  private HashMap dynamicProperties;
  private DynamicObject dynamicMap;
  private DynamicObject dynamicBean;

  public HybridDynamicObject(Object object) {
    this.object = object;
    this.dynamicBean = MetaObject.forObject(object).getDynamicObject();
    this.dynamicProperties = new HashMap();
    this.dynamicMap = MetaObject.forObject(dynamicProperties).getDynamicObject();
  }

  public Object getObject() {
    return object;
  }

  public Object get(PropertyTokenizer prop) {
    if (dynamicBean.hasGetter(prop.getName())) {
      return dynamicBean.get(prop);
    } else {
      return dynamicMap.get(prop);
    }
  }

  public void set(PropertyTokenizer prop, Object value) {
    if (dynamicBean.hasSetter(prop.getName())) {
      dynamicBean.set(prop,value);
    } else {
      dynamicMap.set(prop,value);
    }
  }

  public String findProperty(String name) {
    if (dynamicBean.hasGetter(name)) {
      return dynamicBean.findProperty(name);
    } else {
      return dynamicMap.findProperty(name);
    }
  }

  public String[] getGetterNames() {
    String[] beanGetters = dynamicBean.getGetterNames();
    String[] mapGetters = dynamicMap.getGetterNames();
    String []result = new String[beanGetters.length+mapGetters.length];
    System.arraycopy(beanGetters, 0, result, 0, beanGetters.length);
    System.arraycopy(mapGetters, 0, result, beanGetters.length, mapGetters.length);
    return result;
  }

  public String[] getSetterNames() {
    String[] beanSetters = dynamicBean.getSetterNames();
    String[] mapSetters = dynamicMap.getSetterNames();
    String []result = new String[beanSetters.length+mapSetters.length];
    System.arraycopy(beanSetters, 0, result, 0, beanSetters.length);
    System.arraycopy(mapSetters, 0, result, beanSetters.length, mapSetters.length);
    return result;
  }

  public Class getSetterType(String name) {
    if (dynamicBean.hasGetter(name)) {
      return dynamicBean.getSetterType(name);
    } else {
      return dynamicMap.getSetterType(name);
    }
  }

  public Class getGetterType(String name) {
    if (dynamicBean.hasGetter(name)) {
      return dynamicBean.getGetterType(name);
    } else {
      return dynamicMap.getGetterType(name);
    }
  }

  public boolean hasSetter(String name) {
    return true;
  }

  public boolean hasGetter(String name) {
    return true;
  }

}
