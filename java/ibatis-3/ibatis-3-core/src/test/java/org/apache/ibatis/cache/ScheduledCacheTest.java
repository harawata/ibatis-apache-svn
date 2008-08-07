package org.apache.ibatis.cache;

import org.apache.ibatis.cache.decorators.*;
import org.apache.ibatis.cache.impl.PerpetualCache;
import org.junit.*;

public class ScheduledCacheTest {

  @Test
  public void shouldDemonstrateHowAllObjectsAreFlushedAfterBasedOnTime() throws Exception {
    Cache cache = new PerpetualCache();
    cache = new ScheduledCache(cache, 2500);
    cache = new LoggingCache(cache);
    for (int i = 0; i < 100; i++) {
      cache.putObject(i, i);
      Assert.assertEquals(i, cache.getObject(i));
    }
    Thread.sleep(5000);
    Assert.assertEquals(0, cache.getSize());
  }

  @Test
  public void shouldRemoveItemOnDemand() {
    Cache cache = new PerpetualCache();
    cache = new ScheduledCache(cache, 60000);
    cache = new LoggingCache(cache);
    cache.putObject(0, 0);
    Assert.assertNotNull(cache.getObject(0));
    cache.removeObject(0);
    Assert.assertNull(cache.getObject(0));
  }

  @Test
  public void shouldFlushAllItemsOnDemand() {
    Cache cache = new PerpetualCache();
    cache = new ScheduledCache(cache, 60000);
    cache = new LoggingCache(cache);
    for (int i = 0; i < 5; i++) {
      cache.putObject(i, i);
    }
    Assert.assertNotNull(cache.getObject(0));
    Assert.assertNotNull(cache.getObject(4));
    cache.clear();
    Assert.assertNull(cache.getObject(0));
    Assert.assertNull(cache.getObject(4));
  }

}