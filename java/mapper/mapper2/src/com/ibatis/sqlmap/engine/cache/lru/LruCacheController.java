/**
 * User: Clinton Begin
 * Date: Aug 21, 2003
 * Time: 3:18:33 PM
 */
package com.ibatis.sqlmap.engine.cache.lru;

import com.ibatis.sqlmap.engine.cache.*;

import java.util.*;

public class LruCacheController implements CacheController {

  private int cacheSize;
  private Map cache;
  private List keyList;

  public LruCacheController() {
    this.cacheSize = 100;
    this.cache = new HashMap();
    this.keyList = new ArrayList();
  }

  /**
   * Configures the cache
   * @param props Optionally can contain properties [reference-type=WEAK|SOFT|STRONG]
   */
  public void configure(Properties props) {
    String size = props.getProperty("cache-size");
    if (size == null) {
      size = props.getProperty("size");
    }
    if (size != null) {
      cacheSize = Integer.parseInt(size);
    }
  }

  /** Add an object to the cache
   * @param cacheModel The cacheModel
   * @param key The key of the object to be cached
   * @param value The object to be cached
   */
  public void putObject(CacheModel cacheModel, Object key, Object value) {
    synchronized (this) {
      cache.put(key, value);
      keyList.add(key);
      if (keyList.size() > cacheSize) {
        Object oldestKey = keyList.remove(0);
        cache.remove(oldestKey);
      }
    }
  }

  /** Get an object out of the cache.
   * @param cacheModel The cache model
   * @param key The key of the object to be returned
   * @return The cached object (or null)
   */
  public Object getObject(CacheModel cacheModel, Object key) {
    synchronized (this) {
      keyList.remove(key);
      keyList.add(key);
      return cache.get(key);
    }
  }

  public Object removeObject(CacheModel cacheModel, Object key) {
    synchronized (this) {
      keyList.remove(key);
      keyList.add(key);
      return cache.remove(key);
    }
  }

  /**
   * Flushes the cache.
   * @param cacheModel The cache model
   */
  public void flush(CacheModel cacheModel) {
    synchronized (this) {
      cache.clear();
      keyList.clear();
    }
  }

}
