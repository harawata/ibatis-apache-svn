/**
 * User: Clinton Begin
 * Date: Aug 21, 2003
 * Time: 10:10:17 AM
 */
package com.ibatis.sqlmap.engine.cache;
import java.util.*;
/**
 * Cache controller (implementation) interface
 */
public interface CacheController {
  
  /**
   * Flush a cache model
   * @param cacheModel - the model to flush
   */
  public void flush(CacheModel cacheModel);
  
  /**
   * Get an object from a cache model
   * @param cacheModel - the model
   * @param key - the key to the object
   * @return the object if in the cache, or null(?)
   */
  public Object getObject(CacheModel cacheModel, Object key);
  
  /**
   * Remove an object from a cache model
   * @param cacheModel - the model to remove the object from
   * @param key - the key to the object
   * @return the removed object(?)
   */
  public Object removeObject(CacheModel cacheModel, Object key);

  /**
   * Put an object into a cache model
   * @param cacheModel - the model to add the object to
   * @param key - the key to the object
   * @param object - the object to add
   */
  public void putObject(CacheModel cacheModel, Object key, Object object);
  
  /**
   * Configure a cache controller
   * @param props - the properties object continaing configuration information
   */
  public void configure(Properties props);
  
}