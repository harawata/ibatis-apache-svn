package org.apache.ibatis.reflection;

import java.lang.reflect.Constructor;
import java.util.*;

public class DefaultObjectFactory implements ObjectFactory {

  public Object create(Class type) {
    return create(type, null, null);
  }

  public Object create(Class type, List<Class> constructorArgTypes, List<Object> constructorArgs) {
    Class classToCreate = resolveCollectionInterface(type);
    return instantiateClass(classToCreate, constructorArgTypes, constructorArgs);
  }

  public void setProperty(String name, String value) {
    // no props for default
  }

  private Object instantiateClass(Class type, List<Class> constructorArgTypes, List<Object> constructorArgs) {
    try {
      Constructor constructor;
      if (constructorArgTypes == null || constructorArgs == null) {
        constructor = type.getDeclaredConstructor();
        if (!constructor.isAccessible()) {
          constructor.setAccessible(true);
        }
        return constructor.newInstance();
      } else {
        constructor = type.getDeclaredConstructor(constructorArgTypes.toArray(new Class[constructorArgTypes.size()]));
        if (!constructor.isAccessible()) {
          constructor.setAccessible(true);
        }
        return constructor.newInstance(constructorArgs.toArray(new Object[constructorArgs.size()]));
      }
    } catch (Exception e) {
      throw new RuntimeException("Error instantiating " + type + ". Cause: " + e, e);
    }
  }


  private Class resolveCollectionInterface(Class type) {
    Class classToCreate;
    if (type == List.class || type == Collection.class) {
      classToCreate = ArrayList.class;
    } else if (type == Map.class) {
      classToCreate = HashMap.class;
    } else if (type == Set.class) {
      classToCreate = HashSet.class;
    } else {
      classToCreate = type;
    }
    return classToCreate;
  }

}
