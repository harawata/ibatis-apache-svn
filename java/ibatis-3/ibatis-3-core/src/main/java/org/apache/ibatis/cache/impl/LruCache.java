package org.apache.ibatis.cache.impl;

import org.apache.ibatis.cache.BaseCache;

import java.util.*;

/**
 * LRU (least recently used) cache controller implementation
 */
public class LruCache extends BaseCache {

  private int size = 256;
  private Map cache = new HashMap();
  private List keyList = new LinkedList();

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public void putObject(Object key, Object value) {
    cache.put(key, value);
    keyList.add(key);
    if (keyList.size() > size) {
      Object oldestKey = keyList.remove(0);
      cache.remove(oldestKey);
    }
  }

  public Object getObject(Object key) {
    Object result = cache.get(key);
    keyList.remove(key);
    if (result != null) {
      keyList.add(key);
    }
    return result;
  }

  public boolean hasKey(Object key) {
    return cache.containsKey(key);
  }

  public Object removeObject(Object key) {
    keyList.remove(key);
    return cache.remove(key);
  }

  public void clear() {
    cache.clear();
    keyList.clear();
  }

}
