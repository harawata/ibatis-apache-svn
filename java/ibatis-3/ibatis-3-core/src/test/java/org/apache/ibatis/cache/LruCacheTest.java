package org.apache.ibatis.cache;

import org.apache.ibatis.cache.impl.LruCache;
import org.junit.*;

public class LruCacheTest {

  @Test
  public void shouldRemoveLeastRecentlyUsedItemInBeyondFiveEntries() {
    LruCache cache = new LruCache();
    cache.setSize(5);
    for (int i = 0; i < 5; i++) {
      cache.putObject(i, i);
    }
    Assert.assertEquals(0, cache.getObject(0));
    cache.putObject(5, 5);
    Assert.assertNull(cache.getObject(1));
    Assert.assertEquals(5, cache.getSize());
  }

  @Test
  public void shouldRemoveItemOnDemand() {
    Cache cache = new LruCache();
    cache.putObject(0, 0);
    Assert.assertNotNull(cache.getObject(0));
    cache.removeObject(0);
    Assert.assertNull(cache.getObject(0));
  }

  @Test
  public void shouldFlushAllItemsOnDemand() {
    Cache cache = new LruCache();
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