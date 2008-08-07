package org.apache.ibatis.reflection;

import java.lang.reflect.InvocationTargetException;

interface Invoker {
  Object invoke(Object target, Object[] args) throws IllegalAccessException, InvocationTargetException;

  Class getType();
}
