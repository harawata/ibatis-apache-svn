package org.apache.ibatis.cache;

import org.apache.ibatis.cache.decorators.*;
import org.apache.ibatis.cache.impl.*;
import org.junit.*;

public class SoftCacheTest {

  @Test //@Ignore("Until we can find a reliable way to test it on all machines.")
  public void shouldDemonstrateObjectsBeingCollectedAsNeeded() throws Exception {
    SoftCache cache = new SoftCache(new PerpetualCache());
    int n = 100000;
    for (int i = 0; i < n; i++) {
      byte[] array = new byte[5001]; //waste a bunch of memory
      array[5000] = 1;
      cache.putObject(i, array);
      Object value = cache.getObject(i);
    }
    System.out.println(cache.getSize());
    Assert.assertTrue(cache.getSize() < n);
  }

  @Test
  public void shouldDemonstrateCopiesAreEqual() {
    Cache cache = new SoftCache(new PerpetualCache());
    cache = new SerializedCache(cache);
    for (int i = 0; i < 1000; i++) {
      cache.putObject(i, i);
      Object value = cache.getObject(i);
      Assert.assertTrue(value == null || value.equals(i));
    }
  }

  @Test
  public void shouldRemoveItemOnDemand() {
    Cache cache = new SoftCache(new PerpetualCache());
    cache.putObject(0, 0);
    Assert.assertNotNull(cache.getObject(0));
    cache.removeObject(0);
    Assert.assertNull(cache.getObject(0));
  }

  @Test
  public void shouldFlushAllItemsOnDemand() {
    Cache cache = new SoftCache(new PerpetualCache());
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