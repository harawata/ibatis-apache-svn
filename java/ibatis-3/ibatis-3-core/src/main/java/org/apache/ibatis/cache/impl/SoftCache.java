package org.apache.ibatis.cache.impl;

import org.apache.ibatis.cache.BaseCache;

import javax.util.SoftHashMap;
import java.util.Map;

/**
 * Weak Reference cache implementation
 */
public class SoftCache extends BaseCache {

  private Map cache = new SoftHashMap();

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