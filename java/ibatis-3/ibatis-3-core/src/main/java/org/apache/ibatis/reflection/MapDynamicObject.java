package org.apache.ibatis.reflection;

import java.util.Map;

public class MapDynamicObject extends BaseDynamicObject {

  private Map map;

  public MapDynamicObject(MetaObject metaObject, Map map) {
    super(metaObject);
    this.map = map;
  }

  public Object get(PropertyTokenizer prop) {
    if (prop.getIndex() != null) {
      Object collection = resolveCollection(prop, map);
      return getCollectionValue(prop, collection);
    } else {
      return map.get(prop.getName());
    }
  }

  public void set(PropertyTokenizer prop, Object value) {
    if (prop.getIndex() != null) {
      Object collection = resolveCollection(prop, map);
      setCollectionValue(prop, collection, value);
    } else {
      map.put(prop.getName(), value);
    }
  }

  public String findProperty(String name) {
    return name;
  }

  public String[] getGetterNames() {
    return (String[]) ((Map) map).keySet().toArray(new String[map.size()]);
  }

  public String[] getSetterNames() {
    return (String[]) ((Map) map).keySet().toArray(new String[map.size()]);
  }

  public Class getSetterType(String name) {
    return map.get(name) == null ? Object.class : map.get(name).getClass();
  }

  public Class getGetterType(String name) {
    return map.get(name) == null ? Object.class : map.get(name).getClass();
  }

  public boolean hasSetter(String name) {
    return true;
  }

  public boolean hasGetter(String name) {
    return true;
  }
}
