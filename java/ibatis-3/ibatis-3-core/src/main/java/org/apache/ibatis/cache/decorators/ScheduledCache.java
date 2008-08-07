package org.apache.ibatis.cache.decorators;

import org.apache.ibatis.cache.*;

public class ScheduledCache extends BaseCache {

  private Cache delegate;
  protected long clearInterval = 60 * 60 * 1000; // 1 hour
  protected long lastClear = System.currentTimeMillis();

  public ScheduledCache(Cache delegate) {
    this.delegate = delegate;
  }

  public ScheduledCache(Cache delegate, int clearInterval) {
    this.delegate = delegate;
    this.clearInterval = clearInterval;
  }

  public long getClearInterval() {
    return clearInterval;
  }

  public void setClearInterval(long clearInterval) {
    this.clearInterval = clearInterval;
  }

  public String getId() {
    return delegate.getId();
  }

  public int getSize() {
    clearWhenStale();
    return delegate.getSize();
  }

  public void putObject(Object key, Object object) {
    clearWhenStale();
    delegate.putObject(key, object);
  }

  public Object getObject(Object key) {
    if (clearWhenStale()) {
      return null;
    } else {
      return delegate.getObject(key);
    }
  }

  public boolean hasKey(Object key) {
    if (clearWhenStale()) {
      return false;
    } else {
      return delegate.hasKey(key);
    }
  }

  public Object removeObject(Object key) {
    clearWhenStale();
    return delegate.removeObject(key);
  }

  public void clear() {
    lastClear = System.currentTimeMillis();
    delegate.clear();
  }

  private boolean clearWhenStale() {
    if (System.currentTimeMillis() - lastClear > clearInterval) {
      clear();
      return true;
    }
    return false;
  }

}
