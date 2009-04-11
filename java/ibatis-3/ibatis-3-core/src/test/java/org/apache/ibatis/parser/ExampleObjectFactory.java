package org.apache.ibatis.parser;

import org.apache.ibatis.reflection.DefaultObjectFactory;

import java.util.*;

public class ExampleObjectFactory extends DefaultObjectFactory {

  public Object create(Class type) {
    return super.create(type);
  }

  public Object create(Class type, List<Class> constructorArgTypes, List<Object> constructorArgs) {
    return super.create(type, constructorArgTypes, constructorArgs);
  }

  public void setProperties(Properties properties) {
    super.setProperties(properties);
  }

}
