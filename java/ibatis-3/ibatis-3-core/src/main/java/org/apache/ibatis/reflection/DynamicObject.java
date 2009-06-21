package org.apache.ibatis.reflection;

public interface DynamicObject {

  Object get(PropertyTokenizer prop);

  void set(PropertyTokenizer prop, Object value);

  String findProperty(String name);

  String[] getGetterNames();

  String[] getSetterNames();

  Class getSetterType(String name);

  Class getGetterType(String name);

  boolean hasSetter(String name);

  boolean hasGetter(String name);
  
}
