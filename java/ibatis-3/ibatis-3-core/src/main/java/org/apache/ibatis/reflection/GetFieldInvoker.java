package org.apache.ibatis.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

class GetFieldInvoker implements Invoker {
  private Field field;

  public GetFieldInvoker(Field field) {
    this.field = field;
  }

  public Object invoke(Object target, Object[] args) throws IllegalAccessException, InvocationTargetException {
    return field.get(target);
  }

  public Class getType() {
    return field.getType();
  }
}
