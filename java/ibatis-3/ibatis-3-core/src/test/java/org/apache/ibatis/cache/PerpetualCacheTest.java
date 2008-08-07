package org.apache.ibatis.cache;

import org.apache.ibatis.cache.decorators.*;
import org.apache.ibatis.cache.impl.PerpetualCache;
import org.junit.*;

public class PerpetualCacheTest {

  @Test
  public void shouldDemonstrateHowAllObjectsAreKept() {
    Cache cache = new PerpetualCache();
    cache = new SynchronizedCache(cache);
    for (int i = 0; i < 100000; i++) {
      cache.putObject(i, i);
      Assert.assertEquals(i, cache.getObject(i));
    }
    Assert.assertEquals(100000, cache.getSize());
  }

  @Test
  public void shouldDemonstrateCopiesAreEqual() {
    Cache cache = new PerpetualCache();
    cache = new SerializedCache(cache);
    for (int i = 0; i < 1000; i++) {
      cache.putObject(i, i);
      Assert.assertEquals(i, cache.getObject(i));
    }
  }

  @Test
  public void shouldRemoveItemOnDemand() {
    Cache cache = new PerpetualCache();
    cache = new SynchronizedCache(cache);
    cache.putObject(0, 0);
    Assert.assertNotNull(cache.getObject(0));
    cache.removeObject(0);
    Assert.assertNull(cache.getObject(0));
  }

  @Test
  public void shouldFlushAllItemsOnDemand() {
    Cache cache = new PerpetualCache();
    cache = new SynchronizedCache(cache);
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