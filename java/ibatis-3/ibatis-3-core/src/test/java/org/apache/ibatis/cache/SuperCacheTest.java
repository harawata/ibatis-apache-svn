package org.apache.ibatis.cache;

import org.junit.*;
import org.apache.ibatis.cache.impl.PerpetualCache;
import org.apache.ibatis.cache.decorators.*;

public class SuperCacheTest {

  @Test
  public void shouldDemonstrate5LevelSuperCacheHandlesLotsOfEntriesWithoutCrashing() {
    final int N = 1000000;
    Cache cache = new PerpetualCache();
    cache = new LruCache(cache,500000);
    cache = new FifoCache(cache,500000);
    cache = new SoftCache(cache);
    cache = new WeakCache(cache);
    for (int i = 0; i < N; i++) {
      cache.putObject(i, i);
      Object o = cache.getObject(i);
      Assert.assertTrue(o == null || i == ((Integer) o));
    }
    System.out.println(cache.getSize());
    Assert.assertTrue(cache.getSize() < N);
  }


}
