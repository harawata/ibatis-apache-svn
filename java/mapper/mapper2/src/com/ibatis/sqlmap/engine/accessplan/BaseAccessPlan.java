package com.ibatis.sqlmap.engine.accessplan;

import com.ibatis.common.beans.ClassInfo;

import java.lang.reflect.Method;

/**
 * User: Clinton Begin
 * Date: Nov 27, 2003
 * Time: 9:14:22 PM
 */
public abstract class BaseAccessPlan implements AccessPlan {

  protected Class clazz;
  protected String[] propertyNames;
  protected ClassInfo info;

  BaseAccessPlan(Class clazz, String[] propertyNames) {
    this.clazz = clazz;
    this.propertyNames = propertyNames;
    info = ClassInfo.getInstance(clazz);
  }

  protected Class[] getTypes(String[] propertyNames) {
    Class[] types = new Class[propertyNames.length];
    for (int i = 0; i < propertyNames.length; i++) {
      types[i] = info.getGetterType(propertyNames[i]);
    }
    return types;
  }

  protected Method[] getGetters(String[] propertyNames) {
    Method[] methods = new Method[propertyNames.length];
    for (int i = 0; i < propertyNames.length; i++) {
      methods[i] = info.getGetter(propertyNames[i]);
    }
    return methods;
  }

  protected Method[] getSetters(String[] propertyNames) {
    Method[] methods = new Method[propertyNames.length];
    for (int i = 0; i < propertyNames.length; i++) {
      methods[i] = info.getSetter(propertyNames[i]);
    }
    return methods;
  }

  protected String[] getGetterNames(String[] propertyNames) {
    String[] names = new String[propertyNames.length];
    for (int i = 0; i < propertyNames.length; i++) {
      names[i] = info.getGetter(propertyNames[i]).getName();
    }
    return names;
  }

  protected String[] getSetterNames(String[] propertyNames) {
    String[] names = new String[propertyNames.length];
    for (int i = 0; i < propertyNames.length; i++) {
      names[i] = info.getSetter(propertyNames[i]).getName();
    }
    return names;
  }


}
