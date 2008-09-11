package org.apache.ibatis.cache;

import org.apache.ibatis.cache.decorators.*;
import org.apache.ibatis.cache.impl.*;
import org.junit.*;

public class WeakCacheTest {

  @Test
  public void shouldDemonstrateObjectsBeingCollectedAsNeeded() {
    final int N = 300000;
    WeakCache cache = new WeakCache(new PerpetualCache("default"));
    for (int i = 0; i < N; i++) {
      cache.putObject(i, i);
    }
    Assert.assertTrue(cache.getSize() < N);
  }

  @Test
  public void shouldDemonstrateCopiesAreEqual() {
    Cache cache = new WeakCache(new PerpetualCache("default"));
    cache = new SerializedCache(cache);
    for (int i = 0; i < 1000; i++) {
      cache.putObject(i, i);
      Object value = cache.getObject(i);
      Assert.assertTrue(value == null || value.equals(i));
    }
  }

  @Test
  public void shouldRemoveItemOnDemand() {
    WeakCache cache = new WeakCache(new PerpetualCache("default"));
    cache.putObject(0, 0);
    Assert.assertNotNull(cache.getObject(0));
    cache.removeObject(0);
    Assert.assertNull(cache.getObject(0));
  }

  @Test
  public void shouldFlushAllItemsOnDemand() {
    WeakCache cache = new WeakCache(new PerpetualCache("default"));
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
