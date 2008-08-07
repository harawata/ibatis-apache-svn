package org.apache.ibatis.cache;

import org.apache.ibatis.cache.decorators.*;
import org.apache.ibatis.cache.impl.PerpetualCache;
import org.junit.*;

import java.util.*;

public class BaseCacheTest {

  @Test
  public void shouldDemonstrateEqualsAndHashCodeForVariousCacheTypes() {
    PerpetualCache cache = new PerpetualCache();
    cache.setId("test_cache");
    Assert.assertTrue(cache.equals(cache));
    Assert.assertTrue(cache.equals(new SynchronizedCache(cache)));
    Assert.assertTrue(cache.equals(new SerializedCache(cache)));
    Assert.assertTrue(cache.equals(new LoggingCache(cache)));
    Assert.assertTrue(cache.equals(new ScheduledCache(cache, 5000)));
    Set<Cache> caches = new HashSet<Cache>();
    caches.add(cache);
    caches.add(new SynchronizedCache(cache));
    caches.add(new SerializedCache(cache));
    caches.add(new LoggingCache(cache));
    caches.add(new ScheduledCache(cache, 5000));
    Assert.assertEquals(1, caches.size());
  }

}
