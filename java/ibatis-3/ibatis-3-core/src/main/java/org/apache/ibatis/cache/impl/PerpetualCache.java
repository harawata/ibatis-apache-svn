package org.apache.ibatis.cache.impl;

import org.apache.ibatis.cache.impl.BaseCache;

import java.util.*;

public class PerpetualCache extends BaseCache {

  private Map cache = new HashMap();

  public int getSize() {
    return cache.size();
  }

  public void putObject(Object key, Object value) {
    cache.put(key, value);
  }

  public Object getObject(Object key) {
    return cache.get(key);
  }

  public boolean hasKey(Object key) {
    return cache.containsKey(key);
  }

  public Object removeObject(Object key) {
    return cache.remove(key);
  }

  public void clear() {
    cache.clear();
  }

}
