package org.apache.ibatis.reflection;

import java.util.List;

public interface ObjectFactory {

  Object create(Class type);

  Object create(Class type, List<Class> constructorArgTypes, List<Object> constructorArgs);

  void setProperty(String name, String value);

}
