package org.apache.ibatis.cache.impl;

import org.apache.ibatis.cache.*;

import java.util.concurrent.locks.*;

/**
 * Base Cache class implements a template method pattern for subclasses.
 */
public abstract class BaseCache implements Cache {

  protected String id;

  private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public ReadWriteLock getReadWriteLock() {
    return readWriteLock;
  }

  public boolean equals(Object o) {
    if (getId() == null) throw new CacheException("Cache instances require an ID.");
    if (this == o) return true;
    if (!(o instanceof Cache)) return false;

    Cache otherCache = (Cache) o;

    if (!getId().equals(otherCache.getId())) return false;

    return true;
  }

  public int hashCode() {
    if (getId() == null) throw new CacheException("Cache instances require an ID.");
    return getId().hashCode();
  }
}