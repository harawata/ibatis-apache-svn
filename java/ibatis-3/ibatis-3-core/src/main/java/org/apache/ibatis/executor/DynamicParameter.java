package org.apache.ibatis.executor;

import org.apache.ibatis.reflection.MetaObject;

import java.util.*;

public class DynamicParameter {

  private MetaObject metaObject;
  private DynamicParameter parameterObject;
  private Map<String, Object> dynamicProperties;

  public DynamicParameter(DynamicParameter parameterObject) {
    this.metaObject = MetaObject.forObject(parameterObject);
    this.parameterObject = parameterObject;
    this.dynamicProperties = new HashMap<String,Object>();
  }

  public Object getParameterObject() {
    return parameterObject;
  }

  public void setValue(String name, Object value) {
    dynamicProperties.put(name,value);
  }
  
  public Object getValue(String name) {
    if (dynamicProperties.containsKey(name)) {
      return dynamicProperties.get(name);
    } else {
      return metaObject.getValue(name);
    }
  }



}
