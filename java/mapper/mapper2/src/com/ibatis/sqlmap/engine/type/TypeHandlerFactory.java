/**
 * User: Clinton Begin
 * Date: Apr 11, 2003
 * Time: 11:05:53 PM
 */
package com.ibatis.sqlmap.engine.type;

import java.util.*;
import java.math.*;

public class TypeHandlerFactory {

  private static final Map TYPE_HANDLER_MAP = new HashMap();
  private static final TypeHandler UNKNOWN_TYPE_HANDLER = new UnknownTypeHandler();

  /* Constructor */

  private TypeHandlerFactory() {
  }

  /* Static Initializer */

  static {
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

  public static TypeHandler getTypeHandler(Class type) {
    return getTypeHandler(type, null);
  }

  public static TypeHandler getTypeHandler(Class type, String jdbcType) {
    Map jdbcHandlerMap = (Map) TYPE_HANDLER_MAP.get(type);
    TypeHandler handler = null;
    if (jdbcHandlerMap != null) {
      handler = (TypeHandler) jdbcHandlerMap.get(jdbcType);
      if (handler == null) {
        handler = (TypeHandler) jdbcHandlerMap.get(null);
      }
    }
    return handler;
  }

  public static TypeHandler getUnkownTypeHandler() {
    return UNKNOWN_TYPE_HANDLER;
  }


  public static boolean hasTypeHandler(Class type) {
    return getTypeHandler(type) != null;
  }

  /* Private Methods */

  private static void put(Class type, TypeHandler handler) {
    put(type, null, handler);
  }

  private static void put(Class type, String jdbcType, TypeHandler handler) {
    Map map = (Map) TYPE_HANDLER_MAP.get(type);
    if (map == null) {
      map = new HashMap();
      TYPE_HANDLER_MAP.put(type, map);
    }
    map.put(jdbcType, handler);
  }
}
