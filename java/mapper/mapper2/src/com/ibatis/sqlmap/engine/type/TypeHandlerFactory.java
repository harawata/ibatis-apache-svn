/*
 *  Copyright 2004 Clinton Begin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.ibatis.sqlmap.engine.type;

import com.ibatis.sqlmap.client.SqlMapException;

import java.math.BigDecimal;
import java.util.*;

public class TypeHandlerFactory {

  private final Map typeHandlerMap = new HashMap();
  private final TypeHandler unknownTypeHandler = new UnknownTypeHandler(this);
  private HashMap typeAliases = new HashMap();


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
    register(Object.class, "OBJECT", new ObjectTypeHandler());

    register(Date.class, new DateTypeHandler());
    register(Date.class, "DATE", new DateOnlyTypeHandler());
    register(Date.class, "TIME", new TimeOnlyTypeHandler());


    register(java.sql.Date.class, new SqlDateTypeHandler());
    register(java.sql.Time.class, new SqlTimeTypeHandler());
    register(java.sql.Timestamp.class, new SqlTimestampTypeHandler());


    putTypeAlias("string", String.class.getName());
    putTypeAlias("byte", Byte.class.getName());
    putTypeAlias("long", Long.class.getName());
    putTypeAlias("short", Short.class.getName());
    putTypeAlias("int", Integer.class.getName());
    putTypeAlias("integer", Integer.class.getName());
    putTypeAlias("double", Double.class.getName());
    putTypeAlias("float", Float.class.getName());
    putTypeAlias("boolean", Boolean.class.getName());
    putTypeAlias("date", Date.class.getName());
    putTypeAlias("decimal", BigDecimal.class.getName());
    putTypeAlias("object", Object.class.getName());
    putTypeAlias("map", Map.class.getName());
    putTypeAlias("hashmap", HashMap.class.getName());
    putTypeAlias("list", List.class.getName());
    putTypeAlias("arraylist", ArrayList.class.getName());
    putTypeAlias("collection", Collection.class.getName());
    putTypeAlias("iterator", Iterator.class.getName());

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


  public String resolveAlias(String string) {
    String newString = null;
    if (typeAliases.containsKey(string)) {
      newString = (String) typeAliases.get(string);
    }
    if (newString != null) {
      string = newString;
    }
    return string;
  }

  public void putTypeAlias(String alias, String value) {
    if (typeAliases.containsKey(alias)) {
      throw new SqlMapException("Error in XmlSqlMapClientBuilder.  Alias name conflict occurred.  The alias '" + alias + "' is already mapped to the value '" + typeAliases.get(alias) + "'.");
    }
    typeAliases.put(alias, value);
  }

}
