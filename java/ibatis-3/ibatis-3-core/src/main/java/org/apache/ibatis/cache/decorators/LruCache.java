package org.apache.ibatis.cache.decorators;

import org.apache.ibatis.cache.Cache;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * Lru (first in, first out) cache decorator
 */
public class LruCache implements Cache {

  private final Cache delegate;

  private final Map keyMap;

  private Object eldestKey;

  public LruCache(Cache delegate) {
    this(delegate, 1024);
  }

  public LruCache(Cache delegate, final int size) {
    this.delegate = delegate;
    keyMap = new LinkedHashMap(size, .75F, true) {
      protected boolean removeEldestEntry(Map.Entry eldest) {
        eldestKey = eldest.getKey();
        return size() > size;
      }
    };
  }

  public String getId() {
    return delegate.getId();
  }

  public int getSize() {
    return delegate.getSize();
  }

  public void putObject(Object key, Object value) {
    cycleKeyList(key);
    delegate.putObject(key, value);
  }

  public Object getObject(Object key) {
    try {
      return delegate.getObject(key);
    } finally {
      cycleKeyList(key);
    }
  }

  public boolean hasKey(Object key) {
    return delegate.hasKey(key);
  }

  public Object removeObject(Object key) {
    return delegate.removeObject(key);
  }

  public void clear() {
    delegate.clear();
    keyMap.clear();
  }

  public ReadWriteLock getReadWriteLock() {
    return delegate.getReadWriteLock();
  }

  private void cycleKeyList(Object key) {
    keyMap.put(key,key);
    if (eldestKey != null) {
      delegate.removeObject(eldestKey);
      eldestKey = null;
    }
  }

}

