/**
 * User: Clinton Begin
 * Date: Aug 21, 2003
 * Time: 3:36:14 PM
 */
package com.ibatis.sqlmap.engine.cache.fifo;

import com.ibatis.sqlmap.engine.cache.*;

import java.util.*;

/**
 * FIFO (first in, first out) cache controller implementation
 */
public class FifoCacheController implements CacheController {

  private int cacheSize;
  private Map cache;
  private List keyList;

  /**
   * Default constructor
   */
  public FifoCacheController() {
    this.cacheSize = 100;
    this.cache = Collections.synchronizedMap(new HashMap());
    this.keyList = Collections.synchronizedList(new ArrayList());
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
      cache.put(key, value);
      keyList.add(key);
      if (keyList.size() > cacheSize) {
        Object oldestKey = keyList.remove(0);
        cache.remove(oldestKey);
      }
  }

  /** Get an object out of the cache.
   * @param cacheModel The cache model
   * @param key The key of the object to be returned
   * @return The cached object (or null)
   */
  public Object getObject(CacheModel cacheModel, Object key) {
      return cache.get(key);
  }

  public Object removeObject(CacheModel cacheModel, Object key) {
    return cache.remove(key);
  }

  /**
   * Flushes the cache.
   * @param cacheModel The cache model
   */
  public void flush(CacheModel cacheModel) {
      cache.clear();
      keyList.clear();
  }

}
