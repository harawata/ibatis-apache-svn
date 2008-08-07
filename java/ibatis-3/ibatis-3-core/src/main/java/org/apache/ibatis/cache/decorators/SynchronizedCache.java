package org.apache.ibatis.cache.decorators;

import org.apache.ibatis.cache.*;

public class SynchronizedCache extends BaseCache {

  private Cache delegate;

  public SynchronizedCache(Cache delegate) {
    this.delegate = delegate;
  }

  public String getId() {
    return delegate.getId();
  }

  public synchronized int getSize() {
    return delegate.getSize();
  }

  public synchronized void putObject(Object key, Object object) {
    delegate.putObject(key, object);
  }

  public synchronized Object getObject(Object key) {
    return delegate.getObject(key);
  }

  public synchronized boolean hasKey(Object key) {
    return delegate.hasKey(key);
  }

  public synchronized Object removeObject(Object key) {
    return delegate.removeObject(key);
  }

  public synchronized void clear() {
    delegate.clear();
  }

}
