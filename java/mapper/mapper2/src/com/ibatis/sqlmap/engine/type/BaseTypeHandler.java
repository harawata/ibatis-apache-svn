package com.ibatis.sqlmap.engine.type;

/**
 * User: Clinton
 * Date: 1-Aug-2004
 * Time: 3:36:14 PM
 */
public abstract class BaseTypeHandler implements TypeHandler {

  public boolean equals(Object object, String string) {
    if (object == null || string == null) {
      return object == string;
    } else {
      Object castedObject = valueOf(string);
      return object.equals(castedObject);
    }
  }

}
