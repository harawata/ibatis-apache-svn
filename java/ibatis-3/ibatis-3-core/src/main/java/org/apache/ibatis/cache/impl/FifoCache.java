package org.apache.ibatis.cache.impl;

import org.apache.ibatis.cache.impl.BaseCache;

import java.util.*;

/**
 * FIFO (first in, first out) cache controller implementation
 */
public class FifoCache extends BaseCache {

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
    return cache.get(key);
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
