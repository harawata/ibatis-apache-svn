/**
 * User: Clinton Begin
 * Date: Apr 11, 2003
 * Time: 11:05:53 PM
 */
package com.ibatis.sqlmap.engine.type;

import java.util.*;
import java.math.*;

public class TypeHandlerFactory {

  private final Map typeHandlerMap = new HashMap();
  private final TypeHandler unknownTypeHandler = new UnknownTypeHandler(this);

  /* Constructor */

  public TypeHandlerFactory() {
    TypeHandler handler;

    handler = new BooleanTypeHandler();
    register(Boolean.class, handler);
    register(boolean.class, handler);

    handler = new ByteTypeHandler();
    register(Byte.class, handler);
    register(byte.class, handler);

    handler = new ShortTypeHandler();
    register(Short.class, handler);
    register(short.class, handler);

    handler = new IntegerTypeHandler();
    register(Integer.class, handler);
    register(int.class, handler);

    handler = new LongTypeHandler();
    register(Long.class, handler);
    register(long.class, handler);

    handler = new FloatTypeHandler();
    register(Float.class, handler);
    register(float.class, handler);

    handler = new DoubleTypeHandler();
    register(Double.class, handler);
    register(double.class, handler);

    register(String.class, new StringTypeHandler());

    register(BigDecimal.class, new BigDecimalTypeHandler());

    register(byte[].class, new ByteArrayTypeHandler());

    register(Object.class, new ObjectTypeHandler());

    register(Date.class, new DateTypeHandler());
    register(Date.class, "DATE", new DateOnlyTypeHandler());
    register(Date.class, "TIME", new TimeOnlyTypeHandler());


    register(java.sql.Date.class, new SqlDateTypeHandler());
    register(java.sql.Time.class, new SqlTimeTypeHandler());
    register(java.sql.Timestamp.class, new SqlTimestampTypeHandler());
  }

  /* Public Methods */

  public TypeHandler getTypeHandler(Class type) {
    return getTypeHandler(type, null);
  }

  public TypeHandler getTypeHandler(Class type, String jdbcType) {
    Map jdbcHandlerMap = (Map) typeHandlerMap.get(type);
    TypeHandler handler = null;
    if (jdbcHandlerMap != null) {
      handler = (TypeHandler) jdbcHandlerMap.get(jdbcType);
      if (handler == null) {
        handler = (TypeHandler) jdbcHandlerMap.get(null);
      }
    }
    return handler;
  }

  public TypeHandler getUnkownTypeHandler() {
    return unknownTypeHandler;
  }


  public boolean hasTypeHandler(Class type) {
    return getTypeHandler(type) != null;
  }

  public void register(Class type, TypeHandler handler) {
    register(type, null, handler);
  }

  public void register(Class type, String jdbcType, TypeHandler handler) {
    Map map = (Map) typeHandlerMap.get(type);
    if (map == null) {
      map = new HashMap();
      typeHandlerMap.put(type, map);
    }
    map.put(jdbcType, handler);
  }
  

}
