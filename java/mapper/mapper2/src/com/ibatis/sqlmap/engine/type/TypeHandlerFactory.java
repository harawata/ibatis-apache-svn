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
    put(Boolean.class, handler);
    put(boolean.class, handler);

    handler = new ByteTypeHandler();
    put(Byte.class, handler);
    put(byte.class, handler);

    handler = new ShortTypeHandler();
    put(Short.class, handler);
    put(short.class, handler);

    handler = new IntegerTypeHandler();
    put(Integer.class, handler);
    put(int.class, handler);

    handler = new LongTypeHandler();
    put(Long.class, handler);
    put(long.class, handler);

    handler = new FloatTypeHandler();
    put(Float.class, handler);
    put(float.class, handler);

    handler = new DoubleTypeHandler();
    put(Double.class, handler);
    put(double.class, handler);

    put(String.class, new StringTypeHandler());

    put(BigDecimal.class, new BigDecimalTypeHandler());

    put(byte[].class, new ByteArrayTypeHandler());

    put(Object.class, new ObjectTypeHandler());

    put(Date.class, new DateTypeHandler());
    put(Date.class, "DATE", new DateOnlyTypeHandler());
    put(Date.class, "TIME", new TimeOnlyTypeHandler());


    put(java.sql.Date.class, new SqlDateTypeHandler());
    put(java.sql.Time.class, new SqlTimeTypeHandler());
    put(java.sql.Timestamp.class, new SqlTimestampTypeHandler());
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

  /* Private Methods */

  private void put(Class type, TypeHandler handler) {
    put(type, null, handler);
  }

  private void put(Class type, String jdbcType, TypeHandler handler) {
    Map map = (Map) typeHandlerMap.get(type);
    if (map == null) {
      map = new HashMap();
      typeHandlerMap.put(type, map);
    }
    map.put(jdbcType, handler);
  }
}
