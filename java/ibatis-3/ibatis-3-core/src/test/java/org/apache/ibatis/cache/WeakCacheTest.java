package org.apache.ibatis.cache;

import org.apache.ibatis.cache.decorators.SerializedCache;
import org.apache.ibatis.cache.impl.WeakCache;
import org.junit.*;

public class WeakCacheTest {

  @Test
  public void shouldDemonstrateObjectsBeingCollectedAsNeeded() {
    WeakCache cache = new WeakCache();
    for (int i = 0; i < 1000000; i++) {
      cache.putObject(i, i);
    }
    Assert.assertTrue(cache.getSize() < 500000);
  }

  @Test
  public void shouldDemonstrateCopiesAreEqual() {
    Cache cache = new WeakCache();
    cache = new SerializedCache(cache);
    for (int i = 0; i < 1000; i++) {
      cache.putObject(i, i);
      Object value = cache.getObject(i);
      Assert.assertTrue(value == null || value.equals(i));
    }
  }

  @Test
  public void shouldRemoveItemOnDemand() {
    Cache cache = new WeakCache();
    cache.putObject(0, 0);
    Assert.assertNotNull(cache.getObject(0));
    cache.removeObject(0);
    Assert.assertNull(cache.getObject(0));
  }

  @Test
  public void shouldFlushAllItemsOnDemand() {
    Cache cache = new WeakCache();
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
