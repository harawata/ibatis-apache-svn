/**
 * User: Clinton Begin
 * Date: Aug 21, 2003
 * Time: 10:10:17 AM
 */
package com.ibatis.sqlmap.engine.cache;

import java.util.*;

public interface CacheController {

  public void flush(CacheModel cacheModel);

  public Object getObject(CacheModel cacheModel, Object key);

  public Object removeObject(CacheModel cacheModel, Object key);

  public void putObject(CacheModel cacheModel, Object key, Object object);

  public void configure(Properties props);

}
